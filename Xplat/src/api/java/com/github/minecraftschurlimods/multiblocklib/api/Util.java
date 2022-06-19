package com.github.minecraftschurlimods.multiblocklib.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Util {
    private static final Pattern BS = Pattern.compile("^((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\\[((?:\\w+=\\w+,)*\\w+=\\w+)])?(\\{.*})?$");
    public static final Codec<BlockStateParser.BlockResult> BLOCK_STATE_CODEC = Codec.either(
            Registry.BLOCK.byNameCodec().dispatch("block", o -> o.blockState().getBlock(), Util::blockStateCodecFor),
            Codec.STRING.flatXmap(Util::parse, Util::stringify)
    ).xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);
    public static final Codec<Character> CHAR_CODEC = Codec.STRING.flatXmap(s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error("Expected single character not string"), c -> DataResult.success(String.valueOf(c)));

    private static <T, V extends Comparable<V>> DataResult<Pair<Property<?>, Comparable<?>>> decode(final DynamicOps<T> ops, final Pair<T, T> pair, final Codec<Property<?>> keyCodec) {
        DataResult<Property<V>> key = keyCodec.parse(ops, pair.getFirst()).map(property -> (Property<V>) property);
        DataResult<V> value = key.flatMap(vProperty -> vProperty.codec().parse(ops, pair.getSecond()));
        return key.apply2stable(Pair::of, value);
    }

    @NotNull
    private static DataResult<String> stringify(BlockStateParser.BlockResult blockResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(Registry.BLOCK.getKey(blockResult.blockState().getBlock()));
        if (!blockResult.properties().isEmpty()) {
            sb.append("[");
            sb.append(blockResult.properties().entrySet().stream().map(e -> e.getKey().getName() + "=" + ((Property)e.getKey()).getName(e.getValue())).collect(Collectors.joining(",")));
            sb.append("]");
        }
        if (blockResult.nbt() != null) {
            sb.append(blockResult.nbt());
        }
        return DataResult.success(sb.toString());
    }

    private static DataResult<BlockStateParser.BlockResult> parse(String s) {
        Matcher matcher = BS.matcher(s);
        String blockName = matcher.group(1);
        String properties = matcher.group(2);
        String nbt = matcher.group(3);
        if (blockName == null) {
            return DataResult.error("Invalid block name");
        }
        Block block = Registry.BLOCK.get(new ResourceLocation(blockName));
        BlockState state = block.defaultBlockState();
        final Map<Property<?>, Comparable<?>> propertiesMap = new HashMap<>();
        if (properties != null) {
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String propertyString : properties.split(",")) {
                String[] split = propertyString.split("=");
                Property<?> property = stateDefinition.getProperty(split[0]);
                if (property == null) {
                    return DataResult.error(String.format("Unknown property '%s'", split[0]));
                }
                Optional<?> value = property.getValue(split[1]);
                if (value.isEmpty()) {
                    return DataResult.error(String.format("Unknown value '%s' for property '%s'", split[1], split[0]));
                }
                propertiesMap.put(property, (Comparable<?>) value.get());
                state = modifyState(state, property, value.get());
            }
        }
        CompoundTag nbtTag = null;
        if (nbt != null) {
            try {
                nbtTag = TagParser.parseTag(nbt);
            } catch (CommandSyntaxException e) {
                return DataResult.error(e.getMessage());
            }
        }
        return DataResult.success(new BlockStateParser.BlockResult(state, propertiesMap, nbtTag));
    }

    public static <T extends Comparable<T>> BlockState modifyState(BlockState state, Property<?> property, Object value) {
        return state.setValue((Property<T>) property, (T) value);
    }

    private static Codec<BlockStateParser.BlockResult> blockStateCodecFor(Block block) {
        BlockState blockState = block.defaultBlockState();
        if (blockState.getValues().isEmpty()) {
            return Codec.unit(new BlockStateParser.BlockResult(blockState, new HashMap<>(), null));
        }
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        Codec<Property<?>> keyCodec = Codec.STRING.flatXmap(s -> {
            Property<?> property = stateDefinition.getProperty(s);
            if (property == null) {
                return DataResult.error(String.format("Unknown property '%s'", s));
            }
            return DataResult.success(property);
        }, property -> DataResult.success(property.getName()));
        MapCodec<Map<Property<?>, Comparable<?>>> propertyCodec = new MapCodec<>() {
            @Override
            public <T> RecordBuilder<T> encode(Map<Property<?>, Comparable<?>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                for (final Map.Entry<Property<?>, Comparable<?>> entry : input.entrySet()) {
                    prefix.add(keyCodec.encodeStart(ops, entry.getKey()), ((Codec<Comparable<?>>) entry.getKey().codec()).encodeStart(ops, entry.getValue()));
                }
                return prefix;
            }

            @Override
            public <T> DataResult<Map<Property<?>, Comparable<?>>> decode(DynamicOps<T> ops, MapLike<T> input) {
                ImmutableMap.Builder<Property<?>, Comparable<?>> read = ImmutableMap.builder();
                ImmutableList.Builder<Pair<T, T>> failed = ImmutableList.builder();
                DataResult<Unit> result = input.entries().reduce(
                        DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                        (r, pair) -> {
                            final DataResult<Pair<Property<?>, Comparable<?>>> entry = Util.decode(ops, pair, keyCodec);
                            entry.error().ifPresent(e -> failed.add(pair));

                            return r.apply2stable((u, p) -> {
                                read.put(p.getFirst(), p.getSecond());
                                return u;
                            }, entry);
                        },
                        (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
                );
                final Map<Property<?>, Comparable<?>> elements = read.build();
                final T errors = ops.createMap(failed.build().stream());

                return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return stateDefinition.getProperties().stream().map(Property::getName).map(ops::createString);
            }
        };
        return propertyCodec.fieldOf("properties").codec().xmap(properties -> new BlockStateParser.BlockResult(blockState, properties, null), BlockStateParser.BlockResult::properties);
    }
}

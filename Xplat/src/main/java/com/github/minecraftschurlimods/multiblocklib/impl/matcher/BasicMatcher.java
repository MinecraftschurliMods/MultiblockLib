package com.github.minecraftschurlimods.multiblocklib.impl.matcher;

import com.github.minecraftschurlimods.multiblocklib.Util;
import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;
import java.util.stream.Collectors;

public class BasicMatcher implements StateMatcher {
    public static final Codec<BasicMatcher> LOOSE_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(basicMatcher -> basicMatcher.state.getBlock())
    ).apply(inst, block -> new BasicMatcher(block.defaultBlockState(), false, Init.LOOSE_BLOCK_MATCHER.getId())));
    public static final Codec<BasicMatcher> STRICT_BLOCK = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(basicMatcher -> basicMatcher.state.getBlock())
    ).apply(inst, block -> new BasicMatcher(block.defaultBlockState(), true, Init.STRICT_BLOCK_MATCHER.getId())));
    public static final Codec<BasicMatcher> STRICT_STATE = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.fieldOf("state").forGetter(basicMatcher -> basicMatcher.state)
    ).apply(inst, block -> new BasicMatcher(block, true, Init.STRICT_STATE_MATCHER.getId())));
    public static final Codec<BasicMatcher> FILTERED_STATE = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.fieldOf("state").forGetter(basicMatcher -> basicMatcher.state),
            Codec.STRING.listOf().<Set<String>>xmap(HashSet::new, ArrayList::new).fieldOf("properties").forGetter(basicMatcher -> basicMatcher.properties.stream().map(Property::getName).collect(Collectors.toSet()))
    ).apply(inst, (block, props) -> new BasicMatcher(block, true, Init.FILTERED_STATE_MATCHER.getId())));

    private final BlockState state;
    private final boolean strict;
    private final ResourceLocation type;
    private final Set<Property<?>> properties;

    public BasicMatcher(BlockState state, boolean strict, ResourceLocation type) {
        this(state, strict, type, Set.of());
    }

    public BasicMatcher(BlockState state, boolean strict, ResourceLocation type, Set<String> properties) {
        this.state = state;
        this.strict = strict;
        this.type = type;
        this.properties = properties.isEmpty() ? new HashSet<>(state.getProperties()) : state.getProperties().stream().filter(property -> properties.contains(property.getName())).collect(Collectors.toSet());
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

    @Override
    public BlockState displayState(final long gameTime) {
        return state;
    }

    @Override
    public boolean test(final BlockStateMatchContext context) {
        if (!strict) {
            return context.state().getBlock() == state.getBlock();
        }
        return properties.stream().allMatch(property -> state.hasProperty(property) && context.state().hasProperty(property) && Objects.equals(state.getValue(property), context.state().getValue(property)));
    }
}

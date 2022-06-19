package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TagMatcher implements StateMatcher {
    public static final Codec<TagMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("tag").forGetter(TagMatcher::tag),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("properties").forGetter(TagMatcher::properties),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(TagMatcher::nbt)
    ).apply(inst, TagMatcher::new));
    private final HolderSet<Block> tag;
    private final Optional<Map<String, String>> properties;
    private final Optional<CompoundTag> nbt;
    private BlockState[] displayStateCache;

    public TagMatcher(HolderSet<Block> tag, Optional<Map<String, String>> properties, Optional<CompoundTag> nbt) {
        this.tag = tag;
        this.properties = properties;
        this.nbt = nbt;
        this.displayStateCache = new BlockState[tag.size()];
    }

    @Override
    public Codec<? extends StateMatcher> codec() {
        return CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        if (tag().size() == 0) {
            return Blocks.BARRIER.defaultBlockState();
        }
        if (tag().size() != displayStateCache.length) {
            displayStateCache = new BlockState[tag().size()];
        }
        int index = tag().size() > 1 ? (int) (gameTime / 20 % tag().size()) : 0;
        if (displayStateCache[index] == null) {
            Holder<Block> blockHolder = tag.get(index);
            Block block = blockHolder.value();
            BlockState state = block.defaultBlockState();
            if (properties().isPresent()) {
                StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
                for (Map.Entry<String, String> entry : properties().get().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Property<?> property = stateDefinition.getProperty(key);
                    if (property != null) {
                        Optional<?> optional = property.getValue(value);
                        if (optional.isPresent()) {
                            state = Util.modifyState(state, property, optional.get());
                        }
                    }
                }
            }
            displayStateCache[index] = state;
        }
        return displayStateCache[index];
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        if (!context.state().is(tag())) {
            return false;
        }
        if (properties().isPresent()) {
            StateDefinition<Block, BlockState> stateDefinition = context.state().getBlock().getStateDefinition();
            for (Map.Entry<String, String> entry : properties().get().entrySet()) {
                Property<?> property = stateDefinition.getProperty(entry.getKey());
                if (property == null) {
                    continue;
                }
                Optional<?> value = property.getValue(entry.getValue());
                if (value.isEmpty()) {
                    continue;
                }
                if (!context.state().getValue(property).equals(value.get())) {
                    return false;
                }
            }
        }
        if (nbt().isPresent()) {
            BlockEntity blockEntity = context.blockEntity();
            return blockEntity != null && NbtUtils.compareNbt(nbt().get(), blockEntity.saveWithFullMetadata(), true);
        }
        return true;
    }

    public HolderSet<Block> tag() {
        return tag;
    }

    public Optional<Map<String, String>> properties() {
        return properties;
    }

    public Optional<CompoundTag> nbt() {
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TagMatcher) obj;
        return Objects.equals(this.tag, that.tag) &&
                Objects.equals(this.properties, that.properties) &&
                Objects.equals(this.nbt, that.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, properties, nbt);
    }

    @Override
    public String toString() {
        return "TagMatcher[" +
                "tag=" + tag + ", " +
                "properties=" + properties + ", " +
                "nbt=" + nbt + ']';
    }

}

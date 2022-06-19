package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record BlockStateMatcher(BlockStateParser.BlockResult state, Set<String> match) implements StateMatcher {
    public static final Codec<BlockStateMatcher> STRICT_STATE = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.fieldOf("state").forGetter(BlockStateMatcher::state)
    ).apply(inst, BlockStateMatcher::new));
    public static final Codec<BlockStateMatcher> FILTERED_STATE = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.fieldOf("state").forGetter(BlockStateMatcher::state),
            Codec.STRING.listOf().<Set<String>>xmap(HashSet::new, ArrayList::new).fieldOf("match").forGetter(BlockStateMatcher::match)
    ).apply(inst, BlockStateMatcher::new));

    public BlockStateMatcher(BlockStateParser.BlockResult state) {
        this(state, Set.of());
    }

    @Override
    public BlockState displayState(long gameTime) {
        return state.blockState();
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        if (!context.state().is(state.blockState().getBlock())) {
            return false;
        }
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.properties().entrySet()) {
            Property<?> property = entry.getKey();
            if (!match().isEmpty() && !match().contains(property.getName())) {
                continue;
            }
            if (!context.state().getValue(property).equals(entry.getValue())) {
                return false;
            }
        }
        if (state.nbt() != null) {
            BlockEntity blockEntity = context.blockEntity();
            return blockEntity != null && NbtUtils.compareNbt(state.nbt(), blockEntity.saveWithFullMetadata(), true);
        }
        return true;
    }

    @Override
    public Codec<? extends StateMatcher> codec() {
        return match().isEmpty() ? STRICT_STATE : FILTERED_STATE;
    }
}

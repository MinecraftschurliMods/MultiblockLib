package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record DisplayOnlyMatcher(BlockState displayState) implements StateMatcher {
    public static final Codec<DisplayOnlyMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Util.BLOCK_STATE_CODEC.xmap(BlockStateParser.BlockResult::blockState, state -> new BlockStateParser.BlockResult(state, state.getValues(), null)).fieldOf("display").forGetter(displayOnlyMatcher -> displayOnlyMatcher.displayState)
    ).apply(inst, DisplayOnlyMatcher::new));

    @Override
    public Codec<? extends StateMatcher> codec() {
        return CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        return displayState();
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        return true;
    }
}

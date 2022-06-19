package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BlockMatcher(Block block, boolean strict) implements StateMatcher {
    public static final Codec<BlockMatcher> STRICT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockMatcher::block)
    ).apply(inst, (Block block) -> new BlockMatcher(block, true)));
    public static final Codec<BlockMatcher> LOOSE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockMatcher::block)
    ).apply(inst, (Block block) -> new BlockMatcher(block, true)));

    @Override
    public Codec<? extends StateMatcher> codec() {
        return strict() ? STRICT_CODEC : LOOSE_CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        return block().defaultBlockState();
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        return strict() ? context.state() == block().defaultBlockState() : context.state().is(block());
    }
}

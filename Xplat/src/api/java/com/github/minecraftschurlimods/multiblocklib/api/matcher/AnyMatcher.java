package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record AnyMatcher() implements StateMatcher {
    public static final Codec<AnyMatcher> CODEC = Codec.unit(AnyMatcher::new);

    @Override
    public Codec<? extends StateMatcher> codec() {
        return CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean test(final BlockStateMatchContext context) {
        return true;
    }
}

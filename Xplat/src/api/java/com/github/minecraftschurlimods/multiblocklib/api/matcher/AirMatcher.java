package com.github.minecraftschurlimods.multiblocklib.api.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record AirMatcher() implements StateMatcher {
    public static final Codec<AirMatcher> CODEC = Codec.unit(AirMatcher::new);

    @Override
    public Codec<? extends StateMatcher> codec() {
        return CODEC;
    }

    @Override
    public BlockState displayState(long gameTime) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean test(BlockStateMatchContext context) {
        return context.state().isAir();
    }

    @Override
    public boolean isAir() {
        return true;
    }
}

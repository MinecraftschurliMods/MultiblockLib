package com.github.minecraftschurlimods.multiblocklib.impl.matcher;

import com.github.minecraftschurlimods.multiblocklib.api.BlockStateMatchContext;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AirMatcher implements StateMatcher {
    public static final Codec<AirMatcher> CODEC = Codec.unit(AirMatcher::new);

    @Override
    public ResourceLocation getType() {
        return Init.AIR_MATCHER.getId();
    }

    @Override
    public BlockState displayState(final long gameTime) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean test(final BlockStateMatchContext context) {
        return context.state().isAir();
    }
}

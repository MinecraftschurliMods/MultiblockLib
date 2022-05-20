package com.github.minecraftschurlimods.multiblocklib.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockStateMatchContext {
    BlockPos pos();
    BlockState state();
    BlockGetter level();
    <T> T getAdditionalData(String key);
    <T> void withAdditionalData(String key, T value);
}

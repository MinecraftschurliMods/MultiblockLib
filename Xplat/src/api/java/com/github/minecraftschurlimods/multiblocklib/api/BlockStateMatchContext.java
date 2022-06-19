package com.github.minecraftschurlimods.multiblocklib.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public interface BlockStateMatchContext {
    BlockPos pos();
    BlockState state();
    FluidState fluidState();
    @Nullable BlockEntity blockEntity();
    BlockGetter level();
    <T> T getAdditionalData(String key);
    <T> void withAdditionalData(String key, T value);

}

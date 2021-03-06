package com.github.minecraftschurlimods.multiblocklib.xplat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ServiceLoader;

public interface ClientXplatAbstractions {
    ClientXplatAbstractions INSTANCE = ServiceLoader.load(ClientXplatAbstractions.class).findFirst().orElseThrow();

    void renderInUI(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, MultiBufferSource.BufferSource buffers, RandomSource rand);

    void renderInWorld(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, MultiBufferSource.BufferSource buffers);
}

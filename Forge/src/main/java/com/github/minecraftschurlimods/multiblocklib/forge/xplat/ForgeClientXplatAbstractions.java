package com.github.minecraftschurlimods.multiblocklib.forge.xplat;

import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Random;

public class ForgeClientXplatAbstractions implements ClientXplatAbstractions {
    @Override
    public void renderForMultiblock(final BlockState state, final BlockPos pos, final BlockAndTintGetter level, final PoseStack stack, final MultiBufferSource.BufferSource buffers, final Random rand) {
        for (var layer : RenderType.chunkBufferLayers()) {
            if (ItemBlockRenderTypes.canRenderInLayer(state, layer)) {
                ForgeHooksClient.setRenderType(layer);
                var buffer = buffers.getBuffer(layer);
                Minecraft.getInstance().getBlockRenderer().renderBatched(state, pos, level, stack, buffer, false, rand);
                ForgeHooksClient.setRenderType(null);
            }
        }
    }
}

package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class FabricClientXplatAbstractions implements ClientXplatAbstractions {
    @Override
    public void renderInUI(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, MultiBufferSource.BufferSource buffers, RandomSource rand) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
            VertexConsumer buffer = buffers.getBuffer(layer);
            blockRenderer.renderLiquid(pos, level, new LiquidBlockVertexConsumer(buffer, stack, pos), state, fluidState);
        }
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            RenderType layer = ItemBlockRenderTypes.getChunkRenderType(state);
            VertexConsumer buffer = buffers.getBuffer(layer);
            blockRenderer.renderBatched(state, pos, level, stack, buffer, false, rand);
        }
    }

    @Override
    public void renderInWorld(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack stack, MultiBufferSource.BufferSource buffers) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            RenderType layer = ItemBlockRenderTypes.getRenderLayer(fluidState);
            VertexConsumer buffer = buffers.getBuffer(layer);
            blockRenderer.renderLiquid(pos, level, new LiquidBlockVertexConsumer(buffer, stack, pos), state, fluidState);
        }
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            blockRenderer.renderSingleBlock(state, stack, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
    }

    private record LiquidBlockVertexConsumer(VertexConsumer parent, PoseStack poseStack, BlockPos pos) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            final float dx = pos().getX() & 15;
            final float dy = pos().getY() & 15;
            final float dz = pos().getZ() & 15;
            parent().vertex(poseStack().last().pose(), (float) x - dx, (float) y - dy, (float) z - dz);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            parent().color(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            parent().uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            parent().overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            parent().uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            parent().normal(poseStack().last().normal(), x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            parent().endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            parent().defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            parent().unsetDefaultColor();
        }
    }
}

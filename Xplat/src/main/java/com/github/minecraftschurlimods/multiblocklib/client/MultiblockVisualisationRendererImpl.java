package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.github.minecraftschurlimods.multiblocklib.mixin.AccessorMultiBufferSource;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public final class MultiblockVisualisationRendererImpl implements MultiblockVisualisationRenderer {
    public static final MultiblockVisualisationRenderer INSTANCE = new MultiblockVisualisationRendererImpl();

    private Multiblock multiblock;
    private BlockPos anchorPos;
    private Rotation rotation;
    private Mirror mirror;
    private int blocksDone, blocks, airFilled;
    private Collection<Multiblock.SimulateResult> simulateCache;
    private boolean enabled;
    private boolean dirty = true;
    private MultiBufferSource.BufferSource buffers = null;

    private MultiblockVisualisationRendererImpl() {}

    @Override
    public void setMultiblock(ResourceLocation id) {
        setMultiblock(MBAPI.INSTANCE.getMultiblock(id));
    }

    @Override
    public void setMultiblock(Multiblock multiblock) {
        this.multiblock = multiblock;
        this.dirty = true;
    }

    @Override
    public void setAnchorPos(final BlockPos anchorPos) {
        this.anchorPos = anchorPos;
        this.dirty = true;
    }

    @Override
    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
        this.dirty = true;
    }

    @Override
    public void setMirror(final Mirror mirror) {
        this.mirror = mirror;
        this.dirty = true;
    }

    @Override
    public void setEnabled() {
        setEnabled(true);
    }

    @Override
    public void setDisabled() {
        setEnabled(false);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getBlocks() {
        return blocks;
    }

    @Override
    public int getBlocksDone() {
        return blocksDone;
    }

    @Override
    public int getAirFilled() {
        return airFilled;
    }

    @Override
    public void renderMultiblock(PoseStack stack) {
        if (!enabled || multiblock == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;
        if (dirty) {
            if (rotation == null || multiblock.isSymmetrical()) {
                rotation = Rotation.NONE;
            }
            if (mirror == null || multiblock.isSymmetrical()) {
                mirror = Mirror.NONE;
            }
            var pos = anchorPos;
            if (pos == null) {
                if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
                    pos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
                } else {
                    return;
                }
                assert minecraft.player != null;
                rotation = switch (minecraft.player.getDirection()) {
                    case DOWN, NORTH, UP -> Rotation.NONE;
                    case SOUTH -> Rotation.CLOCKWISE_180;
                    case WEST -> Rotation.COUNTERCLOCKWISE_90;
                    case EAST -> Rotation.CLOCKWISE_90;
                };
            }
            simulateCache = multiblock.simulate(pos, rotation, mirror);
            dirty = false;
        }
        EntityRenderDispatcher erd = minecraft.getEntityRenderDispatcher();
        double renderPosX = erd.camera.getPosition().x();
        double renderPosY = erd.camera.getPosition().y();
        double renderPosZ = erd.camera.getPosition().z();
        stack.pushPose();
        stack.translate(-renderPosX, -renderPosY, -renderPosZ);
        if (buffers == null) {
            buffers = initBuffers(minecraft.renderBuffers().bufferSource());
        }
        blocks = blocksDone = airFilled = 0;
        for (final Multiblock.SimulateResult r : simulateCache) {
            if (r.stateMatcher().getType().equals(Init.ANY_MATCHER.getId())) continue;
            boolean airMatcher = r.stateMatcher().getType().equals(Init.AIR_MATCHER.getId());
            if (!airMatcher) {
                blocks++;
            }
            if (!r.test(level, rotation, mirror)) {
                BlockState renderState = r.stateMatcher().displayState(level.getGameTime());
                renderBlock(renderState, r.worldPos(), stack);

                if (airMatcher) {
                    airFilled++;
                }
            }
            blocksDone++;
        }
        buffers.endBatch();
        stack.popPose();
    }

    private void renderBlock(BlockState state, BlockPos pos, PoseStack stack) {
        stack.pushPose();
        stack.translate(pos.getX(), pos.getY(), pos.getZ());

        if (state.getBlock() == Blocks.AIR) {
            float scale = 0.3F;
            float off = (1F - scale) / 2;
            stack.translate(off, off, -off);
            stack.scale(scale, scale, scale);

            state = Blocks.RED_CONCRETE.defaultBlockState();
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, stack, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);

        stack.popPose();
    }

    private MultiBufferSource.BufferSource initBuffers(final MultiBufferSource.BufferSource original) {
        BufferBuilder fallback = ((AccessorMultiBufferSource) original).getFallbackBuffer();
        Map<RenderType, BufferBuilder> layerBuffers = ((AccessorMultiBufferSource) original).getFixedBuffers();
        Map<RenderType, BufferBuilder> remapped = new Object2ObjectLinkedOpenHashMap<>();
        for (Map.Entry<RenderType, BufferBuilder> e : layerBuffers.entrySet()) {
            remapped.put(GhostRenderLayer.remap(e.getKey()), e.getValue());
        }
        return new GhostBuffers(fallback, remapped);
    }

    private static class GhostBuffers extends MultiBufferSource.BufferSource {
        protected GhostBuffers(BufferBuilder fallback, Map<RenderType, BufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return super.getBuffer(GhostRenderLayer.remap(type));
        }
    }

    private static class GhostRenderLayer extends RenderType {
        private static final Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();

        private GhostRenderLayer(RenderType original) {
            super(String.format("%s_%s_ghost", original, MBAPI.MODID), original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, () -> {
                original.setupRenderState();

                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 0.4F);
            }, () -> {
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();

                original.clearRenderState();
            });
        }

        public static RenderType remap(RenderType in) {
            if (in instanceof GhostRenderLayer) {
                return in;
            } else {
                return remappedTypes.computeIfAbsent(in, GhostRenderLayer::new);
            }
        }
    }
}

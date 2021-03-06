package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.api.matcher.AnyMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.matcher.DisplayOnlyMatcher;
import com.github.minecraftschurlimods.multiblocklib.mixin.AccessorMultiBufferSource;
import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MultiblockVisualisationRendererImpl implements MultiblockVisualisationRenderer {
    private Multiblock multiblock;
    private BlockPos currentPos;
    private BlockPos anchorPos;
    private Rotation rotation;
    private Mirror mirror;
    private int blocksDone, blocks, airFilled;
    private Collection<Multiblock.SimulateResult> simulateCache;
    private boolean enabled;
    private boolean dirty = true;
    private MultiBufferSource.BufferSource buffers = null;

    public MultiblockVisualisationRendererImpl() {}

    @Override
    public void setMultiblock(ResourceLocation id) {
        setMultiblock(Minecraft.getInstance().level.registryAccess().registryOrThrow(MBAPI.MULTIBLOCK_REGISTRY).get(id));
    }

    @Override
    public void setMultiblock(@Nullable Multiblock multiblock) {
        this.multiblock = multiblock;
        this.dirty = true;
    }

    @Override
    public Multiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void setAnchorPos(final BlockPos anchorPos) {
        this.anchorPos = anchorPos;
        this.dirty = true;
    }

    @Override
    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    @Override
    public BlockPos getCurrentPos() {
        return currentPos;
    }

    @Override
    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
        this.dirty = true;
    }

    @Override
    public Rotation getRotation() {
        return rotation;
    }

    @Override
    public void setMirror(final Mirror mirror) {
        this.mirror = mirror;
        this.dirty = true;
    }

    @Override
    public Mirror getMirror() {
        return mirror;
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

    public void renderMultiblock(PoseStack stack) {
        if (multiblock == null) {
            setDisabled();
        }
        if (!enabled) return;
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) return;
        if (dirty || anchorPos == null) {
            if (rotation == null || multiblock.isSymmetrical()) {
                rotation = Rotation.NONE;
            }
            if (mirror == null || multiblock.isSymmetrical()) {
                mirror = Mirror.NONE;
            }
            BlockPos newPos = anchorPos;
            if (newPos == null) {
                if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
                    newPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
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
            if (dirty || !Objects.equals(currentPos, newPos)) {
                currentPos = newPos;
                simulateCache = multiblock.simulate(currentPos, rotation, mirror);
                dirty = false;
            }
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
        MBProxyLevel proxy = new MBProxyLevel(level, simulateCache);
        for (final Multiblock.SimulateResult r : simulateCache) {
            StateMatcher stateMatcher = r.stateMatcher();
            if (stateMatcher instanceof DisplayOnlyMatcher || stateMatcher instanceof AnyMatcher) continue;
            boolean airMatcher = stateMatcher.isAir();
            if (!airMatcher) {
                blocks++;
            }
            if (!r.test(level)) {
                BlockState renderState = r.displayState(level.getGameTime());
                renderBlock(renderState, r.worldPos(), stack, proxy);

                if (airMatcher) {
                    airFilled++;
                }
            }
            blocksDone++;
        }
        buffers.endBatch();
        stack.popPose();
    }

    private void renderBlock(BlockState state, BlockPos pos, PoseStack stack, BlockAndTintGetter level) {
        stack.pushPose();
        stack.translate(pos.getX(), pos.getY(), pos.getZ());

        if (state.isAir()) {
            float scale = 0.3F;
            float off = (1F - scale) / 2;
            stack.translate(off, off, -off);
            stack.scale(scale, scale, scale);

            state = Blocks.RED_CONCRETE.defaultBlockState();
        }

        ClientXplatAbstractions.INSTANCE.renderInWorld(state, pos, level, stack, buffers);

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

    private static class MBProxyLevel implements BlockAndTintGetter {
        private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
        private final Level parent;
        private final Collection<Multiblock.SimulateResult> simulateCache;

        public MBProxyLevel(Level parent, Collection<Multiblock.SimulateResult> simulateCache) {
            this.parent = parent;
            this.simulateCache = simulateCache;
        }

        @Override
        public float getShade(Direction dir, boolean var2) {
            return 1.0F;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return parent.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
            return parent.getBlockTint(blockPos, colorResolver);
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            BlockState state = getBlockState(pos);
            if (state.getBlock() instanceof EntityBlock) {
                return teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
            }
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos blockPos) {
            return simulateCache.stream()
                    .filter(r -> r.worldPos().equals(blockPos))
                    .findFirst()
                    .map(simulateResult -> simulateResult.displayState(parent.getGameTime()))
                    .orElse(Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos blockPos) {
            return getBlockState(blockPos).getFluidState();
        }

        @Override
        public int getHeight() {
            return parent.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return parent.getMinBuildHeight();
        }
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

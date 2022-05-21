package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockWidget;
import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MultiblockWidgetImpl implements MultiblockWidget {
    private static final Random RAND = new Random();
    private Multiblock multiblock;
    private Rotation rotation;
    private Mirror mirror;
    private Collection<Multiblock.SimulateResult> simulateCache;
    private boolean dirty = true;
    private float guiRotation;
    private FakeMBLevel level;
    private float width;
    private float height;

    @Override
    public void render(final PoseStack stack, final int mouseX, final int mouseY, final float partialTicks) {
        if (dirty) {
            if (rotation == null || multiblock.isSymmetrical()) {
                rotation = Rotation.NONE;
            }
            if (mirror == null || multiblock.isSymmetrical()) {
                mirror = Mirror.NONE;
            }
            simulateCache = multiblock.simulate(BlockPos.ZERO, rotation, mirror);
            level = new FakeMBLevel(multiblock.size(), simulateCache);
            dirty = false;
        }
        Vec3i size = multiblock.size();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        float maxX = width;
        float maxY = height;
        float diag = (float) Math.sqrt(sizeX * sizeX + sizeZ * sizeZ);
        float scaleX = maxX / diag;
        float scaleY = maxY / sizeY;
        float scale = -Math.min(scaleX, scaleY);

        int xPos = 0;
        int yPos = 0;
        stack.pushPose();
        stack.translate(xPos, yPos, 100);
        stack.scale(scale, scale, scale);
        stack.translate(-(float) sizeX / 2, -(float) sizeY / 2, 0);
        Vector4f eye = new Vector4f(0, 0, -100, 1);
        Matrix4f rotMat = new Matrix4f();
        rotMat.setIdentity();
        stack.mulPose(Vector3f.XP.rotationDegrees(-30F));
        rotMat.multiply(Vector3f.XP.rotationDegrees(30));
        float offX = (float) -sizeX / 2;
        float offZ = (float) -sizeZ / 2 + 1;
        stack.translate(-offX, 0, -offZ);
        stack.mulPose(Vector3f.YP.rotationDegrees(guiRotation));
        rotMat.multiply(Vector3f.YP.rotationDegrees(-guiRotation));
        stack.mulPose(Vector3f.YP.rotationDegrees(45));
        rotMat.multiply(Vector3f.YP.rotationDegrees(-45));
        stack.translate(offX, 0, offZ);
        eye.transform(rotMat);
        eye.perspectiveDivide();
        renderElements(stack);
        stack.popPose();
    }

    private void renderElements(PoseStack ms) {
        ms.pushPose();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        ms.translate(0, 0, -1);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        doWorldRenderPass(ms, buffers);
        //doTileEntityRenderPass(ms, buffers);

        buffers.endBatch();
        ms.popPose();
    }

    private void doWorldRenderPass(PoseStack stack, final @Nonnull MultiBufferSource.BufferSource buffers) {
        for (Multiblock.SimulateResult r : simulateCache) {
            BlockState state = r.stateMatcher().displayState(0);
            BlockPos pos = r.worldPos();
            stack.pushPose();
            stack.translate(pos.getX(), pos.getY(), pos.getZ());
            ClientXplatAbstractions.INSTANCE.renderForMultiblock(state, pos, level, stack, buffers, RAND);
            stack.popPose();
        }
    }

    @Override
    public void setWidth(final float width) {
        this.width = width;
    }

    @Override
    public void setHeight(final float height) {
        this.height = height;
    }

    @Override
    public void setGuiRotation(float rotation) {
        this.guiRotation = rotation;
    }

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
    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
        this.dirty = true;
    }

    @Override
    public void setMirror(final Mirror mirror) {
        this.mirror = mirror;
        this.dirty = true;
    }

    private static class FakeMBLevel implements BlockAndTintGetter {
        private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
        private final Vec3i size;
        private final Map<BlockPos, StateMatcher> cache = new HashMap<>();

        private FakeMBLevel(Multiblock multiblock, BlockPos anchor, Rotation rotation, Mirror mirror) {
            this(multiblock.size(), multiblock.simulate(anchor, rotation, mirror));
        }

        private FakeMBLevel(final Vec3i size, final Collection<Multiblock.SimulateResult> simulate) {
            this.size = size;
            for (final Multiblock.SimulateResult simulateResult : simulate) {
                cache.put(simulateResult.worldPos(), simulateResult.stateMatcher());
            }
        }

        @Override
        public float getShade(final Direction var1, final boolean var2) {
            return 0;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return null;
        }

        @Override
        public int getBlockTint(final BlockPos pos, final ColorResolver color) {
            var plains = RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
            return color.getColor(plains, pos.getX(), pos.getZ());
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(final BlockPos pos) {
            BlockState state = getBlockState(pos);
            if (state.getBlock() instanceof EntityBlock) {
                return teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
            }
            return null;
        }

        @Override
        public BlockState getBlockState(final BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            if (x < 0 || y < 0 || z < 0 || x >= size.getX() || y >= size.getY() || z >= size.getZ()) {
                return Blocks.AIR.defaultBlockState();
            }
            long ticks = Minecraft.getInstance().level.getGameTime();
            var m = cache.get(pos);
            if (m == null) {
                return Blocks.AIR.defaultBlockState();
            }
            return m.displayState(ticks);
        }

        @Override
        public FluidState getFluidState(final BlockPos var1) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getHeight() {
            return Minecraft.getInstance().level.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return Minecraft.getInstance().level.getMinBuildHeight();
        }
    }
}

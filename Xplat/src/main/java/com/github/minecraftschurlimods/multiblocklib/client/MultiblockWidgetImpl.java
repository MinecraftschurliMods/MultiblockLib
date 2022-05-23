package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockWidget;
import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
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

public class MultiblockWidgetImpl extends AbstractWidget implements MultiblockWidget {
    private static final Random RAND = new Random();
    private Multiblock multiblock;
    private Mirror mirror;
    private Collection<Multiblock.SimulateResult> simulateCache;
    private boolean dirty = true;
    private Quaternion rotation = Quaternion.ONE;
    private FakeMBLevel level;

    public MultiblockWidgetImpl(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY);
    }

    @Override
    public void renderButton(final PoseStack stack, final int mouseX, final int mouseY, final float partialTicks) {
        if (multiblock == null) return;
        if (dirty) {
            if (mirror == null || multiblock.isSymmetrical()) {
                mirror = Mirror.NONE;
            }
            simulateCache = multiblock.simulate(BlockPos.ZERO, Rotation.NONE, mirror);
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

        stack.pushPose();
        stack.translate(x, y, 100);
        stack.translate(width/2., height/2., 0);
        stack.scale(scale, scale, scale);
        stack.mulPose(rotation);
        stack.translate(-0.5, -0.5, 0.5);
        renderElements(stack);
        stack.popPose();
    }

    @Override
    public void updateNarration(final NarrationElementOutput var1) {
        var1.add(NarratedElementType.TITLE, new TranslatableComponent("gui.narrate.multiblock", getMessage()));
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
    public void setWidth(final int width) {
        this.width = width;
    }

    @Override
    public void setHeight(final int height) {
        this.height = height;
    }

    @Override
    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    @Override
    public boolean setMultiblock(ResourceLocation id) {
        Multiblock multiblock = MBAPI.INSTANCE.getMultiblock(id);
        if (multiblock != null) {
            setMultiblock(multiblock);
            return true;
        }
        return false;
    }

    @Override
    public void setMultiblock(Multiblock multiblock) {
        this.multiblock = multiblock;
        setMessage(new TranslatableComponent(Util.makeDescriptionId("multiblock", MBAPI.INSTANCE.getMultiblockId(multiblock))));
        this.dirty = true;
    }

    @Override
    public void setMirror(final Mirror mirror) {
        this.mirror = mirror;
        this.dirty = true;
    }

    @Override
    public boolean mouseClicked(final double $$0, final double $$1, final int $$2) {
        return false;
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
            return 1.0F;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return null;
        }

        @Override
        public int getBrightness(LightLayer type, BlockPos pos) {
            return 15;
        }

        @Override
        public int getRawBrightness(BlockPos pos, int ambientDarkening) {
            return 15 - ambientDarkening;
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

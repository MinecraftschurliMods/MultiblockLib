package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockWidget;
import com.github.minecraftschurlimods.multiblocklib.xplat.ClientXplatAbstractions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Collection;

public class MultiblockWidgetImpl extends AbstractWidget implements MultiblockWidget {
    private static final RandomSource RAND = RandomSource.create();
    private Multiblock multiblock;
    private Mirror mirror;
    private Collection<Multiblock.SimulateResult> simulateCache;
    private boolean dirty = true;
    private Quaternion rotation = Quaternion.ONE;
    private FakeMBLevel level;
    private Multiblock.SimulateFilter filter = Multiblock.SimulateFilter.ALL;

    public MultiblockWidgetImpl(int x, int y, int width, int height, ResourceLocation multiblock) {
        this(x, y, width, height);
        setMultiblock(multiblock);
    }

    public MultiblockWidgetImpl(int x, int y, int width, int height, Multiblock multiblock) {
        this(x, y, width, height);
        setMultiblock(multiblock);
    }

    public MultiblockWidgetImpl(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        if (multiblock == null) return;
        if (dirty) {
            if (mirror == null || multiblock.isSymmetrical()) {
                mirror = Mirror.NONE;
            }
            simulateCache = multiblock.simulate(BlockPos.ZERO, Rotation.NONE, mirror, this.filter);
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
    public void updateNarration(NarrationElementOutput var1) {
        var1.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.multiblock", getMessage()));
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

    private void doWorldRenderPass(PoseStack stack, @Nonnull MultiBufferSource.BufferSource buffers) {
        for (Multiblock.SimulateResult r : simulateCache) {
            BlockState state = r.stateMatcher().displayState(0);
            BlockPos pos = r.worldPos();
            stack.pushPose();
            stack.translate(pos.getX(), pos.getY(), pos.getZ());
            ClientXplatAbstractions.INSTANCE.renderInUI(state, pos, level, stack, buffers, RAND);
            stack.popPose();
        }
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    @Override
    public boolean setMultiblock(ResourceLocation id) {
        Multiblock multiblock = Minecraft.getInstance().level.registryAccess().registryOrThrow(MBAPI.MULTIBLOCK_REGISTRY).get(id);
        if (multiblock != null) {
            setMultiblock(multiblock);
            return true;
        }
        return false;
    }

    @Override
    public void setMultiblock(Multiblock multiblock) {
        this.multiblock = multiblock;
        setMessage(multiblock.getName());
        this.dirty = true;
    }

    @Override
    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
        this.dirty = true;
    }

    @Override
    public void setFilter(Multiblock.SimulateFilter filter) {
        this.filter = filter;
        this.dirty = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int $$2) {
        return false;
    }
}

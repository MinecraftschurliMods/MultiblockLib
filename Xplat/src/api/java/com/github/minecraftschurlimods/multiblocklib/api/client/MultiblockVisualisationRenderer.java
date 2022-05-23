package com.github.minecraftschurlimods.multiblocklib.api.client;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.ServiceLoader;

public interface MultiblockVisualisationRenderer {
    MultiblockVisualisationRenderer INSTANCE = ServiceLoader.load(MultiblockVisualisationRenderer.class).findFirst().orElseThrow();

    void setMultiblock(ResourceLocation id);

    void setMultiblock(Multiblock multiblock);

    Multiblock getMultiblock();

    void setAnchorPos(BlockPos anchorPos);

    BlockPos getAnchorPos();

    BlockPos getCurrentPos();

    void setRotation(Rotation rotation);

    Rotation getRotation();

    void setMirror(Mirror mirror);

    Mirror getMirror();

    void setEnabled();

    void setDisabled();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    int getBlocks();

    int getBlocksDone();

    int getAirFilled();
}

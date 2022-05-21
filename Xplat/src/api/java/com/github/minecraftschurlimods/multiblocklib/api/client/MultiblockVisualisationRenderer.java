package com.github.minecraftschurlimods.multiblocklib.api.client;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * @author georg
 * @version 2022-05-21
 */
public interface MultiblockVisualisationRenderer {
    void setMultiblock(ResourceLocation id);

    void setMultiblock(Multiblock multiblock);

    void setAnchorPos(BlockPos anchorPos);

    void setRotation(Rotation rotation);

    void setMirror(Mirror mirror);

    void setEnabled();

    void setDisabled();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    int getBlocks();

    int getBlocksDone();

    int getAirFilled();

    void renderMultiblock(PoseStack stack);
}

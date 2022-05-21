package com.github.minecraftschurlimods.multiblocklib.api.client;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public interface MultiblockWidget extends Widget {
    void setMultiblock(ResourceLocation id);
    void setMultiblock(Multiblock multiblock);
    void setRotation(Rotation rotation);
    void setMirror(Mirror mirror);
    void setGuiRotation(float rotation);
    void setWidth(float width);
    void setHeight(float height);
}

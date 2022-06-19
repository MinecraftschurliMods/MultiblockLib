package com.github.minecraftschurlimods.multiblocklib.api.client;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.mojang.math.Quaternion;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;

public interface MultiblockWidget extends Widget, GuiEventListener, NarratableEntry {
    /**
     * Set the multiblock for this widget.
     *
     * @param id the id of the multiblock to set
     * @return true if the multiblock with that id exists, false otherwise
     */
    boolean setMultiblock(ResourceLocation id);

    /**
     * Set the multiblock for this widget.
     *
     * @param multiblock the multiblock to set
     */
    void setMultiblock(Multiblock multiblock);

    /**
     * Set the mirror mode for this multiblock widget.
     *
     * @param mirror the mirror mode to set
     */
    void setMirror(Mirror mirror);

    /**
     * Set the angle of rotation for this multiblock widget.
     *
     * @param rotation the rotation quaternion for the multiblock widget
     */
    void setRotation(Quaternion rotation);

    /**
     * Set the width of this multiblock widget.
     *
     * @param width the new width of this multiblock widget
     */
    void setWidth(int width);

    /**
     * Set the height of this multiblock widget.
     *
     * @param height the new height of this multiblock widget
     */
    void setHeight(int height);

    /**
     * Set the filter of this multiblock widget.
     *
     * @param filter the new filter of this multiblock widget
     */
    void setFilter(Multiblock.SimulateFilter filter);
}

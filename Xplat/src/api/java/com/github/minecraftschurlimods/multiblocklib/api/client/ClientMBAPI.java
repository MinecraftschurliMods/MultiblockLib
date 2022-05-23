package com.github.minecraftschurlimods.multiblocklib.api.client;

import java.util.ServiceLoader;

public interface ClientMBAPI {
    /**
     * The instance of the client API.
     */
    ClientMBAPI INSTANCE = ServiceLoader.load(ClientMBAPI.class).findFirst().orElseThrow();

    /**
     * Create a new multiblock widget with the given parameters.
     *
     * @param x      the x position of the widget
     * @param y      the y position of the widget
     * @param width  the width of the widget
     * @param height the height of the widget
     * @return a new multiblock widget with the given parameters
     */
    MultiblockWidget newMultiblockWidget(int x, int y, int width, int height);
}

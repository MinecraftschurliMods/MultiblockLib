package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.client.ClientMBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockWidget;

public class ClientMBAPIImpl implements ClientMBAPI {
    @Override
    public MultiblockWidget newMultiblockWidget(int x, int y, int width, int height) {
        return new MultiblockWidgetImpl(x, y, width, height);
    }
}

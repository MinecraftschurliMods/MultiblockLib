package com.github.minecraftschurlimods.multiblocklib.fabric.client;

import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.client.MultiblockVisualisationRendererImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FabricClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END.register(context -> ((MultiblockVisualisationRendererImpl) MultiblockVisualisationRenderer.INSTANCE).renderMultiblock(context.matrixStack()));
    }
}

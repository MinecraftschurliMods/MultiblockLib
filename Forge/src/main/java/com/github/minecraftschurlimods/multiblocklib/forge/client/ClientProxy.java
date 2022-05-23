package com.github.minecraftschurlimods.multiblocklib.forge.client;

import com.github.minecraftschurlimods.multiblocklib.api.client.MultiblockVisualisationRenderer;
import com.github.minecraftschurlimods.multiblocklib.client.MultiblockVisualisationRendererImpl;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy {
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ClientProxy::onLevelRenderLast);
    }

    private static void onLevelRenderLast(RenderLevelLastEvent event) {
        ((MultiblockVisualisationRendererImpl) MultiblockVisualisationRenderer.INSTANCE).renderMultiblock(event.getPoseStack());
    }
}

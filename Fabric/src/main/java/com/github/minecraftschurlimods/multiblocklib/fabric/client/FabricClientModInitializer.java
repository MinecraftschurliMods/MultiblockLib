package com.github.minecraftschurlimods.multiblocklib.fabric.client;

import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class FabricClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> XplatAbstractions.INSTANCE.clientInit());
    }
}

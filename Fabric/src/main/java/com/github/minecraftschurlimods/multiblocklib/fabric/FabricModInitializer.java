package com.github.minecraftschurlimods.multiblocklib.fabric;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.MBAPIImpl;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.resources.ResourceLocation;

public class FabricModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        XplatAbstractions.INSTANCE.init();
        Init.register();
    }
}

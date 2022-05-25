package com.github.minecraftschurlimods.multiblocklib.fabric;

import com.github.minecraftschurlimods.multiblocklib.impl.MBAPIImpl;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.fabricmc.api.ModInitializer;

public class FabricModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        MBAPIImpl.init();
        Init.register();
        XplatAbstractions.INSTANCE.init();
    }
}

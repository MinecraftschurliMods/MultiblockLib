package com.github.minecraftschurlimods.multiblocklib.forge;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.forge.client.ClientProxy;
import com.github.minecraftschurlimods.multiblocklib.forge.xplat.ForgeXplatAbstractions;
import com.github.minecraftschurlimods.multiblocklib.impl.MBAPIImpl;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(MBAPI.MODID)
public class MBLib {
    public MBLib() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::init);
        MBAPIImpl.init();
        Init.register();
        XplatAbstractions.INSTANCE.init();
    }
}

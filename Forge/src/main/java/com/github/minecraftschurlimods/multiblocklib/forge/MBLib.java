package com.github.minecraftschurlimods.multiblocklib.forge;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.impl.MBAPIImpl;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import net.minecraftforge.fml.common.Mod;

@Mod(MBAPI.MODID)
public class MBLib {
    public MBLib() {
        MBAPIImpl.init();
        Init.register();
    }
}

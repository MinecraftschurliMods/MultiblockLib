package com.github.minecraftschurlimods.multiblocklib.forge;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.minecraftforge.fml.common.Mod;

@Mod(MBAPI.MODID)
public class MBLib {
    public MBLib() {
        XplatAbstractions.INSTANCE.init();
    }
}

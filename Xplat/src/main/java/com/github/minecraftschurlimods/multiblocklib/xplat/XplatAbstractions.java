package com.github.minecraftschurlimods.multiblocklib.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;

import java.util.ServiceLoader;

public interface XplatAbstractions {
    XplatAbstractions INSTANCE = ServiceLoader.load(XplatAbstractions.class).findFirst().orElseThrow();

    void init();

    Codec<Codec<? extends Multiblock>> getMultiblockTypeRegistryCodec();

    Codec<Codec<? extends StateMatcher>> getStateMatcherTypeRegistryCodec();
}

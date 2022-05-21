package com.github.minecraftschurlimods.multiblocklib.xplat;

import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ServiceLoader;
import java.util.function.Consumer;

public interface XplatAbstractions {
    XplatAbstractions INSTANCE = ServiceLoader.load(XplatAbstractions.class).findFirst().orElseThrow();

    void registerServerReloadListener(Consumer<Consumer<PreparableReloadListener>> consumer);
}

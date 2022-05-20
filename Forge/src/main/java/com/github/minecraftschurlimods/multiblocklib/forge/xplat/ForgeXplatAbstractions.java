package com.github.minecraftschurlimods.multiblocklib.forge.xplat;

import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class ForgeXplatAbstractions implements XplatAbstractions {
    private final Collection<Consumer<Consumer<PreparableReloadListener>>> serverReloadListeners = new ArrayList<>();

    @Override
    public void registerServerReloadListener(final Consumer<Consumer<PreparableReloadListener>> consumer) {
        serverReloadListeners.add(consumer);
    }

    @Override
    public void init() {
        XplatAbstractions.super.init();
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent evt) -> serverReloadListeners.forEach(listener -> listener.accept(evt::addListener)));
    }
}

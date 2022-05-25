package com.github.minecraftschurlimods.multiblocklib.xplat;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface XplatAbstractions {
    XplatAbstractions INSTANCE = ServiceLoader.load(XplatAbstractions.class).findFirst().orElseThrow();

    void registerServerReloadListener(Consumer<Consumer<PreparableReloadListener>> consumer);

    <K, V> void registerDatapackSyncer(ResourceLocation id, Codec<K> keyCodec, Codec<V> valueCodec, Supplier<Map<K, V>> serverDataSupplier, Consumer<Map<K, V>> clientDataConsumer);

    void init();

    void clientInit();
}

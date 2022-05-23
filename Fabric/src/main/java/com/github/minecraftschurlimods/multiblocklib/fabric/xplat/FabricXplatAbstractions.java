package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricXplatAbstractions implements XplatAbstractions {
    private static final ResourceLocation ID = new ResourceLocation(MBAPI.MODID, MBAPI.MODID);

    @Override
    public void registerServerReloadListener(final Consumer<Consumer<PreparableReloadListener>> consumer) {
        var listeners = new HashSet<PreparableReloadListener>();
        consumer.accept(listeners::add);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ID;
            }

            @Override
            public CompletableFuture<Void> reload(final PreparationBarrier preparationBarrier, final ResourceManager resourceManager, final ProfilerFiller profilerFiller, final ProfilerFiller profilerFiller2, final Executor executor, final Executor executor2) {
                return CompletableFuture.allOf(listeners.stream().map(preparableReloadListener -> preparableReloadListener.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2)).toArray(CompletableFuture[]::new));
            }
        });
    }

    @Override
    public <K, V> void registerDatapackSyncer(final ResourceLocation id, final Codec<K> keyCodec, final Codec<V> valueCodec, final Supplier<Map<K, V>> serverDataSupplier, final Consumer<Map<K, V>> clientDataConsumer) {

    }

    @Override
    public void init() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                //syncData();
            }
        });
    }
}

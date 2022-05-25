package com.github.minecraftschurlimods.multiblocklib.fabric.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricXplatAbstractions implements XplatAbstractions {
    private static final ResourceLocation ID = new ResourceLocation(MBAPI.MODID, "datapacks");
    private static final Map<ResourceLocation, Triple<Consumer<FriendlyByteBuf>, Function<FriendlyByteBuf, Map<?, ?>>, Consumer<Map<?, ?>>>> SYNCER = new HashMap<>();
    private final Set<PreparableReloadListener> listeners = new HashSet<>();

    @Override
    public void registerServerReloadListener(final Consumer<Consumer<PreparableReloadListener>> consumer) {
        consumer.accept(listeners::add);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> void registerDatapackSyncer(final ResourceLocation id, final Codec<K> keyCodec, final Codec<V> valueCodec, final Supplier<Map<K, V>> serverDataSupplier, final Consumer<Map<K, V>> clientDataConsumer) {
        var codec = Codec.unboundedMap(keyCodec, valueCodec);
        SYNCER.put(id, Triple.of(buf -> buf.writeWithCodec(codec, serverDataSupplier.get()), buf -> buf.readWithCodec(codec), map -> clientDataConsumer.accept((Map<K, V>)map)));
    }

    @Override
    public void init() {
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
    public void clientInit() {
        ClientPlayNetworking.registerReceiver(ID, (c, h, buf, responseSender) -> {
            ResourceLocation id = buf.readResourceLocation();
            var syncer = SYNCER.get(id);
            if (syncer == null) return;
            Map<?,?> data = syncer.getMiddle().apply(buf);
            syncer.getRight().accept(data);
        });
    }

    public static void reloadResources(final ServerPlayer player) {
        SYNCER.forEach((id, syncer) -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeResourceLocation(id);
            syncer.getLeft().accept(buf);
            ServerPlayNetworking.send(player, ID, buf);
        });
    }
}

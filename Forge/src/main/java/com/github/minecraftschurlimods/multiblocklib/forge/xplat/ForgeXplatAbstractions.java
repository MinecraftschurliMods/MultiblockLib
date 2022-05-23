package com.github.minecraftschurlimods.multiblocklib.forge.xplat;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ForgeXplatAbstractions implements XplatAbstractions {
    private static final Map<ResourceLocation, DatapackSyncer<?,?>> DATAPACK_SYNCERS = new HashMap<>();
    private final SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MBAPI.MODID, "main"), () -> "1", Predicate.isEqual("1"), Predicate.isEqual("1"));
    private final Collection<Consumer<Consumer<PreparableReloadListener>>> serverReloadListeners = new ArrayList<>();
    private int index;

    @Override
    public void registerServerReloadListener(final Consumer<Consumer<PreparableReloadListener>> consumer) {
        serverReloadListeners.add(consumer);
    }

    @Override
    public <K, V> void registerDatapackSyncer(ResourceLocation id, Codec<K> keyCodec, Codec<V> valueCodec, final Supplier<Map<K, V>> serverDataSupplier, final Consumer<Map<K, V>> clientDataConsumer) {
        DATAPACK_SYNCERS.put(id, new DatapackSyncer<>(keyCodec, valueCodec, serverDataSupplier, clientDataConsumer));
    }

    public void init() {
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent evt) -> serverReloadListeners.forEach(listener -> listener.accept(evt::addListener)));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent evt) -> {
            var player = evt.getPlayer();
            for (final ResourceLocation id : DATAPACK_SYNCERS.keySet()) {
                channel.send(player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player), new DatapackSyncPacket(id));
            }
        });
        channel.registerMessage(index++, DatapackSyncPacket.class, DatapackSyncPacket::encode, DatapackSyncPacket::new, DatapackSyncPacket::handle);
    }

    private static class DatapackSyncer<K, V> {
        private final Consumer<FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, Map<K, V>> decoder;
        private final Consumer<Map<K, V>> handler;

        public DatapackSyncer(final Codec<K> keyCodec, final Codec<V> valueCodec, final Supplier<Map<K, V>> serverDataSupplier, final Consumer<Map<K, V>> clientDataConsumer) {
            Codec<Map<K, V>> mapCodec = Codec.unboundedMap(keyCodec, valueCodec);
            encoder = (FriendlyByteBuf buf) -> buf.writeWithCodec(mapCodec, serverDataSupplier.get());
            decoder = (FriendlyByteBuf buf) -> buf.readWithCodec(mapCodec);
            handler = clientDataConsumer;
        }
    }

    private static class DatapackSyncPacket {
        private final ResourceLocation id;
        private Map<?, ?> data;

        public DatapackSyncPacket(final ResourceLocation id) {
            this.id = id;
        }

        public DatapackSyncPacket(final FriendlyByteBuf buf) {
            this.id = buf.readResourceLocation();
            this.data = DATAPACK_SYNCERS.get(this.id).decoder.apply(buf);
        }

        public void encode(final FriendlyByteBuf buf) {
            buf.writeResourceLocation(this.id);
            DATAPACK_SYNCERS.get(this.id).encoder.accept(buf);
        }

        public void handle(final Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> ((Consumer)DATAPACK_SYNCERS.get(this.id).handler).accept(this.data));
            context.setPacketHandled(true);
        }
    }
}

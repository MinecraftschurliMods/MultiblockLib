package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.BasicMatcher;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class MBAPIImpl implements MBAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(MBAPI.class);
    private static final BiMap<ResourceLocation, Multiblock> MULTIBLOCKS = HashBiMap.create();
    private static final Registry<Codec<? extends StateMatcher>> STATE_MATCHER_TYPE_REGISTRY = createRegistry("state_matcher_type");
    private static final Registry<Codec<? extends Multiblock>> MULTIBLOCK_TYPE_REGISTRY = createRegistry("multiblock_type");

    private static <T> Registry<T> createRegistry(final String name) {
        var key = ResourceKey.<T>createRegistryKey(new ResourceLocation(MBAPI.MODID, name));
        var reg = new MappedRegistry<>(key, Lifecycle.experimental(), null);
        //noinspection unchecked,rawtypes
        ((WritableRegistry) Registry.REGISTRY).register(key, reg, Lifecycle.experimental());
        return reg;
    }

    public static void init() {
        XplatAbstractions.INSTANCE.registerServerReloadListener(consumer -> consumer.accept(new SimpleJsonResourceReloadListener(new GsonBuilder().create(), "multiblocks") {
            @Override
            protected void apply(final Map<ResourceLocation, JsonElement> raw, final ResourceManager manager, final ProfilerFiller profiler) {
                MULTIBLOCKS.clear();
                raw.forEach((resourceLocation, jsonElement) -> MULTIBLOCKS.put(resourceLocation, Multiblock.CODEC.decode(JsonOps.INSTANCE, jsonElement).getOrThrow(false, LOGGER::warn).getFirst()));
            }
        }));
        XplatAbstractions.INSTANCE.registerDatapackSyncer(new ResourceLocation(MBAPI.MODID, "multiblock"), ResourceLocation.CODEC, Multiblock.CODEC, () -> MULTIBLOCKS, map -> {
            MULTIBLOCKS.clear();
            MULTIBLOCKS.putAll(map);
        });
    }

    @Nullable
    @Override
    public Multiblock getMultiblock(final ResourceLocation id) {
        return MULTIBLOCKS.get(id);
    }

    @Override
    public ResourceLocation getMultiblockId(final Multiblock multiblock) {
        return MULTIBLOCKS.inverse().get(multiblock);
    }

    @Override
    public Codec<? extends StateMatcher> getStateMatcherCodec(final ResourceLocation resourceLocation) {
        return Objects.requireNonNullElse(STATE_MATCHER_TYPE_REGISTRY.get(resourceLocation), BasicMatcher.STRICT_STATE);
    }

    @Override
    public Codec<? extends Multiblock> getMultiblockCodec(final ResourceLocation resourceLocation) {
        return Objects.requireNonNullElse(MULTIBLOCK_TYPE_REGISTRY.get(resourceLocation), DenseMultiblock.CODEC);
    }
}

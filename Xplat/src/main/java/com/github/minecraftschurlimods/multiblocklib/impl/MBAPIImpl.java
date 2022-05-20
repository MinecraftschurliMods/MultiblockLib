package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.xplat.XplatAbstractions;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MBAPIImpl implements MBAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(MBAPI.class);
    private static final Map<ResourceLocation, Multiblock> MULTIBLOCKS = new HashMap<>();
    private static final Registry<Codec<? extends StateMatcher>> STATE_MATCHER_TYPE_REGISTRY = createRegistry("state_matcher_type");
    private static final Registry<Codec<? extends Multiblock>> MULTIBLOCK_TYPE_REGISTRY = createRegistry("multiblock_type");

    private static <T> Registry<T> createRegistry(final String name) {
        var key = ResourceKey.<T>createRegistryKey(new ResourceLocation("multiblocklib", name));
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
    }

    @Override
    public Multiblock getMultiblock(final ResourceLocation id) {
        return MULTIBLOCKS.get(id);
    }

    @Override
    public Codec<? extends StateMatcher> getStateMatcherCodec(final ResourceLocation resourceLocation) {
        return STATE_MATCHER_TYPE_REGISTRY.get(resourceLocation);
    }

    @Override
    public Codec<? extends Multiblock> getMultiblockCodec(final ResourceLocation resourceLocation) {
        return MULTIBLOCK_TYPE_REGISTRY.get(resourceLocation);
    }
}

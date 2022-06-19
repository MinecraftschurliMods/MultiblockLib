package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ServiceLoader;

public interface MBAPI {
    /**
     * The mod id of multiblock lib
     */
    String MODID = "multiblocklib";

    /**
     * The instance of the API
     */
    MBAPI INSTANCE = ServiceLoader.load(MBAPI.class).findFirst().orElseThrow();

    /**
     * The key for the multiblock type registry.
     */
    ResourceKey<Registry<Codec<? extends Multiblock>>> MULTIBLOCK_TYPE_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "multiblock_type"));

    /**
     * The key for the state matcher type registry.
     */
    ResourceKey<Registry<Codec<? extends StateMatcher>>> STATE_MATCHER_TYPE_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "state_matcher_type"));

    /**
     * The key for the multiblock registry.
     */
    ResourceKey<Registry<Multiblock>> MULTIBLOCK_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "multiblock"));

    /**
     * Get the multiblock for the given id.
     *
     * @param id the id of the multiblock to get
     * @return the multiblock for the given id, or null if there is no multiblock for the id
     */
    @Nullable
    Multiblock getMultiblock(ResourceLocation id);

    /**
     * Get the id for the given multiblock.
     *
     * @param multiblock the multiblock to get the id for
     * @return the id for the given multiblock, or null if the multiblock is not associated with an id
     */
    @Nullable
    ResourceLocation getMultiblockId(Multiblock multiblock);

    /**
     * Get the codec for entries of the {@link #MULTIBLOCK_TYPE_REGISTRY}.
     * @return the codec for entries of the {@link #MULTIBLOCK_TYPE_REGISTRY}.
     */
    Codec<Codec<? extends Multiblock>> getMultiblockTypeRegistryCodec();

    /**
     * Get the codec for entries of the {@link #STATE_MATCHER_TYPE_REGISTRY}.
     * @return the codec for entries of the {@link #STATE_MATCHER_TYPE_REGISTRY}.
     */
    Codec<Codec<? extends StateMatcher>> getStateMatcherTypeRegistryCodec();
}

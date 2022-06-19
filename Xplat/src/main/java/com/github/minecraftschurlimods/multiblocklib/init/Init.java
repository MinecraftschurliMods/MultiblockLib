package com.github.minecraftschurlimods.multiblocklib.init;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.matcher.*;
import com.github.minecraftschurlimods.multiblocklib.impl.SparseMultiblock;
import com.github.minecraftschurlimods.multiblocklib.impl.DenseMultiblock;
import com.mojang.serialization.Codec;

public interface Init {
    RegistrationProvider<Codec<? extends Multiblock>> MULTIBLOCK_TYPES = RegistrationProvider.create(MBAPI.MULTIBLOCK_TYPE_REGISTRY, MBAPI.MODID);
    RegistrationProvider<Codec<? extends com.github.minecraftschurlimods.multiblocklib.api.StateMatcher>> STATE_MATCHER_TYPES = RegistrationProvider.create(MBAPI.STATE_MATCHER_TYPE_REGISTRY, MBAPI.MODID);

    RegistryObject<Codec<DenseMultiblock>> DENSE = MULTIBLOCK_TYPES.register("dense", () -> DenseMultiblock.CODEC);
    RegistryObject<Codec<SparseMultiblock>> SPARSE = MULTIBLOCK_TYPES.register("sparse", () -> SparseMultiblock.CODEC);

    RegistryObject<Codec<AirMatcher>> AIR_MATCHER = STATE_MATCHER_TYPES.register("air", () -> AirMatcher.CODEC);
    RegistryObject<Codec<AnyMatcher>> ANY_MATCHER = STATE_MATCHER_TYPES.register("any", () -> AnyMatcher.CODEC);
    RegistryObject<Codec<FluidOnlyMatcher>> FLUID_ONLY_MATCHER = STATE_MATCHER_TYPES.register("fluid_only", () -> FluidOnlyMatcher.CODEC);
    RegistryObject<Codec<DisplayOnlyMatcher>> DISPLAY_ONLY_MATCHER = STATE_MATCHER_TYPES.register("display_only", () -> DisplayOnlyMatcher.CODEC);
    RegistryObject<Codec<TagMatcher>> TAG_MATCHER = STATE_MATCHER_TYPES.register("tag", () -> TagMatcher.CODEC);
    RegistryObject<Codec<BlockMatcher>> LOOSE_BLOCK_MATCHER = STATE_MATCHER_TYPES.register("loose_block", () -> BlockMatcher.LOOSE_CODEC);
    RegistryObject<Codec<BlockMatcher>> STRICT_BLOCK_MATCHER = STATE_MATCHER_TYPES.register("strict_block", () -> BlockMatcher.STRICT_CODEC);
    RegistryObject<Codec<BlockStateMatcher>> STRICT_STATE_MATCHER = STATE_MATCHER_TYPES.register("strict_state", () -> BlockStateMatcher.STRICT_STATE);
    RegistryObject<Codec<BlockStateMatcher>> FILTERED_STATE_MATCHER = STATE_MATCHER_TYPES.register("filtered_state", () -> BlockStateMatcher.FILTERED_STATE);

    static void register() {}
}

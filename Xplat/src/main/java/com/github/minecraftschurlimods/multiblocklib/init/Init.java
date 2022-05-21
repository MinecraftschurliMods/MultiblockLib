package com.github.minecraftschurlimods.multiblocklib.init;

import com.github.minecraftschurlimods.multiblocklib.api.MBAPI;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.SparseMultiblock;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AirMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AnyMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.BasicMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.DisplayOnlyMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.DenseMultiblock;
import com.mojang.serialization.Codec;

public interface Init {
    RegistrationProvider<Codec<? extends Multiblock>> MULTIBLOCK_TYPES = RegistrationProvider.create(MBAPI.MULTIBLOCK_TYPE_REGISTRY, "multiblocklib");
    RegistrationProvider<Codec<? extends StateMatcher>> STATE_MATCHER_TYPES = RegistrationProvider.create(MBAPI.STATE_MATCHER_TYPE_REGISTRY, "multiblocklib");

    RegistryObject<Codec<DenseMultiblock>> DENSE = MULTIBLOCK_TYPES.register("dense", () -> DenseMultiblock.CODEC);
    RegistryObject<Codec<SparseMultiblock>> SPARSE = MULTIBLOCK_TYPES.register("sparse", () -> SparseMultiblock.CODEC);

    RegistryObject<Codec<AirMatcher>> AIR_MATCHER = STATE_MATCHER_TYPES.register("air", () -> AirMatcher.CODEC);
    RegistryObject<Codec<AnyMatcher>> ANY_MATCHER = STATE_MATCHER_TYPES.register("any", () -> AnyMatcher.CODEC);
    RegistryObject<Codec<DisplayOnlyMatcher>> DISPLAY_ONLY_MATCHER = STATE_MATCHER_TYPES.register("display_only", () -> DisplayOnlyMatcher.CODEC);
    RegistryObject<Codec<BasicMatcher>> LOOSE_BLOCK_MATCHER = STATE_MATCHER_TYPES.register("loose_block", () -> BasicMatcher.LOOSE_BLOCK);
    RegistryObject<Codec<BasicMatcher>> STRICT_BLOCK_MATCHER = STATE_MATCHER_TYPES.register("strict_block", () -> BasicMatcher.STRICT_BLOCK);
    RegistryObject<Codec<BasicMatcher>> STRICT_STATE_MATCHER = STATE_MATCHER_TYPES.register("strict_state", () -> BasicMatcher.STRICT_STATE);
    RegistryObject<Codec<BasicMatcher>> FILTERED_STATE_MATCHER = STATE_MATCHER_TYPES.register("filtered_state", () -> BasicMatcher.FILTERED_STATE);

    static void register() {}
}

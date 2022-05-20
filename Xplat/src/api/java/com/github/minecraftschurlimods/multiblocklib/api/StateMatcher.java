package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public interface StateMatcher {
    Codec<StateMatcher> CODEC = ResourceLocation.CODEC.dispatch("type", StateMatcher::getType, MBAPI.INSTANCE::getStateMatcherCodec);

    ResourceLocation getType();

    BlockState displayState(final long gameTime);
    boolean test(BlockStateMatchContext context);
}

package com.github.minecraftschurlimods.multiblocklib.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.Collection;

public interface Multiblock {
    Codec<Multiblock> CODEC = ResourceLocation.CODEC.dispatch("type", Multiblock::getType, MBAPI.INSTANCE::getMultiblockCodec);
    ResourceLocation getType();
    boolean isSymmetrical();
    Vec3i size();
    void place(Level level, BlockPos anchorPos, Rotation rotation, Mirror mirror);
    boolean matches(BlockGetter level, BlockPos anchorPos, Rotation rotation, Mirror mirror);
    Pair<Rotation, Mirror> matches(BlockGetter level, BlockPos anchorPos);
    Collection<SimulateResult> simulate(BlockGetter level, BlockPos anchorPos, Rotation rotation, Mirror mirror);
    interface SimulateResult {
        BlockPos worldPos();
        StateMatcher stateMatcher();
        boolean test(BlockGetter level, Rotation rotation, Mirror mirror);
    }
}

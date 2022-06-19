package com.github.minecraftschurlimods.multiblocklib.client;

import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FakeMBLevel implements BlockAndTintGetter {
    private final transient Map<BlockPos, BlockEntity> teCache = new HashMap<>();
    private final Vec3i size;
    private final Map<BlockPos, StateMatcher> cache = new HashMap<>();

    private FakeMBLevel(Multiblock multiblock, BlockPos anchor, Rotation rotation, Mirror mirror, Multiblock.SimulateFilter filter) {
        this(multiblock.size(), multiblock.simulate(anchor, rotation, mirror, filter));
    }

    FakeMBLevel(Vec3i size, Collection<Multiblock.SimulateResult> simulate) {
        this.size = size;
        for (final Multiblock.SimulateResult simulateResult : simulate) {
            cache.put(simulateResult.worldPos(), simulateResult.stateMatcher());
        }
    }

    @Override
    public float getShade(Direction dir, boolean var2) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }

    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int ambientDarkening) {
        return getBrightness(LightLayer.BLOCK, pos) - ambientDarkening;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver color) {
        var plains = RegistryAccess.BUILTIN.get().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
        return color.getColor(plains, pos.getX(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState state = getBlockState(pos);
        if (state.getBlock() instanceof EntityBlock) {
            return teCache.computeIfAbsent(pos.immutable(), p -> ((EntityBlock) state.getBlock()).newBlockEntity(pos, state));
        }
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (x < 0 || y < 0 || z < 0 || x >= size.getX() || y >= size.getY() || z >= size.getZ()) {
            return Blocks.AIR.defaultBlockState();
        }
        long ticks = getClientLevel().getGameTime();
        var m = cache.get(pos);
        if (m == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return m.displayState(ticks);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return getClientLevel().getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return getClientLevel().getMinBuildHeight();
    }

    private static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
}

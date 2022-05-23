package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.Util;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SparseMultiblock extends AbstractMultiblock {
    public static final Codec<SparseMultiblock> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(Util.CHAR_CODEC, BlockPos.CODEC.listOf()).fieldOf("structure").forGetter(sparseMultiblock -> sparseMultiblock.structure),
            Codec.unboundedMap(Util.CHAR_CODEC, StateMatcher.CODEC).fieldOf("mapping").forGetter(sparseMultiblock -> removeDefaultMappings(sparseMultiblock.mapping)),
            Codec.BOOL.optionalFieldOf("symmetrical", false).forGetter(Multiblock::isSymmetrical)
    ).apply(inst, SparseMultiblock::make));

    private final Map<BlockPos, StateMatcher> data;
    private final Map<Character, List<BlockPos>> structure;
    private final Map<Character, StateMatcher> mapping;

    private SparseMultiblock(final Map<Character, List<BlockPos>> structure, final Map<Character, StateMatcher> mapping, final boolean symmetrical) {
        this(makeData(structure, mapping), structure, mapping, symmetrical);
    }

    private SparseMultiblock(final Map<BlockPos, StateMatcher> data, final Map<Character, List<BlockPos>> structure, final Map<Character, StateMatcher> mapping, final boolean symmetrical) {
        super(symmetrical, calculateSize(data));
        this.data = data;
        this.structure = structure;
        this.mapping = mapping;
    }

    public static SparseMultiblock make(final Map<Character, List<BlockPos>> structure, final Map<Character, StateMatcher> mapping, final Boolean symmetrical) {
        return new SparseMultiblock(structure, addDefaultMappings(mapping), symmetrical);
    }

    private static Map<BlockPos, StateMatcher> makeData(final Map<Character, List<BlockPos>> structure, final Map<Character, StateMatcher> mapping) {
        ImmutableMap.Builder<BlockPos, StateMatcher> builder = ImmutableMap.builder();
        for (var entry : structure.entrySet()) {
            char key = entry.getKey();
            if (!mapping.containsKey(key)) {
                throw new IllegalArgumentException("No mapping for key " + key);
            }
            for (var pos : entry.getValue()) {
                builder.put(pos, mapping.get(key));
            }
        }
        return builder.build();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Vec3i calculateSize(final Map<BlockPos, StateMatcher> data) {
        if (data.isEmpty()) return Vec3i.ZERO;
        int minX = data.keySet().stream().mapToInt(BlockPos::getX).min().getAsInt();
        int maxX = data.keySet().stream().mapToInt(BlockPos::getX).max().getAsInt();
        int minY = data.keySet().stream().mapToInt(BlockPos::getY).min().getAsInt();
        int maxY = data.keySet().stream().mapToInt(BlockPos::getY).max().getAsInt();
        int minZ = data.keySet().stream().mapToInt(BlockPos::getZ).min().getAsInt();
        int maxZ = data.keySet().stream().mapToInt(BlockPos::getZ).max().getAsInt();
        return new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
    }

    @Override
    public ResourceLocation getType() {
        return Init.SPARSE.getId();
    }

    @Override
    public Collection<SimulateResult> simulate(final BlockPos anchorPos, final Rotation rotation, final Mirror mirror) {
        return data.entrySet()
                .stream()
                .<SimulateResult>map(entry -> new SimulateResultImpl(entry.getValue(), anchorPos.offset(entry.getKey().rotate(rotation)), rotation, mirror))
                .toList();
    }

    @Override
    public boolean equals(final AbstractMultiblock o) {
        final SparseMultiblock that = (SparseMultiblock) o;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}

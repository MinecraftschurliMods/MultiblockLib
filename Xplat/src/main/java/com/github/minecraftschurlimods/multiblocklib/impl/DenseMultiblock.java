package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.Util;
import com.github.minecraftschurlimods.multiblocklib.api.Multiblock;
import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.*;

public class DenseMultiblock extends AbstractMultiblock {
    public static final Codec<DenseMultiblock> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.xmap(String::toCharArray, String::new).listOf().listOf().flatXmap(lists -> {
                char[][][] array = new char[lists.size()][][];
                int s = 0;
                for (int i = 0; i < lists.size(); i++) {
                    List<char[]> strings = lists.get(i);
                    if (i == 0) {
                        s = strings.size();
                    } else if (strings.size() != s) {
                        return DataResult.error("All rows must be the same length");
                    }
                    array[i] = new char[strings.size()][];
                    int s2 = 0;
                    for (int j = 0; j < strings.size(); j++) {
                        char[] str = strings.get(j);
                        if (j == 0) {
                            s2 = str.length;
                        } else if (str.length != s2) {
                            return DataResult.error("All columns must be the same length");
                        }
                        array[i][j] = str;
                    }
                }
                return DataResult.success(array);
            }, array -> {
                List<List<char[]>> list = new ArrayList<>();
                for (final char[][] strings : array) {
                    list.add(Arrays.asList(strings));
                }
                return DataResult.success(list);
            }).fieldOf("pattern").forGetter(denseMultiblock -> denseMultiblock.structure),
            Codec.unboundedMap(Util.CHAR_CODEC, StateMatcher.CODEC).fieldOf("mapping").forGetter(denseMultiblock -> denseMultiblock.mapping),
            Codec.BOOL.optionalFieldOf("symmetrical", false).forGetter(Multiblock::isSymmetrical)
    ).apply(inst, DenseMultiblock::new));

    private final char[][][] structure;
    private final Map<Character, StateMatcher> mapping;
    private final BlockPos origin;

    public DenseMultiblock(char[][][] structure, Map<Character, StateMatcher> mapping, boolean symmetrical) {
        super(symmetrical, new Vec3i(structure.length, structure[0].length, structure[0][0].length));
        this.structure = structure;
        this.mapping = addDefaultMappings(mapping);
        BlockPos origin = null;
        for (int x = 0; x < structure.length; x++) {
            for (int y = 0; y < structure[0].length; y++) {
                for (int z = 0; z < structure[0][0].length; z++) {
                    char key = structure[x][y][z];
                    if (!this.mapping.containsKey(key)) {
                        throw new IllegalArgumentException("No state matcher found for key " + key);
                    }
                    if (key == '0') {
                        if (origin != null) {
                            throw new IllegalArgumentException("Multiple origins found");
                        }
                        origin = new BlockPos(x, y, z);
                    }
                }
            }
        }
        if (origin == null) {
            throw new IllegalArgumentException("No origin found");
        }
        this.origin = origin;
    }

    @Override
    public Codec<? extends Multiblock> codec() {
        return CODEC;
    }

    @Override
    public Collection<SimulateResult> simulate(BlockPos anchorPos, Rotation rotation, Mirror mirror, SimulateFilter filter) {
        List<SimulateResult> results = new ArrayList<>();
        for (int x = 0; x < this.structure.length; x++) {
            for (int y = 0; y < this.structure[0].length; y++) {
                for (int z = 0; z < this.structure[0][0].length; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!filter.test(blockPos)) continue;
                    BlockPos pos = anchorPos.offset(blockPos.subtract(this.origin).rotate(rotation));
                    results.add(new SimulateResultImpl(this.mapping.get(this.structure[x][y][z]), pos, rotation, mirror));
                }
            }
        }
        return Collections.unmodifiableCollection(results);
    }

    @Override
    public boolean equals(AbstractMultiblock o) {
        final DenseMultiblock that = (DenseMultiblock) o;

        if (!Arrays.deepEquals(structure, that.structure)) {
            return false;
        }
        return mapping.equals(that.mapping);
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(structure);
        result = 31 * result + mapping.hashCode();
        return result;
    }
}

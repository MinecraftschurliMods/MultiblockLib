package com.github.minecraftschurlimods.multiblocklib.impl;

import com.github.minecraftschurlimods.multiblocklib.api.StateMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AirMatcher;
import com.github.minecraftschurlimods.multiblocklib.impl.matcher.AnyMatcher;
import com.github.minecraftschurlimods.multiblocklib.init.Init;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            Codec.unboundedMap(Codec.STRING.flatXmap(s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error("Expected single character not string"), c -> DataResult.success(String.valueOf(c))), StateMatcher.CODEC).fieldOf("stateMatchers").forGetter(denseMultiblock -> denseMultiblock.mapping),
            Codec.BOOL.optionalFieldOf("symmetrical", false).forGetter(denseMultiblock -> denseMultiblock.symmetrical)
    ).apply(inst, DenseMultiblock::new));

    private final char[][][] structure;
    private final Map<Character, StateMatcher> mapping;
    private final StateMatcher[][][] mappedStructure;
    private final BlockPos origin;

    public DenseMultiblock(char[][][] structure, Map<Character, StateMatcher> mapping, boolean symmetrical) {
        super(symmetrical, new Vec3i(structure.length, structure[0].length, structure[0][0].length));
        this.structure = structure;
        this.mapping = mapping;
        this.mapping.putIfAbsent(' ', new AirMatcher());
        this.mapping.putIfAbsent('_', new AnyMatcher());
        this.mapping.putIfAbsent('0', new AirMatcher());
        this.mappedStructure = new StateMatcher[structure.length][structure[0].length][structure[0][0].length];
        BlockPos origin = null;
        for (int x = 0; x < structure.length; x++) {
            for (int y = 0; y < structure[0].length; y++) {
                for (int z = 0; z < structure[0][0].length; z++) {
                    char key = structure[x][y][z];
                    if (key == '0') {
                        if (origin != null) {
                            throw new IllegalArgumentException("Multiple origins found");
                        }
                        origin = new BlockPos(x, y, z);
                    }
                    this.mappedStructure[x][y][z] = mapping.get(key);
                }
            }
        }
        if (origin == null) {
            throw new IllegalArgumentException("No origin found");
        }
        this.origin = origin;
    }

    @Override
    public ResourceLocation getType() {
        return Init.DENSE.getId();
    }

    @Override
    public Collection<SimulateResult> simulate(BlockGetter level, BlockPos anchorPos, Rotation rotation, Mirror mirror) {
        BlockPos root = anchorPos.subtract(this.origin);
        List<SimulateResult> results = new ArrayList<>();
        for (int x = 0; x < this.mappedStructure.length; x++) {
            for (int y = 0; y < this.mappedStructure[0].length; y++) {
                for (int z = 0; z < this.mappedStructure[0][0].length; z++) {
                    results.add(new SimulateResultImpl(this.mappedStructure[x][y][z], root.offset(new BlockPos(x, y, z).rotate(rotation))));
                }
            }
        }
        return Collections.unmodifiableCollection(results);
    }

    private record SimulateResultImpl(StateMatcher stateMatcher, BlockPos worldPos) implements SimulateResult {
        @Override
        public boolean test(final BlockGetter blockGetter, final Rotation rotation, final Mirror mirror) {
            return stateMatcher().test(new SimpleBlockStateMatchContext(worldPos(), blockGetter, rotation, mirror));
        }
    }
}

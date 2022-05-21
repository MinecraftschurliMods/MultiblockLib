package com.github.minecraftschurlimods.multiblocklib;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class Util {
    public static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.either(BlockState.CODEC, Codec.STRING.flatXmap(s -> {
        try {
            BlockStateParser parser = new BlockStateParser(new StringReader(s), true).parse(false);
            BlockState state = parser.getState();
            return DataResult.success(state);
        } catch (CommandSyntaxException e) {
            return DataResult.error(e.getMessage());
        }
    }, blockState -> DataResult.success(blockState.toString()))).xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);
    public static final Codec<Character> CHAR_CODEC = Codec.STRING.flatXmap(s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error("Expected single character not string"), c -> DataResult.success(String.valueOf(c)));
}

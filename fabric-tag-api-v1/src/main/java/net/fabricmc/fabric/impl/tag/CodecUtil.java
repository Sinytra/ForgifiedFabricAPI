package net.fabricmc.fabric.impl.tag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Arrays;
import java.util.stream.Stream;

public class CodecUtil {
	public static <T> MapCodec<T> aliasedField(Codec<T> codec, T defaultValue,
	                                           String canonical, String... aliases) {
		return new MapCodec<>() {
			@Override
			public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
				O value = input.get(canonical);
				for (int j = 0; value == null && j < aliases.length; j++)
					value = input.get(aliases[j]);
				return value == null ? DataResult.success(defaultValue) : codec.parse(ops, value);
			}

			@Override
			public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
				return prefix.add(canonical, codec.encodeStart(ops, input));
			}

			@Override
			public <O> Stream<O> keys(DynamicOps<O> ops) {
				return Stream.concat(Stream.of(canonical), Arrays.stream(aliases)).map(ops::createString);
			}
		};
	}
}

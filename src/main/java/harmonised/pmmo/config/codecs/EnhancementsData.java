package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.util.Functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public record EnhancementsData(
		boolean override,
		Map<Integer, Map<String, Long>> skillArray) implements DataSource<EnhancementsData>{
	
	public EnhancementsData() {this(false, new HashMap<>());}
	
	public static final MapCodec<EnhancementsData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(cme -> Optional.of(cme.override())),
			CodecTypes.LONG_CODEC.listOf().xmap(list -> {
				Map<Integer, Map<String, Long>> dataOut = new HashMap<>();
				for (int i = 0; i < list.size(); i++) {
					dataOut.put(i, list.get(i));
				}
				return dataOut;
			}, map -> {
				List<Map<String, Long>> dataOut = new ArrayList<>();
				for (int i = 0; i <= map.keySet().stream().max(Integer::compare).orElse(0); i++) {
					dataOut.add(map.getOrDefault(i, new HashMap<>()));
				}
				return dataOut;
			}).fieldOf("levels").forGetter(EnhancementsData::skillArray)
		).apply(instance, (o, map) -> new EnhancementsData(o.orElse(false), new HashMap<>(map))));
	
	@Override
	public EnhancementsData combine(EnhancementsData two) {
		Map<Integer, Map<String, Long>> skillArray = new HashMap<>();
		
		BiConsumer<EnhancementsData, EnhancementsData> bothOrNeither = (o, t) -> {
			Map<Integer, Map<String, Long>> combinedMap = new HashMap<>(o.skillArray());
			t.skillArray().forEach((lvl, map) -> {
				combinedMap.merge(lvl, map, (oldMap, newMap) -> {
					newMap.forEach((skill, level) -> {
						oldMap.merge(skill, level, Long::max);
					});
					return oldMap;
				});
			});
		};
		Functions.biPermutation(this, two, this.override(), two.override(), 
				(o, t) -> {
					skillArray.clear();
					skillArray.putAll(o.skillArray().isEmpty() ? t.skillArray() : o.skillArray());
				}, 
				bothOrNeither, 
				bothOrNeither);
		
		return new EnhancementsData(this.override() || two.override(), skillArray);
	}

	@Override
	public boolean isUnconfigured() {
		return skillArray().isEmpty();
	}


	

	
}

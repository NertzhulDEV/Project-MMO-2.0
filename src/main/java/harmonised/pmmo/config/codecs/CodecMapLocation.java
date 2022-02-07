package harmonised.pmmo.config.codecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.codecs.CodecTypes.*;
import harmonised.pmmo.config.readers.ModifierDataType;
import net.minecraft.resources.ResourceLocation;

public record CodecMapLocation (
	Optional<List<ResourceLocation>> tagValues,
	Optional<ModifierData> bonusMap,
	Optional<Map<ResourceLocation, Integer>> positive,
	Optional<Map<ResourceLocation, Integer>> negative,
	Optional<List<ResourceLocation>> veinBlacklist,
	Optional<Map<String, Integer>> travelReq,
	Optional<MobMultiplierData> mobModifiers) {
	
	public static final Codec<CodecMapLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("isTagFor").forGetter(CodecMapLocation::tagValues),
			CodecTypes.MODIFIER_CODEC.optionalFieldOf("bonus").forGetter(CodecMapLocation::bonusMap),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("positive_effect").forGetter(CodecMapLocation::positive),
			Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("negative_effect").forGetter(CodecMapLocation::negative),
			Codec.list(ResourceLocation.CODEC).optionalFieldOf("vein_blacklist").forGetter(CodecMapLocation::veinBlacklist),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("travel_req").forGetter(CodecMapLocation::travelReq),
			CodecTypes.MOB_MULTIPLIER_CODEC.optionalFieldOf("mob_multiplier").forGetter(CodecMapLocation::mobModifiers)
			).apply(instance, CodecMapLocation::new));
	
	public static record LocationMapContainer (
		List<ResourceLocation> tagValues,
		Map<ModifierDataType, Map<String, Double>> bonusMap,
		Map<ResourceLocation, Integer> positive,
		Map<ResourceLocation, Integer> negative,
		List<ResourceLocation> veinBlacklist ,
		Map<String, Integer> travelReq,
		Map<ResourceLocation, Map<String, Double>> mobModifiers) {
		
		public LocationMapContainer(CodecMapLocation src) {
			this(src.tagValues().isPresent() ? src.tagValues().get() : new ArrayList<>(),
			src.bonusMap().isPresent() ? src.bonusMap().get().obj() : new HashMap<>(),
			src.positive().orElseGet(() -> new HashMap<>()),
			src.negative().orElseGet(() -> new HashMap<>()),
			src.veinBlacklist().orElseGet(() -> new ArrayList<>()),
			src.travelReq().orElseGet(() -> new HashMap<>()),
			src.mobModifiers().isPresent() ? src.mobModifiers().get().obj() : new HashMap<>());
		}
		public LocationMapContainer() {
			this(new ArrayList<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
		}
		
		public static LocationMapContainer combine(LocationMapContainer one, LocationMapContainer two) {
			List<ResourceLocation> tagValues = new ArrayList<>(one.tagValues());
			two.tagValues.forEach((rl) -> {
				if (!tagValues.contains(rl))
					tagValues.add(rl);
			});
			Map<ModifierDataType, Map<String, Double>> bonusMap = new HashMap<>(one.bonusMap());
			bonusMap.putAll(two.bonusMap);
			Map<ResourceLocation, Integer> positive = new HashMap<>(one.positive());
			positive.putAll(two.positive);
			Map<ResourceLocation, Integer> negative = new HashMap<>(one.negative());
			negative.putAll(two.negative);
			List<ResourceLocation> veinBlacklist = new ArrayList<>(one.veinBlacklist());
			two.veinBlacklist.forEach((rl) -> {
				if (!veinBlacklist.contains(rl)) 
					veinBlacklist.add(rl);
			});
			Map<String, Integer> travelReq = new HashMap<>(one.travelReq());
			travelReq.putAll(two.travelReq);
			Map<ResourceLocation, Map<String, Double>> mobModifiers = new HashMap<>(one.mobModifiers());
			mobModifiers.putAll(two.mobModifiers);
			return new LocationMapContainer(tagValues, bonusMap, positive, negative, veinBlacklist, travelReq, mobModifiers);
		}
	}
}
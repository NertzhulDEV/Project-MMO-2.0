package harmonised.pmmo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ForgeConfigSpec;

public class PerksConfig {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	private static final Codec<Map<EventType, Map<String, List<CompoundTag>>>> CODEC = 
			Codec.unboundedMap(EventType.CODEC, 
					Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC.listOf()));
	
	static {
		generateDefaults();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		buildPerkSettings(SERVER_BUILDER);
		
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ConfigObject<Map<EventType, Map<String, List<CompoundTag>>>> PERK_SETTINGS;
	private static Map<EventType, Map<String, List<CompoundTag>>> defaultSettings;
	
	private static void buildPerkSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("These settings define which perks are used and the settings which govern them.").push("Perks");
		
		PERK_SETTINGS = TomlConfigHelper.defineObject(builder, "For_Event", CODEC, defaultSettings);
		
		builder.pop();
	}
	
	private static void generateDefaults() {
		defaultSettings = new HashMap<>();
		Map<String, List<CompoundTag>> bodyMap = new HashMap<>();
		
		//====================BREAK SPEED DEFAULTS========================
		bodyMap.put("mining", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("pickaxe_dig", 0.05).build()));
		bodyMap.put("excavation", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("shovel_dig", 0.05).build()));
		bodyMap.put("woodcutting", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("axe_dig", 0.05).build()));
		bodyMap.put("farming", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("hoe_dig", 0.05).withDouble("shears_dig", 0.05).build()));
		bodyMap.put("combat", List.of(TagBuilder.start().withString("perk", "pmmo:break_speed").withInt("modifier", 1000).withDouble("sword_dig", 0.05).build()));
		defaultSettings.put(EventType.BREAK_SPEED, bodyMap);
		bodyMap = new HashMap<>();
		//TODO finish the fireworks defaults
		//====================SKILL_UP DEFAULTS==========================
		bodyMap.put("combat", List.of(
				TagBuilder.start().withString("perk", "pmmo:damage").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString("skill", "combat").build()));
		bodyMap.put("endurance", List.of(
				TagBuilder.start().withString("perk", "pmmo:health").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString("skill", "endurance").build()));
		bodyMap.put("building", List.of(
				TagBuilder.start().withString("perk", "pmmo:reach").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString("skill", "building").build()));
		bodyMap.put("agility", List.of(
				TagBuilder.start().withString("perk", "pmmo:speed").build(),
				TagBuilder.start().withString("perk", "pmmo:fireworks").withString("skill", "agility").build()));
		defaultSettings.put(EventType.SKILL_UP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.005).build()));
		defaultSettings.put(EventType.JUMP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.01).build()));
		defaultSettings.put(EventType.SPRINT_JUMP, bodyMap);
		bodyMap = new HashMap<>();
				
		//=====================JUMP DEFAULTS=============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:jump_boost").withDouble("per_level", 0.015).build()));
		defaultSettings.put(EventType.CROUCH_JUMP, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================SUBMERGED DEFAULTS========================
		bodyMap.put("swimming", List.of(
				TagBuilder.start().withString("perk", "pmmo:breath").build(),
				TagBuilder.start().withString("perk", "pmmo:night_vision").build()));
		defaultSettings.put(EventType.SUBMERGED, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================FROM_IMPACT==============================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:fall_save").withDouble("per_level", 0.05).build()));
		bodyMap.put("endurance", List.of(TagBuilder.start().withString("perk", "pmmo:fall_save").withDouble("per_level", 0.25).build()));
		defaultSettings.put(EventType.FROM_IMPACT, bodyMap);
		bodyMap = new HashMap<>();
		
		//=====================DEAL_RANGED_DAMAGE=======================
		bodyMap.put("agility", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString("applies_to", "archeryWeapon").build()));
		bodyMap.put("magic", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString("applies_to", "magicWeapon").build()));
		bodyMap.put("gunslinging", List.of(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString("applies_to", "gunGood Weapon").build()));
		defaultSettings.put(EventType.DEAL_RANGED_DAMAGE, bodyMap);
	}
	
	
}

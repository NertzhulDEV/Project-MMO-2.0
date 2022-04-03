package harmonised.pmmo.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.config.readers.ModifierDataType;
import harmonised.pmmo.config.readers.TomlConfigHelper;
import harmonised.pmmo.config.readers.TomlConfigHelper.ConfigObject;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class Config {
	public static ForgeConfigSpec CLIENT_CONFIG;
	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		
		setupClient(CLIENT_BUILDER);
		setupCommon(COMMON_BUILDER);
		setupServer(SERVER_BUILDER);
		
		CLIENT_CONFIG = CLIENT_BUILDER.build();
		COMMON_CONFIG = COMMON_BUILDER.build();
		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	//====================CLIENT SETTINGS===============================
	
	private static void setupClient(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Client Configuration").push("Client");
		
		buildGUI(builder);
		buildTooltips(builder);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_X;
	public static ForgeConfigSpec.ConfigValue<Double> SKILL_LIST_OFFSET_Y;
	public static ForgeConfigSpec.ConfigValue<Boolean> SKILL_LIST_DISPLAY;
	
	private static void buildGUI(ForgeConfigSpec.Builder builder) {
		builder.comment("Configuration settings for the guis").push("GUI");
		
		SKILL_LIST_OFFSET_X = builder.comment("how far right from the top left corner the skill list should be")
						.define("Skill List Xoffset", 0d);
		SKILL_LIST_OFFSET_Y = builder.comment("how far down from the top left corner the skill list should be")
						.define("Skill List Yoffset", 0d);
		SKILL_LIST_DISPLAY = builder.comment("Should the skill list be displayed")
						.define("Display Skill List", true);
		
		builder.pop();
	}
	
	public static BooleanValue HIDE_MET_REQS;
	
	private static BooleanValue[] TOOLTIP_REQ_ENABLED;
	private static BooleanValue[] TOOLTIP_XP_ENABLED;
	private static BooleanValue[] TOOLTIP_BONUS_ENABLED;
	
	private static final String TOOLTIP_SUFFIX = " tooltip enabled";
	
	public static BooleanValue tooltipReqEnabled(ReqType type) {return TOOLTIP_REQ_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipXpEnabled(EventType type) {return TOOLTIP_XP_ENABLED[type.ordinal()];}
	public static BooleanValue tooltipBonusEnabled(ModifierDataType type) {return TOOLTIP_BONUS_ENABLED[type.ordinal()];}
	
	private static void buildTooltips(ForgeConfigSpec.Builder builder) {
		builder.comment("").push("ToolTip_Settings");
		HIDE_MET_REQS = builder.comment("Should met reqs be hidden on the tooltip.")
						.define("Hide Met Req Tooltips", true);
		builder.pop();
		builder.comment("This section covers the various tooltip elements and whether they should be enabled").push("Tooltip_Visibility");
		
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		builder.push("Requirement_Tooltips");
		TOOLTIP_REQ_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+" Req "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //requirements
		
		List<EventType> rawEventList = new ArrayList<>(Arrays.asList(EventType.values()));
		builder.push("Xp_Gain_Tooltips");
		TOOLTIP_XP_ENABLED = rawEventList.stream().map((t) -> {
			return builder.define(t.toString()+" XP Gain "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //xp gains
		
		List<ModifierDataType> rawBonusList = new ArrayList<>(Arrays.asList(ModifierDataType.values()));
		builder.push("Bonus_Tooltips");
		TOOLTIP_BONUS_ENABLED = rawBonusList.stream().map((t) -> {
			return builder.define(t.toString()+" Bonus "+TOOLTIP_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		builder.pop(); //bonuses
		
		builder.pop(); //outer
	}
	
	//====================COMMON SETTINGS===============================
	
	private static void setupCommon(ForgeConfigSpec.Builder builder) {
		builder.comment("===============================================","","",
				"Most Configurations are found in the server config",
				"You can find that in worldname/serverconfig/",
				"","","===============================================").push("Common");
		
		buildMsLoggy(builder);
		
		builder.pop(); //Common Blocks
	}
	
	public static ForgeConfigSpec.ConfigValue<Boolean> INFO_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> DEBUG_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> WARN_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> ERROR_LOGGING;
	public static ForgeConfigSpec.ConfigValue<Boolean> FATAL_LOGGING;
	
	private static void buildMsLoggy(ForgeConfigSpec.Builder builder) {
		builder.comment("PMMO Error Logging Configuration").push("Ms Loggy");
		
		INFO_LOGGING = builder.comment("Should MsLoggy info logging be enabled?  This will flood your log with data, but provides essential details",
									  " when trying to find data errors and bug fixing.  ")
						.define("Info Logging", false);
		DEBUG_LOGGING = builder.comment("Should MsLoggy debug logging be enabled?  This will flood your log with data, but provides essential details",
				  					  " when trying to find bugs. DEVELOPER SETTING (mostly).  ")
						.define("Debug Logging", false);
		WARN_LOGGING = builder.comment("Should MsLoggy warn logging be enabled?  This log type is helpful for catching important but non-fatal issues")
						.define("Warn Logging", true);
		ERROR_LOGGING = builder.comment("Should Error Logging be enabled.  it is highly recommended this stay true.  however, you can",
									  "disable it to remove pmmo errors from the log.")
						.define("Error Logging", true);
		FATAL_LOGGING = builder.comment("Should MsLoggy fatal logging be enabled?  I can't imagine a situation where you'd want this off, but here you go.")
						.define("Fatal Logging", true);
		
		builder.pop(); //Ms. Loggy Block
	}
	
	//====================SERVER SETTINGS===============================	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		buildBasics(builder);
		buildLevels(builder);
		buildRequirements(builder);
		buildXpGains(builder);
		buildPartySettings(builder);
		buildMobScalingSettings(builder);
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> CREATIVE_REACH;
	
	private static void buildBasics(ForgeConfigSpec.Builder builder) {
		builder.comment("General settings on the server").push("General");
		
		CREATIVE_REACH = builder.comment("how much extra reach should a player get in creative mode")
				.defineInRange("Creative Reach", 50d, 4d, Double.MAX_VALUE);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Integer> 	MAX_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	LOSS_ON_DEATH;
	public static ForgeConfigSpec.ConfigValue<Boolean>	LOSE_LEVELS_ON_DEATH;
	public static ForgeConfigSpec.ConfigValue<Boolean> 	LOSE_ONLY_EXCESS;
	public static ForgeConfigSpec.ConfigValue<Boolean> 	USE_EXPONENTIAL_FORUMULA;
	public static ForgeConfigSpec.ConfigValue<Long> 	LINEAR_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	LINEAR_PER_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Integer> 	EXPONENTIAL_BASE_XP;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_POWER_BASE;
	public static ForgeConfigSpec.ConfigValue<Double> 	EXPONENTIAL_LEVEL_MOD;
	
	private static void buildLevels(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related level gain").push("Levels");
		
		MAX_LEVEL = builder.comment("The highest level a player can acheive in any skill.")
						.defineInRange("Max Level", 1523, 1, Integer.MAX_VALUE);
		USE_EXPONENTIAL_FORUMULA = builder.comment("shold levels be determined using an exponential forumula?")
						.define("Use Exponential Formula", true);
		LOSS_ON_DEATH = builder.comment("How much experience should players lose when they die?"
						, "zero is no loss, one is lose everything")
						.defineInRange("Loss on death", 0.05, 0d, 1d);
		LOSE_LEVELS_ON_DEATH = builder.comment("should loss of experience cross levels?"
						, "for example, if true, a player with 1 xp above their current level would lose the"
						, "[Loss on death] percentage of xp and fall below their current level.  However,"
						, "if false, the player would lose only 1 xp as that would put them at the base xp of their current level")
						.define("Lose Levels On Death", false);
		LOSE_ONLY_EXCESS = builder.comment("This setting only matters if [Lose Level On Death] is set to false."
						, "If this is true the [Loss On Death] applies only to the experience above the current level"
						, "for exmample if level 3 is 1000k xp and the player has 1020 and dies.  the player will only lose"
						, "the [Loss On Death] of the 20 xp above the level's base.")
						.define("Lose Only Excess", true);
		
		//========LINEAR SECTION===============
		builder.comment("Settings for Linear XP configuration").push("LINEAR LEVELS");
		LINEAR_BASE_XP = builder.comment("what is the base xp to reach level 2 (this + level * xpPerLevel)")
							.defineInRange("Base XP", 250l, 1l, Long.MAX_VALUE);
		LINEAR_PER_LEVEL = builder.comment("What is the xp increase per level (baseXp + level * this)")
							.defineInRange("Per Level", 50d, 1d, Double.MAX_VALUE);		
		builder.pop(); //COMPLETE LINEAR BLOCK
		
		//========EXPONENTIAL SECTION==========
		builder.comment("Settings for Exponential XP configuration").push("EXPONENTIAL LEVELS");
		EXPONENTIAL_BASE_XP = builder.comment("What is the x in: x * ([Power Base]^([Per Level] * level))")
							.defineInRange("Base XP", 83, 1, Integer.MAX_VALUE);
		EXPONENTIAL_POWER_BASE = builder.comment("What is the x in: [Base XP] * (x^([Per Level] * level))")
							.defineInRange("Power Base", 1.104088404342588d, 0d, Double.MAX_VALUE);
		EXPONENTIAL_LEVEL_MOD = builder.comment("What is the x in: [Base XP] * ([Power Base]^(x * level))")
							.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);
		builder.pop(); //COMPLETE EXPONENTIAL BLOCK
		builder.pop(); //COMPLETE LEVELS BLOCK
		
	}
	
	private static BooleanValue[] REQ_ENABLED;
	private static BooleanValue[] STRICT_REQS;
	
	private static final String REQ_ENABLED_SUFFIX = " Req Enabled";
	private static final String STRICT_REQ_SUFFIX  = " Req Strict";
	
	public static BooleanValue reqEnabled(ReqType type) {return REQ_ENABLED[type.ordinal()];}
	public static BooleanValue reqStrict(ReqType type) {return STRICT_REQS[type.ordinal()];}
	
	private static void buildRequirements(ForgeConfigSpec.Builder builder) {
		List<ReqType> rawReqList = new ArrayList<>(Arrays.asList(ReqType.values()));
		
		builder.comment("Settngs governing requirements at the macro level").push("Requirements");
		builder.comment("Should requirements apply for the applicable action type").push("Req_Enabled");		
		
		REQ_ENABLED = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+REQ_ENABLED_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
		builder.comment("Should requirements be strictly enforced?"
				, "if no, then requirements will have certain penalties applied"
				, "proportionate to the gap between the player and the requirement").push("Strict_Reqs");
		
		STRICT_REQS = rawReqList.stream().map((t) -> {
			return builder.define(t.toString()+STRICT_REQ_SUFFIX, true);
		}).toArray(BooleanValue[]::new);
		
		builder.pop();
		builder.pop();
		
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> REUSE_PENALTY;
	public static ForgeConfigSpec.ConfigValue<Boolean> SUMMATED_MAPS;
	
	private static void buildXpGains(ForgeConfigSpec.Builder builder) {
		builder.comment("All settings related to the gain of experience").push("XP_Gains");
		
		REUSE_PENALTY = builder.comment("how much of the original XP should be awarded when a player breaks a block they placed")
				.defineInRange("Reuse Penalty", 0d, 0d, Double.MAX_VALUE);
		SUMMATED_MAPS = builder.comment("Should xp Gains from perks be added onto by configured xp values")
				.define("Perks Plus Configs", false);
		
		buildEventBasedXPSettings(builder);
		
		builder.pop();
	}
	
	public static ConfigObject<Map<String, Double>> FROM_ENVIRONMENT_XP;
	public static ConfigObject<Map<String, Double>> FROM_IMPACT_XP;
	public static ConfigObject<Map<String, Double>> FROM_MAGIC_XP;
	public static ConfigObject<Map<String, Double>> FROM_PROJECTILE_XP;
	
	public static ConfigObject<Map<String, Double>> RECEIVE_DAMAGE_XP;
	public static ConfigObject<Map<String, Double>> DEAL_MELEE_DAMAGE_XP;
	public static ConfigObject<Map<String, Double>> DEAL_RANGED_DAMAGE_XP;
	
	public static ConfigObject<Map<String, Double>> JUMP_XP;
	public static ConfigObject<Map<String, Double>> SPRINT_JUMP_XP;
	public static ConfigObject<Map<String, Double>> CROUCH_JUMP_XP;
	
	public static ConfigObject<Map<String, Double>> BREATH_CHANGE_XP;
	public static ConfigObject<Map<String, Double>> HEALTH_CHANGE_XP;
	public static ConfigObject<Map<String, Double>> SPRINTING_XP;
	public static ConfigObject<Map<String, Double>> SUBMERGED_XP;
	public static ConfigObject<Map<String, Double>> SWIMMING_XP;
	public static ConfigObject<Map<String, Double>> DIVING_XP;
	public static ConfigObject<Map<String, Double>> SURFACING_XP;
	public static ConfigObject<Map<String, Double>> SWIM_SPRINTING_XP;
	
	private static void buildEventBasedXPSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("Settings related to certain default event XP awards.").push("Event_XP_Specifics");
		
		builder.push("Damage_Received");
			FROM_ENVIRONMENT_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"FROM_ENVIRONMENT Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 10d));
			FROM_IMPACT_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"FROM_IMPACT Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 15d));
			FROM_MAGIC_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"FROM_MAGIC Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("magic", 15d));
			FROM_PROJECTILE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder,
					"FROM_PROJECTILE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 15d));
			RECEIVE_DAMAGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"RECEIVE_DAMAGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 1d));
		builder.pop();
		
		builder.push("Damage_Dealt");
			DEAL_MELEE_DAMAGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"DEAL_MELEE_DAMAGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("combat", 1d));
			DEAL_RANGED_DAMAGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"DEAL_RANGED_DAMAGE SKills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("archery", 1d));
		builder.pop();
		
		builder.push("Jumps");
			JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
			SPRINT_JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SPRINT_JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
			CROUCH_JUMP_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"CROUCH_JUMP Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 2.5));
		builder.pop();
		
		builder.push("Player_Actions");
			BREATH_CHANGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 					
					"BREATH_CHANGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 1d));
			HEALTH_CHANGE_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"HEALTH_CHANGE Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("endurance", 1d));
			SPRINTING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SPRINTING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("agility", 100d));
			SUBMERGED_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SUBMERGED Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 1d));
			SWIMMING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SWIMMING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 100d));
			DIVING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"DIVING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 150d));
			SURFACING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SURFACING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 50d));
			SWIM_SPRINTING_XP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
					"SWIM_SPRINTING Skills and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("swimming", 200d));
		builder.pop();
			
		builder.pop();
	}
	
	public static ForgeConfigSpec.IntValue PARTY_RANGE;
	
	private static void buildPartySettings(ForgeConfigSpec.Builder builder) {
		builder.comment("All setings governing party behavior").push("Party");
			PARTY_RANGE = builder.comment("How close do party members have to be to share experience.")
					.defineInRange("Party Range", 50, 0, Integer.MAX_VALUE);
		builder.pop();
	}
	
	public static ForgeConfigSpec.BooleanValue MOB_SCALING_ENABLED;
	
	public static ForgeConfigSpec.ConfigValue<Boolean> 	MOB_USE_EXPONENTIAL_FORUMULA;
	public static ForgeConfigSpec.ConfigValue<Integer>  MOB_SCALING_AOE;
	public static ForgeConfigSpec.ConfigValue<Integer> 	MOB_SCALING_BASE_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_LINEAR_PER_LEVEL;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_EXPONENTIAL_POWER_BASE;
	public static ForgeConfigSpec.ConfigValue<Double> 	MOB_EXPONENTIAL_LEVEL_MOD;
	
	public static ConfigObject<Map<String, Double>> MOB_SCALE_HP;
	public static ConfigObject<Map<String, Double>> MOB_SCALE_SPEED;
	public static ConfigObject<Map<String, Double>> MOB_SCALE_DAMAGE;
	
	private static void buildMobScalingSettings(ForgeConfigSpec.Builder builder) {
		builder.comment("settings related to how strong mobs get based on player level.").push("Mob_Scaling");
		
		MOB_SCALING_ENABLED = builder.comment("Should mob scaling be turned on.")
				.define("Enable Mob Scaling", true);
		MOB_SCALING_AOE = builder.comment("How far should players be from spawning mobs to affect scaling?")
				.defineInRange("Scaling AOE", 50, 0, Integer.MAX_VALUE);
		MOB_SCALING_BASE_LEVEL = builder.comment("what is the minimum level for scaling to kick in")
				.defineInRange("Base Level", 0, 0, Integer.MAX_VALUE);
		
			builder.comment("How should mob attributes be calculated with respect to the player's level.").push("Formula");
				MOB_USE_EXPONENTIAL_FORUMULA = builder.comment("shold levels be determined using an exponential forumula?")
						.define("Use Exponential Formula", true);
				//========LINEAR SECTION===============
				builder.comment("Settings for Linear scaling configuration").push("LINEAR_LEVELS");				
				MOB_LINEAR_PER_LEVEL = builder.comment("What is the xp increase per level ((level - base_level) * this)")
									.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);		
				builder.pop(); //COMPLETE LINEAR BLOCK
				
				//========EXPONENTIAL SECTION==========
				builder.comment("Settings for Exponential scaling configuration").push("EXPONENTIAL_LEVELS");
				MOB_EXPONENTIAL_POWER_BASE = builder.comment("What is the x in: (x^([Per Level] * level))")
									.defineInRange("Power Base", 1.104088404342588d, 0d, Double.MAX_VALUE);
				MOB_EXPONENTIAL_LEVEL_MOD = builder.comment("What is the x in: ([Power Base]^(x * level))")
									.defineInRange("Per Level", 1d, 0d, Double.MAX_VALUE);
			builder.pop(); //Formula
			builder.comment("These settings control which skills affect scaling and the ratio for each skill"
					, "HP Scale: 1 = half a heart, or 1 hitpoint"
					, "SPD Scale: 0.7 is base for most mobs.  this is added to that. so 0.7 from scaling is double speed"
					, "DMG Scale: is a multiplier of their base damage.  1 = no change, 2 = double damage"
					, "negative values are possible and you can use this to create counterbalance skills").push("Scaling_Settings");
				MOB_SCALE_HP = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
						"HP Scaling and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("combat", 0.01));
				MOB_SCALE_SPEED = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
						"Speed Scaling and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("combat", 0.001));
				MOB_SCALE_DAMAGE = TomlConfigHelper.<Map<String, Double>>defineObject(builder, 
						"Damage Scaling and Ratios", CodecTypes.DOUBLE_CODEC, Collections.singletonMap("combat", 0.001));
			builder.pop(); //Scaling Settings
		builder.pop(); //Mob_Scaling
	}
}

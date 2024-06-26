package harmonised.pmmo.features.mobscaling;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME)
public class MobAttributeHandler {
	private static final ResourceLocation MODIFIER_ID = Reference.rl("mob_scaling_modifier");
	/**Used for balancing purposes to ensure configurations do not exceed known limits.*/
	private static final Map<ResourceLocation, Float> CAPS = Map.of(
			Attributes.MAX_HEALTH.unwrapKey().get().location(), 1024f,
			Attributes.MOVEMENT_SPEED.unwrapKey().get().location(), 1.5f,
			Attributes.ATTACK_DAMAGE.unwrapKey().get().location(), 2048f,
			Attributes.SPAWN_REINFORCEMENTS_CHANCE.unwrapKey().get().location(), 1f
	);

	@SubscribeEvent
	public static void onBossAdd(EntityJoinLevelEvent event) {
		if (!Config.server().mobScaling().enabled())
			return;
		if (event.getEntity().getType().is(Tags.EntityTypes.BOSSES)
				&& event.getEntity() instanceof LivingEntity entity
				&& event.getLevel() instanceof ServerLevel level) {
			handle(entity, level
					, new Vec3(entity.getX(), entity.getY(), entity.getZ())
					, level.getDifficulty().getId());
		}
	}
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onMobSpawn(MobSpawnEvent.PositionCheck event) {
	    if (!Config.server().mobScaling().enabled())
	        return;
		if (event.getEntity().getType().is(Reference.MOB_TAG)) {
			handle(event.getEntity(), event.getLevel().getLevel()
					, new Vec3(event.getX(), event.getY(), event.getZ())
					, event.getLevel().getDifficulty().getId());
		}
	}

	private static void handle(LivingEntity entity, ServerLevel level, Vec3 spawnPos, int diffScale) {
		int range = Config.server().mobScaling().aoe();
		TargetingConditions targetCondition = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().range(Math.pow(range, 2)*3);
		List<Player> nearbyPlayers = level.getNearbyPlayers(targetCondition, entity, AABB.ofSize(spawnPos, range, range, range));
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "NearbyPlayers on Spawn: "+MsLoggy.listToString(nearbyPlayers));

		//get values for biome and dimension scaling
		Core core = Core.get(level.getLevel());
		LocationData dimData = core.getLoader().DIMENSION_LOADER.getData(level.getLevel().dimension().location());
		LocationData bioData = core.getLoader().BIOME_LOADER.getData(RegistryUtil.getId(level.getBiome(entity.getOnPos())));

		Map<String, Double> dimMods = dimData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new HashMap<>());
		Map<String, Double> bioMods = bioData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new HashMap<>());
		Map<ResourceLocation, Map<String, Double>> multipliers = Config.server().mobScaling().ratios();
		final float bossMultiplier = entity.getType().is(Tags.EntityTypes.BOSSES) ? Config.server().mobScaling().bossScaling().floatValue() : 1f;

		Set<ResourceLocation> attributeKeys = Stream.of(dimMods.keySet(), bioMods.keySet())
				.flatMap(Set::stream)
				.map(Reference::of)
				.collect(Collectors.toSet());
		attributeKeys.addAll(multipliers.keySet());

		//Set each Modifier type
		attributeKeys.forEach(attributeID -> {
			Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(attributeID).get();

            Map<String, Double> config = multipliers.getOrDefault(attributeID, new HashMap<>());
			AttributeInstance attributeInstance = entity.getAttribute(attribute);
			if (attributeInstance != null) {
				double base = baseValue(entity, attributeID, attributeInstance);
				float cap = CAPS.getOrDefault(attributeID, Float.MAX_VALUE);
				float bonus = getBonus(nearbyPlayers, config, diffScale, base, cap);
				bonus += dimMods.getOrDefault(attributeID.toString(), 0d).floatValue();
				bonus += bioMods.getOrDefault(attributeID.toString(), 0d).floatValue();
				bonus *= bossMultiplier;
				AttributeModifier modifier = new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE);
				attributeInstance.removeModifier(MODIFIER_ID);
				attributeInstance.addPermanentModifier(modifier);
				MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Entity={} Attribute={} value={}", entity.getDisplayName().getString(), attributeID.toString(), bonus);
			}
		});
		//sets health to max if max HP was modified.  This is a case catch.
		entity.setHealth(entity.getMaxHealth());
	}
	
	/**This function serves as an intermediary for balance
	 * purposes.  It seeks to intercept certain values which
	 * for one reason or another do not provide a balanced
	 * value from the standard methods.
	 * 
	 * @param entity
	 * @param id
	 * @param ai
	 * @return
	 */
	private static double baseValue(LivingEntity entity, ResourceLocation id, AttributeInstance ai) {
		return switch (id.toString()) {
		case "minecraft:generic.attack_damage" -> 1f;
		case "minecraft:generic.movement_speed" -> entity.getSpeed();
		default -> ai.getBaseValue();};
	}
	
	/**Calculates the bonus amount to be applied to this
	 * attribute based on configuration settings from
	 * toml configs.  This precedes settings applied by
	 * biomes or dimensions.
	 * 
	 * @param nearbyPlayers all players in range to affect scaling
	 * @param config the setting for this attribute scaling
	 * @param scale the gamemode difficulty
	 * @param ogValue the original value of this attribute
	 * @param cap the max value for this attribute
	 * @return the value to modify the attribute by
	 */
	private static float getBonus(List<Player> nearbyPlayers, Map<String, Double> config, int scale, double ogValue, float cap) {
		//summate all levels from the configured skills for each nearby player
		Map<String, Long> totalLevel = new HashMap<>();
		//pass through case for dim/biome bonuses to still apply.
		if (nearbyPlayers.isEmpty()) return 0f;
		nearbyPlayers.forEach(player -> {
			config.keySet().stream().collect(Collectors.toMap(str -> str, str -> Core.get(player.level()).getData().getLevel(str, player.getUUID())))
					.forEach((skill, level) -> {
				totalLevel.merge(skill, level, Long::sum);
			});
		});
		//get the average level for each skill and calculate its modifier from the configuration formula
		float outValue = 0f;
		for (Map.Entry<String, Double> configEntry : config.entrySet()) {
			long averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0L)/nearbyPlayers.size();
			if (averageLevel < Config.server().mobScaling().baseLevel()) continue;
			outValue += Config.server().mobScaling().useExponential()
					? Math.pow(Config.server().mobScaling().powerBase(), (Config.server().mobScaling().perLevel() * (averageLevel - Config.server().mobScaling().baseLevel())))
					: (averageLevel - Config.server().mobScaling().baseLevel()) * Config.server().mobScaling().perLevel();
			outValue *= configEntry.getValue();
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}	
}

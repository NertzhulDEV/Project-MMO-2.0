package harmonised.pmmo.features.mobscaling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class MobAttributeHandler {
	private static final float HARD_CAP_HP = 1024f;
	private static final float HARD_CAP_SPD = 1.5f;
	private static final float HARD_CAP_DMG = 2048f;
	
	@SubscribeEvent
	public static void onMobSpawn(SpecialSpawn event) {
		if (event.getEntityLiving().getType().is(Reference.MOB_TAG)) {
			int diffScale = event.getWorld().getDifficulty().getId();
			Vec3 spawnPos = new Vec3(event.getX(), event.getY(), event.getZ());
			int range = Config.MOB_SCALING_AOE.get();
			TargetingConditions targetCondition = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().range(Math.pow(range, 2)*3);
			List<Player> nearbyPlayers = event.getWorld().getNearbyPlayers(targetCondition, event.getEntityLiving(), AABB.ofSize(spawnPos, range, range, range));
			MsLoggy.debug("NearbyPlayers on Spawn: "+MsLoggy.listToString(nearbyPlayers));
			if (nearbyPlayers.isEmpty()) return;
			//Set each Modifier type
			setMobModifier(event.getEntityLiving(), getBonus(nearbyPlayers, Config.MOB_SCALE_HP.get(), diffScale, event.getEntityLiving().getMaxHealth(), HARD_CAP_HP), Attributes.MAX_HEALTH, ModifierID.HP);
			setMobModifier(event.getEntityLiving(), getBonus(nearbyPlayers, Config.MOB_SCALE_SPEED.get(), diffScale, event.getEntityLiving().getSpeed(), HARD_CAP_SPD), Attributes.MOVEMENT_SPEED, ModifierID.SPEED);
			setMobModifier(event.getEntityLiving(), getBonus(nearbyPlayers, Config.MOB_SCALE_DAMAGE.get(), diffScale, 1f, HARD_CAP_DMG), Attributes.ATTACK_DAMAGE, ModifierID.DAMAGE);
			event.getEntityLiving().setHealth(event.getEntityLiving().getMaxHealth());
			if (Config.DEBUG_LOGGING.get()) {
				LivingEntity entity = event.getEntityLiving();
				entity.setCustomName(new TextComponent("SCALED: HP:"+entity.getMaxHealth()+"| SPD:"+entity.getSpeed()));
				entity.setCustomNameVisible(true);
			}
		}
	}
	
	private static float getBonus(List<Player> nearbyPlayers, Map<String, Double> config, int scale, float ogValue, float cap) {
		//summate all levels from the configured skills for each nearby player
		Map<String, Integer> totalLevel = new HashMap<>();
		for (Player player : nearbyPlayers) {
			for (Map.Entry<String, Integer> level : getConfigLevel(player, config).entrySet()) {
				totalLevel.merge(level.getKey(), level.getValue(), (o, n) -> o + n);
			}
		}
		//get the average level for each skill and calculate its modifier from the configuration formula
		float outValue = 0f;
		for (Map.Entry<String, Double> configEntry : config.entrySet()) {
			int averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0)/nearbyPlayers.size();
			if (averageLevel < Config.MOB_SCALING_BASE_LEVEL.get()) return 0f;
			if (Config.MOB_USE_EXPONENTIAL_FORUMULA.get()) {
				outValue += Math.pow(Config.MOB_EXPONENTIAL_POWER_BASE.get(), (Config.MOB_EXPONENTIAL_LEVEL_MOD.get() * (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get())));
			}
			else 
				outValue += (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get()) * Config.MOB_LINEAR_PER_LEVEL.get();
			outValue *= configEntry.getValue();
		}
		MsLoggy.debug("Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}
	
	private static enum ModifierID {
		HP(UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb")),
		SPEED(UUID.fromString("d6103cbc-b90b-4c4b-b3c0-92701fb357b3")),
		DAMAGE(UUID.fromString("992b11f1-7b3f-48d9-8ebd-1acfc3257b17"));
		
		public UUID id;
		ModifierID(UUID id) {this.id = id;}
	}
	
	private static void setMobModifier(LivingEntity mob, float bonus, Attribute attribute, ModifierID modifID) {
		AttributeInstance attributeInstance = mob.getAttribute(attribute);
		MsLoggy.debug(modifID.name()+" isNull:"+(attributeInstance==null));
		if (attributeInstance != null) {
			AttributeModifier modifier = new AttributeModifier(modifID.id, "Boost to Mob Scaling", bonus, AttributeModifier.Operation.ADDITION);
			attributeInstance.removeModifier(modifID.id);
			attributeInstance.addPermanentModifier(modifier);
		}
	}
	
	private static Map<String, Integer> getConfigLevel(Player player, Map<String, Double> config) {
		Map<String, Integer> outMap = new HashMap<>();
		for (String skill : config.keySet()) {
			outMap.put(skill, Core.get(player.level).getData().getPlayerSkillLevel(skill, player.getUUID()));
		}
		return outMap;
	}	
}

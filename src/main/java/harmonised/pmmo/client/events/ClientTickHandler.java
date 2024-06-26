package harmonised.pmmo.client.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.Reference;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME, value= Dist.CLIENT)
public class ClientTickHandler {
	private static int ticksElapsed = 0;
	
	public static void tickGUI() {ticksElapsed++;}	
	public static boolean isRefreshTick() {return ticksElapsed >= 15;}
	public static void resetTicks() {ticksElapsed = 0;}
	
	public static final List<GainEntry> xpGains = new ArrayList<>();
	
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		ticksElapsed++;
		tickDownGainList();
	}
	
	public static void tickDownGainList() {
		xpGains.forEach(GainEntry::downTick);
		xpGains.removeIf(entry -> entry.duration <= 0);
	}
	
	public static void addToGainList(String skill, long amount) {
		SkillData skillData = Config.skills().get(skill);
		if (Config.GAIN_BLACKLIST.get().contains(skill) 
				|| (skillData.isSkillGroup() && skillData.getGroup().containsKey(skill)))
			return;
		if (xpGains.stream().anyMatch(entry -> entry.skill.equals(skill))) {
			GainEntry existingEntry = xpGains.stream().filter(entry -> entry.skill.equals(skill)).findFirst().get();
			xpGains.remove(existingEntry);
			xpGains.add(new GainEntry(skill, existingEntry.value+amount));
		}
		else
			xpGains.add(new GainEntry(skill, amount));
	}
	
	public static class GainEntry {
		public int duration, color;
		private final String skill;
		private final int skillColor;
		private final long value;
		public GainEntry(String skill, long value) {
			this.skill = skill;
			this.duration = MsLoggy.DEBUG.logAndReturn(Config.GAIN_LIST_LINGER_DURATION.get()
								, LOG_CODE.GUI, "Gain Duration Set as: {}");
			this.value = value;
			this.skillColor = CoreUtils.getSkillColor(skill);
		}
		public void downTick() {duration--;}

		public Component display() {
			return Component.literal((value >= 0 ? "+" : "")+value+" ")
					.append(Component.translatable("pmmo."+skill))
					.setStyle(CoreUtils.getSkillStyle(skill));
		}
		public int getColor() {
			this.color = CoreUtils.setTransparency(this.skillColor, (double)duration/(double)Config.GAIN_LIST_LINGER_DURATION.get());
			return this.color;
		}

		@Override
		public String toString() {
			return "Duration:"+duration+"|"+display().toString();
		}
	}
}

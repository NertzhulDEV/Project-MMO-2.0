package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;

public class CraftHandler {
	public static void handle(ItemCraftedEvent event) {
		Core core = Core.get(event.getPlayer().getLevel());
		if (!core.isActionPermitted(ReqType.CRAFT, event.getCrafting(), event.getPlayer())) {
			event.setCanceled(true);
			//TODO notify player of inability to perform
		}
		else if (!event.getPlayer().level.isClientSide){
			CompoundTag eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.CRAFT, event, new CompoundTag());
			if (eventHookOutput.getBoolean(APIUtils.IS_CANCELLED)) 
				event.setCanceled(true);
			else {
				//proecess perks
				CompoundTag perkDataIn = eventHookOutput;
				//if break data is needed by perks, we can add it here.  this is just default implementation.
				CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(EventType.CRAFT, (ServerPlayer) event.getPlayer(), perkDataIn));
				Map<String, Long> xpAward = core.getExperienceAwards(EventType.CRAFT, event.getCrafting(), event.getPlayer(), perkOutput);
				List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
				core.awardXP(partyMembersInRange, xpAward);
			}
		}
	}
}
package me.mafkees92.MVdWPlaceholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import me.mafkees92.Main;
import org.bukkit.entity.Player;

import java.util.UUID;

public class mafkees_island_size {
	
public mafkees_island_size(Main plugin) {
	
		PlaceholderAPI.registerPlaceholder(plugin, "mafkees_island_size", event -> {
			Player player = event.getPlayer();
			if(player != null) {
				if(ASkyBlockAPI.getInstance().inTeam(player.getUniqueId())) {
					UUID leader = ASkyBlockAPI.getInstance().getTeamLeader(player.getUniqueId());
					int size = ASkyBlockAPI.getInstance().getIslandOwnedBy(leader).getProtectionSize();
					return size + "x" + size;
				}
				else {
					Island is = ASkyBlockAPI.getInstance().getIslandOwnedBy(player.getUniqueId());
					if(is != null) {
						return is.getProtectionSize() + "x" + is.getProtectionSize();
					}
				}
			}
			return null;
		});
	}

}

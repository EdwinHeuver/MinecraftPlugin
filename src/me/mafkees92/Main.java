package me.mafkees92;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.wasteofplastic.askyblock.ASkyBlock;

import me.mafkees92.ActionBar.ActionBar;
import me.mafkees92.CustomHoppers.GiveChunkHopper;
import me.mafkees92.CustomHoppers.HopperData;
import me.mafkees92.CustomHoppers.HopperEvents;
import me.mafkees92.CustomPotions.CustomSplashPotions;
import me.mafkees92.CustomPotions.GiveCustomPotion;
import me.mafkees92.CustomVouchers.FlyExpirationListener;
import me.mafkees92.CustomVouchers.GetRemainingFlyTime;
import me.mafkees92.CustomVouchers.GiveVoucher;
import me.mafkees92.CustomVouchers.VoucherUsageEvent;
import me.mafkees92.Files.Messages;
import me.mafkees92.HologramParkour.HologramParkour;
import me.mafkees92.MVdWPlaceholders.mvdwPlaceholders;
import me.mafkees92.RenameItems.RenameItem;
import me.mafkees92.VoidTeleportation.VoidDamage;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	private LuckPerms luckperms;
	private boolean shopGuiPlusApiEnabled = false;
    public static Economy econ = null;
	private HopperData data;

	public void onEnable() {

		// saveDefaultConfig();
		new Messages(this, "Messages.yml");
		data = new HopperData(this, "HopperData.yml");

		getServer().getPluginManager().registerEvents(new VoidDamage(this), this);
		getServer().getPluginManager().registerEvents(new CustomSplashPotions(this), this);
		getServer().getPluginManager().registerEvents(new ActionBar(this), this);
		getServer().getPluginManager().registerEvents(new VoucherUsageEvent(this), this);
		getServer().getPluginManager().registerEvents(new HopperEvents(this), this);
		getCommand("rename").setExecutor(new RenameItem());
		getCommand("givecustompotion").setExecutor(new GiveCustomPotion());
		getCommand("givevoucher").setExecutor(new GiveVoucher());
		getCommand("flytime").setExecutor(new GetRemainingFlyTime(this));
		getCommand("givecustomhopper").setExecutor(new GiveChunkHopper(this));
		new HologramParkour(this);

		if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			new mvdwPlaceholders(this);
		}

		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
			Bukkit.getLogger().info("Hooked into LuckPerms");
			luckperms = provider.getProvider();
			new FlyExpirationListener(this, luckperms);
		}

		if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null) {
			Bukkit.getLogger().info("Hooked into ShopGUI+");
			shopGuiPlusApiEnabled = true;
		}
		
		if(!setupEconomy()) {
			Bukkit.getLogger().warning(ChatColor.DARK_RED + "ERROR: could not load vault plugin");
		}
	}

	private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = ASkyBlock.getPlugin().getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return econ != null;
	}

	public LuckPerms getLuckperms() {
		return luckperms;
	}

	public HopperData getHopperData() {
		return data;
	}
	
	public boolean isShopGuiPlusEnabled() {
		return shopGuiPlusApiEnabled;
	}
}

package me.mafkees92.HologramParkour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.mafkees92.Main;
import me.mafkees92.Files.BaseFile;
import me.mafkees92.Files.Messages;
import me.mafkees92.Utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class Parkour extends BaseFile implements CommandExecutor{


	private final String createParkourPermission = "mafkees.createparkour";
	private final String startParkourPermission = "mafkees.startparkour";
	
	private HashMap<Player, String> recordingPlayers = new HashMap<>();    //recorder, parkourName
	private HashMap<Player, List<Location>> recordingParkours = new HashMap<>();  //recorder, way points
	private HashMap<String, List<Location>> loadedParkours = new HashMap<>(); // parkourName, way points
	public static List<Player> parkourParticipants = new ArrayList<>();
	
	public Parkour(Main plugin, String fileName) {
		super(plugin, fileName);
		// TODO Auto-generated constructor stub
		
		
	}


	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(!(sender instanceof Player))
			return false;
		
		Player player = (Player) sender;
		
		if(args.length > 0) {
			
			switch (args[0].toLowerCase()) {
			
				// /parkour create <parkourname>
			case "create":
				if(player.hasPermission(this.createParkourPermission)) {
					if(args.length == 2 && args[1] != null && !args[1].equals("")) {
						createParkour(player, args[1]);
					}
					else {
						player.sendMessage(Utils.colorize("&cInvalid arguments. Please try /parkour create <parkourname>"));
					}
				}
				else {
					player.sendMessage(Messages.noPermission);
				}
				break;

			case "waypoint":
			case "wp":
				if(player.hasPermission(this.createParkourPermission)) {
					if(this.isRecording(player)) {
						this.createWayPoint(player);
					}
					else {
						player.sendMessage(Utils.colorize("&cYou have to be recording before you can add a waypoint"));
					}
				}
				else {
					player.sendMessage(Messages.noPermission);
				}
				
				break;

			case "undo":
				if(player.hasPermission(this.createParkourPermission)) {
					if(this.isRecording(player)) {
						this.deleteLastWayPoint(player);
					}
					else {
						player.sendMessage(Utils.colorize("&cYou have to be recording before you can remove a waypoint"));
					}
				}
				else {
					player.sendMessage(Messages.noPermission);
				}
				
				break;
				
			case "save":
				if(player.hasPermission(this.createParkourPermission)) {
					if(this.isRecording(player)) {
						this.saveParkour(player);
					}
					else {
						player.sendMessage(Utils.colorize("&cYou have to be recording before you can save your parkour"));
					}
				}
				else {
					player.sendMessage(Messages.noPermission);
				}
				
				break;

			case "delete":
			case "remove":
				if(player.isOp()) {
					String parkourName = args[1];
					if(config.contains(parkourName)) {
						config.set(parkourName, null);
						this.save();
						player.sendMessage("Deleted parkour" + parkourName + " from the save files");
					}
					else {
						player.sendMessage("There doesn't exist a parkour by the name: " + parkourName);
					}
				}
					//remove a parkour with specified name from the config and loaded data
				break;

			case "start":
				if(Parkour.parkourParticipants.contains(player)) {
					player.sendMessage(Utils.colorize("&cYou are already doing a parkour!"));
					return true;
				}
				if(player.hasPermission(this.startParkourPermission)){
					this.startParkour(player, args[1]);
				}
				else {
					player.sendMessage(Messages.noPermission);
				}
				break;
				
			case "list":
				if(player.isOp()) {
					player.sendMessage(Utils.colorize("&aThe current existing parkours are:"));
					Set<String> keys = config.getKeys(false);
					Iterator<String> it = keys.iterator();

					String message = "";
					
					while(it.hasNext()) {
						String key = it.next();
						if(message.length() < 90) {
							if(it.hasNext()) {
								message += key + ", ";
							}
							else {
								message += key;
							}
						}
						else {
							player.sendMessage(Utils.colorize("&6" + message));
							if(it.hasNext()) {
								message += key + ", ";
							}
							else {
								message += key;
							}
						}
					}
					player.sendMessage(Utils.colorize("&6" + message));
				}
				else {
					player.sendMessage(Utils.colorize("&cYou dont have permission to execute this command"));
				}
				break;
			
			default:
				this.sendInformationMessage(player);
				break;
			}
			
			
		}
		else 
			this.sendInformationMessage(player);
		
		
		return true;
	}

	

	private void createParkour(Player player, String parkourName) {
		//send info message about how to create a parkour
		// first /parkour create <name>
		// then /parkour waypoint/wp to record locations
		// then /parkour save to save the created parkour
		player.sendMessage("You have started recording a parkour");
		player.sendMessage("Step 1: [/parkour create <name>]  [Check]");
		player.sendMessage("Step 2: [/parkour waypoint/wp] to save your current location as a waypoint");
		player.sendMessage("Step 3: [/parkour save] to save all waypoints to a parkour");
		
		//add player to list of recording players
		this.recordingPlayers.put(player, parkourName);	
		
		//add parkour to list of recording parkours
		this.recordingParkours.put(player, new ArrayList<>());
	}
	
	private void createWayPoint(Player player) {
		
		Location locToAdd = player.getLocation();
		int x = locToAdd.getBlockX();
		double y = locToAdd.getY() + 1;
		int z = locToAdd.getBlockZ();
		String parkourName = recordingPlayers.get(player);
		
		//add waypoint 
		this.recordingParkours.get(player).add(locToAdd.clone().add(0,1.1,0));
		
		//tell player he added a waypoint
		player.sendMessage(String.format(Utils.colorize("&6Created a waypoint at X:%s Y:%s Z:%s"), x, y , z));
		player.sendMessage(Utils.colorize("&eParkour " + parkourName + " now contains " + recordingParkours.get(player).size() +  " waypoints."));
	}
	
	private void deleteLastWayPoint(Player player) {
		if(this.recordingParkours.get(player).size() == 0) {
			player.sendMessage(Utils.colorize("&cYou dont have any waypoints left to remove."));
			return;
		}
		this.recordingParkours.get(player).remove(this.recordingParkours.get(player).size() -1);
		player.sendMessage(Utils.colorize("&6Removed the last recorded waypoint."));
	}

	private void saveParkour(Player player) {
		//get the list of waypoints;
		String parkourName = recordingPlayers.get(player);
		List<String> wayPoints = new ArrayList<>();
		for(Location loc : this.recordingParkours.get(player)) {
			wayPoints.add(this.locationToString(loc));
		}
		
		config.set(parkourName, wayPoints);
		this.save();
		player.sendMessage(Utils.colorize("&6Succesfully saved the parkour. You are no longer recording."));
		this.recordingPlayers.remove(player);
		this.recordingParkours.remove(player);
	}
	
	
	private void loadParkour(String parkourName) {
		List<String> temp = config.getStringList(parkourName);
		if(temp == null ||temp.size() == 0) {
			Bukkit.getLogger().warning("Failed loading parkour data of parkour: " + parkourName);
			return;
		}
		List<Location> locations = new ArrayList<>();
		for (String wp : temp) {
			String[] split = wp.split("[:]");
			locations.add(new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3])));
		}
		
		this.loadedParkours.put(parkourName, locations);
	}
	
	
	
	
	private void startParkour(Player player, String parkourName) {
		if(!this.loadedParkours.containsKey(parkourName)) {
			this.loadParkour(parkourName);
		}
		//savety check to see if loading the parkour went fine
		if(this.loadedParkours.containsKey(parkourName)) {
			player.sendMessage(Utils.colorize("&eYou have started the parkour. Good Luck!"));
			new HologramParkour(new LinkedList<>(loadedParkours.get(parkourName)), player, plugin);
			
		}
		else {
			player.sendMessage(Utils.colorize("&cFailed loading the parkour, please contact an admin for further help"));
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean isRecording(Player player) {
		return this.recordingPlayers.containsKey(player);
	}
	
	
	
	private void sendInformationMessage(Player player) {

		ComponentBuilder builder = new ComponentBuilder("")
		.append(Utils.colorize("&6&m              &7[&6&lTOOT&e&lMC &7: &eParkour Information&7]&6&m              "))
		.append("\n\n");
		
		if(player.isOp() || player.hasPermission(this.createParkourPermission)) {
			builder.append(Utils.createTextComponentCommand(
					Utils.colorize(" &7- &6List: &eShow all parkours currently created"),
					Utils.colorize("&6&lClick to display all parkour names"), 
					"/parkour list"));
			builder.append("\n");
			builder.event((HoverEvent)null).event((ClickEvent) null);
			
			
			builder.append(Utils.colorize(" &7- &6Create <parkourName>: &eCreate a new Parkour"));
			builder.append("\n");
		}
		
		builder.append(Utils.colorize(" &7- &6Start <parkourName>: &eStart a specific parkour"));
		
		builder.append("\n\n");
		builder.append(Utils.colorize("&6&m                                                                     "));

		BaseComponent[] message = builder.create();
		player.spigot().sendMessage(message);
	}
	
	
	
	
	
	
	
	
	
	private String locationToString(Location loc) {
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
	}
	
	
	
	
}

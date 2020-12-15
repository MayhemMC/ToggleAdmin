package com.jmer05.toggleadmin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	// Get console instance
	private ConsoleCommandSender console = getServer().getConsoleSender();
	private String brand(String msg) {
		return ChatColor.DARK_RED + "[" + ChatColor.RED + "ToggleAdmin" + ChatColor.DARK_RED + "] " + ChatColor.RESET + msg;
	};

	@Override
	public void onEnable() {
		
		// Save default config
		this.saveDefaultConfig();
		
		// Log enabled
		console.sendMessage(brand("Enabled"));

	}
	
	@Override
	public void onDisable() {
		
		// Log disabled
		console.sendMessage(brand("Disabled"));

	}
	
	// On command
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		// Make sure command is `/ta` or `/toggleadmin`
		if(!(cmd.getName().equalsIgnoreCase("ta") || cmd.getName().equalsIgnoreCase("toggleadmin"))) return false;
		
		// Make sure sender is a player
		if(!(sender instanceof Player)) {
			sender.sendMessage(brand(ChatColor.DARK_RED + "Only in-game players can use this command."));
			return true;
		}
		
		// Get player from sender
		Player player = (Player) sender;
		
		// If player dosn't have permissions
		if(!player.hasPermission("toggleadmin.use")) {
			player.sendMessage(brand(ChatColor.DARK_RED + "You do not have permission to use this command."));
			return true;
		}
		
		
		// Get datafile
		File file = new File(this.getDataFolder(), "data/" + player.getUniqueId() + ".dat");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {}

		// Get data from datafile
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

		// Get if file is setup
		if(yaml.getBoolean("is-setup") != true) {
			
			yaml.set("is-setup", true);
			player.sendMessage(brand(ChatColor.DARK_AQUA + "Admin mode set up. Use " + ChatColor.AQUA + "/ta" + ChatColor.DARK_AQUA + " again to toggle admin mode."));
			
		} else {
			
			if(yaml.getBoolean("is-admin") == false) {
				enable(yaml, player);
			} else {
				disable(yaml, player);
			}
			
		}
		
		//Saving the data
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
		
	}

	@SuppressWarnings("unchecked")
	private void disable(YamlConfiguration yaml, Player player) {
		
		ConfigurationSection config = this.getConfig();
		
		// Set admin mode off
		yaml.set("is-admin", false);
		
		// Manage inventory
		if(config.getBoolean(player.getUniqueId() + ".keepInventory", config.getBoolean("global.keepInventory")) == false) {
			yaml.set("admin.inventory", player.getInventory().getContents());
			yaml.set("admin.armor", player.getInventory().getArmorContents());
			if(yaml.isSet("default.armor")) {
				ItemStack[] content = ((List<ItemStack>) yaml.get("default.armor")).toArray(new ItemStack[0]);
		        player.getInventory().setArmorContents(content);
			}
			if(yaml.isSet("default.inventory")) {
				ItemStack[] content = ((List<ItemStack>) yaml.get("default.inventory")).toArray(new ItemStack[0]);
		        player.getInventory().setContents(content);
			}
		}
		
		// Manage location
		if(config.getBoolean(player.getUniqueId() + ".keepLocation", config.getBoolean("global.keepLocation")) == false) {
			yaml.set("admin.location", player.getLocation());
			if(yaml.isSet("default.location")) {
				player.teleport((Location) yaml.get("default.location"));
			}
		}
		
		// Do commands
		List<String> globalCommands = (List<String>) config.get("global.off-commands");
		globalCommands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\{name\\}", player.getName())));
		if(config.contains(player.getUniqueId() + ".off-commands")) {
			List<String> playerCommands = (List<String>) config.get(player.getUniqueId() + ".off-commands");
			playerCommands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\{name\\}", player.getName())));
		}
		
		// Send message
		player.sendMessage(brand(ChatColor.DARK_AQUA + "Admin mode " + ChatColor.RED + "disabled" + ChatColor.DARK_AQUA + ". Use " + ChatColor.AQUA + "/ta" + ChatColor.DARK_AQUA + " to return to admin mode."));
		
	}

	@SuppressWarnings("unchecked")
	private void enable(YamlConfiguration yaml, Player player) {
		
		ConfigurationSection config = this.getConfig();
		
		// Set admin mode on
		yaml.set("is-admin", true);
		
		// Manage inventory
		if(config.getBoolean(player.getUniqueId() + ".keepInventory", config.getBoolean("global.keepInventory")) == false) {
			yaml.set("default.inventory", player.getInventory().getContents());
			yaml.set("default.armor", player.getInventory().getArmorContents());
			if(yaml.isSet("admin.armor")) {
				ItemStack[] content = ((List<ItemStack>) yaml.get("admin.armor")).toArray(new ItemStack[0]);
		        player.getInventory().setArmorContents(content);
			}
			if(yaml.isSet("admin.inventory")) {
				ItemStack[] content = ((List<ItemStack>) yaml.get("admin.inventory")).toArray(new ItemStack[0]);
		        player.getInventory().setContents(content);
			}
		}
		
		// Manage location
		if(config.getBoolean(player.getUniqueId() + ".keepLocation", config.getBoolean("global.keepLocation")) == false) {
			yaml.set("default.location", player.getLocation());
			if(yaml.isSet("admin.location")) {
				player.teleport((Location) yaml.get("admin.location"));
			}
		}
		
		// Do commands
		List<String> globalCommands = (List<String>) config.get("global.on-commands");
		globalCommands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\{name\\}", player.getName())));
		if(config.contains(player.getUniqueId() + ".on-commands")) {
			List<String> playerCommands = (List<String>) config.get(player.getUniqueId() + ".on-commands");
			playerCommands.forEach(cmd -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\{name\\}", player.getName())));
		}
		
		// Send message
		player.sendMessage(brand(ChatColor.DARK_AQUA + "Admin mode " + ChatColor.GREEN + "enabled" + ChatColor.DARK_AQUA + ". Use " + ChatColor.AQUA + "/ta" + ChatColor.DARK_AQUA + " to leave admin mode."));
		
	}
	
}

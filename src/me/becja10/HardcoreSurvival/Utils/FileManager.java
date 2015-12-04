package me.becja10.HardcoreSurvival.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager
{
	private static FileConfiguration config = null;
	private static File players = null;

	/*
	 * Get information about the players stored in players.yml
	 */
	public static FileConfiguration getPlayers() {
		if (config == null)
			reloadPlayers();
		return config;
	}
	
	/*
	 * Reloads the player.yml file
	 */
	public static void reloadPlayers() {
		if (players == null)
			players = new File(HardcoreSurvival.getPlugin().getDataFolder(), "players.yml");
		config = YamlConfiguration.loadConfiguration(players);

		InputStream defConfigStream = HardcoreSurvival.getPlugin().getResource("players.yml");
		if (defConfigStream != null) {
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/*
	 * saves information to player.yml
	 */
	public static void savePlayers() {
		if ((config == null) || (players == null))
			return;
		try {
			getPlayers().save(players);
		} catch (IOException ex) {
			System.out.println("[HardcoreSurvival] Could not save config to " + players);
		}
	}

	/*
	 * Creates the default, empty player.yml file
	 */
	public static void saveDefaultPlayers() {
		if (players == null)
			players = new File(HardcoreSurvival.getPlugin().getDataFolder(), "players.yml");
		if (!players.exists())
			HardcoreSurvival.getPlugin().saveResource("players.yml", false);
	}
}
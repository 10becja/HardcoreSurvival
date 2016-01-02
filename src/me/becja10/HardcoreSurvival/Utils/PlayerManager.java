package me.becja10.HardcoreSurvival.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerManager
{
	private static FileConfiguration config = null;
	private static File players = null;
	private static String path = HardcoreSurvival.instance.getDataFolder().getAbsolutePath() 
			+ File.separator + "players.yml";

	/*
	 * Get information about the players stored in players.yml
	 */
	public static FileConfiguration getPlayers() {
		/*
		 * <uuid>:
		 *   name:
		 *   kills: 
   		 *   deaths:
   		 *   score:
		 *   timePlayed: <in seconds>
		 *   base:
		 *     x:
		 *     y:
		 *     z:
		 *     world:
		 */
		if (config == null)
			reloadPlayers();
		return config;
	}
	
	/*
	 * Reloads the player.yml file
	 */
	public static void reloadPlayers() {
		if (players == null)
			players = new File(path);
		config = YamlConfiguration.loadConfiguration(players);

		InputStream defConfigStream = HardcoreSurvival.instance.getResource("players.yml");
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
			players = new File(path);
		if (!players.exists())
			HardcoreSurvival.instance.saveResource("players.yml", false);
	}
}
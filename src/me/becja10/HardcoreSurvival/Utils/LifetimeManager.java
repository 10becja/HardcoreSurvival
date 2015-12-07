package me.becja10.HardcoreSurvival.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LifetimeManager
{
	private static FileConfiguration config = null;
	private static File stats = null;
	private static String path = HardcoreSurvival.instance.getDataFolder().getAbsolutePath() 
			+ File.separator + "lifetimeStats.yml";


	/*
	 * Get information about the players stored in players.yml
	 */
	public static FileConfiguration getStats() {
		/*
		 * <uuid>: 
		 *   totalScore:
		 *   totalKills:
		 *   totalDeaths:
		 */
		if (config == null)
			reloadStats();
		return config;
	}
	
	/*
	 * Reloads the scores.yml file
	 */
	public static void reloadStats() {
		if (stats == null)
			stats = new File(path);
		config = YamlConfiguration.loadConfiguration(stats);

		InputStream defConfigStream = HardcoreSurvival.instance.getResource("lifetimeStats.yml");
		if (defConfigStream != null) {
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/*
	 * saves information to scores.yml
	 */
	public static void saveStats() {
		if ((config == null) || (stats == null))
			return;
		try {
			getStats().save(stats);
		} catch (IOException ex) {
			System.out.println("[HardcoreSurvival] Could not save config to " + stats);
		}
	}

	/*
	 * Creates the default, empty scores.yml file
	 */
	public static void saveDefaultStats() {
		if (stats == null)
			stats = new File(path);
		if (!stats.exists())
			HardcoreSurvival.instance.saveResource("lifetimeStats.yml", false);
	}
}
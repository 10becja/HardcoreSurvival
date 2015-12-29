package me.becja10.HardcoreSurvival.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ScoresManager
{
	private static FileConfiguration config = null;
	private static File scores = null;
	private static String path = HardcoreSurvival.instance.getDataFolder().getAbsolutePath() 
			+ File.separator + "scores.yml";

	/*
	 * Get information about the scores stored in scores.yml
	 */
	public static FileConfiguration getScores() {
		/*
		 * <uuid>:
		 *   kills:
		 *   	<all mobs they've killed>: <amount>
		 *   other ways to get scores: ...
		 *   
		 */
		if (config == null)
			reloadScores();
		return config;
	}
	
	/*
	 * Reloads the player.yml file
	 */
	public static void reloadScores() {
		if (scores == null)
			scores = new File(path);
		config = YamlConfiguration.loadConfiguration(scores);

		InputStream defConfigStream = HardcoreSurvival.instance.getResource("scores.yml");
		if (defConfigStream != null) {
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/*
	 * saves information to player.yml
	 */
	public static void saveScores() {
		if ((config == null) || (scores == null))
			return;
		try {
			getScores().save(scores);
		} catch (IOException ex) {
			System.out.println("[HardcoreSurvival] Could not save config to " + scores);
		}
	}

	/*
	 * Creates the default, empty player.yml file
	 */
	public static void saveDefaultScores() {
		if (scores == null)
			scores = new File(path);
		if (!scores.exists())
			HardcoreSurvival.instance.saveResource("scores.yml", false);
	}
}
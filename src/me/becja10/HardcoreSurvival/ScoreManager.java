package me.becja10.HardcoreSurvival;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ScoreManager
{
	private static FileConfiguration config = null;
	private static File scores = null;

	/*
	 * Get information about the players stored in players.yml
	 */
	public static FileConfiguration getPlayers() {
		if (config == null)
			reloadScores();
		return config;
	}
	
	/*
	 * Reloads the scores.yml file
	 */
	public static void reloadScores() {
		if (scores == null)
			scores = new File(HardcoreSurvival.getInstance().getDataFolder(), "scores.yml");
		config = YamlConfiguration.loadConfiguration(scores);

		InputStream defConfigStream = HardcoreSurvival.getInstance().getResource("scores.yml");
		if (defConfigStream != null) {
			@SuppressWarnings("deprecation")
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/*
	 * saves information to scores.yml
	 */
	public static void saveScores() {
		if ((config == null) || (scores == null))
			return;
		try {
			getPlayers().save(scores);
		} catch (IOException ex) {
			System.out.println("[HardcoreSurvival] Could not save config to " + scores);
		}
	}

	/*
	 * Creates the default, empty scores.yml file
	 */
	public static void saveDefaultScores() {
		if (scores == null)
			scores = new File(HardcoreSurvival.getInstance().getDataFolder(), "scores.yml");
		if (!scores.exists())
			HardcoreSurvival.getInstance().saveResource("scores.yml", false);
	}
}
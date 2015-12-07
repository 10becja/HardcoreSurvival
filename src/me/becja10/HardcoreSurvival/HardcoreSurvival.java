package me.becja10.HardcoreSurvival;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.EventHandlers.EntityEventHandler;
import me.becja10.HardcoreSurvival.EventHandlers.PlayerEventHandler;
import me.becja10.HardcoreSurvival.Utils.PlayerData;
import me.becja10.HardcoreSurvival.Utils.PlayerManager;
import me.becja10.HardcoreSurvival.Utils.LifetimeManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HardcoreSurvival extends JavaPlugin implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static HardcoreSurvival instance;
	
	//config stuff	
	public String msgColor;						//Death message color
	public String banMsg;						//The message to display when a player is banned
	public String coordsColor;					//customize coords color
	public boolean newPlayerProtection;			//Whether or not to protect newbies
	public int newbieTimer;						//How long a new player is on the newbie list
	public int graceTimer;						//How long a player has after they died
	public boolean announce;					//if there are other servers we should announce to
	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	//Score config Stuff
	public int playerKill1;
	public int playerKill2;
	public int playerKill3;
	public int zombiePlayerKill;
	
	public int blazeMobKill;
	public int creeperMobKill;
	public int endermiteMobKill;
	public int ghastMobKill;
	public int guardianMobKill;
	public int magmacubeMobKill;
	public int skeletonMobKill;
	public int slimeMobKill;
	public int witchMobKill;
	public int witherSkeletonMobKill;
	public int zombieMobKill;
	public int dragonMobKill;
	public int witherMobKill;
	public int bossMobKill;
	
	public int playtime;
	public int recievingDamage;
	public int playerDeath;
	public int zombiePlayerDeath;
	
	private String scoreConfigPath;
	private FileConfiguration scoreConfig;
	private FileConfiguration outScoreConfig;
	
	public static HashMap<UUID, PlayerData> players = new HashMap<UUID, PlayerData>();
	
	public static PlayerData getPlayerData(Player p) {return players.get(p.getUniqueId());}
	
	private void loadConfig()
	{        
        //load config, creating defaults if they don't exist
        msgColor = config.getString("msgColor", "f");
        banMsg = config.getString("banMsg", "You've died! Better luck next time.");
        coordsColor = config.getString("coordsColor", "f");
        newPlayerProtection = config.getBoolean("newPlayerProtection", true);
        newbieTimer = config.getInt("newbieTimer", 30);
        graceTimer = config.getInt("graceTimer", 5);
        announce = config.getBoolean("announce", false);
        
        //write to output file
        outConfig.set("msgColor", msgColor);
        outConfig.set("banMsg", banMsg);
        outConfig.set("coordsColor", coordsColor);
        outConfig.set("newPlayerProtection", newPlayerProtection);
        outConfig.set("newbieTimer", newbieTimer);
        outConfig.set("graceTimer", graceTimer);
        outConfig.set("announce", announce);
		save(outConfig, configPath);
	}
	
	private void loadScoreConfig()
	{
		playerKill1 = scoreConfig.getInt("Killing level 1 player", 1000);
		playerKill2 = scoreConfig.getInt("Killing level 2 player", 1000);
		playerKill3 = scoreConfig.getInt("Killing level 3 player", 1000);
		zombiePlayerKill = scoreConfig.getInt("Killing zombie player", 1000);
		
		blazeMobKill = scoreConfig.getInt("Killing Mobs." + "Killing level 1 player", 1000);
		creeperMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		endermiteMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		ghastMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		guardianMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		magmacubeMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		skeletonMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		slimeMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		witchMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		witherSkeletonMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		zombieMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		dragonMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		witherMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		bossMobKill = scoreConfig.getInt("Killing level 1 player", 1000);
		
		playtime = scoreConfig.getInt("Killing level 1 player", 1000);
		recievingDamage = scoreConfig.getInt("Killing level 1 player", 1000);
		playerDeath = scoreConfig.getInt("Killing level 1 player", 1000);
		zombiePlayerDeath = scoreConfig.getInt("Killing level 1 player", 1000);
	}
	
	@Override
	public void onDisable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Has Been Disabled!");
		save(outConfig, configPath);
		save(outScoreConfig, scoreConfigPath);
		for(PlayerData pd : players.values())
		{
			pd.updateTime();
			pd.savePlayer();
		}
		LifetimeManager.saveStats();
		PlayerManager.savePlayers();
	}
	
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Version "+ pdfFile.getVersion() + " Has Been Enabled!");
	    getServer().getPluginManager().registerEvents(this, this); //register events
		instance = this; 
		
		configPath = instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();
		
		scoreConfigPath = instance.getDataFolder().getAbsolutePath() + File.separator + "scoreConfig.yml";
		scoreConfig = YamlConfiguration.loadConfiguration(new File(scoreConfigPath));
		outScoreConfig = new YamlConfiguration();
		
		//save file
		loadConfig();
		loadScoreConfig();
	    PlayerManager.saveDefaultPlayers();
	    LifetimeManager.saveDefaultStats();
	    
	    PluginManager pluginManager = getServer().getPluginManager();
	    
	    PlayerEventHandler playerEventHandler = new PlayerEventHandler();
	    EntityEventHandler entityEventHandler = new EntityEventHandler();
	    
	    pluginManager.registerEvents(playerEventHandler, this);
	    pluginManager.registerEvents(entityEventHandler, this);
	    
	    
	}
	
	//commands to set your base
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		
		if(cmd.getName().equalsIgnoreCase("reloadplayers"))
		{
			players.clear();
			for(Player p : Bukkit.getOnlinePlayers())
			{
				players.put(p.getUniqueId(), new PlayerData(p));
			}
		}
		
		//hardcorereload
		if(cmd.getName().equalsIgnoreCase("hardcorereload"))
		{
			//if is player, and doesn't have permission
			if((sender instanceof Player) && !(sender.hasPermission("hardcore.reload")))
					sender.sendMessage(ChatColor.DARK_RED+"No permission.");
			else
			{
				PlayerManager.reloadPlayers();
				loadConfig();
			}
		}
		//setbase
		else if(cmd.getName().equalsIgnoreCase("setbase"))
		{
			if (!(sender instanceof Player))
				sender.sendMessage("This command can only be run by a player.");
			else
			{
				Player p = (Player) sender;
				if(!(p.hasPermission("hardcore.setbase")))
					p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use this command!");
				else
				{
					String id = p.getUniqueId().toString();
					//save the players location as their base
					PlayerManager.getPlayers().set(id+".name", p.getName());
					PlayerManager.getPlayers().set(id+".x", p.getLocation().getX());
					PlayerManager.getPlayers().set(id+".y", p.getLocation().getY());
					PlayerManager.getPlayers().set(id+".z", p.getLocation().getZ());
					PlayerManager.savePlayers();
					
					p.sendMessage(ChatColor.GOLD+"Base set!");
				}
			}
		}
		
		return true;
	}
	
	private void save(FileConfiguration config, String path)
	{
        try
        {
            config.save(path);
        }
        catch(IOException exception)
        {
            logger.info("Unable to write to the configuration file at \"" + path + "\"");
        }
	}
}
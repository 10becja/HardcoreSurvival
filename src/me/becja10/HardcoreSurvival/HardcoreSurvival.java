package me.becja10.HardcoreSurvival;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.Commands.RemoveProtectionCommand;
import me.becja10.HardcoreSurvival.Commands.SetBaseCommand;
import me.becja10.HardcoreSurvival.Commands.TargetCommand;
import me.becja10.HardcoreSurvival.EventHandlers.EntityEventHandler;
import me.becja10.HardcoreSurvival.EventHandlers.PlayerEventHandler;
import me.becja10.HardcoreSurvival.Utils.PlayerData;
import me.becja10.HardcoreSurvival.Utils.PlayerManager;
import me.becja10.HardcoreSurvival.Utils.LifetimeManager;
import me.becja10.HardcoreSurvival.Utils.Messages;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HardcoreSurvival extends JavaPlugin implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static HardcoreSurvival instance;
	
	public Entity boss = null;
	
	//config stuff	
	public boolean newPlayerProtection;			//Whether or not to protect newbies
	public int newbieTimer;						//How long a new player is on the newbie list
	public int graceTimer;						//How long a player has after they died
	public boolean announce;					//if there are other servers we should announce to
	public int bossHealth;						
	
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
	
	public static PlayerData getPlayerData(Player p) 
	{
		PlayerData pd = players.get(p.getUniqueId());
		if(pd == null)
			return new PlayerData(p);
		return pd;
	}
	
	private void loadConfig()
	{        
        //load config, creating defaults if they don't exist
        newPlayerProtection = config.getBoolean("newPlayerProtection", true);
        newbieTimer = config.getInt("newbieTimer", 30);
        graceTimer = config.getInt("graceTimer", 5);
        announce = config.getBoolean("announce", false);
        bossHealth = config.getInt("bossHealth", 500);
        
        //write to output file
        outConfig.set("newPlayerProtection", newPlayerProtection);
        outConfig.set("newbieTimer", newbieTimer);
        outConfig.set("graceTimer", graceTimer);
        outConfig.set("announce", announce);
        outConfig.set("bossHealth", bossHealth);
		save(outConfig, configPath);
	}
	
	private void loadScoreConfig()
	{
		playerKill1 = scoreConfig.getInt("Killing level 1 player", 1000);
		playerKill2 = scoreConfig.getInt("Killing level 2 player", 1000);
		playerKill3 = scoreConfig.getInt("Killing level 3 player", 1000);
		zombiePlayerKill = scoreConfig.getInt("Killing zombie player", 1000);
		
		blazeMobKill = scoreConfig.getInt("Killing Mobs.blaze", 1000);
		creeperMobKill = scoreConfig.getInt("Killing Mobs.creeper", 1000);
		endermiteMobKill = scoreConfig.getInt("Killing Mobs.endermite", 1000);
		ghastMobKill = scoreConfig.getInt("Killing Mobs.ghast", 1000);
		guardianMobKill = scoreConfig.getInt("Killing Mobs.guardian", 1000);
		magmacubeMobKill = scoreConfig.getInt("Killing Mobs.magmacube", 1000);
		skeletonMobKill = scoreConfig.getInt("Killing Mobs.skeleton", 1000);
		slimeMobKill = scoreConfig.getInt("Killing Mobs.slime", 1000);
		witchMobKill = scoreConfig.getInt("Killing Mobs.witch", 1000);
		witherSkeletonMobKill = scoreConfig.getInt("Killing Mobs.witherSkeleton", 1000);
		zombieMobKill = scoreConfig.getInt("Killing Mobs.zombie", 1000);
		dragonMobKill = scoreConfig.getInt("Killing Mobs.dragon", 1000);
		witherMobKill = scoreConfig.getInt("Killing Mobs.wither", 1000);
		bossMobKill = scoreConfig.getInt("Killing Mobs.boss", 1000);
		
		playtime = scoreConfig.getInt("Points per second played", 1000);
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
					sender.sendMessage(ChatColor.DARK_RED + Messages.no_permission.getMsg());
			else
			{
				PlayerManager.reloadPlayers();
				loadConfig();
			}
		}
		//setbase
		else if(cmd.getName().equalsIgnoreCase("setbase"))
		{
			return SetBaseCommand.HandleCommand(sender);
		}
		
		else if(cmd.getName().equalsIgnoreCase("target"))
		{
			return TargetCommand.HandleCommand(sender, args);
		}
		else if(cmd.getName().equalsIgnoreCase("removeprotection"))
		{
			return RemoveProtectionCommand.HandleCommand(sender);
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
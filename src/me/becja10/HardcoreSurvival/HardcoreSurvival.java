package me.becja10.HardcoreSurvival;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.EventHandlers.PlayerEventHandler;
import me.becja10.HardcoreSurvival.Utils.FileManager;
import me.becja10.HardcoreSurvival.Utils.ScoreManager;

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
	private static HardcoreSurvival plugin;
	

	
	//config stuff	
	public String msgColor;						//Death message color
	public String banMsg;						//The message to display when a player is banned
	public String coordsColor;					//customize coords color
	public boolean newPlayerProtection;			//Whether or not to protect newbies
	public int newbieTimer;						//How long a new player is on the newbie list
	public boolean announce;					//if there are other servers we should announce to
	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	public static JavaPlugin getPlugin() {return plugin;}
	public static HardcoreSurvival instance;
		
	private void loadConfig()
	{        
        //load config, creating defaults if they don't exist
        msgColor = config.getString("msgColor", "f");
        banMsg = config.getString("banMsg", "You've died! Better luck next time.");
        coordsColor = config.getString("coordsColor", "f");
        newPlayerProtection = config.getBoolean("newPlayerProtection", true);
        newbieTimer = config.getInt("newbieTimer", 30);
        announce = config.getBoolean("announce", false);
        
        //write to output file
        outConfig.set("msgColor", msgColor);
        outConfig.set("banMsg", banMsg);
        outConfig.set("coordsColor", coordsColor);
        outConfig.set("newPlayerProtection", newPlayerProtection);
        outConfig.set("newbieTimer", newbieTimer);
        outConfig.set("announce", announce);
        save();
	}

	@Override
	public void onDisable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Has Been Disabled!");
		save();
		ScoreManager.saveScores();
		FileManager.savePlayers();
	}
	
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Version "+ pdfFile.getVersion() + " Has Been Enabled!");
	    getServer().getPluginManager().registerEvents(this, this); //register events
		plugin = this; 
		
		configPath = plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();
		
		//save file
		loadConfig();
	    FileManager.saveDefaultPlayers();
	    ScoreManager.saveDefaultScores();
	    
	    PluginManager pluginManager = getServer().getPluginManager();
	    
	    PlayerEventHandler playerEventHandler = new PlayerEventHandler();
	    pluginManager.registerEvents(playerEventHandler, this);
	    
	    
	}
	
	//commands to set your base
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		//hardcorereload
		if(cmd.getName().equalsIgnoreCase("hardcorereload"))
		{
			//if is player, and doesn't have permission
			if((sender instanceof Player) && !(sender.hasPermission("hardcore.reload")))
					sender.sendMessage(ChatColor.DARK_RED+"No permission.");
			else
			{
				FileManager.reloadPlayers();
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
					FileManager.getPlayers().set(id+".name", p.getName());
					FileManager.getPlayers().set(id+".x", p.getLocation().getX());
					FileManager.getPlayers().set(id+".y", p.getLocation().getY());
					FileManager.getPlayers().set(id+".z", p.getLocation().getZ());
					FileManager.savePlayers();
					
					p.sendMessage(ChatColor.GOLD+"Base set!");
				}
			}
		}
		
		return true;
	}
	
	private void save()
	{
        try
        {
            outConfig.save(configPath);
        }
        catch(IOException exception)
        {
            logger.info("Unable to write to the configuration file at \"" + configPath + "\"");
        }
	}
}
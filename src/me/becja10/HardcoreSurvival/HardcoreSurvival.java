package me.becja10.HardcoreSurvival;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.Commands.LostCommand;
import me.becja10.HardcoreSurvival.Commands.InfoCommand;
import me.becja10.HardcoreSurvival.Commands.RemoveProtectionCommand;
import me.becja10.HardcoreSurvival.Commands.SetBaseCommand;
import me.becja10.HardcoreSurvival.Commands.TargetCommand;
import me.becja10.HardcoreSurvival.EventHandlers.EntityEventHandler;
import me.becja10.HardcoreSurvival.EventHandlers.PlayerEventHandler;
import me.becja10.HardcoreSurvival.Utils.PlayerData;
import me.becja10.HardcoreSurvival.Utils.PlayerManager;
import me.becja10.HardcoreSurvival.Utils.ScoresManager;
import me.becja10.HardcoreSurvival.Utils.LifetimeManager;
import me.becja10.HardcoreSurvival.Utils.Messages;

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
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

public class HardcoreSurvival extends JavaPlugin implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static HardcoreSurvival instance;
	
//	public Entity boss = null;
	
	//config stuff	
	public boolean newPlayerProtection;			//Whether or not to protect newbies
	public int newbieTimer;						//How long a new player is on the newbie list
	public int graceTimer;						//How long a player has after they died
	public boolean announce;					//if there are other servers we should announce to
	public int bossHealth;
	public boolean scaleScores;
	public int startingScore;
	
	private String configPath;
	private FileConfiguration config;
	private FileConfiguration outConfig;
	
	//Score config Stuff
	public int playerKill1;
	public int playerKill2;
	public int playerKill3;
	public int zombiePlayerKill;
	public int revengeKill;
	
	public int blazeMobKill;
	public int creeperMobKill;
	public int elderGuardianMobKill;
	public int endermanMobKill;
	public int endermiteMobKill;
	public int ghastMobKill;
	public int guardianMobKill;
	public int magmacubeMobKill;
	public int skeletonMobKill;
	public int slimeMobKill;
	public int spiderMobKill;
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
	public static Scoreboard scoreboard;
	public static final String zombieTeam = "hcs_zombies";
	public static final String playerTeam = "hcs_players";
	
	public static PlayerData getPlayerData(Player p) 
	{
		PlayerData pd = players.get(p.getUniqueId());
		if(pd == null)
			return new PlayerData(p);
		return pd;
	}
	
	private void loadConfig()
	{        
		configPath = instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
		config = YamlConfiguration.loadConfiguration(new File(configPath));
		outConfig = new YamlConfiguration();		
		
        //load config, creating defaults if they don't exist
        newPlayerProtection = config.getBoolean("newPlayerProtection", true);
        newbieTimer = config.getInt("newbieTimer", 30);
        graceTimer = config.getInt("graceTimer", 5);
        announce = config.getBoolean("announce", false);
        bossHealth = config.getInt("bossHealth", 500);
        scaleScores = config.getBoolean("scaleScores", true);
        startingScore = config.getInt("startingScore", 1000);
        
        //write to output file
        outConfig.set("newPlayerProtection", newPlayerProtection);
        outConfig.set("newbieTimer", newbieTimer);
        outConfig.set("graceTimer", graceTimer);
        outConfig.set("announce", announce);
        outConfig.set("bossHealth", bossHealth);
        outConfig.set("scaleScores", scaleScores);
        outConfig.set("startingScore", startingScore);
		save(outConfig, configPath);
	}
	
	private void loadScoreConfig()
	{
		scoreConfigPath = instance.getDataFolder().getAbsolutePath() + File.separator + "scoreConfig.yml";
		scoreConfig = YamlConfiguration.loadConfiguration(new File(scoreConfigPath));
		outScoreConfig = new YamlConfiguration();
		
		playerKill1 = scoreConfig.getInt("Killing level 1 player", 1000);
		playerKill2 = scoreConfig.getInt("Killing level 2 player", 750);
		playerKill3 = scoreConfig.getInt("Killing level 3 player", 500);
		zombiePlayerKill = scoreConfig.getInt("Killing zombie player", 50);
		revengeKill = scoreConfig.getInt("Revenge kill bonus", 500);
		
		blazeMobKill = scoreConfig.getInt("Killing Mobs.blaze", 200);
		creeperMobKill = scoreConfig.getInt("Killing Mobs.creeper", 250);
		elderGuardianMobKill = scoreConfig.getInt("Killing Mobs.elderGuardin", 1000);
		endermanMobKill = scoreConfig.getInt("Killing Mobs.enderman", 300);
		endermiteMobKill = scoreConfig.getInt("Killing Mobs.endermite", 50);
		ghastMobKill = scoreConfig.getInt("Killing Mobs.ghast", 150);
		guardianMobKill = scoreConfig.getInt("Killing Mobs.guardian", 0);
		magmacubeMobKill = scoreConfig.getInt("Killing Mobs.magmacube", 50);
		skeletonMobKill = scoreConfig.getInt("Killing Mobs.skeleton", 125);
		slimeMobKill = scoreConfig.getInt("Killing Mobs.slime", 25);
		spiderMobKill = scoreConfig.getInt("Killing Mobs.spider", 100);
		witchMobKill = scoreConfig.getInt("Killing Mobs.witch", 200);
		witherSkeletonMobKill = scoreConfig.getInt("Killing Mobs.witherSkeleton", 500);
		zombieMobKill = scoreConfig.getInt("Killing Mobs.zombie", 100);
		dragonMobKill = scoreConfig.getInt("Killing Mobs.dragon", 10000);
		witherMobKill = scoreConfig.getInt("Killing Mobs.wither", 5000);
		bossMobKill = scoreConfig.getInt("Killing Mobs.boss", 1000);
		
		playtime = scoreConfig.getInt("Points per minute played", 5);
		recievingDamage = scoreConfig.getInt("Points lost per damage taken", 25);
		playerDeath = scoreConfig.getInt("Points lost for dying as a player", 1000);
		zombiePlayerDeath = scoreConfig.getInt("Points lost for dying as a zombie player", 0);
		
		outScoreConfig.set("Killing level 1 player", playerKill1);
		outScoreConfig.set("Killing level 2 player", playerKill2);
		outScoreConfig.set("Killing level 3 player", playerKill3);
		outScoreConfig.set("Killing zombie player", zombiePlayerKill);
		outScoreConfig.set("Revenge kill bonus", revengeKill);
		
		outScoreConfig.set("Killing Mobs.blaze", blazeMobKill);
		outScoreConfig.set("Killing Mobs.creeper", creeperMobKill);
		outScoreConfig.set("Killing Mobs.elderGuardin", elderGuardianMobKill);
		outScoreConfig.set("Killing Mobs.enderman", endermanMobKill);
		outScoreConfig.set("Killing Mobs.endermite", endermiteMobKill);
		outScoreConfig.set("Killing Mobs.ghast", ghastMobKill);
		outScoreConfig.set("Killing Mobs.guardian", guardianMobKill);
		outScoreConfig.set("Killing Mobs.magmacube", magmacubeMobKill);
		outScoreConfig.set("Killing Mobs.skeleton", skeletonMobKill);
		outScoreConfig.set("Killing Mobs.slime", slimeMobKill);
		outScoreConfig.set("Killing Mobs.spider", spiderMobKill);
		outScoreConfig.set("Killing Mobs.witch", witchMobKill);
		outScoreConfig.set("Killing Mobs.witherSkeleton", witherSkeletonMobKill);
		outScoreConfig.set("Killing Mobs.zombie", zombieMobKill);
		outScoreConfig.set("Killing Mobs.dragon", dragonMobKill);
		outScoreConfig.set("Killing Mobs.wither", witherMobKill);
		outScoreConfig.set("Killing Mobs.boss", bossMobKill);
		
		outScoreConfig.set("Points per minute played", playtime);
		outScoreConfig.set("Points lost per damage taken", recievingDamage);
		outScoreConfig.set("Points lost for dying as a player", playerDeath);
		outScoreConfig.set("Points lost for dying as a zombie player", zombiePlayerDeath);
		 
		 save(outScoreConfig, scoreConfigPath);
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
		}
		LifetimeManager.saveStats();
		PlayerManager.savePlayers();
		ScoresManager.saveScores();
	}
	
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " Version "+ pdfFile.getVersion() + " Has Been Enabled!");
	    getServer().getPluginManager().registerEvents(this, this); //register events
		instance = this; 
		scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		if(scoreboard.getTeam(zombieTeam) == null)
		{
			scoreboard.registerNewTeam(zombieTeam).setPrefix(ChatColor.DARK_GREEN + "");
			scoreboard.getTeam(zombieTeam).setAllowFriendlyFire(false);
			scoreboard.getTeam(zombieTeam).setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
		}
		if(scoreboard.getTeam(playerTeam) == null)
		{
			scoreboard.registerNewTeam(playerTeam).setAllowFriendlyFire(true);
		}
		
		//save file
		loadConfig();
		loadScoreConfig();
	    PlayerManager.saveDefaultPlayers();
	    ScoresManager.saveDefaultScores();
	    LifetimeManager.saveDefaultStats();
	    
	    PluginManager pluginManager = getServer().getPluginManager();
	    
	    PlayerEventHandler playerEventHandler = new PlayerEventHandler();
	    EntityEventHandler entityEventHandler = new EntityEventHandler();
	    
	    pluginManager.registerEvents(playerEventHandler, this);
	    pluginManager.registerEvents(entityEventHandler, this);
	    
	    
	}
	
//	public void ChangeToBoss(LivingEntity mob)
//	{
//		Location l = mob.getLocation();
//		ArmorStand name = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
//		name.setCustomName("Bossy Mob");
//		name.setCustomNameVisible(true);
//		name.setVisible(false);
//		name.setNoDamageTicks(Integer.MAX_VALUE);
//		name.setRemoveWhenFarAway(false);
//		
//		mob.setPassenger(name);
//		
//		mob.setMaxHealth(HardcoreSurvival.instance.bossHealth);
//		mob.setCustomName("Bossy Mob");
//		mob.setCustomNameVisible(false);
//		mob.setHealth(HardcoreSurvival.instance.bossHealth);
//		mob.setRemoveWhenFarAway(false);
//		mob.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
//		mob.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_CHESTPLATE));
//		mob.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
//		mob.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
//		
//		boss = mob;
//	}
	
	//commands to set your base
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{	
		if(cmd.getName().equalsIgnoreCase("hardcorereload"))
		{
			//if is player, and doesn't have permission
			if((sender instanceof Player) && !(sender.hasPermission("hardcore.reload")))
					sender.sendMessage(ChatColor.DARK_RED + Messages.no_permission.getMsg());
			else
			{
				PlayerManager.reloadPlayers();
				LifetimeManager.reloadStats();
				ScoresManager.reloadScores();
				loadConfig();
				loadScoreConfig();
				
				for(PlayerData pd : players.values())
					pd.savePlayer();
				
				players.clear();
				for(Player p : Bukkit.getOnlinePlayers())
					players.put(p.getUniqueId(), new PlayerData(p));
			}
		}
		
		else if(cmd.getName().equalsIgnoreCase("curezombie"))
		{
			if(args.length != 1) return false;
			if((sender instanceof Player) && !(sender.hasPermission("hardcore.curezombie")))
				sender.sendMessage(ChatColor.DARK_RED + Messages.no_permission.getMsg());
			else
			{
				Player p = Bukkit.getPlayer(args[0]);
				if(p == null) sender.sendMessage(Messages.player_not_found.getMsg());
				else
				{
					PlayerData pd = getPlayerData(p);
					pd.deaths = 0;
					pd.savePlayer();					
				}
			}
		}
		
		else if(cmd.getName().equalsIgnoreCase("setbase"))
			return SetBaseCommand.HandleCommand(sender);
		
		else if(cmd.getName().equalsIgnoreCase("target"))
			return TargetCommand.HandleCommand(sender, args);
		
		else if(cmd.getName().equalsIgnoreCase("removeprotection"))
			return RemoveProtectionCommand.HandleCommand(sender);
		
		else if(cmd.getName().equalsIgnoreCase("spawnboss"))
			return true;//SpawnBossCommand.HandleCommand(sender, args);
		
		else if(cmd.getName().equalsIgnoreCase("lost"))
			return LostCommand.HandleCommand(sender);
		
		else if(cmd.getName().equalsIgnoreCase("top"))
			return InfoCommand.Top(sender);
		
		else if(cmd.getName().equalsIgnoreCase("rank"))
			return InfoCommand.Rank(sender, args);
		
		else if(cmd.getName().equalsIgnoreCase("stats"))
			return InfoCommand.Stats(sender, args);
		
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
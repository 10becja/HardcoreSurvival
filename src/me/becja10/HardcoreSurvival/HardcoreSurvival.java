package me.becja10.HardcoreSurvival;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.FileManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class HardcoreSurvival extends JavaPlugin implements Listener, PluginMessageListener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	private static HardcoreSurvival plugin;
	
	private static double FAR = 5000;
	private static String prefix = "\u00A7";
	private static List<String> newbies = new ArrayList<String>();
	
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
	
	public static JavaPlugin getInstance() {return plugin;}
		
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
	    
	    
	    getServer().getMessenger().registerOutgoingPluginChannel(this,
	    		"BungeeCord");
	    getServer().getMessenger().registerIncomingPluginChannel(this, 
	    		"BungeeCord", this);
	}
	
	public void sendMessage(Player p)
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		System.out.println("blah");
		try{
			out.writeUTF("Forward");
			out.writeUTF("ALL");
			out.writeUTF("HardcoreSurvival");
			String msg = "yo yo yo";
			out.writeUTF(msg);
			out.writeShort(msg.length());
			
		}catch(Exception ex)
		{
			//do nothing
		}
		
		p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// TODO Auto-generated method stub
		try{
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			String subchannel = in.readUTF();
			System.out.println(subchannel);
			short len = in.readShort();
			byte[] data = new byte[len];
			in.readFully(data);
			
			String s = new String(data);
			
			System.out.println(s);
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}	
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		//for GP, erase all claims so people can loot them
		Player p = event.getEntity();
		p.performCommand("abandonallclaims");
		//announce to all servers with the listener plugin.
		if(announce)
		{
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
							"sync console all hardcoresurvivalannounce " + event.getDeathMessage());
		}
		//let everyone know where the person died
		event.setDeathMessage(prefix+msgColor + event.getDeathMessage()+" at"+prefix+coordsColor +
														 " x: " + p.getLocation().getBlockX() + 
														 " y: " + p.getLocation().getBlockY() +
														 " z: " + p.getLocation().getBlockZ());
		
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		//in case someone needs to rejoin.
		if(event.getPlayer().hasPermission("hardcore.noban")) return;
		//ban the player when they respawn.
		plugin.getServer().dispatchCommand(getServer().getConsoleSender(), "ban "+event.getPlayer().getName()+" "+banMsg);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event)
	{
		//remove newbie protection on log out
		//also safeguards incase runnable screws up
		if(newbies.contains(event.getPlayer().getName()))
				newbies.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if(newPlayerProtection)
		{
			String id = event.getPlayer().getUniqueId().toString();
			//if they are a new player, give them protection
			if(!FileManager.getPlayers().contains(id))
			{
				FileManager.getPlayers().set(id+".name", event.getPlayer().getName());
				FileManager.savePlayers();
				final String name = event.getPlayer().getName();
				newbies.add(name);
				//schedule task to remove them from newbie list after so long
				Bukkit.getScheduler().scheduleSyncDelayedTask(this,	new Runnable()
				{
					public void run()
					{
						newbies.remove(name); //remove them from the newbie list
					}
				}, 20L * newbieTimer * 60); //number of minutes before they are removed.
			}	
		}
	}
	
	//Tell players how to use the compass
	@EventHandler
	public void changeHand(PlayerItemHeldEvent event)
	{
		final Player player = event.getPlayer();
		
		//See if they are switching to compass
		ItemStack newItemStack = player.getInventory().getItem(event.getNewSlot());
		if(newItemStack != null && newItemStack.getType() == Material.COMPASS)
		{			
			//Tell player how to use compass, but only if they hold it for a little while
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					//if they aren't online anymore
					if(!player.isOnline()) return;
					//if they aren't holding the compass anymore
					if(player.getItemInHand().getType() != Material.COMPASS) return;
					player.sendMessage(ChatColor.RED+"Right click to hunt the nearest player.");
					player.sendMessage(ChatColor.GREEN+"Left click to find your way to your base.");
				}
			}
			, 20L); //wait ~1 seconds before telling them about the compass				
		}
	}
	
	//point compass towards nearest player
	@EventHandler
	public void onClick(PlayerInteractEvent event)
	{
		Player p = event.getPlayer();
		if(p.getItemInHand().getType() == Material.COMPASS)
		{
			
			if((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR) && 
				(p.getWorld().getEnvironment().equals(Environment.NETHER) || p.getWorld().getEnvironment().equals(Environment.THE_END)))
			{
				p.sendMessage(ChatColor.GOLD+"Compasses don't work here!");
				return;
			}
			
			if(event.getAction() == Action.RIGHT_CLICK_AIR)
			{
				if(newbies.contains(p.getName()))
				{
					p.sendMessage(ChatColor.GOLD+"Your compass doesn't work yet.");
					return;
				}
				Location here = p.getLocation();
				Location closest = null; //for error checking
				double farthest = FAR;
				//loop through all online players
				for(Player t : Bukkit.getOnlinePlayers())
				{
					//allows for some people to not be tracked, don't track self, and hide newbies
					if(t.hasPermission("hardcore.notrack") || t.equals(p) || newbies.contains(t.getName())) continue;
					
					//don't try and track across worlds
					if(p.getWorld() != t.getWorld()) continue;
					
					Location there = t.getLocation(); //the location of the player we are checking
					double curDist = here.distance(there);
					if(curDist < farthest) //if the current player is closest than the farthest so far
					{
						farthest = curDist;
						closest = there; //set the closest location so far to the current player
					}
				}
				if(closest == null)
					p.sendMessage(ChatColor.DARK_RED+"Couldn't find a player to track");
				else
				{
					//now that we have the closest player, point compass to them
					p.setCompassTarget(closest);
					p.sendMessage(ChatColor.RED+"Tracking last known location of closest player");
				}
			}
			
			//else point them home
			else if(event.getAction() == Action.LEFT_CLICK_AIR)
			{	
				String id = p.getUniqueId().toString();
				//if the player has stored their home
				if(FileManager.getPlayers().contains(id+".x"))
				{
					Location home = new Location(p.getWorld(),
												FileManager.getPlayers().getDouble(id+".x"),
												FileManager.getPlayers().getDouble(id+".y"),
												FileManager.getPlayers().getDouble(id+".z"));
					p.setCompassTarget(home);
					p.sendMessage(ChatColor.GREEN+"Pointing towards home");
				}
				//else they haven't stored their home yet, point towards world spawn
				else
				{
					//sets their compass to either their last slept in bed (will be set with RandomTP) or spawn if not
					Location loc = (p.getBedSpawnLocation() == null) ? p.getWorld().getSpawnLocation() : p.getBedSpawnLocation();
					p.setCompassTarget(loc);
					p.sendMessage(ChatColor.GOLD+"You haven't set your base coordinates, pointing you to spawn. Set base coordinates with /setbase");
				}
			}
		}
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
		
		else if(cmd.getName().equalsIgnoreCase("testsend")){
			sendMessage((Player) sender);
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
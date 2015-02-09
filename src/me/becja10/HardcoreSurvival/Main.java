package me.becja10.HardcoreSurvival;

import java.util.logging.Logger;

import me.becja10.HardcoreSurvival.FileManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	private static Main plugin;
	private static double FAR = 5000;
	private static String banMsg = "You've died! Better luck next time.";
	
	public static JavaPlugin getInstance() {return plugin;}

	@Override
	public void onDisable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
	}
	
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version "+ pdfFile.getVersion() + " Has Been Enabled!");
	    getServer().getPluginManager().registerEvents(this, this); //register events
		plugin = this; 
		//save players.yml file
	    FileManager.saveDefaultPlayers();

	}
	
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		//for GP, erase all claims so people can loot them
		Player p = event.getEntity();
		p.performCommand("abandonallclaims");
		//let everyone know where the person died
		event.setDeathMessage(ChatColor.BLUE+event.getDeathMessage()+ChatColor.BLUE+" at x: " + p.getLocation().getBlockX() + 
														 " y: " + p.getLocation().getBlockY() +
														 " z: " + p.getLocation().getBlockZ());
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		//in case someone needs to rejoin.
		if(event.getPlayer().hasPermission("hardcore.noban")) return;
		//ban the player when they respawn.
		getServer().dispatchCommand(getServer().getConsoleSender(), "ban "+event.getPlayer().getName()+" "+banMsg);
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
				Location here = p.getLocation();
				Location closest = null; //for error checking
				double farthest = FAR;
				//loop through all online players
				for(Player t : Bukkit.getOnlinePlayers())
				{
					//allows for some people to not be tracked, and don't track self
					if(t.hasPermission("hardcore.notrack") || t.equals(p)) continue;
					
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
				if(FileManager.getPlayers().contains(id))
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
					p.setCompassTarget(p.getWorld().getSpawnLocation());
					p.sendMessage(ChatColor.GOLD+"You haven't set your base coordinates, pointing you to spawn. Set base coordinates with /setbase");
				}
			}
		}
	}
	
	//commands to set your base
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		//setbase
		if(cmd.getName().equalsIgnoreCase("setbase"))
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
			return true;		
		}
		return false;
	}
}
package me.becja10.HardcoreSurvival.EventHandlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
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
import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.FileManager;

public class PlayerEventHandler implements Listener{
	
	private static double FAR = 5000;
	private static List<String> newbies = new ArrayList<String>();
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{

	}
	
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if(HardcoreSurvival.instance.newPlayerProtection)
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
				Bukkit.getScheduler().scheduleSyncDelayedTask(HardcoreSurvival.getPlugin(), new Runnable()
				{
					public void run()
					{
						newbies.remove(name); //remove them from the newbie list
					}
				}, 20L * HardcoreSurvival.instance.newbieTimer * 60); //number of minutes before they are removed.
			}	
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event)
	{
		//remove newbie protection on log out
		//also safeguards incase runnable screws up
		if(newbies.contains(event.getPlayer().getName()))
				newbies.remove(event.getPlayer().getName());
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
			Bukkit.getScheduler().scheduleSyncDelayedTask(HardcoreSurvival.getPlugin(), new Runnable()
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

}

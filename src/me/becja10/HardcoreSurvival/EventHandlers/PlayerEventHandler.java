package me.becja10.HardcoreSurvival.EventHandlers;

import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;

public class PlayerEventHandler implements Listener{

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player p = event.getEntity();
		Player killer = p.getKiller();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		PlayerData kd = (killer == null) ? null : HardcoreSurvival.getPlayerData(killer);
		
		pd.deaths++;
		if(pd.deaths > 2)
			pd.isZombie = true;
		pd.lastDeath = System.currentTimeMillis();
		pd.lastKiller = killer;
		
		//TODO do score stuff

		if(kd != null)
		{
			//TODO make revenge time configurable
			if(kd.lastKiller != null && kd.lastKiller.equals(p) && System.currentTimeMillis() - kd.lastDeath <= 1000*60 * HardcoreSurvival.instance.graceTimer)
			{
				//TODO bonus score for revenge killing
				killer.sendMessage(ChatColor.DARK_RED + Messages.revenge_kill.getMsg());
			}
			kd.kills++;
			kd.savePlayer();
		}		
		pd.savePlayer();

	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		final Player p = event.getPlayer();
		final PlayerData pd = HardcoreSurvival.getPlayerData(p);
		
		switch(pd.deaths)
		{
		case 0:
			pd.maxHealth = 20;
			break;
		case 1:
			pd.maxHealth = 16;
			break;
		case 2:
			pd.maxHealth = 10;
			break;
		default:
			pd.maxHealth = 2;
			break;
		}
		
		final boolean z = pd.isZombie;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(HardcoreSurvival.instance, new Runnable(){

			@Override
			public void run() {
				if(z)
					p.setFoodLevel(2);
			}
		}, 10L);
		
		p.setMaxHealth(pd.maxHealth);
		
		if(pd.base.getWorld() != null && !pd.isZombie)
		{
			event.setRespawnLocation(pd.base);
		}
	}
		
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		
		//Play a zombie noise randomly when zombie players walk around
		if(pd.isZombie)
		{
			int chance = ThreadLocalRandom.current().nextInt(0, 100);
			if(chance == 0)
			{
				for(Player t: Bukkit.getOnlinePlayers())
				{
					t.playSound(p.getLocation(), Sound.ZOMBIE_IDLE, 1, 1);
				}
			}
			p.setFoodLevel(2);
			
			//set their xp so that they can find the nearest person
			Location tar = nearestPlayerLocation(p);
			if(tar != null)
			{
				Location eye = p.getEyeLocation();
				Location offsetTar = new Location(tar.getWorld(), 
						tar.getX() - eye.getX(),
						tar.getY(),
						tar.getZ() - eye.getZ());

				double angle;
				long percent = 0;
				if(offsetTar.getBlockX() == 0)
				{
					angle = eye.getPitch();
					percent = (offsetTar.getBlockZ() > 0) ? 0 : 1;
				}
				else
				{
					double sign = Math.abs(offsetTar.getBlockX())/offsetTar.getBlockX();
					double ratio = Math.abs(offsetTar.getZ()/offsetTar.getX());
					angle = Math.atan(ratio);
					if(offsetTar.getBlockZ() < 0) angle += 90;			
					angle *= sign;
					percent = Math.round(eye.getPitch() / angle);
				}
				System.out.println(percent);
				p.setLevel((int) percent);
				p.setExp(percent);
			}
			
		}
		
		else if(pd.isNewbie)
		{
			long currentSession = System.currentTimeMillis() - pd.lastLogin;
			if(pd.timePlayed + currentSession > HardcoreSurvival.instance.newbieTimer * 1000 * 60)
			{
				pd.isNewbie = false;
				p.sendMessage(ChatColor.RED + Messages.grace_ended.getMsg());
			}
		}
		
		else if(pd.isGraced)
		{
			long currentSession = System.currentTimeMillis() - pd.lastLogin;
			if(pd.timePlayed + currentSession > HardcoreSurvival.instance.graceTimer * 1000 * 60)
			{
				pd.isGraced = false;
				p.sendMessage(ChatColor.RED + Messages.grace_ended.getMsg());
			}
		}		
	}

	@EventHandler
	public void onEat(PlayerItemConsumeEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isZombie)
		{
			p.getWorld().dropItemNaturally(p.getLocation(), p.getItemInHand());
			p.setItemInHand(null);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isZombie)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = new PlayerData(p);
		if(pd.isNewbie)
		{
			p.sendMessage(ChatColor.GREEN + Messages.newbie_login.getMsg());
		}
		HardcoreSurvival.players.put(p.getUniqueId(), pd);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		pd.updateTime();
		
		//TODO do score stuff
		pd.savePlayer();
		HardcoreSurvival.players.remove(p.getUniqueId());		
	}

	@EventHandler
	public void onIntentoryOpen(InventoryOpenEvent event)
	{
		Player p = (Player) event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isZombie)
			event.setCancelled(true);
	}

	//Tell players how to use the compass
	@EventHandler
	public void changeHand(PlayerItemHeldEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isZombie && !p.getItemInHand().getType().equals(Material.AIR))
		{
			p.getWorld().dropItemNaturally(p.getLocation(), p.getItemInHand());
			p.setItemInHand(null);
		}
	}

	//point compass towards nearest player
	@EventHandler
	public void onClick(PlayerInteractEvent event)
	{
		Player p = event.getPlayer();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isZombie)
			event.setCancelled(true);
		
		
	}
	
	private Location nearestPlayerLocation(Player p) {
		Location ret = null;
		Double far = Double.MAX_VALUE;
		
		for(Player t : Bukkit.getOnlinePlayers())
		{
			PlayerData td = HardcoreSurvival.getPlayerData(t);
			//if they aren't in the same world
			if(td.isZombie || td.isGraced || td.isNewbie || !(t.getWorld().equals(p.getWorld()))) continue;
			Location l = t.getLocation();
			double dist = l.distance(p.getLocation());
			if(dist < far)
			{
				far = dist;
				ret = t.getLocation();
			}
		}
		return ret;
	}

}

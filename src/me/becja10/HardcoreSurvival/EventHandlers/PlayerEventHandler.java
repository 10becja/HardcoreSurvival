package me.becja10.HardcoreSurvival.EventHandlers;

import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
	
	private PotionEffect strength = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false);
	private PotionEffect speed = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2, false, false);
	private PotionEffect night = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2, false, false);
	private PotionEffect hunger = new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0, true, true);
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player p = event.getEntity();
		Player killer = p.getKiller();
		
		//p.getLastDamageCause()
		
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		PlayerData kd = (killer == null) ? null : HardcoreSurvival.getPlayerData(killer);
		
		pd.deaths++;
		
		if(pd.deaths > 2 && !pd.isZombie)
		{
			pd.isZombie = true;
			p.sendMessage(ChatColor.DARK_GREEN + "You are now a zombie!");
			p.performCommand("abandonallclaims");
			HardcoreSurvival.scoreboard.getTeam(HardcoreSurvival.zombieTeam).addPlayer(p);
		}

		pd.lastDeath = System.currentTimeMillis();
		pd.lastKiller = killer;
		
		int deadScore = (pd.isZombie) ? HardcoreSurvival.instance.zombiePlayerDeath : HardcoreSurvival.instance.playerDeath;

		if(kd != null)
		{
			
			int base = 0; 
			switch(pd.deaths)
			{
				case 1:
					base = HardcoreSurvival.instance.playerKill1;
					break;
				case 2:
					base = HardcoreSurvival.instance.playerKill2;
					break;
				case 3:
					base = HardcoreSurvival.instance.playerKill3;
					break;
				default:
					base = HardcoreSurvival.instance.zombiePlayerKill;
			}
			
			long ratio = (HardcoreSurvival.instance.scaleScores && !kd.isZombie && !pd.isZombie) ? (pd.score / kd.score) : 1;
			int kScore = Math.round(ratio * base);
			deadScore = Math.round(ratio * deadScore);
			
			if(kd.lastKiller != null && kd.lastKiller.equals(p) && System.currentTimeMillis() - kd.lastDeath <= 1000*60 * HardcoreSurvival.instance.graceTimer)
			{
				kScore += HardcoreSurvival.instance.revengeKill;
				kd.lastKiller = null;
				killer.sendMessage(ChatColor.DARK_RED + Messages.revenge_kill.getMsg());
			}
			kd.kills++;
			if(pd.score > 0)
				kd.score += kScore;
			kd.savePlayer();
		}
		if(kd == null) pd.envDeaths++;
		else pd.plrDeaths++;
		pd.score -= deadScore;
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
			pd.maxHealth = 4;
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
			if(p.getFoodLevel() < 2)
				p.setFoodLevel(2);
			
			if(!p.hasPotionEffect(PotionEffectType.HUNGER))
				p.addPotionEffect(hunger);
			
			//set their xp so that they can find the nearest person
			zombieTargetting(p, pd);
			
			if(p.getWorld().getTime() > 14000 )
			{
				p.addPotionEffect(speed);
				p.addPotionEffect(strength);
				p.addPotionEffect(night);
			}
			else
			{
				for(PotionEffect pe : p.getActivePotionEffects())
				{
					if(pe.getDuration() > 20L * 1000 && !(pe.getType().equals(PotionEffectType.HUNGER)))
					{
						p.removePotionEffect(pe.getType());
					}
				}
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

	//Force zombies to drop anything in their hand
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
		{
			event.setCancelled(true);
			return;
		}
		
		//only care about when they have a compass
		if(!(p.getItemInHand().getType() == Material.COMPASS))
			return;
		
		if(event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			if(p.isSneaking())
			{
				if(p.getWorld().getEnvironment() == Environment.NORMAL)
				{
					p.setCompassTarget(p.getWorld().getSpawnLocation());
					p.sendMessage(ChatColor.BLUE + "Pointing towards spawn.");
				}
			}
			else
			{
				if(pd.base.getWorld() == null)
				{
					p.sendMessage(ChatColor.GOLD + "You have no base set.");
				}
				else if(pd.base.getWorld() != p.getLocation().getWorld())
				{
					p.sendMessage(ChatColor.GOLD + "Your base is not in this world.");
				}
				else
				{
					p.setCompassTarget(pd.base);
					p.sendMessage(ChatColor.GREEN + "Your compass now points towards your base");
				}
			}
		}
		else if(event.getAction() == Action.LEFT_CLICK_AIR)
		{
			if(pd.isNewbie)
			{
				p.sendMessage(ChatColor.GOLD + "You are still under newbie protection and can not hunt. Use /removeprotection if "
						+ "you want to end your protection early");
			}
			else
			{
				if(p.isSneaking())
				{
//					Entity boss = HardcoreSurvival.instance.boss;
//					if(boss == null)
//						p.sendMessage(ChatColor.RED + "There is no boss to hunt.");
//					else
//					{
//						p.sendMessage(ChatColor.AQUA + "Targetting the last known location of " + boss.getCustomName());
//						p.setCompassTarget(boss.getLocation());
//					}
				}
				else
				{
					Player tar = nearestPlayer(p);
					if(tar == null)
					{
						p.sendMessage(ChatColor.RED + "There are no players to target.");
					}
					else
					{
						p.setCompassTarget(tar.getLocation());
						p.sendMessage(ChatColor.GREEN + "Targetting the last known location of the nearest player. They are "
								+ p.getLocation().distance(tar.getLocation()) + " blocks away.");
					}
				}
			}
		}		
	}
	
	private void zombieTargetting(Player p, PlayerData pd) {
		Location tar = pd.getTargetLocation();
		if(tar == null)
		{
			pd.target = nearestPlayer(p);
			p.sendMessage(ChatColor.GREEN + "Targetting the nearest player: " + ChatColor.RED + pd.target.getName());
		}
		if(tar != null)
		{
			Location eyeLoc = p.getEyeLocation();
			Location offsetTar = new Location(tar.getWorld(), 
					tar.getX() - eyeLoc.getX(),
					tar.getY(),
					tar.getZ() - eyeLoc.getZ());
			
			double x = offsetTar.getX(), z = offsetTar.getZ();
			int quad;
							
			if	   (x < 0 && z > 0) quad = 0;
			else if(x < 0 && z < 0) quad = 1;
			else if(x > 0 && z < 0) quad = 2;
			else   					quad = 3;
			
			double angle;
			if(x == 0.0)
				angle = (z > 0) ? 0 : 180;
			else if(z == 0.0)
				angle = (x > 0) ? 270 : 90;
			else{
				double tx = Math.abs(offsetTar.getX());
				double tz = Math.abs(offsetTar.getZ());
				angle = Math.toDegrees(Math.atan2(tx,tz));
				switch(quad){
				case 1:	angle = 180 - angle; break;
				case 2:	angle = 180 + angle; break;
				case 3: angle = 360 - angle; break;
				}
			}
			double theta = Math.max(eyeLoc.getYaw(), angle) - Math.min(eyeLoc.getYaw(), angle);
			if(theta > 180) theta = 360 - theta;
			theta = 1 - (theta/180);
			p.setLevel((int)Math.round(p.getLocation().distance(tar)));
			p.setExp((float) theta);				
		}
	}

	
	private Player nearestPlayer(Player p) {
		Player ret = null;
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
				ret = t;
			}
		}
		return ret;
	}

}

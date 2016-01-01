package me.becja10.HardcoreSurvival.EventHandlers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityEventHandler implements Listener {

	private PotionEffectType[] list = new PotionEffectType[]
			{
				PotionEffectType.POISON,
				PotionEffectType.BLINDNESS,
				PotionEffectType.CONFUSION,
				PotionEffectType.HARM,
				PotionEffectType.HUNGER,
				PotionEffectType.WEAKNESS,
				PotionEffectType.SLOW
			};
	
	private HashSet<UUID> badSpawns = new HashSet<UUID>();
	
//	private EntityType[] bossTypes = new EntityType[]
//			{
//				EntityType.ZOMBIE,
//				EntityType.SKELETON,
//				EntityType.SPIDER,
//				EntityType.WITCH
//			};
	
	private PotionEffect[] zombieEffects = new PotionEffect[]
			{
				new PotionEffect(PotionEffectType.CONFUSION, 20*20, 4),
				new PotionEffect(PotionEffectType.POISON, 20*10, 0),
				new PotionEffect(PotionEffectType.SLOW, 20*15, 3)
			};

	@EventHandler
	public void onHealthRegen(EntityRegainHealthEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;

		Player p = (Player) event.getEntity();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);

		if(pd.isZombie){
			event.setCancelled(true);
			return;
		}

		double amount = event.getAmount();
		double pHealth = p.getHealth();
		if(pHealth + amount > pd.maxHealth)
			p.setHealth(pd.maxHealth);

	}
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent event){
		if(event instanceof CreatureSpawnEvent)
		{
			if(!((CreatureSpawnEvent) event).getSpawnReason().equals(SpawnReason.NATURAL))
			{
				badSpawns.add(event.getEntity().getUniqueId());
			}
		}
		
//		if(HardcoreSurvival.instance.boss == null)
//		{
//			if(Arrays.asList(bossTypes).contains(event.getEntity().getType()))
//			{
//				LivingEntity mob = (LivingEntity) event.getEntity();
//				HardcoreSurvival.instance.ChangeToBoss(mob);
//			}
//		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event)
	{
		if(badSpawns.contains(event.getEntity().getUniqueId()))
			return;
		
		LivingEntity mob = event.getEntity();
		Player p = mob.getKiller();
		if(p == null)
			return;
		
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		pd.addMobKill(mob); //this takes care of all scoring and saving
		
//		if(event.getEntity() == HardcoreSurvival.instance.boss)
//		{
//			LivingEntity boss = event.getEntity();
//			Entity name = boss.getPassenger();
//			name.remove();	
//			HardcoreSurvival.instance.boss = null;
//			//TODO add scoring
//			
//		}
	}

	//Don't let hostile mobs target zombie players
	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent event)
	{
		//if the thing targetting is a player, or if the target ISN'T a player, don't do anything
		if(event.getEntity() instanceof Player || !(event.getTarget() instanceof Player) /*|| event.getEntity() == HardcoreSurvival.instance.boss*/)
			return;

		Player target = (Player) event.getTarget();
		PlayerData td = HardcoreSurvival.getPlayerData(target);
		if(td.isZombie)
			event.setTarget(null);

	}

	@EventHandler
	public void onDamage(EntityDamageEvent event)
	{
		//also only concerned if the thing being damaged is a Player
		if(!(event.getEntity() instanceof Player)) return;

		Player p = (Player) event.getEntity();
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		DamageCause dc = event.getCause();
		//zombies can't be hurt by most damage
		if(pd.isZombie && !(dc.equals(DamageCause.ENTITY_ATTACK)))
		{
			event.setCancelled(true);
			return;
		}
		
		pd.recievedDamage(event.getDamage());

		//create a null Player in case it turns out the attacker wasn't a player
		Player attacker = getAttacker(event);
		
		if(attacker == null)
		{
//			if(event instanceof EntityDamageByEntityEvent)
//			{
//				Entity e = ((EntityDamageByEntityEvent)event).getDamager();
//				if( e == HardcoreSurvival.instance.boss)
//				{
//					if(pd.isZombie)
//						event.setDamage(event.getDamage() * .25);
//					else
//						event.setDamage(event.getDamage() * 4);
//				}
//			}
			return;
		}
		
		PlayerData attackerData = HardcoreSurvival.getPlayerData(attacker);
		
		//at this point, we know it's a player attacking another player
		if(attackerData.isGraced)
		{
			attackerData.isGraced = false;
			attacker.sendMessage(ChatColor.RED + Messages.grace_ended.getMsg());
		}
		else if(attackerData.isNewbie)
		{
			event.setCancelled(true);
			attacker.sendMessage(ChatColor.GOLD + Messages.cant_attack_while_newb.getMsg());
		}
		
		else if(pd.isGraced || pd.isNewbie)
		{
			event.setCancelled(true);
			attacker.sendMessage(ChatColor.RED + p.getName() + Messages.cant_attack_graced_players.getMsg());
		}
		else if(attackerData.isZombie)
		{
			p.addPotionEffects(Arrays.asList(zombieEffects));
		}
	}

	@EventHandler
	public void onSplash(PotionSplashEvent e)
	{
		//only care if a player threw something
		if(e.getEntity().getShooter() instanceof Player)
		{
			Player thrower = (Player) e.getEntity().getShooter(); 

			for(LivingEntity thing : e.getAffectedEntities())
			{	
				//only care if entity is Player
				if(thing instanceof Player)
				{					
					Player hit = (Player) thing;
					if(hit.equals(thrower)) continue;
					
					PlayerData hitData = HardcoreSurvival.getPlayerData(hit);
					if(hitData.isGraced || hitData.isNewbie)
					{						
						for(PotionEffect effect : e.getPotion().getEffects())
						{
							if(Arrays.asList(list).contains(effect.getType()))						
								e.setIntensity(hit, -1);
						}
					}
					else if(hitData.isZombie)
						e.setIntensity(hit, -1);
				}
			}
		}
	}


	private Player getAttacker(EntityDamageEvent event)
	{
		if(!(event instanceof EntityDamageByEntityEvent))
			return null;
		
		Entity damageSource = ((EntityDamageByEntityEvent)event).getDamager();
		Player attacker = null;

		//if the damaged is caused by a player
		if(damageSource instanceof Player)
			attacker = (Player) damageSource;

		else if (damageSource instanceof Arrow)
		{
			Arrow arrow = (Arrow) damageSource;
			if(arrow.getShooter() instanceof Player)
				attacker = (Player)arrow.getShooter();
		}
		else if(damageSource instanceof ThrownPotion)
		{
			ThrownPotion potion = (ThrownPotion)damageSource;
			if(potion.getShooter() instanceof Player)
				attacker = (Player)potion.getShooter();
		}
		else if(damageSource instanceof Egg)
		{
			Egg egg = (Egg) damageSource;
			if(egg.getShooter() instanceof Player)
				attacker = (Player)egg.getShooter();
		}
		else if(damageSource instanceof Snowball)
		{
			Snowball ball = (Snowball) damageSource;
			if(ball.getShooter() instanceof Player)
				attacker = (Player)ball.getShooter();
		}
		else if(damageSource instanceof FishHook)
		{
			FishHook hook = (FishHook) damageSource;
			if(hook.getShooter() instanceof Player)
				attacker = (Player)hook.getShooter();
		}
		else if(damageSource instanceof EnderPearl)
		{
			EnderPearl pearl = (EnderPearl) damageSource;
			if(pearl.getShooter() instanceof Player)
				attacker = (Player)pearl.getShooter();
		}

		return attacker;
	}

}

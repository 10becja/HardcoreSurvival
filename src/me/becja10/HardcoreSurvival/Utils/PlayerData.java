package me.becja10.HardcoreSurvival.Utils;

import java.util.UUID;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

public class PlayerData {
	
	public UUID playerID;
	
	public String playerName;
	public int kills;
	public int deaths;
	public double score;
	public long timePlayed;
	public Boolean isNewbie;
	public Location base; 
	
	public Boolean isZombie;
	public Boolean isGraced;
	public long lastDeath;
	public Player lastKiller;			//A way to do revenge killings
	public double maxHealth;
	public Player target;
	public long lastTimestamp;
	
	public int blazeMobKills;
	public int creeperMobKills;
	public int elderGuardianMobKills;
	public int endermanMobKills;
	public int endermiteMobKills;
	public int ghastMobKills;
	public int guardianMobKills;
	public int magmacubeMobKills;
	public int skeletonMobKills;
	public int slimeMobKills;
	public int spiderMobKills;
	public int witchMobKills;
	public int witherSkeletonMobKills;
	public int zombieMobKills;
	public int dragonMobKills;
	public int witherMobKills;
	public int bossMobKills;
	
	public int envDeaths;
	public int plrDeaths;
	
	@SuppressWarnings("deprecation")
	public PlayerData(Player player)
	{
		playerID = player.getUniqueId();
		playerName = player.getName(); 
		String id = playerID.toString();
		
		kills = PlayerManager.getPlayers().getInt(id+".kills", 0);
		deaths = PlayerManager.getPlayers().getInt(id+".deaths", 0);
		score = PlayerManager.getPlayers().getDouble(id+".score", (long) HardcoreSurvival.instance.startingScore);
		timePlayed = PlayerManager.getPlayers().getLong(id+".timePlayed", 0);
		isNewbie = PlayerManager.getPlayers().getBoolean(id+".isNewbie", true);
		
		double x, y, z;
		String worldName;
		x = PlayerManager.getPlayers().getDouble(id+".base.x", 0);
		y = PlayerManager.getPlayers().getDouble(id+".base.y", 0);
		z = PlayerManager.getPlayers().getDouble(id+".base.z", 0);
		worldName = PlayerManager.getPlayers().getString(id+".base.world", "null");
		World world = Bukkit.getWorld(worldName);
		
		base = new Location(world, x, y, z);
		
		lastTimestamp = System.currentTimeMillis();
		isGraced = false;
		isZombie = deaths > 2;
		if(isZombie) HardcoreSurvival.scoreboard.getTeam(HardcoreSurvival.zombieTeam).addPlayer(player);
		else HardcoreSurvival.scoreboard.getTeam(HardcoreSurvival.playerTeam).addPlayer(player);

		isNewbie = (isNewbie && (timePlayed < HardcoreSurvival.instance.newbieTimer * 60000));
		lastDeath = 0;
		lastKiller = null;
		switch(deaths)
		{
		case 0:
			maxHealth = 20;
			break;
		case 1:
			maxHealth = 16;
			break;
		case 2:
			maxHealth = 10;
			break;
		default:
			maxHealth = 4;
			break;
		}
		player.setMaxHealth(maxHealth);
		
		blazeMobKills = ScoresManager.getScores().getInt(id+".kills.blaze", 0);
		creeperMobKills = ScoresManager.getScores().getInt(id+".kills.creeper", 0);
		elderGuardianMobKills = ScoresManager.getScores().getInt(id+".kills.elderGuardian", 0);
		endermanMobKills = ScoresManager.getScores().getInt(id+".kills.enderman", 0);
		endermiteMobKills = ScoresManager.getScores().getInt(id+".kills.endermite", 0);
		ghastMobKills = ScoresManager.getScores().getInt(id+".kills.ghast", 0);
		guardianMobKills = ScoresManager.getScores().getInt(id+".kills.guardian", 0);
		magmacubeMobKills = ScoresManager.getScores().getInt(id+".kills.magmacube", 0);
		skeletonMobKills = ScoresManager.getScores().getInt(id+".kills.skeleton", 0);
		slimeMobKills = ScoresManager.getScores().getInt(id+".kills.slime", 0);
		spiderMobKills = ScoresManager.getScores().getInt(id+".kills.spider", 0);
		witchMobKills = ScoresManager.getScores().getInt(id+".kills.witch", 0);
		witherSkeletonMobKills = ScoresManager.getScores().getInt(id+".kills.witherSkeleton", 0);
		zombieMobKills = ScoresManager.getScores().getInt(id+".kills.zombie", 0);
		dragonMobKills = ScoresManager.getScores().getInt(id+".kills.dragon", 0);
		witherMobKills = ScoresManager.getScores().getInt(id+".kills.wither", 0);
		bossMobKills = ScoresManager.getScores().getInt(id+".kills.boss", 0);
		
		plrDeaths = ScoresManager.getScores().getInt(id+"death by player", 0);
		envDeaths = ScoresManager.getScores().getInt(id+"death by environment", 0);
		
		savePlayer();
	}
	
	public void updateTime(){
		long timeDiff = (System.currentTimeMillis() - lastTimestamp);
		int seconds = (int) (timeDiff/1000);
						
		if(seconds > 0 )
		{
			lastTimestamp = System.currentTimeMillis();

			timePlayed += timeDiff;
			
			score += (HardcoreSurvival.instance.playtime) * (seconds / (double) 60);
			savePlayer();
		}
	}
	
	public void recievedDamage(double damage)
	{
		score -= HardcoreSurvival.instance.recievingDamage * damage;
		if(score < 0) score = 0;
		savePlayer();
	}
	
	public void setBase(Location l){
		base = l;
		savePlayer();
	}
	
	public void addMobKill(LivingEntity mob)
	{
		EntityType type = mob.getType();
		switch(type)
		{
		case BLAZE:
			blazeMobKills++;
			score += HardcoreSurvival.instance.blazeMobKill;
			break;
		case CREEPER:
			creeperMobKills++;
			score += HardcoreSurvival.instance.creeperMobKill;
			break;
		case ENDERMAN:
			if(!mob.getLocation().getWorld().getEnvironment().equals(Environment.THE_END))
			{
				endermanMobKills++;
				score += HardcoreSurvival.instance.endermanMobKill;
			}
			break;
		case ENDERMITE:
			endermiteMobKills++;
			score += HardcoreSurvival.instance.endermiteMobKill;
			break;
		case ENDER_DRAGON:
			dragonMobKills++;
			score += HardcoreSurvival.instance.dragonMobKill;
			break;
		case GHAST:
			ghastMobKills++;
			score += HardcoreSurvival.instance.ghastMobKill;
			break;
		case GUARDIAN:
			if(((Guardian) mob).isElder())
			{
				elderGuardianMobKills++;
				score += HardcoreSurvival.instance.elderGuardianMobKill;
			}
			else
			{
				guardianMobKills++;
				score += HardcoreSurvival.instance.guardianMobKill;
			}
			break;
		case MAGMA_CUBE:
			magmacubeMobKills++;
			score += HardcoreSurvival.instance.magmacubeMobKill;
			break;
		case SKELETON:
			if(((Skeleton) mob).getSkeletonType().equals(SkeletonType.WITHER))
			{
				witherSkeletonMobKills++;
				score += HardcoreSurvival.instance.witherSkeletonMobKill;
			}
			else
			{
				skeletonMobKills++;
				score += HardcoreSurvival.instance.skeletonMobKill;
			}
			break;
		case SLIME:
			slimeMobKills++;
			score += HardcoreSurvival.instance.slimeMobKill;
			break;
		case SPIDER:
			spiderMobKills++;
			score += HardcoreSurvival.instance.spiderMobKill;
			break;
		case WITCH:
			witchMobKills++;
			score += HardcoreSurvival.instance.witchMobKill;
			break;
		case WITHER:
			witherMobKills++;
			score += HardcoreSurvival.instance.witherMobKill;
			break;
		case ZOMBIE:
			zombieMobKills++;
			score += HardcoreSurvival.instance.zombieMobKill;
			break;
		default:
			break;
		}
		
		savePlayer();
	}
	
	public void savePlayer(){
		String id = playerID.toString();
		String world = base.getWorld() == null ? "null" : base.getWorld().getName();

		PlayerManager.getPlayers().set(id+".name", playerName);
		PlayerManager.getPlayers().set(id+".kills", kills);
		PlayerManager.getPlayers().set(id+".deaths", deaths);
		PlayerManager.getPlayers().set(id+".score", score);
		PlayerManager.getPlayers().set(id+".timePlayed", timePlayed);
		PlayerManager.getPlayers().set(id+".isNewbie", isNewbie);
		PlayerManager.getPlayers().set(id+".base.x", base.getX());
		PlayerManager.getPlayers().set(id+".base.y", base.getY());
		PlayerManager.getPlayers().set(id+".base.z", base.getZ());
		PlayerManager.getPlayers().set(id+".base.world", world);
		
		ScoresManager.getScores().set(id+".kills.blaze", blazeMobKills);
		ScoresManager.getScores().set(id+".kills.creeper", creeperMobKills);
		ScoresManager.getScores().set(id+".kills.elderGuardian", elderGuardianMobKills);
		ScoresManager.getScores().set(id+".kills.enderman", endermanMobKills);
		ScoresManager.getScores().set(id+".kills.endermite", endermiteMobKills);
		ScoresManager.getScores().set(id+".kills.ghast", ghastMobKills);
		ScoresManager.getScores().set(id+".kills.guardian", guardianMobKills);
		ScoresManager.getScores().set(id+".kills.magmacube", magmacubeMobKills);
		ScoresManager.getScores().set(id+".kills.skeleton", skeletonMobKills);
		ScoresManager.getScores().set(id+".kills.slime", slimeMobKills);
		ScoresManager.getScores().set(id+".kills.witch", witchMobKills);
		ScoresManager.getScores().set(id+".kills.witherSkeleton", witherSkeletonMobKills);
		ScoresManager.getScores().set(id+".kills.zombie", zombieMobKills);
		ScoresManager.getScores().set(id+".kills.dragon", dragonMobKills);
		ScoresManager.getScores().set(id+".kills.wither", witherMobKills);
		ScoresManager.getScores().set(id+".kills.boss", bossMobKills);
		
		ScoresManager.getScores().set(id+".death by player", plrDeaths);
		ScoresManager.getScores().set(id+".death by environment", envDeaths);
		
		ScoresManager.saveScores();
		PlayerManager.savePlayers();
		
	}

	public Location getTargetLocation() {
		Player p = Bukkit.getPlayer(playerID);
		if(target == null) return null;
		if(!target.isOnline())
		{
			p.sendMessage(ChatColor.GOLD + "Your target is no longer online.");
			target = null;
		}
		else if(target.getLocation().getWorld() != p.getWorld())
		{
			p.sendMessage(ChatColor.GOLD + "Your target is no longer in the same world as you.");
			target = null;
		}
		return (target == null) ? null : target.getLocation();
	}
	
	
		
	

}

package me.becja10.HardcoreSurvival.Utils;

import java.util.UUID;

import me.becja10.HardcoreSurvival.HardcoreSurvival;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerData {
	
	public UUID playerID;
	
	public String playerName;
	public int kills;
	public int deaths;
	public int score;
	public long timePlayed;
	public long lastLogin;
	public Location base; 
	
	public Boolean isZombie;
	public Boolean isNewbie;
	public Boolean isGraced;
	public long lastDeath;
	public Player lastKiller;			//A way to do revenge killings
	public double maxHealth;
	
	public PlayerData(Player player)
	{
		playerID = player.getUniqueId();
		playerName = player.getName(); 
		String id = playerID.toString();
		
		kills = PlayerManager.getPlayers().getInt(id+".kills", 0);
		deaths = PlayerManager.getPlayers().getInt(id+".deaths", 0);
		score = PlayerManager.getPlayers().getInt(id+".score", 0);
		timePlayed = PlayerManager.getPlayers().getLong(id+".timePlayed", 0);
		lastLogin = System.currentTimeMillis();
		
		double x, y, z;
		String worldName;
		x = PlayerManager.getPlayers().getDouble(id+".base.x", 0);
		y = PlayerManager.getPlayers().getDouble(id+".base.y", 0);
		z = PlayerManager.getPlayers().getDouble(id+".base.z", 0);
		worldName = PlayerManager.getPlayers().getString(id+".base.world", "null");
		World world = Bukkit.getWorld(worldName);
		
		base = new Location(world, x, y, z);
		
		isGraced = false;
		isZombie = deaths > 2;		
		isNewbie = (!PlayerManager.getPlayers().contains(id)) || 
				   (PlayerManager.getPlayers().getLong(id+".timePlayed") < HardcoreSurvival.instance.newbieTimer * 60000);
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
			maxHealth = 2;
			break;
		}
		player.setMaxHealth(maxHealth);
	}
	
	public void updateTime(){
		long timeDiff = System.currentTimeMillis() - lastLogin;
		timePlayed += timeDiff;
	}
	
	public void savePlayer(){
		String id = playerID.toString();
		PlayerManager.getPlayers().set(id+".name", playerName);
		PlayerManager.getPlayers().set(id+".kills", kills);
		PlayerManager.getPlayers().set(id+".deaths", deaths);
		PlayerManager.getPlayers().set(id+".score", score);
		PlayerManager.getPlayers().set(id+".timePlayed", timePlayed);
		PlayerManager.getPlayers().set(id+".lastLogin", lastLogin);
		PlayerManager.getPlayers().set(id+".base.x", base.getX());
		PlayerManager.getPlayers().set(id+".base.y", base.getY());
		PlayerManager.getPlayers().set(id+".base.z", base.getZ());
		String world = base.getWorld() == null ? "null" : base.getWorld().getName();
		PlayerManager.getPlayers().set(id+".base.world", world);
		
		PlayerManager.savePlayers();
		
	}
	
	
		
	

}

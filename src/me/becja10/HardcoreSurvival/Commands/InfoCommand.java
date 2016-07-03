package me.becja10.HardcoreSurvival.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;
import me.becja10.HardcoreSurvival.Utils.PlayerManager;

public class InfoCommand {
	
	

	public static boolean Top(CommandSender sender) 
	{
		
		if(!sender.hasPermission("hardcore.command.top"))
		{
			sender.sendMessage(Messages.no_permission.getMsg());
			return true;
		}
		
		updatePlayers();		
		final FileConfiguration players = PlayerManager.getPlayers();
		List<String> list = getSortedList(players);
		
		//should have a list of UUIDs sorted based off score now
		for(int i = 0; i < Math.min(10, list.size()); i++)
		{
			String id = list.get(i);
			String name = players.getString(id+".name");
			int score = players.getInt(id+".score");
			int deaths = players.getInt(id+".deaths");
			
			name = (deaths > 2) ? ChatColor.DARK_GREEN + name : ChatColor.DARK_AQUA + name;
			
			String index = ChatColor.YELLOW + "" + (i + 1) + ". ";						
			sender.sendMessage(index + name + ChatColor.YELLOW + ": " + ChatColor.GOLD + score);
		}
		
		return true;
	}

	public static boolean Rank(CommandSender sender, String[] args) 
	{
		if (!(sender instanceof Player) && args.length == 0)
		{
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		updatePlayers();		
		final FileConfiguration players = PlayerManager.getPlayers();
		List<String> list = getSortedList(players);
		
		if(args.length == 0)
		{
			if(!sender.hasPermission("hardcore.command.rank"))
			{
				sender.sendMessage(Messages.no_permission.getMsg());
				return true;
			}
			
			Player p = (Player) sender;
			int rank = list.indexOf(p.getUniqueId().toString()) + 1;
			
			p.sendMessage(ChatColor.DARK_AQUA+"You are ranked " + ChatColor.YELLOW  + rank 
					+ ChatColor.DARK_AQUA + " out of " + ChatColor.YELLOW + list.size());
			
		}
		else
		{
			if(sender instanceof Player && !sender.hasPermission("hardcore.command.rank.others"))
			{
				sender.sendMessage(Messages.no_permission.getMsg());
				return true;
			}
			
			@SuppressWarnings("deprecation")
			String id = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
			if(!players.contains(id))
			{
				sender.sendMessage(Messages.player_not_found.getMsg());
			}
			else
			{
				int rank = list.indexOf(id);
				sender.sendMessage(ChatColor.DARK_AQUA + players.getString(id+".name") + " is ranked " 
						+ ChatColor.YELLOW + rank + ChatColor.DARK_AQUA + " out of " + ChatColor.YELLOW + list.size());
			}
		}		
		return true;
	}
	

	public static boolean Stats(CommandSender sender, String[] args) 
	{
		if (!(sender instanceof Player) && args.length == 0)
		{
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		updatePlayers();		
		final FileConfiguration players = PlayerManager.getPlayers();
		
		if(args.length == 0)
		{
			if(!sender.hasPermission("hardcore.command.stats"))
			{
				sender.sendMessage(Messages.no_permission.getMsg());
				return true;
			}
			
			Player p = (Player) sender;

			GetStatsFor(sender, p.getUniqueId().toString(), players);
			
		}
		else
		{
			if(sender instanceof Player && !sender.hasPermission("hardcore.command.stats.others"))
			{
				sender.sendMessage(Messages.no_permission.getMsg());
				return true;
			}
			
			@SuppressWarnings("deprecation")
			String id = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
			if(!players.contains(id))
			{
				sender.sendMessage(Messages.player_not_found.getMsg());
			}
			else
			{
				GetStatsFor(sender, id, players );
			}
		}		
		return true;
	}
	
	private static void GetStatsFor(CommandSender sender, String id, FileConfiguration players)
	{
		/*
		 * 
		 * Stats for <name>:
		 *   Kills: 
   		 *   Deaths:
   		 *   Score:
		 *   Time Played: <formatted time>
		 *    
		 */
		
		sender.sendMessage(ChatColor.DARK_AQUA + "Stats for " + ChatColor.YELLOW + players.getString(id+".name") + ChatColor.DARK_AQUA + ":");
		sender.sendMessage("  " + ChatColor.RED + "Kills: " + ChatColor.YELLOW + players.getInt(id+".kills"));
		sender.sendMessage("  " + ChatColor.GRAY + "Deaths: " + ChatColor.YELLOW + players.getInt(id+".deaths"));
		sender.sendMessage("  " + ChatColor.AQUA + "Score: " + ChatColor.YELLOW + players.getInt(id+".score"));
		sender.sendMessage("  " + ChatColor.DARK_PURPLE + "Time Played: " + ChatColor.YELLOW + FormatTime(players.getInt(id+".timePlayed")/1000));
		
		sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.YELLOW + "/rank " + players.getString(id+".name") + 
				ChatColor.DARK_AQUA + " to see their ranking." );
	}
	
	private static String FormatTime(int t)
	{
		String time =  "";
		if (t >= 3600)
		{
			int hours = (t / 3600); //how many hours
			int min = ((t - (hours * 3600))/60);// left over minutes
			int sec = (t - (hours * 3600) - (min * 60));
			String sHour = (hours > 1) ? " hours " : " hour ";
			String sMin = (min > 1) ? " minutes " : " minute ";
			String sSec = (sec > 1) ? " seconds" : " second";
			time = hours + sHour + min + sMin + sec + sSec;
		}
		else if (t >= 60)
		{
			int min = t/60;
			int sec = ((t - (min * 60)));
			String sMin = (min > 1) ? " minutes " : " minute ";
			String sSec = (sec > 1) ? " seconds" : " second";
			time = min + sMin + sec + sSec;
		}
		else
			time = t + ((t > 1) ? " seconds" : " second");
		
		return time;
	}
	
	private static List<String> getSortedList(final FileConfiguration players) {
		List<String> list = new ArrayList<String>(players.getKeys(false));
		
		Collections.sort(list, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				double i1 = players.getDouble(o1+".score");
				double i2 = players.getDouble(o2+".score");				
				return i1 > i2 ? -1 : i1 == 12 ? 0 : 1;
			}			
		});
		return list;
	}
	
	private static void updatePlayers() {
		for(PlayerData pd : HardcoreSurvival.players.values())
		{
			pd.updateTime();
		}
		
	}
}

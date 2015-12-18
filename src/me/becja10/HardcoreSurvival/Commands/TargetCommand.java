package me.becja10.HardcoreSurvival.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;

public class TargetCommand {

	
	public static boolean HandleCommand(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		
		if(args.length != 1)
			return false;
		
		Player p = (Player) sender;
		if(!p.hasPermission("hardcoresurvival.command.target"))
		{
			sender.sendMessage(Messages.no_permission.getMsg());
			return true;
		}
		
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		
		if(!pd.isZombie)
		{
			p.sendMessage(ChatColor.AQUA + Messages.not_zombie.getMsg() + " Use a compass to hunt other players.");
			return true;
		}
		
		Player tar = Bukkit.getPlayer(args[0]);
		if(tar == null)
		{
			p.sendMessage(Messages.player_not_found.getMsg());
			return true;
		}
		pd.target = tar;
		p.sendMessage(ChatColor.GREEN + "Target aquired");
		
		
		return true;
	}
}

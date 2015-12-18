package me.becja10.HardcoreSurvival.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;

public class SetBaseCommand {

	
	public static boolean HandleCommand(CommandSender sender)
	{
		if (!(sender instanceof Player))
			sender.sendMessage("This command can only be run by a player.");
		else
		{
			Player p = (Player) sender;
			if(!(p.hasPermission("hardcore.command.setbase")))
				p.sendMessage(Messages.no_permission.getMsg());
			else
			{
				PlayerData pd = HardcoreSurvival.getPlayerData(p);
				pd.setBase(p.getLocation());				
				p.sendMessage(ChatColor.GOLD+"Base set!");
			}
		}
		return true;
	}
}

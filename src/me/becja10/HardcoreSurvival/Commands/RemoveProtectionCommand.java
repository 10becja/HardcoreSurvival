package me.becja10.HardcoreSurvival.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.becja10.HardcoreSurvival.HardcoreSurvival;
import me.becja10.HardcoreSurvival.Utils.Messages;
import me.becja10.HardcoreSurvival.Utils.PlayerData;
import net.md_5.bungee.api.ChatColor;

public class RemoveProtectionCommand {
	
	public static boolean HandleCommand(CommandSender sender)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		Player p = (Player) sender;
		if(!p.hasPermission("hardcore.command.removeprotection"))
		{
			sender.sendMessage(Messages.no_permission.getMsg());
			return true;
		}
		PlayerData pd = HardcoreSurvival.getPlayerData(p);
		if(pd.isNewbie)
		{
			pd.isNewbie = false;
			p.sendMessage(ChatColor.RED + "You are no longer under newbie protection. You can be tracked and killed");
		}
		else{
			p.sendMessage(ChatColor.RED + "You don't have newbie protection");
		}
		return true;
	}

}

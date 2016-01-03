package me.becja10.HardcoreSurvival.Commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.becja10.HardcoreSurvival.Utils.Messages;

public class LostCommand {

	public static boolean HandleCommand(CommandSender sender) {
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		
		Player p = (Player) sender;
		if(!p.hasPermission("hardcore.command.lost"))
		{
			sender.sendMessage(Messages.no_permission.getMsg());
			return true;
		}
		
		if(p.getInventory().contains(Material.COMPASS))
		{
			sender.sendMessage(Messages.have_compass.getMsg());
		}
		else
		{
			p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(Material.COMPASS));
			sender.sendMessage(Messages.new_compass.getMsg());
		}
		
		return true;
	}

}

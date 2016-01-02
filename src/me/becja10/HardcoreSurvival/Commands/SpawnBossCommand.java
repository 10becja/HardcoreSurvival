package me.becja10.HardcoreSurvival.Commands;
//
//import org.bukkit.Location;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.EntityType;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//
//import me.becja10.HardcoreSurvival.HardcoreSurvival;
//import me.becja10.HardcoreSurvival.Utils.Messages;
//
public class SpawnBossCommand {
//
//	public static boolean HandleCommand(CommandSender sender, String[] args)
//	{
//		if (!(sender instanceof Player))
//		{
//			sender.sendMessage("This command can only be run by a player.");
//			return true;
//		}
//		
//		Player p = (Player) sender;
//		if(!p.hasPermission("hardcoresurvival.command.spawnboss"))
//		{
//			sender.sendMessage(Messages.no_permission.getMsg());
//			return true;
//		}
//		
//		EntityType type = EntityType.ZOMBIE;
//		if(args.length == 1)
//		{
//			type = EntityType.valueOf(args[0]);
//		}
//		
//		Location l = p.getLocation();
//		LivingEntity mob = (LivingEntity) l.getWorld().spawnEntity(l, type);
//		HardcoreSurvival.instance.ChangeToBoss(mob);
//		
//		return true;
//	}
}

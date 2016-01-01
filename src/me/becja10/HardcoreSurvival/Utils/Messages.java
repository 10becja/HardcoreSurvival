package me.becja10.HardcoreSurvival.Utils;

import org.bukkit.ChatColor;

public enum Messages {
	
	grace_ended("Your grace period has ended."),
	cant_attack_graced_players(" is under a grace period and can't be attacked"),
	cant_attack_while_newb("You can't attack players while you're still under newbie protection. Use /removeprotection if you want to attack people"), 
	newbie_login("You are under newbie protection"),
	not_zombie("You are not a zombie!"),
	revenge_kill("You have avenged your death!"), 
	player_not_found(ChatColor.RED + "Could not find a player by that name"), 
	no_permission(ChatColor.DARK_RED + "You do not have permission to use this command."), 
	have_compass(ChatColor.GRAY + "Use your compass to find the way."), 
	new_compass(ChatColor.GRAY + "Here's a compass. Use it to find your way!");
	
	String msg;
	
	private Messages(String s){
		msg = s;
	}
	
	public String getMsg(){
		return msg;
	}
}

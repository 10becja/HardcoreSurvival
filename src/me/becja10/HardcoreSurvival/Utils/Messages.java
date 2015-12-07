package me.becja10.HardcoreSurvival.Utils;

public enum Messages {
	
	grace_ended("Your grace period has ended."),
	cant_attack_graced_players(" is under a grace period and can't be attacked"),
	cant_attack_while_newb("You can't attack players while you're still under newbie grace. Use /endgrace if you want to end your grace period early"), 
	newbie_login("You are under newbie protection"),
	revenge_kill("You have avenged your death!");
	
	String msg;
	
	private Messages(String s){
		msg = s;
	}
	
	public String getMsg(){
		return msg;
	}
}

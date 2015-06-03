package node;

import java.util.ArrayList;

import classic.Flotte;
import classic.Planete;
import classic.Player;

public interface Observer {
	//Observer gets the informations from the readers
	//This observer gathers the informations from the PlanetButtons on the gameDisplay
	//Gathers also the information of local player id
	//is used to update the contextual panel
	public static ArrayList<Observer> observers=new ArrayList<Observer>();
	public void update(Planete plan); //updates the contextual panel
	public void update(Flotte fleet); //updates the contextual panel
	public void follow(int playerID); //updates the contextual panel
	public void add(int number); //updates the cursor for planet production
	public void CreatingFleet(boolean selected, Planete plan); //Updates the buttons mode from nomal to creating fleet mode.
	public void nextTurn();
	public void players(Player[] players);
	
	//while in creatingFleet mode, the already selected button (corresponding to planete plan) becomes unefficient.
	//The fleetbuttons become unefficient, and the "update(planete)" of the creatingFleet now updates the destination planet
}

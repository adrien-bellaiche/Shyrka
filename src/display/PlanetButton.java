package display;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import node.Observer;
import classic.Flotte;
import classic.Planete;
import classic.Player;

public class PlanetButton extends JButton implements Observer {
	

	private static final long serialVersionUID = 7865154762310590406L;
	private Planete planete;
	private boolean selected=false;
	
	public PlanetButton(Planete planete) {
		Observer.observers.add(this);
		this.setEnabled(true);
		this.planete=planete;
		setBackground(Color.red);
		setIcon(new ImageIcon(planete.getImage()));
		setBorderPainted(false);
		setContentAreaFilled(false);
	}
	
	public Planete getPlanet() {return this.planete;}
	public boolean getSelected() {return this.selected;}
	
	@Override public void update(Planete plan) {}
	@Override public void update(Flotte fleet) {}
	@Override public void follow(int playerID) {}
	@Override public void add(int number) {}
	@Override public void CreatingFleet(boolean selected, Planete plan) {if(this.planete.equals(plan)) {this.selected=selected;}}
	@Override public void nextTurn() {}
	@Override public void players(Player[] players) {};
}

package display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

import node.List;
import node.ListNode;
import node.Observer;
import classic.Client;
import classic.Flotte;
import classic.Planete;
import classic.Player;

public class Contextual extends JPanel implements Observer {

	//No idea what this serial does. But Eclipse wants it.
	private Planete plan;
	private Flotte fleet;
	private JLabel name;
	private JLabel text;
	private int PlayerID=Client.iaID;
	private Player[] players;

	public Contextual() {
		Observer.observers.add(this);
		this.setLayout(null);
		this.name=new JLabel();
		this.name.setVisible(true);
		this.text=new JLabel();
		this.text.setVisible(true);
		name.setFont(new Font("Tahoma", Font.BOLD, 16));
		name.setHorizontalAlignment(JLabel.CENTER);
		text.setFont(new Font("Tahoma", Font.BOLD, 14));
		text.setHorizontalAlignment(JLabel.LEFT);
		text.setVerticalAlignment(JLabel.TOP);
		text.setForeground(Color.blue);
		this.add(name);
		this.add(text);
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		this.name.setBounds(0, 0, this.getWidth(), 30);
		this.text.setBounds(0, 40, this.getWidth(), this.getHeight());
		if(plan!=null) {
			if(this.plan.getPlayer().getPlayID()==this.PlayerID) {name.setForeground(Color.green);}
			else if(this.plan.getPlayer().getPlayID()==0) {name.setForeground(Color.white);}
			else {name.setForeground(Color.red);}
			text.setText(textedFuture());
		} else if (fleet!=null) { //here it is the fleet
			if(this.fleet.getPlayOrig()==this.PlayerID) {name.setForeground(Color.green);}
			else {name.setForeground(Color.red);}
			text.setText(FleetStatus());
		} else {
			this.doNothing();
		}

	}

	public void doNothing() {} // I really need to sleep.
	
	private String textedFuture() {//This is solely to generate the html code to add in the contextual panel, case planet
		String str="<html>A-Ships : " + this.plan.getNbVA() +"<br>D-Ships : " + this.plan.getNbVD() +"<br>Coeff : " + this.plan.getCoef() +"<br> Production : " + this.plan.getProd()+ "<br>Owner : " + this.players[this.plan.getPlayer().getPlayID()];
		if(this.plan.getFuture()!=null) {//Add the "future"-related data //if we say data as a plural, do we say "datum" as a singular ? i'll check. //Talkative. Go to bed. //Check. Data is actually the plural of Datum. Good to know. //I'll go to bed.
			str+="<br><br>Estimated future :<br>";
			double[][] future=this.plan.getFuture();
			for(int k=0;k<future.length;k++) { 
				//future[k][0] gives the number of the round after which state k will be efficient. As a double, it is the last eny attack time on a planet.
				//future[k][1] gives the number of attack ships, future[k][2] gives the number of defense ships, future[k][3] gives the defensive power of this planet.
				//future[k][4] gives the owner id at this state
				String temp=String.valueOf(future[k][0]);
				if(temp.length()>=4) {temp=temp.substring(0,4)+" ";} //We do not want a long number. Plus they are already sorted by real arrival time. So doesn't really matter
				str+=" turn : " + temp + " A-Ships : " + String.valueOf((int)future[k][1]) + " D-Ships : " + String.valueOf((int)future[k][2]) + " Owner : " + this.players[(int) future[k][4]] + "<br>";
			}	
		}
		if(this.plan.getThreats().getSize()>0) {
			str+="<br><br>Estimated incoming fleets :<br>";
			List threats=this.plan.getThreats();
			for(ListNode ln=threats.getFirst();ln!=null;ln=ln.getNext()) { 
				Flotte flt=(Flotte)ln.getObj();
				String turn=String.valueOf(flt.getArrival(this.plan.getIdPlan()));
				if(turn.length()>=4) {turn=turn.substring(0,4);} //We do not want a long number. Plus they are already sorted by real arrival time. So doesn't really matter
				String power=String.valueOf(flt.getPower());
				if(power.length()>=4) {power=power.substring(0,4);}
				str+="On turn : " + turn + " with a power of : " + String.valueOf(power) + " from " + String.valueOf(players[flt.getPlayOrig()]) + "<br>";
			}
			
		} else {
			str+="<br>No threats on this planet";
		}
		String temp=String.valueOf(plan.getImportance());
		if(temp.length()>=4) {temp=temp.substring(0,4);}
		str+="<br><br> Strategic rentability of conquering this planet " + temp; 
		str+="</html>";
		return str;
	}

	public String FleetStatus() {
		String str="<html>A-Ships : " + this.fleet.getShips()[0] +"<br>D-Ships : " + this.fleet.getShips()[1] +"<br>With coef : " + this.fleet.getCoef() +"<br>Total Power : " + this.fleet.getPower() + "<br>Owner : " + this.players[this.fleet.getPlayOrig()];
		str+="<br><br>Computed Probabilities to hit :<br>";
		for(int k=1;k<fleet.getPlanetes().length;k++) {
			String temp=String.valueOf(fleet.getProba()[k]);
			if(temp.length()>=5) {temp=temp.substring(0,5);}
			str+=fleet.getPlanetes()[k].getName() + " : " + temp+"%";
			if(fleet.getThreated()[k]) {
				//TODO : if most probable
				str+=" -> Highly Probable";
			}
 			str+="<br>";
		}
		return str;
	}
	
	
	@Override public void update(Planete plan) {
		System.out.println(plan + " selected");
		this.plan=plan;
		this.name.setText(plan.getName());
		this.fleet=null;
		this.repaint();
	}

	@Override public void update(Flotte fleet) {
		this.plan=null;
		this.name.setText("Flotte");
		this.fleet=fleet;
		this.repaint();
	}

	@Override public void follow(int playerID) {this.PlayerID=playerID; this.repaint();}
	@Override public void add(int number) {}
	@Override public void CreatingFleet(boolean selected, Planete plan) {}
	@Override public void nextTurn() {}
	@Override public void players(Player[] players) {this.players=players;}
}

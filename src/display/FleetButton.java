package display;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import classic.Flotte;

public class FleetButton extends JButton {

	private static final long serialVersionUID = 1817575387168372311L;
	private Flotte fleet;

	public FleetButton(Flotte fleet) {
		this.fleet=fleet;
		setBorderPainted(false);
		setContentAreaFilled(false);
		setVisible(true);
		setBackground(Color.green);
		//Definition of the image
		String image = "A"; //first letter of the ships image
		double y=fleet.mostProbableDest()[1]-fleet.getPlanOrig().getPosy();
		double x=fleet.mostProbableDest()[0]-fleet.getPlanOrig().getPosx();
		//System.out.println("DeltaY=" + y + " DeltaX=" +x);
		if(x==0) {//Rare but possible, meaning fully vertical movement
			//System.out.println("rare case");
			if(y>0) {
				image+="-90";
			} else { //here is hidden the stupid case (in which you send a fleet from a planet to the same one).
				image+="90";
			}
		} else {
			double co=Math.acos(x/Math.sqrt((y*y+x*x)));
			if(y<0) { //acos gives an answer in [0;pi]
				co=-co;
			}
			co=180*co/Math.PI;
			//System.out.println("Actual Angle : " + co);
			int val=0;
			if(co<-175) { //Stupid case
				image+="180";
			} else {
				for(int k=-150;k<=180;k+=30) {
					if(Math.abs(co-k)<=15) {
						val=-k; //Because the y-axis of swing is reverted
						break;
					}
				}
				image+=String.valueOf(val);
				//System.out.println("Noted Value= " + val);
			}
		}
		image+=".png";
		//System.out.println("Image :" + image);
		setIcon(new ImageIcon(image));
	}

	public Flotte getFleet() {
		return this.fleet;
	}
}

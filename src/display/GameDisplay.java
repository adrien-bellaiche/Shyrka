package display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import node.List;
import node.ListNode;
import node.Observer;
import classic.Client;
import classic.Cluster;
import classic.Flotte;
import classic.Planete;
import classic.Player;

public class GameDisplay extends JPanel implements ActionListener,Observer {
	
	private static final long serialVersionUID = 5351524803259659315L;
	private double[][] mapSize={{0,15},{0,15}};
	private double[] mapLength={15,15};
	private ArrayList<PlanetButton> pbuttons;
	private ArrayList<FleetButton> fbuttons;
	
	public GameDisplay() {
		
		this.setLayout(null);
		this.pbuttons=new ArrayList<PlanetButton>();
		this.fbuttons=new ArrayList<FleetButton>();
		Observer.observers.add(this);
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		Image backgnd;
		try {
			backgnd = ImageIO.read(new File("deepspace.png"));
			g.drawImage(backgnd, 0, 0, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(fbuttons.size()>0) {
			g.setColor(Color.red);
			for (FleetButton fb: fbuttons) {
				Flotte fleet=fb.getFleet();
				double[] orig=fleet.getPlanOrig().getPos();
				double[] dest=fleet.mostProbableDest();
				double[] pos=new double[2];
				double arriTime=fleet.getArrival(fleet.mostProbablePlan().getIdPlan());
				pos[0]=orig[0] + (dest[0]-orig[0])*(Client.round-fleet.getLaunchRound())/(arriTime-fleet.getLaunchRound());
				pos[1]=orig[1] + (dest[1]-orig[1])*(Client.round-fleet.getLaunchRound())/(arriTime-fleet.getLaunchRound());
				g.drawLine(toWidth(orig[0])+15, toHeight(orig[1])+15, toWidth(dest[0])+15, toHeight(dest[1])+15); //from the origin planet to the destination planet, find the good 
				fb.setBounds(toWidth(pos[0])+10,toHeight(pos[1])+10,11,11); //Ajouter les bonnes tailles
				fb.repaint();
			}
		}
	}

	public void setClusters(Cluster[] clusters) {
		for(PlanetButton pb : pbuttons) {
			this.remove(pb);
		}
		for (int k=0;k<clusters.length;k++) {
			Planete plan[] = clusters[k].getPlanetes();
			for (Planete planete : plan) {
				PlanetButton jb=new PlanetButton(planete);
				jb.setBounds(toWidth(planete.getPosx()), toHeight(planete.getPosy()), 30, 30);
				this.add(jb);
				jb.addActionListener(this);
				this.pbuttons.add(jb);
				jb.setVisible(true);
				
			}
		}
		this.repaint();
	}
	public void setFleets(List flottes) {
		for(FleetButton fb : fbuttons) {
			this.remove(fb);
		}
		for (ListNode lnf=flottes.getFirst();lnf!=null;lnf=lnf.getNext()) {
			Flotte fleet=(Flotte)lnf.getObj();
			FleetButton fb=new FleetButton(fleet);
			this.add(fb);
			fb.addActionListener(this);
			this.fbuttons.add(fb);
		}
		this.repaint();
	}

	public int toWidth(double db) {
		return (int)((db-this.mapSize[0][0])*(this.getWidth()-60)/mapLength[0]) + 10;
	}

	public int toHeight(double db) {
		return (int)((db-this.mapSize[1][0])*(this.getHeight()-60)/mapLength[1]) + 10;
	}

	public void setMapSize(double[][] mapSize) {
		this.mapSize=mapSize;
		this.mapLength[0]=this.mapSize[0][1]-this.mapSize[0][0];
		this.mapLength[1]=this.mapSize[1][1]-this.mapSize[1][0];
		this.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		for(PlanetButton pb : pbuttons ) {
			if(arg0.getSource() == pb) {
				pb.setContentAreaFilled(true);
				System.out.println("Selection : planet " + pb.getPlanet().getName() + " " + pb.getPlanet().getIdPlan());
				for(Observer obs : Observer.observers){
					obs.update(pb.getPlanet());
				}
			} else {
				pb.setContentAreaFilled(false);
			}
		}
		for(FleetButton fb : fbuttons) {
			if(arg0.getSource() == fb) {
				fb.setContentAreaFilled(true);
				System.out.println("Selection : Fleet " + fb.getFleet().shorten());
				for(Observer obs : Observer.observers){
					obs.update(fb.getFleet());
				}
			} else {
				fb.setContentAreaFilled(false);
			}
		}

	}

	@Override public void update(Planete plan) {}
	@Override public void update(Flotte fleet) {}
	@Override public void follow(int playerID) {}
	@Override public void add(int number) {}
	@Override public void CreatingFleet(boolean selected, Planete plan) {}
	@Override public void nextTurn() {}
	@Override public void players(Player[] players) {};
	
}

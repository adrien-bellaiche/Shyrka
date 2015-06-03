package display;
import javax.swing.JFrame;

import node.List;
import classic.Cluster;

public class GameWindow extends JFrame {

	private static final long serialVersionUID = -4728231584438328421L;
	private GameDisplay planDisp=new GameDisplay();
	private Contextual context=new Contextual();
	private ButtonBar menu=new ButtonBar();

	public GameWindow() {
		this.setLayout(null);
		this.setTitle("Affichage Planetes");
		this.setSize(1000, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		menu.setBounds(0, 0, 1000, 30);
		planDisp.setBounds(0, 30, 600, 570);
		context.setBounds(600, 30, 400, 570);
		this.add(menu);
		this.add(context);
		this.add(planDisp);
		planDisp.repaint();
		context.repaint();
		menu.repaint();
		this.setResizable(false);
		this.setVisible(true);
	}

	public void setClusters(Cluster[] cluster) {
		this.planDisp.setClusters(cluster);
		System.out.println("number of clusters : " + cluster.length);
	}

	public void setFleets(List flottes) {
		this.planDisp.setFleets(flottes);
		System.out.println("number of fleets : " + flottes.getSize());
	}

	public void setMapSize(double[][] mapSize) {
		this.planDisp.setMapSize(mapSize);
	}

}
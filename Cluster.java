package classic;

import java.awt.Color;
import node.List;
import node.ListNode;

public class Cluster {

	private Planete[] planetes;
	private List<Planete> tempPlanete;
	private boolean compacted=false;
	private final static Color[] colors={Color.gray,Color.black,Color.blue,Color.cyan,Color.green,Color.magenta,Color.yellow,Color.pink,new Color(255,0,255,0),new Color(255,125,125,0)};
	private static int colored=0;
	private final static String[] names={"Alpha", "Beta", "Gamma","Koprulu","ZZ9 Plural Z Alpha", "QQ7 Active J Gamma", "Outer Eastern Rim", "Malastare", "Brentaal", "Helska", "Kashyyk" }; //list of names of clusters
	private static int named=0;
	private String name;
	private Color color;
	
	public Cluster() {
		this.tempPlanete=new List<>();
		this.planetes=null;
		if(named<names.length) {
			this.name=names[named];
			Cluster.named++;
		}
		else {
			this.name=String.valueOf(named);
			Cluster.named++;
		}
		if(colored<colors.length) {
			this.color=colors[colored];
			Cluster.colored++;
		}
		else {
			this.color=colors[colored-1];
		}
		
	}
	
	public void compactCluster() {//is used to save memory through turning Lists into arrays
		if(!compacted) {
			this.planetes=Client.toTabPlan(this.tempPlanete);
			tempPlanete.removeAll();
			tempPlanete=null;
			compacted=true;
		}
	}
	
	public void addPlanet(Planete plan) {
		if(!compacted) {
			this.tempPlanete.add(plan);
		}
	}
	
	public Planete[] getPlanetes() {
		if(compacted) {return this.planetes;}
		else {return null;}
	}
	
	public Color getColor() {return this.color;}
	public String getName() {return this.name;}
	public List getTempPlanete() {return this.tempPlanete;}
	
	public static void main(String[] args) {
		ExternalPlayer play=new ExternalPlayer("Bob", 1);
		ExternalPlayer play2=new ExternalPlayer("Max", 2);
		System.out.println("2 Players Created");
		Planete planete=new Planete(1, play, 3, 4, 0.7d, 8, 5, 7);
		Planete planete2=new Planete(2, play2, 12, 13, 0.4d, 4, 1, 5);
		Planete planete3=new Planete(1, play, 4, 5, 0.8d, 4, 2, 6);
		System.out.println("3 Planets Created");
		Planete[] univ = new Planete[4];
		List planList=new List();
		List clusters=new List();
		univ[1]=planete;
		planList.add(planete);
		univ[2]=planete2;
		planList.add(planete2);
		univ[3]=planete3;
		planList.add(planete3);
		Planete tampPlan=null;
		boolean placed=false;
		for(ListNode tamp=planList.getFirst();tamp!=null;tamp=tamp.getNext()) { //for each planet in the universe
			tampPlan=(Planete)tamp.getObj(); // look at this planet
			if(!tampPlan.clustered()) {//if not already clustered //is just another failsafe. Normally useless.
				placed=false;
				Planete rapatafla=null; //temporary variable
				Cluster clust=null; //Another one
				for(ListNode rantanplan=clusters.getFirst();rantanplan!=null && !placed;rantanplan=rantanplan.getNext()){ //look at all the clusters
					clust=(Cluster)rantanplan.getObj(); //take the cluster
					for(ListNode planetNode=clust.tempPlanete.getFirst(); planetNode!=null && !placed;planetNode=planetNode.getNext()) {
						rapatafla=(Planete) planetNode.getObj();
							if(Client.distance(rapatafla.getPos(), tampPlan.getPos())<=2) { //and if close enough to one planet of the cluster
								clust.addPlanet(tampPlan); //add it to the cluster 
								tampPlan.cluster(); //note it as placed in a cluster
								placed=true; //and declare it placed
							}
					}
				}
				if(!placed) { //if not close enough to any existing cluster
					clusters.add(new Cluster()); //create a new one
					clust= (Cluster) clusters.getLast().getObj();
					clust.addPlanet(tampPlan); //and add it to it.
				}
			}
		} // Clusters are now defined. They will be "compacted" (the lists are now getting turned into definitive arrays)
		Cluster clust=null;
		for(ListNode tamp=clusters.getFirst(); tamp!=null;tamp=tamp.getNext()) {
			clust=(Cluster) tamp.getObj();
			clust.compactCluster();
		}
		//cluster=Client.toTabCluster(clusters);
		//clusters=null;
		System.out.println("Done");
	}
	
}

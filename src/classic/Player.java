package classic;
import node.List;
import node.ListNode;

public abstract class Player {

	/**Class Player
	 * Fully functionnal
	 */
	
	protected List<Planete> planets;
	protected String name;
	int playerID;
	
	public Player(String name, int playID) {
		this.name=name;
		this.playerID=playID;
		this.planets=new List<>();
	}
	@Override public String toString() {return this.name;}
	public int getPlayID() {return this.playerID;}
	public void addPlanet(Planete planet) {this.planets.add(planet);}
	public void removePlanet(Planete planet) {this.planets.removeObj(planet);}
	
	public Planete[] getPlanets() {
		Planete obj[]=new Planete[this.planets.getSize()+1];
		ListNode tamp=planets.getFirst();
		for(int k=0; tamp!=null; k++) {
			obj[k]=(Planete) tamp.getObj();
			tamp=tamp.getNext();
		}
		return obj;
	}
	
	public static void main(String args[]) {
		Player bob=new ExternalPlayer("Bob", 1);
		Player max=new ExternalPlayer("Max", 2);
		Planete planete=new Planete(1, max, 3, 4, 0.7d, 8, 5, 7);
		Planete planete2=new Planete(2, bob, 4, 5, 0.4d, 4, 1, 5);
		Planete planete3=new Planete(3, max, 4, 6, 0.8d, 4, 2, 6);
		planete.getCoef(); //Just there to get rid of a useless warning
		planete3.getCoef(); //same here
		System.out.println("3 planets & 2 player created");
		String planetS="";
		for(int k=0;k<max.getPlanets().length;k++) {
			planetS+=max.getPlanets()[k]+" ";
		}
		System.out.println("getPlanets (must be planete1 planete3) \n" + planetS);
		planete2.setPlayer(max);
		System.out.println("planete 2 given to player max");
		planetS="";
		for(int k=0;k<max.getPlanets().length;k++) {
			planetS+=max.getPlanets()[k]+" ";
		}
		System.out.println("test de getPlanets (must be planet1 planet3 planet2) \n" + planetS);
	}

}
package display;
import node.List;
import node.ListNode;
import node.Observer;
import classic.Client;
import classic.Cluster;
import classic.ExternalPlayer;
import classic.Flotte;
import classic.IA;
import classic.NeutralPlayer;
import classic.Planete;
import classic.Player;

public class GraphicTest {

	public static void main(String[] args){ //This class is here to test the graphic components
		Player[] players=new Player[3];
		
		Player play=new ExternalPlayer("Max", 1);
		Player play2=new IA(2);
		NeutralPlayer neu=new NeutralPlayer();
		players[0]=neu;
		players[1]=play;
		players[2]=play2;
		Cluster clusters[]=new Cluster[4];

		//First Cluster
		Planete planete1=new Planete(1, play, 10, 14, 0.5 , 10 , 6, 7);
		Planete planete2=new Planete(2, play2, 3, 1, 0.5 , 10 , 5, 8);
		Planete planete3=new Planete(3, neu, 3, 3, 0.5 , 8 , 4, 9);
		clusters[0]=new Cluster();
		clusters[0].addPlanet(planete1);
		clusters[0].addPlanet(planete2);
		clusters[0].addPlanet(planete3);
		clusters[0].compactCluster();

		//Second Cluster
		Planete planete4=new Planete(4, neu, 7, 8, 0.5 , 16 , 3, 4);
		Planete planete5=new Planete(5, neu, 4, 9, 0.5 , 3 , 0, 6);
		clusters[1]=new Cluster();
		clusters[1].addPlanet(planete4);
		clusters[1].addPlanet(planete5);
		clusters[1].compactCluster();

		//Third Cluster
		Planete planete6=new Planete(6, neu, 2, 11, 0.5 , 12 , 3, 9);
		Planete planete7=new Planete(7, play, 7, 2, 0.5 , 11 , 12, 2);
		Planete planete8=new Planete(8, play2, 13, 3, 0.5 , 7 , 6, 4);
		Planete planete9=new Planete(9, play, 2, 4, 0.5 , 6 , 25, 1);
		clusters[2]=new Cluster();
		clusters[2].addPlanet(planete6);
		clusters[2].addPlanet(planete7);
		clusters[2].addPlanet(planete8);
		clusters[2].addPlanet(planete9);
		clusters[2].compactCluster();

		//Fourth Cluster
		Planete planete10=new Planete(10, play2, 10, 11, 0.5 , 10 , 5, 5);
		Planete planete11=new Planete(11, play, 10, 9, 0.5 , 10 , 2, 8);
		Planete planete12=new Planete(12, play2, 11, 10, 0.5 , 10 , 12, 8);
		clusters[3]=new Cluster();
		clusters[3].addPlanet(planete10);
		clusters[3].addPlanet(planete11);
		clusters[3].addPlanet(planete12);
		clusters[3].compactCluster();

		Planete[] univ = new Planete[13];
		univ[1]=planete1;
		univ[2]=planete2;
		univ[3]=planete3;
		univ[4]=planete4;
		univ[5]=planete5;
		univ[6]=planete6;
		univ[7]=planete7;
		univ[8]=planete8;
		univ[9]=planete9;
		univ[10]=planete10;
		univ[11]=planete11;
		univ[12]=planete12;
		Client.planets=univ;
		Client.iaID=2;
		List fleets=new List();
		Flotte squad1=new Flotte(10, 2, planete1, planete1.getPlayer().getPlayID(),1, 12);
		Flotte squad2=new Flotte(0, 10, planete2, planete2.getPlayer().getPlayID(),1, 12);
		Flotte squad3=new Flotte(8, 2, planete7, planete7.getPlayer().getPlayID(),1, 12);
		Flotte squad4=new Flotte(3, 5, planete8, planete8.getPlayer().getPlayID(),1, 12);
		Flotte squad5=new Flotte(5, 2, planete10, planete10.getPlayer().getPlayID(),1, 12);
		Flotte squad6=new Flotte(2, 10, planete11, planete11.getPlayer().getPlayID(),1, 12);
		Flotte squad7=new Flotte(9, 2, planete12, planete12.getPlayer().getPlayID(),1, 12);
		Flotte squad8=new Flotte(4, 5, planete9, planete9.getPlayer().getPlayID(),1, 12);
		Flotte squad9=new Flotte(4, 5, planete9, planete9.getPlayer().getPlayID(),2, 12);
		fleets.add(squad1);
		fleets.add(squad2);
		fleets.add(squad3);
		fleets.add(squad4);
		fleets.add(squad5);
		fleets.add(squad6);
		fleets.add(squad7);
		fleets.add(squad8);
		fleets.add(squad9);
		Client.round=3;
		for(ListNode fltn=fleets.getFirst();fltn!=null;fltn=fltn.getNext()) {
			Flotte flt=(Flotte)fltn.getObj();
			flt.refresh(univ,Client.round);
		}
		for(Cluster clu : clusters) {
			for(Planete p : clu.getPlanetes()) {
				p.computeFuture();
				p.getAvailable();
			}
		}
		GameWindow display = new GameWindow();
		Client.iaID=2;
		for (Observer obs : Observer.observers) {
			obs.players(players);
		}
		display.setClusters(clusters);
		display.setFleets(fleets);
		display.repaint();
	}
}
package classic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import node.List;
import node.ListNode;
import node.Observer;
import display.GameWindow;

public class Client implements Observer {


	private Socket mySocket;/** socket to communicate with the server */
	private PrintStream writer;/** stream used to send messages */
	private BufferedReader reader;/** stream used to read messages */
	private Player[] players;
	public static Planete planets[];  /**Caution : the planet which id is n is in planets[n] ; planets[0] corresponds to the direction for squads without known direction*/
	private List<Cluster> clusters;	 //temporary list for computing clusters
	private Cluster[] cluster; //Which is then casted into this array
	private boolean endGame=false; //Obviously.
	private double mapSize[][]=new double[2][2];
	private List<Flotte> flottes; //Ongoing fleets. Is the real dynamic object of the game
	public static int round;
	private IA ia; //local player ID. Is also in the list of players.
	public static int iaID=0;
	public static boolean irrelevantFleets;
	private int shipsAvailable[][];
	private List<Planete> targets;
	private List<Planete> agressors;
	public int nbFlottes=0;
	private boolean victory=false;
	private boolean waitingForNextTurn=true;
	
	public Client(String serverName, int serverPort) {
		try {
			mySocket = new Socket(serverName, serverPort);// ouvre la socket
			writer = new PrintStream(mySocket.getOutputStream());// initialise les flots en lecture et ecriture
			reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		} catch (IOException e) {e.printStackTrace();}
		Client.round=0;
		this.flottes=new List<>();
		this.clusters=new List<>();
		this.targets=new List<>();
		//this.waitingForNextTurn=false;
		this.agressors=new List<>();
		Observer.observers.add(this);
	}

	public void sendMessage(String message) {
		writer.println(message);
		System.out.println("Sent to server : " + message);
	}

	//Classic Setters and Getters
	public IA getIA() {return this.ia;}
	public Socket getMySocket() {return mySocket;}
	public PrintStream getWriter() {return writer;}
	public void setWriter(PrintStream writer) {this.writer = writer;}
	public BufferedReader getReader() {return reader;}
	public void setReader(BufferedReader reader) {this.reader = reader;}
	public void setMySocket(Socket mySocket) {this.mySocket = mySocket;}
	public Player[] getPlayers() {return players;}
	public Planete[] getPlanets() {return planets;}
	public boolean isEndGame() {return endGame;}
	public void setEndGame(boolean endGame) {this.endGame = endGame;}
	public double[][] getMapSize() {return mapSize;}
	public void setMapSize(double[][] mapSize) {this.mapSize = mapSize;}
	public List getFlottes() {return flottes;}
	public void setFlottes(List flottes) {this.flottes = flottes;}
	public static double distance(double orig[], double dest[]) {return Math.sqrt(Math.pow((orig[1]-dest[1]),2)+Math.pow((orig[0]-dest[0]),2));}

	public String getMessage() {
		String res = null;
		System.out.print("Reading the socket : ");
		try {
			res = reader.readLine();
		} catch (IOException e) {e.printStackTrace();}
		System.out.println("Received : " + res);
		return res;
	}

	public void handleMessage() {//This is not really clean. I'll do better if i have time for it.
		String str=getMessage();
		if (str != null && str.length()>0) {
			StringTokenizer token = new StringTokenizer(str);
			String ordre = token.nextToken();
			if (ordre.equalsIgnoreCase("modifications")) {
				ordre=getMessage();
				token=new StringTokenizer(ordre);
				while(!ordre.equalsIgnoreCase("fin_modifications")){ //get the modifications
					String type=token.nextToken(); 
					if (type.equalsIgnoreCase("depart")) {
						modDepart(token);
						if(nbFlottes!=this.flottes.getSize()){
							System.out.println("Issue with Fleet treatment");
						}
					} else if (type.equalsIgnoreCase("renfort")) {
						nbFlottes--;
						modRenfort(token);
					} else if (type.equalsIgnoreCase("attaque")) {
						nbFlottes--;
						modAttaque(token);
						if(nbFlottes!=this.flottes.getSize()){
							System.out.println("Issue with Fleet treatment");
						}
					} else if (type.equalsIgnoreCase("production")) {
						modProduction(token);
					}
					ordre=getMessage();
					token=new StringTokenizer(ordre);
				}
			}
			else if (ordre.equalsIgnoreCase("id")) { //For initialisation
				initialisation(token);
			}
			else if (ordre.equalsIgnoreCase("gagne") || ordre.equalsIgnoreCase("perdu")) { //for the end of game.
				this.endGame=true;
				this.victory=ordre.equalsIgnoreCase("gagne");
			}
		}
	}

	private void modProduction(StringTokenizer token) {
		int planeteID=Integer.parseInt(token.nextToken());
		int nbVA=Integer.parseInt(token.nextToken());
		int nbVD=Integer.parseInt(token.nextToken());
		Client.planets[planeteID].production(nbVA, nbVD);
		/*
		 * un message de production sera :
le message PRODUCTION
un identifiant de planï¿½te
un nombre de vaisseaux d'attaque produits
un nombre de vaisseaux de dï¿½fense produits
		 */
	}

	private void modAttaque(StringTokenizer token) {
		int attackPlayerID=Integer.parseInt(token.nextToken());
		int planeteOrigID=Integer.parseInt(token.nextToken());
		int planeteID=Integer.parseInt(token.nextToken());
		int OwnerID=Integer.parseInt(token.nextToken());
		int nbVA=Integer.parseInt(token.nextToken());
		int nbVD=Integer.parseInt(token.nextToken());
		//these two cases are only separated because the calculation of power is not the same if the planet got conquered
		if(Client.planets[planeteID].getPlayer().getPlayID()!=OwnerID) { //case if got conquered
			removeClosestFleet(attackPlayerID, planeteID, 2*nbVA+nbVD, planeteOrigID); //Explanations for this in the method body
		}
		else { //case if not conquered
			double fltpower=0;
			if(Client.planets[planeteID].getNbVA()==0) {
				fltpower=Client.planets[planeteID].getPower()*(1-nbVD/Client.planets[planeteID].getNbVD());	
			} else if(Client.planets[planeteID].getNbVD()==0) {
				fltpower=Client.planets[planeteID].getPower()*(1-nbVA/Client.planets[planeteID].getNbVA());
			} else {
				fltpower=Client.planets[planeteID].getPower()*(1-0.5*nbVA/Client.planets[planeteID].getNbVA()-0.5*nbVD/Client.planets[planeteID].getNbVD());
			}
			//juste pour qu'on voit la formule.
			removeClosestFleetNotConquered(attackPlayerID, planeteID, fltpower, planeteOrigID); //Explanations for this in the method body
		}
		Client.planets[planeteID].attaque(nbVA, nbVD, this.players[OwnerID]);
		/*
		 * le message ATTAQUE
un identifiant de joueur attaquant
un identifiant de planï¿½te attaquï¿½e
l'identifiant du nouveau propriï¿½taire de la planï¿½te
un nombre de vaisseaux d'attaque restant
un nombre de vaisseaux de dï¿½fense restant
		 */
	}

	public void removeClosestFleet(int APlayID, int planeteID, int shipspower, int planeteOrigID) { //case planet conquered
		Flotte closestOne=null;
		double ecart=320000;
		for(ListNode node=this.flottes.getFirst();node!=null;node=node.getNext()) {
			Flotte flt=(Flotte)node.getObj();
			if(flt.getPlayOrig()==APlayID && Math.floor(flt.getArrival(planeteID))==Client.round && flt.getPlanOrig().getIdPlan()==planeteOrigID) {
				double delta=Math.abs(shipspower*flt.getCoef()-flt.getPower()+Client.planets[planeteID].getPower())/Math.sqrt(shipspower*shipspower+1);
				//Un peu de calcul formel montre que l'on ne peut pas (connaissant seulement les vaisseaux restants) remonter au nombre de vaisseaux précis de la flotte d'origine (inconnues : nbvaisseaux_flotte avant attaque (2 inconnues), et son coef attk/def, et nous n'avons que deux équations, celles du nombre de vaisseaux survivants)
				//J'ai donc choisi comme critère de ma recherche la distance de chaque flotte (ou plutot du point (coef,puissance_flotte_avant_attaque) associé) à la droite d'équation (2*nbvaRestant+nbvdRestant)*x-y+puissance_planète_avant_attaque=0
				if(delta<ecart) {
					closestOne=flt;
					ecart=delta;
				}
			}
		}
		if(closestOne!=null) {//Failsafe. Failsafe everywhere.
			closestOne.removeThreats();
			this.flottes.removeObj(closestOne);
		} else {
			System.out.println("Fleet not found ");
		}
	}


	public void removeClosestFleetNotConquered(int APlayID, int planeteID, double fltpower, int planeteOrigID) { //Case planet not conquered
		//Here we know the power of the attacking fleet (almost actually, because of the math.ceil(survivingShips)). So we don't need to compute anything.
		List flts=this.flottes;
		Flotte closestOne=null;
		double ecart=320000;
		for(ListNode node=flts.getFirst();node!=null;node=node.getNext()) {
			Flotte flt=(Flotte)node.getObj();
			if(Math.floor(flt.getArrival(planeteID))==Client.round && flt.getPlanOrig().getIdPlan()==planeteOrigID) {
				double delta=Math.abs(flt.getPower()-fltpower);
				if(delta<ecart) {
					closestOne=flt;
					ecart=delta;
				}
			}
		}
		if(closestOne!=null) {//Failsafes. Failsafes everywhere.
			closestOne.removeThreats();
			this.flottes.removeObj(closestOne);
		} else {
			System.out.println("Null object");
		}
	}



	private void modRenfort(StringTokenizer token) {
		int planeteID=Integer.parseInt(token.nextToken());
		int nbVA=Integer.parseInt(token.nextToken());
		int nbVD=Integer.parseInt(token.nextToken());
		Client.planets[planeteID].renfort(nbVA, nbVD);
		findRemoveFlotte(nbVA, nbVD, planeteID);
	}

	private void modDepart(StringTokenizer token) {
		int planeteID=Integer.parseInt(token.nextToken());
		int nbVA=Integer.parseInt(token.nextToken());
		int nbVD=Integer.parseInt(token.nextToken());
		Client.planets[planeteID].renfort(-nbVA, -nbVD);
		if(Client.planets[planeteID].getPlayer().getPlayID()!=this.ia.getPlayID()) { //Not sure about this.
			this.flottes.add(new Flotte(nbVA, nbVD, Client.planets[planeteID], Client.planets[planeteID].getPlayer().getPlayID(), Client.round, planets.length));
			this.flottes.getLast().getObj().refresh(planets, Client.round); //At creation, we "update" the fleet already
		} else {
			this.flottes.add(new Flotte(nbVA,nbVD, Client.planets[planeteID], Client.planets[planeteID].getPlayer().getPlayID(), Client.round, Client.planets.length, VirtualFleet.compare(planeteID, nbVA, nbVD))); //Send a fleet with known destination
			this.flottes.getLast().getObj().refresh(Client.planets, Client.round);
		}
		this.nbFlottes++;
		/*
		 * le message DEPART
un identifiant de planï¿½te de dï¿½part
un nombre de vaisseaux d'attaque envoyï¿½s
un nombre de vaisseaux de dï¿½fense envoyï¿½s
		 */
	}

	private void initialisation(StringTokenizer token) {
		int locPlayID = Integer.parseInt(token.nextToken());
		int nbPlayers=Integer.parseInt(getMessage())+1;
		this.players=new Player[nbPlayers+1];
		this.players[0]=new NeutralPlayer();
		StringTokenizer message;
		for(int k=1; k<=nbPlayers; k++) { //creating players
			message= new StringTokenizer(getMessage());
			int playID=Integer.parseInt(message.nextToken());
			String str= message.nextToken();
			if (locPlayID!=playID) {
				this.players[k]=new ExternalPlayer(str,playID);
			}
			else {
				this.ia=new IA(playID);
				this.players[k]=this.ia;
				Client.iaID=k;
			}
		}
		message= new StringTokenizer(getMessage());
		Client.planets=new Planete[Integer.parseInt(message.nextToken())+1];
		this.shipsAvailable=new int[Client.planets.length][1];
		double minX=15;double maxX=0;
		double minY=15;double maxY=0;
		List<Planete> planList=new List<>(); //is used to generate clusters for the IA
		for(int k=1;k<Client.planets.length;k++) { //creating planets

			message=new StringTokenizer(getMessage());
			int planID=Integer.parseInt(message.nextToken());
			double planPosX=Double.parseDouble(message.nextToken());
			double planPosY=Double.parseDouble(message.nextToken());
			int OwnerID=Integer.parseInt(message.nextToken());
			double coef=Double.parseDouble(message.nextToken());
			int planProd=Integer.parseInt(message.nextToken());
			int nbVA=Integer.parseInt(message.nextToken());
			int nbVD=Integer.parseInt(message.nextToken());
			Client.planets[k]=new Planete(planID, this.players[OwnerID],planPosX, planPosY, coef, planProd, nbVA, nbVD);
			planList.add(planets[k]);
			//evaluation of the size of the map
			if(minX>planPosX) {minX=planPosX;}
			else if(maxX<planPosX) {maxX=planPosX;}
			if(minY>planPosY) {minY=planPosY;}
			else if(maxY<planPosY) {maxY=planPosY;}
		}
		this.mapSize[0][0]=minX;this.mapSize[0][1]=maxX;
		this.mapSize[1][0]=minY;this.mapSize[1][1]=maxY;
		// identif joueur courant int
		// nombre de joueurs -1
		// liste joueurs : int puis string
		// liste planetes : int id, int posx, int posy, id proprietaire, coeff attack/def, prod par tour, vaisseaux attaque, vaisseau defense	

		//Useless for now.
		//Generation of local clusters (for IA treatment). Is tested in the Cluster class
		//Problème de répartition des planètes. Peut être dans la compaction inclure une possibilité de fusionner les clusters s'ils sont trop proches.
		//Comme les clusters ne sont pas utilisés pour l'instant, ça ne gène pas. Une idée serait de faire "grossir les clusters"
		//Tiens bah je vais le faire maintenant.
		Planete tampPlan; //Ancienne version du code de clustering
		boolean placed;
		for(ListNode tamp=planList.getFirst();tamp!=null;tamp=tamp.getNext()) { //for each planet in the universe
			tampPlan=(Planete)tamp.getObj(); // look at this planet
			if(!tampPlan.clustered()) {//if not already clustered //is just another failsafe. Normally useless.
				placed=false;
				Planete rapatafla; //temporary variable
				Cluster clust; //Another one

				for(ListNode rantanplan=clusters.getFirst();rantanplan!=null && !placed;rantanplan=rantanplan.getNext()){ //look at all the clusters
					clust=(Cluster)rantanplan.getObj(); //take the cluster
					for(ListNode planetNode=clust.getTempPlanete().getFirst(); planetNode!=null && !placed;planetNode=planetNode.getNext()) {
						rapatafla=(Planete) planetNode.getObj();
						if(Client.distance(rapatafla.getPos(), tampPlan.getPos())<=3) { //and if close enough to one planet of the cluster
							clust.addPlanet(tampPlan); //add it to the cluster 
							tampPlan.cluster(); //note it as placed in a cluster
							placed=true; //and declare it placed
						}
					}
				}
				if(!placed) { //if not close enough to any existing cluster
					clusters.add(new Cluster()); //create a new one
					clust= clusters.getLast().getObj();
					clust.addPlanet(tampPlan); //and add it to it.
				}
			}
		} // Clusters are now defined. They will be "compacted" (the lists are now getting turned into definitive arrays)

		/*while(planList.getSize()>0) { //nouvelle version du code de clustering, non debuggée !
			Cluster clu=new Cluster(); //The one we are creating
			clu.addPlanet((Planete)planList.takeFirst()); //remove the first one of the list, and add it to the cluster
			List toRemovePlanets=new List(); // I'm afraid if i modify the list while i'm reading it, it will result in bugs, so i'll treat that later
			for(ListNode ln=planList.getFirst();ln!=null;ln=ln.getNext()) { //Look at all the planets which are still to cluster
				boolean placed=false;
				for(ListNode ltclu=clu.getTempPlanete().getFirst();ltclu!=null && !placed;ltclu=ltclu.getNext()) { //now look at all the planets already in the cluster
					if(Client.distance(((Planete)ltclu.getObj()).getPos(), ((Planete)ln.getObj()).getPos())<2) { //if you are close enough to any of the planets already in the cluster, then i'll add you to it.
						clu.addPlanet((Planete)ln.getObj());
						toRemovePlanets.add((Planete)ln.getObj());
						placed=true;
					}
				}
			}
			//now i've placed all the planets close enough to the first one. I can remove this planets from the list of ones to be clustered
			for(ListNode rl=toRemovePlanets.getFirst();rl!=null;rl=rl.getNext()) {
				planList.removeObj(rl.getObj());
			}
		}*/
		Cluster clust;
		for(ListNode tamp=clusters.getFirst(); tamp!=null;tamp=tamp.getNext()) {
			clust=(Cluster) tamp.getObj();
			clust.compactCluster();
		}
		cluster=Client.toTabCluster(clusters);
		clusters=null;
	}

	/* the following functions are used to remove fleets which arrived, based on the calculation of their time of arrival, and their power
	 * player is always the owner of the fleet
	 * planetDestID is the ID of the planet receiving the fleet.
	 */

	public void findRemoveFlotte(int nbVA, int nbVD, int planeteDestID) {//case when everything is known (case of an allied squad)
		Flotte closestOne=null;
		double delta=320000; //My definition of "high".
		for(ListNode tamp=this.flottes.getFirst(); tamp!=null; tamp=tamp.getNext()) { //find all the matches
			Flotte temp=(Flotte) tamp.getObj();
			double ecart=(temp.getShips()[0]-nbVA)*(temp.getShips()[0]-nbVA)+(temp.getShips()[1]-nbVD)*(temp.getShips()[1]-nbVD)+Math.pow(temp.getArrival(planeteDestID)-Client.round,2);
			if(ecart<delta) { //if it has the right number of ships
				delta=ecart;
				closestOne=temp; //take this fleet as the right one
			}
		}
		//Now that the right fleet has been found, remove it from the fleet list
		if(closestOne!=null) {// if not found, well ... just forget about this one, the matching one will be erased when it won't be arrived anywhere and should have arrived
			this.flottes.removeObj(closestOne);
			closestOne.removeThreats();
		}
		else {//This is just a failsafe, and is useless, but just in case ...
			if(this.flottes.getSize()!=0) {
				System.out.println("Warning : a fleet has not been found : nbVA = " + nbVA + "; nbVD= " + nbVD + "; Destination = " + planeteDestID + "; Arrival Round =" + round);
			}
		}
	}

	public String moveOrder(int planID, int planDestID, int nbVA, int nbVD) {
		if(nbVA<0 || nbVD<0) {
			System.out.println("negative");
		}
		return "MOUVEMENT " + String.valueOf(planID) + " " + String.valueOf(planDestID) + " " + String.valueOf(nbVA) + " " + String.valueOf(nbVD);
		/*Un ordre de mouvement contient :
		 * 	le message MOUVEMENT
		 *	l'identifiant de la planÃ¨te de dÃ©part
		 *	l'identifiant de la planÃ¨te de destination
		 *	le nombre de vaisseaux d'attaque Ã  envoyer
		 *	le nombre de vaisseaux de dÃ©fense Ã  envoyer
		 */
	}

	public String prodOrder(int planID, int nbVAtoProd) {
		if(nbVAtoProd<0) {
			System.out.println("negative");
		}
		return "PRODUCTION" + " "+String.valueOf(planID) + " " + String.valueOf(nbVAtoProd);

		/*un ordre de production contient :
		 *  le message PRODUCTION
		 *  l'identifiant de la planÃ¨te concernÃ©e
		 *  le nombre de vaisseaux d'attaque Ã  produire par tour (le reste de la production sera constituÃ©e de vaisseaux de dÃ©fense)
		 */
	}

	public void production() {
		for(int k=1;k<Client.planets.length;k++) { //For each planet, update its status
			Planete planet=Client.planets[k];
			planet.computeFuture();
			if(planet.getPlayer().getPlayID()==this.ia.getPlayID()) { //If the planet is owned by the ia
				this.shipsAvailable[k]=planet.getAvailable(); //Get its available ships
				//And go for production
				sendMessage(prodOrder(k,planet.getProd()-this.shipsAvailable[k][3]));
				String answer=getMessage();
				if(!answer.equalsIgnoreCase("OK")) {
					int tried=planet.getProd()-this.shipsAvailable[k][3];
					while(!answer.equalsIgnoreCase("OK")) { //Not really possible, but still.
						if(tried<0) {tried++;} //Impossible, but still.
						else {tried--;}
						sendMessage(prodOrder(k,tried));
						answer=getMessage();
					}
				}// End of the production sequence
			} else { //If the planet is not owned by the local ia
				this.shipsAvailable[k][0]=-1; //to define a clear "not allied planet" case
			}
		} 
	}

	public void defense() {
		//Find where are allied planets who need DShips, and give what they need.
		for(int k=1;k<Client.planets.length;k++) { //shipsAvailable : {Dships required, Aships to give away, turn before which DShips are expected, recommended defensive production)
			if(shipsAvailable[k][0]>0 && shipsAvailable[k][2]>Client.round) { //This is a real requirement, the question about iff is solved by the shipsAvailable[k][0]=-1
				int sentDShips=0; //DShips sent to this planet
				for(int l=1;l<Client.planets.length && shipsAvailable[k][0]>sentDShips;l++) {
					//The idea is to give enough DShips. But if we can't we'll give as much as possible. Why ? Because in case a fleet's destination is badly guessed, then other planets might give away some other DShips, and therefore we might save the planet. A lot of "if", yes, but that's an AI.
					//Actually, an VI
					if(k!=l && Client.planets[l].getPlayer().getPlayID()==Client.iaID && Client.planets[l].getLocalNbVD()>0) { //if it is not the same planet && if it is an allied one && there are available DShips
						//Remember there is no possibility for a planet to give away & request DShips at the same time
						if(Client.round + Client.distance(Client.planets[k].getPos(), Client.planets[l].getPos())<this.shipsAvailable[k][2]) {//And if these ships can actually arrive before when they are needed
							//Send them.
							int DShipsToSend=Math.max(0, Math.min(Client.planets[l].getLocalNbVD(), shipsAvailable[k][0]));
							sendMessage(moveOrder(l,k,0,DShipsToSend));
							String answer=getMessage();
							if(!answer.equalsIgnoreCase("OK")) {//For debug, but virtually impossible
								System.out.println("Problem encountered while sending fleet from " + Client.planets[k].getName() + " with " + Client.planets[k].getNbVA() + " AShips " + Client.planets[k].getNbVD() + " DShips, Attempt to send " + DShipsToSend + "DShips");
							} else {
								Client.planets[l].removeLocalNbVD(DShipsToSend);
								sentDShips+=DShipsToSend;
								this.flottes.add(new Flotte(0, DShipsToSend, Client.planets[l], planets[l].getPlayer().getPlayID(), Client.round, Client.planets.length, k)); //Send a fleet with known destination
								this.flottes.getLast().getObj().refresh(Client.planets, Client.round);
							}
						}
					}
				}
			}
		}
	}


	public void attack() {
		targets.removeAll(); //resort the planets.
		//We first select the targets
		for(int k=1;k<Client.planets.length;k++) {
			if(planets[k].getPlayer()!=this.ia) {//We'll only target foe's planet !
				addTargets(planets[k]);
			}
		}
		//Now we select the allied planets able to attack it in the right "arrival window"
		for(ListNode lnp=this.targets.getLast();lnp!=null;lnp=lnp.getPrev()) {
			Planete target=(Planete)lnp.getObj();
			this.agressors.removeAll();
			for(int k=1;k<Client.planets.length;k++) { //Theses conditions might be upgraded
				if(Client.planets[k].getPlayer().getPlayID()==Client.iaID && Client.planets[k].getLocalNbVA()>0 && Client.distance(Client.planets[k].getPos(),target.getPos())>Client.planets[k].getWeakestTime()-Client.round) {//if this planet has some A-Ships left to spare & these ships would arrive between the weakestmoment of the target & the next attack of it
					if(Client.distance(Client.planets[k].getPos(),target.getPos())<Client.planets[k].getWeakestTime()-Client.round+Client.planets[k].getDelai()) {//And if it arrives before the next enemy's attack
						addAgressor(Client.planets[k]); //Then this is a credible aggressor
					}
				}
			}
			//Then write the attack order, and send it
			double pow=target.getWeakestPower();
			for(ListNode lna=this.agressors.getFirst();lna!=null;lna=lna.getNext()) {
				Planete agressor=(Planete)lna.getObj();
				double timeRelatedPower=2*target.getCoef()*target.getProd()*Math.ceil(1+Client.distance(agressor.getPos(), target.getPos()));
				int AShipsToSend=(int) Math.floor(Math.min(agressor.getLocalNbVA(),Math.ceil(5+(timeRelatedPower+pow)/(2*agressor.getCoef()))));
				String order=moveOrder(agressor.getIdPlan(),target.getIdPlan(),AShipsToSend,0);
				System.out.println("Order to send :" + order);
				sendMessage(order);
				String answer=getMessage();
				if(!answer.equalsIgnoreCase("OK")) {//For debug, but virtually impossible
					System.out.println("Problem encountered while sending fleet from " + agressor.getName() + " with " + agressor.getNbVA() + " AShips " + agressor.getNbVD()  + " DShips, Attempt to send " + AShipsToSend + "AShips");
				} else {
					Client.planets[agressor.getIdPlan()].removeLocalNbVA(AShipsToSend);
					pow-=2*AShipsToSend*agressor.getCoef();
					VirtualFleet.newVFleet(agressor.getIdPlan(), AShipsToSend, 0, target.getIdPlan());

				}
			}
		}
	}

	public void addTargets(Planete plan) { //Same code as in the addThreats(Flotte flt), adapted with the fact that now this is sorted by importance. It is still sorted in increasing order, but as it is doublely linked list, it doesn't really matter 
		if(this.targets.isEmpty()) {
			this.targets.add(plan);
		} else {
			int k=1;
			ListNode pla;
			boolean placed=false;
			for(pla=this.targets.getFirst(); !placed && pla!=null;pla=pla.getNext()) {
				if(plan.getImportance()<((Planete)pla.getObj()).getImportance()) {
					this.targets.insert(plan,k);
					placed=true;
				}
				k++;
			}
			if(pla==null && !placed) {
				this.targets.add(plan);
			}
		}
	}

	public void addAgressor(Planete plan) { //Same code as in the addThreats(Flotte flt), adapted with the fact that now this is sorted by importance. It is still sorted in increasing order, but as it is doublely linked list, it doesn't really matter 
		if(this.agressors.isEmpty()) {
			this.agressors.add(plan);
		} else {
			int k=1;
			ListNode pla;
			boolean placed=false;
			for(pla=this.targets.getFirst(); !placed && pla!=null;pla=pla.getNext()) {
				if(plan.getImportance()<((Planete)pla.getObj()).getImportance()) {
					this.targets.insert(plan,k);
					placed=true;
				}
				k++;
			}
			if(pla==null && !placed) {
				this.targets.add(plan);
			}
		}
	}

	public void orders() {
		for(ListNode lnf= this.flottes.getFirst();lnf!=null;lnf=lnf.getNext()) { //update the fleets. The planets update are done in the production
			((Flotte)lnf.getObj()).refresh();
		}
		this.production();
		this.waitingForNextTurn=true;
		while(this.waitingForNextTurn) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
		this.defense();
		Client.round+=1; //This is turn's number
		this.attack();
		this.sendMessage("FIN_TOUR");
	}

	public void connect() {
		sendMessage("Hello Shyrka");
		handleMessage();
	}

	public static Planete[] toTabPlan(List planets) {
		Planete plan[]=new Planete[planets.getSize()];
		ListNode tamp=planets.getFirst();
		for(int k=0; tamp!=null; k++) {
			plan[k]=(Planete) tamp.getObj();
			tamp=tamp.getNext();
		}
		return plan;
	}

	@Override public void update(Planete plan) {}
	@Override public void update(Flotte fleet) {}
	@Override public void follow(int playerID) {}
	@Override public void add(int number) {}
	@Override public void CreatingFleet(boolean selected, Planete plan) {}
	@Override public void nextTurn() {this.waitingForNextTurn=false;}
	@Override public void players(Player[] players) {};

	public static Player[] toTabPlay(List players) {
		Player play[]=new Player[players.getSize()];
		ListNode tamp=players.getFirst();
		for(int k=0; tamp!=null; k++) {
			play[k]=(Player) tamp.getObj();
			tamp=tamp.getNext();
		}
		return play;
	}


	public static Cluster[] toTabCluster(List<Cluster> clusters) {
		Cluster clust[]=new Cluster[clusters.getSize()];
		ListNode<Cluster> tamp=clusters.getFirst();
		for(int k=0; tamp!=null; k++) {
			clust[k]=tamp.getObj();
			tamp=tamp.getNext();
		}
		return clust;
	}


	public static void main(String[] args) {
		Client client = new Client("localhost", 31331); //Client creation
		client.connect(); //Client connection
		GameWindow display = new GameWindow(); //Graphic display initialization
		display.setClusters(client.cluster); //Set up of the graphic display 
		//display.setMapSize(client.getMapSize()); // Not perfect. Mess up the display
		for(Observer obs :Observer.observers) {
			obs.players(client.players);
		}
		Client.round=0; //Not sure it is really useful.
		while(!client.endGame) { //Each round is defined as following :
			client.orders(); //Let the VI do its job
			System.out.println("Orders sent");
			client.handleMessage(); //Read the modifications
			display.setClusters(client.cluster);

			//Remove the irrelevant fleets if some were detected
			if(Client.irrelevantFleets) {
				List<Flotte> notRelevantFleets = new List<>();
				for(ListNode flt=client.flottes.getFirst();flt!=null;flt=flt.getNext()) { //Refresh all the fleets
					Flotte fleet=(Flotte)flt.getObj();
					fleet.refresh();
					if(fleet.isNotRelevant) {notRelevantFleets.add(fleet);}
				}
				if(notRelevantFleets.getSize()>0) {
					for(ListNode flt=notRelevantFleets.getFirst();flt!=null;flt=flt.getNext()) { //Refresh all the fleets
						client.flottes.removeObj((Flotte)flt.getObj());
					}
				}
				Client.irrelevantFleets=false;

			}
			display.setFleets(client.flottes);
		}
	}


}
package classic;
import node.*;

public class Planete {

	private Player player;
	private int idPlan;
	private double posx;
	private double posy;
	private int prod;
	private double coef;
	private int nbVA; //values given by the server
	private int nbVD; //values given by the server
	private int localNbVA; //Ships available at this turn taking into account the actions already done by the player
	private int localNbVD; //Ships available at this turn taking into account the actions already done by the player
	private List<Flotte> threats;
	private boolean clustered=false;
	private String name;
	private final static String[] names={"Sqornshellous Zeta", "Tatooine", "Utapau", "Felucia", "Frogstar World A", "Esflovian", "Damogran", "Hoth", "Hawalius", "Yavin", "Nano", "Lamuela", "Magrathea", "Santraginus", "Gagrakacka", "Ciceronicus 12",  "Frogstar World B","Frogstar World C","Ursa Minor Alpha","Ursa Minor Beta", "Blagulon Kappa", "Eroticon VI", "NowWhat", "Oglaroon", "Santraginus V", "Stug", "Vod", "Zirzla", "Door", "MoreDoor"};
	private static int named=0;
	private String image;
	private Planete realPlanete;
	private final static String[] images={"acid1.png","desert1.png","acid2.png","earth1.png","desert2.png","desert3.png","desert4.png","ice1.png","earth2.png","earth3.png","forest.png","gaia.png","ice2.png","ice3.png","ice4.png","ocean1.png","ocean2.png","ocean3.png","plains1.png","plains2.png","rock1.png","rock2.png","rock3.png","rock4.png"};
	private static int imaged=0;
	private double importance; //Strategic value of the planet within the game // corresponds to (delay after weakestime - weakestTime)*prod), it is the amount of ships virtually constructible before this planet will need to protect itself
	private double[][] future; //future of the planet. Is recomputed at each turn and for every eny planet.
	private double firstLostMoment; //Is the closest moment in the near future when the planet's owner changes.
	private double weakestTime; //Do this really need an explanation ?
	private double weakestPower; //Power at weakestTime
	private double delai;

	public Planete(int idPlan, Player player, double posx, double posy, double coef, int prod, int nbVA, int nbVD) {
		this.idPlan=idPlan;
		this.posx=posx;
		this.posy=posy;
		this.player=player;
		this.player.addPlanet(this);
		this.coef=coef;
		this.prod=prod;
		this.nbVA=nbVA;
		this.nbVD=nbVD;
		this.threats=new List<>();
		if(named<names.length) {this.name=names[named];Planete.named++;}
		else {this.name=names[names.length-1];Planete.named++;}

		if(imaged<images.length) {this.image=images[imaged];Planete.imaged++;}
		else {this.image=images[images.length-1];Planete.imaged++;}
	}

	public Planete(Planete planete) {//Constructor for virtual planets
		this.idPlan=planete.getIdPlan();
		this.player=planete.getPlayer();
		this.posx=planete.getPosx();
		this.posy=planete.getPosy();
		this.coef=planete.getCoef();
		this.prod=planete.getProd();
		this.nbVA=planete.getNbVA();
		this.threats=planete.getThreats();
		this.nbVD=planete.getNbVD();
		this.name=planete.getName();
		this.image=planete.getImage();
		this.realPlanete=planete;
	}

	//Getters ... Getters everywhere.
	public double getWeakestPower() {return this.weakestPower;}
	public double getDelai() {return this.delai;}
	public Planete getRealPlanete() {return this.realPlanete;}
	public double getWeakestTime() {return this.weakestTime;}
	public List<Flotte> getThreats() {return this.threats;}
	public double getPower() {return (this.nbVA+2.0d*this.nbVD)*this.coef;}
	public String getName() {return this.name;}
	public int getIdPlan() {return this.idPlan;}
	public String getImage() {return this.image;}
	public Player getPlayer() {return player;}
	public int getNbVD() {return nbVD;}
	public void removeThreat(Flotte squad) {this.threats.removeObj(squad);}
	public int getNbVA() {return nbVA;}
	public double getImportance() {return this.importance;}
	public double getPosy() {return posy;}
	public int getProd() {return prod;}
	public double getCoef() {return coef;}
	public double getPosx() {return posx;}
	public void cluster() {this.clustered=true;}
	public int getLocalNbVA() {return this.localNbVA;}
	public int getLocalNbVD() {return this.localNbVD;}
	public void removeLocalNbVA(int toRemove) {this.localNbVA-=toRemove;}
	public void removeLocalNbVD(int toRemove) {	this.localNbVD-=toRemove;}
	public boolean clustered() {return this.clustered;}
	public void production(int nbVA, int nbVD) {this.renfort(nbVA, nbVD);}
	public double[][] getFuture() {return this.future;}


	public void addThreat(Flotte squad) {//failing //Not anymore.
		if(this.threats.isEmpty()) {
			this.threats.add(squad);
		} else {
			int k=1;
			ListNode<Flotte> thre;
			boolean placed=false;
			for(thre=this.threats.getFirst(); !placed && thre!=null;thre=thre.getNext()) {
				if(squad.getArrival(this.idPlan)<(thre.getObj()).getArrival(this.idPlan)) {
					this.threats.insert(squad,k);
					placed=true;
				}
				k++;
			}
			if(thre==null && !placed) {
				this.threats.add(squad);
			}
		}
	}


	public void setPlayer(Player player) {
		if(player!=this.player) {
			this.player.removePlanet(this);
			this.player = player;
			this.player.addPlanet(this);
		}
	}

	public double[] getPos() {
		return new double[]{this.posx,this.posy};
	}

	public void renfort(int nbVA, int nbVD) {//Inbound reinforcement numbers
		this.nbVA+=nbVA;
		this.localNbVA=this.nbVA;
		this.nbVD+=nbVD;
		this.localNbVD=this.nbVD;
	}

	public void attaque(int nbVA, int nbVD, Player newOwner) {//numbers of ships left
		this.nbVA=nbVA;
		this.nbVD=nbVD;
		if(!this.player.equals(newOwner)) {
			this.player.planets.removeObj(this);
			this.player=newOwner;
		}
	}

	public int[] getAvailable() {// Is used on ally planets to get the ships it can spare without risking its protection
		//Return {number of DefshipsRequired,number of Ships available to give, round before which they are needed,res[3]} available for the war effort. Will only be used on allied planets (at least at the beginning, to play on a defensive pattern. 
		//res[3] is the minimum recommended defensive production on the planet
		int[] res=new int[4];
		//first, compute how many fleets it should remain able to "endure". This might be changed
		Flotte fleet;
		//Then, estimates how many ships it needs to endure theses fleets
		if(this.threats.isEmpty()) {//if no attack, no reinforcement needed at all, and we can spare all we have
			res[0]=0;
			res[1]=this.nbVA;
			res[2]=-1;
			res[3]=0;
			this.localNbVA=this.nbVA;
			this.localNbVD=this.nbVD; //this means we give away these ships
			//System.out.println("Free of Threaths : Available from planete " + this.getName() + " " + res[0] + " DShips, " + res[1] + " AShips. Recommends the production of " + res[3] + " DShips" );
		} else {//compute the number of def ships needed before each attack to save the planet, and time to build it.
			int lastArrivalTime=(int)Math.ceil((threats.getLast().getObj()).getArrival(this.idPlan)); //time of arrival of the "last fleet"
			int nextArrivalTime;
			int p=1;// Number of defships we'd like to have after all the attacks
			int rebuildTime;
			int shipsBeforeAttack=1;
			int firstFullDefensiveBuildTurn=-1;
			for(ListNode thre=threats.getLast();thre!=null;thre=thre.getPrev()) { //this time, we don't try to find out how many ships we need from the beginning, but how many we need to save at least one ship at the very end of the assaults
				fleet=(Flotte)thre.getObj();
				if(fleet.getPlayOrig()==this.getPlayer().getPlayID()) {
					shipsBeforeAttack=p-fleet.getShips()[1];
				} else {
					shipsBeforeAttack=(int) Math.ceil((2*p*this.coef + fleet.getPower())/(2*this.coef));
				}
				nextArrivalTime=(int)fleet.getArrival(this.idPlan);
				rebuildTime=lastArrivalTime-nextArrivalTime;
				if(shipsBeforeAttack-rebuildTime*this.prod>=1) {
					//System.out.println("Will fully build on turn " + lastArrivalTime); //That's just to see the world turning round
					firstFullDefensiveBuildTurn=lastArrivalTime-1;
				}
				p=Math.max(1, shipsBeforeAttack-rebuildTime*this.prod);
				lastArrivalTime=nextArrivalTime;
			}
			nextArrivalTime=Client.round;
			rebuildTime=lastArrivalTime-nextArrivalTime;
			res[0]=Math.max(shipsBeforeAttack-rebuildTime*this.prod-this.nbVD,0); //That's the number of ships this planet need now.
			if(res[0]==0) { // In case we do not need exterior help to endure the attacks
				if(firstFullDefensiveBuildTurn!=-1) {//And announce since when the production must go full defensive.
					res[2]=firstFullDefensiveBuildTurn-1;
				}
				res[1]=this.nbVA;
				this.localNbVA=res[1];
				this.localNbVA=this.nbVA;
				if(Math.ceil((shipsBeforeAttack-this.nbVD)/this.prod)<rebuildTime) {//If we have enough time to spare a turn before using all the production to defensive
					res[3]=0;
					//System.out.println("Available from planete " +this.getName() + " " + res[0] + " DShips, " + res[1] + " AShips, recommended to build " + res[3] + "DShips. Will ask to produce full defensive at turn " + res[2] );
				} else { //if we don't have time, just put all the production to defensive
					//System.out.println(this.getName() + " is in urge to produce DShips, but requires " + res[0] + " (no)  DShips, and gives away " + res[1] + " AShips, need to build " + res[3] + " DShips");
					res[3]=this.prod;
				}

			} else { //...---... ...---...
				//In that case, we'll need some ships to defend. The question being how many. Well res[0].
				//However, can we save some ships ?
				res[3]=this.prod; //However, let's stay on a fully defensive mode (the higher the defense, the harder the victory on it, and since it looks like a lost case, go for the last stand)
				if(this.nbVA>2*res[0]) { //If we have enough attack ships to endure (even in "defensive" mode, in which they are twice left efficient, but this case should not happen frequently, due to all the other cases which are going to happen more often)
					res[1]=this.nbVA-2*res[0]; //Then spare the rest.
					this.localNbVA=res[1];
					this.localNbVD=0;
					res[0]=0; //and in that case, we don't need any exterior defensive ship
					res[2]=-1; //And then i do not need any exterior help.
					//System.out.println(this.name + " is under high danger, but its AShips will save it. Requires "+ res[0] + " (no) DShips" + "and can give away " + res[1] + " AShips. Need to produce  " + res[3] + " DShips (Full defensive production)" );
				} else { //This is the tricky case, when the planet alone is doomed. (Not the death star's ray kind of doom though) //Best dad ever, btw.
					//Nothing to move on the defensive ship requirement, so res[0] stays res[0].
					this.localNbVA=this.nbVA;
					this.localNbVD=0; //Let's not five away the planet that easily.
					res[1]=this.nbVA; //Since we'll loose them, let's give them to the common wealth
					res[2]=(int)Math.floor(this.firstLostMoment); //The idea is "if, in my almost doomed case, you can save me, i need you to do it before this turn"
					//System.out.println(this.name + " is going to die if noone helps it. It requires " + res[0] + " DShips, before turn " + res[2] + ", can give away " + res[1] + " Aships, and will produce "+ res[3] + " DShips (Full defensive production)");
				}
				//A little piece of thought : if it is the last turn before the conquering eny arrival, what should i do ? The production being done AFTER the attack, setting the production to full attack ships will give me the opportunity to reconquer the planet more easily, but, if it is a very big fleet (the "unstoppable" kind), it will only help him to kill me.
				//.... Another way to slightly increase the efficiency of the IA. Maybe 2 or 3% more victory if i choose the right settings. Let's see to that later.
				//I'm getting talkative. It's late. 23:15. (23/5/2013, i'm not doing that on the last day !)
				//Very talkative indeed. I should go to sleep
			}
		}
		return res;
	}

	public void computeFuture() { //perfect that way.
		//Is used on ennemy planets to guess when they will be the weakest (assuming the worst case)
		//Is also used on ally planets. Because of the case you might have read a bit earlier (the doomed case)
		//future[n][5] is as follows : future[k][0] gives the number of the round after which state k will be efficient. As a double, it is the last eny attack time on a planet.
		//future[k][1] gives the number of attack ships, future[k][2] gives the number of defense ships, future[k][3] gives the defensive power of this planet.
		//future[k][4] gives the owner id at this state
		if(this.threats.getSize()==0) {
			this.weakestTime=-1; //Now but useless. That just mean the faster you'll rush into it, the better.
			this.weakestPower=this.getPower();
			this.delai=3200;
			this.firstLostMoment=-1; //Another way to mean the planet won't be lost.
			this.future=new double[1][5];
			this.future[0][0]=0; //time
			this.future[0][1]=this.nbVA; //do this really need an comment ?
			this.future[0][2]=this.nbVD; //I won't repeat myself
			this.future[0][3]=this.getPower(); //Isn't it obvious ?
			this.future[0][4]=this.player.getPlayID(); //It is.
		} else {
			this.future= new double[threats.getSize()+1][5];
			int p=0;
			int tNbVA=this.nbVA;
			int tNbVD=this.nbVD;
			double tPower=this.coef*(tNbVA+2*tNbVD);
			int tOwner=this.getPlayer().getPlayID();
			boolean ownerChanged=false;
			this.weakestTime=Client.round;
			this.weakestPower=tPower;
			this.future[p][0]=Client.round;
			this.future[p][1]=tNbVA;
			this.future[p][2]=tNbVD;
			this.future[p][3]=tPower;
			this.future[p][4]=tOwner;
			//double arrivalTime=Client.round; //Only for verifying the correct sorting system of the threat system. //Now useless //The line, not the sorting system. //Because the sorting system works perfectly. //If you don't believe me, uncomment it.
			int lastArrival=Client.round;
			int nextArrival;
			int rebuild;
			for(ListNode thre=threats.getFirst();thre!=null;thre=thre.getNext()) {//threat list is sorted by arrival time !
				p++;
				Flotte flt= (Flotte) thre.getObj();
				nextArrival=(int)Math.ceil(flt.getArrival(this.idPlan));
				rebuild=nextArrival-lastArrival;
				if(rebuild<0) {
					System.out.println("Error within the fleet gestion at planet threatening");
					System.out.println("I'll be partially blinded for a few turns (15*sqrt(2) in the worst of cases)");
				}
				if(this.player.getPlayID()==0) {
					tNbVD+=(int)Math.ceil(this.prod*rebuild*0.1d);
				} else {
					tNbVD+=(int)Math.ceil(this.prod*rebuild); //is a bit pessimitic, and assume the ennemy will play on a fully defensive pattern
				}
				tPower= this.coef*(tNbVA+2*tNbVD);
				if(flt.getPlayOrig()!=tOwner) { //if it is an "ennemy" fleet
					if(flt.getPower()>tPower) { //a conquering one
						tNbVA=(int) Math.ceil(flt.getShips()[0]*(1-tPower/flt.getPower()));
						tNbVD=(int) Math.ceil(flt.getShips()[1]*(1-tPower/flt.getPower()));
						tOwner=flt.getPlayOrig();
						if(!ownerChanged) {//By the way, if the owner won't have changed already, let's mark it.
							ownerChanged=true;
							this.firstLostMoment=flt.getArrival(this.idPlan);
						}
					} else { //a failed attack
						tNbVA-=(int) Math.ceil(flt.getPower()*tNbVA/tPower);
						tNbVD-=(int) Math.ceil(flt.getPower()*tNbVD/tPower);
					}
				} else { //allied fleet
					tNbVA+=flt.getShips()[0];
					tNbVD+=flt.getShips()[1];
				}
				tPower= this.coef*(tNbVA+2*tNbVD);
				lastArrival=nextArrival;
				/*if(arrivalTime>flt.getArrival(this.idPlan)) { //These lines are now useless. Because sorting doesn't fail anymore. //You don't believe me ? Uncomment it. I dare you to find this line actually printed in the console //Without modifiying the rest of the source code. That would be cheat.
					System.out.println("Sorting has failed");
				}
				arrivalTime=flt.getArrival(this.idPlan);*/
				
				this.future[p][0]=flt.getArrival(this.idPlan);
				this.future[p][1]=tNbVA;
				this.future[p][2]=tNbVD;
				this.future[p][3]=tPower;
				this.future[p][4]=tOwner;
				if(tPower<this.weakestPower) {//Since we compute the future, the earlier is the more accurate. So i won't try the "equal"
					this.weakestTime=flt.getArrival(this.idPlan);
					this.weakestPower=tPower;
				}
			}
		}
		this.computeImportance();
	}

	public void computeImportance() {
		//The idea is : if i don't already own this planet, and if i have not already send enough fleets to conquer it, how important is this planet to conquer ?
		this.importance=0;
		boolean mineOrAboutToBe=false; //Obvious name.
		int k=0;
		for(;k<this.future.length;k++) {
			if(this.future[k][4]==Client.iaID) {
				mineOrAboutToBe=true;
			}
		}
		if(!mineOrAboutToBe) { //If we already own (or about to own) this planet, we don't care about conquering it again. So forget about it.
			//This is the case i, in no way, am going to get this planet if i don't do a thing.
			for(int p=1;p<Client.planets.length;p++){
				double distance=Client.distance(Client.planets[p].getPos(), this.getPos());
				if(Client.planets[p].getPlayer().getPlayID()==Client.iaID && p!=this.idPlan) {
					this.importance+=100*Client.planets[p].getNbVA()/distance;
				} else if(Client.planets[p].getPlayer().getPlayID()==0 && p!=this.idPlan) {
					this.importance+=3*Client.planets[p].getProd()/distance;
				} else if(p!=this.idPlan) { //Meaning ennemy planet but not this one
					this.importance-=3*Client.planets[p].getNbVA()/distance;
				} else { //importance given from the production of the planet
					this.importance+=this.prod;
				}
			}
			/*double meanAlliedCoef=0;
			int alliedPlanets=0;
			for(int p=1;p<Client.planets.length;p++){
				double distance=Client.distance(Client.planets[p].getPos(), this.getPos());
				if(Client.planets[p].getPlayer().getPlayID()==Client.iaID && p!=this.idPlan) {
					meanAlliedCoef+=Client.planets[p].getCoef();
					alliedPlanets++;
					if(distance<=4) { //Being close is a plus.
						this.importance+=30*Client.planets[p].getProd()/distance; //Being productive is another plus, indeed.
					}
				} else if(Client.planets[p].getPlayer().getPlayID()==0 && p!=this.idPlan) {
					if(distance<=4) { //Being close to pacifists is good for war.
						this.importance+=60*Client.planets[p].getProd()/distance; //Even more important, for we won't have to protect against these ones.
					}
				}
			}
			if(alliedPlanets!=0) {
				meanAlliedCoef/=alliedPlanets;
			}
			if(this.future[this.future.length-1][0]!=this.weakestTime) { //Meaning if there is another attack after this weakest time
				for(int p=1;p<this.future.length-1;p++) {
					if(this.future[p][0]==this.weakestTime) {
						this.importance+=(this.future[p+1][0]-this.weakestTime)*this.prod; //This is the number of ships we can expect from this planet before having to rescue her.
						//The idea is that however, it will be efficient to take a planet. because it is some production the ennemy won't have.
						this.importance-=this.weakestPower/(2*meanAlliedCoef);
						//But we'd rather spend the lowest amount of ships
						break;
					}
				}
			} else {
				this.importance+=alliedPlanets*this.prod; //The closer the better
			}*/
			
		}
		//System.out.println("Planete "+ this.name +"'s Importance =" +this.importance);
	}


	public void affiche(double tab[][]){
		for (double[] aTab : tab) {
			for (double anATab : aTab) {
				System.out.print(anATab + " ");
			}
			System.out.println("");
		}
	}


	@Override
	public String toString() {
		return "planete " + this.idPlan + " " + this.name;
	}

	public static void main(String args[]) {
		Client.round=3;
		System.out.println("We are at turn " + Client.round);
		Player play=new ExternalPlayer("Max", 1);
		Planete planete1=new Planete(1, play, 2, 3, 0.5 , 10 , 5, 8);
		Player play2=new ExternalPlayer("Bob", 2);
		Planete planete2=new Planete(2, play2, 3, 4, 0.5 , 5, 5, 8);
		Planete planete3=new Planete(3, play, 6, 8, 0.5 , 10 , 5, 12);
		Planete planete4=new Planete(4, play2, 9, 1, 0.5 , 10 ,5, 30);
		Planete[] univ = new Planete[5];
		univ[1]=planete1;
		univ[2]=planete2;
		univ[3]=planete3;
		univ[4]=planete4;
		Client.planets=univ;
		Flotte squad1=new Flotte(30, 2, planete3,planete3.getPlayer().getPlayID(), 1, 4);
		Flotte squad4=new Flotte(30, 5, planete3,planete3.getPlayer().getPlayID(), 2, 4);
		Flotte squad2=new Flotte(10, 2, planete3,planete3.getPlayer().getPlayID(), 3, 4);
		Flotte squad3=new Flotte(13, 7, planete4,planete4.getPlayer().getPlayID(), 1, 4);
		squad1.refresh(univ,Client.round);
		squad4.refresh(univ,Client.round);
		squad2.refresh(univ,Client.round);
		squad3.refresh(univ,Client.round);
		System.out.println(squad1);
		System.out.println(squad2);
		System.out.println(squad3);
		System.out.println(squad4);
		System.out.println("\n");
		for(int k=1;k<univ.length;k++) {
			Planete p=univ[k];
			p.computeFuture();
			p.getAvailable();
			System.out.println("\nAbout her future : ");
			p.affiche(p.future);
			System.out.println("\n\n");
		}
	}
}

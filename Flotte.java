package classic;

public class Flotte {

	/**This class is used to compute threats on the planets.
	 * The planets then check their threats and if necessary "warn the IA" which will act accordingly*/

	private int[] ships; //ships[0] is the number of attacking ships //ships[1] is the number of defending ships
	private double[] proba; // estimation of probabilities for planets[k] to be targeted by this fleet
	private Planete planOrig;	//planet which the fleet came from (used for computation of travel time)
	private int playOrig;	//player who sent the fleet (used for computation of probabilities of send)
	private int launchRound; //used to manage time.
	private double power;
	private double coef;
	private int planDestID;	//planet destination ID. 0 if unknown
	private boolean arrivalSet=false;
	private double[] arrival; //Round on which the fleet should arrive on planet[k]
	private boolean[] threated; //true if this fleet has been declared threat on planet[k]
	private static double HThreshold=0.75d;  // Threshold (from 0 to 1) above which the alarm on a planet will be activated.
	private static double LThreshold=0.25d; //Threshold (from 0 to 1) under which the alarm will be disabled.
	boolean isNotRelevant=false; //if this fleet must have arrived, it'll be erased by the client through the check-up routine at the end of every turn
	private Planete[] plan;
	/** Perfectly working so far.*/

	public Flotte(int nbVa, int nbVd, Planete planet, int playOrig, int launchRound, int nPlanet) {//Constructor used if the fleet destination is unknown (dest become 0)
		this.power=planet.getCoef()*(2.0d*nbVa+nbVd);
		this.ships=new int[2];
		this.ships[0]=nbVa;
		this.ships[1]=nbVd;
		this.coef=planet.getCoef();
		this.proba=new double[nPlanet+1];
		this.planOrig=planet;
		this.playOrig=playOrig;
		this.launchRound=launchRound;
		this.planDestID=0;
		this.threated=new boolean[Client.planets.length+1];
		for(int k=1;k<nPlanet;k++) {
			this.threated[k]=false;
		}
		this.arrival=new double[Client.planets.length+1];
		for(int k=1; k<Client.planets.length; k++) {
			this.arrival[k]=this.launchRound+Client.distance(Client.planets[k].getPos(),planOrig.getPos());
		}
		this.arrivalSet=true;
	}


	public Flotte(int nbVa, int nbVd, Planete planet, int playOrig, int launchRound, int nPlanet, int planDestID) { //Constructor used if the fleet destination is known
		this.power=planet.getCoef()*(2.0d*nbVa+nbVd);
		this.ships=new int[2];
		this.ships[0]=nbVa;
		this.ships[1]=nbVd;
		this.coef=planet.getCoef();
		this.proba=new double[nPlanet+1];
		this.arrival=new double[nPlanet+1];
		this.planOrig=planet;
		this.playOrig=playOrig;
		this.launchRound=launchRound;
		this.planDestID=planDestID;
		this.threated=new boolean[Client.planets.length+1];
		this.arrival=new double[Client.planets.length+1];
		for(int k=1; k<Client.planets.length; k++) {
			this.arrival[k]=this.launchRound+Client.distance(Client.planets[k].getPos(),planOrig.getPos());
		}
		this.arrivalSet=true;
		for(int k=1; k<=nPlanet;k++) { //the probabilities are sure
			if(k==this.planDestID) {
				this.proba[k]=100;
				this.threated[k]=true;
			}
			else {
				this.proba[k]=0;
				this.threated[k]=false;
			}
		}
	}

	public boolean[] getThreated() {return this.threated;}
	public Planete[] getPlanetes() {return this.plan;}
	public int[] getShips() {return this.ships;}
	public double getPower() {return this.power;}
	public double getCoef() {return this.coef;}
	public double[] getProba() {return this.proba;}
	public Planete getPlanOrig() {return planOrig;}
	public int getPlayOrig() {return playOrig;}
	public int getLaunchRound() {return launchRound;}
	public double[] getArrival() {return this.arrival;}
	
	public void refresh(Planete planet[], int round) { //The idea of this system is to take into account only the planets in their original configuration. The fleet does not "update and change targets" in the middle of a move.
		this.plan=new Planete[planet.length];
		for(int k=1;k<planet.length;k++) { //Copy the planets in their actual state. Might be more efficient if i make this at the beginning of each turn
			this.plan[k]=new Planete(planet[k]);
		}
		if(this.planDestID!=0) {
			this.plan[this.planDestID].getRealPlanete().addThreat(this);
		}
		this.refresh();
	}

	public void refresh() { //recomputing the probabilities. Is only used at the initialisation of the fleet. Then use only the refresh(int)
		//If a proba[k]>0 on a ennemy planet (from this fleet ... not from the local player ...) you could understand it as a hazard scale. (On a scale from 0 to 100 of course ...)
		if(!isNotRelevant) {
			boolean hasStillValidTargets=false;
			for(int k=1; k<plan.length && !hasStillValidTargets;k++) { //check wether the fleet is still relevant
				if(this.arrival[k]>=Client.round) {
					hasStillValidTargets=true;
				}
			}
			isNotRelevant=!hasStillValidTargets; //and if no valid target were found, consider it not relevant.
			if(isNotRelevant) {
				Client.irrelevantFleets=true;
				for(int k=1;k<plan.length;k++) {
					plan[k].getRealPlanete().removeThreat(this);
					this.threated[k]=false;
				}
				System.out.println("NotRelevantFleet found : " + this.toString());
			}
		}
		if(!isNotRelevant) {
			if(this.planDestID==0) { //if the destination is !=0, the destination is sure and do not need recomputation
				for(int k=1;k<plan.length;k++) {
					this.proba[k]=0;
					if(Client.round<this.arrival[k]) {//if should have already arrived, then it is not the right plan, so stay at 0.
						if(this.planOrig.getIdPlan()!=k && this.playOrig==plan[k].getPlayer().getPlayID()) { // if plan[k] is prop but is not the original one

							if(this.ships[0]>3*this.ships[1]) { //if looks like an attack fleet 
								this.proba[k]-=20; // then it is probably not the aimed plan
								if(plan[k].getCoef()>plan[playOrig].getCoef()) { // if the coeff of the plan is higher
									this.proba[k]+=30; //then it might be the right one.
								}
							}

							else if(3*this.ships[0]<this.ships[1]) { //else if looks like a def fleet
								this.proba[k]+=20; //then it probably is
							}


							if(!plan[k].getThreats().isEmpty()) { //Case if "the fleet could save the ally
								if(this.launchRound>=((Flotte)plan[k].getThreats().getFirst().getObj()).launchRound) { //if it has been sent after the closestThreat
									if(((Flotte)plan[k].getThreats().getFirst().getObj()).getPower()>plan[k].getPower()) {//if this plan is under short term menace
										this.proba[k]+=10; //it will however make the attack fleet more damages, so more probable
										if((this.ships[0]+2*this.ships[1])*plan[k].getCoef()>=((Flotte)plan[k].getThreats().getFirst().getObj()).getPower()-plan[k].getPower()) {//if the def-looking fleet can save the plan
											if(this.arrival[k]<((Flotte)plan[k].getThreats().getFirst().getObj()).getArrival()[k]) { //and if it arrives before the threatening fleet
												this.proba[k]+=30;//then probably is
											}
										}
									}
								}
							}


						} else if(this.playOrig!=plan[k].getPlayer().getPlayID()) { // case plan[k] is not prop

							if(this.ships[0]>3*this.ships[1]) { //if looks like an attack fleet 
								this.proba[k]+=20; // then is probably the aimed plan
							}

							else if(3*this.ships[0]<this.ships[1]) { //else if looks like a def fleet
								this.proba[k]-=20; //then it probably is not the right plan
							}

							if((plan[k].getNbVA()+2*plan[k].getNbVD())*plan[k].getCoef()<(this.ships[0]*2+this.ships[1])*planOrig.getCoef()) { //Probabilities taken from the fleetpower/plandef ratio
								if((plan[k].getNbVA()+2*plan[k].getNbVD())*plan[k].getCoef()<2*(this.ships[0]*2+this.ships[1])*planOrig.getCoef()) { //This is for low level IVs who do not take into account the travel time
									proba[k]+=40-(plan[k].getNbVA()+2*plan[k].getNbVD())*plan[k].getCoef()+(this.ships[0]*2+this.ships[1])*planOrig.getCoef();
									if((plan[k].getNbVA()+2*(plan[k].getNbVD()+(getArrival(k)-launchRound)*plan[k].getProd()))*plan[k].getCoef()<2*(this.ships[0]*2+this.ships[1])*planOrig.getCoef()) { //This is for lvl 2-IVs who take into account the travel time
										proba[k]+=30-(plan[k].getNbVA()+2*(plan[k].getNbVD()+(getArrival(k)-launchRound)*plan[k].getProd()))*plan[k].getCoef()+2*(this.ships[0]*2+this.ships[1])*planOrig.getCoef();
									}
								}
								else {proba[k]+=40-(plan[k].getNbVA()+2*plan[k].getNbVD())*plan[k].getCoef()+(this.ships[0]*2+this.ships[1])*planOrig.getCoef();}
							}
						}
					}
				}
				/* This warning system allow the IA to select the highest danger and take care of it, forgetting less important problems
				 * 	Cannot forecast so far
				 * 	It somehow fits the idea of "the greater good"*/
				double maxProba=0;//getting the max of proba[] to select which plan is to be "warned"
				for (int k=1;k<plan.length; k++) { 
					if(this.proba[k]>maxProba) {maxProba=proba[k];}
				}
				for (int k=1;k<plan.length;k++) {
					if(this.proba[k]>=maxProba*Flotte.HThreshold && !this.threated[k]) { //warn plans which proba is above maxProba*Hthreshold
						plan[k].getRealPlanete().addThreat(this);
						this.threated[k]=true;
					}
					else if((this.proba[k]<=maxProba*Flotte.LThreshold && this.threated[k])) { //disable the alarm on plans which proba is under maxProba*Lthreshold
						plan[k].getRealPlanete().removeThreat(this);
						this.threated[k]=false;
					}
				}
			}
		}
	}

	public double getArrival(int id) {
		if(id<this.arrival.length) {
			return this.arrival[id];
		}
		else {
			return (Double) null;
		}
	}

	public String toString() {
		return "Fleet from " + this.planOrig.getName() + " (" +this.planOrig.getIdPlan()+ ") with " + this.ships[0] + "/" + this.ships[1] + " ships. Launched at round " + this.launchRound + " most probably directed to " + mostProbablePlan().getName() + " (" + mostProbablePlan().getIdPlan() + ") on turn " + this.arrival[mostProbablePlan().getIdPlan()];
	}

	public String shorten() {
		return "Fleet with " + this.ships[0] + "/" + this.ships[1] + " ships most probably directed to " + mostProbablePlan().getName() + " (" + mostProbablePlan().getIdPlan() + ") on turn" + this.arrival[mostProbablePlan().getIdPlan()];
	}

	public void removeThreats() {//is only used to clear the planets when the fleet gets deleted
		for(int k=1; k<this.threated.length; k++) {
			if(this.threated[k]) {
				this.plan[k].getRealPlanete().removeThreat(this);
			}
		}
	}

	public double[] mostProbableDest() {
		double[] res={0,0};
		double proba=-100;
		for(int k=1;k<this.plan.length;k++) {
			if(this.proba[k]>proba) {
				proba=this.proba[k];
				res=plan[k].getPos();
			}
		}
		return res;
	}

	public Planete mostProbablePlan() {
		Planete res=null;
		double proba=-100;
		for(int k=1;k<this.plan.length;k++) {
			if(this.proba[k]>proba) {
				proba=this.proba[k];
				res=plan[k];
			}
		}
		return res;
	}

	public static void main(String[] args) {
		/*ExternalPlayer play=new ExternalPlayer("Bob", 1);
		ExternalPlayer play2=new ExternalPlayer("Max", 2);
		System.out.println("2 Players Created");
		Planete planete=new Planete(1, play, 3, 4, 0.7d, 8, 5, 7);
		Planete planete2=new Planete(2, play2, 4, 5, 0.4d, 4, 1, 5);
		Planete planete3=new Planete(3, play, 4, 6, 0.8d, 4, 2, 6);
		Planete planete4=new Planete(4, play2, 9, 1, 0.5d , 10 , 5, 8);
		System.out.println("2 Planets Created");
		Flotte squad= new Flotte(10, 2, planete, 1, 4);
		System.out.println("Fleet Created");
		Planete[] univ = new Planete[5];
		univ[1]=planete;
		univ[2]=planete2;
		univ[3]=planete3;
		univ[4]=planete4;
		squad.refresh(univ,1);*/
		Player play=new ExternalPlayer("Max", 1);
		Planete planete1=new Planete(1, play, 2, 3, 0.5 , 10 , 5, 8);
		Player play2=new ExternalPlayer("Bob", 2);
		Planete planete2=new Planete(2, play2, 3, 4, 0.5 , 10 , 5, 8);
		Planete planete3=new Planete(3, play, 6, 8, 0.5 , 10 , 5, 8);
		Planete planete4=new Planete(4, play2, 9, 1, 0.5 , 10 , 5, 8);
		Planete[] univ = new Planete[5];
		univ[1]=planete1;
		univ[2]=planete2;
		univ[3]=planete3;
		univ[4]=planete4;
		Client.planets=univ;
		Flotte squad=new Flotte(10, 2, planete1, 1, 1, 4);
		Flotte squad2=new Flotte(0, 10, planete2, 2, 2, 4);
		squad.refresh(univ,1);
		squad2.refresh(univ,1);
		System.out.println(squad);
		System.out.println(squad2);
		System.out.println("About the second squad : ");
		System.out.println("Arrival on Planet 1 = " + squad2.arrival[1]);
		System.out.println("Arrival on Planet 2 = " + squad2.arrival[2]);
		System.out.println("Arrival on Planet 3 = " + squad2.arrival[3]);
		System.out.println("Arrival on Planet 3 = " + squad2.arrival[4]);
		System.out.println("Proba on Planet 1 = " + squad2.proba[1]);
		System.out.println("Proba on Planet 2 = " + squad2.proba[2]);
		System.out.println("Proba on Planet 3 = " + squad2.proba[3]);
		System.out.println("Proba on Planet 4 = " + squad2.proba[4]);

	}
}

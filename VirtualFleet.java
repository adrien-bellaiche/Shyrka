package classic;

import java.util.ArrayList;

public class VirtualFleet {

	public static ArrayList<VirtualFleet> vfleets=new ArrayList<>();
	private int vNBVA;
	private int vNBVD;
	private int vIdPlan;
	private int vDestID;

	public VirtualFleet(int idPlan, int vNBVA, int vNBVD, int destID) {
		this.vNBVA=vNBVA;
		this.vNBVD=vNBVD;
		this.vIdPlan=idPlan;
		this.vDestID=destID;
	}

	public int getDestID() {return this.vDestID;}
	public static int compare(int idPlan, int NBVA, int NBVD) {
		int res=0;
		for(VirtualFleet vflt : VirtualFleet.vfleets) {
			if(vflt.vIdPlan==idPlan && vflt.vNBVA==NBVA && vflt.vNBVD==NBVD) {
				res=vflt.vDestID;
				VirtualFleet.vfleets.remove(vflt);
				break;
			}
		}
		return res;
	}

	public static void newVFleet(int idPlan, int vNBVA, int vNBVD, int destID) {
		VirtualFleet.vfleets.add(new VirtualFleet(idPlan, vNBVA, vNBVD, destID));
	}



}

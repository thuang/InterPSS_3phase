package org.ipss.aclf.threePhase.util;

import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseBus;
import org.ipss.aclf.threePhase.ThreePhaseGen;
import org.ipss.aclf.threePhase.ThreePhaseNetwork;
import org.ipss.aclf.threePhase.ThreePhaseXformer;
import org.ipss.aclf.threePhase.impl.ThreePhaseBranchImpl;
import org.ipss.aclf.threePhase.impl.ThreePhaseBusImpl;
import org.ipss.aclf.threePhase.impl.ThreePhaseGenImpl;
import org.ipss.aclf.threePhase.impl.ThreePhaseXformerImpl;

import com.interpss.common.exp.InterpssException;

public class ThreePhaseObjectFactory {
	
	public static ThreePhaseXformer create3PXformer(){
	   ThreePhaseXformer ph3Xfr = new ThreePhaseXformerImpl();
	   return ph3Xfr;
	}
	
	public static ThreePhaseBus create3PBus(String busId, ThreePhaseNetwork net) throws InterpssException{
		ThreePhaseBus bus = new ThreePhaseBusImpl();
		bus.setId(busId);
		net.addBus(bus);
		return bus;
	}
	
	public static ThreePhaseBranch create3PBranch(String fromBusId, String toBusId, String cirId,ThreePhaseNetwork net) throws InterpssException{
		ThreePhaseBranch branch = new ThreePhaseBranchImpl();
		net.addBranch(branch, fromBusId, toBusId, cirId);
		return branch;
	}
	
	public static ThreePhaseGen  create3PGenerator(String genId){
		ThreePhaseGen gen = new ThreePhaseGenImpl();
		gen.setId(genId);
		return gen;
	}

}

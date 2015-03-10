package org.ipss.aclf.threePhase.util;

import org.ipss.aclf.threePhase.Branch3Phase;
import org.ipss.aclf.threePhase.Bus3Phase;
import org.ipss.aclf.threePhase.Gen3Phase;
import org.ipss.aclf.threePhase.Load3Phase;
import org.ipss.aclf.threePhase.Network3Phase;
import org.ipss.aclf.threePhase.Transformer3Phase;
import org.ipss.aclf.threePhase.impl.Branch3PhaseImpl;
import org.ipss.aclf.threePhase.impl.Bus3PhaseImpl;
import org.ipss.aclf.threePhase.impl.Gen3PhaseImpl;
import org.ipss.aclf.threePhase.impl.Load3PhaseImpl;
import org.ipss.aclf.threePhase.impl.Transformer3PhaseImpl;

import com.interpss.common.exp.InterpssException;

public class ThreePhaseObjectFactory {
	
	public static Transformer3Phase create3PXformer(){
	   Transformer3Phase ph3Xfr = new Transformer3PhaseImpl();
	   return ph3Xfr;
	}
	
	public static Bus3Phase create3PBus(String busId, Network3Phase net) throws InterpssException{
		Bus3Phase bus = new Bus3PhaseImpl();
		bus.setId(busId);
		net.addBus(bus);
		return bus;
	}
	
	public static Branch3Phase create3PBranch(String fromBusId, String toBusId, String cirId,Network3Phase net) throws InterpssException{
		Branch3Phase branch = new Branch3PhaseImpl();
		net.addBranch(branch, fromBusId, toBusId, cirId);
		return branch;
	}
	
	public static Gen3Phase  create3PGenerator(String genId){
		Gen3Phase gen = new Gen3PhaseImpl();
		gen.setId(genId);
		return gen;
	}
	
	public static Load3Phase create3PLoad(String loadId){
		Load3Phase load = new Load3PhaseImpl();
		load.setId(loadId);
		return load;
	}

}

package org.ipss.threePhase.util;

import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;
import org.ipss.threePhase.basic.impl.Branch3PhaseImpl;
import org.ipss.threePhase.basic.impl.Bus3PhaseImpl;
import org.ipss.threePhase.basic.impl.Gen3PhaseImpl;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.basic.impl.Transformer3PhaseImpl;
import org.ipss.threePhase.dynamic.DStabGen3Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.impl.DStabGen3PhaseImpl;

import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;

public class ThreePhaseObjectFactory {
	
	public static Transformer3Phase create3PXformer(){
	   Transformer3Phase ph3Xfr = new Transformer3PhaseImpl();
	   return ph3Xfr;
	}
	
	public static Bus3Phase create3PBus(String busId, DStabNetwork3Phase net) throws InterpssException{
		Bus3Phase bus = new Bus3PhaseImpl();
		bus.setId(busId);
		bus.setBusFreqMeasureBlock(DStabObjectFactory.createBusFreqMeasurement());
		
		net.addBus(bus);
		
		return bus;
	}
	
	public static Branch3Phase create3PBranch(String fromBusId, String toBusId, String cirId,DStabNetwork3Phase net) throws InterpssException{
		Branch3Phase branch = new Branch3PhaseImpl();
		net.addBranch(branch, fromBusId, toBusId, cirId);
		return branch;
	}
	
	public static Gen3Phase  create3PGenerator(String genId){
		Gen3Phase gen = new Gen3PhaseImpl();
		gen.setId(genId);
		return gen;
	}
	
	public static DStabGen3Phase  create3PDynGenerator(String genId){
		DStabGen3Phase gen = new DStabGen3PhaseImpl();
		gen.setId(genId);
		return gen;
	}
	
	public static Load3Phase create3PLoad(String loadId){
		Load3Phase load = new Load3PhaseImpl();
		load.setId(loadId);
		return load;
	}

}

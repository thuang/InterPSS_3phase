package org.ipss.threePhase.util;

import org.interpss.numeric.NumericConstant;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.Network3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;
import org.ipss.threePhase.basic.impl.Branch3PhaseImpl;
import org.ipss.threePhase.basic.impl.Bus3PhaseImpl;
import org.ipss.threePhase.basic.impl.Gen3PhaseImpl;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.basic.impl.Transformer3PhaseImpl;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.impl.DStabNetwork3phaseImpl;
import org.ipss.threePhase.dynamic.model.DStabGen3Phase;
import org.ipss.threePhase.dynamic.model.impl.DStabGen3PhaseImpl;

import com.interpss.DStabObjectFactory;
import com.interpss.common.datatype.Constants;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.netAdj.AclfNetAdjustment;
import com.interpss.core.aclf.netAdj.NetAdjustFactory;
import com.interpss.core.acsc.AcscFactory;
import com.interpss.core.acsc.BusScCode;
import com.interpss.core.acsc.BusScGrounding;
import com.interpss.core.net.Branch;
import com.interpss.core.net.OriginalDataFormat;
import com.interpss.dstab.StaticLoadModel;

public class ThreePhaseObjectFactory {
	
	public static  DStabNetwork3Phase create3PhaseDStabNetwork() {
	       DStabNetwork3Phase net = new DStabNetwork3phaseImpl();
	        
	       //The following is copied from the DStabObjectFactory
	        AclfNetAdjustment netAdj = NetAdjustFactory.eINSTANCE.createAclfNetAdjustment();
			net.setAclfNetAdjust(netAdj);
			netAdj.setAclfNet(net);
			net.setId("undefined");
			net.setOriginalDataFormat(OriginalDataFormat.IPSS_API);
			net.setNetEqnIterationNoEvent(Constants.DStabNetItrNoEvent);
			net.setNetEqnIterationWithEvent(Constants.DStabNetItrWithEvent);
			net.setStaticLoadModel(StaticLoadModel.CONST_Z);
			net.setStaticLoadSwitchVolt(Constants.DStabStaticLoadSwithVolt);
			net.setStaticLoadSwitchDeadZone(Constants.DStabStaticLoadSwithDeadband);
	      return net;
	}
	public static Transformer3Phase create3PXformer(){
	   Transformer3Phase ph3Xfr = new Transformer3PhaseImpl();
	   return ph3Xfr;
	}
	
	public static Bus3Phase create3PBus(String busId, DStabNetwork3Phase net) throws InterpssException{
		Bus3Phase bus = new Bus3PhaseImpl();
	  
		//The following is copied from the DStabObjectFactory
		bus.setId(busId);
		BusScGrounding g = AcscFactory.eINSTANCE.createBusScGrounding();
  		bus.setId(busId);
  		bus.setScCode(BusScCode.NON_CONTRI);
  		bus.setScGenZ1(NumericConstant.LargeBusZ);
  		bus.setScGenZ0(NumericConstant.LargeBusZ);
  		bus.setScGenZ2(NumericConstant.LargeBusZ);
  		bus.setGrounding(g);
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
	public static Branch3Phase create3PBranch() {
		Branch3Phase branch = new Branch3PhaseImpl();
		return branch;
	}

}

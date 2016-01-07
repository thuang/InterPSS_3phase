package org.ipss.threePhase.test;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.impl.DStabNetwork3phaseImpl;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;

import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;

public class TestBase {

	
	 public DStabNetwork3Phase create2BusSys() throws InterpssException{
			
			DStabNetwork3Phase net = new DStabNetwork3phaseImpl();

			double baseKva = 100000.0;
			
			// set system basekva for loadflow calculation
			net.setBaseKva(baseKva);
		  
		   //Bus 1
	  		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PDStabBus("Bus1", net);
	  		// set bus name and description attributes
	  		bus1.setAttributes("Bus 1", "");
	  		// set bus base voltage 
	  		bus1.setBaseVoltage(230000.0);
	  		// set bus to be a swing bus
	  		bus1.setGenCode(AclfGenCode.NON_GEN);
	  		// adapt the bus object to a swing bus object
	  		bus1.setLoadCode(AclfLoadCode.CONST_P);
	  		
	  		//bus1.setLoadPQ(new Complex(1.0,0.2));
	  		
	  		Load3Phase load1 = new Load3PhaseImpl();
			load1.set3PhaseLoad(new Complex3x1(new Complex(1.0,0.2),new Complex(1.0,0.2),new Complex(1.0,0.2)));
			bus1.getThreePhaseLoadList().add(load1);
	  		
			

	  	  	// Bus 3
	  		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PDStabBus("Bus3", net);
	  		// set bus name and description attributes
	  		bus3.setAttributes("Bus 3", "");
	  		// set bus base voltage 
	  		bus3.setBaseVoltage(230000.0);
	  		// set bus to be a swing bus
	  		bus3.setGenCode(AclfGenCode.SWING);
	  		
	  		bus3.setSortNumber(1);
	  		bus3.setVoltage(new Complex(1.025,0));
	  		
	  		DStabGen gen2 = DStabObjectFactory.createDStabGen("Gen2");
	  		gen2.setMvaBase(100.0);
	  		gen2.setDesiredVoltMag(1.025);
	  		//gen2.setGen(new Complex(0.7164,0.2710));
	  		gen2.setPosGenZ(new Complex(0.02,0.2));
	  		gen2.setNegGenZ(new Complex(0.02,0.2));
	  		gen2.setZeroGenZ(new Complex(0.000,1.0E9));
	  		
	  		//add to contributed gen list
	  		bus3.getContributeGenList().add(gen2);
	  		
	  		EConstMachine mach2 = (EConstMachine)DStabObjectFactory.
					createMachine("1", "Mach-1", MachineType.ECONSTANT, net, "Bus3", "Gen2");
	  		
	  		mach2.setRating(100, UnitType.mVA, net.getBaseKva());
			mach2.setRatedVoltage(230000.0);
			mach2.calMultiFactors();
			mach2.setH(5.0E6);
			mach2.setD(0.01);
			mach2.setRa(0.02);
			mach2.setXd1(0.20);
	  				
	  	
	  		
	  		Branch3Phase bra = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus3", "0", net);
			bra.setBranchCode(AclfBranchCode.LINE);
			bra.setZ( new Complex(0.000,   0.100));
			bra.setHShuntY(new Complex(0, 0.200/2));
			bra.setZ0( new Complex(0.0,	  0.3));
			bra.setHB0(0.200/2);
	      
		
			//net.setBusNumberArranged(true);
	  		return net;
			
		}
}

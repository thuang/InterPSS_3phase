package org.ipss.threePhase.test;

import static org.ipss.aclf.threePhase.util.ThreePhaseUtilFunction.threePhaseXfrAptr;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;
import org.interpss.numeric.util.MatrixUtil;
import org.ipss.aclf.threePhase.Branch3Phase;
import org.ipss.aclf.threePhase.Bus3Phase;
import org.ipss.aclf.threePhase.Gen3Phase;
import org.ipss.aclf.threePhase.Transformer3Phase;
import org.ipss.aclf.threePhase.util.ThreePhaseObjectFactory;
import org.ipss.dynamic.threePhase.DStabNetwork3Phase;
import org.ipss.dynamic.threePhase.impl.DStabNetwork3phaseImpl;
import org.junit.Test;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;
import com.interpss.core.algo.LoadflowAlgorithm;

public class TwoBus_3Phase_Test {
	
	
	//@Test
	public void testLF() throws InterpssException{
		
		IpssCorePlugin.init();
		
		DStabNetwork3Phase net = create2BusSys();
	
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
	  	
	  	
	  	assertTrue(algo.loadflow())	;
		//output load flow summary result
	  	/*
	  	 * 
     BusID          Code           Volt(pu)   Angle(deg)     P(pu)     Q(pu)      Bus Name   
  -------------------------------------------------------------------------------------------
  Bus1         PV                   1.04000        5.77       0.7164    0.0681   Bus 1      
  Bus2                              1.03730        3.86       0.0000    0.0000   Bus 2      
  Bus3         Swing                1.02500        0.00      -0.7164   -0.2070   Bus 2 
	  	 */
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		
		   net.initThreePhaseFromLfResult();
		   
			  for(AcscBus bus: net.getBusList()){
				  if(bus instanceof Bus3Phase){
					  Bus3Phase ph3Bus = (Bus3Phase) bus;
					  
					  System.out.println(bus.getId() +": Vabc =  "+ph3Bus.get3PhaseVotlages());
	                  /*
	                   *Bus1: Vabc =  0.94835 + j-0.42688  -0.84386 + j-0.60786  -0.10449 + j1.03474
						Bus2: Vabc =  1.03494 + j0.06989  -0.5780 + j0.86134  -0.45694 + j-0.93123
						Bus3: Vabc =  1.0250 + j0.0000  -0.5125 + j0.88768  -0.5125 + j-0.88768
	                   */
					  if(bus.getId().equals("Bus1")){
						  
						  //phase a: 5.77 - 30 = -24.23
						  assertTrue(ph3Bus.get3PhaseVotlages().a_0.subtract(new Complex(0.94835,-0.42688)).abs()<5.0E-5);
						  
					  }
				  }
			  }
			  
	}
	
	@Test
	public void testYMatrixabc() throws Exception{
		
		IpssCorePlugin.init();
		
		DStabNetwork3Phase net = create2BusSys();
		
	
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
	  	
	  	
	  
	  	assertTrue(algo.loadflow())	;
	  	
	  	ISparseEqnComplexMatrix3x3  Yabc = net.formYMatrixABC();
	  	
	  	System.out.println(Yabc.getSparseEqnComplex());
	  	MatrixUtil.matrixToMatlabMFile("output/twoBusYabc.m", Yabc.getSparseEqnComplex());
		
	}
	
	private DStabNetwork3Phase create2BusSys() throws InterpssException{
		
		DStabNetwork3Phase net = new DStabNetwork3phaseImpl();

		double baseKva = 100000.0;
		
		// set system basekva for loadflow calculation
		net.setBaseKva(baseKva);
	  
	//Bus 1
  		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PBus("Bus1", net);
  		// set bus name and description attributes
  		bus1.setAttributes("Bus 1", "");
  		// set bus base voltage 
  		bus1.setBaseVoltage(16500.0);
  		// set bus to be a swing bus
  		bus1.setGenCode(AclfGenCode.GEN_PV);
  		// adapt the bus object to a swing bus object
  		
  		// create contribute generator
  		// MVABase, power, sourceZ1/2/0
  		Gen3Phase gen1 = ThreePhaseObjectFactory.create3PGenerator("Gen1");
  		gen1.setMvaBase(100.0);
  		gen1.setDesiredVoltMag(1.04);
  		gen1.setGen(new Complex(0.7164,0.2710));
  		gen1.setPosGenZ(new Complex(0.02,0.05));
  		gen1.setNegGenZ(new Complex(0.1,0.05));
  		gen1.setZeroGenZ(new Complex(0,0.05));
  		bus1.getContributeGenList().add(gen1);
  		bus1.setSortNumber(0);
  		
  		
  		
  	// Bus 2
  		Bus3Phase bus2 = ThreePhaseObjectFactory.create3PBus("Bus2", net);
  		// set bus name and description attributes
  		bus2.setAttributes("Bus 2", "");
  		// set bus base voltage 
  		bus2.setBaseVoltage(230000.0);
  		// set bus to be a swing bus
  		bus2.setGenCode(AclfGenCode.NON_GEN);
  		bus2.setSortNumber(1);

  	  	// Bus 3
  		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PBus("Bus3", net);
  		// set bus name and description attributes
  		bus3.setAttributes("Bus 2", "");
  		// set bus base voltage 
  		bus3.setBaseVoltage(230000.0);
  		// set bus to be a swing bus
  		bus3.setGenCode(AclfGenCode.SWING);
  		
  		bus3.setSortNumber(2);
  		
  	// create contribute generator
  		// MVABase, power, sourceZ1/2/0
  		Gen3Phase gen2 = ThreePhaseObjectFactory.create3PGenerator("InfGen");
  		gen2.setMvaBase(100.0);
  		gen2.setDesiredVoltMag(1.025);
//  		gen2.setGen(new Complex(1.6300, 0.0659));
  		gen2.setPosGenZ(new Complex(0,0.05));
  		gen2.setNegGenZ(new Complex(0,0.05));
  		gen2.setZeroGenZ(new Complex(0,0.05));
  		bus3.getContributeGenList().add(gen2);
  		
  		
  		Branch3Phase bra23 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus3", "0", net);
		bra23.setBranchCode(AclfBranchCode.LINE);
		bra23.setZ( new Complex(0.000,   0.100));
		bra23.setHShuntY(new Complex(0, 0.200/2));
		bra23.setZ0( new Complex(0.0,	  0.3));
		bra23.setHB0(0.200/2);
      
		
		//////////////////transformers///////////////////////////////////////////
		Branch3Phase xfr12 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
		xfr12.setBranchCode(AclfBranchCode.XFORMER);
		xfr12.setZ( new Complex( 0.0, 0.05 ));
		xfr12.setZ0( new Complex(0.0, 0.05 ));
		Transformer3Phase xfr = threePhaseXfrAptr.apply(xfr12);
		xfr.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
		xfr.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
  		
		
		//net.setBusNumberArranged(true);
  		return net;
		
	}

}

package org.ipss.threePhase.test;

import static org.ipss.aclf.threePhase.util.ThreePhaseUtilFunction.threePhaseXfrAptr;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;
import org.interpss.numeric.util.MatrixOutputUtil;
import org.ipss.aclf.threePhase.util.ThreePhaseAclfOutFunc;
import org.ipss.aclf.threePhase.util.ThreePhaseObjectFactory;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Transformer3Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.impl.DStabNetwork3phaseImpl;
import org.junit.Test;

import com.interpss.CoreObjectFactory;
import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.dstab.DStabBranch;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;
import com.interpss.dstab.mach.RoundRotorMachine;

public class ThreeBus_3Phase_Test {
	
	@Test
	public void testYMatrixabc() throws Exception{
		
		IpssCorePlugin.init();
		IpssLogger.getLogger().setLevel(Level.INFO);
		
		DStabNetwork3Phase net = create3BusSys();
		
	
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
	  	
	  	
	  
	  	assertTrue(algo.loadflow())	;
	  	
	 	net.initDStabNet();
	  	
	  	ISparseEqnComplexMatrix3x3  Yabc = net.formYMatrixABC();
	  	
	    //System.out.println(Yabc.getSparseEqnComplex());
	    // MatrixOutputUtil.matrixToMatlabMFile("output/ThreeBusYabc.m", Yabc.getSparseEqnComplex());
	  	
	  	DStabBus bus1 = net.getBus("Bus1");
	  	Bus3Phase bus13p = (Bus3Phase) bus1;
	  	Gen3Phase gen1 = (Gen3Phase) bus1.getContributeGen("Gen1");
	  	Complex3x3 yg1abc= gen1.getYabc(false);
	  	
	  	System.out.println("Gen1 yabc = \n"+yg1abc);
	  	
	  	Branch3Phase xfr = (Branch3Phase) net.getBranch("Bus2->Bus1(0)");
	  	System.out.println("xfr yabc = \n"+xfr.getBranchYabc());
	  	System.out.println("xfr yffabc = \n"+xfr.getYffabc());
	  	
	  	/*
	  	 *  yft012 = diag([0;-20i*exp(30/180*pi*i) ; -20i*exp(-30/180*pi*i)])

			yft012 =
			
			   0.0000 + 0.0000i   0.0000 + 0.0000i   0.0000 + 0.0000i
			   0.0000 + 0.0000i  10.0000 -17.3205i   0.0000 + 0.0000i
			   0.0000 + 0.0000i   0.0000 + 0.0000i -10.0000 -17.3205i
			
			>> yftabc=T*yft012*inv(T)
			
			yftabc =
			
			   0.0000 -11.5470i  -0.0000 +11.5470i   0.0000 + 0.0000i
			   0.0000 + 0.0000i   0.0000 -11.5470i  -0.0000 +11.5470i
			  -0.0000 +11.5470i  -0.0000 + 0.0000i   0.0000 -11.5470i
	  	 */
	  	System.out.println("xfr yftabc = \n"+xfr.getYftabc());
	  	System.out.println("xfr yttabc = \n"+xfr.getYttabc());
	  	System.out.println("xfr ytfabc = \n"+xfr.getYtfabc());
	  	
	  	
	  	
	  	DStabBus bus3 = net.getBus("Bus3");
	  	Bus3Phase bus33p = (Bus3Phase) bus3;
	  	Gen3Phase gen2 = (Gen3Phase) bus3.getContributeGen("Gen2");
	  	Complex3x3 yg2abc= gen2.getYabc(false);
	  	
	  	/*
	  	 * yg2_012 = diag([0;1/(0.02+0.2i);1/(0.02+0.2i)])

		yg2_012 =
		
		   0.0000 + 0.0000i   0.0000 + 0.0000i   0.0000 + 0.0000i
		   0.0000 + 0.0000i   0.4950 - 4.9505i   0.0000 + 0.0000i
		   0.0000 + 0.0000i   0.0000 + 0.0000i   0.4950 - 4.9505i
		
		>> yg2_abc=T*yg2_012*inv(T)
		
		yg2_abc =
		
		   0.3300 - 3.3003i  -0.1650 + 1.6502i  -0.1650 + 1.6502i
		  -0.1650 + 1.6502i   0.3300 - 3.3003i  -0.1650 + 1.6502i
		  -0.1650 + 1.6502i  -0.1650 + 1.6502i   0.3300 - 3.3003i
	  	 */
	  	System.out.println("Gen2@Bus3 yabc = \n"+yg2abc);
	  	
	  	
	  	
	  	Branch3Phase line23 = (Branch3Phase) net.getBranch("Bus2->Bus3(0)");
	  	System.out.println("line23 yttabc = \n"+line23.getYttabc());
	}
	
	@Test
	public void testSolvNetwork() throws Exception{
		
		IpssCorePlugin.init();
		IpssLogger.getLogger().setLevel(Level.INFO);
		
		DStabNetwork3Phase net = create3BusSys();
		
	
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
	  	
	  	
	  
	  	assertTrue(algo.loadflow())	;
	  	 System.out.println(AclfOutFunc.loadFlowSummary(net));
	  	
	  	
	  	net.initThreePhaseFromLfResult();
	  

		   
		  for(DStabBus bus: net.getBusList()){
			  if(bus instanceof Bus3Phase){
				  Bus3Phase ph3Bus = (Bus3Phase) bus;
				  
				  System.out.println(bus.getId() +": Vabc =  "+ph3Bus.get3PhaseVotlages());
			  }
		  }
	  	//TODO three-phase load flow result
		
	  	System.out.println(ThreePhaseAclfOutFunc.busLfSummary(net));
	  
	  	
	  	net.initDStabNet();
	  	
	  	ISparseEqnComplexMatrix3x3  Yabc = net.getYMatrixABC();
	  	System.out.println(Yabc.getSparseEqnComplex());
	    MatrixOutputUtil.matrixToMatlabMFile("output/ThreeBusYabc.m", Yabc.getSparseEqnComplex());
	  /**
	   * Xfr :Wye-g Wye-g connection
	   * 
	   * Bus, Igen:Bus1,1.29185 + j-5.1606  -5.11514 + j1.46152  3.82329 + j3.69908     ->abs(Ia)     =5.3198
         Bus, Igen:Bus3,-0.1915 + j-4.87234  -4.12382 + j2.60201  4.31532 + j2.27032    ->abs(Ig2_c)  =4.8761
         
         
         
         Xfr: Delta-Wye-g
         Bus, Igen:Bus1,-1.46152 + j-5.11514  -3.69908 + j3.82329  5.1606 + j1.29185   ->>Same mag, phase shifted 30 deg
         Bus, Igen:Bus3,-0.1915 + j-4.87234  -4.12382 + j2.60201  4.31532 + j2.27032
	   */
	  
	    net.solveNetEqn(false);
	  
	    
	}
	
	
private DStabNetwork3Phase create3BusSys() throws InterpssException{
		
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
  		gen1.setPosGenZ(new Complex(0.003,0.2));
  		gen1.setNegGenZ(new Complex(0.003,0.2));
  		gen1.setZeroGenZ(new Complex(0,1.0E9));
  		
  		//add to contributed gen list
  		bus1.getContributeGenList().add(gen1);
  		bus1.setSortNumber(0);
  		
  		RoundRotorMachine mach = (RoundRotorMachine)DStabObjectFactory.
				createMachine("1", "Mach-1", MachineType.EQ11_ED11_ROUND_ROTOR, net, "Bus1", "Gen1");
  		
  		mach.setRating(100, UnitType.mVA, net.getBaseKva());
		mach.setRatedVoltage(16500.0);
		mach.calMultiFactors();
		mach.setH(5.0);
		mach.setD(0.01);
		mach.setRa(0.003);
		mach.setXl(0.14);
		mach.setXd(1.1);
		mach.setXq(1.08);
		mach.setXd1(0.23);
		mach.setTd01(5.6);
		mach.setXq1(0.23);
		mach.setTq01(1.5);
		mach.setXd11(0.2);
		mach.setTq011(0.05);
		mach.setXq11(0.2);
		mach.setTd011(0.03);
		mach.setSliner(2.0);  // no saturation
		mach.setSe100(0.0);   // no saturation
		mach.setSe120(0.0);		
  		
  		
  		
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
  		
  		Gen3Phase gen2 = ThreePhaseObjectFactory.create3PGenerator("Gen2");
  		gen2.setMvaBase(100.0);
  		gen2.setDesiredVoltMag(1.025);
  		//gen2.setGen(new Complex(0.7164,0.2710));
  		gen2.setPosGenZ(new Complex(0.02,0.2));
  		gen2.setNegGenZ(new Complex(0.02,0.2));
  		gen2.setZeroGenZ(new Complex(0,1.0E9));
  		
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

  		

      
		
		//////////////////transformers///////////////////////////////////////////
		Branch3Phase xfr12 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus1", "0", net);
		xfr12.setBranchCode(AclfBranchCode.XFORMER);
		xfr12.setZ( new Complex( 0.0, 0.05 ));
		xfr12.setZ0( new Complex(0.0, 0.05 ));
		Transformer3Phase xfr = threePhaseXfrAptr.apply(xfr12);
		//TODO change for testing
		xfr.setToConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
		//xfr.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
		xfr.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
  		
		
  		Branch3Phase bra23 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus3", "0", net);
		bra23.setBranchCode(AclfBranchCode.LINE);
		bra23.setZ( new Complex(0.000,   0.100));
		bra23.setHShuntY(new Complex(0, 0.200/2));
		bra23.setZ0( new Complex(0.0,	  0.3));
		bra23.setHB0(0.200/2);
		
		
		//net.setBusNumberArranged(true);
  		return net;
		
	}

}
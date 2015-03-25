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
import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;
import com.interpss.dstab.mach.RoundRotorMachine;

public class TwoBus_3Phase_Test {
	
	//@Test
	public void test3PhaseTransformerYabc(){
		
	}
	
	
	@Test
	public void testInitBasedOnLF() throws InterpssException{
		
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
  Bus1         PV                   1.04000        3.85       0.7164    0.0719   Bus 1      
  Bus3         Swing                1.02500        0.00      -0.7164   -0.2347   Bus 2   
	  	 */
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		
		   net.initThreePhaseFromLfResult();
		   
			  for(AcscBus bus: net.getBusList()){
				  if(bus instanceof Bus3Phase){
					  Bus3Phase ph3Bus = (Bus3Phase) bus;
					  
					  System.out.println(bus.getId() +": Vabc =  "+ph3Bus.get3PhaseVotlages());
	                  /*
	                   *Bus1: Vabc =  1.03765 + j0.06989  -0.57935 + j0.86368  -0.4583 + j-0.93358

						Bus3: Vabc =  1.0250 + j0.0000  -0.5125 + j0.88768  -0.5125 + j-0.88768
	                   */
					  if(bus.getId().equals("Bus1")){
						  
						  //phase a: 5.77 - 30 = -24.23
						  assertTrue(ph3Bus.get3PhaseVotlages().a_0.subtract(new Complex(1.03765,0.06989)).abs()<5.0E-5);
						  
					  }
				  }
			  }
			  
	}
	
	//@Test
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
		
	  	/**
	  	 * y12_012 = diag([-3.33i,-10i,-10i])

			y12_012 =
			
			   0.0000 - 3.3300i   0.0000 + 0.0000i   0.0000 + 0.0000i
			   0.0000 + 0.0000i   0.0000 -10.0000i   0.0000 + 0.0000i
			   0.0000 + 0.0000i   0.0000 + 0.0000i   0.0000 -10.0000i

            >> y12ABC=T*y12_012*inv(T)

		     y12ABC =
		
		   0.0000 - 7.7767i  -0.0000 + 2.2233i  -0.0000 + 2.2233i
		   0.0000 + 2.2233i   0.0000 - 7.7767i  -0.0000 + 2.2233i
		  -0.0000 + 2.2233i   0.0000 + 2.2233i   0.0000 - 7.7767i
	  	 */
	  	
	  	
	  	
	    /*
	     *  yABC_bus1_gen =
	     *    0.0000 - 3.3333i  -0.0000 + 1.6667i  -0.0000 + 1.6667i
			   0.0000 + 1.6667i   0.0000 - 3.3333i  -0.0000 + 1.6667i
			  -0.0000 + 1.6667i  -0.0000 + 1.6667i   0.0000 - 3.3333i
	     */

	
	  	
	  	
	  	
	    /*
	     * yABC_bus2_gen =

		   0.3300 - 3.3003i  -0.1650 + 1.6502i  -0.1650 + 1.6502i
		  -0.1650 + 1.6502i   0.3300 - 3.3003i  -0.1650 + 1.6502i
		  -0.1650 + 1.6502i  -0.1650 + 1.6502i   0.3300 - 3.3003i
	     */
	}
	
	@Test
	public void testSolvNetwork() throws Exception{
		
		IpssCorePlugin.init();
		
		DStabNetwork3Phase net = create2BusSys();
		
	
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
	  	
	  	
	  
	  	assertTrue(algo.loadflow())	;
	  	
	  	
	  	
	  	net.init3PDstabNetwork();
	  	net.solve3PNetworkEquation();
	  	
	    ISparseEqnComplexMatrix3x3  Yabc = net.getYMatrixABC();
	//  	System.out.println(Yabc.getSparseEqnComplex());
	  	MatrixUtil.matrixToMatlabMFile("output/twoBusYabc.m", Yabc.getSparseEqnComplex());
	  	
		
	}
	
	@Test
	public void testIgenABC(){
		
		/*
		 * Igen = Yabc*[b1;b2]

			
			  Igen =
				
				   1.1191 - 5.2046i
				  -5.0668 + 1.6331i
				   3.9477 + 3.5715i
				  -0.1915 - 4.9478i
				  -4.1892 + 2.6397i
				   4.3808 + 2.3080i
				   
		     Igen1_012 = inv(T)*Igen(1:3)

				Igen1_012 =
				
				  -0.0000 + 0.0000i
				   1.1191 - 5.2046i
				   0.0000 - 0.0000i
				
				>> Igen2_012 = inv(T)*Igen(4:6)
				
				Igen2_012 =
				
				   0.0000 + 0.0000i
				  -0.1915 - 4.9478i
				  -0.0000 + 0.0000i
				  

		  
		    b(0): 1.11913 + j-5.20456
			b(1): -5.06684 + j1.63309
			b(2): 3.94772 + j3.57147
			b(3): -0.1915 + j-4.84527
			b(4): -4.10038 + j2.58848
			b(5): 4.29188 + j2.25679
			
		 * 
		 */
		//TODO mismatch found in the Igen2: 
		/*
		Calculated value is: -0.19149859179617584 -j*4.845269350539822
		
		conj(-0.7164-0.2347i)/1.025+1.025/(0.02+0.2i)

		ans =
		
		  -0.1915 - 4.8453i
		
		*/
		
		//TODO   YgABC*VABC + Ipower(Power/V1) = Igen
		
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
  		bus1.setBaseVoltage(230000.0);
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
  		gen1.setZeroGenZ(new Complex(0.000,1.0E9));
  		
  		//add to contributed gen list
  		bus1.getContributeGenList().add(gen1);
  		bus1.setSortNumber(0);
  		
  		RoundRotorMachine mach = (RoundRotorMachine)DStabObjectFactory.
				createMachine("1", "Mach-1", MachineType.EQ11_ED11_ROUND_ROTOR, net, "Bus1", "Gen1");
  		
  		mach.setRating(100, UnitType.mVA, net.getBaseKva());
		mach.setRatedVoltage(230000.0);
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
		mach.setXd11(0.20);
		mach.setTq011(0.05);
		mach.setXq11(0.20);
		mach.setTd011(0.03);
		mach.setSliner(2.0);  // no saturation
		mach.setSe100(0.0);   // no saturation
		mach.setSe120(0.0);		
  		
  		
  		
//  	// Bus 2
//  		Bus3Phase bus2 = ThreePhaseObjectFactory.create3PBus("Bus2", net);
//  		// set bus name and description attributes
//  		bus2.setAttributes("Bus 2", "");
//  		// set bus base voltage 
//  		bus2.setBaseVoltage(230000.0);
//  		// set bus to be a swing bus
//  		bus2.setGenCode(AclfGenCode.NON_GEN);
//  		bus2.setSortNumber(1);

  	  	// Bus 3
  		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PBus("Bus3", net);
  		// set bus name and description attributes
  		bus3.setAttributes("Bus 2", "");
  		// set bus base voltage 
  		bus3.setBaseVoltage(230000.0);
  		// set bus to be a swing bus
  		bus3.setGenCode(AclfGenCode.SWING);
  		
  		bus3.setSortNumber(1);
  		
  		Gen3Phase gen2 = ThreePhaseObjectFactory.create3PGenerator("Gen2");
  		gen2.setMvaBase(100.0);
  		gen2.setDesiredVoltMag(1.025);
  		//gen2.setGen(new Complex(0.7164,0.2710));
  		gen2.setPosGenZ(new Complex(0.02,0.2));
  		gen2.setNegGenZ(new Complex(0.02,0.2));
  		gen2.setZeroGenZ(new Complex(0.000,1.0E9));
  		
  		//add to contributed gen list
  		bus3.getContributeGenList().add(gen2);
  		
//  		EConstMachine mach2 = (EConstMachine)DStabObjectFactory.
//				createMachine("1", "Mach-1", MachineType.ECONSTANT, net, "Bus3", "Gen2");
//  		
//  		mach2.setRating(100, UnitType.mVA, net.getBaseKva());
//		mach2.setRatedVoltage(230000.0);
//		mach2.calMultiFactors();
//		mach2.setH(5.0E6);
//		mach2.setD(0.01);
//		mach2.setRa(0.02);
//		mach2.setXd1(0.20);
  		
		RoundRotorMachine mach2 = (RoundRotorMachine)DStabObjectFactory.
				createMachine("1", "Mach-1", MachineType.EQ11_ED11_ROUND_ROTOR, net, "Bus3", "Gen2");
  		
  		mach2.setRating(100, UnitType.mVA, net.getBaseKva());
		mach2.setRatedVoltage(230000.0);
		mach2.setH(5.0);
		mach2.setD(0.01);
		mach2.setRa(0.02);
		mach2.setXl(0.14);
		mach2.setXd(1.1);
		mach2.setXq(1.08);
		mach2.setXd1(0.23);
		mach2.setTd01(5.6);
		mach2.setXq1(0.23);
		mach2.setTq01(1.5);
		mach2.setXd11(0.20);
		mach2.setTq011(0.05);
		mach2.setXq11(0.20);
		mach2.setTd011(0.03);
		mach2.setSliner(2.0);  // no saturation
		mach2.setSe100(0.0);   // no saturation
		mach2.setSe120(0.0);	

  		
  		

  		
  		
  		Branch3Phase bra23 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus3", "0", net);
		bra23.setBranchCode(AclfBranchCode.LINE);
		bra23.setZ( new Complex(0.000,   0.100));
		bra23.setHShuntY(new Complex(0, 0.200/2));
		bra23.setZ0( new Complex(0.0,	  0.3));
		bra23.setHB0(0.200/2);
      
		
		//////////////////transformers///////////////////////////////////////////
//		Branch3Phase xfr12 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
//		xfr12.setBranchCode(AclfBranchCode.XFORMER);
//		xfr12.setZ( new Complex( 0.0, 0.05 ));
//		xfr12.setZ0( new Complex(0.0, 0.05 ));
//		Transformer3Phase xfr = threePhaseXfrAptr.apply(xfr12);
//		xfr.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
//		xfr.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//  		
//		
		//net.setBusNumberArranged(true);
  		return net;
		
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
  		gen1.setPosGenZ(new Complex(0.003,0.15));
  		gen1.setNegGenZ(new Complex(0.003,0.15));
  		gen1.setZeroGenZ(new Complex(0.003,0.12));
  		
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
		mach.setX0(0.1);
		mach.setX2(0.2);
		mach.setRa(0.003);
		mach.setXl(0.14);
		mach.setXd(1.1);
		mach.setXq(1.08);
		mach.setXd1(0.23);
		mach.setTd01(5.6);
		mach.setXq1(0.23);
		mach.setTq01(1.5);
		mach.setXd11(0.12);
		mach.setTq011(0.05);
		mach.setXq11(0.15);
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
  		gen2.setZeroGenZ(new Complex(0,0.2));
  		
  		//add to contributed gen list
  		bus3.getContributeGenList().add(gen2);
  		
  		EConstMachine mach2 = (EConstMachine)DStabObjectFactory.
				createMachine("1", "Mach-1", MachineType.ECONSTANT, net, "Bus3", "Gen2");
  		
  		mach2.setRating(100, UnitType.mVA, net.getBaseKva());
		mach2.setRatedVoltage(230000.0);
		mach2.calMultiFactors();
		mach2.setH(5.0E6);
		mach2.setD(0.01);
		mach2.setX0(0.1);
		mach2.setX2(0.2);
		mach2.setRa(0.02);
		mach2.setXd1(0.20);

  		
  		

  		
  		
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

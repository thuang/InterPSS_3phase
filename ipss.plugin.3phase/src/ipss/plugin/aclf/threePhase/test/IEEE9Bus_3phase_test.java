package ipss.plugin.aclf.threePhase.test;

import static com.interpss.core.funcImpl.AcscFunction.acscXfrAptr;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.display.AclfOutFunc.BusIdStyle;
import org.interpss.display.impl.AclfOut_BusStyle;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseBus;
import org.ipss.aclf.threePhase.ThreePhaseGen;
import org.ipss.aclf.threePhase.ThreePhaseNetwork;
import org.ipss.aclf.threePhase.impl.ThreePhaseNetworkImpl;
import org.ipss.aclf.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.CoreObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.aclf.adpter.AclfLoadBusAdapter;
import com.interpss.core.aclf.adpter.AclfPVGenBus;
import com.interpss.core.aclf.adpter.AclfSwingBus;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;
import com.interpss.core.algo.LoadflowAlgorithm;

public class IEEE9Bus_3phase_test {
	
	//@Test
	public void testLoadFlow() throws InterpssException {
		//Initialize logger and Spring config
		IpssCorePlugin.init();
		
		ThreePhaseNetwork net = createIEEE9Bus();
	
	// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
	net.initContributeGenLoad();
		
	//create a load flow algorithm object
  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
  	//run load flow using default setting
  	
  	
  	assertTrue(algo.loadflow())	;
	//output load flow summary result
	System.out.println(AclfOutFunc.loadFlowSummary(net));
	
	/**
Load Flow Summary

                         Max Power Mismatches
             Bus              dPmax       Bus              dQmax
            -------------------------------------------------------
            Bus7             0.000001  Bus4             0.000013 (pu)
                            0.0973520                   1.324432 (kva)

     BusID          Code           Volt(pu)   Angle(deg)     P(pu)     Q(pu)      Bus Name   
  -------------------------------------------------------------------------------------------
  Bus1         Swing                1.04000        0.00       0.7164    0.2704   BUS-1   100 
  Bus2         PV                   1.02500        9.28       1.6300    0.0665   BUS-2   100 
  Bus3         PV                   1.02500        4.66       0.8500   -0.1086   BUS-3   100 
  Bus4         PQ                   1.02579       -2.22       0.0000    0.0000   BUS-4   100 
  Bus5         PQ    + ConstP       0.99563       -3.99      -1.2500   -0.5000   BUS-5   100 
  Bus6         PQ    + ConstP       1.01265       -3.69      -0.9000   -0.3000   BUS-6   100 
  Bus7         PQ                   1.02577        3.72       0.0000    0.0000   BUS-7   100 
  Bus8         PQ    + ConstP       1.01588        0.73      -1.0000   -0.3500   BUS-8   100 
  Bus9         PQ                   1.03235        1.97       0.0000    0.0000   BUS-9   100  
	 */
	
	//BusStyle output provides bus generation and load, as well as branch power flow info
	//System.out.println(AclfOut_BusStyle.lfResultsBusStyle(net, BusIdStyle.BusId_No));
	
	}
	
	@Test
	public void test3PNetworkInitialization() throws InterpssException {
		//Initialize logger and Spring config
		IpssCorePlugin.init();
		
		ThreePhaseNetwork net = createIEEE9Bus();
		
		
		// initGenLoad-- summarize the effects of contributive Gen/Load to make equivGen/load for power flow calculation	
		net.initContributeGenLoad();
			
		//create a load flow algorithm object
	  	LoadflowAlgorithm algo = CoreObjectFactory.createLoadflowAlgorithm(net);
	  	//run load flow using default setting
		assertTrue(algo.loadflow())	;
	  	
	  		
		//output load flow summary result
		System.out.println(AclfOutFunc.loadFlowSummary(net));
		
		/**
			                           Load Flow Summary
			
			                         Max Power Mismatches
			             Bus              dPmax       Bus              dQmax
			            -------------------------------------------------------
			            Bus7             0.000001  Bus4             0.000013 (pu)
			                            0.0973520                   1.324432 (kva)
			
			     BusID          Code           Volt(pu)   Angle(deg)     P(pu)     Q(pu)      Bus Name   
			  -------------------------------------------------------------------------------------------
			  Bus1         Swing                1.04000        0.00       0.7164    0.2704   BUS-1   100 
			  Bus2         PV                   1.02500        9.28       1.6300    0.0665   BUS-2   100 
			  Bus3         PV                   1.02500        4.66       0.8500   -0.1086   BUS-3   100 
			  Bus4         PQ                   1.02579       -2.22       0.0000    0.0000   BUS-4   100 
			  Bus5         PQ    + ConstP       0.99563       -3.99      -1.2500   -0.5000   BUS-5   100 
			  Bus6         PQ    + ConstP       1.01265       -3.69      -0.9000   -0.3000   BUS-6   100 
			  Bus7         PQ                   1.02577        3.72       0.0000    0.0000   BUS-7   100 
			  Bus8         PQ    + ConstP       1.01588        0.73      -1.0000   -0.3500   BUS-8   100 
			  Bus9         PQ                   1.03235        1.97       0.0000    0.0000   BUS-9   100 
		*/
		
	   net.initThreePhaseFromLfResult();
	   
	  for(AcscBus bus: net.getBusList()){
		  if(bus instanceof ThreePhaseBus){
			  ThreePhaseBus ph3Bus = (ThreePhaseBus) bus;
			  
			  System.out.print(bus.getId() +": Vabc =  "+ph3Bus.get3PhaseVotlages());
			  
			  if (ph3Bus.isGen()){
				 ThreePhaseGen gen = (ThreePhaseGen) ph3Bus.getContributeGenList().get(0);
				  System.out.print(", \nGenPowerAbc = "+gen.getPowerAbc(UnitType.mVA)+", \nZabc = "+gen.getZabc(true));
			  }
			  System.out.print("\n");
		  }
		  
		  
	  }
	  
	 
		
	}
	
	private static ThreePhaseNetwork createIEEE9Bus() throws InterpssException{
		
		
		// Create an AclfNetwork object
				ThreePhaseNetwork net = new ThreePhaseNetworkImpl();

				double baseKva = 100000.0;
				
				// set system basekva for loadflow calculation
				net.setBaseKva(baseKva);
			  
			//Bus 1
		  		ThreePhaseBus bus1 = ThreePhaseObjectFactory.create3PBus("Bus1", net);
		  		// set bus name and description attributes
		  		bus1.setAttributes("Bus 1", "");
		  		// set bus base voltage 
		  		bus1.setBaseVoltage(16500.0);
		  		// set bus to be a swing bus
		  		bus1.setGenCode(AclfGenCode.SWING);
		  		// adapt the bus object to a swing bus object
		  		
		  		// create contribute generator
		  		// MVABase, power, sourceZ1/2/0
		  		ThreePhaseGen gen1 = ThreePhaseObjectFactory.create3PGenerator("Gen1");
		  		gen1.setMvaBase(100.0);
		  		gen1.setDesiredVoltMag(1.04);
		  		gen1.setGen(new Complex(0.7164,0.2710));
		  		gen1.setPosGenZ(new Complex(0,0.04));
		  		gen1.setNegGenZ(new Complex(0,0.04));
		  		gen1.setZeroGenZ(new Complex(0,0.04));
		  		bus1.getContributeGenList().add(gen1);
		  		
		  		
		  		
		  	// Bus 2
		  		ThreePhaseBus bus2 = ThreePhaseObjectFactory.create3PBus("Bus2", net);
		  		// set bus name and description attributes
		  		bus2.setAttributes("Bus 2", "");
		  		// set bus base voltage 
		  		bus2.setBaseVoltage(18000.0);
		  		// set bus to be a swing bus
		  		bus2.setGenCode(AclfGenCode.GEN_PV);
		  		// adapt the bus object to a swing bus object
		  		//AclfPVGenBus pvBus2 = bus2.toPVBus();
		  		// set swing bus attributes
		  		//pvBus2.setDesiredVoltMag(1.025, UnitType.PU);
		  		//pvBus2.setGenP(1.63);
		  		
		  	// create contribute generator
		  		// MVABase, power, sourceZ1/2/0
		  		ThreePhaseGen gen2 = ThreePhaseObjectFactory.create3PGenerator("Gen2");
		  		gen2.setMvaBase(100.0);
		  		gen2.setDesiredVoltMag(1.025);
		  		gen2.setGen(new Complex(1.6300, 0.0659));
		  		gen2.setPosGenZ(new Complex(0,0.089));
		  		gen2.setNegGenZ(new Complex(0,0.089));
		  		gen2.setZeroGenZ(new Complex(0,0.089));
		  		bus2.getContributeGenList().add(gen2);
		  		
		  		
		  	// Bus 3
		  		ThreePhaseBus bus3 = ThreePhaseObjectFactory.create3PBus("Bus3", net);
		  		// set bus name and description attributes
		  		bus3.setAttributes("Bus 3", "");
		  		// set bus base voltage 
		  		bus3.setBaseVoltage(13800.0);
		  		// set bus to be a swing bus
		  		bus3.setGenCode(AclfGenCode.GEN_PV);
		  		// adapt the bus object to a swing bus object
		  		//AclfPVGenBus pvbus3 = bus3.toPVBus();
		  		
		  		//pvbus3.setDesiredVoltMag(1.025, UnitType.PU);
		  		//pvbus3.setGenP(0.85);
		  		
		  	// create contribute generator
		  		// MVABase, power, sourceZ1/2/0
		  		ThreePhaseGen gen3 = ThreePhaseObjectFactory.create3PGenerator("Gen3");
		  		gen3.setMvaBase(100.0);
		  		gen3.setDesiredVoltMag(1.025);
		  		gen3.setGen(new Complex(0.8500, -0.1092));
		  		gen3.setPosGenZ(new Complex(0,0.107));
		  		gen3.setNegGenZ(new Complex(0,0.107));
		  		gen3.setZeroGenZ(new Complex(0,0.107));
		  		bus3.getContributeGenList().add(gen3);
		  		
		  		
		  	//Bus 4
		  		ThreePhaseBus bus4 =ThreePhaseObjectFactory.create3PBus("Bus4", net);
		  		bus4.setAttributes("Bus 4", "");
		  		bus4.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus4.setGenCode(AclfGenCode.NON_GEN);
		  		
		  	//Bus 5	
		  		ThreePhaseBus bus5 = ThreePhaseObjectFactory.create3PBus("Bus5", net);
		  		bus5.setAttributes("Bus 5", "");
		  		bus5.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus5.setGenCode(AclfGenCode.NON_GEN);
		  		// set the bus to a constant power load bus
		  		bus5.setLoadCode(AclfLoadCode.CONST_P);
		  		// adapt the bus object to a Load bus object
		  		AclfLoadBusAdapter loadBus5 = bus5.toLoadBus();
		  		// set load to the bus
		  		loadBus5.setLoad(new Complex(1.25, 0.5), UnitType.PU);
		  		
		  	//Bus 6	
		  		ThreePhaseBus bus6 = ThreePhaseObjectFactory.create3PBus("Bus6", net);
		  		bus6.setAttributes("Bus 6", "");
		  		bus6.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus6.setGenCode(AclfGenCode.NON_GEN);
		  		// set the bus to a constant power load bus
		  		bus6.setLoadCode(AclfLoadCode.CONST_P);
		  		// adapt the bus object to a Load bus object
		  		AclfLoadBusAdapter loadBus6 = bus6.toLoadBus();
		  		// set load to the bus
		  		loadBus6.setLoad(new Complex(0.9, 0.3), UnitType.PU);
		  		
		  		
		  	//Bus 7
		  		ThreePhaseBus bus7 = ThreePhaseObjectFactory.create3PBus("Bus7", net);
		  		bus7.setAttributes("Bus 7", "");
		  		bus7.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus7.setGenCode(AclfGenCode.NON_GEN);
		  		
		  	//Bus 8	
		  		ThreePhaseBus bus8 = ThreePhaseObjectFactory.create3PBus("Bus8", net);
		  		bus8.setAttributes("Bus 8", "");
		  		bus8.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus8.setGenCode(AclfGenCode.NON_GEN);
		  		// set the bus to a constant power load bus
		  		bus8.setLoadCode(AclfLoadCode.CONST_P);
		  		// adapt the bus object to a Load bus object
		  		AclfLoadBusAdapter loadBus8 = bus8.toLoadBus();
		  		// set load to the bus
		  		loadBus8.setLoad(new Complex(1.00, 0.35), UnitType.PU);
		  		
		  	//Bus 9
		  		ThreePhaseBus bus9 = ThreePhaseObjectFactory.create3PBus("Bus9", net);
		  		bus9.setAttributes("Bus 9", "");
		  		bus9.setBaseVoltage(230000.0);
		  		// set the bus to a non-generator bus
		  		bus9.setGenCode(AclfGenCode.NON_GEN);
		  		
		  //////////////////////////////Lines /////////////////////////////////
		  		ThreePhaseBranch bra78 = ThreePhaseObjectFactory.create3PBranch("Bus7", "Bus8", "0", net);
				bra78.setBranchCode(AclfBranchCode.LINE);
				bra78.setZ( new Complex( 0.00850,   0.07200));
				bra78.setHShuntY(new Complex(0,0.149/2));
				bra78.setZ0( new Complex(0.02125,	  0.18));
				bra78.setHB0(0.14900/2);
				
				
				ThreePhaseBranch bra89 = ThreePhaseObjectFactory.create3PBranch("Bus8", "Bus9", "0", net);
				bra89.setBranchCode(AclfBranchCode.LINE);
				bra89.setZ( new Complex(0.01190,   0.10080));
				bra89.setHShuntY(new Complex(0,0.20900/2));
				bra89.setZ0( new Complex(0.02975,	  0.252));
				bra89.setHB0(0.20900/2);
				
				
				ThreePhaseBranch bra57 = ThreePhaseObjectFactory.create3PBranch("Bus5", "Bus7", "0", net);
				bra57.setBranchCode(AclfBranchCode.LINE);
				bra57.setZ( new Complex(0.03200,   0.16100));
				bra57.setHShuntY(new Complex(0,0.30600/2));
				bra57.setZ0( new Complex(0.08,   	  0.4025));
				bra57.setHB0(0.30600/2);
				
				
				ThreePhaseBranch bra69 = ThreePhaseObjectFactory.create3PBranch("Bus6", "Bus9", "0", net);
				bra69.setBranchCode(AclfBranchCode.LINE);
				bra69.setZ( new Complex(0.03900,   0.17000));
				bra69.setHShuntY(new Complex(0, 0.35800/2));
				bra69.setZ0( new Complex(0.0975,	  0.425));
				bra69.setHB0(0.35800/2);
				
				ThreePhaseBranch bra45 = ThreePhaseObjectFactory.create3PBranch("Bus4", "Bus5", "0", net);
				bra45.setBranchCode(AclfBranchCode.LINE);
				bra45.setZ( new Complex(0.01000,   0.08500));
				bra45.setHShuntY(new Complex(0, 0.17600/2));
				bra45.setZ0( new Complex(0.025,	  0.2125));
				bra45.setHB0(0.17600/2);
				
				
				ThreePhaseBranch bra46 = ThreePhaseObjectFactory.create3PBranch("Bus4", "Bus6", "0", net);
				bra46.setBranchCode(AclfBranchCode.LINE);
				bra46.setZ( new Complex(0.01700,   0.09200));
				bra46.setHShuntY(new Complex(0, 0.15800/2));
				bra46.setZ0( new Complex(0.0425,	  0.23));
				bra46.setHB0(0.15800/2);
              
				
				//////////////////transformers///////////////////////////////////////////
				ThreePhaseBranch xfr14 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus4", "0", net);
				xfr14.setBranchCode(AclfBranchCode.XFORMER);
				xfr14.setZ( new Complex( 0.0, 0.0567 ));
				xfr14.setZ0( new Complex(0.0, 0.0567 ));
				AcscXformer xfr = acscXfrAptr.apply(xfr14);
				xfr.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
				xfr.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
				
				
				ThreePhaseBranch xfr27 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus7", "0", net);
				xfr27.setBranchCode(AclfBranchCode.XFORMER);
				xfr27.setZ( new Complex( 0.0, 0.0625 ));
				xfr27.setZ0( new Complex(0.0, 0.0625 ));
				xfr = acscXfrAptr.apply(xfr27);
				xfr.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
				xfr.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
				
				
				ThreePhaseBranch xfr39 = ThreePhaseObjectFactory.create3PBranch("Bus3", "Bus9", "0", net);
				xfr39.setBranchCode(AclfBranchCode.XFORMER);
				xfr39.setZ( new Complex( 0.0, 0.0586 ));
				xfr39.setZ0( new Complex(0.0, 0.0586 ));
				xfr = acscXfrAptr.apply(xfr39);
				xfr.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
				xfr.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);

		return net;
	}

}

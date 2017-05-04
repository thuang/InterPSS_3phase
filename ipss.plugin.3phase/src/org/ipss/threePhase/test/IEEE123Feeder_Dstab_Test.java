package org.ipss.threePhase.test;

import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.data_parser.OpenDSSDataParser;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.algo.DynamicEventProcessor3Phase;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
import org.ipss.threePhase.util.ThreePhaseAclfOutFunc;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.acsc.fault.SimpleFaultCode;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.algo.DynamicSimuAlgorithm;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.dstab.cache.StateMonitor.MonitorRecord;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;

public class IEEE123Feeder_Dstab_Test {
	
	//@Test
	public void testIEEE123BusPowerflow(){
		
		IpssCorePlugin.init();
		IpssCorePlugin.setLoggerLevel(Level.INFO);
		
		OpenDSSDataParser parser = new OpenDSSDataParser();
		parser.parseFeederData("testData\\feeder\\IEEE123","IEEE123Master_Modified_v2.dss");
		
		parser.calcVoltageBases();
		
		double mvaBase = 1.0;
		parser.convertActualValuesToPU(1.0);
		
		DStabNetwork3Phase distNet = parser.getDistNetwork();
		
//		String netStrFileName = "testData\\feeder\\IEEE123\\ieee123_modified_netString.dat";
//		try {
//			Files.write(Paths.get(netStrFileName), parser.getDistNetwork().net2String().getBytes());
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
		
		// set the  turn ratios of regulators
		parser.getBranchByName("reg1a").setToTurnRatio(1.0438);
		
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
		//distPFAlgo.orderDistributionBuses(true);
		distPFAlgo.setInitBusVoltageEnabled(true);
		//distPFAlgo.setMaxIteration(1);
		distPFAlgo.setTolerance(5.0E-3); // tolearnce = 5 kva
		assertTrue(distPFAlgo.powerflow());
		
		System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(distNet));
		
		Bus3Phase bus150r = (Bus3Phase) distNet.getBus("150r");
		Complex3x1 vabc_150r = bus150r.get3PhaseVotlages();
		/*
		 * 150r,1.0444156221522982,-3.212270295862824E-4,1.043045526755821,4.1894310060612145,1.0433712760113691,2.0939131182587714,1.04442 + j-0.00034  -0.52094 + j-0.90364  -0.52125 + j0.90384
		 */
		//assertTrue(vabc_150r.subtract(new Complex3x1(new Complex(1.04442,-0.00034),new Complex(-0.52094,-0.90364),new Complex(-0.52125,0.90384))).absMax()<1.0E-4);
		
		/// Compared with IEEE TEST FEEDER RESULTS
		//RG1   |  1.0437 at    .00  |  1.0438 at -120.00  |  1.0438 at  120.00 |    
//		assertTrue(Math.abs(vabc_150r.a_0.abs()-1.0437)<1.0E-3); 
//		assertTrue(Math.abs(vabc_150r.b_1.abs()-1.0438)<1.0E-3); 
//		assertTrue(Math.abs(vabc_150r.c_2.abs()-1.0438)<1.0E-3); 
		
		Bus3Phase bus21 = (Bus3Phase) distNet.getBus("21");
		Complex3x1 vabc_21 = bus21.get3PhaseVotlages();
		/*
		 * 21,0.9976629334520096,-0.039839533149384404,1.0320577255690082,4.166775074256249,1.0114596191804741,2.0742543870511136,0.99687 + j-0.03974  -0.53558 + j-0.88221  -0.48799 + j0.88596
         */
		
		
		/// Compared with IEEE TEST FEEDER RESULTS
		//  21    |   .9983 at  -2.34  |  1.0320 at -121.22  |  1.0111 at  118.81 |    .441
//		assertTrue(Math.abs(vabc_21.a_0.abs()-0.9983)<1.0E-3); //0.9983 is IEEE TEST FEEDER RESULT
//		assertTrue(Math.abs(vabc_21.b_1.abs()-1.0320)<1.0E-3); 
//		assertTrue(Math.abs(vabc_21.c_2.abs()-1.0111)<1.0E-3); 
		
		Bus3Phase bus30 = (Bus3Phase) distNet.getBus("30");
		Complex3x1 vabc_30 = bus30.get3PhaseVotlages();
		
		/*
		 *30,0.9963094030535219,-0.04263498620437,1.0332257087662153,4.167548363697641,1.008218641157312,2.073577445296613,0.9954 + j-0.04246  -0.5355 + j-0.88362  -0.48582 + j0.88345
         */
		
		/// Compared with IEEE TEST FEEDER RESULTS
		// 30    |   .9969 at  -2.50  |  1.0331 at -121.18  |  1.0078 at  118.77 |    .701
//		assertTrue(Math.abs(vabc_30.a_0.abs()-0.9969)<1.0E-3);
//		assertTrue(Math.abs(vabc_30.b_1.abs()-1.0331)<1.0E-3);
//		assertTrue(Math.abs(vabc_30.c_2.abs()-1.0078)<1.0E-3);
	}
	
	@Test
	public void testIEEE123BusDstabSim() throws InterpssException{
		
		IpssCorePlugin.init();
		IpssCorePlugin.setLoggerLevel(Level.INFO);
		
		OpenDSSDataParser parser = new OpenDSSDataParser();
		parser.parseFeederData("testData\\feeder\\IEEE123","IEEE123Master_Modified_v2.dss");
		
		parser.calcVoltageBases();
		
		double mvaBase = 100.0;
		parser.convertActualValuesToPU(mvaBase);
		
		DStabNetwork3Phase distNet = parser.getDistNetwork();
		
//		String netStrFileName = "testData\\feeder\\IEEE123\\ieee123_modified_netString.dat";
//		try {
//			Files.write(Paths.get(netStrFileName), parser.getDistNetwork().net2String().getBytes());
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}
		
		// set the  turn ratios of regulators
		parser.getBranchByName("reg1a").setToTurnRatio(1.0438);
		
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
		//distPFAlgo.orderDistributionBuses(true);
		//distPFAlgo.setInitBusVoltageEnabled(true);
		//distPFAlgo.setMaxIteration(1);
		distPFAlgo.setTolerance(5.0E-3); // tolearnce = max voltage difference of two steps
		assertTrue(distPFAlgo.powerflow());
		
		// output three phase total load
		for(DStabBus bus: distNet.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			bus3P.get3PhaseTotalLoad();
		}
		
		// Add the dyanmic machine to the source Bus
		Bus3Phase bus150 = (Bus3Phase) distNet.getBus("150");
		
		DStabGen constantGen = DStabObjectFactory.createDStabGen();
		constantGen.setId("Source");
		constantGen.setMvaBase(100);
		constantGen.setPosGenZ(new Complex(0.0,0.05));
		constantGen.setNegGenZ(new Complex(0.0,0.05));
		constantGen.setZeroGenZ(new Complex(0.0,0.05));
		bus150.getContributeGenList().add(constantGen);
		
		
		EConstMachine mach = (EConstMachine)DStabObjectFactory.
				createMachine("MachId", "MachName", MachineType.ECONSTANT, distNet, "150", "Source");
	
		mach.setRating(100, UnitType.mVA, distNet.getBaseKva());
		mach.setRatedVoltage(4160.0);
		mach.setH(50000.0);
		mach.setXd1(0.05);
		
		
		DynamicSimuAlgorithm dstabAlgo =DStabObjectFactory.createDynamicSimuAlgorithm(
				distNet, IpssCorePlugin.getMsgHub());
			
	
	  	
	  	dstabAlgo.setSimuMethod(DynamicSimuMethod.MODIFIED_EULER);
		dstabAlgo.setSimuStepSec(0.005d);
		dstabAlgo.setTotalSimuTimeSec(1.0);
	    //dstabAlgo.setRefMachine(net.getMachine("Bus3-mach1"));
		distNet.addDynamicEvent(DStabObjectFactory.createBusFaultEvent("150r", distNet, SimpleFaultCode.GROUND_LG,new Complex(0,0.0),new Complex(0,0.0), 0.5,0.07), "SLG@Bus1");
        
		
		StateMonitor sm = new StateMonitor();
		//sm.addGeneratorStdMonitor(new String[]{"Bus1-mach1","Bus2-mach1"});
		sm.addBusStdMonitor(new String[]{"150","150r","300","30","21"});
		sm.add3PhaseBusStdMonitor(new String[]{"150","150r","300","30","21"});
		// set the output handler
		dstabAlgo.setSimuOutputHandler(sm);
		dstabAlgo.setOutPutPerSteps(1);
		
		dstabAlgo.setDynamicEventHandler(new DynamicEventProcessor3Phase());
				
	  	if(dstabAlgo.initialization()){
	  		System.out.println(ThreePhaseAclfOutFunc.busLfSummary(distNet));
	  		System.out.println(distNet.getMachineInitCondition());
	  		
	  		for(String busId: sm.getBusPhAVoltTable().keySet()){
				
				 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)distNet.getBus(busId)).get3PhaseVotlages());
			}
	  	
	  		//dstabAlgo.performSimulation();
	  		while(dstabAlgo.getSimuTime()<=dstabAlgo.getTotalSimuTimeSec()){
				
				dstabAlgo.solveDEqnStep(true);
				
				for(String busId: sm.getBusPhAVoltTable().keySet()){
					
					 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)distNet.getBus(busId)).get3PhaseVotlages());
				}
				
			}
	  	}
	  	System.out.println(sm.toCSVString(sm.getBusPhAVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhBVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhCVoltTable()));
	  	
//	  	System.out.println(sm.toCSVString(sm.getBusAngleTable()));
	  	System.out.println(sm.toCSVString(sm.getBusVoltTable()));
	  	MonitorRecord rec1 = sm.getBusVoltTable().get("30").get(1);
	  	MonitorRecord rec20 = sm.getBusVoltTable().get("30").get(20);
	  	assertTrue(Math.abs(rec1.getValue()-rec20.getValue())<1.0E-4);
	  	
	  	
	  	MonitorRecord rec0 = sm.getBusVoltTable().get("21").get(0);
	  	MonitorRecord rec50 = sm.getBusVoltTable().get("21").get(50);
	  	assertTrue(Math.abs(rec0.getValue()-rec50.getValue())<1.0E-4);
	  	
	  	

	}

}

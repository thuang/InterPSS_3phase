package org.ipss.threePhase.test;

import static com.interpss.core.funcImpl.AcscFunction.acscXfrAptr;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.algo.DynamicEventProcessor3Phase;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
import org.ipss.threePhase.util.ThreePhaseAclfOutFunc;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.algo.DynamicSimuAlgorithm;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.dstab.cache.StateMonitor.MonitorRecord;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;

public class Test6BusFeeder {
	
	@Test
	public void testFeederPowerflow() throws InterpssException{
	       IpssCorePlugin.init();
		
			DStabNetwork3Phase distNet = createFeeder();
			
			DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
			//distPFAlgo.orderDistributionBuses(true);
			
			assertTrue(distPFAlgo.powerflow());
			
			System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(distNet));
			
			System.out.println("power@source = "+distNet.getBus("Bus1").getContributeGen("Source").getGen().toString());
	}
	
	@Test
	public void test6BusFeederDstabSim() throws InterpssException{
		
        IpssCorePlugin.init();
		
		DStabNetwork3Phase distNet = this.createFeeder();
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		
		System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(distNet));
		
		DynamicSimuAlgorithm dstabAlgo =DStabObjectFactory.createDynamicSimuAlgorithm(
				distNet, IpssCorePlugin.getMsgHub());
			
	
	  	
	  	dstabAlgo.setSimuMethod(DynamicSimuMethod.MODIFIED_EULER);
		dstabAlgo.setSimuStepSec(0.005d);
		dstabAlgo.setTotalSimuTimeSec(0.5);
	    //dstabAlgo.setRefMachine(net.getMachine("Bus3-mach1"));
		//distNet.addDynamicEvent(create3PhaseFaultEvent("Bus2",distNet,0.2,0.05),"3phaseFault@Bus2");
        
		
		StateMonitor sm = new StateMonitor();
		//sm.addGeneratorStdMonitor(new String[]{"Bus1-mach1","Bus2-mach1"});
		sm.addBusStdMonitor(new String[]{"Bus2","Bus1"});
		// set the output handler
		dstabAlgo.setSimuOutputHandler(sm);
		dstabAlgo.setOutPutPerSteps(1);
		
		dstabAlgo.setDynamicEventHandler(new DynamicEventProcessor3Phase());
				
	  	if(dstabAlgo.initialization()){
	  		System.out.println(ThreePhaseAclfOutFunc.busLfSummary(distNet));
	  		System.out.println(distNet.getMachineInitCondition());
	  	
	  		dstabAlgo.performSimulation();
	  	}
	  	System.out.println(sm.toCSVString(sm.getBusAngleTable()));
	  	System.out.println(sm.toCSVString(sm.getBusVoltTable()));
	  	MonitorRecord rec1 = sm.getBusVoltTable().get("Bus2").get(1);
	  	MonitorRecord rec20 = sm.getBusVoltTable().get("Bus2").get(20);
	  	assertTrue(Math.abs(rec1.getValue()-rec20.getValue())<1.0E-4);
	}
	
	
	public DStabNetwork3Phase createFeeder() throws InterpssException{
		
	    DStabNetwork3Phase net = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
		
		
		// identify this is a distribution network
		net.setNetworkType(false);
		
		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PDStabBus("Bus1", net);
		bus1.setAttributes("69 kV feeder source", "");
		bus1.setBaseVoltage(69000.0);
		// set the bus to a non-generator bus
		bus1.setGenCode(AclfGenCode.SWING);
		// set the bus to a constant power load bus
		bus1.setLoadCode(AclfLoadCode.NON_LOAD);
		bus1.setVoltage(new Complex(1.01,0));
		
		DStabGen constantGen = DStabObjectFactory.createDStabGen();
		constantGen.setId("Source");
		constantGen.setMvaBase(100);
		constantGen.setPosGenZ(new Complex(0.0,0.05));
		constantGen.setNegGenZ(new Complex(0.0,0.05));
		constantGen.setZeroGenZ(new Complex(0.0,0.05));
		bus1.getContributeGenList().add(constantGen);
		
		
		EConstMachine mach = (EConstMachine)DStabObjectFactory.
				createMachine("MachId", "MachName", MachineType.ECONSTANT, net, "Bus1", "Source");
	
		mach.setRating(100, UnitType.mVA, net.getBaseKva());
		mach.setRatedVoltage(69000.0);
		mach.setH(50000.0);
		mach.setXd1(0.05);

		
	Bus3Phase bus2 = ThreePhaseObjectFactory.create3PDStabBus("Bus2", net);
		bus2.setAttributes("feeder bus 2", "");
		bus2.setBaseVoltage(12500.0);
		// set the bus to a non-generator bus
		bus2.setGenCode(AclfGenCode.NON_GEN);
		// set the bus to a constant power load bus
		bus2.setLoadCode(AclfLoadCode.CONST_P);
		
		
	Branch3Phase xfr1_2 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
		xfr1_2.setBranchCode(AclfBranchCode.XFORMER);
		xfr1_2.setToTurnRatio(1.02);
		xfr1_2.setZ( new Complex( 0.0, 0.04 ));
		//xfr1_2.setZabc(Complex3x3.createUnitMatrix().multiply(new Complex( 0.0, 0.04 )));
		//xfr1_2.setZ0( new Complex(0.0, 0.4 ));
	
	
	AcscXformer xfr0 = acscXfrAptr.apply(xfr1_2);
		xfr0.setFromConnectGroundZ(XfrConnectCode.DELTA11, new Complex(0.0,0.0), UnitType.PU);
		xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);

		
		
	Bus3Phase bus3 = ThreePhaseObjectFactory.create3PDStabBus("Bus3", net);
		bus3.setAttributes("feeder bus 3", "");
		bus3.setBaseVoltage(12500.0);
		// set the bus to a non-generator bus
		bus3.setGenCode(AclfGenCode.GEN_PQ);
		// set the bus to a constant power load bus
		bus3.setLoadCode(AclfLoadCode.CONST_P);
		
		
//		Gen3Phase gen1 = new Gen3PhaseImpl();
//		gen1.setParentBus(bus3);
//		gen1.setId("PVGen");
//		gen1.setGen(new Complex(0.5,0));  // total gen power, system mva based
//		
//		bus3.getThreePhaseGenList().add(gen1);
		
		
		Bus3Phase bus4 = ThreePhaseObjectFactory.create3PDStabBus("Bus4", net);
		bus4.setAttributes("feeder bus 4", "");
		bus4.setBaseVoltage(12500.0);
		// set the bus to a non-generator bus
		bus4.setGenCode(AclfGenCode.GEN_PQ);
		// set the bus to a constant power load bus
		bus4.setLoadCode(AclfLoadCode.CONST_P);
		
		
		Bus3Phase bus5 = ThreePhaseObjectFactory.create3PDStabBus("Bus5", net);
		bus5.setAttributes("feeder bus 5", "");
		bus5.setBaseVoltage(12500.0);
		// set the bus to a non-generator bus
		bus5.setGenCode(AclfGenCode.GEN_PQ);
		// set the bus to a constant power load bus
		bus5.setLoadCode(AclfLoadCode.CONST_P);
		
		
		Bus3Phase bus6 = ThreePhaseObjectFactory.create3PDStabBus("Bus6", net);
		bus6.setAttributes("feeder bus 6", "");
		bus6.setBaseVoltage(12500.0);
		// set the bus to a non-generator bus
		bus6.setGenCode(AclfGenCode.GEN_PQ);
		// set the bus to a constant power load bus
		bus6.setLoadCode(AclfLoadCode.CONST_P);
		
		
		for(int i =2;i<=6;i++){
			Bus3Phase loadBus = (Bus3Phase) net.getBus("Bus"+i);
			Load3Phase load1 = new Load3PhaseImpl();
			load1.set3PhaseLoad(new Complex3x1(new Complex(0.01,0.001),new Complex(0.01,0.001),new Complex(0.01,0.001)));
			loadBus.getThreePhaseLoadList().add(load1);
			
		}
		
		for(int i =2;i<6;i++){
			Branch3Phase Line2_3 = ThreePhaseObjectFactory.create3PBranch("Bus"+i, "Bus"+(i+1), "0", net);
			Line2_3.setBranchCode(AclfBranchCode.LINE);
			Complex3x3 zabcActual = this.getFeederZabc601().multiply(5.28);
			Double zbase = net.getBus("Bus"+i).getBaseVoltage()*net.getBus("Bus"+i).getBaseVoltage()/net.getBaseMva()/1.0E6;
			Line2_3.setZabc(zabcActual.multiply(1/zbase));
			
		}
		
		
		
		return net; 
		
		
		
	}
	
	//TODO  1 Mile = 5280 feets
	//ohms per 1000ft
	private Complex3x3 getFeederZabc601(){
		  Complex3x3 zabc= new Complex3x3();
		  zabc.aa = new Complex(0.0882,0.2074);
		  zabc.ab = new Complex(0.0312,0.0935);
		  zabc.ac = new Complex(0.0306,0.0760);
		  zabc.ba =  zabc.ab;
		  zabc.bb =  new Complex(0.0902, 0.2008);
		  zabc.bc =  new Complex(0.0316,0.0856);
		  zabc.ca =  zabc.ac;
		  zabc.cb =  zabc.bc;
		  zabc.cc =  new Complex(0.0890,0.2049);
		  
		  return zabc;
		  
	}
	
    private Complex3x3 getFeederYabc601(){
		  return new Complex3x3();
	}

}

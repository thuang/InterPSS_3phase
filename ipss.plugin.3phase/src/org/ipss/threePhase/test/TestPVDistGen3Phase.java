package org.ipss.threePhase.test;

import static com.interpss.core.funcImpl.AcscFunction.acscXfrAptr;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Gen3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.impl.Gen3PhaseImpl;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;

public class TestPVDistGen3Phase {
	
	@Test
	public void testPVDistGen3Phase() throws InterpssException{
		
		DStabNetwork3Phase distNet = createDistNetWithDG();
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		
		System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(distNet));
		
		// test the initGenPQ
		
		
		// TEST positiveSeqGenPQ
		
		
		// test the injectCurrent, check whether it can produce the same positive sequence genPQ
		
		
		
	}
	
	
	private DStabNetwork3Phase createDistNetWithDG() throws InterpssException{
		DStabNetwork3Phase net = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
		// identify this is a distribution network
		((DStabNetwork3Phase) net).setNetworkType(false);
		
		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PAclfBus("Bus1", net);
			bus1.setAttributes("69 kV feeder source", "");
			bus1.setBaseVoltage(69000.0);
			// set the bus to a non-generator bus
			bus1.setGenCode(AclfGenCode.SWING);
			// set the bus to a constant power load bus
			bus1.setLoadCode(AclfLoadCode.NON_LOAD);
			bus1.setVoltage(new Complex(1.01,0));

			
		Bus3Phase bus2 = ThreePhaseObjectFactory.create3PAclfBus("Bus2", net);
			bus2.setAttributes("13.8 V feeder bus 2", "");
			bus2.setBaseVoltage(13800.0);
			// set the bus to a non-generator bus
			bus2.setGenCode(AclfGenCode.NON_GEN);
			// set the bus to a constant power load bus
			bus2.setLoadCode(AclfLoadCode.CONST_P);
			
			Load3Phase load1 = new Load3PhaseImpl();
			load1.set3PhaseLoad(new Complex3x1(new Complex(1.5,0.1),new Complex(1.5,0.1),new Complex(1.5,0.1)));
			bus2.getThreePhaseLoadList().add(load1);

			
			
		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PAclfBus("Bus3", net);
			bus3.setAttributes("13.8 V feeder bus 3", "");
			bus3.setBaseVoltage(13800.0);
			// set the bus to a non-generator bus
			bus3.setGenCode(AclfGenCode.GEN_PQ);
			// set the bus to a constant power load bus
			bus3.setLoadCode(AclfLoadCode.NON_LOAD);
			
			
			Gen3Phase gen1 = new Gen3PhaseImpl();
			gen1.setParentBus(bus3);
			gen1.setGen(new Complex(0.5,0));  // total gen power, system mva based
			
			bus3.getThreePhaseGenList().add(gen1);
			
			gen1.setMvaBase(10); // for dynamic simulation only
			
			

			
			Branch3Phase xfr1_2 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
			xfr1_2.setBranchCode(AclfBranchCode.XFORMER);
			xfr1_2.setToTurnRatio(1.02);
			//xfr1_2.setZ( new Complex( 0.0, 0.04 ));
			xfr1_2.setZabc(Complex3x3.createUnitMatrix().multiply(new Complex( 0.0, 0.04 )));
			//xfr1_2.setZ0( new Complex(0.0, 0.4 ));
		
		
			AcscXformer xfr0 = acscXfrAptr.apply(xfr1_2);
			xfr0.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
			xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
			
			// for testing connection and from-to relationship only
	//		xfr0.setToConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
	//		xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
			
			Branch3Phase Line2_3 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus3", "0", net);
			Line2_3.setBranchCode(AclfBranchCode.LINE);
			Line2_3.setZ( new Complex( 0.0, 0.04 ));
			Line2_3.setZ0( new Complex(0.0, 0.08 ));
				
			
			
			
	    return  net;
	}

}



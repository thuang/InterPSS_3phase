package org.ipss.threePhase.test;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.impl.AclfNetwork3Phase;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.aclf.BaseAclfNetwork;
import com.interpss.core.net.Bus;

public class TestDistributionPowerflowAlgo {
	
	@Test
	public void testLineAndXfrGeneralizedMatrices() throws InterpssException {
		
		AclfNetwork3Phase net = createDistNet();
		
		// 1. Test the distribution line models
		Branch3Phase line2_4 = (Branch3Phase) net.getBranch("Bus2", "Bus4", "0");
		
		/*
		 *  [a] = U
		 */
		Complex3x3 a = line2_4.getToBusVabc2FromBusVabcMatrix();
		System.out.println("[a] ="+a.toString());
		assertTrue(Complex3x3.createUnitMatrix().subtract(a).abs()<1.0E-7);
		
		/*
		 *  [b] = Zabc
		 */
		Complex3x3 b = line2_4.getToBusIabc2FromBusVabcMatrix();
		System.out.println("[b] ="+b.toString());
		System.out.println("[Z120] ="+b.To120());
		assertTrue(line2_4.getZabc().subtract(b).abs()<1.0E-7);
		
		/*
		 * [c] = [0]
		 */
		Complex3x3 c = line2_4.getToBusVabc2FromBusIabcMatrix();
		System.out.println("[c] ="+c.toString());
		
		assertTrue(new Complex3x3().subtract(c).abs()<1.0E-7);
		
		/*
		 * [d] = U
		 */
		Complex3x3 d = line2_4.getToBusIabc2FromBusIabcMatrix();
		System.out.println("[d] ="+d.toString());
		assertTrue(Complex3x3.createUnitMatrix().subtract(d).abs()<1.0E-7);
		
		
		/*
		 * [A] = U
		 * 
		 */
		Complex3x3 A = line2_4.getFromBusVabc2ToBusVabcMatrix();
		System.out.println("[A] ="+A.toString());
		
		assertTrue(Complex3x3.createUnitMatrix().subtract(A).abs()<1.0E-7);
		
		/*
		 * [B] = Zabc
		 */
		Complex3x3 B = line2_4.getToBusIabc2ToBusVabcMatrix();
		System.out.println("[B] ="+B.toString());
		assertTrue(line2_4.getZabc().subtract(B).abs()<1.0E-7);
		
		
		//2. Test the transformer models
		
	}
	
	
	//@Test
	public void testDistBusOrdering() throws InterpssException {
		AclfNetwork3Phase net = createDistNet();
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(net);
		distPFAlgo.orderDistributionBuses(true);
		
		for(Bus bus:net.getBusList()){
			System.out.println("sortNum of Bus - "+bus.getId()+" is "+bus.getSortNumber());
		}
		
	}
	
	
	@Test
	public void testDistBusPF() throws InterpssException {
		AclfNetwork3Phase net = createDistNet();
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(net);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		
		for(AclfBus bus:net.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			System.out.println("Vabc of bus -"+bus3P.getId()+","+bus3P.get3PhaseVotlages().toString());
		}
		
	}
	
	private AclfNetwork3Phase createDistNet() throws InterpssException{
		BaseAclfNetwork net = new AclfNetwork3Phase();
		// identify this is a distribution network
		((AclfNetwork3Phase) net).setNetworkType(false);
		
		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PAclfBus("Bus1", net);
  		bus1.setAttributes("13.8 kV feeder source", "");
  		bus1.setBaseVoltage(13800.0);
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
  		bus2.setLoadCode(AclfLoadCode.NON_LOAD);
  		
  		
		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PAclfBus("Bus3", net);
  		bus3.setAttributes("13.8 V feeder bus 3", "");
  		bus3.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus3.setGenCode(AclfGenCode.NON_GEN);
  		// set the bus to a constant power load bus
  		bus3.setLoadCode(AclfLoadCode.CONST_P);
  		Load3Phase load1 = new Load3PhaseImpl();
  		load1.set3PhaseLoad(new Complex3x1(new Complex(0.5,0.1),new Complex(0.5,0.1),new Complex(0.5,0.1)));
  		bus3.getThreePhaseLoadList().add(load1);
  		//bus3.setLoadPQ(new Complex(0.5,0.1));
  		
  		
		Bus3Phase bus4 = ThreePhaseObjectFactory.create3PAclfBus("Bus4", net);
  		bus4.setAttributes("13.8 V feeder bus 4", "");
  		bus4.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus4.setGenCode(AclfGenCode.NON_GEN);
  		// set the bus to a constant power load bus
  		bus4.setLoadCode(AclfLoadCode.CONST_P);

  		//bus4.setLoadPQ(new Complex(1,0.1));
  		
  		Load3Phase load2 = new Load3PhaseImpl();
  		load2.set3PhaseLoad(new Complex3x1(new Complex(1,0.1),new Complex(1,0.1),new Complex(1,0.1)));
  		bus4.getThreePhaseLoadList().add(load2);
  		
  		
		Branch3Phase Line1_2 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
		Line1_2.setBranchCode(AclfBranchCode.LINE);
		Line1_2.setZ( new Complex( 0.0, 0.04 ));
		Line1_2.setZ0( new Complex(0.0, 0.08 ));
		
		Branch3Phase Line2_3 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus3", "0", net);
		Line2_3.setBranchCode(AclfBranchCode.LINE);
		Line2_3.setZ( new Complex( 0.0, 0.04 ));
		Line2_3.setZ0( new Complex(0.0, 0.08 ));
  		
		
		Branch3Phase Line2_4 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus4", "0", net);
		Line2_4.setBranchCode(AclfBranchCode.LINE);
		Line2_4.setZ( new Complex( 0.0, 0.04 ));
		Line2_4.setZ0( new Complex(0.0, 0.08 ));
  		
  		
  		
		//////////////////transformers///////////////////////////////////////////
  		
//		Branch3Phase xfr5_10 = ThreePhaseObjectFactory.create3PBranch("Bus5", "Bus10", "0", dsNet);
//		xfr5_10.setBranchCode(AclfBranchCode.XFORMER);
//		xfr5_10.setZ( new Complex( 0.0, 0.08 ));
//		xfr5_10.setZ0( new Complex(0.0, 0.08 ));
//		
//		
//		AcscXformer xfr0 = acscXfrAptr.apply(xfr5_10);
//		xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//  		
//  		
//		Branch3Phase xfr10_11 = ThreePhaseObjectFactory.create3PBranch("Bus10", "Bus11", "0", dsNet);
//		xfr10_11.setBranchCode(AclfBranchCode.XFORMER);
//		xfr10_11.setZ( new Complex( 0.0, 0.06 ));
//		xfr10_11.setZ0( new Complex(0.0, 0.06 ));
//		
//		AcscXformer xfr1 = acscXfrAptr.apply(xfr10_11);
//		xfr1.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
//		xfr1.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		
//	    
//		
//		Branch3Phase xfr11_12 = ThreePhaseObjectFactory.create3PBranch("Bus11", "Bus12", "0", dsNet);
//		xfr11_12.setBranchCode(AclfBranchCode.XFORMER);
//		xfr11_12.setZ( new Complex( 0.0, 0.025 ));
//		xfr11_12.setZ0( new Complex(0.0, 0.025 ));
//		xfr11_12.setToTurnRatio(1.01);
//		AcscXformer xfr2 = acscXfrAptr.apply(xfr11_12);
//		xfr2.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		xfr2.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);

  		
  		
  		
	    return (AclfNetwork3Phase) net;
  		
	}
	
	private AclfNetwork3Phase createNet4PosSeqPF() throws InterpssException{
		BaseAclfNetwork net = new AclfNetwork3Phase();
		// identify this is a distribution network
		((AclfNetwork3Phase) net).setNetworkType(false);
		
		Bus3Phase bus1 = ThreePhaseObjectFactory.create3PAclfBus("Bus1", net);
  		bus1.setAttributes("13.8 kV feeder source", "");
  		bus1.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus1.setGenCode(AclfGenCode.SWING);
  		// set the bus to a constant power load bus
  		bus1.setLoadCode(AclfLoadCode.NON_LOAD);

  		
		Bus3Phase bus2 = ThreePhaseObjectFactory.create3PAclfBus("Bus2", net);
  		bus2.setAttributes("13.8 V feeder bus 2", "");
  		bus2.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus2.setGenCode(AclfGenCode.NON_GEN);
  		// set the bus to a constant power load bus
  		bus2.setLoadCode(AclfLoadCode.NON_LOAD);
  		
  		
		Bus3Phase bus3 = ThreePhaseObjectFactory.create3PAclfBus("Bus3", net);
  		bus3.setAttributes("13.8 V feeder bus 3", "");
  		bus3.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus3.setGenCode(AclfGenCode.NON_GEN);
  		// set the bus to a constant power load bus
  		bus3.setLoadCode(AclfLoadCode.CONST_P);
//  		Load3Phase load1 = new Load3PhaseImpl();
//  		load1.set3PhaseLoad(new Complex3x1(new Complex(0.5,0.1),new Complex(0.5,0.1),new Complex(0.5,0.1)));
//  		bus3.getThreePhaseLoadList().add(load1);
  		//bus3.setLoadPQ(new Complex(0.5,0.1));
  		
  		
		Bus3Phase bus4 = ThreePhaseObjectFactory.create3PAclfBus("Bus4", net);
  		bus4.setAttributes("13.8 V feeder bus 4", "");
  		bus4.setBaseVoltage(13800.0);
  		// set the bus to a non-generator bus
  		bus4.setGenCode(AclfGenCode.NON_GEN);
  		// set the bus to a constant power load bus
  		bus4.setLoadCode(AclfLoadCode.CONST_P);

  		bus4.setLoadPQ(new Complex(1,0.1));
  		
//  		Load3Phase load2 = new Load3PhaseImpl();
//  		load2.set3PhaseLoad(new Complex3x1(new Complex(1,0.1),new Complex(1,0.1),new Complex(1,0.1)));
//  		bus4.getThreePhaseLoadList().add(load2);
  		
  		
		Branch3Phase Line1_2 = ThreePhaseObjectFactory.create3PBranch("Bus1", "Bus2", "0", net);
		Line1_2.setBranchCode(AclfBranchCode.LINE);
		Line1_2.setZ( new Complex( 0.0, 0.04 ));
		Line1_2.setZ0( new Complex(0.0, 0.08 ));
		
		Branch3Phase Line2_3 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus3", "0", net);
		Line2_3.setBranchCode(AclfBranchCode.LINE);
		Line2_3.setZ( new Complex( 0.0, 0.04 ));
		Line2_3.setZ0( new Complex(0.0, 0.08 ));
  		
		
		Branch3Phase Line2_4 = ThreePhaseObjectFactory.create3PBranch("Bus2", "Bus4", "0", net);
		Line2_4.setBranchCode(AclfBranchCode.LINE);
		Line2_4.setZ( new Complex( 0.0, 0.04 ));
		Line2_4.setZ0( new Complex(0.0, 0.08 ));
  		
  		
  		
		//////////////////transformers///////////////////////////////////////////
  		
//		Branch3Phase xfr5_10 = ThreePhaseObjectFactory.create3PBranch("Bus5", "Bus10", "0", dsNet);
//		xfr5_10.setBranchCode(AclfBranchCode.XFORMER);
//		xfr5_10.setZ( new Complex( 0.0, 0.08 ));
//		xfr5_10.setZ0( new Complex(0.0, 0.08 ));
//		
//		
//		AcscXformer xfr0 = acscXfrAptr.apply(xfr5_10);
//		xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//  		
//  		
//		Branch3Phase xfr10_11 = ThreePhaseObjectFactory.create3PBranch("Bus10", "Bus11", "0", dsNet);
//		xfr10_11.setBranchCode(AclfBranchCode.XFORMER);
//		xfr10_11.setZ( new Complex( 0.0, 0.06 ));
//		xfr10_11.setZ0( new Complex(0.0, 0.06 ));
//		
//		AcscXformer xfr1 = acscXfrAptr.apply(xfr10_11);
//		xfr1.setFromConnectGroundZ(XfrConnectCode.DELTA, new Complex(0.0,0.0), UnitType.PU);
//		xfr1.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		
//	    
//		
//		Branch3Phase xfr11_12 = ThreePhaseObjectFactory.create3PBranch("Bus11", "Bus12", "0", dsNet);
//		xfr11_12.setBranchCode(AclfBranchCode.XFORMER);
//		xfr11_12.setZ( new Complex( 0.0, 0.025 ));
//		xfr11_12.setZ0( new Complex(0.0, 0.025 ));
//		xfr11_12.setToTurnRatio(1.01);
//		AcscXformer xfr2 = acscXfrAptr.apply(xfr11_12);
//		xfr2.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
//		xfr2.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);

  		
  		
  		
	    return (AclfNetwork3Phase) net;
  		
	}

}

package org.ipss.threePhase.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.interpss.IpssCorePlugin;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.data_parser.OpenDSSDataParser;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;

import com.interpss.core.aclf.AclfBus;

public class TestIEEETestFeederPowerFlow {

	
	@Test
	public void testIEEE123BusPowerflow(){
		
		IpssCorePlugin.init();
		IpssCorePlugin.setLoggerLevel(Level.INFO);
		
		OpenDSSDataParser parser = new OpenDSSDataParser();
		parser.parseFeederData("testData\\feeder\\IEEE123","IEEE123Master_Modified.dss");
		
		parser.calcVoltageBases();
		
		double mvaBase = 1.0;
		parser.convertActualValuesToPU(1.0);
		
		DStabNetwork3Phase distNet = parser.getDistNetwork();
		
		String netStrFileName = "testData\\feeder\\IEEE123\\ieee123_modified_netString.dat";
		try {
			Files.write(Paths.get(netStrFileName), parser.getDistNetwork().net2String().getBytes());
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(distNet);
		//distPFAlgo.orderDistributionBuses(true);
		distPFAlgo.setInitBusVoltageEnabled(true);
		//distPFAlgo.setMaxIteration(1);
		distPFAlgo.setTolerance(1.0E-2);
		assertTrue(distPFAlgo.powerflow());
		
		/*
		 *  Vabc of bus -Bus1,1.0100 + j0.0000  -0.5050 + j-0.87469  -0.5050 + j0.87469
			Vabc of bus -Bus2,0.99636 + j-0.05941  -0.54963 + j-0.83317  -0.44673 + j0.89258
			Vabc of bus -Bus3,0.99075 + j-0.07914  -0.56392 + j-0.81844  -0.42683 + j0.89759
			Vabc of bus -Bus4,0.98834 + j-0.09907  -0.57997 + j-0.80639  -0.40837 + j0.90546
		 */
		for(AclfBus bus:distNet.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			System.out.println("Vabc of bus -"+bus3P.getId()+","+bus3P.get3PhaseVotlages().toString());
		}
		
		System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(distNet));
		
	}
}

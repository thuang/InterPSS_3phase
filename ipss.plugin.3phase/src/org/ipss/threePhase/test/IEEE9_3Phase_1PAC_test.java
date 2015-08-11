package org.ipss.threePhase.test;

import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.apache.commons.math3.complex.Complex;
import org.ieee.odm.adapter.IODMAdapter.NetType;
import org.ieee.odm.adapter.psse.PSSEAdapter;
import org.ieee.odm.adapter.psse.PSSEAdapter.PsseVersion;
import org.ieee.odm.model.dstab.DStabModelParser;
import org.interpss.IpssCorePlugin;
import org.interpss.display.AclfOutFunc;
import org.interpss.numeric.util.PerformanceTimer;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.algo.DStab3PhaseSolverImpl;
import org.ipss.threePhase.dynamic.algo.DynamicEventProcessor3Phase;
import org.ipss.threePhase.dynamic.model.impl.SinglePhaseACMotor;
import org.ipss.threePhase.odm.ODM3PhaseDStabParserMapper;
import org.ipss.threePhase.util.ThreePhaseAclfOutFunc;
import org.junit.Test;

import com.interpss.DStabObjectFactory;
import com.interpss.SimuObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.acsc.fault.SimpleFaultCode;
import com.interpss.core.algo.LoadflowAlgorithm;
import com.interpss.dstab.algo.DynamicSimuAlgorithm;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.simu.SimuContext;
import com.interpss.simu.SimuCtxType;

public class IEEE9_3Phase_1PAC_test {
	
	@Test
	public void test_IEEE9_1pac_Dstab() throws InterpssException{
		IpssCorePlugin.init();
		IpssCorePlugin.setLoggerLevel(Level.INFO);
		PSSEAdapter adapter = new PSSEAdapter(PsseVersion.PSSE_30);
		assertTrue(adapter.parseInputFile(NetType.DStabNet, new String[]{
				"testData/IEEE9Bus/ieee9.raw",
				"testData/IEEE9Bus/ieee9.seq",
				//"testData/IEEE9Bus/ieee9_dyn_onlyGen_saturation.dyr"
				"testData/IEEE9Bus/ieee9_dyn_onlyGen.dyr"
		}));
		DStabModelParser parser =(DStabModelParser) adapter.getModel();
		
		//System.out.println(parser.toXmlDoc());

		
		
		SimuContext simuCtx = SimuObjectFactory.createSimuNetwork(SimuCtxType.DSTABILITY_NET);
		
		// The only change to the normal data import is the use of ODM3PhaseDStabParserMapper
		if (!new ODM3PhaseDStabParserMapper(IpssCorePlugin.getMsgHub())
					.map2Model(parser, simuCtx)) {
			System.out.println("Error: ODM model to InterPSS SimuCtx mapping error, please contact support@interpss.com");
			return;
		}
		
		
	    DStabNetwork3Phase dsNet =(DStabNetwork3Phase) simuCtx.getDStabilityNet();
	    
	    
	    /*
	     *   create the 1-phase AC model 
	     */
		
		Bus3Phase bus5 = (Bus3Phase) dsNet.getBus("Bus5");
		
	    SinglePhaseACMotor ac1 = new SinglePhaseACMotor(bus5,"1");
  		ac1.setLoadPercent(50);
  		ac1.setPhase(Phase.A);
  		ac1.setMVABase(25);
  		bus5.getPhaseADynLoadList().add(ac1);
  		
  		
  		
  		SinglePhaseACMotor ac2 = new SinglePhaseACMotor(bus5,"2");
  		ac2.setLoadPercent(50);
  		ac2.setPhase(Phase.B);
  		ac2.setMVABase(25);
  		bus5.getPhaseBDynLoadList().add(ac2);
  		

  		
  		SinglePhaseACMotor ac3 = new SinglePhaseACMotor(bus5,"3");
  		ac3.setLoadPercent(50);
  		ac3.setPhase(Phase.C);
  		ac3.setMVABase(25);
  		bus5.getPhaseCDynLoadList().add(ac3);
	    
	    
	    
		DynamicSimuAlgorithm dstabAlgo = simuCtx.getDynSimuAlgorithm();
		LoadflowAlgorithm aclfAlgo = dstabAlgo.getAclfAlgorithm();
		assertTrue(aclfAlgo.loadflow());
		System.out.println(AclfOutFunc.loadFlowSummary(dsNet));
		
		
		dstabAlgo.setSimuMethod(DynamicSimuMethod.MODIFIED_EULER);
		dstabAlgo.setSimuStepSec(0.005d);
		dstabAlgo.setTotalSimuTimeSec(0.2);
		
		dstabAlgo.setSolver( new DStab3PhaseSolverImpl(dstabAlgo, IpssCorePlugin.getMsgHub()));

		//dstabAlgo.setRefMachine(dsNet.getMachine("Bus1-mach1"));
		
		//applied the event
		dsNet.addDynamicEvent(DStabObjectFactory.createBusFaultEvent("Bus9",dsNet,SimpleFaultCode.GROUND_3P,new Complex(0.0),null,0.01d,0.05),"3phaseFault@Bus5");
        
		
		StateMonitor sm = new StateMonitor();
		sm.addGeneratorStdMonitor(new String[]{"Bus1-mach1","Bus2-mach1","Bus3-mach1"});
		sm.addBusStdMonitor(new String[]{"Bus5","Bus4","Bus1"});
		
		// set the output handler
		dstabAlgo.setSimuOutputHandler(sm);
		dstabAlgo.setOutPutPerSteps(5);
		//dstabAlgo.setRefMachine(dsNet.getMachine("Bus1-mach1"));
		
		IpssLogger.getLogger().setLevel(Level.WARNING);
		
		PerformanceTimer timer = new PerformanceTimer(IpssLogger.getLogger());
		
        // Must use this dynamic event process to modify the YMatrixABC
		dstabAlgo.setDynamicEventHandler(new DynamicEventProcessor3Phase());
		
		if (dstabAlgo.initialization()) {
			System.out.println(ThreePhaseAclfOutFunc.busLfSummary(dsNet));
			
			System.out.println(dsNet.getMachineInitCondition());
			
			System.out.println("Running 3Phase DStab simulation ...");
			timer.start();
			//dstabAlgo.performSimulation();
			
			while(dstabAlgo.getSimuTime()<=dstabAlgo.getTotalSimuTimeSec()){
				
				dstabAlgo.solveDEqnStep(true);}
		}
		
		System.out.println(sm.toCSVString(sm.getBusAngleTable()));
		System.out.println(sm.toCSVString(sm.getBusVoltTable()));
		
	}

}

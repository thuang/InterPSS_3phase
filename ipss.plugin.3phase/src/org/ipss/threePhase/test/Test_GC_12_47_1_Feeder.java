package org.ipss.threePhase.test;

import static com.interpss.core.funcImpl.AcscFunction.acscXfrAptr;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math3.complex.Complex;
import org.interpss.IpssCorePlugin;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Load3Phase;
import org.ipss.threePhase.basic.Phase;
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.dataParser.opendss.OpenDSSDataParser;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.dynamic.algo.DynamicEventProcessor3Phase;
import org.ipss.threePhase.dynamic.model.InductionMotor3PhaseAdapter;
import org.ipss.threePhase.dynamic.model.impl.SinglePhaseACMotor;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
import org.ipss.threePhase.util.ThreePhaseAclfOutFunc;
import org.ipss.threePhase.util.ThreePhaseObjectFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.interpss.DStabObjectFactory;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranchCode;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGenCode;
import com.interpss.core.aclf.AclfLoadCode;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.adpter.AcscXformer;
import com.interpss.core.net.NetworkType;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.algo.DynamicSimuAlgorithm;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.cache.StateMonitor;
import com.interpss.dstab.cache.StateMonitor.DynDeviceType;
import com.interpss.dstab.dynLoad.InductionMotor;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;

public class Test_GC_12_47_1_Feeder {
	//@Test
	public void testPowerflow(){
		
		 IpssCorePlugin.init();
		 DStabNetwork3Phase net = null;
		try {
			net = createTestFeeder(12470,480,28,3,"",1.0);
		} catch (InterpssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(net.net2String());
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(net);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		
		/*
		 *  Vabc of bus -Bus1,1.0100 + j0.0000  -0.5050 + j-0.87469  -0.5050 + j0.87469
			Vabc of bus -Bus2,0.99636 + j-0.05941  -0.54963 + j-0.83317  -0.44673 + j0.89258
			Vabc of bus -Bus3,0.99075 + j-0.07914  -0.56392 + j-0.81844  -0.42683 + j0.89759
			Vabc of bus -Bus4,0.98834 + j-0.09907  -0.57997 + j-0.80639  -0.40837 + j0.90546
		 */
		
		for(AclfBus bus:net.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			System.out.println("Vabc of bus -"+bus3P.getId()+","+bus3P.get3PhaseVotlages().toString());
		}
		
		System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(net));
	}
	
	//@Test
	public void test3PhDynamic() throws InterpssException{
		
		 IpssCorePlugin.init();
		 DStabNetwork3Phase net = null;
		try {
			net = createTestFeeder(12470,480,28,3,"",100.0);
		} catch (InterpssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Add the dyanmic machine to the source Bus
		//TODO The contributeGen has to be enter before running power flow, otherwise it will not be properly initialized
		Bus3Phase node_28 = (Bus3Phase) net.getBus("node_28");
		
		DStabGen constantGen = DStabObjectFactory.createDStabGen();
		constantGen.setId("Source");
		constantGen.setMvaBase(100);
		constantGen.setPosGenZ(new Complex(0.0,0.05));
		constantGen.setNegGenZ(new Complex(0.0,0.05));
		constantGen.setZeroGenZ(new Complex(0.0,1.0E9));
		node_28.getContributeGenList().add(constantGen);
		
		
		
		EConstMachine mach = (EConstMachine)DStabObjectFactory.
				createMachine("MachId", "MachName", MachineType.ECONSTANT, net, "node_28", "Source");
	
		mach.setRating(100, UnitType.mVA, net.getBaseKva());
		mach.setRatedVoltage(12470);
		mach.setH(500.0);
		mach.setXd1(0.05);
		//System.out.println(net.net2String());
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(net);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		

		for(AclfBus bus:net.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			System.out.println("Vabc of bus -"+bus3P.getId()+","+bus3P.get3PhaseVotlages().toString());
		}
		
		//System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(net));
		

		
		DynamicSimuAlgorithm dstabAlgo =DStabObjectFactory.createDynamicSimuAlgorithm(net, IpssCorePlugin.getMsgHub());
			
	 
	  	dstabAlgo.setSimuMethod(DynamicSimuMethod.MODIFIED_EULER);
		dstabAlgo.setSimuStepSec(0.005d);
		dstabAlgo.setTotalSimuTimeSec(.005);
	    //dstabAlgo.setRefMachine(net.getMachine("Bus3-mach1"));
		//distNet.addDynamicEvent(DStabObjectFactory.createBusFaultEvent("150r", distNet, SimpleFaultCode.GROUND_LG,new Complex(0,0.0),new Complex(0,0.0), 0.5,0.07), "SLG@Bus1");
        
		
		StateMonitor sm = new StateMonitor();
		//sm.addGeneratorStdMonitor(new String[]{"Bus1-mach1","Bus2-mach1"});
		sm.addBusStdMonitor(new String[]{"node_7","node_8","node_9","node_28","node_27"});
		sm.add3PhaseBusStdMonitor(new String[]{"node_7","node_8","node_9","node_28","node_27"});
		
//		for(String acMotorId: acMotorIds)
//		    sm.addDynDeviceMonitor(DynDeviceType.ACMotor, acMotorId);
//		
		// set the output handler
		dstabAlgo.setSimuOutputHandler(sm);
		dstabAlgo.setOutPutPerSteps(1);
		
		dstabAlgo.setDynamicEventHandler(new DynamicEventProcessor3Phase());
				
	  	if(dstabAlgo.initialization()){
	  		System.out.println(ThreePhaseAclfOutFunc.busLfSummary(net));
	  		System.out.println(net.getMachineInitCondition());
	  		
	  		for(String busId: sm.getBusPhAVoltTable().keySet()){
				
				 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)net.getBus(busId)).get3PhaseVotlages());
			}
	  	    double vsag = 0.4;
	  		//dstabAlgo.performSimulation();
	  		while(dstabAlgo.getSimuTime()<=dstabAlgo.getTotalSimuTimeSec()){
				
				dstabAlgo.solveDEqnStep(true);
				
				for(String busId: sm.getBusPhAVoltTable().keySet()){
					
					 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)net.getBus(busId)).get3PhaseVotlages());
				}
				
				
				if(dstabAlgo.getSimuTime()>0.5 && dstabAlgo.getSimuTime()<0.5833){
					mach.setE(vsag);
				}
				else if (dstabAlgo.getSimuTime()>=0.6){
					mach.setE(1.0);
				}
			}
	  	}
	  	
	  	System.out.println(sm.toCSVString(sm.getBusPhAVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhBVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhCVoltTable()));
	}
	
	
	@Test
	public void test3PhDynamic_WithBuidingLoad() throws InterpssException{
		 IpssCorePlugin.init();
		 DStabNetwork3Phase net = null;
		try {
			net = createTestFeeder(12470,480,28,3,"",100.0);
		} catch (InterpssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Add the dyanmic machine to the source Bus
		//TODO The contributeGen has to be enter before running power flow, otherwise it will not be properly initialized
		Bus3Phase node_28 = (Bus3Phase) net.getBus("node_28");
		
		DStabGen constantGen = DStabObjectFactory.createDStabGen();
		constantGen.setId("Source");
		constantGen.setMvaBase(100);
		constantGen.setPosGenZ(new Complex(0.0,0.05));
		constantGen.setNegGenZ(new Complex(0.0,0.05));
		constantGen.setZeroGenZ(new Complex(0.0,1.0E9));
		node_28.getContributeGenList().add(constantGen);
		
		
		
		EConstMachine mach = (EConstMachine)DStabObjectFactory.
				createMachine("MachId", "MachName", MachineType.ECONSTANT, net, "node_28", "Source");
	
		mach.setRating(100, UnitType.mVA, net.getBaseKva());
		mach.setRatedVoltage(12470);
		mach.setH(500.0);
		mach.setXd1(0.05);
		//System.out.println(net.net2String());
		
		DistributionPowerFlowAlgorithm distPFAlgo = ThreePhaseObjectFactory.createDistPowerFlowAlgorithm(net);
		//distPFAlgo.orderDistributionBuses(true);
		
		assertTrue(distPFAlgo.powerflow());
		

		for(AclfBus bus:net.getBusList()){
			Bus3Phase bus3P = (Bus3Phase) bus;
			System.out.println("Vabc of bus -"+bus3P.getId()+","+bus3P.get3PhaseVotlages().toString());
		}
		
		//System.out.println(DistPowerFlowOutFunc.powerflowResultSummary(net));
		

		
		DynamicSimuAlgorithm dstabAlgo =DStabObjectFactory.createDynamicSimuAlgorithm(net, IpssCorePlugin.getMsgHub());
			
	 
	  	dstabAlgo.setSimuMethod(DynamicSimuMethod.MODIFIED_EULER);
		dstabAlgo.setSimuStepSec(0.005d);
		dstabAlgo.setTotalSimuTimeSec(.1);
	    //dstabAlgo.setRefMachine(net.getMachine("Bus3-mach1"));
		//distNet.addDynamicEvent(DStabObjectFactory.createBusFaultEvent("150r", distNet, SimpleFaultCode.GROUND_LG,new Complex(0,0.0),new Complex(0,0.0), 0.5,0.07), "SLG@Bus1");
        
		
		StateMonitor sm = new StateMonitor();
		//sm.addGeneratorStdMonitor(new String[]{"Bus1-mach1","Bus2-mach1"});
		sm.addBusStdMonitor(new String[]{"node_7","node_8","node_9","node_28","node_27"});
		sm.add3PhaseBusStdMonitor(new String[]{"node_7","node_8","node_9","node_28","node_27"});
		
//		for(String acMotorId: acMotorIds)
//		    sm.addDynDeviceMonitor(DynDeviceType.ACMotor, acMotorId);
//		
		// set the output handler
		dstabAlgo.setSimuOutputHandler(sm);
		dstabAlgo.setOutPutPerSteps(1);
		
		dstabAlgo.setDynamicEventHandler(new DynamicEventProcessor3Phase());
				
	  	if(dstabAlgo.initialization()){
	  		System.out.println(ThreePhaseAclfOutFunc.busLfSummary(net));
	  		System.out.println(net.getMachineInitCondition());
	  		
	  		for(String busId: sm.getBusPhAVoltTable().keySet()){
				
				 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)net.getBus(busId)).get3PhaseVotlages());
			}
	  	    double vsag = 0.4;
	  		//dstabAlgo.performSimulation();
	  		while(dstabAlgo.getSimuTime()<=dstabAlgo.getTotalSimuTimeSec()){
				
				dstabAlgo.solveDEqnStep(true);
				
				for(String busId: sm.getBusPhAVoltTable().keySet()){
					
					 sm.addBusPhaseVoltageMonitorRecord( busId,dstabAlgo.getSimuTime(), ((Bus3Phase)net.getBus(busId)).get3PhaseVotlages());
				}
				
				
				if(dstabAlgo.getSimuTime()>0.5 && dstabAlgo.getSimuTime()<0.5833){
					mach.setE(vsag);
				}
				else if (dstabAlgo.getSimuTime()>=0.6){
					mach.setE(1.0);
				}
			}
	  	}
	  	
	  	System.out.println(sm.toCSVString(sm.getBusPhAVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhBVoltTable()));
	  	System.out.println(sm.toCSVString(sm.getBusPhCVoltTable()));
	}
	
	
	private DStabNetwork3Phase createTestFeeder(double primaryVolt, double secondaryVolt, int busNum, int loadNum, String feederPrefix, double mvaBase) throws InterpssException{
	    DStabNetwork3Phase net = ThreePhaseObjectFactory.create3PhaseDStabNetwork();
	    net.setNetworkType(NetworkType.DISTRIBUTION);
	    net.setBaseKva(mvaBase*1000.0);
	    
		String nodePrefix = "node:GC-12-47-1_";
		Hashtable<String, Complex3x3> lineZTable = new Hashtable<>();
		
		//read the branch impedance data
		try {	
	         File inputFile = new File("testData/feeder/PNNL_taxonomy_feeder/GC_12_47_1/GC-12.47-1_impedance_dump.xml");
	         DocumentBuilderFactory dbFactory 
	            = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.parse(inputFile);
	         doc.getDocumentElement().normalize();
	         System.out.println("Root element :" 
	            + doc.getDocumentElement().getNodeName());
	         NodeList nList = doc.getElementsByTagName("underground_line");
	         System.out.println("----------------------------");
	         
	         String from_node="",to_node="";
	         
	         for (int temp = 0; temp < nList.getLength(); temp++) {
	            Node nNode = nList.item(temp);
	            System.out.println("\nCurrent Element :" + nNode.getNodeName());
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	               Element eElement = (Element) nNode;
	               from_node = eElement.getElementsByTagName("from_node").item(0).getTextContent();
	               to_node = eElement.getElementsByTagName("to_node").item(0).getTextContent();
	               
//	               System.out.println("from node : "  + from_node);
//	               System.out.println("to node : " + to_node);
//	               System.out.println("b_matrix : ");
	               
	               NodeList bMatrixList = eElement.getElementsByTagName("b_matrix").item(0).getChildNodes();
	               
	               Complex[][] bMatrixAry = new Complex[3][3];
	               int m = 0;
	               int n = 0;
	               for (int i = 0; i<bMatrixList.getLength();i++){
	            	   Node  bMatrixEle = bMatrixList.item(i);
	            	   
	            	   if (bMatrixEle.getNodeType() == Node.ELEMENT_NODE) {
	    	               Element bMtxElement = (Element) bMatrixEle;
	    	               
	    	               String zstr0 = bMtxElement.getTextContent().trim();
                           String zstr =zstr0.substring(1,zstr0.length()-1);
                           double re = 0.0;
                           double im = 0.0;
                           
                           if(zstr.contains("+")){
                        	   String[] zstrAry = zstr.split("\\+");
                        	   re = Double.valueOf(zstrAry[0]);
                        	   im = Double.valueOf(zstrAry[1]);
                           }
                           else{
                        	   String[] zstrAry = zstr.split("\\-");
                        	   re = Double.valueOf(zstrAry[0]);
                        	   im = -Double.valueOf(zstrAry[1]);
                           }
	    	              
                           Complex zmn = new Complex(re,im);
	    	               
	    	               bMatrixAry[m][n] = zmn;
	    	               
	    	              // System.out.println(m+","+n+","+zstr0+","+zmn.toString());
	    	               
	    	               n++;
	    	               if(n==3){
	    	            	   n=0;
	    	            	   m++;
	    	               }
	    	               
	    	               
	    	               
	            	   }
	            	   
	               }
	               
	               // add Z to the lineZTable
	               from_node = from_node.replace(nodePrefix, "");
	               to_node = to_node.replace(nodePrefix, "");
	               String lineId = from_node+":"+to_node;
	               
	               lineZTable.put(lineId, new Complex3x3(bMatrixAry));
	        
	            }
	            
	         }
	         
//	         System.out.println(lineZTable.toString());
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
		
			


		String[] lineNodes = { //"node28_meter4", 
				               "meter_4:node_27",
				               "node_26:node_27",
				               "node_26:node_1",
				               "node_1:node_2",
				               "node_2:node_3",
				               "node_3:node_24",
				               "node_3:node_4",
				               "node_4:node_5",
				               "node_5:node_6",
				               "node_6:node_21",
				               "node_20:node_21",
				               "node_16:node_20",
				               "node_16:node_17",
				               "node_9:node_17",
				              // "meter_2:node_9",
				               "node_15:node_16",
				               "node_8:node_15",
				               //"meter_1:node_8",
				               "node_16:node_19",
				               "node_18:node_19",
				               "node_14:node_18",
				               "node_13:node_14",
				               "node_11:node_13",
				               "node_11:node_12",
				               "node_7:node_12",
				              // "meter_3:node_7",
				               "node_11:node_22",
				               "node_22:node_23",
				               "node_10:node_23",
				               "node_10:node_25",
				               };
		String[] xfrNodes = {"meter_1:node_8", "meter_2:node_9","meter_3:node_7","meter_4:node_28"};
		
		List<String> fuseList = Arrays.asList("node_11:node_12","node_15:node_16","node_16:node_17") ;
		List<String> switchList = Arrays.asList("node_10:node_25","node_22:node_23","node_13:node_14","node_18:node_19","node_20:node_21") ;
		
		
		for(int i =1;i<=busNum;i++){
			Bus3Phase bus = ThreePhaseObjectFactory.create3PDStabBus("node_"+i, net);
			bus.setAttributes("feeder bus "+i, "");
			bus.setBaseVoltage(primaryVolt);
			// set the bus to a non-generator bus
			bus.setGenCode(AclfGenCode.NON_GEN);
			// set the bus to a constant power load bus
			bus.setLoadCode(AclfLoadCode.NON_LOAD);
			
		
			//capacitor
			if(i ==21){
				Load3Phase Shuntload = new Load3PhaseImpl();
				
				Complex3x1 shuntY = new Complex3x1(new Complex(0,-600),new Complex(0.0,-600),new Complex(0.0,-600));
				Shuntload.set3PhaseLoad(shuntY);
				// not used in GridLAB-D
//				bus.getThreePhaseLoadList().add(Shuntload);
			}
			
			// source bus
			if(i ==28){
				bus.setGenCode(AclfGenCode.SWING);
				bus.setVoltage(new Complex(1.0, 0.0));
				
//				DStabGen constantGen = DStabObjectFactory.createDStabGen();
//				constantGen.setId("Source");
//				constantGen.setMvaBase(100);
//				constantGen.setPosGenZ(new Complex(0.0,0.05));
//				constantGen.setNegGenZ(new Complex(0.0,0.05));
//				constantGen.setZeroGenZ(new Complex(0.0,0.05));
//				bus.getContributeGenList().add(constantGen);
//				
//				
//				EConstMachine mach = (EConstMachine)DStabObjectFactory.
//						createMachine("MachId", "MachName", MachineType.ECONSTANT, net, "Bus1", "Source");
//			
//				mach.setRating(100, UnitType.mVA, net.getBaseKva());
//				mach.setRatedVoltage(primaryVolt);
//				mach.setH(50000.0);
//				mach.setXd1(0.05);
			}
			
		}
		
		
		for(int i =1;i<=loadNum;i++){
			Bus3Phase bus = ThreePhaseObjectFactory.create3PDStabBus("meter_"+i, net);
			bus.setAttributes("load bus "+i, "");
			bus.setBaseVoltage(secondaryVolt);
			// set the bus to a non-generator bus
			bus.setGenCode(AclfGenCode.NON_GEN);
			// set the bus to a constant power load bus
			bus.setLoadCode(AclfLoadCode.CONST_P);
			
			/*
			 * object load:36 { 
				     name GC-12-47-1_load_1; 
				     parent GC-12-47-1_meter_1; 
				     phases ABCN; 
				     voltage_A 7200+0.0j; 
				     voltage_B -3600-6235j; 
				     voltage_C -3600+6235j; 
				     constant_power_A 585277.7504+357997.618j; 
				     constant_power_B 596917.157+361239.5414j; 
				     constant_power_C 592476.6189+358995.6939j; 
				     nominal_voltage 480; 
				     load_class I; 
				} 
				
				object load:37 { 
				     name GC-12-47-1_load_2; 
				     parent GC-12-47-1_meter_2; 
				     phases ABCN; 
				     voltage_A 7200+0.0j; 
				     voltage_B -3600-6235j; 
				     voltage_C -3600+6235j; 
				     constant_power_A 585277.7504+357997.618j; 
				     constant_power_B 596917.157+361239.5414j; 
				     constant_power_C 592476.6189+358995.6939j; 
				     nominal_voltage 480; 
				     load_class I; 
				} 
				
				object load:38 { 
				     name GC-12-47-1_load_3; 
				     parent GC-12-47-1_meter_3; 
				     phases ABCN; 
				     voltage_A 7200+0.0j; 
				     voltage_B -3600-6235j; 
				     voltage_C -3600+6235j; 
				     constant_power_A 585277.7504+357997.618j; 
				     constant_power_B 596917.157+361239.5414j; 
				     constant_power_C 592476.6189+358995.6939j; 
				     nominal_voltage 480; 
				     load_class I; 
				} 
			 */
			
			Load3Phase load1 = new Load3PhaseImpl();
			Complex3x1 load3Phase = new Complex3x1(new Complex(585277.7504,357997.618),new Complex(596917.157,361239.5414),new Complex(592476.6189,358995.6939)).multiply(0.001);
			load1.set3PhaseLoad(load3Phase);
			bus.getThreePhaseLoadList().add(load1);
			

			
		}
		
		// the regulator bus
		Bus3Phase bus = ThreePhaseObjectFactory.create3PDStabBus("meter_"+(loadNum+1), net);
		bus.setAttributes("meter bus "+(loadNum+1), "");
		bus.setBaseVoltage(primaryVolt);
		// set the bus to a non-generator bus
		bus.setGenCode(AclfGenCode.NON_GEN);
		// set the bus to a constant power load bus
		bus.setLoadCode(AclfLoadCode.NON_LOAD);
	
		
		
		
          for(int i =0;i<lineNodes.length;i++){
			String lineNode = lineNodes[i];
			String[] nodes = lineNode.split(":");
			Branch3Phase line = ThreePhaseObjectFactory.create3PBranch(nodes[0], nodes[1], "0", net);
			
			line.setBranchCode(AclfBranchCode.LINE);
			//TODO setZabc
			if(fuseList.contains(lineNode)||switchList.contains(lineNode) || lineNode.equals("meter_4:node_27")){
				
				//(Complex selfZorY, Complex mutualZorY)
			   line.setZabc(new Complex3x3(new Complex(0.001,0), new Complex(0)));
			}
			else{
				// under_ground line
				String reverseID = nodes[1]+":"+nodes[0];
				if(lineZTable.containsKey(lineNode)){
					line.setZabc(lineZTable.get(lineNode));
				}
				else if(lineZTable.containsKey(reverseID)){
					line.setZabc(lineZTable.get(reverseID));
				}
				else{
					try {
						throw new Exception("line not found in lineZTable:"+lineNode);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
          }
          
          
          for(int i =0;i<xfrNodes.length;i++){
	  			String xfrNode = xfrNodes[i];
	  			String[] nodes = xfrNode.split(":");
	  			Branch3Phase xfr = ThreePhaseObjectFactory.create3PBranch(nodes[0], nodes[1], "0", net);
	  			
	  			xfr.setBranchCode(AclfBranchCode.XFORMER);
	  			
	  			xfr.setFromTurnRatio(secondaryVolt);
	  			xfr.setToTurnRatio(primaryVolt);
	  			//TODO setZabc
	  			
	  			if(xfrNode.equals("meter_4:node_28")){
	  				// regulator
                    double tap = 1.00;
		  			xfr.setFromTurnRatio(primaryVolt);
		  			xfr.setToTurnRatio(primaryVolt*tap);
		  			
	  				xfr.setZ(new Complex(0,0.001));
	  				xfr.setXfrRatedKVA(3000.0);
	  				
	  				AcscXformer xfr0 = acscXfrAptr.apply(xfr);
	  				xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	  				xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	  			}
	  			else{
	  				// step-down transformers for connecting loads
					/*
					 *      <power_rating>3000.000000</power_rating>
							<resistance>0.000030</resistance>
							<reactance>0.000170</reactance>
					 */
	  				
	  				Complex zpercent = new Complex(0.000030,0.000170); // unit
	  				double powerRating = 3000.0; // kva
	  				double zbase = primaryVolt*primaryVolt/(powerRating*1000);
	  				Complex z = zpercent.multiply(zbase);
	  				xfr.setZ(z);
	  				xfr.setXfrRatedKVA(3000.0);
	  				
	  				AcscXformer xfr0 = acscXfrAptr.apply(xfr);
	  				xfr0.setFromConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	  				xfr0.setToConnectGroundZ(XfrConnectCode.WYE_SOLID_GROUNDED, new Complex(0.0,0.0), UnitType.PU);
	  				
	  				
	  			}
  			
            }
          
          // add building motor models
          try {
//			addLargeOffice(net,"meter_1",1,3000.0, 80.0);
			addSmallOffice(net,"meter_2",3,300.0, 70.0);
			addHotel(net,"meter_3",1,2000.0, 75.0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		  // use the OpenDSSDataParser to convert the system parameter to p.u.
          OpenDSSDataParser parser = new OpenDSSDataParser();
		  parser.setDistNetwork(net);
		  parser.convertActualValuesToPU(mvaBase);
		  
		  return net;
		
	}
	
	private boolean addLargeOffice(DStabNetwork3Phase net, String busId, int buildingNum, double totalKW, double indMotorPercent) throws Exception{
		boolean flag = true;
		
		double kvaBase = net.getBaseKva();
		
		double totalBuildingLoad = totalKW*buildingNum;
		
		double totalMotorKW = totalKW*indMotorPercent/100.0;
		
		// double totalBuildingLoadPU = totalKW/kvaBase;
		
		Bus3Phase bus3p = (Bus3Phase) net.getBus(busId);
		
		Complex sumOfBusLoad = new Complex(0,0);
		
		if(bus3p==null){
			throw new Exception("The bus is not found with ID: "+ busId);
			
		}
		
		
		// if the bus is a load bus 
		// if the total power is larger than totalKW*buildingNum
		// subtract the totalKW*buildingNum from the load
		
		// else, rescale the bus load to meet the new total load
		if(bus3p.isLoad() || bus3p.getThreePhaseLoadList().size()>0){
			
			for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
				Complex loadPQ = ld3P.getInit3PhaseTotalLoad();
				sumOfBusLoad = sumOfBusLoad.add(loadPQ);
			}
			
			if(sumOfBusLoad.getReal()<totalBuildingLoad){
				double ratio = totalBuildingLoad/sumOfBusLoad.getReal();
				
				for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
					ld3P.set3PhaseLoad(ld3P.getInit3PhaseLoad().multiply(ratio));
				}
				
			}
	
		}
		
		// if not a load bus, change it to load bus and add the load of totalKW TO ThreePhaseLoadList
		else{
			bus3p.setLoadCode(AclfLoadCode.CONST_P);
			for(int i = 0; i<buildingNum;i++){
				// by default, assume 0.90 
				double totalKVar = totalKW*Math.tan(Math.acos(0.90));
				
				Load3Phase load1 = new Load3PhaseImpl();
				load1.set3PhaseLoad(new Complex3x1(new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3)));
				bus3p.getThreePhaseLoadList().add(load1);
			
			}
			
				
		}
		
		/*
		Large Office	AHU	Fan	           P2P4P5	MB	307.6923077	0.133084908	13.30849082
		Large Office	VAV	Frac_Fan	   P3P4P5	MD	51.10942384	0.022106152	2.210615218
		Large Office	DOAS	Fan	       P2P4P5	MB	33.33333333	0.014417532	1.441753172
		Large Office	Chiller	Compressor	P1P4P5	MA	875	0.378460208	37.84602076
		Large Office	Chiller	Pump	   P2P5	MC	245	0.105968858	10.59688581
		Large Office	Cool_Tower	Fan	   P2P4P5	MB	105	0.045415225	4.541522491
		Large Office	Boilers	Ind_Draft	P1P4P5	MB	208.125	0.090019464	9.001946367
		Large Office	Boilers	Pump	    P2P5	MC	245	0.105968858	10.59688581
		Large Office	CRAC	Compressor	P1P4P5	MA	106.25	0.045955882	4.595588235
		Large Office	CRAC	Fan	        P1P4P5	MB	30.76923077	0.013308491	1.330849082
		Large Office	CRAC	Frac_Condensor	P3P4P5	MD	51	0.022058824	2.205882353


		 */
		
		String[] motorName = {"AHU_Fan", "VAV_Frac_Fan", "DOAS_Fan", "Chiller_Compressor","Chiller_Pump","Cool_Tower",
				"Boilers	Ind_Draft","Boilers	Pump","CRAC	Compressor","CRAC	Fan","CRAC	Frac_Condensor"};
		String[] motorProtectionType = {"245","345","245","145","25","245","145","25","145","145","345"};
		
		int[] motorType = {2,4,2,1,3,2,2,3,1,2,4};
	    double[] motorLoadPercent = {13.3, 2.21, 1.44,37.84, 10.59,4.54,9.00,10.0,4.59,1.33, 2.20};
		
		assertTrue(motorName.length == motorType.length);
		assertTrue(motorName.length == motorProtectionType.length);
		assertTrue(motorName.length == motorLoadPercent.length);
		
		int motorNum = motorName.length;
	    
		for(int i = 0; i<buildingNum;i++){
			// 3 phase motor, 20%
			   for(int j = 0; j<motorName.length;j++){
				   
				    if(motorType[j]==4){
				    	
				        /*
					     *   create the 1-phase AC model 
					     */
				    	double acmotorbase = totalMotorKW /1000.0*motorLoadPercent[j]/100.0/0.8/3; // assuming 0.8 loading factor
					
						
					    SinglePhaseACMotor ac1 = new SinglePhaseACMotor(bus3p, bus3p.getId()+"_largeOffice_"+buildingNum+"_"+motorName[j]+"_A");
				  		ac1.setLoadPercent(motorLoadPercent[j]);
				  		ac1.setPhase(Phase.A);
				  		ac1.setMVABase(acmotorbase);
				  		bus3p.getPhaseADynLoadList().add(ac1);
				  		
				  		
				  		
				  		SinglePhaseACMotor ac2 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_largeOffice_"+buildingNum+"_"+motorName[j]+"_B");
				  		ac2.setLoadPercent(motorLoadPercent[j]);
				  		ac2.setPhase(Phase.B);
				  		ac2.setMVABase(acmotorbase);
				  		bus3p.getPhaseBDynLoadList().add(ac2);
				  		

				  		
				  		SinglePhaseACMotor ac3 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_largeOffice_"+buildingNum+"_"+motorName[j]+"_C");
				  		ac3.setLoadPercent(motorLoadPercent[j]);
				  		ac3.setPhase(Phase.C);
				  		ac3.setMVABase(acmotorbase);
				  		bus3p.getPhaseCDynLoadList().add(ac3);
				    }
				    else{	
				    	
				  		InductionMotor indMotor= DStabObjectFactory.createInductionMotor(bus3p.getId()+"_largeOffice_"+buildingNum+"_"+motorName[j]);
						indMotor.setDStabBus(bus3p);
			
						indMotor.setXm(3.0);
						indMotor.setXl(0.07);
						indMotor.setRa(0.032);
						indMotor.setXr1(0.3);
						indMotor.setRr1(0.01);
						
						double motorkW = totalMotorKW*motorLoadPercent[j]/100.0;
				        double motorP_sysbase = motorkW/kvaBase;
						indMotor.setLoadFactor(0.8);
				
						switch (motorType[j]){
							case 1: // motor A
								indMotor.setH(0.1);
								indMotor.setA(1.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(0.0); //Toreque = (a+bw+cw^2)*To;
							  break;
							case 2: // motor B
								indMotor.setH(0.5);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
							case 3: // motor C
								indMotor.setH(0.1);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
								  
							case 4:
								  break;
							default: 
								throw new Error("motor type must be among [1, 2, 3,  4]");
						  
						}
					
						InductionMotor3PhaseAdapter indMotor3Phase = new InductionMotor3PhaseAdapter(indMotor);
						indMotor3Phase.setLoadPercent(motorkW/sumOfBusLoad.getReal());
						bus3p.getThreePhaseDynLoadList().add(indMotor3Phase);
					
				    }
			
			    }
			}
		
		return flag;
		
	}
	
	private boolean addSmallOffice(DStabNetwork3Phase net, String busId, int buildingNum, double totalKW, double indMotorPercent) throws Exception{
		boolean flag = true;
		
		double kvaBase = net.getBaseKva();
		
		double totalBuildingLoad = totalKW*buildingNum;
		
		double totalMotorKW = totalKW*indMotorPercent/100.0;
		
		// double totalBuildingLoadPU = totalKW/kvaBase;
		
		Bus3Phase bus3p = (Bus3Phase) net.getBus(busId);
		
		Complex sumOfBusLoad = new Complex(0,0);
		
		if(bus3p==null){
			throw new Exception("The bus is not found with ID: "+ busId);
			
		}
		
		
		// if the bus is a load bus 
		// if the total power is larger than totalKW*buildingNum
		// subtract the totalKW*buildingNum from the load
		
		// else, rescale the bus load to meet the new total load
		if(bus3p.isLoad() || bus3p.getThreePhaseLoadList().size()>0){
			
			for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
				//NOTE, this is  for SI input
				Complex loadPQ = ld3P.getInit3PhaseTotalLoad();
				sumOfBusLoad = sumOfBusLoad.add(loadPQ);
			}
			
			if(sumOfBusLoad.getReal()<totalBuildingLoad){
				double ratio = totalBuildingLoad/sumOfBusLoad.getReal();
				
				for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
					ld3P.set3PhaseLoad(ld3P.getInit3PhaseLoad().multiply(ratio));
				}
				
			}
	
		}
		
		// if not a load bus, change it to load bus and add the load of totalKW TO ThreePhaseLoadList
		else{
			bus3p.setLoadCode(AclfLoadCode.CONST_P);
			for(int i = 0; i<buildingNum;i++){
				// by default, assume 0.90 
				double totalKVar = totalKW*Math.tan(Math.acos(0.90));
				
				Load3Phase load1 = new Load3PhaseImpl();
				load1.set3PhaseLoad(new Complex3x1(new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3)));
				bus3p.getThreePhaseLoadList().add(load1);
			
			}
			
				
		}
		
		/*
		Small Office	AHU	Compressor	MA	    P1P2P4P5	106.25	0.553385417
		Small Office	AHU	Fan	        MB	    P1P2P4P5	30.76923077	0.16025641
		Small Office	VAV	Frac_Fan	MD	    P3P4P5	15.40480968	0.080233384
		Small Office	Boilers	Ind_Draft	MD	P3P4P5	20.8125	0.108398438
		Small Office	CRAC	Compressor	MA	P1P4P5	10.625	0.055338542
//		Small Office	CRAC	Fan	      MB	P1P4P5	3.076923077	0.016025641
//		Small Office	CRAC	Frac_Fan	MD	P3P4P5	5.1	       0.0265625


		 */
		
		String[] motorName = {"AHU_Compressor","AHU_Fan", "VAV_Frac_Fan", "Boilers	Ind_Draft","CRAC	Compressor"};
		String[] motorProtectionType = {"1245","1245","345","345","145"};
		
		int[] motorType = {1,2,4,4,1};
	    double[] motorLoadPercent = {55.3, 16.0, 8.0, 10.8, 5.5};
		
		assertTrue(motorName.length == motorType.length);
		assertTrue(motorName.length == motorProtectionType.length);
		assertTrue(motorName.length == motorLoadPercent.length);
		
		int motorNum = motorName.length;
	    
		for(int i = 0; i<buildingNum;i++){
			// 3 phase motor, 20%
			   for(int j = 0; j<motorName.length;j++){
				   
				    if(motorType[j]==4){
				    	
				        /*
					     *   create the 1-phase AC model 
					     */
				    	double acmotorbase = totalMotorKW /1000.0*motorLoadPercent[j]/100.0/0.8/3; // assuming 0.8 loading factor
					
						
					    SinglePhaseACMotor ac1 = new SinglePhaseACMotor(bus3p, bus3p.getId()+"_smallOffice_"+buildingNum+"_"+motorName[j]+"_A");
				  		ac1.setLoadPercent(motorLoadPercent[j]);
				  		ac1.setPhase(Phase.A);
				  		ac1.setMVABase(acmotorbase);
				  		bus3p.getPhaseADynLoadList().add(ac1);
				  		
				  		
				  		
				  		SinglePhaseACMotor ac2 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_smallOffice_"+buildingNum+"_"+motorName[j]+"_B");
				  		ac2.setLoadPercent(motorLoadPercent[j]);
				  		ac2.setPhase(Phase.B);
				  		ac2.setMVABase(acmotorbase);
				  		bus3p.getPhaseBDynLoadList().add(ac2);
				  		

				  		
				  		SinglePhaseACMotor ac3 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_smallOffice_"+buildingNum+"_"+motorName[j]+"_C");
				  		ac3.setLoadPercent(motorLoadPercent[j]);
				  		ac3.setPhase(Phase.C);
				  		ac3.setMVABase(acmotorbase);
				  		bus3p.getPhaseCDynLoadList().add(ac3);
				    }
				    else{	
				    	
				  		InductionMotor indMotor= DStabObjectFactory.createInductionMotor(bus3p.getId()+"_smallOffice_"+buildingNum+"_"+motorName[j]);
						indMotor.setDStabBus(bus3p);
			
						indMotor.setXm(3.0);
						indMotor.setXl(0.07);
						indMotor.setRa(0.032);
						indMotor.setXr1(0.3);
						indMotor.setRr1(0.01);
						
						
				        double motorkW = totalMotorKW*motorLoadPercent[j]/100.0;
				        double motorP_sysbase = motorkW/kvaBase;
						indMotor.setLoadFactor(0.8);
				
						switch (motorType[j]){
							case 1: // motor A
								indMotor.setH(0.1);
								indMotor.setA(1.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(0.0); //Toreque = (a+bw+cw^2)*To;
							  break;
							case 2: // motor B
								indMotor.setH(0.5);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
							case 3: // motor C
								indMotor.setH(0.1);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
								  
							case 4:
								  break;
							default: 
								throw new Error("motor type must be among [1, 2, 3,  4]");
						  
						}
					
						InductionMotor3PhaseAdapter indMotor3Phase = new InductionMotor3PhaseAdapter(indMotor);
						indMotor3Phase.setLoadPercent(motorkW/sumOfBusLoad.getReal());
						bus3p.getThreePhaseDynLoadList().add(indMotor3Phase);
					
				    }
			
			    }
			}
		
		return flag;
		
	}
	
	private boolean addHotel(DStabNetwork3Phase net, String busId, int buildingNum, double totalKW, double indMotorPercent) throws Exception{
		boolean flag = true;
		
		double kvaBase = net.getBaseKva();
		
		double totalBuildingLoad = totalKW*buildingNum;
		
		double totalMotorKW = totalKW*indMotorPercent/100.0;
		
		// double totalBuildingLoadPU = totalKW/kvaBase;
		
		Bus3Phase bus3p = (Bus3Phase) net.getBus(busId);
		
		Complex sumOfBusLoad = new Complex(0,0);
		
		if(bus3p==null){
			throw new Exception("The bus is not found with ID: "+ busId);
			
		}
		
		
		// if the bus is a load bus 
		// if the total power is larger than totalKW*buildingNum
		// subtract the totalKW*buildingNum from the load
		
		// else, rescale the bus load to meet the new total load
		if(bus3p.isLoad() || bus3p.getThreePhaseLoadList().size()>0){
			
			for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
				Complex loadPQ = ld3P.getInit3PhaseTotalLoad();
				sumOfBusLoad = sumOfBusLoad.add(loadPQ);
			}
			
			if(sumOfBusLoad.getReal()<totalBuildingLoad){
				double ratio = totalBuildingLoad/sumOfBusLoad.getReal();
				
				for(Load3Phase ld3P: bus3p.getThreePhaseLoadList()){
					ld3P.set3PhaseLoad(ld3P.getInit3PhaseLoad().multiply(ratio));
				}
				
			}
	
		}
		
		// if not a load bus, change it to load bus and add the load of totalKW TO ThreePhaseLoadList
		else{
			bus3p.setLoadCode(AclfLoadCode.CONST_P);
			for(int i = 0; i<buildingNum;i++){
				// by default, assume 0.90 
				double totalKVar = totalKW*Math.tan(Math.acos(0.90));
				
				Load3Phase load1 = new Load3PhaseImpl();
				load1.set3PhaseLoad(new Complex3x1(new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3),new Complex(totalKW/3,totalKVar/3)));
				bus3p.getThreePhaseLoadList().add(load1);
			
			}
			
				
		}
		
		/*
			Hotel	PTAC 	Compressor	MA	P3P4	425	0.319069069
			Hotel	PTAC 	Fan	MD	P3	123.0769231	0.092400092
			Hotel	Exhaust	Fan	MD	P3	23.08923077	0.017334257	
			Hotel	Split	Fan	MB	P2P4	123.0769231	0.092400092
			Hotel	Split	Compressor	MA	P2P4	425	0.319069069
			Hotel	Split	Frac_Condensor	MD	P3P4	130	0.097597598
			Hotel	Split	Frac_Ind_Draft	MD	P3P4	83.25	0.0625

		 */
		
		String[] motorName = {"PTAC_Compressor","PTAC_Fan", "Exhaust_Fan", "Split_Fan","Split_Compressor","Split_Condensor","Split_Frac_Ind_Draft"};
		
		String[] motorProtectionType = {"34","3","3","24","24","34","34"};
		
		int[] motorType = {1,4,4,2,1,4,4};
	    double[] motorLoadPercent = {31.9, 9.2, 1.7,9.24, 31.9,9.7,6.25};
		
		assertTrue(motorName.length == motorType.length);
		assertTrue(motorName.length == motorProtectionType.length);
		assertTrue(motorName.length == motorLoadPercent.length);
		
		int motorNum = motorName.length;
	    
		for(int i = 0; i<buildingNum;i++){
			// 3 phase motor, 20%
			   for(int j = 0; j<motorName.length;j++){
				   
				    if(motorType[j]==4){
				    	
				        /*
					     *   create the 1-phase AC model 
					     */
				    	double acmotorbase = totalMotorKW /1000.0*motorLoadPercent[j]/100.0/0.8/3; // assuming 0.8 loading factor
					
						
					    SinglePhaseACMotor ac1 = new SinglePhaseACMotor(bus3p, bus3p.getId()+"_hotel_"+buildingNum+"_"+motorName[j]+"_A");
				  		ac1.setLoadPercent(motorLoadPercent[j]);
				  		ac1.setPhase(Phase.A);
				  		ac1.setMVABase(acmotorbase);
				  		bus3p.getPhaseADynLoadList().add(ac1);
				  		
				  		
				  		
				  		SinglePhaseACMotor ac2 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_hotel_"+buildingNum+"_"+motorName[j]+"_B");
				  		ac2.setLoadPercent(motorLoadPercent[j]);
				  		ac2.setPhase(Phase.B);
				  		ac2.setMVABase(acmotorbase);
				  		bus3p.getPhaseBDynLoadList().add(ac2);
				  		

				  		
				  		SinglePhaseACMotor ac3 = new SinglePhaseACMotor(bus3p,bus3p.getId()+"_hotel_"+buildingNum+"_"+motorName[j]+"_C");
				  		ac3.setLoadPercent(motorLoadPercent[j]);
				  		ac3.setPhase(Phase.C);
				  		ac3.setMVABase(acmotorbase);
				  		bus3p.getPhaseCDynLoadList().add(ac3);
				    }
				    else{	
				    	
				  		InductionMotor indMotor= DStabObjectFactory.createInductionMotor(bus3p.getId()+"_hotel_"+buildingNum+"_"+motorName[j]);
						indMotor.setDStabBus(bus3p);
			
						indMotor.setXm(3.0);
						indMotor.setXl(0.07);
						indMotor.setRa(0.032);
						indMotor.setXr1(0.3);
						indMotor.setRr1(0.01);

						double motorkW = totalMotorKW*motorLoadPercent[j]/100.0;
				        double motorP_sysbase = motorkW/kvaBase;
						indMotor.setLoadFactor(0.8);
				
						switch (motorType[j]){
							case 1: // motor A
								indMotor.setH(0.1);
								indMotor.setA(1.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(0.0); //Toreque = (a+bw+cw^2)*To;
							  break;
							case 2: // motor B
								indMotor.setH(0.5);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
							case 3: // motor C
								indMotor.setH(0.1);
								indMotor.setA(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setB(0.0); //Toreque = (a+bw+cw^2)*To;
								indMotor.setC(1.0); //Toreque = (a+bw+cw^2)*To;
								  break;
								  
							case 4:
								  break;
							default: 
								throw new Error("motor type must be among [1, 2, 3,  4]");
						  
						}
					
						InductionMotor3PhaseAdapter indMotor3Phase = new InductionMotor3PhaseAdapter(indMotor);
						indMotor3Phase.setLoadPercent(motorkW/sumOfBusLoad.getReal());
						bus3p.getThreePhaseDynLoadList().add(indMotor3Phase);
					
				    }
			
			    }
			}
		
		return flag;
		
	}
//	private addSmallRetail(String busId, int buildingNum, double totalKW){
//		
//	}
//	
//	private addMediumRetail(String busId, int buildingNum, double totalKW){
//		
//	}
//	private addLodging(String busId, int buildingNum, double totalKW){
//		
//	}

	

}

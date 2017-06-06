package org.ipss.threePhase.test;

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
import org.ipss.threePhase.basic.impl.Load3PhaseImpl;
import org.ipss.threePhase.dataParser.opendss.OpenDSSDataParser;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;
import org.ipss.threePhase.powerflow.impl.DistPowerFlowOutFunc;
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
import com.interpss.core.net.NetworkType;
import com.interpss.dstab.DStabGen;
import com.interpss.dstab.mach.EConstMachine;
import com.interpss.dstab.mach.MachineType;

public class Test_GC_12_47_1_Feeder {
	@Test
	public void testPowerflow(){
		
		 IpssCorePlugin.init();
		 DStabNetwork3Phase net = null;
		try {
			net = createTestFeeder(12470,480,28,3,"",1.0);
		} catch (InterpssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(net.net2String());
		
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
	               
	               System.out.println("from node : "  + from_node);
	               System.out.println("to node : " + to_node);
	               System.out.println("b_matrix : ");
	               
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
                        	   im = Double.valueOf(zstrAry[1]);
                           }
	    	              
                           Complex zmn = new Complex(re,im);
	    	               System.out.println(m+","+n+","+zstr0+","+zmn.toString());
	    	               bMatrixAry[m][n] = zmn;
	    	               
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
			bus.setGenCode(AclfGenCode.GEN_PQ);
			// set the bus to a constant power load bus
			bus.setLoadCode(AclfLoadCode.CONST_P);
			
		
			//capacitor
			if(i ==21){
				Load3Phase Shuntload = new Load3PhaseImpl();
				Complex3x1 shuntY = new Complex3x1(new Complex(0,-600),new Complex(0.0,-600),new Complex(0.0,-600));
				Shuntload.set3PhaseLoad(shuntY);
				bus.getThreePhaseLoadList().add(Shuntload);
			}
			
			// source bus
			if(i ==28){
				bus.setGenCode(AclfGenCode.SWING);
				bus.setVoltage(1.0, 0.0);
				
//				DStabGen constantGen = DStabObjectFactory.createDStabGen();
//				constantGen.setId("Source");
//				constantGen.setMvaBase(100);
//				constantGen.setPosGenZ(new Complex(0.0,0.05));
//				constantGen.setNegGenZ(new Complex(0.0,0.05));
//				constantGen.setZeroGenZ(new Complex(0.0,0.05));
//				bus1.getContributeGenList().add(constantGen);
//				
//				
//				EConstMachine mach = (EConstMachine)DStabObjectFactory.
//						createMachine("MachId", "MachName", MachineType.ECONSTANT, net, "Bus1", "Source");
		//	
//				mach.setRating(100, UnitType.mVA, net.getBaseKva());
//				mach.setRatedVoltage(baseVolt);
//				mach.setH(50000.0);
//				mach.setXd1(0.05);
			}
			
		}
		
		
		for(int i =1;i<=loadNum;i++){
			Bus3Phase bus = ThreePhaseObjectFactory.create3PDStabBus("meter_"+i, net);
			bus.setAttributes("load bus "+i, "");
			bus.setBaseVoltage(secondaryVolt);
			// set the bus to a non-generator bus
			bus.setGenCode(AclfGenCode.GEN_PQ);
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
		bus.setAttributes("meter bus "+1, "");
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
			   line.setZabc(new Complex3x3(new Complex(0.0001,0), new Complex(0)));
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

		  			xfr.setFromTurnRatio(primaryVolt);
		  			
	  				xfr.setZ(new Complex(0.0001));
	  				xfr.setXfrRatedKVA(3000.0);
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
	  				
	  				
	  			}
  			
            }
		
		  // use the OpenDSSDataParser to convert the system parameter to p.u.
          OpenDSSDataParser parser = new OpenDSSDataParser();
		  parser.setDistNetwork(net);
		  parser.convertActualValuesToPU(mvaBase);
		  
		  return net;
		
	}

}

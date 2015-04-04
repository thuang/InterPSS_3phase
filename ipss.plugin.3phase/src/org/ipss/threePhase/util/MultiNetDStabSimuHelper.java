package org.ipss.threePhase.util;

import java.util.Hashtable;

import org.apache.commons.math3.complex.Complex;
import org.interpss.algo.SubNetworkProcessor;

import com.interpss.dstab.DStabBranch;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.DStabilityNetwork;

public class MultiNetDStabSimuHelper {
	
	
	
	/**
	 *   This Multi SubNetwork processing is for three-sequence only;
	 *   This processing is mainly based on the paper  Chengshan Wang,Jiaan Zhang,"PARALLEL ALGORITHM FOR TRANSIENT STABILITY SIMULATION BASED ON  
	 *   BRANCH CUTTING AND SUBSYSTEM ITERATING " 
	 * 
	 * 
	 *   (1) add the Yff of the tieLine branch to the boundary bus yii
	 *   (2) equivalent current injection into the boundary buses which are to represent the contribution
	 *    from the buses on the other end of the tie-line
	 */
	public static void preProcess3SeqMultiSubNetwork(DStabilityNetwork net, SubNetworkProcessor subNetProc){
		
		/*
		 *  (1) add the Yff of the tieLine branch to the boundary bus shuntY, if there is no fixed shuntY 
		 *  in the boundary bus, create one first.
		 *  
		 *  step-1: obtain the boundary information from SubNetworkProcessor, iterate over the interface branches
		 *  
		 *  step-2: with the iteration, obtain the two terminal buses, add the Yff or Ytt of the tie-line depending the side of the bus on 
		 *  the branch
		 *  
		 *  (2) equivalent current injection into the boundary buses which are to represent the contribution from the buses on the other end of the tie-line
		 *  
		 *  step-3:  calculate the equivalent current injection and add to the subNetwork as custom current injection
		 *  
		 *  
		 */
		for(String branchId:subNetProc.getInterfaceBranchIdList()){
			DStabBranch branch= net.getBranch(branchId);
			//if(!branch.isActive()){
			
					DStabBus fBus = (DStabBus) branch.getFromBus();
					DStabBus tBus = (DStabBus) branch.getToBus();
					
					/*
					 * Step-1, add the tie-line equivalent shuntY1/2/0 to the terminal buses
					 */
					//From bus
					if(fBus.getShuntY()!=null){
						
						fBus.setShuntY(fBus.getShuntY().add(branch.yff()));
						
					}
			        if(fBus.getScFixedShuntY0()==null){
						
						fBus.setScFixedShuntY0(branch.yff0());
						
					}
			        else
			        	fBus.setScFixedShuntY0(fBus.getScFixedShuntY0().add(branch.yff0()));
			        
			        
			        
					//To bus
					if(tBus.getShuntY()!=null){
						
						tBus.setShuntY(tBus.getShuntY().add(branch.ytt()));
						
					}
			        if(tBus.getScFixedShuntY0()==null){
						
						tBus.setScFixedShuntY0(branch.ytt0());
						
					}
			        else
			        	tBus.setScFixedShuntY0(tBus.getScFixedShuntY0().add(branch.ytt0()));
			        
	        
			        /*
					 * Step-2, add the tie-line equivalent curret injection to the terminal buses
					 */
			        //From bus side
			        int fChildNetIdx =  subNetProc.getBusId2SubNetworkTable().get(fBus.getId());
			        DStabilityNetwork fChileNet = (DStabilityNetwork) net.getChildNetList().get(fChildNetIdx).getNetwork();
			        //HasCurrentInejctionTable 
			        if(fChileNet.getCustomBusCurrInjHashtable()==null){
			           Hashtable<String, Complex> customBusCurTable = new Hashtable<>();
			           fChileNet.setCustomBusCurrInjHashtable(customBusCurTable);
			           customBusCurTable.put(fBus.getId(), tBus.getVoltage().multiply(branch.yft()).multiply(-1.0d));
			        }
			        else{
			        	//fChileNet.getCustomBusCurrInjHashtable().put(fBus.getId(), tBus.getVoltage().multiply(branch.yft()).multiply(-1.0d));
			        	
			        	Complex currentInj = fChileNet.getCustomBusCurrInjHashtable().get(fBus.getId());
			        	if(currentInj==null) currentInj = new Complex(0,0) ;
			        	currentInj = currentInj.add(tBus.getVoltage().multiply(branch.yft()).multiply(-1.0d));
			        	fChileNet.getCustomBusCurrInjHashtable().put(fBus.getId(), currentInj);
			        }
			        	
			        
			        
			        
			        //To Bus side
			        int tChildNetIdx =  subNetProc.getBusId2SubNetworkTable().get(tBus.getId());
			        DStabilityNetwork tChileNet = (DStabilityNetwork) net.getChildNetList().get(tChildNetIdx).getNetwork();
			        //HasCurrentInejctionTable 
			        if(tChileNet.getCustomBusCurrInjHashtable()==null){
			           Hashtable<String, Complex> customBusCurTable = new Hashtable<>();
			           tChileNet.setCustomBusCurrInjHashtable(customBusCurTable);
			           customBusCurTable.put(tBus.getId(), fBus.getVoltage().multiply(branch.ytf()).multiply(-1.0d));
			        }
			        else{
			        	Complex currentInj = tChileNet.getCustomBusCurrInjHashtable().get(tBus.getId());
			        	if(currentInj==null) currentInj = new Complex(0,0) ;
			        	currentInj = currentInj.add(fBus.getVoltage().multiply(branch.ytf()).multiply(-1.0d));
			        	tChileNet.getCustomBusCurrInjHashtable().put(tBus.getId(), currentInj);
			        	
			        }
			   // after mapping the effect of the tie-line branch to both terminal buses, set it to out-of-service     
			  branch.setStatus(false);      	
			}
		}
		
		
		
	//}

}

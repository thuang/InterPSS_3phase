package org.ipss.threePhase.powerflow.impl;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Network3Phase;
import org.ipss.threePhase.basic.impl.AclfNetwork3Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.powerflow.DistributionPFMethod;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;

import com.interpss.common.exp.InterpssException;
import com.interpss.common.util.IpssLogger;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfNetwork;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;

public class DistributionPowerFlowAlgorithmImpl implements DistributionPowerFlowAlgorithm{

	private AclfNetwork distNet = null;
	
	private DistributionPFMethod pfMethod = null;
	
	private double tol = 1.0E-4;
	private int    maxIteration = 20;
	private boolean radialNetworkOnly = true;
	private boolean pfFlag =false;
	private Hashtable<String,Complex3x1> busVoltTable =null;
	
	public DistributionPowerFlowAlgorithmImpl(){
		busVoltTable = new Hashtable<>();
	}
	   
    public DistributionPowerFlowAlgorithmImpl(AclfNetwork net){
		this.distNet = net;
		busVoltTable = new Hashtable<>();
	}
	
	
	@Override
	public boolean orderDistributionBuses(boolean radialOnly) {
		Queue<Bus3Phase> onceVisitedBuses = new  LinkedList<>();
		
			// find the source bus, which is the swing bus for radial feeders;
			for(AclfBus b: distNet.getBusList()){
				if(b.isActive() && b.isSwing()){
					onceVisitedBuses.add((Bus3Phase) b);
				}
			}
			

		// perform BFS and set the bus sortNumber 
		BFS(onceVisitedBuses);
		
		
		distNet.setBusNumberArranged(true);

	   
		return true;
	}
	
	
	
    private void BFS (Queue<Bus3Phase> onceVisitedBuses){
    	int orderNumber = 0;
		//Retrieves and removes the head of this queue, or returns null if this queue is empty.
	    while(!onceVisitedBuses.isEmpty()){
			Bus3Phase  startingBus = onceVisitedBuses.poll();
			startingBus.setSortNumber(orderNumber++);
			startingBus.setVisited(true);
			startingBus.setIntFlag(2);
			
			if(startingBus!=null){
				  for(Branch connectedBra: startingBus.getBranchList()){
						if(!connectedBra.isVisited()){
							try {
								Bus findBus = connectedBra.getOppositeBus(startingBus);
								
								//update status
								connectedBra.setVisited(true);
								
								//for first time visited buses
								if(findBus.getIntFlag()==0){
									findBus.setIntFlag(1);
									onceVisitedBuses.add((Bus3Phase) findBus);
									
								}
							} catch (InterpssException e) {
								
								e.printStackTrace();
							}
							
						}
				 }
			 
			}
			
	      }
	}

	@Override
	public boolean initBusVoltages() {
	
			
			for(AclfBus b: distNet.getBusList()){
					Bus3Phase bus = (Bus3Phase) b;
					
					if(b.isSwing())
						 getSwingBusThreePhaseVoltages(b.getVoltageMag(), b.getVoltageAng(UnitType.Deg));
					else if(b.isGenPV()) 
						bus.set3PhaseVoltages(getPVBusThreePhaseVoltages(b.getVoltageMag()));
					else
					    bus.set3PhaseVoltages(getUnitThreePhaseVoltages());
					
			}

		return true;
	}
		
	private Complex phaseShiftCplxFactor(double shiftDeg){
			return new Complex(Math.cos(shiftDeg/180.0d*Math.PI),Math.sin(shiftDeg/180.0d*Math.PI));
	}
	
	private Complex3x1 getUnitThreePhaseVoltages(){
		return new Complex3x1(new Complex(1,0),new Complex(-Math.sin(Math.PI/6),-Math.cos(Math.PI/6)),new Complex(-Math.sin(Math.PI/6),Math.cos(Math.PI/6)));
	}
	
	private Complex3x1 getPVBusThreePhaseVoltages(double Vset){
		return new Complex3x1(new Complex(Vset,0),new Complex(-1*Vset*Math.sin(Math.PI/6),-1*Vset*Math.cos(Math.PI/6)),new Complex(-1*Vset*Math.sin(Math.PI/6),Vset*Math.cos(Math.PI/6)));
	}
	
	private Complex3x1 getSwingBusThreePhaseVoltages(double Vset, double angleDeg){
		return new Complex3x1(new Complex(Vset,0).multiply(phaseShiftCplxFactor(angleDeg)),
				new Complex(-1*Vset*Math.sin(Math.PI/6),-1*Vset*Math.cos(Math.PI/6)).multiply(phaseShiftCplxFactor(angleDeg)),
				new Complex(-1*Vset*Math.sin(Math.PI/6),Vset*Math.cos(Math.PI/6)).multiply(phaseShiftCplxFactor(angleDeg)));
	
	}

	@Override
	public boolean powerflow() {
		//step-1 order the network
		 pfFlag = orderDistributionBuses(radialNetworkOnly);
		
		
		//step-2 initialize bus voltage
		if(!pfFlag)
			try {
				throw new Exception("Error in odering the distribution buses");
			} catch (Exception e) {
				e.printStackTrace();
			}
		else{
			pfFlag = this.initBusVoltages();
		}
			
		if(!pfFlag)
			try {
				throw new Exception("Error in iniitalizing the three-phase voltages of distribution buses");
			} catch (Exception e) {
				e.printStackTrace();
			}
		//step-3 applied a power flow solver forward/backward sweep algorithm. 
		if(this.pfMethod==DistributionPFMethod.Forward_Backword_Sweep){
			 
		        pfFlag =  FBSPowerflow(); 
		        if(pfFlag) {
		        	IpssLogger.getLogger().fine("The distribution network power flow is converged.");
		        }
			 }
		else{
			throw new UnsupportedOperationException("The power flow method is not supported yet:"+this.pfMethod);
		}
		return false;
	}
	
	private boolean FBSPowerflow(){
		/*
		 * 1. Backward sweep:  calculate the current injections of buses starting the most remote bus and the current flows in
		 *  all active lines and transformers, all the way up to the source bus.
		 *    
		 * 2. convergence checking: ||deltaV|| < tolerance. 
		 * 
		 * 3. Forward  sweep:  update the voltages of the buses in the downstream  based on the voltages of the upstream bus and the current of the branch or transformer
		 * 
		 */
		
		for (int i=0;i<this.maxIteration;i++){
		
			for (Branch bra: this.distNet.getBranchList()){
				bra.setIntFlag(0);
			}
			
			//Step-1 backward sweep step
			
			
			for(int sortNum =this.distNet.getNoBus()-1;sortNum>=0;sortNum--){
				AclfBus bus = this.distNet.getBus(sortNum);
				if(bus.isActive()){
					Bus3Phase bus3P = null;
					if(bus instanceof Bus3Phase){
						bus3P = (Bus3Phase) bus;
					}
					else{
						throw new UnsupportedOperationException("The bus oject is not a 3phase type:"+bus.getId());
					}
						
					
					// update the non-visited branch current based on the bus current injection
					// and all the currents of all other connected down-stream branches of this bus
					Complex3x1 sumOfBranchCurrents = new Complex3x1();
					String upStreamBranchId = "";
					String upStreamBusId="";
					int unvisitedBranchNum = 0;
					for (Branch bra: bus.getBranchList()){
						Branch3Phase bra3P = (Branch3Phase) bra;
						// all visited branches are on the downstream side, and there should be only one upstream branch
						if(bra.isActive() && bra.getIntFlag() ==1){
							
							if(bra.getFromBus().getId().equals(bus.getId())){
							 
								
							   sumOfBranchCurrents= sumOfBranchCurrents.add(bra3P.getCurrentAbcAtFromSide());
							}
							else{ 
								
								
								sumOfBranchCurrents= sumOfBranchCurrents.add(bra3P.getCurrentAbcAtToSide().multiply(-1.0));
							}
						}
						else if(bra.isActive() && bra.getIntFlag() ==0){
		
							 upStreamBranchId = bra.getId();
							 unvisitedBranchNum +=1;
							 bra.setIntFlag(1);	
							
						}
					    
					}
					
					
					//Error in the searching
					if(bus.getBranchList().size()==1 && unvisitedBranchNum !=1 && !bus.isSwing()){
						throw new Error(" There must be only one 'upstream' unvisited branch for an active, non-swing bus:"+bus.getId());
					}else {
						
						// consider the existing bus current injection into the network from generators, loads, shunt capacitors, etc.
						Complex3x1 busSelfEquivCurInj3Ph =bus3P.calc3PhEquivCurInj();
						
						// add the branch current flows to obtain the current injections
						Branch3Phase upStreamBranch = (Branch3Phase) this.distNet.getBranch(upStreamBranchId);
						
						Bus3Phase upStreamBus3P = null;
						
						// update the upstream branch current and the upstream bus voltage
						if(upStreamBranch.getFromBus().getId().equals(bus.getId())){
							
							//calculate and set the upstream branch current
							upStreamBranch.setCurrentAbcAtFromSide(busSelfEquivCurInj3Ph.subtract( sumOfBranchCurrents));
							
							upStreamBus3P = (Bus3Phase) upStreamBranch.getToBus();
							
							Complex3x1 vabc =  upStreamBranch.getToBusVabc2FromBusVabcMatrix().multiply(bus3P.get3PhaseVotlages()).add(
									upStreamBranch.getToBusIabc2FromBusVabcMatrix().multiply(upStreamBranch.getCurrentAbcAtFromSide().multiply(-1)));
							
							if(upStreamBus3P.getIntFlag()==0 && !upStreamBus3P.isSwing()){
							   upStreamBus3P.set3PhaseVoltages(vabc);
							   upStreamBus3P.setIntFlag(1);
							}
						}
						else{
							upStreamBranch.setCurrentAbcAtToSide(sumOfBranchCurrents.subtract(busSelfEquivCurInj3Ph));
							
	                        upStreamBus3P = (Bus3Phase) upStreamBranch.getFromBus();
							
							Complex3x1 vabc =  upStreamBranch.getToBusVabc2FromBusVabcMatrix().multiply(bus3P.get3PhaseVotlages()).add(
									upStreamBranch.getToBusIabc2FromBusVabcMatrix().multiply(upStreamBranch.getCurrentAbcAtToSide()));
							
							if(upStreamBus3P.getIntFlag()==0 && !upStreamBus3P.isSwing()){
								   upStreamBus3P.set3PhaseVoltages(vabc);
								   upStreamBus3P.setIntFlag(1);
							}
							
						}
						
					}
						
				}
			}
			
			//TODO refer to Disting's reference book for the logic
			//Step-2 check convergence.
			
			// compare the voltage results of the last two steps
			
				
			double mis = 0;
			this.pfFlag =true;
			for(AclfBus bus: this.distNet.getBusList()){ 
				if(bus.isActive()){
					Bus3Phase bus3P = (Bus3Phase) bus;
					if(i>=1){
						mis=bus3P.get3PhaseVotlages().subtract(busVoltTable.get(bus3P.getId())).abs();
						if(mis>this.getTolerance()){
							this.pfFlag = false;
						}
					}
					busVoltTable.put(bus3P.getId(), bus3P.get3PhaseVotlages());
				 }
			}
			
			// power flow is converged, break the outer iteration and return
			if(i>0 && this.pfFlag) break;
			
			
			//Step-3 :backward sweep step
		
		}
		
		return this.pfFlag;
		
		
		
		
	}

	@Override
	public DistributionPFMethod getPFMethod() {

		return this.pfMethod;
	}

	@Override
	public void setTolerance(double tolerance) {
		this.tol = tolerance;
		
	}

	@Override
	public double getTolerance() {
		
		return this.tol;
	}

	@Override
	public void setMaxIteration(int maxIterNum) {
		this.maxIteration = maxIterNum;
		
	}

	@Override
	public int getMaxIteration() {
		
		return this.maxIteration;
	}

	@Override
	public AclfNetwork getNetwork() {
		
		return this.distNet;
	}

	@Override
	public void setNetwork(AclfNetwork net) {
		this.distNet = net;
		
	}
    
	
}

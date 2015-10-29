package org.ipss.threePhase.powerflow.impl;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Network3Phase;
import org.ipss.threePhase.basic.impl.AclfNetwork3Phase;
import org.ipss.threePhase.dynamic.DStabNetwork3Phase;
import org.ipss.threePhase.powerflow.DistributionPFMethod;
import org.ipss.threePhase.powerflow.DistributionPowerFlowAlgorithm;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;

public class DistributionPowerFlowAlgorithmImpl implements DistributionPowerFlowAlgorithm{

	private Network3Phase distNet = null;
	private DistributionPFMethod pfMethod = null;
	
	private double tol = 1.0E-4;
	private int    maxIteration = 20;
	
	@Override
	public boolean orderDistributionBuses(boolean radialOnly) {
		Queue<Bus3Phase> onceVisitedBuses = new  LinkedList<>();
		
		
		if(distNet instanceof AclfNetwork3Phase){
			AclfNetwork3Phase net = (AclfNetwork3Phase) distNet;
			
			// find the source bus, which is the swing bus for radial feeders;
			for(AclfBus b: net.getBusList()){
				if(b.isActive() && b.isSwing()){
					onceVisitedBuses.add((Bus3Phase) b);
				}
			}
			
		}
		else if(distNet instanceof DStabNetwork3Phase){
			
			DStabNetwork3Phase net = (DStabNetwork3Phase) distNet;
			
			// find the source bus, which is the swing bus for radial feeders;
			for(AclfBus b: net.getBusList()){
				if(b.isActive() && b.isSwing()){
					onceVisitedBuses.add((Bus3Phase) b);
				}
			}
			
		}
		
		// perform BFS and set the bus sortNumber 
		BFS(onceVisitedBuses);
		
		return false;
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
						if(connectedBra.isActive() && !connectedBra.isVisited()){
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean powerflow() {
		// TODO Auto-generated method stub
		return false;
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
	public Network3Phase getNetwork() {
		
		return this.distNet;
	}

	@Override
	public void setNetwork(Network3Phase net) {
		this.distNet = net;
		
	}
    
	
}

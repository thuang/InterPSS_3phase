package org.ipss.threePhase.basic.impl;

import java.util.Queue;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;
import org.ipss.threePhase.basic.Branch3Phase;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Network3Phase;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.acsc.AcscBranch;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.impl.AcscNetworkImpl;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;
import com.interpss.core.sparse.impl.SparseEqnComplexMatrix3x3Impl;

public class AclfNetwork3Phase extends AcscNetworkImpl implements
		Network3Phase {
	
	protected boolean isTransNetwork = true; // modeling transmission network by default
	protected ISparseEqnComplexMatrix3x3 yMatrixAbc =null;

	
	private boolean isDeltaConnected(XfrConnectCode code){
		return code ==XfrConnectCode.DELTA ||
				code== XfrConnectCode.DELTA11;
	}
	
	private void BFSSubTransmission (double phaseShiftDeg, Queue<Bus3Phase> onceVisitedBuses){
		
		//Retrieves and removes the head of this queue, or returns null if this queue is empty.
	    while(!onceVisitedBuses.isEmpty()){
			Bus3Phase  startingBus = onceVisitedBuses.poll();
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
									
									// update the phase voltage
									Complex vpos = ((AclfBus)findBus).getVoltage();
									Complex va = vpos.multiply(phaseShiftCplxFactor(phaseShiftDeg));
									Complex vb = va.multiply(phaseShiftCplxFactor(120.0d));
									Complex vc = vb.multiply(phaseShiftCplxFactor(120.0d));
									
									((Bus3Phase) findBus).set3PhaseVoltages(new Complex3x1(va,vb,vc));
								}
							} catch (InterpssException e) {
								
								e.printStackTrace();
							}
							
						}
				 }
			 
			}
			
	      }
	}
	
	private Complex phaseShiftCplxFactor(double shiftDeg){
		return new Complex(Math.cos(shiftDeg/180.0d*Math.PI),Math.sin(shiftDeg/180.0d*Math.PI));
	}

	@Override
	public ISparseEqnComplexMatrix3x3 formYMatrixABC() throws Exception {
		yMatrixAbc = new SparseEqnComplexMatrix3x3Impl(getNoBus());
		
		for(AcscBus b:this.getBusList()){
			if(b instanceof Bus3Phase){
				int i = b.getSortNumber();
				Bus3Phase ph3Bus = (Bus3Phase) b;
				yMatrixAbc.setA(ph3Bus.getYiiAbc() ,i, i);
			}
			else
				throw new Exception("The processing bus # "+b.getId()+"  is not a threePhaseBus");
		}
		
		for (AcscBranch bra : this.getBranchList()) {
			if (bra.isActive()) {
				if(bra instanceof Branch3Phase){
					Branch3Phase ph3Branch = (Branch3Phase) bra;
					int i = bra.getFromBus().getSortNumber(),
						j = bra.getToBus().getSortNumber();
					yMatrixAbc.addToA( ph3Branch.getYftabc(), i, j );
					yMatrixAbc.addToA( ph3Branch.getYtfabc(), j, i );
				}
				else
					throw new Exception("The processing branch #"+bra.getId()+"  is not a threePhaseBranch");
			}
			
		}
	
		return yMatrixAbc;
	}

	@Override
	public boolean run3PhaseLoadflow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ISparseEqnComplexMatrix3x3 getYMatrixABC() {
		
		return this.yMatrixAbc;
	}

	@Override
	public boolean isTransmissionNetwork() {
		
		return this.isTransNetwork;
	}

	@Override
	public void setNetworkType(boolean isTransmission) {
		this.isTransNetwork = isTransmission;
		
	}

	
	
}

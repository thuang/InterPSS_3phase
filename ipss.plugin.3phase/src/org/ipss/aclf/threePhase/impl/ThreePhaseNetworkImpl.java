package org.ipss.aclf.threePhase.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.NumericObjectFactory;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Unit.UnitType;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.numeric.sparse.ISparseEqnMatrix3x3;
import org.interpss.numeric.sparse.impl.SparseEqnMatrix3x3Impl;
import org.ipss.aclf.threePhase.ThreePhaseBranch;
import org.ipss.aclf.threePhase.ThreePhaseBus;
import org.ipss.aclf.threePhase.ThreePhaseGen;
import org.ipss.aclf.threePhase.ThreePhaseLoad;
import org.ipss.aclf.threePhase.ThreePhaseNetwork;

import com.interpss.common.exp.InterpssException;
import com.interpss.core.aclf.AclfBranch;
import com.interpss.core.aclf.AclfBus;
import com.interpss.core.aclf.AclfGen;
import com.interpss.core.aclf.AclfLoad;
import com.interpss.core.acsc.AcscBranch;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.XfrConnectCode;
import com.interpss.core.acsc.impl.AcscNetworkImpl;
import com.interpss.core.net.Branch;
import com.interpss.core.net.Bus;

public class ThreePhaseNetworkImpl extends AcscNetworkImpl implements
		ThreePhaseNetwork {

	@Override
	public boolean initThreePhaseFromLfResult() {

		/*  
		 * initialize the bus phase voltages. 
		 *   Special attentions need to be paid to the buses within the subtransmission and the distribution system, if any, 
		 *   connected to the LV side of Delta/Yg connected step-down transformer 
		 *   
		 *   steps:
		 *   
		 *   (1) initialization by setting the visited status false
		 *   (2) search the step-down delta/Y connected transformers
		 *   (3) starting from the low voltage side of the transformers, set the phase voltage with 30 deg lagging w.r.t the positive sequenc voltage, 
		 *      by assuming the connection meeting the  U.S. Delta connection standard, with high voltage side leading 30 degree, always 
	     *      Finally, set the visited attribute to be true.
	     *   (4) iterate over all step-down transformers and the connected subtransmissions 
	     *   (5) for the rest of the buses, set the phase votlages directly based on the positive sequence voltage
	     *
	     */
		
		//step (1)
		for(AcscBranch bra: this.getBranchList()){
			bra.setVisited(false);
		}
		for(Bus b: this.getBusList()){
			b.setVisited(false);
			b.setIntFlag(0);
		}
		
		double phaseShiftDeg = 0;
		
		for(AcscBranch bra: this.getBranchList()){
			if(bra.isActive() && bra.isXfr()){
				if((isDeltaConnected(bra.getXfrFromConnectCode()) && 
						!isDeltaConnected(bra.getXfrToConnectCode()))
						
						||
						(!isDeltaConnected(bra.getXfrFromConnectCode()) &&
						    isDeltaConnected(bra.getXfrToConnectCode()))){
					
					 bra.setVisited(true);
					 
					 phaseShiftDeg = -30;
					 ThreePhaseBus  StartingBus =null;
					 
					 //high voltage side leads 30 deg, always starts from the low voltage side
					 if(bra.getFromAclfBus().getBaseVoltage()>bra.getToAclfBus().getBaseVoltage()){
						 
						 StartingBus = (ThreePhaseBus) bra.getToAclfBus();
					 }		
					 else {
					
						 StartingBus = (ThreePhaseBus) bra.getFromAclfBus();
					 }
					 
					    Complex vpos = StartingBus.getVoltage();
						Complex va = vpos.multiply(phaseShiftCplxFactor(phaseShiftDeg));
						Complex vb = va.multiply(phaseShiftCplxFactor(120));
						Complex vc = vb.multiply(phaseShiftCplxFactor(120));
						StartingBus.set3PhaseVoltages(new Complex3x1(va,vb,vc));
					 
					 Queue<ThreePhaseBus> q = new  LinkedList<ThreePhaseBus>();
				     q.add(StartingBus);
				     
				     BFSSubTransmission(phaseShiftDeg,q);
				}
			}
		}
		
		
		
		// initialize the phase voltages of those which are not set before, three-phase generation power output and load
		for(AcscBus b: this.getBusList()){
			
			if(b.isActive() && !b.isVisited()){
				   Complex vpos = b.getVoltage();
					Complex va = vpos;
					Complex vb = va.multiply(phaseShiftCplxFactor(120));
					Complex vc = vb.multiply(phaseShiftCplxFactor(120));
					((ThreePhaseBus) b).set3PhaseVoltages(new Complex3x1(va,vb,vc));
			}
				
			//initialize the 3p power output of generation;
			if(b.isGen()){
				for(AclfGen gen: b.getContributeGenList()){
					if(gen instanceof ThreePhaseGen){
						ThreePhaseGen ph3Gen = (ThreePhaseGen) gen;
						Complex phaseGen = gen.getGen().divide(3);
						ph3Gen.setPowerAbc(new Complex3x1(phaseGen,phaseGen,phaseGen), UnitType.PU);
					}
				}
			}
			
			// initialize the load 3-phase power
			if(b.isLoad()){
				for(AclfLoad load: b.getContributeLoadList()){
					if(load instanceof ThreePhaseLoad){
						ThreePhaseLoad ph3Load = (ThreePhaseLoad) load; 
						Complex phaseLoad = load.getLoad(b.getVoltageMag()).divide(3);
						
						ph3Load.set3PhaseLoad(new Complex3x1(phaseLoad,phaseLoad,phaseLoad));
					}
				}
			}
			
		}
		
		
		
		return true;
	}
	
	private boolean isDeltaConnected(XfrConnectCode code){
		return code ==XfrConnectCode.DELTA ||
				code== XfrConnectCode.DELTA11;
	}
	
	private void BFSSubTransmission (double phaseShiftDeg, Queue<ThreePhaseBus> onceVisitedBuses){
		
		//Retrieves and removes the head of this queue, or returns null if this queue is empty.
	    while(!onceVisitedBuses.isEmpty()){
			ThreePhaseBus  startingBus = onceVisitedBuses.poll();
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
									onceVisitedBuses.add((ThreePhaseBus) findBus);
									
									// update the phase voltage
									Complex vpos = ((AclfBus)findBus).getVoltage();
									Complex va = vpos.multiply(phaseShiftCplxFactor(phaseShiftDeg));
									Complex vb = va.multiply(phaseShiftCplxFactor(120.0d));
									Complex vc = vb.multiply(phaseShiftCplxFactor(120.0d));
									
									((ThreePhaseBus) findBus).set3PhaseVoltages(new Complex3x1(va,vb,vc));
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
	public ISparseEqnMatrix3x3 formYabc() throws Exception {
		final ISparseEqnMatrix3x3 yMatrixAbc = new SparseEqnMatrix3x3Impl(getNoBus());
		
		for(AcscBus b:this.getBusList()){
			if(b instanceof ThreePhaseBus){
				int i = b.getSortNumber();
				ThreePhaseBus ph3Bus = (ThreePhaseBus) b;
				yMatrixAbc.setA(ph3Bus.getYiiAbc() ,i, i);
			}
			else
				throw new Exception("The processing bus # "+b.getId()+"  is not a threePhaseBus");
		}
		
		for (AcscBranch bra : this.getBranchList()) {
			if (bra.isActive()) {
				if(bra instanceof ThreePhaseBranch){
					ThreePhaseBranch ph3Branch = (ThreePhaseBranch) bra;
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

}

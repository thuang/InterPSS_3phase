package org.ipss.threePhase.dynamic;

import static com.interpss.common.util.IpssLogger.ipssLogger;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.NumericConstant;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.exp.IpssNumericException;

import com.interpss.common.exp.InterpssRuntimeException;
import com.interpss.common.msg.IpssMessage;
import com.interpss.core.acsc.AcscBus;
import com.interpss.core.acsc.fault.AcscBusFault;
import com.interpss.core.acsc.fault.SimpleFaultCode;
import com.interpss.dstab.algo.defaultImpl.DynamicEventProcessor;
import com.interpss.dstab.datatype.DStabSimuTimeEvent;
import com.interpss.dstab.devent.DynamicEvent;
import com.interpss.dstab.devent.DynamicEventType;

public class DynamicEventProcessor3Phase extends DynamicEventProcessor {
	
	private DStabNetwork3Phase net = null;
	/**
	 * process network dynamic event
	 * 
	 * @param eventMsg network dynamic event
	 * @return false if there is any issue during event handling process 
	 */
	@Override public boolean onMsgEventStatus(IpssMessage eventMsg) {
		if (eventMsg instanceof DStabSimuTimeEvent) {
			DStabSimuTimeEvent dEventMsg = (DStabSimuTimeEvent) eventMsg;
			if (dEventMsg.getType() == DStabSimuTimeEvent.ProessDynamicEvent) {
				
				this.net = (DStabNetwork3Phase) dEventMsg.getDStabNetData();
				
				double t = dEventMsg.getTime();
				if (hasAnyEvent(t)) {
					
					//System.out.println("dynamic event at: "+t);
					/*
					 * We always start from a full Y-matrix without any fault. At
					 * any point, if there is an event, we apply all current
					 * active events to re-construct the Y-matrix.
					 */

					// apply those events which result in changing Y-matrix,
					// such as turn-off a branch
					for (DynamicEvent dEvent : net.getDynamicEventList()) {
						if (dEvent.hasEvent()) {
							applyDynamicEventBefore(dEvent, t);
						}
					}

					// Rebuild the Ymatrix to no-fault condition, which means
					// all events are cleared.
					//net.setYMatrix(net.formYMatrix(SequenceCode.POSITIVE, false));
					try {
						net.formYMatrixABC();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					
					ipssLogger.fine("Reset Ymatrix for event applying");

					// apply those events which result in adding z to Y-matrix,
					// such as applying fault Z
					for (DynamicEvent dEvent : net.getDynamicEventList()) {
						if (dEvent.hasEvent()) {
							try{
								applyDynamicEventAfter(dEvent, t);
							} catch (IpssNumericException e) {
								ipssLogger.severe(e.toString());
								return false;
							}
						}
					}

					// publish Y matrix change event
					// someone may be interested in the change event, for
					// example, user acceptance testing.
					if (net.getNetChangeListener() != null)
						net.getNetChangeListener().onMsgEvent(
								new DStabSimuTimeEvent(	DStabSimuTimeEvent.YMatrixChangeEvent,
										net.getYMatrixABC(), t));
					
					//TODO output the state first
					

					// if have event condition (applying or clearing)
					// solve network and update bus voltage
					net.solveNetEqn(true);
				}
			}
		}
		return true;
	}
	
	@Override
	protected boolean hasAnyEvent(double t) {
		boolean has = false;
		for (DynamicEvent dEvent : net.getDynamicEventList()) {
			if (dEvent.hasEventAt(t)) {
				has = true;
			}
		}
		return has;
	}

	@Override public void onMsgEvent(IpssMessage eventMsg) {
		throw new InterpssRuntimeException("Method not applicable");
	}
	
	// apply event before building the Y-matrix
	@Override
	protected void applyDynamicEventBefore(DynamicEvent e, double t) {
		
	}
	
	
	// apply event after after building the Y-matrix
	@Override
	protected void applyDynamicEventAfter(DynamicEvent e, double t) throws IpssNumericException {
		// active indicates applying the event. We only modify the Y matrix when
				// applying an event
				if (e.isActive()) {
					if (e.getType() == DynamicEventType.BUS_FAULT) {
						AcscBusFault fault = e.getBusFault();
						AcscBus bus = fault.getBus();
						
						int i = bus.getSortNumber();
						
						Complex ylarge =  NumericConstant.LargeBusZ;
						
						//Need to determine the yfaultABC based on the fault type
			            //TODO assuming the SLG fault is applied on phase A
					
						//net.getYMatrix().addToA(y, i, i);
						Complex3x3 yfaultABC = new Complex3x3();
						if(fault.getFaultCode()==SimpleFaultCode.GROUND_LG){
							yfaultABC.aa = ylarge;
							net.getYMatrixABC().addToA(yfaultABC, i, i);
						}
						//TODO need to check how to model LL
						else if(fault.getFaultCode()==SimpleFaultCode.GROUND_LL){
							Complex3x3 yii = net.getYMatrixABC().getA(i, i);
							yii.ab = new Complex(0,0);
							yii.ba = new Complex(0,0);
							//TODO whether it will affect the yii.aa and yii.bb
							
							net.getYMatrixABC().setA(yii,i,i);
							
						}
	                    else if(fault.getFaultCode()==SimpleFaultCode.GROUND_LLG){
	                    	yfaultABC.aa = ylarge;
	                    	yfaultABC.bb = ylarge;
							net.getYMatrixABC().addToA(yfaultABC, i, i);
	                    	
						}
                        else if(fault.getFaultCode()==SimpleFaultCode.GROUND_3P){
                        	yfaultABC.aa = ylarge;
                        	yfaultABC.bb = ylarge;
                        	yfaultABC.cc = ylarge;
                        	
                        	net.getYMatrixABC().addToA(yfaultABC, i, i);
                        	
						}
						
					}
					/*
					 * Apply the branch fault to the Y-matrix
					 */
					else if (e.getType() == DynamicEventType.BRANCH_FAULT) {
						 throw new UnsupportedOperationException();
					}
					 
					}
	}

}

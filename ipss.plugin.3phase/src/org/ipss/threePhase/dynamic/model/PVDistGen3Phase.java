package org.ipss.threePhase.dynamic.model;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.datatype.Unit.UnitType;

import com.interpss.dstab.DStabGen;
import com.interpss.dstab.device.DynamicBusDevice;

/**
 * PV Distributed generation dynamic model.
 * Currently it supports constant PQ mode with converter current limit considered.
 * 
 * The effect of negative sequence is modeled as constant negative sequence impedance. 
 * @author Qiuhua Huang
 *
 */
public class PVDistGen3Phase extends DynGenModel3Phase{
	
	private Complex  genPQInit = null;  // based on the positive sequence
	private Complex  genCurSource = null;
	private double   currLimit = 9999;
	//TODO control model, constant PQ or Volt/Var control
	
	public double getCurrLimit(){
		return currLimit;
	}
	 
	public void   setCurrLimit(double Ilimit){
		this.currLimit = Ilimit;
	}
	 
	 
	 // obtain the initial positive sequence power flow from the power flow result
	 
	 
	 // for dynamic simulation, in the nextStep, update the equivalent current injection

	
	 // set the positive sequence equivalent current injection as the output object

}

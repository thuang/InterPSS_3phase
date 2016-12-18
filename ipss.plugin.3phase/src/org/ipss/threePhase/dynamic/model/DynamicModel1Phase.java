package org.ipss.threePhase.dynamic.model;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.interpss.numeric.datatype.Complex3x1;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;
import org.ipss.threePhase.dynamic.IDynamicModel1Phase;

import com.interpss.core.net.Network;
import com.interpss.core.net.impl.NameTagImpl;
import com.interpss.dstab.BaseDStabBus;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.device.impl.DynamicBusDeviceImpl;
import com.interpss.dstab.mach.Machine;

public abstract class DynamicModel1Phase extends DynamicBusDeviceImpl implements
		IDynamicModel1Phase {
	
	protected  Phase     connectPhase = null;
	protected  Bus3Phase parentBus = null;
	
    public Bus3Phase getParentBus(){
    	if(this.getDStabBus() instanceof Bus3Phase)
    		return (Bus3Phase)this.getDStabBus();
    	else
    		throw new Error("The parentBus is not a Bus3Phase type, #"+this.getDStabBus().getId());
    }
	
	@Override
	public Phase getPhase() {
		return this.connectPhase;
	}

	@Override
	public void setPhase(Phase p) {
		this.connectPhase = p;

	}
	
	public Complex getBusPhaseVoltage(){
		Complex3x1 vabc = ((Bus3Phase)this.getDStabBus()).get3PhaseVotlages();
		
		switch(this.connectPhase){
		case A: 
			return vabc.a_0;
		case B:
			return vabc.b_1;
		default:
			return vabc.c_2;
		}
		
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean initStates() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean initStates(BaseDStabBus abus) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean initStates(BaseDStabBus abus, Machine mach) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean nextStep(double dt, DynamicSimuMethod method) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean nextStep(double dt, DynamicSimuMethod method, Machine mach) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean nextStepMechanical(double dt, DynamicSimuMethod method, Network net) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean nextStepElectrical(double dt, DynamicSimuMethod method, Network net) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Hashtable<String, Object> getStates(Object ref) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	public Hashtable<String, Object> getStates(Machine mach, Object ref) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getOutput() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getOutput(Machine mach) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getOutputObject() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<?> getOutputList() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean updateAttributes(boolean netChange) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

}

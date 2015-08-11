package org.ipss.threePhase.dynamic.model;

import org.apache.commons.math3.complex.Complex;

import com.interpss.dstab.DStabBus;
import com.interpss.dstab.dynLoad.DynLoadModel;

public abstract class DynLoadModel1Phase extends DynamicModel1Phase implements DynLoadModel{
	
	
	protected double loadPercent;
	protected double mva;

	protected double loadFactor = 0.85;
	
	protected DStabBus bus;
	
	
	protected Complex equivY = null;
	
	protected Complex currInj;

	protected Complex initLoadPQ = null;
	protected Complex loadPQ;
	protected Complex compensateShuntY;
	
	
	public double getLoadPercent() {
		
		return this.loadPercent;
	}

	@Override
	public void setLoadPercent(double percentOfTotalLoad) {
		this.loadPercent = percentOfTotalLoad;
		
	}

	@Override
	public double getMVABase() {
		
		return this.mva;
	}

	@Override
	public void setMVABase(double loadMVABase) {
		this.mva = loadMVABase;
		
	}
	
	public double getLoadFactor() {
		return loadFactor;
	}

	public void setLoadFactor(double loadFactor) {
		this.loadFactor = loadFactor;
	}
	

	@Override
	public Complex getPosSeqEquivY() {
		throw new UnsupportedOperationException();
	}
	
	public  Complex getEquivY(){
		return this.equivY ;
	}
	
	@Override
	public Complex getCompShuntY() {
	    return this.compensateShuntY;
		
	}
	
	@Override
	public Complex getInitLoadPQ() {
	
		return this.initLoadPQ;
	}

	@Override
	public void setInitLoadPQ(Complex initLoadPQ) {
		this.initLoadPQ = initLoadPQ;
		
	}

	@Override
	public Complex getLoadPQ() {

		return this.loadPQ;
		
	}

	@Override
	public void setLoadPQ(Complex loadPQ) {
		this.loadPQ = loadPQ;
	}

}

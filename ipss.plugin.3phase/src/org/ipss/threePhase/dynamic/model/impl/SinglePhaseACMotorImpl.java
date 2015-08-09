package org.ipss.threePhase.dynamic.model.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.ipss.threePhase.basic.Bus3Phase;
import org.ipss.threePhase.basic.Phase;
import org.ipss.threePhase.dynamic.model.DynamicModel1Phase;
import org.ipss.threePhase.dynamic.model.SinglePhaseACMotor;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.net.DataCheckConfiguration;
import com.interpss.core.net.NameTag;
import com.interpss.core.net.Network;
import com.interpss.core.net.impl.NameTagImpl;
import com.interpss.dstab.DStabBus;
import com.interpss.dstab.algo.DynamicSimuMethod;
import com.interpss.dstab.mach.Machine;

public class SinglePhaseACMotorImpl extends DynamicModel1Phase implements SinglePhaseACMotor {

	@Override
	public void setBus(DStabBus parentBus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getLoadPercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLoadPercent(double percentOfTotalLoad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMVABase() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMVABase(double loadMVABase) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Complex getPosSeqEquivY() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Complex getCompCurInj() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Complex getCompShuntY() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Complex getInitLoadPQ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitLoadPQ(Complex initLoadPQ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Complex getLoadPQ() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoadPQ(Complex loadPQ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getScripts() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setScripts(String value) {
		throw new UnsupportedOperationException();
		
	}

	
	
	

}

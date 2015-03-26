package org.ipss.threePhase.dynamic.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
import org.ipss.threePhase.dynamic.DStabGen3Phase;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.interpss.common.exp.InterpssException;
import com.interpss.core.net.DataCheckConfiguration;
import com.interpss.core.net.NameTag;
import com.interpss.core.net.impl.NameTagImpl;
import com.interpss.dstab.DStabGen;

public class DStabGen3PhaseImpl extends NameTagImpl implements DStabGen3Phase {

	@Override
	public DStabGen getGen() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGen(DStabGen gen) {
		// TODO Auto-generated method stub
		
	}

	
}

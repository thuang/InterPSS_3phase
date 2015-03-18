package org.ipss.sparse.Matrix3x3;

import org.interpss.numeric.datatype.Complex3x1;
import org.interpss.numeric.datatype.Complex3x3;
import org.interpss.numeric.exp.IpssNumericException;
import org.interpss.numeric.sparse.ISparseEqnComplex;
import org.interpss.numeric.sparse.ISparseEqnComplexMatrix3x3;

import com.interpss.common.exp.InterpssRuntimeException;
import com.interpss.core.sparse.SparseEqnDataType;
import com.interpss.core.sparse.impl.AbstractSparseEquation;
import com.interpss.core.sparse.impl.SparseEqnComplexImpl;
import com.interpss.core.sparse.impl.SparseEqnDoubleImpl;

public class SparseEqnComplexMatrix3x3Impl extends AbstractSparseEquation implements ISparseEqnComplexMatrix3x3{

	private SparseEqnComplexImpl cplxMatrix; 
	
	public SparseEqnComplexMatrix3x3Impl() {
		this(0);
		this.cplxMatrix = new SparseEqnComplexImpl();
		
	}
	
	public SparseEqnComplexMatrix3x3Impl(int n) {
		super(n);
		cplxMatrix = new SparseEqnComplexImpl(3*n);
		
	}
	
	/**
	*
	* @param n matrix dimension
   */
	@Override
	public void setDimension( final int n ) 	{
		cplxMatrix.setDimension(3*n);
	}

	@Override
	public void addToA(Complex3x3 x, int i, int j) {
	
		if ( x.aa.abs() != 0.0 ) {
			cplxMatrix.addToA( x.aa, 3*i, 3*j );
		}
		if ( x.ab.abs() != 0.0 ) {
			cplxMatrix.addToA( x.ab, 3*i, 3*j+1 );
		}
		if ( x.ac.abs() != 0.0 ) {
			cplxMatrix.addToA( x.ac, 3*i, 3*j+2 );
		}
		if ( x.ba.abs() != 0.0 ) {
			cplxMatrix.addToA( x.ba, 3*i+1, 3*j );
		}
		if ( x.bb.abs() != 0.0 ) {
			cplxMatrix.addToA( x.ba, 3*i+1, 3*j+1 );
		}
		if ( x.bc.abs() != 0.0 ) {
			cplxMatrix.addToA( x.bc, 3*i+1, 3*j+2 );
		}
		if ( x.ca.abs() != 0.0 ) {
			cplxMatrix.addToA( x.ca, 3*i+2, 3*j );
		}
		if ( x.cb.abs() != 0.0 ) {
			cplxMatrix.addToA( x.cb, 3*i+2, 3*j+1 );
		}
		if ( x.cc.abs() != 0.0 ) {
			cplxMatrix.addToA( x.cc, 3*i+2, 3*j+2 );
		}
		
		
	}

	@Override
	public Complex3x1 getX(int i) {
		
		return new Complex3x1(cplxMatrix.getElem(3*i).bi,cplxMatrix.getElem(3*i+1).bi,cplxMatrix.getElem(3*i+2).bi);
	}

	@Override
	public void setA(Complex3x3 x, int i, int j) {
		cplxMatrix.setA(x.aa, 3*i, 3*j);
		cplxMatrix.setA(x.ab, 3*i, 3*j+1);
		cplxMatrix.setA(x.ac, 3*i, 3*j+2);
		
		cplxMatrix.setA(x.ba, 3*i+1, 3*j);
		cplxMatrix.setA(x.bb, 3*i+1, 3*j+1);
		cplxMatrix.setA(x.bc, 3*i+1, 3*j+2);
		
		cplxMatrix.setA(x.ca, 3*i+2, 3*j);
		cplxMatrix.setA(x.cb, 3*i+2, 3*j+1);
		cplxMatrix.setA(x.cc, 3*i+2, 3*j+2);
		
	}

	@Override
	public Complex3x3 getA(int i, int j) {
		Complex3x3 m = new Complex3x3();
		m.aa = cplxMatrix.getA(3*i, 3*j);
		m.ab = cplxMatrix.getA(3*i, 3*j+1);
		m.ac = cplxMatrix.getA(3*i, 3*j+2);
		
		m.ba = cplxMatrix.getA(3*i+1, 3*j);
		m.bb = cplxMatrix.getA(3*i+1, 3*j+1);
		m.bc = cplxMatrix.getA(3*i+1, 3*j+2);
		
		m.ca = cplxMatrix.getA(3*i+2, 3*j);
		m.cb = cplxMatrix.getA(3*i+2, 3*j+1);
		m.cc = cplxMatrix.getA(3*i+2, 3*j+2);
		return m;
	}

	@Override
	public void setBi(Complex3x1 bi, int i) {
		cplxMatrix.getElem(3*i).bi = bi.a_0;
		cplxMatrix.getElem(3*i+1).bi = bi.b_1;
		cplxMatrix.getElem(3*i+2).bi = bi.c_2;
		
	}

	@Override
	public void addToB(Complex3x1 bi, int i) {
		cplxMatrix.getElem(3*i).bi = cplxMatrix.getElem(3*i).bi.add(bi.a_0);
		cplxMatrix.getElem(3*i+1).bi = cplxMatrix.getElem(3*i+1).bi.add(bi.b_1);
		cplxMatrix.getElem(3*i+2).bi = cplxMatrix.getElem(3*i+2).bi.add(bi.c_2);
		
	}

	@Override
	public int getZeroA_row() {
		throw new InterpssRuntimeException("SparseEqnComplex.getZeroA_row() needs to be override");
	}


	@Override
	public void increaseDimension() {
		throw new InterpssRuntimeException("SparseEqnComplex.getZeroA_row() needs to be override");
		
	}

	@Override
	public int getTotalElements() {

		return cplxMatrix.getTotalElements();
	}

	@Override
	public boolean luMatrix(double tolerance) throws IpssNumericException {
		
		return cplxMatrix.luMatrix(tolerance);
	}

	@Override
	public boolean luMatrixAndSolveEqn(double tolerance)
			throws IpssNumericException {
		
		return cplxMatrix.luMatrixAndSolveEqn(tolerance);
	}

	@Override
	public void solveEqn() throws IpssNumericException {
		cplxMatrix.solveEqn();
		
	}

	@Override
	public void setB2Unity(int i) {
		cplxMatrix.setB2Unity(3*i);
		cplxMatrix.setB2Unity(3*i+1);
		cplxMatrix.setB2Unity(3*i+2);
		
	}

	@Override
	public void setB2Zero() {
		cplxMatrix.setB2Zero();
	
		
	}

	@Override
	public void reset() {
		cplxMatrix.reset();
	}

	@Override
	public void setToZero() {
		cplxMatrix.setToZero();
	}

	@Override
	public int getZeroAii_row() {

		return cplxMatrix.getZeroAii_row();
	}
	
	public ISparseEqnComplex getSparseEqnComplex(){
		return this.cplxMatrix;
	}

}
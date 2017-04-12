package org.ipss.threePhase.basic;

import org.interpss.numeric.datatype.Complex3x3;

public class LineConfiguration {
	
	enum InputType { Physical, ZYMatrix, LineCode} 
	


	private Complex3x3 zMtx = null;
	
	private Complex3x3 shuntYMtx = null;
	
	
	public Complex3x3 getZ3x3Matrix() {
		return zMtx;
	}

	public void setZ3x3Matrix(Complex3x3 zMtx) {
		this.zMtx = zMtx;
	}

	public Complex3x3 getShuntY3x3Matrix() {
		return shuntYMtx;
	}

	public void setShuntY3x3Matrix(Complex3x3 shuntYMtx) {
		this.shuntYMtx = shuntYMtx;
	}

}

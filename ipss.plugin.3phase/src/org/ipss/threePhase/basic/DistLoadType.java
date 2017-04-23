package org.ipss.threePhase.basic;

/*
 *  load model code: 
 *  1:Standard constant P+jQ load. (Default)
	2:Constant impedance load.
	3:Const P, Quadratic Q (like a motor).
	4:Nominal Linear P, Quadratic Q (feeder mix). Use this with CVRfactor.
	5:Constant Current Magnitude
	6:Const P, Fixed Q
	7:Const P, Fixed Impedance Q
	8:ZIPV (7 values)
 */
public enum DistLoadType {
    CONST_PQ, CONST_I,CONST_Z, CONST_P_QUAD_Q, LINEAR_P_QUAD_Q,CONST_P_FIXED_Q,CONST_P_FIXED_Z_Q,ZIP
}

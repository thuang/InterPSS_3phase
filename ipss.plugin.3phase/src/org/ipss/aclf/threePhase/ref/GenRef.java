package org.ipss.aclf.threePhase.ref;

import com.interpss.core.aclf.AclfGen;
import com.interpss.core.net.NameTag;

public interface GenRef<TGen extends AclfGen> extends NameTag {
	
	TGen getGen();
	void  setGen(TGen gen);

}

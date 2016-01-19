package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

public class CarbonCyclization extends Cyclization {

	@Override
	protected boolean isTargetAtom(Atom a_oAtom) {
		return ( a_oAtom.getSymbol().equals("C") );
	}

}

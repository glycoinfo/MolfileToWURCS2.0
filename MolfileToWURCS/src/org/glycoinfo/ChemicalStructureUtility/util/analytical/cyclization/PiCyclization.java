package org.glycoinfo.ChemicalStructureUtility.util.analytical.cyclization;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;

public class PiCyclization extends Cyclization {

	@Override
	protected boolean isTargetAtom(Atom a_oAtom) {
		return ( a_oAtom.getNumberOfPiElectron() > 0 );
	}

}

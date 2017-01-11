package org.glycoinfo.ChemicalStructureUtility.util;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.ChemicalGraph;

public class MorganAlgorithmWithAtomType extends MorganAlgorithm {

	public MorganAlgorithmWithAtomType(ChemicalGraph a_oGraph) {
		super(a_oGraph);
	}

	protected int getAtomWeight( Atom a_oAtom ) {
		if ( !Chemical.isKnownAtom( a_oAtom.getSymbol() ) )
			return 999;
		return Chemical.getAtomicNumber( a_oAtom.getSymbol() );
	}
}

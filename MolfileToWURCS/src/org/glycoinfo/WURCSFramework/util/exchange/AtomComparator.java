package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Comparator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;

public class AtomComparator implements Comparator<Atom> {

	@Override
	public int compare(Atom o1, Atom o2) {
		int t_iComp = Chemical.getAtomicNumber( o2.getSymbol() ) - Chemical.getAtomicNumber( o1.getSymbol() );
		if ( t_iComp != 0 ) return t_iComp;
		return 0;
	}

}

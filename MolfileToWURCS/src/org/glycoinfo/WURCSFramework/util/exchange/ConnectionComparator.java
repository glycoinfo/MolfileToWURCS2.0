package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.Comparator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

public class ConnectionComparator implements Comparator<Connection> {

	public int compare(Connection o1, Connection o2) {

		int t_iComp = o2.getBond().getType() - o1.getBond().getType();
		if ( t_iComp != 0 ) return t_iComp;

		AtomComparator t_oAComp = new AtomComparator();
		// Compare end atom
		t_iComp = t_oAComp.compare( o1.endAtom(), o2.endAtom() );
		if ( t_iComp != 0 ) return t_iComp;
		// Compare start atom
		t_iComp = t_oAComp.compare( o1.startAtom(), o2.startAtom() );
		if ( t_iComp != 0 ) return t_iComp;

		return 0;
	}

}

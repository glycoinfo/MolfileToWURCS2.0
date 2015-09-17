package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.NOC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;

public class NOCApproach {

	private HashMap<Atom, Integer> m_mapAtomToNOCNumPhase1 = new HashMap<Atom, Integer>();
	private HashMap<Atom, Integer> m_mapAtomToNOCNumPhase2 = new HashMap<Atom, Integer>();
	private HashSet<Atom> m_aMonosaccharideCarbons = new HashSet<Atom>();

	public NOCApproach(Molecule a_oMol) {
		this.countNabourOxygen(a_oMol);
	}

	public HashMap<Atom, Integer> getNOCNumMapPhase1() {
		return this.m_mapAtomToNOCNumPhase1;
	}

	public HashMap<Atom, Integer> getNOCNumMapPhase2() {
		return this.m_mapAtomToNOCNumPhase2;
	}

	public HashSet<Atom> getMonosaccharideCarbon() {
		return this.m_aMonosaccharideCarbons;
	}

	public void clear() {
		this.m_mapAtomToNOCNumPhase1 = new HashMap<Atom, Integer>();
		this.m_mapAtomToNOCNumPhase2 = new HashMap<Atom, Integer>();
		this.m_aMonosaccharideCarbons = new HashSet<Atom>();
	}

	private void countNabourOxygen(Molecule a_oMol) {
		this.clear();
		// For first phase
		for ( Atom t_oCarbon : a_oMol.getAtoms() ) {
			int t_nOxygen = 0;
			if ( !t_oCarbon.getSymbol().equals("C") ) continue;

			for ( Connection t_oConn : t_oCarbon.getConnections() ) {
				if ( !t_oConn.endAtom().getSymbol().equals("O") ) continue;
				t_nOxygen++;
				if ( t_oConn.getBond().getType() == 2 ) t_nOxygen++;
			}
			this.m_mapAtomToNOCNumPhase1.put(t_oCarbon, t_nOxygen);
		}

		// Second phase
		HashSet<Atom> t_aStartCarbons = new HashSet<Atom>();
		for ( Atom t_oCarbon : this.m_mapAtomToNOCNumPhase1.keySet() ) {
			int t_nOxygen2 = this.m_mapAtomToNOCNumPhase1.get(t_oCarbon);
			for ( Connection t_oConn : t_oCarbon.getConnections() ) {
				if ( !this.m_mapAtomToNOCNumPhase1.containsKey( t_oConn.endAtom() ) ) continue;

				t_nOxygen2 += this.m_mapAtomToNOCNumPhase1.get( t_oConn.endAtom() );
			}
			this.m_mapAtomToNOCNumPhase2.put(t_oCarbon, t_nOxygen2);

			// Set start carbon
			if ( t_nOxygen2 < 3 ) continue;
			t_aStartCarbons.add(t_oCarbon);
		}

		// Choose monosaccharide carbons
		for ( Atom t_oStart : t_aStartCarbons ) {
			if ( this.m_aMonosaccharideCarbons.contains(t_oStart) ) continue;
			this.m_aMonosaccharideCarbons.add(t_oStart);

			LinkedList<Atom> t_aCarbonGroup = new LinkedList<Atom>();
			t_aCarbonGroup.add(t_oStart);
			while ( !t_aCarbonGroup.isEmpty() ) {
				Atom t_oCarbon = t_aCarbonGroup.removeFirst();
				this.m_aMonosaccharideCarbons.add(t_oCarbon);
				for ( Connection t_oConn : t_oCarbon.getConnections() ) {
					Atom t_oConAtom = t_oConn.endAtom();
					// Ignore not carbon
					if ( !t_oConAtom.getSymbol().equals("C") ) continue;
					// Ignore already added carbon
					if ( this.m_aMonosaccharideCarbons.contains(t_oConAtom) ) continue;
//					if ( t_aCarbonGroup.contains(t_oConAtom) ) continue;

					// Do not take carbon with 0 score
					if ( this.m_mapAtomToNOCNumPhase2.get(t_oConAtom) == 0 ) continue;

					t_aCarbonGroup.addLast(t_oConAtom);
				}
			}
		}

	}
}

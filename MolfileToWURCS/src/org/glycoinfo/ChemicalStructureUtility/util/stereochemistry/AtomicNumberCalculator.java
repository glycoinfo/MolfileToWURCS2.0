package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.HashMap;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.util.Chemical;

/**
 * Atom number generator for StereochmistryAnalysis
 * @author MasaakiMatsubara
 *
 */
public class AtomicNumberCalculator {

	private HashMap<Atom, Double> m_mapAtomToExtraWeight;

	public AtomicNumberCalculator() {
		this.m_mapAtomToExtraWeight = new HashMap<Atom, Double>();
	}

	public void setExtraWeightToAtom(Atom a_oAtom, Double t_dWeight) {
		this.m_mapAtomToExtraWeight.put(a_oAtom, t_dWeight);
	}

	public Double getExtraWeight(Atom a_oAtom) {
		if ( !this.m_mapAtomToExtraWeight.containsKey(a_oAtom) ) return 0.0D;
		return this.m_mapAtomToExtraWeight.get(a_oAtom);
	}

	public double getAtomicNumber(Atom a_oAtom) {
		double t_dANum = 0.0D;
		// TODO: Atomic number for Backbone carbons
		t_dANum = (double)Chemical.getAtomicNumber(a_oAtom.getSymbol());

		if ( !this.m_mapAtomToExtraWeight.containsKey(a_oAtom) )
			return t_dANum;

		return t_dANum + this.m_mapAtomToExtraWeight.get(a_oAtom);
	}
}

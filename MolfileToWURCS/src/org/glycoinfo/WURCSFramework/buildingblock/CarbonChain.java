package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;

/**
 * Class for carbon chain
 * @author MasaakiMatsubara
 *
 */
public class CarbonChain {

	private LinkedList<Carbon> m_aCarbons       = new LinkedList<Carbon>();
	private LinkedList<Atom>   m_aOriginalChain = new LinkedList<Atom>();

	public CarbonChain(LinkedList<Atom> a_aChain) {
		this.m_aOriginalChain = a_aChain;
	}

	public LinkedList<Atom> getOriginalCarbonChain() {
		return this.m_aOriginalChain;
	}

	public void add(Carbon a_oCarbon) throws BuildingBlockException {
		if ( this.m_aCarbons.contains(a_oCarbon) )
			throw new BuildingBlockException("Already exist in the carbon chain.");

		this.m_aCarbons.addLast(a_oCarbon);
	}

	/**
	 * Get index of target carbon
	 * @param a_oCarbon A target carbon
	 * @return Number of index (-1 if not exist)
	 * @throws BuildingBlockException
	 */
	public int indexOf(Atom a_oCarbon) {
		int index = -1;
		for ( int i=0; i<this.m_aCarbons.size(); i++ ) {
			if ( ! this.m_aCarbons.get(i).equals(a_oCarbon) ) continue;
			index = i;
		}
		return index;
	}
}

package org.glycoinfo.WURCSFramework.util.chemicalgraph;

import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Bond;
import org.glycoinfo.WURCSFramework.chemicalgraph.ChemicalGraph;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Class for calculating Morgan number using Morgan algorithm
 * @author MasaakiMatsubara
 *
 */
public class MorganAlgorithm {

	private ChemicalGraph m_oGraph;
	private HashMap<Atom, Integer> m_mapAtomToMorganNumber = new HashMap<Atom, Integer>();

	public MorganAlgorithm(ChemicalGraph a_oGraph) {
		this.m_oGraph = a_oGraph;
	}

	/** Get updated Morgan numbers */
	public HashMap<Atom, Integer> getAtomToMorganNumber() {
		HashMap<Atom, Integer> t_mapAtomToMorganNumberCopy = new HashMap<Atom, Integer>();
		for ( Atom t_oAtom : this.m_mapAtomToMorganNumber.keySet() )
			t_mapAtomToMorganNumberCopy.put(t_oAtom, this.m_mapAtomToMorganNumber.get(t_oAtom));
		return t_mapAtomToMorganNumberCopy;
	}

	public int getMorganNumber(Atom a_oAtom) {
		if ( this.m_mapAtomToMorganNumber.get(a_oAtom) == null ) return 0;
		return this.m_mapAtomToMorganNumber.get(a_oAtom);
	}

	/**
	 * Calculate Morgan number of this chemical graph without ignoreAtoms and ignoreBonds.
	 * @param ignoreBonds
	 * @param ignoreAtoms
	 */
	public void calcMorganNumber(final LinkedList<Bond> ignoreBonds, final LinkedList<Atom> ignoreAtoms){
		HashMap<Atom, Integer> t_mapAtomToMorganNum = new HashMap<Atom, Integer>();
		LinkedList<Atom> t_aAtoms = this.m_oGraph.getAtoms();
		for ( Atom atom : t_aAtoms )
			t_mapAtomToMorganNum.put(atom, 1);

		int t_iUniqCountPrev = 1;
		while (true) {
			for ( Atom atom : t_aAtoms )
				this.m_mapAtomToMorganNumber.put( atom, t_mapAtomToMorganNum.get(atom) );

			for ( Atom atom : t_aAtoms )
				t_mapAtomToMorganNum.put(atom, 0);
			for ( Atom atom : t_aAtoms ) {
				if( ignoreAtoms != null && ignoreAtoms.contains(atom)) continue;
				int t_iNum = t_mapAtomToMorganNum.get(atom);
				for ( Connection con : atom.getConnections() ) {
					if ( ignoreBonds != null && ignoreBonds.contains(con.getBond()) ) continue;
					if ( ignoreAtoms != null && ignoreAtoms.contains(con.endAtom()) ) continue;
					Integer t_iConnNum = this.m_mapAtomToMorganNumber.get(con.endAtom());
					if ( t_iConnNum == null ) continue;
					t_iNum += t_iConnNum;
				}
				t_mapAtomToMorganNum.put(atom, t_iNum);
			}
			int t_iUniqCount = this.countUniq(t_mapAtomToMorganNum);
			System.err.println( t_iUniqCount );
			if ( t_iUniqCount <= t_iUniqCountPrev ) break;
			t_iUniqCountPrev = t_iUniqCount;
		}
	}

	/**
	 * Return the number of unique tmp in list
	 * (Use when generate EC number)
	 * @return the number of unique tmp in list
	 */
	private int countUniq(HashMap<Atom, Integer> a_mapAtomToMorganNum) {
		LinkedList<Integer> t_aUniqNums = new LinkedList<Integer>();
		for ( Integer t_iNum : a_mapAtomToMorganNum.values() ) {
			if ( t_iNum == null ) continue;
			if ( t_aUniqNums.contains(t_iNum) ) continue;
			t_aUniqNums.add(t_iNum);
		}
		return t_aUniqNums.size();
/*
		LinkedList<Atom> t_aAtoms = new LinkedList<Atom>();
		t_aAtoms.addAll( a_mapAtomToTempECNum.keySet() );
		int uniqNum = 0;
		int ii, jj;
		int num = t_aAtoms.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
				int t_iMorganNumI = a_mapAtomToTempECNum.get( t_aAtoms.get(ii) );
				int t_iMorganNumJ = a_mapAtomToTempECNum.get( t_aAtoms.get(jj) );

				if ( t_iMorganNumI == t_iMorganNumJ ) break;
			}
			if(jj==num) uniqNum++;
		}
		return uniqNum;
*/
	}
}

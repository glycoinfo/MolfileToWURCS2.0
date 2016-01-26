package org.glycoinfo.ChemicalStructureUtility.util.analytical;

import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

public class CarbonChainAnalyzer {
	private CarbonIdentifier m_objIdentC = new CarbonIdentifier();
	private LinkedList<Atom> m_aCarbonChain = new LinkedList<Atom>();
	private String m_strOxidationSequence = null;
	private String m_strCoOCOSequence = null;

	public CarbonChainAnalyzer setCarbonChain(LinkedList<Atom> chain) {
		this.m_aCarbonChain = chain;
		return this;
	}

	public LinkedList<Integer> getOxygenCountSequence() {
		LinkedList<Integer> t_aOxygenCount = new LinkedList<Integer>();
		for ( Atom atom : this.m_aCarbonChain )
			t_aOxygenCount.add( this.m_objIdentC.setAtom(atom).countConnectedO() );
		return t_aOxygenCount;
	}

	/**
	 * Get string of oxidation sequence of this backbone carbons.
	 * Set number of oxidation to each carbon.
	 * @return String of oxidation sequence of the backbone carbons
	 */
	public String getOxidationSequence() {
		this.m_strOxidationSequence = "";
		for ( Atom atom : this.m_aCarbonChain ) {
			int oxidationNumber = 0;
			for ( Connection connection : atom.getConnections() ) {
				int type = connection.getBond().getType();
				if ( type == 2 ) oxidationNumber += 1;
				if ( type == 3 ) oxidationNumber += 2;

				String symbol = connection.endAtom().getSymbol();
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") )
					oxidationNumber++;
			}
			this.m_strOxidationSequence += oxidationNumber;
		}
		return this.m_strOxidationSequence;
	}

	/**
	 * Get string of co-OCO sequence of this backbone carbons.
	 * Set "1" to each carbon if the carbon has co-OCO connection. Otherwise set "0";
	 * @return String of oxidation sequence of the backbone carbons
	 */
	public String getCoOCOSequence() {
		this.m_strCoOCOSequence = "";
//		int ii = 0;
		for ( Atom atom : this.m_aCarbonChain ) {
			this.m_objIdentC.setAtom(atom);
			this.m_strCoOCOSequence += ( this.m_objIdentC.isAnomericLike() )? "1" : "0";
//			ii++;
//			if(ii>=minBackboneLength) break;
		}
//		System.err.println(this.m_strCoOCOSequence);
		return this.m_strCoOCOSequence;
	}

	/**
	 * Get first cyclic ether carbon.
	 * If two carbons contained Backbone are connected by a heteroatom,
	 *  return the carbon which found earlier in the list.
	 *  Otherwise return null.
	 * <pre>
	 * C0-C1-C2-C3-C4-C5
	 *    |        |
	 *    O--------+
	 * In the case, return "C1".
	 * </pre>
	 * @return the first carbon which connect hetero atom of cyclic ether
	 */
	public Atom getFirstCyclicEtherCarbon(){
		for ( Atom atom1 : this.m_aCarbonChain ) {
			for ( Connection connect1 : atom1.getConnections()){
				Atom con1Atom = connect1.endAtom();
				if ( con1Atom.getSymbol().equals("C")) continue;
				if ( this.m_aCarbonChain.contains(con1Atom)) continue;
				for(Connection connect2 : con1Atom.getConnections()){
					Atom con2Atom = connect2.endAtom();
					if(con2Atom == atom1) continue;
					if(this.m_aCarbonChain.contains(con2Atom)){
						return atom1;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get ring ether atoms. Ether atom is bridged carbon chain.
	 * @return List of ring ether atoms
	 */
	public LinkedList<Atom> getRingEtherAtoms() {
		LinkedList<Atom> t_aEtherAtoms = new LinkedList<Atom>();
		for ( Atom t_oCarbon : this.m_aCarbonChain ) {
			// Search atom connected to other carbon
			for ( Connection t_oConn : t_oCarbon.getConnections()){
				Atom t_oConnAtom = t_oConn.endAtom();
				if ( t_oConnAtom.getSymbol().equals("C")) continue;
				if ( this.m_aCarbonChain.contains(t_oConnAtom)) continue;
				for( Connection t_oConn2 : t_oConnAtom.getConnections() ) {
					Atom t_oConn2Atom = t_oConn2.endAtom();
					if ( t_oConn2Atom.equals(t_oCarbon) ) continue;
					if ( !this.m_aCarbonChain.contains(t_oConn2Atom) ) continue;
					t_aEtherAtoms.add(t_oConnAtom);
				}
			}
		}
		return t_aEtherAtoms;
	}
	/**
	 * Get anomeric carbon. If number of candidate anomeric carbon is not one, return first anomeric carbon.
	 * @return Atom Anomeric carbon
	 */
	public Atom getAnomericCarbon() {
/*
		for ( Atom atom : this.m_aCarbonChain ) {
			this.m_objIdentC.setAtom(atom);
			if ( this.m_objIdentC.isAnomericLike() ) return atom;
		}
		return this.m_aCarbonChain.getFirst();
*/
		if ( this.getRingEtherAtoms().isEmpty() ) return null;
		LinkedList<Atom> t_aCandidateAnomericCarbons = new LinkedList<Atom>();
		for ( Atom t_oEtherAtom : this.getRingEtherAtoms() ) {
			for ( Connection t_oConn : t_oEtherAtom.getConnections() ) {
				Atom t_oCarbon = t_oConn.endAtom();
				if ( !this.m_aCarbonChain.contains(t_oCarbon) ) continue;
				int t_nMod = 0;
				// Search carbon which has only one modification except for hydrogen, backbone carbon and ring ether
				for ( Connection t_oConn2 : t_oCarbon.getConnections() ) {
					// For only single bond modification (ignore acid)
					if ( t_oConn2.getBond().getType() != 1 ) continue;
					Atom t_oConn2Atom = t_oConn2.endAtom();
					if ( t_oConn2Atom.equals(t_oEtherAtom) ) continue;
					if ( this.m_aCarbonChain.contains(t_oConn2Atom) ) continue;
					if ( t_oConn2Atom.getSymbol().equals("H") ) continue;
					t_nMod++;
				}
				if ( t_nMod != 1 ) continue;
				if ( t_aCandidateAnomericCarbons.contains(t_oCarbon) ) continue;
				t_aCandidateAnomericCarbons.add(t_oCarbon);
			}
		}
		if ( t_aCandidateAnomericCarbons.isEmpty() ) return null;

		// Screen candidate anomeric center
		// Prioritize a carbon which has smaller number in the main chain
		Atom t_oAnomCenter = t_aCandidateAnomericCarbons.getFirst();
		int t_nMinCarbon = this.m_aCarbonChain.indexOf(t_oAnomCenter);
		for ( Atom t_oCandidateAnomCenter : t_aCandidateAnomericCarbons ) {
			int t_nCarbon = this.m_aCarbonChain.indexOf(t_oCandidateAnomCenter);
			if ( t_nCarbon >= t_nMinCarbon ) continue;
			t_oAnomCenter = t_oCandidateAnomCenter;
			t_nMinCarbon = t_nCarbon;
		}
		return t_oAnomCenter;
	}

	public Atom getRingEtherAtomOnAnomericCarbon() {
		Atom t_oAnomCenter = this.getAnomericCarbon();
		if ( t_oAnomCenter == null ) return null;
		for ( Connection t_oConn : t_oAnomCenter.getConnections() ) {
			Atom t_oEtherAtom = t_oConn.endAtom();
			if ( this.m_aCarbonChain.contains(t_oEtherAtom) ) continue;
			for ( Connection t_oConn2 : t_oEtherAtom.getConnections() ) {
				Atom t_oConn2Atom = t_oConn2.endAtom();
				if ( t_oConn2Atom.equals(t_oAnomCenter) ) continue;
				if ( !this.m_aCarbonChain.contains(t_oConn2Atom) ) continue;
				return t_oEtherAtom;
			}
		}
		return null;
	}
}

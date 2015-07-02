package chemicalgraph.util.analytical;

import java.util.LinkedList;

import chemicalgraph.Atom;
import chemicalgraph.Connection;

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
	 * Get anomeric carbon. If number of candidate anomeric carbon is not one, return first anomeric carbon.
	 * @return Atom Anomeric carbon
	 */
	public Atom getAnomericCarbon() {
		for ( Atom atom : this.m_aCarbonChain ) {
			this.m_objIdentC.setAtom(atom);
			if ( this.m_objIdentC.isAnomericLike() ) return atom;
		}
		return this.m_aCarbonChain.getFirst();
	}

}

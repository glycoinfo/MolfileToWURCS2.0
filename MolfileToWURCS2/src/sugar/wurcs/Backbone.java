package sugar.wurcs;

import java.util.LinkedList;

import util.analytical.CarbonIdentifier;
import chemicalgraph2.Atom;
import chemicalgraph2.Connection;

public class Backbone extends LinkedList<Atom> implements WURCSGlycanObject{
	private String m_strOxidationSequence = null;
	private String m_strCoOCOSequence = null;
	private String m_strSkeltonCode = null;

	/**
	 * Get string of oxidation sequence of this backbone carbons.
	 * Set number of oxidation to each carbon.
	 * @return String of oxidation sequence of the backbone carbons
	 */
	public String getOxidationSequence() {
		if ( this.m_strOxidationSequence != null )
			return this.m_strOxidationSequence;

		for ( Atom atom : this ) {
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
		if ( this.m_strCoOCOSequence != null )
			return this.m_strCoOCOSequence;

		// Using CarbonIdentifier
		CarbonIdentifier identC = new CarbonIdentifier();
//		int ii = 0;
		for(Atom atom : this){
			identC.setAtom(atom);
			if ( identC.isCarboxyLike() )
				{ this.m_strCoOCOSequence += "0"; continue; }
			if ( identC.isAldehydeLike() || identC.isKetoneLike() || identC.isKetalLike() || identC.isAcetalLike() )
				{ this.m_strCoOCOSequence += "1"; continue; }
			this.m_strCoOCOSequence += "0";
//			ii++;
//			if(ii>=minBackboneLength) break;
		}
		return this.m_strCoOCOSequence;
	}

	public void setSkeltonCode( String code ) {
		this.m_strSkeltonCode = code;
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
		for(Atom atom1 : this){
			for(Connection connect1 : atom1.getConnections()){
				Atom con1Atom = connect1.endAtom();
				if(con1Atom.getSymbol().equals("C")) continue;
				if(this.contains(con1Atom)) continue;
				for(Connection connect2 : con1Atom.getConnections()){
					Atom con2Atom = connect2.endAtom();
					if(con2Atom == atom1) continue;
					if(this.contains(con2Atom)){
						return atom1;
					}
				}
			}
		}
		return null;
	}


}

package util.analytical;

import chemicalgraph2.Connection;

/**
 * Class for carbon identify
 * @author MasaakiMatsubara
 *
 */
public class CarbonIdentifier extends AtomIdentifier {

	/**
	 * Return true if the atom is oxygen of hydroxy group(R-OH). Return false otherwise.
	 * @return true if the atom is oxygen of hydroxy group(R-OH). false otherwise.
	 */
	public boolean isHydroxy(){
		int num1R    = 0;
		int numOther = 0;
		if ( !this.m_objAtom.getSymbol().equals("O") ) return false;
		for ( Connection connection : this.m_objAtom.getConnections() ) {
			if ( connection.endAtom().getSymbol().equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				num1R++; continue;
			}
			numOther++;
		}
		return (num1R==1 && numOther==0);
	}

	/**
	 * Return true if the atom is nitrogen of (R-NHn). Return false otherwise.
	 * @return true if the atom is nitrogen of (R-NHn). false otherwise.
	 */
	public boolean isNHn(){
		int num1R    = 0;
		int numOther = 0;
		if ( !this.m_objAtom.getSymbol().equals("N") ) return false;
		for ( Connection connection : this.m_objAtom.getConnections() ) {
			if ( connection.endAtom().getSymbol().equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				num1R++; continue;
			}
			numOther++;
		}
		return (num1R==1 && numOther==0);
	}

	/**
	 * Return true if the atom is carbon of acetal like group(R3-C(OR1)(OR2)-H). Return false otherwise.
	 * @return true if the atom is carbon of acetal like group(R3-C(OR1)(OR2)-H). false otherwise.
	 */
	public boolean isAcetalLike(){
		int num1C    = 0;
		int num1NOS  = 0;
		int numOther = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				if ( symbol.equals("C") ) { num1C++; continue; }
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num1NOS++; continue; }
			}
			numOther++;
		}
		return (num1C==1 && num1NOS==2 && numOther==0);
	}

	/**
	 * Return true if the atom is carbon of aldehyde like group. Return false otherwise.
	 * @return true if the atom is carbon of aldehyde like group. false otherwise.
	 */
	public boolean isAldehydeLike(){
		int num1C    = 0;
		int num2NOS  = 0;
		int numOther = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				if ( symbol.equals("C") ) { num1C++; continue; }
			}
			if ( connection.getBond().getType() == 2 ) {
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num2NOS++; continue; }
			}
			numOther++;
		}
		return (num1C==1 && num2NOS==1 && numOther == 0);
	}

	/**
	 * Return true if the atom is carbon of carboxy like group(R-COOH). Return false otherwise.
	 * @return true if the atom is carbon of carboxy like group(R-COOH). false otherwise.
	 */
	public boolean isCarboxyLike(){
		int num1C    = 0;
		int num1NOS  = 0;
		int num2NOS  = 0;
		int numOther = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				if ( symbol.equals("C") ) { num1C++; continue; }
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num1NOS++; continue; }
			}
			if( connection.getBond().getType() == 2 ) {
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num2NOS++; continue; }
			}
			numOther++;
		}
		return (num1C==1 && num1NOS==1 && num2NOS==1 && numOther==0);
	}

	/**
	 * Return true if the atom is carbon of ketal like group, Ketal(R3-C(OR1)(OR2)-R4), HemiKetal(R3-C(OH) (OR2)-R4), Aminal(R1-C(NR2)(NR3)-R4) or HemiAminal(R1-C(NR2)(OR3)-R4). Return false otherwise.
	 * @return true if the atom is carbon of Ketal(R3-C(OR1)(OR2)-R4), HemiKetal(R3-C(OH) (OR2)-R4), Aminal(R1-C(NR2)(NR3)-R4) or HemiAminal(R1-C(NR2)(OR3)-R4). false otherwise.
	 */
	public boolean isKetalLike(){
		int num1C    = 0;
		int num1NOS  = 0;
		int numOther = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				if ( symbol.equals("C") ) { num1C++; continue; }
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num1NOS++; continue; }
			}
			numOther++;
		}
		return (num1C==2 && num1NOS==2 && numOther==0);
	}

	/**
	 * Return true if the atom is carbon of ketone like group. Return false otherwise.
	 * @return true if the atom is carbon of ketone like group. false otherwise.
	 */
	public boolean isKetoneLike(){
		int num2NOS = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if( connection.getBond().getType() == 2 ) {
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num2NOS++; continue; }
			}
		}
		return (num2NOS==1);
	}

	/**
	 * Return true if the atom is carbon of lactone like group. Return false otherwise.
	 * @return true if the atom is carbon of lactone like group. false otherwise.
	 */
	public boolean isLactoneLike(){
		int num1C    = 0;
		int num1NOS  = 0;
		int num2NOS  = 0;
		int numOther = 0;
		if(!this.m_objAtom.getSymbol().equals("C")) return false;
		for(Connection connection : this.m_objAtom.getConnections()){
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("H") ) continue;
			if ( connection.getBond().getType() == 1 ) {
				if ( symbol.equals("C") ) { num1C++; continue; }
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num1NOS++; continue; }
			}
			if( connection.getBond().getType() == 2 ) {
				if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") ) { num2NOS++; continue; }
			}
			numOther++;
		}
		return (num1C==1 && num1NOS==1 && num2NOS==1 && numOther==0) ? true : false;
	}

/*
	public boolean isAnomer() {
		if(!this.isBackbone()) return false;
		return (this.m_objAtom.backbone.getAnomer() == this) ? true : false;
	}
*/

}

package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Class for carbon identify
 * @author MasaakiMatsubara
 *
 */
public class CarbonIdentifier extends AtomIdentifier {

	@Override
	public CarbonIdentifier setAtom(Atom atom) {
		super.setAtom(atom);
		return this;
	}

	/**
	 * Count connected oxygens
	 * @return Number of connected oxygens.
	 */
	public int countConnectedO() {
		int numO = 0;
		for ( Connection connection : this.m_objAtom.getConnections() ) {
			String symbol = connection.endAtom().getSymbol();
			if ( !symbol.equals("O") ) continue;
			int type = connection.getBond().getType();
			if ( type != 1 && type != 2 && type != 3 ) continue;
			numO += type;
		}
		return numO;
	}

	/**
	 * Count connected N, O, and S atoms
	 * @return Number of N, O, and S atom.
	 */
	public int countConnectedNOS(){
		int numNOS = 0;
		for ( Connection connection : this.m_objAtom.getConnections() ) {
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") )
				numNOS++;
		}
		return numNOS;
	}

	/**
	 * TODO: Remove or move
	 * Whether or not the atom is oxygen of hydroxy group(R-OH).
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
	 * TODO: Remove or move
	 * Whether or not the atom is nitrogen of (R-NHn).
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
	 * Whether or not the atom like carbon of acetal group(R3-C(OR1)(OR2)-H).
	 * @return true if the atom like carbon of acetal group(R3-C(OR1)(OR2)-H). false otherwise.
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
	 * Whether or not the atom is carbon of aldehyde like group.
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
	 * Whether or not the atom is carbon of carboxy like group(R-COOH).
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
	 * Whether or not the atom is carbon of ketal like group,<br>
	 * which is Ketal(R3-C(OR1)(OR2)-R4), HemiKetal(R3-C(OH)(OR2)-R4), Aminal(R1-C(NR2)(NR3)-R4) or HemiAminal(R1-C(NR2)(OR3)-R4).
	 * @return true if the atom is carbon of Ketal, HemiKetal, Aminal or HemiAminal. false otherwise.
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
	 * Whether or not the atom is carbon of ketone like group.
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


	public boolean isAnomericLike() {
		if ( isCarboxyLike() ) return false;
		if ( isAldehydeLike() || isAcetalLike() || isKetoneLike() || isKetalLike() ) return true;
		return false;
	}


}

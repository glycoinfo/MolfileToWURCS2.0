package org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.Chemical;

/**
 * Class for Atom identifier
 * @author Masaaki Matsubara
 *
 */
public class AtomIdentifier {

	protected Atom m_objAtom;

	public AtomIdentifier setAtom(Atom a_objAtom) {
		this.m_objAtom = a_objAtom;
		return this;
	}

	public void clear() {
		this.m_objAtom = null;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------

	/**
	 * Get sum of all bond orders
	 * @return sum of all bond orders
	 */
	public int getSumBondOrders() {
		int nSumOrder=0;
		for ( Connection con : this.m_objAtom.getConnections() ) {
			nSumOrder += con.getBond().getType();
		}
		return nSumOrder;
	}

	/**
	 * Count bond type
	 * @param type Number of bond order
	 * @return number of bond type which is "type"
	 */
	public int countBondType(int type) {
		int nType = 0;
		for ( Connection con : this.m_objAtom.getConnections() ) {
			if ( con.getBond().getType() != type ) continue;
			nType++;
		}
		return nType;
	}

	/**
	 * Get valence number of the atom
	 * @return number of valence
	 */
	public int getValenceNumber() {
		int nAtomic = Chemical.getAtomicNumber( this.m_objAtom.getSymbol() );
		if (this.m_objAtom.getCharge() != 0 ) nAtomic -= this.m_objAtom.getCharge();
		// First row
		return  ( nAtomic <=  2 )? nAtomic      : // First row (1,2)
				( nAtomic <= 10 )? nAtomic - 2  : // Second row (2~10)
				( nAtomic <= 18 )? nAtomic - 10 : // Third row (11~18)
				( nAtomic <= 20 )? nAtomic - 18 : // Fourth row (19,20)
				( nAtomic <= 29 )? 2            : // Fourth row (21~29:transision metals)
				( nAtomic <= 36 )? nAtomic - 27 : // Fourth row (30~36)
				0;
	}

	/**
	 * Get number of lone pair which the atom has calculated by atomic number and charge
	 * @return Number of lone pair
	 */
	public int countLonePair() {
		int nAtomic = Chemical.getAtomicNumber( this.m_objAtom.getSymbol() );
		int nValence = this.getValenceNumber();
		return  ( nAtomic <=  2 )? 0 :
				( nAtomic <= 10 )? Math.max( nValence - 4, 0 ) :
				( nAtomic <= 18 )? Math.max( nValence - 4 - this.countBondType(2), 0) :
				0;
	}

	/**
	 * Get number of hidden bond
	 * @return Number of hidden bond
	 */
	public int getHiddenBondNumber() {
		int nValence = this.getValenceNumber();
		int nSumOrder = this.getSumBondOrders();
		int nLP = this.countLonePair();
		return nValence-nSumOrder-nLP*2;
	}

	/**
	 * Return String of hybrid orbital "sp", "sp2", "sp3" or "" using atom valences.
	 * @return Hybrid orbital("sp", "sp2", "sp3" or "")
	 */
	public String getHybridOrbital0() {
		int nOrbital = this.m_objAtom.getConnections().size() + this.countLonePair();
		return  ( nOrbital == 4 )? "sp3" :
				( nOrbital == 3 )? "sp2" :
				( nOrbital == 2 )? "sp"  :
				"";
	}

	/**
	 * Return String of hybrid orbital "sp", "sp2", "sp3" or "".
	 * @return hybridOrbital("sp", "sp2", "sp3" or "")
	 */
	public String getHybridOrbital(){
		// return only for stereo determining, see InChI's doc.
		// 立体判定に必要なケースのみ出力 ※InChIのドキュメントを参照
		// sp2
		if(this.is("C" , 0, 2, 1, 0)) return "sp2";
		if(this.is("Si", 0, 2, 1, 0)) return "sp2";
		if(this.is("Ge", 0, 2, 1, 0)) return "sp2";
		if(this.is("N" , 0, 1, 1, 0)) return "sp2";
		if(this.is("N" , 1, 2, 1, 0)) return "sp2";
		if(this.is("O",  0, 0, 1, 0)) return "sp2";

		// sp3
		if(this.is("B", -1, 4, 0, 0)) return "sp3";

		if(this.is("C",  0, 4, 0, 0)) return "sp3";
		if(this.is("Si", 0, 4, 0, 0)) return "sp3";
		if(this.is("Ge", 0, 4, 0, 0)) return "sp3";
		if(this.is("Sn", 0, 4, 0, 0)) return "sp3";

		if(this.is("N",  1, 4, 0, 0)) return "sp3";
		if(this.is("N",  0, 3, 1, 0)) return "sp3";
		if(this.is("N",  0, 3, 0, 0)) return "sp3";
		if(this.is("P",  1, 4, 0, 0)) return "sp3";
		if(this.is("P",  0, 3, 1, 0)) return "sp3";
		if(this.is("As", 1, 4, 0, 0)) return "sp3";

		if(this.is("O",  0, 2, 0, 0)) return "sp3";
		if(this.is("S",  1, 3, 1, 0)) return "sp3";
		if(this.is("S",  1, 3, 0, 0)) return "sp3";
		if(this.is("S",  0, 2, 2, 0)) return "sp3";
		if(this.is("S",  0, 2, 1, 0)) return "sp3";
		if(this.is("Se", 1, 3, 1, 0)) return "sp3";
		if(this.is("Se", 1, 3, 0, 0)) return "sp3";
		if(this.is("Se", 0, 2, 2, 0)) return "sp3";
		if(this.is("Se", 0, 2, 1, 0)) return "sp3";

		// sp ※InChIのドキュメントになかったので気がついたケースについて記述
		// 主鎖炭素判定に必要な分をとりあえず記述
		if(this.is("C" , 0, 1, 0, 1)) return "sp";
		if(this.is("C" , 0, 0, 2, 0)) return "sp";
		if(this.is("C" , null, null, 0, 0)) return "sp3";

		return "";
	}

	/**
	 * Return the oxidation number.
	 * @return the oxidation number
	 */
	public int getOxidationNumber(){
		int oxidationNumber = 0;
		for(Connection connection : this.m_objAtom.getConnections()){
			int type = connection.getBond().getType();
			if(     type == 2){          oxidationNumber += 1; }
			else if(type == 3){          oxidationNumber += 2; }

			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") )
				oxidationNumber++;
		}
		return oxidationNumber;
	}

	/**
	 * Wether or not the atom satisfy input conditions. For number of single bond,
	 * @param symbol Atom symbol
	 * @param charge Atom charge
	 * @param singleBondNum Number of single bond which have the atom
	 * @param doubleBondNum Number of double bond which have the atom
	 * @param tripleBondNum Number of triple bond which have the atom
	 * @return true if the atom satisfy input conditions.
	 */
	public boolean is(String symbol, Integer charge, Integer singleBondNum, Integer doubleBondNum, Integer tripleBondNum){
		if((symbol != null) && !this.m_objAtom.getSymbol().equals(symbol)) return false;
		if((charge != null) && this.m_objAtom.getCharge() != charge) return false;
		if((singleBondNum != null) && this.countBondType(1) >singleBondNum) return false;
		if((doubleBondNum != null) && this.countBondType(2)!=doubleBondNum) return false;
		if((tripleBondNum != null) && this.countBondType(3)!=tripleBondNum) return false;
		return true;
	}

	/**
	 * Return true if the atom is metal atom. Return false otherwise.
	 * @return true if the atom is metal atom. false otherwise.
	 */
	public boolean isMetal(){
		return Chemical.isMetal(this.m_objAtom.getSymbol());
	}

	public int getMaxDepth() {
		int maxdepth = 0;
		LinkedList<Atom> ancestors = new LinkedList<Atom>();
		ancestors.addLast(this.m_objAtom);
		maxdepth = this.getMaxDepth(ancestors);
		return maxdepth;
	}

	private int getMaxDepth(LinkedList<Atom> ancestors) {
		int maxdepth = ancestors.size();
		for(Connection connection : ancestors.getLast().getConnections()){
			Atom atom = connection.endAtom();
			if(ancestors.contains(atom)) continue;
			ancestors.addLast(atom);
			maxdepth = Math.max(this.getMaxDepth(ancestors), maxdepth);
			ancestors.removeLast();
		}
		return maxdepth;
	}

}

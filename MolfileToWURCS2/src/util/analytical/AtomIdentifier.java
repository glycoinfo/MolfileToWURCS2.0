package util.analytical;

import java.util.LinkedList;

import util.Chemical;
import chemicalgraph2.Atom;
import chemicalgraph2.Connection;

/**
 * Class for Atom identifier
 * @author Masaaki Matsubara
 *
 */
public class AtomIdentifier {

	protected Atom m_objAtom;

	public AtomIdentifier() { }

	public void setAtom(Atom a_objAtom) {
		this.m_objAtom = a_objAtom;
	}

	public void clear() {
		this.m_objAtom = null;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
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
	 * Oxygen is N, O, S...
	 * @return true if this contain element which connect with 2 N, O, or S atom.
	 */
	public boolean connectsTwoNOS(){
		int numNOS = 0;
		for ( Connection connection : this.m_objAtom.getConnections() ) {
			String symbol = connection.endAtom().getSymbol();
			if ( symbol.equals("N") || symbol.equals("O") || symbol.equals("S") )
				numNOS++;
		}
		return (numNOS==2);
	}

	/**
	 * Return true if the input conditions are satisfied
	 * @param symbol
	 * @param charge
	 * @param singleBondNum
	 * @param doubleBondNum
	 * @param tripleBondNum
	 * @return true if the input conditions are satisfied.
	 */
	public boolean is(String symbol, Integer charge, Integer singleBondNum, Integer doubleBondNum, Integer tripleBondNum){
		if((symbol != null) && !this.m_objAtom.getSymbol().equals(symbol)) return false;
		if((charge != null) && this.m_objAtom.getCharge() != charge) return false;
		int nSingle = 0;
		int nDouble = 0;
		int nTriple = 0;
		for(Connection connection : this.m_objAtom.getConnections()){
			if(connection.getBond().getType() == 1) nSingle++;
			if(connection.getBond().getType() == 2) nDouble++;
			if(connection.getBond().getType() == 3) nTriple++;
		}
		if((singleBondNum != null) && nSingle >singleBondNum) return false;
		if((doubleBondNum != null) && nDouble!=doubleBondNum) return false;
		if((tripleBondNum != null) && nTriple!=tripleBondNum) return false;
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

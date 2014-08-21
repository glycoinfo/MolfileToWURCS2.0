package chemicalgraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import chemicalgraph.subgraph.backbone.BackboneList;

import utility.HierarchicalDigraph;

/**
 * @author KenichiTanaka
 */
public class ConnectionList extends LinkedList<Connection>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;
	
	public boolean isUniqOrder;
	public boolean tmpflg;
	
	//----------------------------
	// Public method (void)
	//----------------------------
	/**
	 * sort list by CIP order.
	 * CIP order can be set by utility.HierarchicalDigraph
	 * 
	 * @see HierarchicalDigraph
	 * @see <a href="http://homepage1.nifty.com/nomenclator/text/seqrule.htm">化合物命名法談義</a>
	 */
	public void sortByCIPorder(){
		Collections.sort(this, new Comparator<Connection>() {
			public int compare(Connection connection1, Connection connection2) {
				return connection1.CIPorder - connection2.CIPorder;
			}
		});
	}
	
	/**
	 * @param backbones
	 */
	public void sortForCanonicalWURCS(final BackboneList backbones) {
		Collections.sort(this, new Comparator<Connection>() {
			public int compare(Connection connection1, Connection connection2) {
				// １．主鎖番号backboness.indexOf(connection.start().backbone)
				int backboneNo1 = backbones.indexOf(connection1.start().backbone);
				int backboneNo2 = backbones.indexOf(connection2.start().backbone);
				if(backboneNo1 != backboneNo2) return backboneNo1 - backboneNo2;
				
				// ２．主鎖の第何位の炭素か？connection.start().backbone.indexOf(connection.start())
				int backboneAtomNo1 = connection1.start().backbone.indexOf(connection1.start());
				int backboneAtomNo2 = connection2.start().backbone.indexOf(connection2.start());
				if(backboneAtomNo1!=backboneAtomNo2) return backboneAtomNo1 - backboneAtomNo2;

				// ３．主鎖からみたconnectionのCIP順位connection.CIPorder
				// CIPで順序を付けた後、主鎖炭素鎖の順位が低い炭素を手前に持ってきた時、0, 1, 2, 3, e, z, x, ?のいずれかが入っている
				String stereoForWURCS1 = connection1.stereoForWURCS;
				String stereoForWURCS2 = connection2.stereoForWURCS;
				if(!stereoForWURCS1.equals(stereoForWURCS2)) return stereoForWURCS1.compareTo(stereoForWURCS2);
				
				// 4．修飾第何位の原子か？modAtoms.indexOf(connection.atom)
				int modAtomNo1 = connection1.atom.modification.atomsOfModification.indexOf(connection1.atom);
				int modAtomNo2 = connection2.atom.modification.atomsOfModification.indexOf(connection2.atom);
				if(modAtomNo1!=modAtomNo2) return modAtomNo1 - modAtomNo2;
				
				return 0;
			}
		});
	}

	/**
	 * @param b
	 */
	public void setTmpFlg(final boolean b) {
		this.tmpflg = b;
		for(Connection connection : this){
			connection.tmpflg = b;
		}
	}

	/**
	 * HierarchicalDigraphの全構築が完了したかどうかのフラグをセット
	 * @param b
	 */
	public void setIsCompletedFullSearch(boolean b) {
		for(Connection connection : this){
			connection.isCompletedFullSearch = b;
		}
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * @return backbones in this list
	 */
	public BackboneList backbones(){
		BackboneList backbones = new BackboneList();
		for(Connection connection : this){
			if(connection.atom.isBackbone()) backbones.add(connection.atom.backbone);
		}
		return backbones;
	}
	
	/**
	 * @param atom
	 * @return connection which have input atom
	 */
	public Connection getConnect(final Atom atom){
		for(Connection connection : this){
			if(connection.atom == atom) return connection;
		}
		return null;
	}
	
	/**
	 * @param atoms
	 * @return the number of connections which connect input atoms
	 */
	public int count(final AtomList atoms){
		int num = 0;
		for(Connection connection : this){
			if(atoms.contains(connection.atom)) num++;
		}
		return num;
	}
	
	/**
	 * @return the number of connection which connect with backbone
	 */
	public int countBackbone(){
		int num = 0;
		for(Connection connection : this){
			if(connection.atom.isBackbone()) num++;
		}
		return num;
	}

	/**
	 * @param type
	 * @return the number of bond which type is input type.
	 */
	public int countBond(final int type){
		int num = 0;
		for(Connection connection : this){
			if(connection.bond.type == type) num++;
		}
		return num;
	}
	
	/**
	 * @return the number of single bond.
	 */
	public int countSingleBond(){
		return this.countBond(1);
	}
	
	/**
	 * @return the number of double bond.
	 */
	public int countDoubleBond(){
		return this.countBond(2);
	}
	
	/**
	 * @return the number of triple bond.
	 */
	public int countTripleBond(){
		return this.countBond(3);
	}

	/**
	 * Oxygen is N, O, S...
	 * @return true if this contain element which connect with 2 N, O, or S atom.
	 */
	public boolean containsTwoNOS(){
		int numNOS = 0;
		for(Connection connection : this){
			if(     connection.atom.symbol.equals("N")){ numNOS++; }
			else if(connection.atom.symbol.equals("O")){ numNOS++; }
			else if(connection.atom.symbol.equals("S")){ numNOS++; }
		}
		return (numNOS==2) ? true : false;
	}
	
	/**
	 * @param atom
	 * @return true if this contains elements which atom is input atom.
	 */
	public boolean contains(final Atom atom){
		for(Connection connection : this){
			if(connection.atom == atom) return true;
		}
		return false;
	}
	
	/**
	 * @param bond
	 * @return true if this contains elements which bond is input bond.
	 */
	public boolean contains(final Bond bond){
		for(Connection connection : this){
			if(connection.bond == bond) return true;
		}
		return false;
	}
	
	/**
	 * @param atom
	 * @return remove all connections from this list which atom is input atom.
	 */
	public boolean remove(final Atom atom){
		ConnectionList removeConnects = new ConnectionList();
		for(Connection connection : this){
			if(connection.atom == atom){
				removeConnects.add(connection);
			}
		}
		
		boolean flg = false;
		for(Connection connection : removeConnects){
			if(this.remove(connection)) flg = true;
		}
		return flg;
	}
}

package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Comparator for HierarchicalDigraph
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphComparator implements Comparator<HierarchicalDigraphNode>{

	private static final double EPS = 0.000000001;
	private boolean m_bFoundSameBranch;

	public boolean foundSameBranch() {
		return this.m_bFoundSameBranch;
	}

	public int compare(HierarchicalDigraphNode a_oNode1, HierarchicalDigraphNode a_oNode2) {
		LinkedList<HierarchicalDigraphNode> t_aGraph1Width = new LinkedList<HierarchicalDigraphNode>();
		LinkedList<HierarchicalDigraphNode> t_aGraph2Width = new LinkedList<HierarchicalDigraphNode>();

		this.m_bFoundSameBranch = false;

		// Compare atomic number using width search
		// 1. First, prioritize the atom which has greater atomic number.
		// 2. Next, compare next atoms by first compare method.
		// 3. Repeat compare.
		// (1)直接結合する原子の原子番号が大きい方を優位とする。
		// (2)前項で決まらないときは、最初の原子に結合している原子（すなわち中心から2番目の原子）について (i) の基準で比べる。2番目の原子が最初の原子に複数結合しているときは、原子番号の大きい順に候補を1つずつ出して違いのあった時点で決める。
		// (3)前項で決まらないときは、中心から2番目の原子（ただし、(ii)で除外された原子は除く）に結合している原子で比べる。以降、順に中心から離れた原子を比べる。
		t_aGraph1Width.addLast(a_oNode1);
		t_aGraph2Width.addLast(a_oNode2);
		while(t_aGraph1Width.size()!=0 && t_aGraph2Width.size()!=0){
			HierarchicalDigraphNode t_oNode1 = t_aGraph1Width.removeFirst();
			HierarchicalDigraphNode t_oNode2 = t_aGraph2Width.removeFirst();

			// Prioritize greater atomic number
			if(Math.abs(t_oNode1.getAverageAtomicNumber() - t_oNode2.getAverageAtomicNumber()) > EPS){
				return (t_oNode1.getAverageAtomicNumber() > t_oNode2.getAverageAtomicNumber()) ? -1 : 1;
			}

			// Prioritize exist atom
			if ( t_oNode1.getConnection() != null && t_oNode2.getConnection() == null ) return -1;
			if ( t_oNode1.getConnection() == null && t_oNode2.getConnection() != null ) return 1;

			// Check for comaparing same branch
			Connection t_oConn1 = t_oNode1.getConnection();
			Connection t_oConn2 = t_oNode2.getConnection();
			if ( t_oConn1 != null && t_oConn2 != null && t_oConn1.equals(t_oConn2) ) {
				this.m_bFoundSameBranch = true;
				return 0;
			}

			// Compare number of children
			LinkedList<HierarchicalDigraphNode> t_aChildren1 = t_oNode1.getChildren();
			LinkedList<HierarchicalDigraphNode> t_aChildren2 = t_oNode2.getChildren();
			// Sort children of each node
			Collections.sort(t_aChildren1, this);
			Collections.sort(t_aChildren2, this);

			// Add children to serch list
			for ( HierarchicalDigraphNode t_oChild1 : t_aChildren1 ) {
				t_aGraph1Width.addLast(t_oChild1);
			}
			for ( HierarchicalDigraphNode t_oChild2 : t_aChildren2 ) {
				t_aGraph2Width.addLast(t_oChild2);
			}

			int t_iDiff = t_aChildren1.size() - t_aChildren2.size();
			if ( t_iDiff == 0 ) continue;

			// Add pseudo node as child of digraph having less number of children
			HierarchicalDigraphNode t_oPseudoNode = new HierarchicalDigraphNode(null, 0.0);
			LinkedList<HierarchicalDigraphNode> t_aWidth = (t_iDiff<0)? t_aGraph1Width : t_aGraph2Width;
			for ( int i=0; i<Math.abs(t_iDiff); i++ )
				t_aWidth.addLast(t_oPseudoNode);
		}

		// (4)2つの基が位相的に等しい（構成する原子の元素、個数、結合順序が等しい）が、質量数が異なる原子を含む場合、質量数の大きい原子を含む基を優位とする。
		// WURCS生成時には、正規化の段階で同位体情報は削除されるので、この比較は行わない。
		// Compare isotope (this comparison is not perfomed here since isomer infomation is reduced at normalization.)

		return 0;
	}
}

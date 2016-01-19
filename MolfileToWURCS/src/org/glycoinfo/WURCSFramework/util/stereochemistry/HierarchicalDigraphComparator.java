package org.glycoinfo.WURCSFramework.util.stereochemistry;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

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
		LinkedList<HierarchicalDigraphNode> t_oGraph1Width = new LinkedList<HierarchicalDigraphNode>();
		LinkedList<HierarchicalDigraphNode> t_oGraph2Width = new LinkedList<HierarchicalDigraphNode>();

		this.m_bFoundSameBranch = false;

		// Compare atomic number using width search
		// 1. First, prioritize the atom which has greater atomic number.
		// 2. Next, compare next atoms by first compare method.
		// 3. Repeat compare.
		// (1)直接結合する原子の原子番号が大きい方を優位とする。
		// (2)前項で決まらないときは、最初の原子に結合している原子（すなわち中心から2番目の原子）について (i) の基準で比べる。2番目の原子が最初の原子に複数結合しているときは、原子番号の大きい順に候補を1つずつ出して違いのあった時点で決める。
		// (3)前項で決まらないときは、中心から2番目の原子（ただし、(ii)で除外された原子は除く）に結合している原子で比べる。以降、順に中心から離れた原子を比べる。
		t_oGraph1Width.addLast(a_oNode1);
		t_oGraph2Width.addLast(a_oNode2);
		while(t_oGraph1Width.size()!=0 && t_oGraph2Width.size()!=0){
			HierarchicalDigraphNode t_oNode1 = t_oGraph1Width.removeFirst();
			HierarchicalDigraphNode t_oNode2 = t_oGraph2Width.removeFirst();

			// Check for comaparing same branch
			Connection t_oToParent1 = t_oNode1.getConnection();
			Connection t_oToParent2 = t_oNode2.getConnection();
			if ( t_oToParent1 != null && t_oToParent2 != null && t_oToParent1.equals(t_oToParent2) ) {
				this.m_bFoundSameBranch = true;
				return 0;
			}

			// Prioritize greater atomic number
			if(Math.abs(t_oNode1.getAverageAtomicNumber() - t_oNode2.getAverageAtomicNumber()) > EPS){
				return (t_oNode1.getAverageAtomicNumber() > t_oNode2.getAverageAtomicNumber()) ? -1 : 1;
			}

			// Compare number of children
			LinkedList<HierarchicalDigraphNode> t_aChildren1 = t_oNode1.getChildren();
			LinkedList<HierarchicalDigraphNode> t_aChildren2 = t_oNode2.getChildren();
			// Prioritize digraph having much number of children
			if(t_aChildren1.size() != t_aChildren2.size()) return t_aChildren2.size() - t_aChildren1.size();
			// Sort children of each node
			Collections.sort(t_aChildren1, this);
			Collections.sort(t_aChildren2, this);

			// Add children to serch list
			for ( HierarchicalDigraphNode t_oChild1 : t_aChildren1 ) {
				t_oGraph1Width.addLast(t_oChild1);
			}
			for ( HierarchicalDigraphNode t_oChild2 : t_aChildren2 ) {
				t_oGraph2Width.addLast(t_oChild2);
			}
		}

		// (4)2つの基が位相的に等しい（構成する原子の元素、個数、結合順序が等しい）が、質量数が異なる原子を含む場合、質量数の大きい原子を含む基を優位とする。
		// WURCS生成時には、正規化の段階で同位体情報は削除されるので、この比較は行わない。
		// Compare isotope (this comparison is not perfomed here since isomer infomation is reduced at normalization.)

		return 0;
	}
}

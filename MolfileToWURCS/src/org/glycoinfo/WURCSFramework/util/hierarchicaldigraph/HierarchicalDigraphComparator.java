package org.glycoinfo.WURCSFramework.util.hierarchicaldigraph;

import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Comparator for HierarchicalDigraph
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphComparator implements Comparator<HierarchicalDigraph>{

	private static final double EPS = 0.000000001;
	private boolean m_bFoundSameBranch;

	public boolean foundSameBranch() {
		return this.m_bFoundSameBranch;
	}

	public int compare(HierarchicalDigraph a_objGraph1, HierarchicalDigraph a_objGraph2) {
		LinkedList<HierarchicalDigraph> widthsearch1 = new LinkedList<HierarchicalDigraph>();
		LinkedList<HierarchicalDigraph> widthsearch2 = new LinkedList<HierarchicalDigraph>();

		this.m_bFoundSameBranch = false;

		// Compare atomic number using width search
		// 1. First, prioritize the atom which has greater atomic number.
		// 2. Next, compare next atoms by first compare method.
		// 3. Repeat compare.
		// (1)直接結合する原子の原子番号が大きい方を優位とする。
		// (2)前項で決まらないときは、最初の原子に結合している原子（すなわち中心から2番目の原子）について (i) の基準で比べる。2番目の原子が最初の原子に複数結合しているときは、原子番号の大きい順に候補を1つずつ出して違いのあった時点で決める。
		// (3)前項で決まらないときは、中心から2番目の原子（ただし、(ii)で除外された原子は除く）に結合している原子で比べる。以降、順に中心から離れた原子を比べる。
		widthsearch1.addLast(a_objGraph1);
		widthsearch2.addLast(a_objGraph2);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraph graph1 = widthsearch1.removeFirst();
			HierarchicalDigraph graph2 = widthsearch2.removeFirst();

			// Check for comaparing same branch
			Connection t_oToParent1 = graph1.getConnectionToParent();
			Connection t_oToParent2 = graph2.getConnectionToParent();
			if ( t_oToParent1 != null && t_oToParent2 != null && t_oToParent1.equals(t_oToParent2) ) {
				this.m_bFoundSameBranch = true;
				return 0;
			}

			// 原子番号が大きい方を優位
			// Prioritize greater atomic number
			if(Math.abs(graph1.getAverageAtomicNumber() - graph2.getAverageAtomicNumber()) > EPS){
				return (graph1.getAverageAtomicNumber() > graph2.getAverageAtomicNumber()) ? -1 : 1;
			}

			// Compare children
			LinkedList<HierarchicalDigraph> children1 = graph1.getChildren();
			LinkedList<HierarchicalDigraph> children2 = graph2.getChildren();
			int minChildNum = Math.min(children1.size(), children2.size());
			for(int ii=0; ii<minChildNum; ii++){
				double averageAtomicNumber1 = children1.get(ii).getAverageAtomicNumber();
				double averageAtomicNumber2 = children2.get(ii).getAverageAtomicNumber();
				if(Math.abs(averageAtomicNumber1 - averageAtomicNumber2) > EPS){
					return (averageAtomicNumber1 > averageAtomicNumber2) ? -1 : 1;
				}
			}

			// Prioritize digraph having much number of children
			if(children1.size() != children2.size()) return children2.size() - children1.size();

			// Add children to serch list
			for ( HierarchicalDigraph child1 : children1 ) {
				widthsearch1.addLast(child1);
			}
			for ( HierarchicalDigraph child2 : children2 ) {
				widthsearch2.addLast(child2);
			}
		}

		// (4)2つの基が位相的に等しい（構成する原子の元素、個数、結合順序が等しい）が、質量数が異なる原子を含む場合、質量数の大きい原子を含む基を優位とする。
		// WURCS生成時には、正規化の段階で同位体情報は削除されるので、この比較は行わない。
		// Isomer infomation is reduced at normalization, this comparison is not perfomed.

		return 0;
	}
}

package org.glycoinfo.WURCSFramework.util.chemicalgraph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Bond;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 *
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphComparator implements Comparator<HierarchicalDigraph>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final double EPS = 0.000000001;
	private boolean m_bEZRSCheck;
	private HashMap<Atom, String> m_hashAtomToStereo;
	private HashMap<Bond, String> m_hashBondToStereo;

	//----------------------------
	// Accessor
	//----------------------------
	public void setCheckType(final boolean EZRSCheck) {
		this.m_bEZRSCheck = EZRSCheck;
	}

	public void setAtomStereos(final HashMap<Atom, String> a_hashAtomToStereo ) {
		this.m_hashAtomToStereo = a_hashAtomToStereo;
	}

	public void setBondStereos(final HashMap<Bond, String> a_hashBondToStereo ) {
		this.m_hashBondToStereo = a_hashBondToStereo;
	}

	public void clear() {
		this.m_hashAtomToStereo.clear();
		this.m_hashBondToStereo.clear();
	}

	//----------------------------
	// Compare
	//----------------------------
	public int compare(HierarchicalDigraph a_objGraph1, HierarchicalDigraph a_objGraph2) {
		LinkedList<HierarchicalDigraph> widthsearch1 = new LinkedList<HierarchicalDigraph>();
		LinkedList<HierarchicalDigraph> widthsearch2 = new LinkedList<HierarchicalDigraph>();

		// Compare atomic number
		// 1. First, prioritize the atom which has greater atomic number.
		// 2. Next, compare next atoms by first compare method.
		// 3.
		// (1)直接結合する原子の原子番号が大きい方を優位とする。
		// (2)前項で決まらないときは、最初の原子に結合している原子（すなわち中心から2番目の原子）について (i) の基準で比べる。2番目の原子が最初の原子に複数結合しているときは、原子番号の大きい順に候補を1つずつ出して違いのあった時点で決める。
		// (3)前項で決まらないときは、中心から2番目の原子（ただし、(ii)で除外された原子は除く）に結合している原子で比べる。以降、順に中心から離れた原子を比べる。
		widthsearch1.addLast(a_objGraph1);
		widthsearch2.addLast(a_objGraph2);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraph graph1 = widthsearch1.removeFirst();
			HierarchicalDigraph graph2 = widthsearch2.removeFirst();

			// 原子番号が大きい方を優位
			// Prioritize greater atomic number
			if(Math.abs(graph1.getAverageAtomicNumber() - graph2.getAverageAtomicNumber()) > EPS){
				return (graph1.getAverageAtomicNumber() > graph2.getAverageAtomicNumber()) ? -1 : 1;
			}

			// 子供の比較
			// Compare child
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

			// 子供の数が異なる場合、少ない方に空原子を追加するが、空原子は空でない原子より劣勢である為、この時点で順位がつく
			// less number of children
			if(children1.size() != children2.size()) return children2.size() - children1.size();

			// 子供をリストに追加
			// Add children
			for ( HierarchicalDigraph child1 : children1 ) {
				widthsearch1.addLast(child1);
			}
			for ( HierarchicalDigraph child2 : children2 ) {
				widthsearch2.addLast(child2);
			}
		}

		// (4)2つの基が位相的に等しい（構成する原子の元素、個数、結合順序が等しい）が、質量数が異なる原子を含む場合、質量数の大きい原子を含む基を優位とする。
		// WURCS生成時には、正規化の段階で同位体情報は削除されるので、この比較は行わない。

		// (5)2つの基が物質的かつ位相的に等しい（構成する原子の元素、個数、結合順序、質量数が等しい）が、立体化学が異なる場合。
		if(this.m_bEZRSCheck) return this.compareEZRS(a_objGraph1, a_objGraph2);

		return 0;
	}

	// (5)2つの基が物質的かつ位相的に等しい（構成する原子の元素、個数、結合順序、質量数が等しい）が、立体化学が異なる場合。
	// まず二重結合の幾何異性に関して、ZをEより優位とする。
	// 次いでジアステレオ異性に関して、like （R,R またはS,S）を unlike （R,S またはS,R）より優位とする。
	// 次いで、鏡像異性に関して、RをS より優位とする。
	// 最後に、擬似不斉原子に関して、rをs より優位とする。
	private int compareEZRS(final HierarchicalDigraph a_objGraph1, final HierarchicalDigraph a_objGraph2){
		LinkedList<HierarchicalDigraph> widthsearch1 = new LinkedList<HierarchicalDigraph>();
		LinkedList<HierarchicalDigraph> widthsearch2 = new LinkedList<HierarchicalDigraph>();
		widthsearch1.addLast(a_objGraph1);
		widthsearch2.addLast(a_objGraph2);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraph graph1 = widthsearch1.removeFirst();
			HierarchicalDigraph graph2 = widthsearch2.removeFirst();

			if ( graph1.getAtom() == null && graph2.getAtom() == null ) continue;
			String atomStereo1 = this.m_hashAtomToStereo.get(graph1.getAtom());
			String atomStereo2 = this.m_hashAtomToStereo.get(graph2.getAtom());

			// Compare children
			int minChildNum = Math.min(graph1.getChildren().size(), graph2.getChildren().size());
			for(int ii=0; ii<minChildNum; ii++){
				HierarchicalDigraph child1 = graph1.getChildren().get(ii);
				HierarchicalDigraph child2 = graph2.getChildren().get(ii);

				// Get connection to parent
				if ( child1.getAtom() == null || child2.getAtom() == null ) continue;
//				if(child1.getAtom()!=null&&child2.getAtom()!=null){ }

				Connection connection1 = child1.getConnectionToParent();
				Connection connection2 = child2.getConnectionToParent();

				// まず二重結合の幾何異性に関して、ZをEより優位とする。
				// For double bond geometrical isomerism, to prioritize "Z" more than "E"
				String bondStereo1 = this.m_hashBondToStereo.get(connection1.getBond());
				String bondStereo2 = this.m_hashBondToStereo.get(connection2.getBond());
				if( bondStereo1!=null && bondStereo1!=null){
					if( bondStereo1.equals("Z") && bondStereo2.equals("E")) return -1;
					if( bondStereo1.equals("E") && bondStereo2.equals("Z")) return 1;
				}

				String conatomStereo1 = this.m_hashAtomToStereo.get(connection1.endAtom());
				String conatomStereo2 = this.m_hashAtomToStereo.get(connection2.endAtom());
				if( conatomStereo1 == null || conatomStereo2 == null) continue;

				// 次いでジアステレオ異性に関して、like （R,R またはS,S）を unlike （R,S またはS,R）より優位とする。
				// For diastereoisomerism, prioritize "like" (R,R or S,S) more than "unlike" (R,S or S,R)
				if( atomStereo1!=null && atomStereo2!=null){
					if( conatomStereo1.equals(atomStereo1) && !conatomStereo2.equals(atomStereo2)) return -1;
					if(!conatomStereo1.equals(atomStereo1) &&  conatomStereo2.equals(atomStereo2)) return 1;
				}

				// 次いで、鏡像異性に関して、RをS より優位とする。
				// For enantiomerism, prioritize "R" more than "S"
				if( conatomStereo1.equals("R") && conatomStereo2.equals("S")) return -1;
				if( conatomStereo1.equals("S") && conatomStereo2.equals("R")) return 1;

				// 最後に、擬似不斉原子に関して、rをs より優位とする。
				// For the atom with pseudoasymmetry, prioritize "r" more than "s"
				if( conatomStereo1.equals("r") && conatomStereo2.equals("s")) return -1;
				if( conatomStereo1.equals("s") && conatomStereo2.equals("r")) return 1;
			}

			// 子供をリストに追加
			// Add children
			for(HierarchicalDigraph child1 : graph1.getChildren()){
				widthsearch1.addLast(child1);
			}
			for(HierarchicalDigraph child2 : graph2.getChildren()){
				widthsearch2.addLast(child2);
			}
		}

		return 0;
	}
}

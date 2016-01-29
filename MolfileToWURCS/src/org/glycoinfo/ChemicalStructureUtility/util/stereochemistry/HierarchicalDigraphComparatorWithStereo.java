package org.glycoinfo.ChemicalStructureUtility.util.stereochemistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;

/**
 * Comparator for HierarchicalDigraph using stereochemistry
 * @author MasaakiMatsubara
 *
 */
public class HierarchicalDigraphComparatorWithStereo extends HierarchicalDigraphComparator {

	private HashMap<Atom, String> m_hashAtomToStereo;
	private HashMap<Bond, String> m_hashBondToStereo;

	public void clear() {
		this.m_hashAtomToStereo.clear();
		this.m_hashBondToStereo.clear();
	}

	public void setAtomStereos(final HashMap<Atom, String> a_hashAtomToStereo ) {
		this.m_hashAtomToStereo = a_hashAtomToStereo;
	}

	public void setBondStereos(final HashMap<Bond, String> a_hashBondToStereo ) {
		this.m_hashBondToStereo = a_hashBondToStereo;
	}

	public int compare(final HierarchicalDigraphNode a_objGraph1, final HierarchicalDigraphNode a_objGraph2){

		// Compare by super class
		int t_iComp = super.compare(a_objGraph1, a_objGraph2);
		if ( t_iComp != 0 ) return t_iComp;

		// (5)2つの基が物質的かつ位相的に等しい（構成する原子の元素、個数、結合順序、質量数が等しい）が、立体化学が異なる場合。
		// まず二重結合の幾何異性に関して、ZをEより優位とする。
		// 次いでジアステレオ異性に関して、like （R,R またはS,S）を unlike （R,S またはS,R）より優位とする。
		// 次いで、鏡像異性に関して、RをS より優位とする。
		// 最後に、擬似不斉原子に関して、rをs より優位とする。
		LinkedList<HierarchicalDigraphNode> widthsearch1 = new LinkedList<HierarchicalDigraphNode>();
		LinkedList<HierarchicalDigraphNode> widthsearch2 = new LinkedList<HierarchicalDigraphNode>();
		widthsearch1.addLast(a_objGraph1);
		widthsearch2.addLast(a_objGraph2);
		while(widthsearch1.size()!=0 && widthsearch2.size()!=0){
			HierarchicalDigraphNode graph1 = widthsearch1.removeFirst();
			HierarchicalDigraphNode graph2 = widthsearch2.removeFirst();

			if ( graph1.getConnection() == null || graph2.getConnection() == null ) continue;
			String atomStereo1 = this.m_hashAtomToStereo.get(graph1.getConnection().endAtom());
			String atomStereo2 = this.m_hashAtomToStereo.get(graph2.getConnection().endAtom());

			// Compare children
			LinkedList<HierarchicalDigraphNode> children1 = graph1.getChildren();
			LinkedList<HierarchicalDigraphNode> children2 = graph2.getChildren();
			Collections.sort(children1, this);
			Collections.sort(children2, this);
			int minChildNum = Math.min(children1.size(), children2.size());
			for(int ii=0; ii<minChildNum; ii++){
				HierarchicalDigraphNode child1 = children1.get(ii);
				HierarchicalDigraphNode child2 = children2.get(ii);

				// Get connection from parent
				if ( child1.getConnection() == null || child2.getConnection() == null ) continue;
//				if(child1.getAtom()!=null&&child2.getAtom()!=null){ }

				Connection connection1 = child1.getConnection();
				Connection connection2 = child2.getConnection();

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
			for(HierarchicalDigraphNode child1 : graph1.getChildren()){
				widthsearch1.addLast(child1);
			}
			for(HierarchicalDigraphNode child2 : graph2.getChildren()){
				widthsearch2.addLast(child2);
			}
		}

		return 0;
	}

}

package org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator;

import java.util.Comparator;

import org.glycoinfo.WURCSFramework.wurcsglycan.Modification;

/**
 * Class for Modificaiton comparison
 * @author MasaakiMatsubara
 * TODO:
 */
public class ModificationComparator implements Comparator<Modification> {

	@Override
	public int compare(Modification m1, Modification m2) {
		// TODO 自動生成されたメソッド・スタブ

		int edgeCount1 = m1.getEdges().size();
		int edgeCount2 = m2.getEdges().size();
		if ( edgeCount1 != edgeCount2 ) return edgeCount1 - edgeCount2;
		return 0;
	}

}

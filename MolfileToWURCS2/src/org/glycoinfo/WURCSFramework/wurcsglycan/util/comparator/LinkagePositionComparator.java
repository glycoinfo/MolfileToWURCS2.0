package org.glycoinfo.WURCSFramework.wurcsglycan.util.comparator;

import java.util.Comparator;

import org.glycoinfo.WURCSFramework.wurcsglycan.LinkagePosition;

public class LinkagePositionComparator implements Comparator<LinkagePosition> {

	@Override
	public int compare(LinkagePosition o1, LinkagePosition o2) {
		// TODO 自動生成されたメソッド・スタブ

		// Compare position number on backbone
		// Prioritize lower position number
		int iPCB1 = o1.getBackbonePosition();
		int iPCB2 = o2.getBackbonePosition();
		if ( iPCB1 != iPCB2 ) return iPCB1 - iPCB2;

		// Compare string of direction on backbone carbon
		// "0" > "1" > "2" > "3" > "e" > "z" > "x"
		String strDMB1 = o1.getDirection();
		String strDMB2 = o2.getDirection();
		if ( !strDMB1.equals(strDMB2) ) return strDMB1.compareTo(strDMB2);

		// Compare position number on modification
		int iPCM1 = o1.getModificationPosition();
		int iPCM2 = o2.getModificationPosition();
		if ( iPCM1 != iPCM2 ) return iPCM1 - iPCM2;

		return 0;
	}

}

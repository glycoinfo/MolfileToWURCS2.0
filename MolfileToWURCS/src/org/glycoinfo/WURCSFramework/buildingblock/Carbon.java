package org.glycoinfo.WURCSFramework.buildingblock;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.CarbonIdentifier;

public class Carbon {

	public static final int UNKNOWN = 0;
	public static final int SP  = 1;
	public static final int SP2 = 2;
	public static final int SP3 = 3;

	private CarbonChain m_oParentChain;
	private Carbon m_oPrevCarbon = null;
	private Carbon m_oNextCarbon = null;

	/**
	 * Connection around the carbon<br>
	 * For SP3 carbon:
	 * <pre>
	 *   P        3        P
	 *   |        |        |
	 * 1-C-2 or 1-C-2 or 1-C-2
	 *   |        |        |
	 *   N        N        3
	 * P: Previous carbon
	 * N: Next carbon
	 * 1: Left side atom in Fisher projection
	 *    (Direction is 'u')
	 * 2: Right side atom in Fisher projection
	 *    (Direction is 'd')
	 * 3: An atom which behave as next or previous carbon if the carbon is start or end of chain
	 *    (Direction is 't')
	 *
	 * For asymmetric carbon, 3 is lower order than 1 and 2.
	 * For symmetric carbon with two same atom, 1 and 2 is same atom and 3 is remain atom (Direction is needed to 1 and 2).
	 * For symmetric carbon with three same atom, direction of all atom is 'n' if exactry same.
	 * and 3 is lowest modification if modification is not same.
	 * </pre>
	 * For SP2 carbon:
	 * <pre>
	 *   P      1   2      P
	 *   |       \ /       |
	 *   C-1 or   C   or   C
	 *   |        |       / \
	 *   N        N      1   2
	 * P: Previous carbon
	 * N: Next carbon
	 * 1: Highest order modification (prioritize higher bond order)
	 * 2: Lowest order modification
	 * </pre>
	 */
	private Atom m_oCarbonAtom;

	public Carbon(Atom a_oCarbon) {
		CarbonIdentifier t_oCI = new CarbonIdentifier();
		t_oCI.setAtom(a_oCarbon);
		this.m_oCarbonAtom = a_oCarbon;
	}

	public Atom getAtom() {
		return this.m_oCarbonAtom;
	}

	public void setNext(Carbon a_oNext) {
		this.m_oNextCarbon = a_oNext;
	}

	public void setPrev(Carbon a_oPrev) {
		this.m_oPrevCarbon = a_oPrev;
	}
}

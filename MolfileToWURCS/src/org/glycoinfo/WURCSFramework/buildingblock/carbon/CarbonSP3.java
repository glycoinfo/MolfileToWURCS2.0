package org.glycoinfo.WURCSFramework.buildingblock.carbon;

import org.glycoinfo.WURCSFramework.buildingblock.Carbon;
import org.glycoinfo.WURCSFramework.buildingblock.CarbonChain;
import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * Class for SP3 carbon<br>
 * Connection around the carbon:
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
 */
public class CarbonSP3 extends Carbon {

	private Connection m_oPrevCon = null;
	private Connection m_oNextCon = null;
	private Connection m_oMod1Con = null; // 'u' first  / upside
	private Connection m_oMod2Con = null; // 'd' second / downside
	private Connection m_oMod3Con = null; // 't' third

	public CarbonSP3(Atom a_oAtom, CarbonChain a_oChain) {
		super(a_oAtom, a_oChain);
		// TODO 自動生成されたコンストラクター・スタブ
	}


}

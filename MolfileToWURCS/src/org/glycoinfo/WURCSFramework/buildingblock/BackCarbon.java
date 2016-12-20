package org.glycoinfo.WURCSFramework.buildingblock;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.util.analytical.CarbonIdentifier;
import org.glycoinfo.WURCSFramework.wurcs.graph.old.CarbonDescriptor;

public class BackCarbon extends Atom {

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_SP  = 1;
	public static final int TYPE_SP2 = 2;
	public static final int TYPE_SP3 = 3;

	private Atom m_oOriginal;
	private CarbonDescriptor m_enumDescriptor;
	private BackCarbonChain m_oParentChain;
	private BackCarbon m_oPrevCarbon = null;
	private BackCarbon m_oNextCarbon = null;
	private Connection m_oConn1 = null;
	private Connection m_oConn2 = null;
	private Connection m_oConn3 = null;

	/**
	 * Connection around the carbon<br>
	 * For SP3 carbon:
	 * <pre>
	 *   P        3        P
	 *   |        |        |
	 * 1-C-2 or 1-C-2 or 1-C-2
	 *   |        |        |
	 *   N        N        3
	 * middle    head     tail
	 *
	 * P: Previous carbon
	 * N: Next carbon
	 * 1, 2, 3: Orderd modifications
	 *
	 * </pre>
	 * For SP2 carbon:
	 * <pre>
	 *   P      1   2      P
	 *   |       \ /       |
	 *   C-1 or   C   or   C
	 *   |        |       / \
	 *   N        N      1   2
	 * middle    head     tail
	 *
	 * P: Previous carbon
	 * N: Next carbon
	 * 1, 2: Orderd modifications
	 * </pre>
	 * For SP carbon:
	 * <pre>
	 *   P        1        P
	 *   |        |        |
	 *   C   or   C   or   C
	 *   |        |        |
	 *   N        N        1
	 * middle    head     tail
	 *
	 * P: Previous carbon
	 * N: Next carbon
	 * 1: Modification
	 * </pre>
	 */

	public BackCarbon(Atom a_oAtom, BackCarbonChain a_oChain) {
		super(a_oAtom.getSymbol());
		this.m_oOriginal = a_oAtom;
		this.m_oParentChain = a_oChain;
		CarbonIdentifier t_oCI = new CarbonIdentifier();
		t_oCI.setAtom(a_oAtom);
	}

	public Atom getOriginal() {
		return this.m_oOriginal;
	}

	public BackCarbonChain getChain() {
		return this.m_oParentChain;
	}

	public void setPrev(BackCarbon a_oPrev) {
		this.m_oPrevCarbon = a_oPrev;
	}

	public BackCarbon getPrev() {
		return this.m_oPrevCarbon;
	}

	public void setNext(BackCarbon a_oNext) {
		this.m_oNextCarbon = a_oNext;
	}

	public BackCarbon getNext() {
		return this.m_oNextCarbon;
	}

}

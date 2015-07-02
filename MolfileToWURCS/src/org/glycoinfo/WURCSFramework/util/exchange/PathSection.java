package org.glycoinfo.WURCSFramework.util.exchange;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Bond;
import org.glycoinfo.WURCSFramework.chemicalgraph.Connection;

/**
 * 修飾に含まれる部分構造を探索し、修飾文字列を生成する際、探索した原子の情報を格納するクラス
 * Class for storing atomic information to generate MAP(ALIN) code
 * @author KenichiTanaka
 * @author MasaakiMatsubara
 */
public class PathSection {
	//----------------------------
	// Member variable
	//----------------------------
	private PathSection m_objLastSection;
	private PathSection m_objNextSection;
	private Atom m_objAtom;
	private Bond m_objBond;

	//----------------------------
	// Constructor
	//----------------------------
	/** For head of pathway */
	public PathSection( final Atom atom ) {
		this.m_objLastSection = null;
		this.m_objNextSection = this;
		this.m_objAtom = atom;
		this.m_objBond = null;
	}

	/** For not head of pathway*/
	public PathSection( final PathSection last, final PathSection next, final Connection con ) {
		this.m_objLastSection = last;
		this.m_objNextSection = (next==null)? this: next;
		this.m_objAtom = (next==null)? con.endAtom() : null;
		this.m_objBond = con.getBond();
	}

	//----------------------------
	// Accessor
	//----------------------------
	public PathSection getLast() {
		return this.m_objLastSection;
	}

	public PathSection getNext() {
		return this.m_objNextSection;
	}

	public Atom getAtom() {
		return this.m_objAtom;
	}

	public Bond getBond() {
		return this.m_objBond;
	}

}

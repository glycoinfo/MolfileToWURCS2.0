package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

public class Bond {
	//----------------------------
	// Member variable
	//----------------------------
	/** Connect atoms */
	private Atom m_oAtom1;
	private Atom m_oAtom2;
	/** Bond type */
	private int m_iType = 0;
	private BondType m_oBondType;
	/** Stereo information for drawing */
	private int m_iStereo = 0;
	private BondStereo m_oBondStereo;
	/** Geometrical isomer */
	private String m_strGeometric = null;

	//----------------------------
	// Constructor
	//----------------------------
	public Bond(final Atom a_oAtom1, final Atom a_oAtom2, final int a_iType, final int a_iStereo) {
		this.m_oAtom1 = a_oAtom1;
		this.m_oAtom2 = a_oAtom2;
		this.m_iType = a_iType;
		this.m_oBondType = BondType.forType(a_iType);
		this.m_iStereo = a_iStereo;
		this.m_oBondStereo = BondStereo.forStereoValue( a_iStereo, this.m_oBondType.getMultiplicity() );

		Connection t_oConnection1 = new Connection(a_oAtom2, this, a_iStereo);
		Connection t_oConnection2 = new Connection(a_oAtom1, this, (a_iStereo != 1 && a_iStereo != 6) ? a_iStereo : 0);

		a_oAtom1.addConnection(t_oConnection1);
		a_oAtom2.addConnection(t_oConnection2);
	}

	//----------------------------
	// Accessor
	//----------------------------
	public Atom getAtom1() {
		return this.m_oAtom1;
	}

	public Atom getAtom2() {
		return this.m_oAtom2;
	}

	public int getType() {
		return this.m_iType;
	}

	public int getStereo() {
		return this.m_iStereo;
	}

	public String getGeometric() {
		return this.m_strGeometric;
	}

	public void setGeometric(String geom) {
		this.m_strGeometric = geom;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return the length of this bond.
	 * @return the length of this bond.
	 */
	public double length(){
		double[] crd0 = this.m_oAtom1.getCoordinate();
		double[] crd1 = this.m_oAtom2.getCoordinate();
		double[] v = new double[3];
		v[0] = crd1[0] - crd0[0];
		v[1] = crd1[1] - crd0[1];
		v[2] = crd1[2] - crd0[2];
		return Math.sqrt( v[0]*v[0] + v[1]*v[1] + v[2]*v[2] );
	}

	public Bond copy(Atom a_oAtom1, Atom a_oAtom2) {
		return new Bond( a_oAtom1, a_oAtom2, this.m_iType, this.m_iStereo );
	}
}

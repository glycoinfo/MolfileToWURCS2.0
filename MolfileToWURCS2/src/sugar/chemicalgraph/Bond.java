package sugar.chemicalgraph;

public class Bond {
	//----------------------------
	// Member variable
	//----------------------------
	/** Connect atoms */
	private Atom[] m_aAtoms = new Atom[2];
	/** Bond type */
	private int m_iType = 0;
	/** Stereo information for drawing */
	private int m_iStereo = 0;
	/** Geometrical isomer */
	private String m_strGeometric = null;

	//----------------------------
	// Constructor
	//----------------------------
	public Bond(final Atom atom0, final Atom atom1, final int type, final int stereo) {
		this.m_aAtoms[0] = atom0;
		this.m_aAtoms[1] = atom1;
		this.m_iType = type;
		this.m_iStereo = stereo;

		Connection connection0 = new Connection(atom1, this, stereo);
		Connection connection1 = new Connection(atom0, this, (stereo != 1 && stereo != 6) ? stereo : 0);

		atom0.addConnection(connection0);
		atom1.addConnection(connection1);
	}

	//----------------------------
	// Accessor
	//----------------------------
	public Atom getAtom1() {
		return this.m_aAtoms[0];
	}

	public Atom getAtom2() {
		return this.m_aAtoms[1];
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
		double[] crd0 = this.m_aAtoms[0].getCoordinate();
		double[] crd1 = this.m_aAtoms[1].getCoordinate();
		double[] v = new double[3];
		v[0] = crd1[0] - crd0[0];
		v[1] = crd1[1] - crd0[1];
		v[2] = crd1[2] - crd0[2];
		return Math.sqrt( v[0]*v[0] + v[1]*v[1] + v[2]*v[2] );
	}

}

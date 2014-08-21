package chemicalgraph;

/**
 * Class for bond (undirected graph information).
 * @author KenichiTanaka
 */
public class Bond {
	//----------------------------
	// Member variable
	//----------------------------
	/** Connect atoms */
	public Atom[] atoms = new Atom[2];
	/** Bond type */
	public int type = 0;
	/** Stereo information for drawing. */
	public int stereo = 0;
	
	/** Stereo Chemistry for double bond */
	public String stereoTmp = null;
	/** Stereo Chemistry for double bond */
	public String stereoMolecule = null;
	/** Stereo Chemistry for double bond */
	public String stereoModification = null;
	
	//----------------------------
	// Constructor
	//----------------------------
	public Bond() {
	}

	public Bond(final Atom atom0, final Atom atom1, final int type, final int stereo) {
		this.atoms[0] = atom0;
		this.atoms[1] = atom1;
		this.type = type;
		this.stereo = stereo;
		
		Connection connection0 = new Connection(atom1, this, stereo);
		Connection connection1 = new Connection(atom0, this, (stereo != 1 && stereo != 6) ? stereo : 0);
		
		atom0.connections.addLast(connection0);
		atom1.connections.addLast(connection1);
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return the length of this bond.
	 * @return the length of this bond.
	 */
	public double length(){
		return Math.sqrt(
				Math.pow(this.atoms[1].coordinate[0] - this.atoms[0].coordinate[0], 2.0) +
				Math.pow(this.atoms[1].coordinate[1] - this.atoms[0].coordinate[1], 2.0) +
				Math.pow(this.atoms[1].coordinate[2] - this.atoms[0].coordinate[2], 2.0));
	}
}

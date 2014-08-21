package chemicalgraph;

/**
 * Class for connection (directed graph information).
 * @author KenichiTanaka
 */
public class Connection {
	//----------------------------
	// Member variable
	//----------------------------
	public Atom atom;
	public Bond bond;
	public int stereo;
	public int CIPorder;
	public boolean isUniqOrder;
	public String stereoForWURCS;
	public boolean tmpflg;
	public boolean isCompletedFullSearch;

	//----------------------------
	// Constructor
	//----------------------------
	public Connection(final Atom atom, final Bond bond, final int stereo){
		this.atom = atom;
		this.bond = bond;
		this.stereo = stereo;
		this.isUniqOrder = true;
	}
	
	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return the start atom of this connection.
	 * @return the start atom of this connection
	 */
	public Atom start(){
		return (this.bond.atoms[1]==this.atom) ? this.bond.atoms[0] : this.bond.atoms[1];
	}

	/**
	 * Return the end atom of this connection.
	 * @return the end atom of this connection
	 */
	public Atom end(){
		return this.atom;
	}

	/**
	 * Return the reverse connection.
	 * @return the reverse connection
	 */
	public Connection getReverse(){
		for(Connection connection : this.atom.connections){
			if(connection.atom == this.start()) return connection;
		}
		return null;
	}

	/**
	 * Return the length of this connection.
	 * @return the length of this connection
	 */
	public double length(){
		return this.bond.length();
	}

	/**
	 * Return the unit vector of this connect.
	 * @return unit vector
	 */
	public double[] unitVector3D(){
		Atom start = this.start();
		Atom end   = this.end();
		double[] unitVector3D = new double[3];
		unitVector3D[0] = end.coordinate[0] - start.coordinate[0];
		unitVector3D[1] = end.coordinate[1] - start.coordinate[1];
		unitVector3D[2] = end.coordinate[2] - start.coordinate[2];

		// Up down がついている場合は、紙面に対して45°傾くように、Z軸を増減する。
		if(this.stereo == 1 || this.stereo == 6){
			double bondLengthPre = Math.sqrt(Math.pow(unitVector3D[0], 2.0) + Math.pow(unitVector3D[1], 2.0) + Math.pow(unitVector3D[2], 2.0));
			unitVector3D[2] += (this.stereo == 1) ? bondLengthPre : -bondLengthPre;
		}
		
		// 単位ベクトルの生成
		double bondLengthPost = Math.sqrt(Math.pow(unitVector3D[0], 2.0) + Math.pow(unitVector3D[1], 2.0) + Math.pow(unitVector3D[2], 2.0));
		unitVector3D[0] /= bondLengthPost;
		unitVector3D[1] /= bondLengthPost;
		unitVector3D[2] /= bondLengthPost;
		
		return unitVector3D;
	}
}

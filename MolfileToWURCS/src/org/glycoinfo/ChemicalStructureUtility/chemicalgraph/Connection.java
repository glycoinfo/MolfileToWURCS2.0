package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitable;
import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitor;
import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitorException;

public class Connection implements AtomicVisitable {
	//----------------------------
	// Member variable
	//----------------------------
	private Atom m_objAtom;
	private Bond m_objBond;
	private int m_iStereo;

	//----------------------------
	// Constructor
	//----------------------------
	public Connection(final Atom atom, final Bond bond, final int stereo){
		this.m_objAtom = atom;
		this.m_objBond = bond;
		this.m_iStereo = stereo;
	}

	//----------------------------
	// Accessor
	//----------------------------
	public Bond getBond() {
		return this.m_objBond;
	}

	//----------------------------
	// Public method (non void)
	//----------------------------
	/**
	 * Return the start atom of this connection.
	 * @return the start atom of this connection
	 */
	public Atom startAtom(){
		if ( this.m_objBond == null ) return null;
		return (this.m_objBond.getAtom2()==this.m_objAtom) ? this.m_objBond.getAtom1() : this.m_objBond.getAtom2();
	}

	/**
	 * Return the end atom of this connection.
	 * @return the end atom of this connection
	 */
	public Atom endAtom(){
		return this.m_objAtom;
	}

	/**
	 * Return the reverse connection.
	 * @return the reverse connection
	 */
	public Connection getReverse(){
		for(Connection connection : this.m_objAtom.getConnections() ){
			if(connection.endAtom() == this.startAtom()) return connection;
		}
		return null;
	}

	public int getStereo() {
		return this.m_iStereo;
	}

	/**
	 * Return the unit vector of this connect.
	 * @return unit vector
	 */
	public double[] unitVector3D(){
		double[] startcrd = this.startAtom().getCoordinate();
		double[] endcrd   = this.endAtom().getCoordinate();
		double[] unitVector3D = new double[3];
		unitVector3D[0] = endcrd[0] - startcrd[0];
		unitVector3D[1] = endcrd[1] - startcrd[1];
		unitVector3D[2] = endcrd[2] - startcrd[2];

		// Add bond length to z direction if bond stereo is set to up(1) or down(6)
		if(this.m_iStereo == 1 || this.m_iStereo == 6){
			unitVector3D[2] += (this.m_iStereo == 1) ? this.m_objBond.length() : -this.m_objBond.length();
		}

		// Generate unit vector
//		double bondLengthPost = Math.sqrt(Math.pow(unitVector3D[0], 2.0) + Math.pow(unitVector3D[1], 2.0) + Math.pow(unitVector3D[2], 2.0));
		double bondLengthPost = Math.sqrt( unitVector3D[0]*unitVector3D[0] + unitVector3D[1]*unitVector3D[1] + unitVector3D[2]*unitVector3D[2] );
		unitVector3D[0] /= bondLengthPost;
		unitVector3D[1] /= bondLengthPost;
		unitVector3D[2] /= bondLengthPost;

		return unitVector3D;
	}

	@Override
	public void accept(AtomicVisitor a_objVisitor) throws AtomicVisitorException {
		a_objVisitor.visit(this);
	}

}

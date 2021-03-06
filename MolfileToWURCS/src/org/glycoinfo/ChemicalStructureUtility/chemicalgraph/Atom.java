package org.glycoinfo.ChemicalStructureUtility.chemicalgraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitable;
import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitor;
import org.glycoinfo.ChemicalStructureUtility.util.visitor.AtomicVisitorException;

/**
 * Class for atom
 * @author MasaakiMatsubara
 */
public class Atom implements AtomicVisitable {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of connections from this atom to other elements.                 */
	private LinkedList<Connection> m_aConnections  = new LinkedList<Connection>();
	/** Atom symbol                                                           */
	private String   m_strSymbol           = null;
	/** Atom coordinates                                                      */
	private double[] m_dCoordinate         = new double[3];
	/** Charge (MolfileToWURCS set 0 this variable even if this information is written in input CTFile.)      */
	private int      m_iCharge             = 0;
	/** For isotope.(MolfileToWURCS set 0 this variable even if this information is written in input CTFile.) */
	private int      m_iMass               = 0;
	/** Radical (MolfileToWURCS does not use this variable.)                  */
	private int      m_iRadical            = 0;
	/** Number of pi electron                                                 */
	private int      m_iPiElectron         = 0;
	/** Aromatic atom flag */
	private boolean  m_bIsAromatic         = false;
	/** Chirarity                                                             */
	private String   m_strChirality        = null;
	/** Alias name                                                            */
	private String   m_strAliasName        = null;
	/** ID of the atom in input molecule (incremental number for hidden hydrogen) */
	private int      m_iAtomID             = -1;

	//----------------------------
	// Constructor
	//----------------------------
	public Atom(String symbol) {
		this.m_strSymbol = symbol;
	}

	//----------------------------
	// Accessor(getter)
	//----------------------------
	public LinkedList<Connection> getConnections() {
		return this.m_aConnections;
	}

	public String getSymbol() {
		return this.m_strSymbol;
	}

	public double[] getCoordinate() {
		return this.m_dCoordinate;
	}

	public int getCharge(){
		return this.m_iCharge;
	}

	public int getMass() {
		return this.m_iMass;
	}

	public int getRadical() {
		return this.m_iRadical;
	}

	public int getNumberOfPiElectron() {
		return this.m_iPiElectron;
	}

	public boolean isAromatic() {
		return this.m_bIsAromatic;
	}

	public String getChirality() {
		return this.m_strChirality;
	}

	public String getAliasName() {
		return this.m_strAliasName;
	}

	public int getAtomID() {
		return this.m_iAtomID;
	}

	//----------------------------
	// Accessor(setter)
	//----------------------------
	public void addConnection(Connection con) {
		this.m_aConnections.addLast(con);
	}

	public void setCoordinate(double[] crd) {
		this.m_dCoordinate = crd;
	}

	public void setCharge(int charge) {
		this.m_iCharge = charge;
	}

	public void setMass(int mass) {
		this.m_iMass = mass;
	}

	public void setRadical(int radical) {
		this.m_iRadical = radical;
	}

	public void setNumberOfPiElectron(int numPi) {
		this.m_iPiElectron = numPi;
	}

	public void setAromaticity() {
		this.m_bIsAromatic = true;
	}

	public void setChirality(String chiral) {
		this.m_strChirality = chiral;
	}

	public void setAliasName(String alias) {
		this.m_strAliasName = alias;
//		if ( this.m_strSymbol.equals("*") )
//			this.m_strSymbol = this.m_strAliasName.substring(0,1);
	}

	public void setAtomID(int atomID) {
		this.m_iAtomID = atomID;
	}

	//----------------------------
	// Public method
	//----------------------------
	public void sortConnections(Comparator<Connection> comparator) {
		Collections.sort(this.m_aConnections, comparator);
	}

	public boolean removeConnection(Bond a_oBond) {
		LinkedList<Connection> t_aRemoveConnections = new LinkedList<Connection>();
		for ( Connection t_oConn : this.m_aConnections ) {
			if ( !t_oConn.getBond().equals(a_oBond) ) continue;
			t_aRemoveConnections.add(t_oConn);
		}
		if ( t_aRemoveConnections.isEmpty() ) return false;
		return this.removeConnection(t_aRemoveConnections);
	}

	public boolean removeConnection(Atom a_oAtom) {
		LinkedList<Connection> t_aRemoveConnections = new LinkedList<Connection>();
		for ( Connection t_oConn : this.m_aConnections ) {
			if ( !t_oConn.endAtom().equals(a_oAtom) ) continue;
			t_aRemoveConnections.add(t_oConn);
		}
		if ( t_aRemoveConnections.isEmpty() ) return false;
		return this.removeConnection(t_aRemoveConnections);
	}

	private boolean removeConnection(LinkedList<Connection> a_aConn) {
		boolean t_bSuccess = true;
		for ( Connection t_oConn : a_aConn ) {
			if ( !this.m_aConnections.remove(t_oConn) )
				t_bSuccess = false;
		}
		return t_bSuccess;
	}
	public void accept(AtomicVisitor a_objVisitor) throws AtomicVisitorException {
		a_objVisitor.visit(this);
	}

	/**
	 * Copy this atom
	 * @return Copied atom
	 */
	public Atom copy() {
		Atom t_oCopy = new Atom(this.m_strSymbol);
		t_oCopy.setCoordinate(this.m_dCoordinate);
		t_oCopy.setCharge(this.m_iCharge);
		t_oCopy.setMass(this.m_iMass);
		t_oCopy.setNumberOfPiElectron(this.m_iPiElectron);
		t_oCopy.setAliasName(this.m_strAliasName);
		t_oCopy.setAtomID(this.m_iAtomID);
		return t_oCopy;
	}
}

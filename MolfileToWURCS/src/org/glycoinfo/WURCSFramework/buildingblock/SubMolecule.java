package org.glycoinfo.WURCSFramework.buildingblock;

import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Bond;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Connection;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;

/**
 * Class for molecule of a Modification contains the connected Backbone carbon(s).
 * Contained atoms and bonds must have originals
 * @author MasaakiMatsubara
 *
 */
public class SubMolecule extends Molecule {

	private HashMap<Atom, Atom> m_mapCopyToOrigAtom;
	private HashMap<Bond, Bond> m_mapCopyToOrigBond;
	private LinkedList<Atom> m_aBackboneCarbons;
	private HashMap<Atom, Double> m_mapBackboneCarbonToWeight;

	public SubMolecule() {
		this.m_mapCopyToOrigAtom = new HashMap<Atom, Atom>();
		this.m_mapCopyToOrigBond = new HashMap<Bond, Bond>();
		this.m_aBackboneCarbons = new LinkedList<Atom>();
		this.m_mapBackboneCarbonToWeight = new HashMap<Atom, Double>();
	}

	/**
	 * Associate an original atom with the atom in the SubMolecule
	 * @param a_oAtom Atom in the SubMolecule
	 * @param a_oOriginal Original Atom
	 * @return {@code false} if the a_oAtom is not contained in the SubMolecule
	 */
	public boolean associateAtomWithOriginal(Atom a_oAtom, Atom a_oOriginal) {
		if ( !this.contains(a_oAtom) ) return false;
		this.m_mapCopyToOrigAtom.put(a_oAtom, a_oOriginal);
		return true;
	}

	/**
	 * Get an original atom from an atom in the SubMolecule
	 * @param a_oAtom Atom in the SubMolecule
	 * @return Original Atom of a_oAtom ({@code null} if the atom is not cotained in the SubMolecule or original is not set)
	 */
	public Atom getOriginalAtom(Atom a_oAtom) {
		if ( !this.contains(a_oAtom) ) return null;
		return this.m_mapCopyToOrigAtom.get(a_oAtom);
	}

	/**
	 * Associate an original bond with the bond in the SubMolecule
	 * @param a_oBond Bond in the SubMolecule
	 * @param a_oOriginal Original Bond
	 * @return {@code false} if the a_oBond is not contained in the SubMolecule
	 */
	public boolean associateBondWithOriginal(Bond a_oBond, Bond a_oOriginal) {
		if ( !this.contains(a_oBond) ) return false;
		this.m_mapCopyToOrigBond.put(a_oBond, a_oOriginal);
		return true;
	}

	/**
	 * Get an original atom from an atom in the SubMolecule
	 * @param a_oBond Bond in the SubMolecule
	 * @return Original Bond related to a_oBond ({@code null} if the bond is not contained in the SubMolecule or original is not set)
	 */
	public Bond getOriginalBond(Bond a_oBond) {
		if ( !this.contains(a_oBond) ) return null;
		return this.m_mapCopyToOrigBond.get(a_oBond);
	}

	/**
	 * Get an original connection from a connection in the SubMolecule
	 * @param a_oConn Connection in the SubMolecule
	 * @return Original Connection related to a_oConn ({@code null} if the connecion is not contained in the SubMolecule)
	 */
	public Connection getOriginalConnection(Connection a_oConn) {
		if ( !this.contains( a_oConn.getBond() ) ) return null;
		Atom t_oOrigStart = this.m_mapCopyToOrigAtom.get( a_oConn.startAtom() );
		for ( Connection t_oConn : t_oOrigStart.getConnections() ) {
			if ( !t_oConn.endAtom().equals( this.m_mapCopyToOrigAtom.get( a_oConn.endAtom() ) ) )
				continue;
			return t_oConn;
		}
		return null;
	}

	/**
	 * Set an atom in the SubMolecule to treat as a Backbone carbon (the initial stereo weight 0.01D is also set)
	 * @param a_oCarbon Atom treated as a Backbone carbon
	 * @return {@code false} if the a_oCarbon is not contained in the SubMolecule
	 */
	public boolean setBackboneCarbon(Atom a_oCarbon) {
		if ( !this.contains(a_oCarbon) ) return false;
		this.m_aBackboneCarbons.addLast(a_oCarbon);
		this.m_mapBackboneCarbonToWeight.put(a_oCarbon, 0.01D);
		return true;
	}

	/**
	 * Get atoms treated as Backbone carbons
	 * @return LinkedList of atom treated as Backbone carbons
	 */
	public LinkedList<Atom> getBackboneCarbons() {
		return this.m_aBackboneCarbons;
	}

	/**
	 * Get a connection from a backbone carbon to a connected modification atom
	 * @param a_oCarbon Backbone carbon
	 * @return Connection from a backbone carbon to a connected modification atom
	 */
	public Connection getConnectionFromBackbone(Atom a_oCarbon) {
		if ( !this.m_aBackboneCarbons.contains(a_oCarbon) ) return null;
		for ( Connection t_oConn : a_oCarbon.getConnections() ) {
			if ( t_oConn.endAtom().getSymbol().equals("H") ) continue;
			return t_oConn;
		}
		return null;
	}

	/**
	 * Set additional weight for a Backbone carbon in the SubMolecule
	 * @param a_oCarbon Target Backbone carbon to be ranked
	 * @param a_dWeight Double of the additional backbone carbon weight
	 * @return {@code false} if the a_oCarbon is not contained in the list of Backbone carbons
	 */
	public boolean setWeightForBackboneCarbon(Atom a_oCarbon, Double a_dWeight) {
		if ( !this.m_aBackboneCarbons.contains(a_oCarbon) ) return false;
		this.m_mapBackboneCarbonToWeight.put(a_oCarbon, a_dWeight);
		return true;
	}

	/**
	 * Get additional weight of a Backbone carbon in the SubMolecule
	 * @param a_oCarbon Weighted Backbone carbon
	 * @return Double of additional weight of a_oCarbon ({@code null} if the a_oCarbon is not contained in the list of Backbone carbons)
	 */
	public Double getWeightOfBackboneCarbon(Atom a_oCarbon) {
		if ( !this.m_aBackboneCarbons.contains(a_oCarbon) ) return null;
		return this.m_mapBackboneCarbonToWeight.get(a_oCarbon);
	}
}

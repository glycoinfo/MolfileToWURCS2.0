package util.creator;

import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.WURCSGlycanObject;
import sugar.wurcs.WURCSGlycanObjectException;
import chemicalgraph.subgraph.aglycone.Aglycone;
import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.modification.Modification;
import chemicalgraph2.Atom;
import chemicalgraph2.Connection;

public class ModificationCreator extends WURCSGlycanObjectCreator{

	/** Backbone list*/
	private LinkedList<Backbone> m_aBackbones = null;
	/** Aglycone list*/
	private LinkedList<Aglycone> m_aAglycones = null;

	/** Backbone atoms */
	private HashSet<Atom> m_aBackboneAtoms = new HashSet<Atom>();
	/** Aglycone atoms */
	private HashSet<Atom> m_aAglyconeAtoms = new HashSet<Atom>();
	/** Modification atoms */
	private HashSet<Atom> m_aModificationAtoms = new HashSet<Atom>();

	/** Modification list */
	private LinkedList<Modification> m_aModifications = new LinkedList<Modification>();

	//----------------------------
	// Accessor
	//----------------------------
	public void setBackbones(LinkedList<Backbone> backbones) {
		this.m_aBackbones = backbones;
		for ( Backbone backbone : backbones ) {
			this.m_aBackboneAtoms.addAll(backbone);
			// Set start atoms for Modification creation
			for ( Atom atom : backbone) {
				for ( Connection con : atom.connections ) {
					this.addStartAtom(con.atom);
				}
			}
		}
		this.addIgnoreAtoms(this.m_aBackboneAtoms);
	}

	public void setAglycones(LinkedList<Aglycone> aglycones) {
		this.m_aAglycones = aglycones;
		for ( Aglycone aglycone : aglycones ) {
			this.m_aAglyconeAtoms.addAll(aglycone.atoms);
		}
		this.addIgnoreAtoms(this.m_aAglyconeAtoms);
	}

	public HashSet<Atom> getModificationAtoms() {
		return this.m_aModificationAtoms;
	}

	public LinkedList<Modification> getModifications() {
		return this.m_aModifications;
	}

	public void clear() {
		super.clear();
		this.m_aBackbones = null;
		this.m_aAglycones = null;
		this.m_aModifications = null;
		this.m_aBackboneAtoms.clear();
		this.m_aAglyconeAtoms.clear();
		this.m_aModificationAtoms.clear();
	}

	//----------------------------
	// Public method
	//----------------------------
	public void create() throws WURCSGlycanObjectException {
		if ( this.m_aBackbones == null ) {
			throw new WURCSGlycanObjectException("Must set backbone list using this.setBackbones().");
		}
		if ( this.m_aAglycones == null ) {
			throw new WURCSGlycanObjectException("Must set aglycone list using this.setAglycones().");
		}

		for ( WURCSGlycanObject candidate : this.createCandidateSubgraphs() ) {
			// Convert candidate subgraph to modification
//			this.m_aAglycones.add(candidate.toAglycone());
			this.m_aModifications.add((Modification)candidate);
//			for(Atom atom : candidate.atoms){
//				atom.modification = candidate;
//			}
			this.m_aModificationAtoms.addAll(candidate.atoms);
		}
	}

	@Override
	protected WURCSGlycanObject createWURCSObject() {
		return new Modification();
	}

	@Override
	protected boolean checkCandidateSubgraph(WURCSGlycanObject candidate) {
		// Add backbone carbons
		for ( Atom atom : candidate.getConnectedAtoms() ) {
			if (!this.m_aBackboneAtoms.contains(atom) ) continue;
			candidate.add(atom);
		}
		// Always true
		return true;
	}

}

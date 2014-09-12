package util.creator;

import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.WURCSGlycanObject;
import sugar.wurcs.WURCSGlycanObjectException;
import chemicalgraph.Atom;
import chemicalgraph.Connection;
import chemicalgraph.subgraph.aglycone.Aglycone;
import chemicalgraph.subgraph.backbone.Backbone;

public class AglyconeCreator extends WURCSGlycanObjectCreator {

	/** Backbone list*/
	private LinkedList<Backbone> m_aBackbones = null;
	/** Backbone atoms */
	private HashSet<Atom> m_aBackboneAtoms = new HashSet<Atom>();
	/** Aglycone atoms */
	private HashSet<Atom> m_aAglyconeAtoms = new HashSet<Atom>();
	/** Anomeric carbons */
	private HashSet<Atom> m_aAnomericCarbons = new HashSet<Atom>();

	/** Aglycone list */
	private LinkedList<Aglycone> m_aAglycones = new LinkedList<Aglycone>();

	//----------------------------
	// Accessor
	//----------------------------
	public void setBackbones(LinkedList<Backbone> backbones) {
		this.m_aBackbones = backbones;
		for ( Backbone backbone : backbones ) {
			for ( Atom atom : backbone ) {
				this.m_aBackboneAtoms.add(atom);
				if( atom.equals(backbone.getAnomer()) ) {
					this.m_aAnomericCarbons.add(atom);
				}
			}
		}
		// Set start atoms for Aglycone creation
		for ( Atom atom : this.m_aAnomericCarbons ) {
			for ( Connection con : atom.connections ) {
				if ( this.m_aBackboneAtoms.contains(con.atom) ) continue;
				this.addStartAtom(con.atom);
			}
		}
	}

	public HashSet<Atom> getAglyconeAtoms() {
		return this.m_aAglyconeAtoms;
	}

	public LinkedList<Aglycone> getAglycones() {
		return this.m_aAglycones;
	}

	public void clear() {
		super.clear();
		this.m_aBackbones = null;
		this.m_aBackboneAtoms.clear();
		this.m_aAglyconeAtoms.clear();
		this.m_aAnomericCarbons.clear();
	}

	//----------------------------
	// Public method
	//----------------------------
	public void create() throws WURCSGlycanObjectException {
		if ( this.m_aBackbones == null ) {
			throw new WURCSGlycanObjectException("Must set backbone list using this.setBackbones().");
		}

		for ( WURCSGlycanObject candidate : this.createCandidateSubgraphs() ) {
			// Cast candidate subgraph to aglycone
			this.m_aAglycones.add(candidate.toAglycone());
//			this.m_aAglycones.add((Aglycone)candidate);
//			for(Atom atom : candidate.atoms){
//				atom.aglycone = candidate;
//			}
			this.m_aAglyconeAtoms.addAll(candidate.atoms);
		}
	}

	@Override
	protected WURCSGlycanObject createWURCSObject() {
		return new Aglycone();
	}

	@Override
	protected boolean checkCandidateSubgraph(WURCSGlycanObject candidate) {
		// Return true if the candidate subgraph is aglycone
		for ( Atom atom : candidate.getConnectedAtoms() ) {
			if(!this.m_aBackboneAtoms.contains(atom) ) continue;
			if( this.m_aAnomericCarbons.contains(atom) ) continue;
			return false;
		}
		return true;
	}
}

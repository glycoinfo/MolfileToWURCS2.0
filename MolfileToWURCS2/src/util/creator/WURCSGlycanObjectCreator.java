package util.creator;

import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.WURCSGlycanObject;
import sugar.wurcs.WURCSGlycanObjectException;
import chemicalgraph.Atom;

public abstract class WURCSGlycanObjectCreator {
	/** Start atom list to make subgraph */
	private HashSet<Atom> m_aStartAtoms = new HashSet<Atom>();
	/** Ignore atom list */
	private HashSet<Atom> m_aIgnoreAtoms = new HashSet<Atom>();

	//----------------------------
	// Accessor
	//----------------------------
	public void addStartAtom(Atom startAtom) {
		this.m_aStartAtoms.add(startAtom);
	}

	public void addStartAtoms(HashSet<Atom> startAtoms) {
		this.m_aStartAtoms.addAll(startAtoms);
	}

	public void addIgnoreAtoms(HashSet<Atom> ignoreAtoms) {
		this.m_aIgnoreAtoms.addAll(ignoreAtoms);
	}

	public void clear() {
		this.m_aIgnoreAtoms.clear();
		this.m_aStartAtoms.clear();
	}

	//----------------------------
	// Public method
	//----------------------------
	/**
	 * Create subgraph extended WURCSGlycanObject
	 * @throws WURCSGlycanObjectException
	 */
	public abstract void create() throws WURCSGlycanObjectException;

	//----------------------------
	// Protected method
	//----------------------------
	/**
	 * Create candidate subgraphs
	 * @return candidate subgraphs
	 */
	protected LinkedList<WURCSGlycanObject> createCandidateSubgraphs()  throws WURCSGlycanObjectException{
		if ( this.m_aStartAtoms.isEmpty() ) {
			throw new WURCSGlycanObjectException("Please set start atoms.");
		}

		LinkedList<WURCSGlycanObject> t_aObjects = new LinkedList<WURCSGlycanObject>();
		for(Atom startAtom : this.m_aStartAtoms) {
			if ( this.m_aIgnoreAtoms.contains(startAtom) ) continue;
			// Set start atom to candidate subgraph and expand
			WURCSGlycanObject candidate = this.createWURCSObject();
			candidate.expand(startAtom, this.m_aIgnoreAtoms);

			// Check the candidate subgraph is ok
			if ( !this.checkCandidateSubgraph(candidate) ) continue;

			// Add atoms of the candidate subgraph to ignore list
			this.m_aIgnoreAtoms.addAll(candidate.atoms);

			t_aObjects.add(candidate);
		}

		return t_aObjects;
	}

	protected abstract WURCSGlycanObject createWURCSObject();
	protected abstract boolean checkCandidateSubgraph(WURCSGlycanObject candidate);
}

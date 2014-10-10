package sugar.chemicalgraph;

import java.util.HashSet;
import java.util.LinkedList;

import sugar.wurcs.glycan.WURCSException;

public class SubGraphCreator {
	/** Start atom list to make subgraph */
	private HashSet<Atom> m_aStartAtoms = new HashSet<Atom>();
	/** Ignore atom list */
	private HashSet<Atom> m_aIgnoreAtoms = new HashSet<Atom>();

	//----------------------------
	// Accessor
	//----------------------------
	public void addStartAtom( Atom startAtom ) {
		this.m_aStartAtoms.add(startAtom);
	}

	public void addStartAtoms( HashSet<Atom> startAtoms ) {
		this.m_aStartAtoms.addAll(startAtoms);
	}

	public void addIgnoreAtoms( HashSet<Atom> ignoreAtoms ) {
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
	 * Create subgraph
	 * @throws WURCSException
	 */
	public LinkedList<SubGraph> create() {
		if ( this.m_aStartAtoms.isEmpty() ) {
			System.err.println("There is no start atom for create sub graph.");
		}

		LinkedList<SubGraph> t_hashGraphs = new LinkedList<SubGraph>();
		for(Atom startAtom : this.m_aStartAtoms) {
			if ( this.m_aIgnoreAtoms.contains(startAtom) ) continue;
			// Set start atom to candidate subgraph and expand
			SubGraph candidate = new SubGraph();
			candidate.expand(startAtom, this.m_aIgnoreAtoms);

			// Add atoms of the candidate subgraph to ignore list
			this.m_aIgnoreAtoms.addAll(candidate.getAtoms());

			t_hashGraphs.add(candidate);
		}

		return t_hashGraphs;
	}

}

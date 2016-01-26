package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Atom;
import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.SubGraph;

public class SubGraphCreatorOld {
	/** Start atom list to make subgraph */
	private HashSet<Atom> m_aStartAtoms;
	/** Ignore atom list */
	private HashSet<Atom> m_aIgnoreAtoms;

	//----------------------------
	// Constructor
	//----------------------------
	/**
	 * @param aStartAtoms Start atoms for subgraph
	 * @param aIgnoreAtoms Ignore atoms for subgraph
	 */
	public SubGraphCreatorOld( HashSet<Atom> aStartAtoms, HashSet<Atom> aIgnoreAtoms ) {
		this.m_aStartAtoms  = aStartAtoms;
		this.m_aIgnoreAtoms = aIgnoreAtoms;
	}

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
	 * Create subgraphs
	 */
	public LinkedList<SubGraph> create() {
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

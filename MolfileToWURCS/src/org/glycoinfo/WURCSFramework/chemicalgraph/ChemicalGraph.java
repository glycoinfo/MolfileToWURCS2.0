package org.glycoinfo.WURCSFramework.chemicalgraph;

import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.StereochemicalAnalyzer;

/**
 * Class for chemical graph
 * @author MasaakiMatsubara
 */
public abstract class ChemicalGraph {
	//----------------------------
	// Member variable
	//----------------------------
	/** List of atoms */
	protected LinkedList<Atom> m_aAtoms = new LinkedList<Atom>();
	/** List of bonds */
	protected LinkedList<Bond> m_aBonds = new LinkedList<Bond>();

	/** Analyzer for stereochemistry */
	protected StereochemicalAnalyzer m_objAnalyzer = new StereochemicalAnalyzer();

//	private HashMap<Atom, Integer>       m_hashAtomToTmp                = new HashMap<Atom, Integer>();
	private HashMap<Atom, Integer>       m_hashAtomToSubgraphECNumber   = new HashMap<Atom, Integer>();

	//----------------------------
	// Accessor
	//----------------------------
	public LinkedList<Atom> getAtoms() {
		return this.m_aAtoms;
	}

	public LinkedList<Bond> getBonds() {
		return this.m_aBonds;
	}

	public void clear() {
		this.m_aAtoms.clear();
		this.m_aBonds.clear();
	}

	public void add(Atom atom) {
		this.m_aAtoms.addLast(atom);
	}

	public void add(Bond bond) {
		this.m_aBonds.addLast(bond);
	}

	//----------------------------
	// Public method
	//----------------------------
	/**
	 * Whether or not this chemical graph contains the atom.
	 * @param atom
	 * @return true if this chemical graph contains the atom.
	 */
	public boolean contains(final Atom atom){
		return this.m_aAtoms.contains(atom);
	}

	/**
	 * Whether or not this chemical graph contains the bond.
	 * @param bond
	 * @return true if this chemical graph contains the bond.
	 */
	public boolean contains(final Bond bond){
		return this.m_aBonds.contains(bond);
	}

	/**
	 * Remove objects which connect with the atom, then remove the atom.
	 * @param atom Remove atom
	 * @return true if the objects are removed successfully.
	 */
	public abstract boolean remove(final Atom atom);

	/**
	 * Calculate the stereo chemistry for each atom. target is whole molecule.
	 */
	public void setStereo() {
		this.m_objAnalyzer.analyze(this);
//		this.setStereo();
		for(Atom atom : this.getAtoms()){
			atom.setChirality( this.m_objAnalyzer.getStereo(atom) );
		}
		for(Bond bond : this.getBonds()){
			bond.setGeometric( this.m_objAnalyzer.getStereo(bond) );
		}
	}

	/** Get updated EC numbers */
	public HashMap<Atom, Integer> getAtomToECNumber() {
		HashMap<Atom, Integer> t_mapAtomToECNumberCopy = new HashMap<Atom, Integer>();
		for ( Atom t_oAtom : this.m_hashAtomToSubgraphECNumber.keySet() )
			t_mapAtomToECNumberCopy.put(t_oAtom, this.m_hashAtomToSubgraphECNumber.get(t_oAtom));
		return t_mapAtomToECNumberCopy;
	}

	/**
	 * Calculate ECnumber of this chemical graph without ignoreAtoms and ignoreBonds.
	 * @param ignoreBonds
	 * @param ignoreAtoms
	 */
	public void updateECnumber(final LinkedList<Bond> ignoreBonds, final LinkedList<Atom> ignoreAtoms){
//		this.atoms.setTmp(1);
		HashMap<Atom, Integer> t_mapAtomToTempECNum = new HashMap<Atom, Integer>();
		for ( Atom atom : this.m_aAtoms ) {
			t_mapAtomToTempECNum.put(atom, 1);
		}
		int uniqUpdateECnumber = this.countUniqTmp(t_mapAtomToTempECNum);

		int uniqECnumber;
		do{
//			this.m_aAtoms.copyTmpToECNumber();
			for ( Atom atom : this.m_aAtoms ) {
				this.m_hashAtomToSubgraphECNumber.put( atom, t_mapAtomToTempECNum.get(atom) );
			}
			uniqECnumber = uniqUpdateECnumber;

//			this.atoms.setTmp(0);
			for ( Atom atom : this.m_aAtoms ) {
				t_mapAtomToTempECNum.put(atom, 0);
			}
			for ( Atom atom : this.m_aAtoms ) {
				if( ignoreAtoms != null && ignoreAtoms.contains(atom)) continue;
				int t_iTmpEC = t_mapAtomToTempECNum.get(atom);
				for ( Connection con : atom.getConnections() ) {
					if ( !this.m_aBonds.contains(con.getBond()) ) continue;
					if (  ignoreBonds != null && ignoreBonds.contains(con.getBond()) ) continue;
					if (  ignoreAtoms != null && ignoreAtoms.contains(con.endAtom()) ) continue;
//					atom.tmp += con.atom.subgraphECnumber;
					t_iTmpEC += this.m_hashAtomToSubgraphECNumber.get(con.endAtom());
				}
				t_mapAtomToTempECNum.put(atom, t_iTmpEC);
			}
			uniqUpdateECnumber = this.countUniqTmp(t_mapAtomToTempECNum);
		}while(uniqECnumber < uniqUpdateECnumber);
	}

	/**
	 * Return the number of unique tmp in list
	 * (Use when generate EC number)
	 * @return the number of unique tmp in list
	 */
	private int countUniqTmp(HashMap<Atom, Integer> a_mapAtomToTempECNum){
		int uniqNum = 0;
		int ii, jj;
		int num = this.m_aAtoms.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
//				if(atoms.get(ii).tmp == atoms.get(jj).tmp) break;

				if ( a_mapAtomToTempECNum.get(this.m_aAtoms.get(ii)) == a_mapAtomToTempECNum.get(this.m_aAtoms.get(jj)) ) break;
			}
			if(jj==num) uniqNum++;
		}
		return uniqNum;
	}

}

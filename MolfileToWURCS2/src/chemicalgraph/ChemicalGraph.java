package chemicalgraph;

import java.util.HashMap;
import java.util.LinkedList;

import chemicalgraph.util.analytical.StereochemicalAnalyzer;

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
	private StereochemicalAnalyzer m_objAnalyzer = new StereochemicalAnalyzer();

	private HashMap<Atom, Integer>       m_hashAtomToTmp                = new HashMap<Atom, Integer>();
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

	//----------------------------
	// Public method (void)
	//----------------------------
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

	/** Get updatedECNumbers */
	public HashMap<Atom, Integer> getAtomToECNumber() {
		return this.m_hashAtomToSubgraphECNumber;
	}

	/**
	 * Calculate ECnumber of this chemical graph without ignoreAtoms and ignoreBonds.
	 * @param ignoreBonds
	 * @param ignoreAtoms
	 */
	public void updateECnumber(final LinkedList<Bond> ignoreBonds, final LinkedList<Atom> ignoreAtoms){
//		this.atoms.setTmp(1);
		for ( Atom atom : this.m_aAtoms ) {
			this.m_hashAtomToTmp.put(atom, 1);
		}
		int uniqUpdateECnumber = this.countUniqTmp();

		int uniqECnumber;
		do{
//			this.m_aAtoms.copyTmpToECNumber();
			for ( Atom atom : this.m_aAtoms ) {
				this.m_hashAtomToSubgraphECNumber.put( atom, this.m_hashAtomToTmp.get(atom) );
			}
			uniqECnumber = uniqUpdateECnumber;

//			this.atoms.setTmp(0);
			for ( Atom atom : this.m_aAtoms ) {
				this.m_hashAtomToTmp.put(atom, 0);
			}
			for ( Atom atom : this.m_aAtoms ) {
				if( ignoreAtoms != null && ignoreAtoms.contains(atom)) continue;
				for ( Connection con : atom.getConnections() ) {
					if ( !this.m_aBonds.contains(con.getBond()) ) continue;
					if (  ignoreBonds != null && ignoreBonds.contains(con.getBond()) ) continue;
					if (  ignoreAtoms != null && ignoreAtoms.contains(con.endAtom()) ) continue;
//					atom.tmp += con.atom.subgraphECnumber;
					int tmp = this.m_hashAtomToTmp.get(atom);
					tmp += this.m_hashAtomToTmp.get(con.endAtom());
					this.m_hashAtomToTmp.put(atom, tmp);

				}
			}
			uniqUpdateECnumber = this.countUniqTmp();
		}while(uniqECnumber < uniqUpdateECnumber);
	}

	/**
	 * Return the number of unique tmp in list
	 * (Use when generate EC number)
	 * @return the number of unique tmp in list
	 */
	private int countUniqTmp(){
		int uniqNum = 0;
		int ii, jj;
		int num = this.m_aAtoms.size();
		for(ii=0; ii<num; ii++){
			for(jj=ii+1; jj<num; jj++){
//				if(atoms.get(ii).tmp == atoms.get(jj).tmp) break;

				if ( this.m_hashAtomToTmp.get(this.m_aAtoms.get(ii)) == this.m_hashAtomToTmp.get(this.m_aAtoms.get(jj)) ) break;
			}
			if(jj==num) uniqNum++;
		}
		return uniqNum;
	}

}

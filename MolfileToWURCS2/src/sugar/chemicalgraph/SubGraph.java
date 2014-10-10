package sugar.chemicalgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Class for sub chemical graph
 * @author MasaakiMatsubara
 *
 */
public class SubGraph extends ChemicalGraph {
	/**
	 * Remove atom and bonds which connect with input atom from this subgraph.
	 * @param atom Remove Atom
	 * @return true if this chemical graph contains input atom.
	 */
	public boolean remove(final Atom atom){
		if ( !this.contains(atom) ) return false;
		ArrayList<Bond> removeBonds = new ArrayList<Bond>();
		for(Connection connection : atom.getConnections()){
			removeBonds.add(connection.getBond());
		}
		for(Bond bond : removeBonds){
			if ( !this.contains(bond) ) continue;
			this.m_aBonds.remove(bond);
		}
		return this.m_aAtoms.remove(atom);
	}

	/**
	 * Recursively expand this chemical graph without ignoreAtoms and hydrogen.
	 * TODO: Must ignore hydrogens?
	 * @param startAtom Start atom of this chemical graph
	 * @param ignoreAtoms HashSet of ignore Atoms
	 * @author MasaakiMatsubara
	 */
	public void expand(Atom startAtom, HashSet<Atom> ignoreAtoms) {
		this.clear();
		this.add(startAtom);
//		for ( Iterator<Atom> it = this.m_aAtoms.iterator(); it.hasNext(); ) {
//			Atom atom = it.next();
		for ( int i=0; i<this.m_aAtoms.size(); i++ ) {
			Atom atom = this.m_aAtoms.get(i);
			for ( Connection con : atom.getConnections() ){
				Atom conAtom = con.endAtom();
				if ( ignoreAtoms.contains(conAtom) ) continue;
				if ( conAtom.getSymbol().equals("H") ) continue;
				if ( !this.contains(conAtom) ){
					this.add(conAtom);
				}
				if ( !this.contains(con.getBond()) ){
					this.add(con.getBond());
				}
			}
		}
	}

	/**
	 * Get connections which connect from this chemical graph to external atoms without hydrogen.
	 * TODO: Must ignore hydrogens?
	 * @return HashSet of connections from this chemical graph
	 * @author MasaakiMatsubara
	 */
	public HashSet<Connection> getExternalConnections() {
		HashSet<Connection> connections = new HashSet<Connection>();
		for ( Iterator<Atom> it = this.m_aAtoms.iterator(); it.hasNext(); ) {
			Atom atom = it.next();
			for ( Connection con : atom.getConnections() ) {
				if ( this.m_aAtoms.contains(con.endAtom()) ) continue;
				if ( atom.getSymbol().equals("H") ) continue;
				connections.add(con);
			}
		}
		return connections;
	}

}

package chemicalgraph;

import java.util.LinkedList;

/**
 * Class for bond list
 * @author KenichiTanaka
 */
public class BondList extends LinkedList<Bond>{
	//----------------------------
	// Member variable
	//----------------------------
	private static final long serialVersionUID = 1L;

	//----------------------------
	// Public method
	//----------------------------
	/**
	 * Remove the specified element from this list.
	 * @param bond
	 * @return true if this list contained the specified element (or equivalently, if this list changed as a result of the call).
	 */
	public boolean remove(final Bond bond){
		Atom atom0 = bond.atoms[0];
		Atom atom1 = bond.atoms[1];
		atom0.connections.remove(atom1);
		atom1.connections.remove(atom0);
		return super.remove(bond);
	}
}

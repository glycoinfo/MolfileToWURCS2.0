package buildingblock;

import java.util.LinkedList;

import chemicalgraph.Atom;

/**
 * Class for carbon chain
 * @author MasaakiMatsubara
 *
 */
public class CarbonChain {

	private LinkedList<Atom> m_aCarbons = new LinkedList<Atom>();

	public LinkedList<Atom> getCarbons() {
		return this.m_aCarbons;
	}

	public void add(Atom a_oCarbon) throws BuildingBlockException {
		if ( this.m_aCarbons.contains(a_oCarbon) )
			throw new BuildingBlockException("Already exist in the carbon chain.");

		this.m_aCarbons.addLast(a_oCarbon);
	}

	public void remove(Atom a_oCarbon) throws BuildingBlockException {
		this.checkCarbon(a_oCarbon);

		this.m_aCarbons.remove(a_oCarbon);
	}

	/**
	 * Get previous carbon of target carbon in the carbon chain
	 * @param a_oCarbon A target carbon
	 * @return Previous carbon atom in the carbon chain (null if target carbon is first or not exist)
	 */
	public Atom getPrevCarbonOf(Atom a_oCarbon) {
		int index = this.indexOf(a_oCarbon);
		if (index == -1)
			return null;

		if ( a_oCarbon.equals( this.m_aCarbons.getFirst() ) )
			return null;

		return this.m_aCarbons.get( index - 1 );
	}

	/**
	 * Get next carbon of target carbon in the carbon chain
	 * @param a_oCarbon A target carbon
	 * @return Next carbon atom in the carbon chain (null if target carbon is last or not exist)
	 */
	public Atom getNextCarbonOf(Atom a_oCarbon) {
		int index = this.indexOf(a_oCarbon);
		if (index == -1)
			return null;

		if ( a_oCarbon.equals( this.m_aCarbons.getLast() ) )
			return null;

		return this.m_aCarbons.get( this.indexOf(a_oCarbon) + 1 );
	}

	/**
	 * Get index of target carbon
	 * @param a_oCarbon A target carbon
	 * @return Number of index (-1 if not exist)
	 * @throws BuildingBlockException
	 */
	public int indexOf(Atom a_oCarbon) {
		int index = -1;
		for ( int i=0; i<this.m_aCarbons.size(); i++ ) {
			if ( ! this.m_aCarbons.get(i).equals(a_oCarbon) ) continue;
			index = i;
		}
		return index;
	}

	/**
	 * Check target carbon
	 * @param a_oCarbon A target carbon
	 * @throws BuildingBlockException
	 */
	private void checkCarbon(Atom a_oCarbon) throws BuildingBlockException {
		if ( a_oCarbon == null )
			throw new BuildingBlockException("Invalid object.");

		if ( !this.m_aCarbons.contains(a_oCarbon) )
			throw new BuildingBlockException("Not exist in the carbon chain.");
	}
}

package carbohydrate;

import java.util.LinkedList;

/**
 * Class for linkage between Backbone and Modification
 * @author MasaakiMatsubara
 *
 */
public class Linkage {

	private Backbone m_objBackbone;
	private Modification m_objModification;
	private LinkedList<LinkageSite> m_aLinkageSites = new LinkedList<LinkageSite>();

	public void setBackbone(Backbone backbone) {
		this.m_objBackbone = backbone;
	}

	public Backbone getBackbone() {
		return this.m_objBackbone;
	}

	public void setModification(Modification mod) {
		this.m_objModification = mod;
	}

	public Modification getModification() {
		return this.m_objModification;
	}

	public boolean addLinkageSite( LinkageSite linksite ) {
		if ( !this.m_aLinkageSites.contains( linksite ) ) return false;
		return this.m_aLinkageSites.add( linksite );
	}

	public LinkedList<LinkageSite> getLinkageSites() {
		return this.m_aLinkageSites;
	}


}

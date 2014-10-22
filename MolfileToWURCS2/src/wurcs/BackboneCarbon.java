package wurcs;

/**
 * Class for carbon indicate position of backbone
 * @author MasaakiMatsubara
 *
 */
public class BackboneCarbon {
	/** Backbone which contain this*/
	private Backbone m_objBackbone;
	/** Whether or not this is anomeric like */
	private boolean m_bIsAnomericLike = false;
	/** Descriptor for this carbon */
	private CarbonDescriptor m_objCarbonDescriptor;

	/**
	 * Constructor
	 * @param cd CarbonDescriptor
	 * @param anom Whether or not this is anomeric like
	 */
	public BackboneCarbon( Backbone backbone, CarbonDescriptor cd, boolean anom ) {
		this.m_objBackbone = backbone;
		this.m_objCarbonDescriptor = cd;
		this.m_bIsAnomericLike = anom;
	}

	public Backbone getBackbone() {
		return this.m_objBackbone;
	}

	public CarbonDescriptor getDesctriptor() {
		return this.m_objCarbonDescriptor;
	}

	public boolean isAnomeric() {
		return this.m_bIsAnomericLike;
	}

}

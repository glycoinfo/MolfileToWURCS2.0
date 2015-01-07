package org.glycoinfo.WURCSFramework.wurcsglycan;

/**
 * Class for a carbon in backbone
 * @author MasaakiMatsubara
 *
 */
public class BackboneCarbon {
	/** Backbone which contain this*/
	private Backbone m_objBackbone;
	/** Descriptor for this carbon */
	private CarbonDescriptor m_objCarbonDescriptor;
	/** Whether or not this is anomeric like */
	private boolean m_bIsAnomericLike = false;
	/** Whether or not the carbon has unkown length */
	private boolean m_bHasUnknownLength = false;

	/**
	 * Constructor
	 * @param backbone Backbone which contain this
	 * @param cd CarbonDescriptor
	 * @param chiral Whether or not this is chirality
	 * @param anom Whether or not this is anomeric like
	 */
	public BackboneCarbon( Backbone backbone, CarbonDescriptor cd, boolean anom, boolean unknown ) {
		this.m_objBackbone         = backbone;
		this.m_objCarbonDescriptor = cd;
		this.m_bIsAnomericLike     = anom;
		this.m_bHasUnknownLength   = unknown;
	}

	public BackboneCarbon( Backbone backbone, CarbonDescriptor cd, boolean anom ) {
		this(backbone, cd, anom, false);
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

	public boolean hasUnknownLength() {
		return this.m_bHasUnknownLength;
	}

	public boolean isChiral() {
		return ( this.m_objCarbonDescriptor.getHybridOrbital().equals("sp3")
				&& this.m_objCarbonDescriptor.getStereo() != null );
	}

	/**
	 * Clone
	 * @param backbone
	 * @return clone backbone carbon
	 */
	public BackboneCarbon copy(Backbone backbone) {
		return new BackboneCarbon(backbone, this.m_objCarbonDescriptor, this.m_bIsAnomericLike, this.m_bHasUnknownLength);
	}

	/**
	 * Invert
	 * @param backbone
	 * @return inverted backbone carbon
	 */
	public BackboneCarbon invert(Backbone backbone) {
		char symbolInv = this.m_objCarbonDescriptor.getChar();
		symbolInv = ( symbolInv == '1' )? '2' :
					( symbolInv == '2' )? '1' :
					( symbolInv == '3' )? '4' :
					( symbolInv == '4' )? '3' :
					( symbolInv == '5' )? '6' :
					( symbolInv == '6' )? '5' :
					( symbolInv == '7' )? '8' :
					( symbolInv == '8' )? '7' :
					( symbolInv == 's' )? 'r' :
					( symbolInv == 'r' )? 's' :
					( symbolInv == 'S' )? 'R' :
					( symbolInv == 'R' )? 'S' : symbolInv;
		CarbonDescriptor cdInv = CarbonDescriptor.forCharacter( symbolInv, this.m_objCarbonDescriptor.isTerminal() );
		return new BackboneCarbon(backbone, cdInv, this.m_bIsAnomericLike, this.m_bHasUnknownLength);
	}
}

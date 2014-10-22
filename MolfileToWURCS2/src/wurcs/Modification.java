package wurcs;



/**
 * Class for modification
 * @author MasaakiMatsubara
 *
 */
public class Modification extends WURCSComponent{

	private String m_strALINCode;
	private boolean m_bIsAglycone = true;

	public Modification( String ALINCode ) {
		this.m_strALINCode = ALINCode;
	}

	public String getALINCode() {
		return this.m_strALINCode;
	}

	public boolean isAglycone() {
		return this.m_bIsAglycone;
	}

	@Override
	protected void checkAnomeric(BackboneCarbon bc) {
		if ( !bc.isAnomeric() ) this.m_bIsAglycone = false;
	}

}

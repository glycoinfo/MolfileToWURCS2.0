package wurcsglycan;



/**
 * Class for modification
 * @author MasaakiMatsubara
 *
 */
public class Modification extends WURCSComponent{

	private String m_strMAPCode;
	private boolean m_bIsAglycone = true;

	public Modification( String MAPCode ) {
		this.m_strMAPCode = MAPCode;
	}

	public String getMAPCode() {
		return this.m_strMAPCode;
	}

	public boolean isAglycone() {
		return this.m_bIsAglycone;
	}

	@Override
	protected void checkAnomeric(BackboneCarbon bc) {
		if ( !bc.isAnomeric() ) this.m_bIsAglycone = false;
	}

}

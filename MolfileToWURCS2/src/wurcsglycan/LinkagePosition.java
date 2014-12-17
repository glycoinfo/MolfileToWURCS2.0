package wurcsglycan;


/**
 * Class for information of linkage position on the Backbone carbon
 * @author MasaakiMatsubara
 *
 */
public class LinkagePosition {

	/** Carbon number in linked Backbone (calling "PCB" in WURCS) */
	private int m_iBackbonePosition = 0;
	/** Direction type of Modification on the Backbone carbon (calling "DMB" in WURCS) */
	private String m_strDirection;
	/** Carbon number in linked Modification (calling "PCM" in WURCS) */
	private int m_iModificationPosition;
	private boolean m_bCompressDMB;
	private boolean m_bCompressPCM;

	public LinkagePosition(int iPCB, String strDMB, boolean compressDMB, int iPCM, boolean compressPCM) {
		this.m_iBackbonePosition = iPCB;
		this.m_strDirection = strDMB;
		this.m_bCompressDMB = compressDMB;
		this.m_iModificationPosition = iPCM;
		this.m_bCompressPCM = compressPCM;
	}

	public String getDirection() {
		return this.m_strDirection;
	}

	public int getBackbonePosition() {
		return this.m_iBackbonePosition;
	}

	public int getModificationPosition() {
		return this.m_iModificationPosition;
	}

	public String getCOLINCode(boolean compress) {
		String COLINCode = "";

		COLINCode += this.m_iBackbonePosition;
		if ( !(compress && this.m_bCompressDMB) )
			COLINCode +=  ":" + this.m_strDirection;
		if ( !(compress && this.m_bCompressPCM) )
			COLINCode +=  "-" + this.m_iModificationPosition;

		return COLINCode;
	}
}

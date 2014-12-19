package org.glycoinfo.WURCSFramework.wurcsglycan;

import java.text.NumberFormat;


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
	private boolean m_bCanCompressDMB;
	private boolean m_bCanCompressPCM;

	private double m_dProbabilityUpper = 1.0;
	private double m_dProbabilityLower = 1.0;

	public static final int BACKBONESIDE     = 1;
	public static final int MODIFICATIONSIDE = 2;
	private int m_iProbabilityPosition = LinkagePosition.BACKBONESIDE;

	public LinkagePosition(int iPCB, String strDMB, boolean compressDMB, int iPCM, boolean compressPCM) {
		this.m_iBackbonePosition = iPCB;
		this.m_strDirection = strDMB;
		this.m_bCanCompressDMB = compressDMB;
		this.m_iModificationPosition = iPCM;
		this.m_bCanCompressPCM = compressPCM;
	}

	public LinkagePosition(int iPCB, String strDMB, int iPCM) {
		this(iPCB, strDMB, true, iPCM, true);
	}

	public void setProbabilityUpper(double prob) throws WURCSException {
		if ( prob > 1 )
			throw new WURCSException("Probability must be lower than 1.0.");
		this.m_dProbabilityUpper = prob;
	}

	public void setProbabilityLower(double prob) throws WURCSException {
		if ( prob > 1 )
			throw new WURCSException("Probability must be lower than 1.0.");
		this.m_dProbabilityLower = prob;
	}

	public void setProbabilityPosition(int pos) throws WURCSException {
		if ( pos != BACKBONESIDE || pos != MODIFICATIONSIDE )
			throw new WURCSException("Probability position must be 1 (backbone side) or 2 (modification side).");
		this.m_iProbabilityPosition = pos;
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

	public double getProbabilityUpper() {
		return this.m_dProbabilityUpper;
	}

	public double getProbabilityLower() {
		return this.m_dProbabilityLower;
	}

	public int getProbabilityPosition() {
		return this.m_iProbabilityPosition;
	}

	public String getCOLINCode(int a_iNodeID, boolean a_bCompress) {
		String COLINCode = "";

		if ( a_iNodeID > 0 )
			COLINCode += a_iNodeID + "+";
		COLINCode += this.m_iBackbonePosition;
		if ( !a_bCompress || !this.m_bCanCompressDMB )
			COLINCode +=  ":" + this.m_strDirection;
		if ( !a_bCompress || !this.m_bCanCompressPCM )
			COLINCode +=  "-" + this.m_iModificationPosition;

		if ( this.m_dProbabilityLower == 1.0 ) return COLINCode;

		// For probability
		String strProb = "";
		if (this.m_dProbabilityUpper < 0.0 && this.m_dProbabilityLower < 0.0) { // probability is unknown
			strProb = "?";
		} else {
			strProb = NumberFormat.getNumberInstance().format(this.m_dProbabilityLower).substring(1);
		}
		if ( this.m_dProbabilityLower != this.m_dProbabilityUpper ) {
			strProb += "-"+ NumberFormat.getNumberInstance().format(this.m_dProbabilityUpper).substring(1);
		}
		strProb = "%"+strProb+"%";
		if ( this.m_iProbabilityPosition == BACKBONESIDE ) {
			COLINCode = strProb+COLINCode;
		} else {
			COLINCode += strProb;
		}
		return COLINCode;
	}
}

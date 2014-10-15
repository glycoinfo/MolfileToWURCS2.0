package carbohydrate;

import java.util.LinkedList;


public class Modification extends CarbohydrateComponent{
	private String m_strALINCode;

	public Modification(LinkedList<BackboneCarbon> a_aCarbons) {
		super(a_aCarbons);
	}

	public void setALINCode(String ALIN) {
		this.m_strALINCode = ALIN;
	}

	public String getALINCode() {
		return this.m_strALINCode;
	}
}

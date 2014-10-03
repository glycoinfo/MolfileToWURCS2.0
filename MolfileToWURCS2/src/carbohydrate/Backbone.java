package carbohydrate;

import java.util.LinkedList;

public class Backbone {
	private LinkedList<BackboneCarbonType> m_aCarbonTypes = new LinkedList<BackboneCarbonType>();

	public void addCarbonTypes( BackboneCarbonType ctype ) {
		this.m_aCarbonTypes.addLast(ctype);
	}

}

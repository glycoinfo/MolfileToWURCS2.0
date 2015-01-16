package org.glycoinfo.WURCSFramework.wurcsglycan;

public enum DirectionDescriptor {

	N('n', 0), // No chiral
	U('u', 1), // First (uno)
	D('d', 2), // Second (dos)
	T('t', 3), // Third (tres)
	E('e', 4), // Trans (entgegen)
	Z('z', 5), // Cis (zusammen)
	X('x', 6); // Unknown

	private char m_cDirection;
	private int  m_iScore;

	private DirectionDescriptor(char a_cDirection, int a_iScore) {
		this.m_cDirection = a_cDirection;
		this.m_iScore = a_iScore;
	}

	public char getDirection() {
		return this.m_cDirection;
	}

	public int getScore() {
		return this.m_iScore;
	}

	public static DirectionDescriptor forChar(char a_cDirection) {
		for ( DirectionDescriptor DD : DirectionDescriptor.values() ) {
			if ( DD.m_cDirection == a_cDirection ) return DD;
		}
		return null;
	}
}

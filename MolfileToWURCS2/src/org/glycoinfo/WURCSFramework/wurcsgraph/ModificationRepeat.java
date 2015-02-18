package org.glycoinfo.WURCSFramework.wurcsgraph;

public class ModificationRepeat extends Modification {

	private int m_iRepeatCountMin = 0;
	private int m_iRepeatCountMax = 0;

	public ModificationRepeat(String MAPCode, int nRepMin, int nRepMax) {
		super(MAPCode);
		this.m_iRepeatCountMin = nRepMin;
		this.m_iRepeatCountMax = nRepMax;
	}

	public int getRepeatCountMin() {
		return this.m_iRepeatCountMin;
	}

	public int getRepeatCountMax() {
		return this.m_iRepeatCountMax;
	}
}

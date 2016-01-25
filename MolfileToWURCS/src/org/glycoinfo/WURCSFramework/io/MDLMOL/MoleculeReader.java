package org.glycoinfo.WURCSFramework.io.MDLMOL;

import java.util.Iterator;

import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;

public class MoleculeReader {

	private ParameterReader m_oParam;
	private Iterator<String> m_itFilePath;
	private CTFileReader m_oCTReader;

	public MoleculeReader(ParameterReader a_oParam) {
		// Read argument and files using SelectFileDialog
		this.m_oParam = a_oParam;
		this.m_itFilePath = a_oParam.m_aCTFilePaths.iterator();
	}

	public boolean readNext() {
		if ( this.m_oCTReader != null && this.m_oCTReader.readNext() ) return true;
		if ( !this.m_itFilePath.hasNext() ) return false;
		this.m_oCTReader = new CTFileReader(this.m_itFilePath.next(), this.m_oParam.m_bOutputSDFile);
		return this.m_oCTReader.readNext();
	}

	public Molecule getMolecule() {
		return this.m_oCTReader.getMolecule();
	}

	public String getID() {
		return this.m_oCTReader.getFieldData( this.m_oParam.m_strID );
	}
}

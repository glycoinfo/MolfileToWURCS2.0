package org.glycoinfo.ChemicalStructureUtility.io.MDLMOL;

import java.util.ArrayList;
import java.util.Iterator;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.exec.ParameterReader;

public class MoleculeReader {

	private ParameterReader m_oParam;
	private Iterator<String> m_itFilePath;
	private CTFileReader m_oCTReader;

	public MoleculeReader(ArrayList<String> a_aFilePaths) {
		// Read argument and files using SelectFileDialog
		this.m_itFilePath = a_aFilePaths.iterator();
	}

	public boolean readNext() {
		if ( this.m_oCTReader != null && this.m_oCTReader.readNext() ) return true;
		if ( !this.m_itFilePath.hasNext() ) return false;
//		this.m_oCTReader = new CTFileReader(this.m_itFilePath.next(), this.m_oParam.m_bOutputSDFile);
		this.m_oCTReader = new CTFileReader(this.m_itFilePath.next());
		return this.m_oCTReader.readNext();
	}

	public Molecule getMolecule() {
		return this.m_oCTReader.getMolecule();
	}

	public String getID() {
		return this.m_oCTReader.getFieldData( this.m_oParam.m_strID );
	}
}

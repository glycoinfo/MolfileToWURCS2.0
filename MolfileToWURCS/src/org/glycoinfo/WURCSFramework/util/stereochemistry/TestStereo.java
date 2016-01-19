package org.glycoinfo.WURCSFramework.util.stereochemistry;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.io.MDLMOL.CTFileReader;
import org.glycoinfo.WURCSFramework.io.MDLMOL.ParameterReader;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.MoleculeNormalizer;

public class TestStereo {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			readCTFile(t_strFilepath, t_objParam.m_ID, t_objParam.m_sdfileOutput);
		}

	}

	public static void readCTFile(String a_strFilePath, String a_strFieldID, boolean a_bOutput) {

		// read CTFiles
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, a_bOutput);

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");

		while(true){
			// read a record from CTFile
			Molecule mol = t_objCTReader.getMolecule();
			if(mol==null) break;
			String ID = t_objCTReader.getFieldData(a_strFieldID);
			try {
				ID = String.format("%1$05d", Integer.parseInt(ID) );
			} catch (NumberFormatException e) {
			}
			if ( t_aSkipIDs.contains(ID) ) {
				System.err.println(ID + " is skipped.");
				continue;
			}
			MoleculeNormalizer t_oNormalize = new MoleculeNormalizer();
			t_oNormalize.normalize(mol);
			StereochemistryAnalysis t_oStereo = new StereochemistryAnalysis();
			t_oStereo.start(mol);
		}
	}

}

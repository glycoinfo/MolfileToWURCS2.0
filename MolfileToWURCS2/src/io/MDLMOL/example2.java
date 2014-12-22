package io.MDLMOL;

import java.io.IOException;
import java.util.ArrayList;

import org.glycoinfo.WURCSFramework.wurcsglycan.WURCSException;

import chemicalgraph.Molecule;

public class example2 extends example {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		WURCSGlycanImporterMolecule t_objImporterMol = new WURCSGlycanImporterMolecule();
		// Set parameters for backbone creation
		t_objImporterMol.getCarbonChainCreator().setParameters(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);

		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			// read CTFiles
			CTFileReader t_objCTReader = new CTFileReader(t_strFilepath, t_objParam.m_sdfileOutput);
			while(true){
				// read a record from CTFile
				Molecule mol = t_objCTReader.getMolecule();
				if(mol==null) break;
				if(!t_objParam.m_sdfileOutput){
					System.err.print( t_objCTReader.getFieldData(t_objParam.m_ID) );
				}
				System.err.println( t_objCTReader.getFieldData("ID") );

				try {
					t_objImporterMol.start(mol);
				} catch (WURCSException e) {
					// TODO 自動生成された catch ブロック
					System.err.println(e.getErrorMessage());
					e.printStackTrace();
				}

//				mol.generateWURCS(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);  //Issaku YAMADA
				// generate WURCS
/*				try {
					t_objGlycanImporter.generateWURCS(mol);
				} catch (WURCSGlycanObjectException e) {
					e.printStackTrace();
				}
*/
				if(mols!=null) mols.add(mol);
			}

			// close CTfile
			try {
				t_objCTReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

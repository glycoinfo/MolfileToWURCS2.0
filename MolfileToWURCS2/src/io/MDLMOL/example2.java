package io.MDLMOL;

import java.io.IOException;
import java.util.ArrayList;

import org.glycoinfo.WURCSFramework.util.exchange.WURCSGraphToArray;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcsgraph.WURCSGraphNormalizer;

import chemicalgraph.Molecule;

public class example2 {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		WURCSGraphImporterMolecule t_objImporterMol = new WURCSGraphImporterMolecule();
		// Set parameters for backbone creation
		t_objImporterMol.getCarbonChainCreator().setParameters(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);

		WURCSGraphNormalizer t_objGraphNormalizer = new WURCSGraphNormalizer();
		WURCSGraphToArray    t_objGraphToArray    = new WURCSGraphToArray();

		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			// read CTFiles
			CTFileReader t_objCTReader = new CTFileReader(t_strFilepath, t_objParam.m_sdfileOutput);
			while(true){
				// read a record from CTFile
				Molecule mol = t_objCTReader.getMolecule();
				if(mol==null) break;
				String ID = t_objCTReader.getFieldData(t_objParam.m_ID);
//				if ( !ID.equals("23373") ) continue;
//				if(!t_objParam.m_sdfileOutput){
//					System.err.print( ID+":" );
//				}
//				System.err.println(t_objCTReader.getFieldData("ID") );

				try {
					WURCSGraph t_objGlycan = t_objImporterMol.start(mol);
					t_objGraphNormalizer.start(t_objGlycan);
					t_objGraphToArray.start(t_objGlycan);
					System.out.print(t_objCTReader.getFieldData(t_objParam.m_ID)+"\t");
					t_objGraphToArray.getWURCSArray();
//					System.exit(0);
				} catch (WURCSException e) {
					// TODO 自動生成された catch ブロック
					System.err.println(e.getErrorMessage());
					e.printStackTrace();
				}

//				if(mols!=null) mols.add(mol);
//				break;
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

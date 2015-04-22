package io.MDLMOL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import org.glycoinfo.WURCSFramework.exec.WURCSFileWriter;
import org.glycoinfo.WURCSFramework.util.WURCSExporter;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSGraphToArray;
import org.glycoinfo.WURCSFramework.wurcs.WURCSArray;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSException;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraphNormalizer;

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
		TreeMap<String, String> t_mapIDtoWURCS = new TreeMap<String, String>();
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			// read CTFiles
			CTFileReader t_objCTReader = new CTFileReader(t_strFilepath, t_objParam.m_sdfileOutput);
			while(true){
				// read a record from CTFile
				Molecule mol = t_objCTReader.getMolecule();
				if(mol==null) break;
				String ID = t_objCTReader.getFieldData(t_objParam.m_ID);

				ID = String.format("%1$05d", Integer.parseInt(ID) );
//				if ( !ID.equals("23373") ) continue;
//				if(!t_objParam.m_sdfileOutput){
//					System.err.print( ID+":" );
//				}
//				System.err.println(t_objCTReader.getFieldData("ID") );

				try {
					WURCSGraph t_objGlycan = t_objImporterMol.start(mol);
//					t_objGraphNormalizer.start(t_objGlycan);
					t_objGraphToArray.start(t_objGlycan);
					System.err.println(t_objCTReader.getFieldData(t_objParam.m_ID));
					WURCSArray t_oArray = t_objGraphToArray.getWURCSArray();
					String t_strWURCS = (new WURCSExporter()).getWURCSString(t_oArray);
					t_mapIDtoWURCS.put(ID, t_strWURCS);
//					System.exit(0);
				} catch (WURCSException e) {
					// TODO 自動生成された catch ブロック
					System.err.println(e.getErrorMessage());
					e.printStackTrace();
				}

//				if(mols!=null) mols.add(mol);
//				break;
			}
			PrintWriter pw = WURCSFileWriter.printWURCS(t_mapIDtoWURCS, "C:\\GlycoCTToMOL\\", "result.txt");
			pw.println("Total: "+t_mapIDtoWURCS.size());
			pw.close();

			// close CTfile
			try {
				t_objCTReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

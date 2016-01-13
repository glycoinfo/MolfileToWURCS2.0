package org.glycoinfo.WURCSFramework.io.MDLMOL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.util.WURCSConversionLogger;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.WURCSFileWriter;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSGraphImporterMolecule;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorSeparateWURCSGraphByAglycone;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class MOLToWURCS {

	private static int minNOS = 0;
	private static int minO = 0;
	private static int minBackboneLength = 0;
	private static int maxBackboneLength = 0;
	private static float ratioBackboneNOS = 0;

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);
		minNOS = t_objParam.m_minNOS;
		minO   = t_objParam.m_minO;
		minBackboneLength = t_objParam.m_minBackboneLength;
		maxBackboneLength = t_objParam.m_maxBackboneLength;
		ratioBackboneNOS = t_objParam.m_ratioBackboneNOS;

		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			readCTFile(t_strFilepath, t_objParam.m_ID, t_objParam.m_sdfileOutput);
		}

	}

	public static void readCTFile(String a_strFilePath, String a_strFieldID, boolean a_bOutput) {
		TreeMap<String, String> t_mapIDtoWURCS = new TreeMap<String, String>();

		// read CTFiles
		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, a_bOutput);

		WURCSConversionLogger t_oLogger = new WURCSConversionLogger();

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");
//		t_aSkipIDs.add("CHEBI:51385");
//		t_aSkipIDs.add("CHEBI:51386");
//		t_aSkipIDs.add("CHEBI:51399");

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
//			if ( !ID.equals("3u2w_G_5") ) continue;
//			if ( !ID.equals("G92964ZO") ) continue;
//			if ( !ID.equals("23373") ) continue;
//			if ( !ID.equals("CHEBI:15692") ) continue;
//			if(!t_objParam.m_sdfileOutput){
//				System.err.print( ID+":" );
//			}
			System.err.println(ID);

			try {
				WURCSGraphImporterMolecule t_objImporterMol = new WURCSGraphImporterMolecule();
				// Set parameters for finding backbone
				t_objImporterMol.getCarbonChainFinder().setParameters(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);

				WURCSGraph t_objGlycan = t_objImporterMol.start(mol);
				WURCSGraphNormalizer t_objGraphNormalizer = new WURCSGraphNormalizer();
				t_objGraphNormalizer.start(t_objGlycan);

				WURCSFactory t_oFactory = new WURCSFactory(t_objGlycan);
				String t_strWURCS = t_oFactory.getWURCS();
//				t_objGraphToArray.start(t_objGlycan);
				System.err.println(t_objCTReader.getFieldData(a_strFieldID));
				t_mapIDtoWURCS.put(ID, t_strWURCS);

				t_oLogger.addWURCS(ID, t_strWURCS);
				System.err.println(t_strWURCS);
//				System.exit(0);

				// For separated graph
				WURCSVisitorSeparateWURCSGraphByAglycone t_oSeparateGraph = new WURCSVisitorSeparateWURCSGraphByAglycone();
				t_oSeparateGraph.start(t_objGlycan);
				if ( t_oSeparateGraph.getAglycones().isEmpty() ) continue;

				int i=0;
				for ( WURCSGraph t_oSepGraph : t_oSeparateGraph.getSeparatedGraphs() ) {
					i++;
					t_objGraphNormalizer.start(t_oSepGraph);
					WURCSFactory t_oSepFactory = new WURCSFactory(t_oSepGraph);
					String t_strSepWURCS = t_oSepFactory.getWURCS();

					// For aglycone
					LinkedList<String> t_aUniqueAbbrs = new LinkedList<String>();
					String t_strAglycone = "";
					for ( Modification t_oAglycone : t_oSeparateGraph.getMapSeparatedGraphToAglycones().get(t_oSepGraph) ) {
						String t_strAbbr = t_oSeparateGraph.getMapAglyconeToAbbr().get(t_oAglycone);
						if ( t_aUniqueAbbrs.contains(t_strAbbr) ) continue;
						t_aUniqueAbbrs.add(t_strAbbr);
						t_strAglycone += "\t"+t_strAbbr+": "+t_oAglycone.getMAPCode();
					}
					System.err.println("Sep"+i+": "+t_strSepWURCS+t_strAglycone);
					t_mapIDtoWURCS.put(ID+"("+i+")", t_strSepWURCS+t_strAglycone);
				}

			} catch (WURCSException e) {
//				t_mapIDtoWURCS.put(ID, e.getErrorMessage());
				System.err.println(ID+"\t"+e.getErrorMessage());
				t_oLogger.addMessage(ID, e.getErrorMessage(), "");
				e.printStackTrace();
			}

//			if(mols!=null) mols.add(mol);
//			break;
		}

		// Output results
		String t_strFileName = t_objCTReader.getFileName();
		String t_strDirName = t_objCTReader.getDirectoryName() + File.separator;
		System.err.println("Output result to "+t_strDirName);
		WURCSFileWriter.printWURCSList(t_mapIDtoWURCS, t_strDirName, t_strFileName+"_result.txt").close();
		try {
			t_oLogger.printLog(  WURCSFileWriter.getResultFilePath(t_strDirName, t_strFileName+"_result.log") );
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// close CTfile
		try {
			t_objCTReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

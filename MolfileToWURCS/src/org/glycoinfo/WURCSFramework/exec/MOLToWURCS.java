package org.glycoinfo.WURCSFramework.exec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import org.glycoinfo.ChemicalStructureUtility.chemicalgraph.Molecule;
import org.glycoinfo.ChemicalStructureUtility.io.MDLMOL.CTFileReader;
import org.glycoinfo.WURCSFramework.util.WURCSConversionLogger;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.WURCSFactoryForAglycone;
import org.glycoinfo.WURCSFramework.util.WURCSFileWriter;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSGraphImporterMolecule;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class MOLToWURCS {

	// Version
	private static final String VERSION = "2.0.160624";

	private static int minNOS = 0;
	private static int minO = 0;
	private static int minBackboneLength = 0;
	private static int maxBackboneLength = 0;
	private static float ratioBackboneNOS = 0;

	public static void main(String[] args) {
		// usage
		for ( String arg : args ) {
			if( !arg.equals("-help") ) continue;
			usage();
			System.exit(0);
		}

		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);
		minNOS = t_objParam.m_nMinNOS;
		minO   = t_objParam.m_nMinO;
		minBackboneLength = t_objParam.m_iMinBackboneLength;
		maxBackboneLength = t_objParam.m_iMaxBackboneLength;
		ratioBackboneNOS = t_objParam.m_fRatioBackboneNOS;

		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			readCTFile(t_strFilepath, t_objParam.m_strID, t_objParam.m_bOutputSDFile);
		}

	}

	public static void usage() {
		System.err.println("WURCS2.0 Conversion System from CTFile (Molfile or SDfile)");
		System.err.println("\tCurrent version: "+VERSION);
		System.err.println();
		System.err.println("Usage: java (this program).jar [OPTION]... [FILE]... ");
		System.err.println();
		System.err.println("where OPTION include:");
		System.err.println("\t-minNOS <number of NOS>");
		System.err.println("\t\t\tto set minimum number of NOS on a monosaccharide");
		System.err.println("\t-minO <number of O>");
		System.err.println("\t\t\tto set minimum number of O with single bond on a monosaccharide");
		System.err.println("\t-minBackboneLength <length of backbone>");
		System.err.println("\t\t\tto set minimum backbone length of a monosaccharide");
		System.err.println("\t-maxBackboneLength <length of backbone>");
		System.err.println("\t\t\tto set maximum backbone length of a monosaccharide");
		System.err.println("\t-ID <tag ID in sd file>\tto select the tag ID in sd file");
		System.err.println("\t-dir <directory path>\tto read files in the directory");
		System.err.println("\t-sdf\t\toutput sd file with WURCS information to stdout");
		System.err.println("\t-end\t\tto ignore arguments after this option");
		System.err.println("\t-help\t\tto print this help message");
		System.err.println();
		System.err.println("FILE is mol or sd file and must be include filename extension \".mol\" or \".sdf\".");
	}


	public static void readCTFile(String a_strFilePath, String a_strFieldID, boolean a_bOutput) {
		TreeMap<String, String> t_mapIDtoWURCS = new TreeMap<String, String>();

		// read CTFiles
		ArrayList<Molecule> mols = new ArrayList<Molecule>();
//		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, a_bOutput);
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath);

		WURCSConversionLogger t_oLogger = new WURCSConversionLogger();

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");
//		t_aSkipIDs.add("CHEBI:51385");
//		t_aSkipIDs.add("CHEBI:51386");
//		t_aSkipIDs.add("CHEBI:51399");

		while ( t_objCTReader.readNext() ) {
			// read a record from CTFile
			Molecule mol = t_objCTReader.getMolecule();
			String ID = t_objCTReader.getFieldData(a_strFieldID);

			try {
				if ( a_strFieldID != null && !a_strFieldID.equals("PDB_Chemical_Component_ID") )
					ID = String.format("%1$05d", Integer.parseInt(ID) );
			} catch (NumberFormatException e) {
			}
			if ( t_aSkipIDs.contains(ID) ) {
				System.err.println(ID + " is skipped.");
				continue;
			}
//			if ( !ID.equals("MR8") ) continue;
//			if ( !ID.equals("N-0000-001846") ) continue;
//			if ( !ID.equals("3u2w_G_5") ) continue;
//			if ( !ID.equals("G00513YN") ) continue;
//			if ( !ID.equals("23373") ) continue;
//			if ( !ID.equals("CHEBI:50071") ) continue;
//			if ( !ID.equals("CHEBI:67762") ) continue;
//			if(!t_objParam.m_sdfileOutput){
//				System.err.print( ID+":" );
//			}
			System.err.println(ID);

			// Output MOL string
			if ( a_bOutput )
				System.out.print( t_objCTReader.getMOLString() );

			try {
				WURCSGraphImporterMolecule t_objImporterMol = new WURCSGraphImporterMolecule();
				// Set parameters for finding backbone
				t_objImporterMol.getCarbonChainFinder().setParameters(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);

				WURCSGraph t_objGlycan = t_objImporterMol.start(mol);
//				WURCSGraphNormalizer t_objGraphNormalizer = new WURCSGraphNormalizer();
//				t_objGraphNormalizer.start(t_objGlycan);

				WURCSFactory t_oFactory = new WURCSFactory(t_objGlycan);
				String t_strWURCS = t_oFactory.getWURCS();
				System.err.println(t_strWURCS);
//				t_objGraphToArray.start(t_objGlycan);

				// TODO: Temporary repairs for MAP
				if ( t_strWURCS.contains("*OCO*/3CO/6=O/3C") ) {
					t_strWURCS = t_strWURCS.replaceAll("\\*OCO\\*/3CO/6=O/3C", "*OC^XO*/3CO/6=O/3C");
				}
				if ( t_strWURCS.contains("*OP^XO*/3O/3=O") ) {
					t_strWURCS = t_strWURCS.replaceAll("\\*OP\\^XO\\*/3O/3=O", "*OPO*/3O/3=O");
				}

				System.err.println(t_objCTReader.getFieldData(a_strFieldID));

				t_oLogger.addWURCS(ID, t_strWURCS);
//				System.exit(0);


				// Treatment aglycone
				WURCSFactoryForAglycone t_oFactoryA = new WURCSFactoryForAglycone(t_strWURCS);

				// For no aglycone
				if ( !t_oFactoryA.hasAglycone() ) {
					// For no aglycone
					t_mapIDtoWURCS.put(ID+"\t1\tSTANDARD", t_strWURCS);

					// Output WURCS tags
					if ( a_bOutput ) {
						System.out.print("> <WURCS2.0>\n"+t_strWURCS+"\n\n");
						System.out.print("$$$$\n");
					}
					continue;
				}

				// For aglycone
				t_mapIDtoWURCS.put(ID+"\t1\tWITH_AGLYCONE", t_strWURCS);
				System.err.println("WURCS2.0 WITH AGLYCONE:\t"+t_strWURCS+"\n");

				// For separated WURCS by each aglycone
				String t_strSepWURCSs = "";
				for ( String t_strSepWURCS : t_oFactoryA.getSeparatedWURCSs() )
					t_strSepWURCSs += t_strSepWURCS+"\n";
				// For separated aglycones
				String t_strAglycones = "";
				for ( String t_strAglyconeAbbr : t_oFactoryA.getSeparatedAglycones() )
					t_strAglycones += t_strAglyconeAbbr+"\n";
				String t_strStdWURCSs = "";

				// For separated WURCSs with aglycone
				int i=0;
				for ( String t_strSepWURCSA : t_oFactoryA.getSeparatedWURCSsWithAglycone() ) {
					i++;
					t_mapIDtoWURCS.put(ID+"\t"+i+"\tSEPARATED", t_strSepWURCSA);
				}

				// For standerd WURCSs (remain one atom aglycone)
				i=0;
				for ( String t_strStdWURCS : t_oFactoryA.getStandardWURCSs() ) {
					i++;
					t_strStdWURCSs += t_strStdWURCS+"\n";
					t_mapIDtoWURCS.put(ID+"\t"+i+"\tSTANDARD", t_strStdWURCS);
				}
				System.err.println("WURCS2.0_STANDARD:\n"+t_strStdWURCSs);

				// Output WURCS tags with aglycones
				if ( a_bOutput ) {
					System.out.print("> <WURCS2.0_WITH_AGLYCONE>\n"+t_strWURCS+"\n\n");
					System.out.print("> <WURCS2.0_SEPARATED>\n"+t_strSepWURCSs+"\n");
					System.out.print("> <WURCS2.0_AGLYCONES>\n"+t_strAglycones+"\n");
					System.out.print("> <WURCS2.0>\n"+t_strStdWURCSs+"\n");
					System.out.print("$$$$\n");
				}

			} catch (WURCSException e) {
//				t_mapIDtoWURCS.put(ID, e.getErrorMessage());
				System.err.println(ID+"\t"+e.getErrorMessage());
				t_oLogger.addMessage(ID, e.getErrorMessage(), "");
//				e.printStackTrace();
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

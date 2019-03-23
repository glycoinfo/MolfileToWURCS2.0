package org.glycoinfo.WURCSFramework.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.glycoinfo.WURCSFramework.util.WURCSValidator;
import org.glycoinfo.WURCSFramework.util.exchange.MoleculeToWURCSGraph;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;
//import java.util.Scanner;

public class MOLToWURCS2Std {

	// Version
	private static final String VERSION = "2.0.180815US";

	private static int minNOS = 0;
	private static int minO = 0;
	private static int minBackboneLength = 0;
	private static int maxBackboneLength = 0;
	private static float ratioBackboneNOS = 0;
	private static boolean b_outputfile = false;

	public static void main(String[] args) {
		// usage
		for ( String arg : args ) {
			if( !arg.equals("-help") ) continue;
			usage();
			System.exit(0);
		}
		for ( String arg : args ) {
			if( !arg.equals("-h") ) continue;
			usage();
			System.exit(0);
		}

		for ( String arg : args ) {
			if( !arg.equals("-output") ) continue;
			b_outputfile = true;
		}
		for ( String arg : args ) {
			if( !arg.equals("-out") ) continue;
			b_outputfile = true;
		}

		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);
		minNOS = t_objParam.m_nMinNOS;
		minO   = t_objParam.m_nMinO;
		minBackboneLength = t_objParam.m_iMinBackboneLength;
		maxBackboneLength = t_objParam.m_iMaxBackboneLength;
		ratioBackboneNOS = t_objParam.m_fRatioBackboneNOS;


		//Scanner scanner = new Scanner(System.in);
		//String input1 = scanner.toString();
		//scanner.close();

		for ( String arg : args ) {
			if( !arg.equals("-stdin") ) continue;
	        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
	        readCTStd(buffer, t_objParam.m_strID, t_objParam.m_bOutputSDFile);
			System.exit(0);
		}


		for ( String arg : args ) {
			if( !arg.equals("-f") ) continue;
				for ( String t_strFilepath : t_objParam.getCTfileList() ){
					readCTFile(t_strFilepath, t_objParam.m_strID, t_objParam.m_bOutputSDFile);
				}
		}


/*
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			readCTFile(t_strFilepath, t_objParam.m_strID, t_objParam.m_bOutputSDFile);
		}
*/
	}


	public static void usage() {
		System.err.println("WURCS2.0 Conversion System from CTFile (Molfile or SDfile)");
		System.err.println("\tCurrent version: "+VERSION);
		System.err.println();
		System.err.println("Usage: cat [File] | java (this program).jar [OPTION]... ");
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
		System.err.println("\t-f <file path>\tto read file");
		System.err.println("\t-sdf\t\toutput sd file with WURCS information to stdout");
		System.err.println("\t-end\t\tto ignore arguments after this option");
		System.err.println("\t-stdin\t\tread from STDIN");
		System.err.println("\t\t ex. $ cat input.sdf | java -jar (this program).jar -stdin -out");

		System.err.println("\t-output or -out\t\tcreate text file and log data in a directry ./[YYYYMMDD]");
		System.err.println("\t-help or -h\t\tto print this help message");
		System.err.println();
		System.err.println("FILE is mol or sd file and must be include filename extension \".mol\" or \".sdf\".");
		System.err.println("This soft is only support molfile/sdfile V2000.");
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
			Molecule t_oMolecule = t_objCTReader.getMolecule();
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
			//System.err.println(ID);

			// Output MOL string
			if ( a_bOutput )
				System.out.print( t_objCTReader.getMOLString() );

			try {
				// Convert Molecule to WURCSGraph
				MoleculeToWURCSGraph t_oMol2Graph = new MoleculeToWURCSGraph();
				t_oMol2Graph.getCarbonChainFinder().setParameters(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);
				t_oMol2Graph.start(t_oMolecule);
				WURCSGraph t_oGraph = t_oMol2Graph.getWURCSGraph();

				// Normalize WURCSGraph and generate WURCS
				WURCSFactory t_oFactory = new WURCSFactory(t_oGraph);
				String t_strWURCS = t_oFactory.getWURCS();
				//System.err.println(t_strWURCS);
//				t_objGraphToArray.start(t_objGlycan);

				// TODO: Temporary repairs for MAP
				if ( t_strWURCS.contains("*OP^XO*/3O/3=O") ) {
					t_strWURCS = t_strWURCS.replaceAll("\\*OP\\^XO\\*/3O/3=O", "*OPO*/3O/3=O");
				}

				//System.err.println(t_strWURCS);

				// XXX: remove print
//				System.err.println(t_objCTReader.getFieldData(a_strFieldID));











				t_oLogger.addWURCS(ID, t_strWURCS);
//				System.exit(0);


				// Treatment aglycone
				WURCSFactoryForAglycone t_oFactoryA = new WURCSFactoryForAglycone(t_strWURCS);

				// For no aglycone
				if ( !t_oFactoryA.hasAglycone() ) {
					// For no aglycone
					t_mapIDtoWURCS.put(ID+"\t1", t_strWURCS);

					// Output WURCS tags
					if ( a_bOutput ) {
						System.out.print("> <WURCS2.0>\n"+t_strWURCS+"\n\n");
						System.out.print("$$$$\n");
					}
					continue;
				}

				// For separated WURCS by each aglycone
				String t_strSepWURCSs = "";
				for ( String t_strSepWURCS : t_oFactoryA.getSeparatedWURCSs() )
					t_strSepWURCSs += t_strSepWURCS+"\n";
				// For separated aglycones
				String t_strAglycones = "";
				for ( String t_strAglyconeAbbr : t_oFactoryA.getSeparatedAglycones() )
					t_strAglycones += t_strAglyconeAbbr+"\n";
				String t_strStdWURCSs = "";


				// For standerd WURCSs (remain one atom aglycone)
				int i=0;
				for ( String t_strStdWURCS : t_oFactoryA.getStandardWURCSs() ) {
					i++;
					//t_strStdWURCSs += t_strStdWURCS+"\n";

					// WURCSValidator start
					WURCSValidator validator=new WURCSValidator();
					validator.start(t_strStdWURCS);
					t_strStdWURCS=validator.getStandardWURCS();
					if(validator.getTheNumberOfErrors()==0) System.out.println(t_strStdWURCS);
					System.out.println("the number of errors: "+validator.getTheNumberOfErrors());
					for(String er: validator.getErrors()) System.out.println("	Error:   "+er);
					for(String wa: validator.getWarnings()) System.out.println("	Warning: "+wa);
					System.out.println();
					// WURCSValidator end

					t_strStdWURCSs += t_strStdWURCS;
					t_mapIDtoWURCS.put(ID+"\t"+i, t_strStdWURCS);
				}

				//System.err.println(t_strStdWURCSs);

				// Output WURCS tags with aglycones
				if ( a_bOutput ) {
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

		// console output
		// TreeMap<String, String>
		for ( String id : t_mapIDtoWURCS.keySet() ) {
			System.err.println(id+"\t"+t_mapIDtoWURCS.get(id));
		}


		if (b_outputfile == true ) {

			// Output results
			String t_strFileName = t_objCTReader.getFileName();
			String t_strDirName = "";
			if (t_objCTReader.getDirectoryName() == "null") {
				t_strDirName = t_objCTReader.getDirectoryName() + File.separator;
			}

			//System.err.println("Output result to "+t_strDirName);


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


	public static void readCTStd(BufferedReader buffer, String a_strFieldID, boolean a_bOutput) {
		TreeMap<String, String> t_mapIDtoWURCS = new TreeMap<String, String>();

		// read CTFiles
		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		CTFileReader t_objCTReader = new CTFileReader(buffer);
		//CTFileReader t_objCTReader = new CTFileReader(a_strFilePath);

		WURCSConversionLogger t_oLogger = new WURCSConversionLogger();

		// Set skip IDs
		LinkedList<String> t_aSkipIDs = new LinkedList<String>();
//		t_aSkipIDs.add("CHEBI:52917");
//		t_aSkipIDs.add("CHEBI:51385");
//		t_aSkipIDs.add("CHEBI:51386");
//		t_aSkipIDs.add("CHEBI:51399");

		while ( t_objCTReader.readNext() ) {
		//while ( scanner.next() != null) {
			// read a record from CTFile
			Molecule t_oMolecule = t_objCTReader.getMolecule();
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
			//System.err.println(ID);

			// Output MOL string
			if ( a_bOutput )
				System.out.print( t_objCTReader.getMOLString() );

			try {
				// Convert Molecule to WURCSGraph
				MoleculeToWURCSGraph t_oMol2Graph = new MoleculeToWURCSGraph();
				t_oMol2Graph.getCarbonChainFinder().setParameters(minNOS, minO, minBackboneLength, maxBackboneLength, ratioBackboneNOS);
				t_oMol2Graph.start(t_oMolecule);
				WURCSGraph t_oGraph = t_oMol2Graph.getWURCSGraph();

				// Normalize WURCSGraph and generate WURCS
				WURCSFactory t_oFactory = new WURCSFactory(t_oGraph);
				String t_strWURCS = t_oFactory.getWURCS();
				//System.err.println(t_strWURCS);
//				t_objGraphToArray.start(t_objGlycan);

				// TODO: Temporary repairs for MAP
				if ( t_strWURCS.contains("*OP^XO*/3O/3=O") ) {
					t_strWURCS = t_strWURCS.replaceAll("\\*OP\\^XO\\*/3O/3=O", "*OPO*/3O/3=O");
				}

				//System.err.println(t_strWURCS);

				// XXX: remove print
//				System.err.println(t_objCTReader.getFieldData(a_strFieldID));

				t_oLogger.addWURCS(ID, t_strWURCS);
//				System.exit(0);


				// Treatment aglycone
				WURCSFactoryForAglycone t_oFactoryA = new WURCSFactoryForAglycone(t_strWURCS);

				// For no aglycone
				if ( !t_oFactoryA.hasAglycone() ) {
					// For no aglycone
					t_mapIDtoWURCS.put(ID+"\t1", t_strWURCS);

					// Output WURCS tags
					if ( a_bOutput ) {
						System.out.print("> <WURCS2.0>\n"+t_strWURCS+"\n\n");
						System.out.print("$$$$\n");
					}
					continue;
				}

				// For separated WURCS by each aglycone
				String t_strSepWURCSs = "";
				for ( String t_strSepWURCS : t_oFactoryA.getSeparatedWURCSs() )
					t_strSepWURCSs += t_strSepWURCS+"\n";
				// For separated aglycones
				String t_strAglycones = "";
				for ( String t_strAglyconeAbbr : t_oFactoryA.getSeparatedAglycones() )
					t_strAglycones += t_strAglyconeAbbr+"\n";
				String t_strStdWURCSs = "";


				// For standerd WURCSs (remain one atom aglycone)
				int i=0;
				for ( String t_strStdWURCS : t_oFactoryA.getStandardWURCSs() ) {
					i++;

					// WURCSValidator start
					WURCSValidator validator=new WURCSValidator();
					validator.start(t_strStdWURCS);
					t_strStdWURCS=validator.getStandardWURCS();
					if(validator.getTheNumberOfErrors()==0) System.out.println(t_strStdWURCS);
					System.out.println("the number of errors: "+validator.getTheNumberOfErrors());
					for(String er: validator.getErrors()) System.out.println("	Error:   "+er);
					for(String wa: validator.getWarnings()) System.out.println("	Warning: "+wa);
					System.out.println();
					// WURCSValidator end


					//t_strStdWURCSs += t_strStdWURCS+"\n";
					t_strStdWURCSs += t_strStdWURCS;
					t_mapIDtoWURCS.put(ID+"\t"+i, t_strStdWURCS);
				}

				//System.err.println(t_strStdWURCSs);

				// Output WURCS tags with aglycones
				if ( a_bOutput ) {
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

		// console output
		// TreeMap<String, String>
		for ( String id : t_mapIDtoWURCS.keySet() ) {
			System.err.println(id+"\t"+t_mapIDtoWURCS.get(id));
		}


		if (b_outputfile == true ) {

			// Output results
			String t_strFileName = t_objCTReader.getFileName();
			String t_strDirName = "";
			if (t_objCTReader.getDirectoryName() == "null") {
				t_strDirName = t_objCTReader.getDirectoryName() + File.separator;
			}

			//System.err.println("Output result to "+t_strDirName);


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



}

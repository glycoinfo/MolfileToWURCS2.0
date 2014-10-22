package io.MDLMOL;

import glycan.Glycan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import chemicalgraph.subgraph.backbone.Backbone;
import chemicalgraph.subgraph.molecule.Molecule;

/**
 *
 * @author Masaaki Matsubara
 *
 */
public class example {
	//----------------------------
	// Public method
	//----------------------------
	public static void main(final String[] args) {

		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		/*
		try {
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
			System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(df.format(date)
					+ "_m_minO_" + t_objParam.m_minO
					+ "_minNOS_" + t_objParam.m_minNOS
					+ "_maxBoackBone_" + t_objParam.m_maxBackboneLength
					+ "_WURCS_v1.0_unicarb-db.txt", false)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/

//		WURCSGlycanImporter t_objGlycanImporter = new WURCSGlycanImporter();
//		t_objGlycanImporter.setBackboneParameters(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);

//		String t_strFilepath = "C:\\webapps\\GlycoCTtoMolfile\\fuel\\app\\tmp\\20140807result-GlycomeDBnew.sdf";
		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		for ( String t_strFilepath : t_objParam.getCTfileList() ){
			// read CTFiles
			CTFileReader t_objCTReader = new CTFileReader(t_strFilepath, t_objParam.m_sdfileOutput);
			while(true){
				// read a record from CTFile
				Molecule mol = t_objCTReader.getMolecule();
				if(mol==null) break;
				if(!t_objParam.m_sdfileOutput){
					System.out.print( t_objCTReader.getFieldData(t_objParam.m_ID) );
				}
				System.err.println( t_objCTReader.getFieldData("ID") );

				mol.generateWURCS(t_objParam.m_minNOS, t_objParam.m_minO, t_objParam.m_minBackboneLength, t_objParam.m_maxBackboneLength, t_objParam.m_ratioBackboneNOS);  //Issaku YAMADA
				// generate WURCS
/*				try {
					t_objGlycanImporter.generateWURCS(mol);
				} catch (WURCSGlycanObjectException e) {
					e.printStackTrace();
				}
*/
				// Output results
				if(t_objParam.m_sdfileOutput){
					System.out.println("> <WURCS>");
//					for(Glycan glycan : t_objGlycanImporter.m_aGlycans){
					for(Glycan glycan : mol.glycans){
						System.out.println(glycan.WURCS);
					}
					System.out.println();
					System.out.println("> <MainChain>");
					for(LinkedList<Backbone> backbones : mol.candidateBackboneGroups){
//					for(LinkedList<Backbone> backbones : t_objGlycanImporter.m_aCandidateBackboneGroups){
						for(Backbone backbone : backbones){
							if(!backbone.isBackbone) continue;
							System.out.println(backbone.molfileAtomNos());
						}
					}
					System.out.println();
					System.out.println("$$$$");
				}else{
					for(Glycan glycan : mol.glycans){
//					for(Glycan glycan : t_objGlycanImporter.m_aGlycans){
						System.err.println("\t" + glycan.WURCS);
					}
					System.err.println();
				}
				t_objCTReader.recordNo++;
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
/*
*/
}

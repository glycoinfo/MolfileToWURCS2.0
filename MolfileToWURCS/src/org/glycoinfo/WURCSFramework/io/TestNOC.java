package org.glycoinfo.WURCSFramework.io;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.chemicalgraph.Atom;
import org.glycoinfo.WURCSFramework.chemicalgraph.Molecule;
import org.glycoinfo.WURCSFramework.io.MDLMOL.CTFileReader;
import org.glycoinfo.WURCSFramework.io.MDLMOL.ParameterReader;
import org.glycoinfo.WURCSFramework.util.FileIOUtils;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.NOC.NOCApproach;
import org.glycoinfo.WURCSFramework.util.chemicalgraph.analytical.cyclization.Aromatization;

public class TestNOC {

	public static void main(String[] args) {
		// read argument and files using SelectFileDialog
		ParameterReader t_objParam = new ParameterReader(args, true);

		for ( String t_strFilepath : t_objParam.getCTfileList() ) {
			// New SDFile
			String t_strOutSDFile = t_strFilepath;
			if ( t_strOutSDFile.contains(".mol") )
				t_strOutSDFile = t_strOutSDFile.replace(".mol", "");
			if ( t_strOutSDFile.contains(".sdf") )
				t_strOutSDFile = t_strOutSDFile.replace(".sdf", "");
			System.out.println(t_strOutSDFile);
			try {
				PrintWriter t_pwNewSDF = FileIOUtils.openTextFileW(t_strOutSDFile +"_score0.sdf");
				readCTFile(t_strFilepath, t_pwNewSDF, NOCApproach.SCORING_TYPE0);
				t_pwNewSDF.close();
				t_pwNewSDF = FileIOUtils.openTextFileW(t_strOutSDFile +"_score1.sdf");
				readCTFile(t_strFilepath, t_pwNewSDF, NOCApproach.SCORING_TYPE1);
				t_pwNewSDF.close();
				t_pwNewSDF = FileIOUtils.openTextFileW(t_strOutSDFile +"_score2.sdf");
				readCTFile(t_strFilepath, t_pwNewSDF, NOCApproach.SCORING_TYPE2);
				t_pwNewSDF.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void readCTFile(String a_strFilePath, PrintWriter a_pwOut, int a_iScoringType) {
		// read CTFiles
		CTFileReader t_objCTReader = new CTFileReader(a_strFilePath, false);

		int t_nMol = 0;
		while(true){
			// read a record from CTFile
			Molecule mol = t_objCTReader.getMolecule();
			if(mol==null) break;
			t_nMol++;
			String ID = t_objCTReader.getFieldData("ChEBI_ID");
			System.out.println(t_nMol+":"+ID);

			// Get aromatic carbons
			// Search aromatic atoms
			HashSet<Atom> t_aAromatics = new HashSet<Atom>();
			Aromatization t_oAromatic = new Aromatization();
			for ( Atom a : mol.getAtoms() ) {
				t_oAromatic.clear();
				if ( t_oAromatic.start(a) ) t_aAromatics.addAll(t_oAromatic);
			}

			NOCApproach t_oNOC = new NOCApproach(mol);
			if ( a_iScoringType == NOCApproach.SCORING_TYPE1 )
				t_oNOC.setType1();
			if ( a_iScoringType == NOCApproach.SCORING_TYPE2 )
				t_oNOC.setType2();
			String t_strPhaseI  = "";
			String t_strPhaseII = "";
			String t_strAromatics = "";
			String t_strHitAtoms = "";
			HashSet<Atom> t_aMSCarbons = t_oNOC.getMonosaccharideCarbon();
			LinkedList<Atom> t_aAtoms = mol.getAtoms();
			for ( Atom t_oAtom : t_aAtoms ) {
				int t_iID = t_aAtoms.indexOf(t_oAtom) + 1;
				if ( !t_oAtom.getSymbol().equals("C") ) continue;

				if ( !t_strPhaseI.equals("") ) t_strPhaseI += ",";
				t_strPhaseI  += t_iID+":"+t_oNOC.getNOCNumMapPhase1().get(t_oAtom);
				if ( !t_strPhaseII.equals("") ) t_strPhaseII += ",";
				t_strPhaseII += t_iID+":"+t_oNOC.getNOCNumMapPhase2().get(t_oAtom);

				// For aromatics
				if ( t_aAromatics.contains(t_oAtom) ){
					if ( !t_strAromatics.equals("") ) t_strAromatics += ",";
					t_strAromatics += t_iID;
					continue;
				}

				if ( !t_aMSCarbons.contains(t_oAtom) ) continue;

				// Make hit atoms
				if ( !t_strHitAtoms.equals("") ) t_strHitAtoms += ",";
				t_strHitAtoms += t_iID;
			}

			// Make tags
			String t_strTag = "> <NOC-PassI>\n";
			t_strTag += t_strPhaseI+"\n\n";
			t_strTag += "> <NOC-PassII>\n";
			t_strTag += t_strPhaseII+"\n\n";
			t_strTag += "> <AromaticCarbons>\n";
			t_strTag += t_strAromatics+"\n\n";
			t_strTag += "> <HitAtoms>\n";
			t_strTag += t_strHitAtoms+"\n\n";

			String t_strNewSDF = t_objCTReader.getMOLString();
			t_strNewSDF += t_strTag+"$$$$\n";

			a_pwOut.write( t_strNewSDF );
			a_pwOut.flush();
		}
	}

}

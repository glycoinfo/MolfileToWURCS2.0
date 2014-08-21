package molfile2wurcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import javax.swing.JFileChooser;

/**
 * 引数処理
 * @author KenichiTanaka
 * @author IssakuYAMADA
 */
public class Parameter{
	//----------------------------
	// Member variable
	//----------------------------
	public static int m_minNOS = 2; //BackboneLength 2014/05/27 IY // static 2014/06/18 Issaku YAMADA
	public static int m_minO = 2;   //BackboneLength 2014/05/27 IY  // static 2014/06/18 Issaku YAMADA
	public int m_minBackboneLength = 3; //ok 2014/05/27 Issaku YAMADA
	//public static int m_maxBackboneLength = 9; //;9,10,...20, 100, 1000, max(10000) 2014/05/27　2014/06/18 (999 IY // static 2014/06/18 IY
	public static int m_maxBackboneLength = 999; //2014/07/24 Issaku YAMADA
	public String m_dir = null;
	public String m_ID = null;
	public boolean m_sdfileOutput = false;
	public LinkedList<CTFile> ctfiles = new LinkedList<CTFile>();
	private BufferedReader br = null;
	//2014/07/29 Issaku YAMADA
	//Backbone炭素数をBackboneに結合しているNOSの数で割った値 > ratoBackboneNOSであれば単糖Backbone候補とする。
	public static float ratioBackboneNOS = 2.0f;

	//----------------------------
	// Constructor
	//----------------------------
	public Parameter(final String[] args, final boolean useFileChooser){
		// 引数をパースし、該当するメンバ変数に値を入れる。
		for(int ii=0; ii<args.length; ii++){
			if(      args[ii].equals("-end")             ){ break; }
			else if( args[ii].equals("-minNOS")          ){ ii++; this.m_minNOS = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minO")            ){ ii++; this.m_minO = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-minBackboneLength")){ ii++; this.m_minBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-maxBackboneLength")){ ii++; this.m_maxBackboneLength = Integer.parseInt(args[ii]); }
			else if( args[ii].equals("-ratioBackboneNOS")){ ii++; this.ratioBackboneNOS = Float.parseFloat(args[ii]); }//2014/07/29 Issaku YAMADA
			else if( args[ii].equals("-ID")              ){ ii++; this.m_ID = args[ii]; }
			else if( args[ii].equals("-dir")             ){ ii++; this.m_dir = args[ii]; }
			else if( args[ii].equals("-sdf")             ){ this.m_sdfileOutput = true; }
			else{                                         this.recursivelyAddFiles(args[ii], this.ctfiles); }
		}

		// ファイルが一つも見つからず、useFileChooserがtrueの場合、ファイル選択ダイアログを用いたファイル選択を実施
		if ((this.ctfiles.size() == 0 ) && useFileChooser) {
			JFileChooser filechooser = new JFileChooser(this.m_dir);
			filechooser.setMultiSelectionEnabled(true);
			filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int selected = filechooser.showOpenDialog(null);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File[] files = filechooser.getSelectedFiles();
				for(File file : files){
					this.recursivelyAddFiles(file.getPath(), this.ctfiles);
				}
			}
		}
		
		// IDに用いるフィールド名が選択されていない場合、入力CTFileからユニークなフィールド一覧を取得して表示し
		if(this.m_ID == null){
			// ユニークなフィールド一覧の取得
			LinkedList<String> fields = new LinkedList<String>();
			for(CTFile ctfile : this.ctfiles){
				File file = new File(ctfile.filepath);
				ctfile.filename = file.getName();
				ctfile.totalRecodeNum = 0;
				try {
					FileReader fr = new FileReader(file);
			        br = new BufferedReader(fr);
			        String line = br.readLine();
					while(line != null){
						if(line.length()>=6 && line.substring(0, 6).trim().equals("M  END")){
							ctfile.totalRecodeNum++;
						}

						if(line.length() != 0){
				    		if(line.substring(0, 1).equals(">")){
				    			// DataHeader[0] = "> 25 ", DataHeader[1] = "ALTERNATE.NAMES>"
				    			// DataHeader_tmp[0] = "ALTERNATE.NAMES"
				    			String DataHeader[] = line.split("<");
				    			String DataHeader_tmp[] = DataHeader[1].split(">");
				    			String key = DataHeader_tmp[0];
				    			if(!fields.contains(key))fields.add(key);
				    		}
						}
			    		line = br.readLine();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(fields.size() != 0){
				// フィールドの表示
				int No = 0;
				for(String ID : fields){
					System.err.println((No) + " : " + ID);
					No++;
				}
				System.err.print("Prease select ID field : ");

				// フィールドの選択
				String inputdata = null;
				try {
					br = new BufferedReader(new InputStreamReader(System.in), 1);
					inputdata = br.readLine();
					No = Integer.parseInt(inputdata);
					this.m_ID = (No < fields.size()) ? fields.get(No) : null;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NumberFormatException e){
					this.m_ID = null;
				}
			}
		}
	}

	//----------------------------
	// Private method
	//----------------------------
	private void recursivelyAddFiles(String filename, LinkedList<CTFile> ctfiles){
		File fp = new File(filename);
		if(fp.isFile()){
			if(!(fp.getPath().endsWith(".mol")) && !(fp.getPath().endsWith(".sdf"))) return;
			CTFile molfile = new CTFile();
			molfile.filepath = filename;
			ctfiles.add(molfile);
		}else if(fp.isDirectory()){
			File[] filelist = fp.listFiles();
			for(int jj=0; jj<filelist.length; jj++){
				this.recursivelyAddFiles(filelist[jj].getPath(), ctfiles);
			}
		}
	}
}

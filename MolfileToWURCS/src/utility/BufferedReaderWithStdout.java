package utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * BufferedReaderで読み込んだ内容を標準出力に出力する。
 * 読み込み内容に付加情報を付けて出力する際に利用
 * @author KenichiTanaka
 */
public class BufferedReaderWithStdout {
	//----------------------------
	// Member variable
	//----------------------------
	BufferedReader br;

	//----------------------------
	// Constructor
	//----------------------------
	public BufferedReaderWithStdout(FileReader fr){
		br = new BufferedReader(fr);
	}

	//----------------------------
	// Public method
	//----------------------------
	public String readLine(boolean outputStdout, String ignore){
		String line = "";
		try {
			line = br.readLine();
			if(line==null) return null;
			if(outputStdout){
				if(ignore==null || !line.equals(ignore)){
					System.out.println(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
	public String readLine(boolean outputStdout){
		return this.readLine(outputStdout, null);
	}
}

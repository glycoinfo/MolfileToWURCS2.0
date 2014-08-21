package chemicalgraph.subgraph.modification;

import chemicalgraph.Atom;
import chemicalgraph.Bond;

/**
 * 修飾に含まれる部分構造を探索し、修飾文字列を生成する際、探索した原子の情報を格納するクラス
 * @author KenichiTanaka
 */
public class Path {
	//----------------------------
	// Member variable
	//----------------------------
	Path pathStart;
	Path pathEnd;
	Atom atom;
	Bond bond;

	//----------------------------
	// Constructor
	//----------------------------
	public Path(final Path start, final Path end, final Atom atom, final Bond bond){
		this.pathStart = start;
		this.pathEnd = (end!=null) ? end : this;
		this.bond = bond;
		this.atom = (end==null) ? atom : null;
	}
}

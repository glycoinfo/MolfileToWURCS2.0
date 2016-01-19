package org.glycoinfo.WURCSFramework.util.chemicalgraph;

public enum PeriodicTable {

	H (  1, "H"),
	He(  2, "He"), //   1 -   2

	Li(  3, "Li"),
	Be(  4, "Be"),
	B (  5, "B"),
	C (  6, "C"),
	N (  7, "N"),
	O (  8, "O"),
	F (  9, "F"),
	Ne( 10, "Ne"), //   3 -  10

	Na( 11, "Na"),
	Mg( 12, "Mg"),
	Al( 13, "Al"),
	Si( 14, "Si"),
	P ( 15, "P"),
	S ( 16, "S"),
	Cl( 17, "Cl"),
	Ar( 18, "Ar"), //  11 -  18

	K ( 19, "K"),
	Ca( 20, "Ca"),
	Sc( 21, "Sc"),
	Ti( 22, "Ti"),
	V ( 23, "V"),
	Cr( 24, "Cr"),
	Mn( 25, "Mn"),
	Fe( 26, "Fe"),
	Co( 27, "Co"),
	Ni( 28, "Ni"),
	Cu( 29, "Cu"),
	Zn( 30, "Zn"),
	Ga( 31, "Ga"),
	Ge( 32, "Ge"),
	As( 33, "As"),
	Se( 34, "Se"),
	Br( 35, "Br"),
	Kr( 36, "Kr"), //  19 -  36

	Rb( 37, "Rb"),
	Sr( 38, "Sr"),
	Y ( 39, "Y"),
	Zr( 40, "Zr"),
	Nb( 41, "Nb"),
	Mo( 42, "Mo"),
	Tc( 43, "Tc"),
	Ru( 44, "Ru"),
	Rh( 45, "Rh"),
	Pd( 46, "Pd"),
	Ag( 47, "Ag"),
	Cd( 48, "Cd"),
	In( 49, "In"),
	Sn( 50, "Sn"),
	Sb( 51, "Sb"),
	Te( 52, "Te"),
	I ( 53, "I"),
	Xe( 54, "Xe"), //  37 -  54

	Cs( 55, "Cs"),
	Ba( 56, "Ba"), //  55 -  56

	La( 57, "La"),
	Ce( 58, "Ce"),
	Pr( 59, "Pr"),
	Nd( 60, "Nd"),
	Pm( 61, "Pm"),
	Sm( 62, "Sm"),
	Eu( 63, "Eu"),
	Gd( 64, "Gd"),
	Tb( 65, "Tb"),
	Dy( 66, "Dy"),
	Ho( 67, "Ho"),
	Er( 68, "Er"),
	Tm( 69, "Tm"),
	Yb( 70, "Yb"),
	Lu( 71, "Lu"), //  57 -  71

	Hf( 72, "Hf"),
	Ta( 73, "Ta"),
	W ( 74, "W"),
	Re( 75, "Re"),
	Os( 76, "Os"),
	Ir( 77, "Ir"),
	Pt( 78, "Pt"),
	Au( 79, "Au"),
	Hg( 80, "Hg"),
	Tl( 81, "Tl"),
	Pb( 82, "Pb"),
	Bi( 83, "Bi"),
	Po( 84, "Po"),
	At( 85, "At"),
	Rn( 86, "Rn"), //  72 -  86

	Fr( 87, "Fr"),
	Ra( 88, "Ra"), //  87 -  88

	Ac( 89, "Ac"),
	Th( 90, "Th"),
	Pa( 91, "Pa"),
	U ( 92, "U"),
	Np( 93, "Np"),
	Pu( 94, "Pu"),
	Am( 95, "Am"),
	Cm( 96, "Cm"),
	Bk( 97, "Bk"),
	Cf( 98, "Cf"),
	Es( 99, "Es"),
	Fm(100, "Fm"),
	Md(101, "Md"),
	No(102, "No"),
	Lr(103, "Lr"), //  89 - 103

	A  (-1, "A"),
	Q  (-1, "Q"),
	X  (-1, "X"),
	UNK(-1, "?"),
	R  (-1, "R"); // unknown or anonymous symbols

	private int m_iAtomicNumber;
	private String m_strSymbol;

	private PeriodicTable(int a_iNum, String a_strSymbol) {
		this.m_iAtomicNumber = a_iNum;
		this.m_strSymbol     = a_strSymbol;
	}

	public int getAtomicNumber() {
		return this.m_iAtomicNumber;
	}

	public String getSymbol() {
		return this.m_strSymbol;
	}
}

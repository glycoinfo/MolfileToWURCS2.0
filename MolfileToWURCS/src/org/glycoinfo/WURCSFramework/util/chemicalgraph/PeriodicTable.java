package org.glycoinfo.WURCSFramework.util.chemicalgraph;

public enum PeriodicTable {

	H ("H"),
	He("He"), //   1 -   2

	Li("Li"),
	Be("Be"),
	B ("B"),
	C ("C"),
	N ("N"),
	O ("O"),
	F ("F"),
	Ne("Ne"), //   3 -  10

	Na("Na"),
	Mg("Mg"),
	Al("Al"),
	Si("Si"),
	P ("P"),
	S ("S"),
	Cl("Cl"),
	Ar("Ar"), //  11 -  18

	K ("K"),
	Ca("Ca"),
	Sc("Sc"),
	Ti("Ti"),
	V ("V"),
	Cr("Cr"),
	Mn("Mn"),
	Fe("Fe"),
	Co("Co"),
	Ni("Ni"),
	Cu("Cu"),
	Zn("Zn"),
	Ga("Ga"),
	Ge("Ge"),
	As("As"),
	Se("Se"),
	Br("Br"),
	Kr("Kr"), //  19 -  36

	Rb("Rb"),
	Sr("Sr"),
	Y ("Y"),
	Zr("Zr"),
	Nb("Nb"),
	Mo("Mo"),
	Tc("Tc"),
	Ru("Ru"),
	Rh("Rh"),
	Pd("Pd"),
	Ag("Ag"),
	Cd("Cd"),
	In("In"),
	Sn("Sn"),
	Sb("Sb"),
	Te("Te"),
	I ("I"),
	Xe("Xe"), //  37 -  54

	Cs("Cs"),
	Ba("Ba"), //  55 -  56

	La("La"),
	Ce("Ce"),
	Pr("Pr"),
	Nd("Nd"),
	Pm("Pm"),
	Sm("Sm"),
	Eu("Eu"),
	Gd("Gd"),
	Tb("Tb"),
	Dy("Dy"),
	Ho("Ho"),
	Er("Er"),
	Tm("Tm"),
	Yb("Yb"),
	Lu("Lu"), //  57 -  71

	Hf("Hf"),
	Ta("Ta"),
	W ("W"),
	Re("Re"),
	Os("Os"),
	Ir("Ir"),
	Pt("Pt"),
	Au("Au"),
	Hg("Hg"),
	Tl("Tl"),
	Pb("Pb"),
	Bi("Bi"),
	Po("Po"),
	At("At"),
	Rn("Rn"), //  72 -  86

	Fr("Fr"),
	Ra("Ra"), //  87 -  88

	Ac("Ac"),
	Th("Th"),
	Pa("Pa"),
	U ("U"),
	Np("Np"),
	Pu("Pu"),
	Am("Am"),
	Cm("Cm"),
	Bk("Bk"),
	Cf("Cf"),
	Es("Es"),
	Fm("Fm"),
	Md("Md"),
	No("No"),
	Lr("Lr"), //  89 - 103

	A  ("A"),
	Q  ("Q"),
	X  ("X"),
	UNK("?"),
	R  ("R"); // others

	private String m_strSymbol;

	private PeriodicTable(String a_strSymbol) {
		this.m_strSymbol = a_strSymbol;
	}

	public String getSymbol() {
		return this.m_strSymbol;
	}
}

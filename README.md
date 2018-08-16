# MolfileToWURCS2.0

MolfileToWURCS2.0 is a format convertor from molfile/sdfile to WURCS2.0. 
molfile/sdfile can be use only V2000 format. 

 * Web3 Unique Representation Carbohydrate Structure (WURCS)
 * Version 2
 * Software version 2.0.180815US
 * Aug 15, 2018
 * Originally developed by Japanese Glycoinformatics Group.
 * Modifications and additions by The Noguchi Institue.
 *

MolfileToWURCS2.0/MolfileToWURCS/src/org/glycoinfo/WURCSFramework/exec/MOLToWURCS2Std.java
* "{ID}	{#}	STANDARD	WURCS=2.0/..." is standard WURCS2.0.
* "WITH_AGLYCONE" and "SEPARATED" is not Standard. 

MolfileToWURCS2.0/MolfileToWURCS/src/org/glycoinfo/WURCSFramework/exec/MOLToWURCS2Std.java
 
Usage: cat [File] | java -jar (this program).jar -ID [ID]  -stdin -output [OPTION]...


* where OPTION include:
	-ID <tag ID in sd file>	to select the tag ID in sd file
	-dir <directory path>	to read files in the directory
	-f <file path>	to read file
	-sdf		output sd file with WURCS information to stdout
	-end		to ignore arguments after this option
	-stdin		read from STDIN
		 ex. $ cat input.sdf | java -jar (this program).jar -stdin -out
	-output or -out		create text file and log data in a directry ./[YYYYMMDD]
	-help or -h		to print this help message


Contact: yamadaissaku@gmail.com


# MolfileToWURCS2.0


 * Web3 Unique Representation Carbohydrate Structure (WURCS)
 * Version 2
 * Software version 2.0.20161213-2.0.17829
 * Sep 12, 2017
 * Originally developed by Japanese Glycoinformatics Group.
 * Modifications and additions by The Noguchi Institue.
 *

MolfileToWURCS2.0/MolfileToWURCS/src/org/glycoinfo/WURCSFramework/exec/MOLToWURCS.java
* "{ID}	{#}	STANDARD	WURCS=2.0/..." is standard WURCS2.0.
* "WITH_AGLYCONE" and "SEPARATED" is not Standard. 

MolfileToWURCS2.0/MolfileToWURCS/src/org/glycoinfo/WURCSFramework/exec/MOLToWURCS2.java
 
Usage: java (this program).jar [OPTION]... [FILE]... 

* where OPTION include:
* 	-ID <tag ID in sd file>	to select the tag ID in sd file
* 	-dir <directory path>	to read files in the directory
* 	-sdf		output sd file with WURCS information to stdout
* 	-end		to ignore arguments after this option
* 	-v		to print this Version
* 	-help		to print this help message


Contact: yamadaissaku@gmail.com


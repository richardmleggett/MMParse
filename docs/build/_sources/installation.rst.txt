.. _installation:

Download and installation=========================
Installing LCAParse-------------------LCAParse can be downloaded from GitHub. You can either download the .zip file, or if you have git installed, you can type::
  git clone https://github.com/richardmleggett/LCAParse.gitLCAParse is a Java application. To run it, you just need the LCAParse.jar file. It can be executed by typing::

  java -jar /path/to/LCAParse.jar -help

We also provide a script that executes the jar. This can be found in the bin directory. At the top of it is a line::

  JARFILE=/Users/leggettr/Documents/github/LCAParse/target/LCAParse.jar

You should change this to point to the location of your LCAParse.jar file. You can then place the lcaparse file in a directory pointed to by your PATH variable, so that it is easily available without having to specify the full path. 

Alternatively, add the bin directory to your path variable. On Linux, you would typically do this by adding the following command to your .bash_profile (or .profile on Ubuntu) file or 'source' script::     export PATH=/path/to/LCAParse/bin:$PATHOnce you have done this (you may need to close and re-open your terminal window), you should be able to run by typing::

  lcaparse -help Taxonomy files--------------LCAParse requires the nodes.dmp and names.dmp files from the NCBI Taxonomy. These are available as part of the taxdump download which can be obtained from `https://ftp.ncbi.nlm.nih.gov/pub/taxonomy/ <https://ftp.ncbi.nlm.nih.gov/pub/taxonomy/>`_.

For parsing accession IDs (if tax IDs are not in the Blast output), LCAParse also requires  the nucl_wgs.accession2taxid file from the accession2taxid directory of the NCBI Taxonomy FTP site above.
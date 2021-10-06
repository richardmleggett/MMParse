.. _running:

Running LCAParse
================

Preparing accession maps
------------------------

Blast is capable of outputting the taxon ID of matches if a custom output format is specified. However for the default BlastTab and minimap2 outputs, it is necessary to map accession IDs to taxa using the NCBI accession2taxid data. 

For speed and memory reasons, LCAParse uses a reformatted version of accession2taxid and you will need to create this file.

You can do this using the following command::

  lcaparse -makemap -input /path/to/nucl_gb.accession2taxid -output /path/to/file_prefix -taxonomy /path/to/taxonomy_files

where:

-  ``-input`` specifies the name of a nucl_gb.accession2taxid file
-  ``-output`` specifes a prefix to use for output filenames
-  ``-taxonomy`` specifies the directory containing NCBI taxonomy files (files needed are nodes.dmp and names.dmp)

lcaparse will output six files:

-  prefix_bacteria.txt - a mapping file between accession IDs and bacterial taxon IDs.
-  prefix_viruses.txt - a mapping file between accession IDs and viral taxon IDs.
-  prefix_archea.txt - a mapping file between accession IDs and archea taxon IDs.
-  prefix_eukaryota.txt - a mapping file between accession IDs and eukaryote taxon IDs.
-  preifx_other.txt - a mapping file between accession IDs and other taxon IDs.
-  prefix_unclassified.txt - a mapping file between accession IDs and unclassified taxons.

You can merge these files if you need to. For example, if you want a mapping file for bacteria and viruses::

   cat map_bacteria.txt map_viruses.txt > map_bacvir.txt

Running LCAParse
----------------

To run, type a command of the form::

  lcaparse -input filename.txt -output output.txt -taxonomy /path/to/taxonomy/dir -mapfile /path/to/mapfile.txt -format blasttab

where:

-  ``-input`` specifies the name of an input Blast or minimap2 file
-  ``-output`` specifies the name of an output file for LCA results
-  ``-taxonomy`` specifies the directory containing NCBI taxonomy files (files needed are nodes.dmp and names.dmp)
-  ``-format`` specifies the input file format - either 'nanook', 'blasttab' or 'PAF'.
-  ``-mapfile`` specifies the name of an accession map file created as detailed above. This is needed for blasttab and PAF format files.

Other options:

-  ``-maxhits`` specifies maximum number of hits to consider for given read (default 20)
-  ``-scorepercent`` specifies minimum score threshold as percentage of top score for given read (default 90)
-  ``-limitspecies`` limits taxonomy to species level (i.e. not strain)

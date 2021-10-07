# MMParse

MMParse parses MetaMaps output files. It was originally written for analysis of nanopore adaptive sampling data.

A Java JAR file can be found in the target directory.

## To analyse enriched yield

Inputs are two .reads2Taxon files, one for the enriched channels, one for control channels.

To run:

    java -jar MMParse.jar -count -control control.reads2Taxon -enriched enriched.read2Taxon -lengths readlengths.txt -taxon 47420 -taxonomy /path/to/taxonomy

The lengths file is a two column tab separated file consiting of read ID and read length. It can be generated using the get_contig_stats.pl script in the bin directory, e.g.:

    get_contig_stats.pl -i input.fastq -fastq -r readlengths.txt

The taxon is the target taxon. Any descendent of this taxon will be counted as a hit.

The taxonomy path leads to a directory containing nodes.dmp and names.dmp.

## To generate a file suitable for importing to MEGAN

mmparse can also be used to generate a comma separated taxon,abudance file for importing into MEGAN. Use:

    java -jar MMParse.jar -megan -input sample.reads2Taxon -taxonomy /path/to/taxonomy

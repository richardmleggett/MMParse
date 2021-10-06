/*
 * Author: Richard M. Leggett
 * © Copyright 2021 Earlham Institute
 */

package leggett.mmparse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;

public class MMParse {  
    public MMParseOptions options;
    
    public MMParse(MMParseOptions o) {
        options = o;
    }
    
    public void readLengthsFile(String filename, Hashtable<String,Integer> lengths) {
       BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(filename));
            String line;

            do {
                line = br.readLine();
                if (line != null) {
                    String[] fields = line.split("\t");
                    if (fields.length == 2) {
                        String id = fields[0];
                        int length = Integer.parseInt(fields[1]);
                        if (lengths.containsKey(id)) {
                            System.out.println("That's weird - id appears twice "+id);
                        } else {
                            lengths.put(id, length);
                        }
                    } else {
                        System.out.println("Badly formatted length line "+line);
                    }
                }
            } while (line != null);
            br.close();
        } catch (Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    public void analyseMetaMapsFile(String filename, Hashtable<String,Integer> lengths, Taxonomy taxonomy) {
       BufferedReader br;
       int readCount = 0;
       int bpCount = 0;

        try {
            br = new BufferedReader(new FileReader(filename));
            String line;

            do {
                line = br.readLine();
                if (line != null) {
                    String[] fields = line.split("\t");
                    if (fields.length == 2) {
                        String id = fields[0];
                        String taxonString = fields[1];
                        long taxon = 0;
                        
                        if (taxonString.startsWith("x")) {
                            taxon = taxonomy.getMinimapPsuedospecies(taxonString);
                            //System.out.println("Got x - "+id);
                        } else {
                            taxon = Integer.parseInt(fields[1]);
                        }
                        
                        if (taxon > 0) {
                            if (taxonomy.isTaxonAncestor(taxon, 47420)) {
                                readCount++;
                                bpCount += lengths.get(id);
                            }
                        } else {
                            //System.out.println("Got taxon " + taxonString + " " + line);
                        }                        
                    } else {
                        System.out.println("Badly formatted line " + line);
                    }
                }
            } while (line != null);
            br.close();            
        } catch (Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
            System.exit(1);
        }        
        System.out.println("Reads "+readCount);
        System.out.println("Size: "+bpCount);
            
    }
    
    public void processMetaMaps() {
        Hashtable<String,Integer> readLengths = new Hashtable<String,Integer>();
        readLengthsFile(options.getLengthsFilename(), readLengths);

        Taxonomy taxonomy = new Taxonomy(options, options.getTaxonomyDirectory() + "/nodes.dmp", options.getTaxonomyDirectory() + "/names.dmp");  
        
        System.out.println("Analysing control...");
        analyseMetaMapsFile(options.getControlFilename(), readLengths, taxonomy);
        System.out.println("Analysing enriched...");
        analyseMetaMapsFile(options.getEnrichedFilename(), readLengths, taxonomy);
    }

    public static void main(String[] args) {
        MMParseOptions ops = new MMParseOptions();        
        ops.processCommandLine(args);
        MMParse lcap = new MMParse(ops);
        lcap.processMetaMaps();
    }        
}

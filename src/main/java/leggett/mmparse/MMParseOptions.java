/*
 * Author: Richard M. Leggett
 * © Copyright 2021 Earlham Institute
 */

package leggett.mmparse;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MMParseOptions {
    public final static String version="v0.1";
    private String controlFilename = null;
    private String enrichedFilename = null;
    private String outputPrefix = null;
    private String taxonomyDirectory = null;
    private String mapFilename = null;
    private String lengthsFilename = null;
    private boolean withWarnings = true;
    private boolean runningCount = false;
    private boolean runningMegan = false;
    
    public MMParseOptions() {
    }
        
    public void displayHelp() {
        System.out.println("");
        System.out.println("mmparse "+version);
        System.out.println("Comments/queries: richard.leggett@earlham.ac.uk");
        System.out.println("");
        System.out.println("To count reads and total bp:");
        System.out.println("    mmparse -count -control <filename> -enriched <filename> -lengths <filename> -taxonomy <directory>");        
        System.out.println("");
        System.out.println("To convert a reads2Taxon file for MEGAN:");
        System.out.println("    mmparse -megan -input <filename> -taxonomy <directory>");
        System.out.println("");        
        System.out.println("Where:");
        System.out.println("    -control specifies the name of the control reads2Taxon file to parse");
        System.out.println("    -enriched specifies the name of the enriched reads2Taxon file to parse");
        System.out.println("    -lengths specifies the name of a contig length file");
        System.out.println("    -taxonomy specifies the directory containing NCBI taxonomy files");
        System.out.println("              (files needed are nodes.dmp and names.dmp)");
                
        System.out.println("");
    }
        
    public void processCommandLine(String[] args) {
        int i = 0;
        
        while (i < (args.length)) {
            if ((args[i].equalsIgnoreCase("-help")) || (args[i].equalsIgnoreCase("-h"))) {
                displayHelp();
                System.exit(0);
            } else if (args[i].equalsIgnoreCase("-count")) {
                runningCount = true;
                i++;
            } else if (args[i].equalsIgnoreCase("-megan")) {
                runningMegan = true;
                i++;
            } else if (args[i].equalsIgnoreCase("-control") || args[i].equalsIgnoreCase("-input")) {
                controlFilename = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-enriched")) {
                enrichedFilename = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-output")) {
                outputPrefix = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-taxonomy")) {
                taxonomyDirectory = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-lengths")) {
                lengthsFilename = args[i+1];
                i+=2;
            } else {                
                System.out.println("Unknown parameter: " + args[i]);
                System.exit(1);
            }           
        }
        
        if (args.length == 0) {
            displayHelp();
            System.exit(0);
        }
        
        if ((!runningCount) && (!runningMegan)) {
            System.out.println("Error: you must specify -count or -megan");
            System.exit(1);
        }
        
        if (controlFilename == null) {
            System.out.println("Error: you must specify a -control parameter");
            System.exit(1);
        }

        if ((runningCount) && (enrichedFilename == null)) {
            System.out.println("Error: you must specify a -enriched parameter");
            System.exit(1);
        }
        
        if ((runningCount) && (lengthsFilename == null)) {
            System.out.println("Error: you must specify a -lengths parameter");
            System.exit(1);
        }
        
        //if (outputPrefix == null) {
        //    System.out.println("Error: you must specify a -output parameter");
        //    System.exit(1);
        //}

        if (taxonomyDirectory == null) {
            System.out.println("Error: you must specify a -taxonomy parameter");
            System.exit(1);
        }
    }
    
    public boolean isRunningCount() {
        return runningCount;
    }
    
    public boolean isRunningMegan() {
        return runningMegan;
    }
    
    public String getControlFilename() {
        return controlFilename;
    }

    public String getEnrichedFilename() {
        return enrichedFilename;
    }
    
    public String getLengthsFilename() {
        return lengthsFilename;
    }
        
    public String getOutputPrefix() {
        return outputPrefix;
    }
    
    public String getTaxaSummaryOutputFilename() {
        return outputPrefix+"_summary.txt";
    }

    public String getPerReadOutputFilename() {
        return outputPrefix+"_perread.txt";
    }
    
    public void setTaxonomyDirectory(String dir) {
        taxonomyDirectory = dir;
    }
    
    public String getTaxonomyDirectory() {
        return taxonomyDirectory;
    }
    
    public String getMapFilename() {
        return mapFilename;
    }
                                
    public String getTimeString() {
        GregorianCalendar timeNow = new GregorianCalendar();
        String s = String.format("%d/%d/%d %02d:%02d:%02d",
                                 timeNow.get(Calendar.DAY_OF_MONTH),
                                 timeNow.get(Calendar.MONTH)+1,
                                 timeNow.get(Calendar.YEAR),
                                 timeNow.get(Calendar.HOUR_OF_DAY),
                                 timeNow.get(Calendar.MINUTE),
                                 timeNow.get(Calendar.SECOND));
        return s;
    }
        
    public void displayMemory() {
        System.out.println("Total memory: "+ (Runtime.getRuntime().totalMemory() / (1024*1024)) + " Mb");
        System.out.println(" Free memory: "+ (Runtime.getRuntime().freeMemory() / (1024*1024)) + " Mb");
        System.out.println(" Used memory: "+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024)) + " Mb");
    }    
            
    public boolean showWarnings() {
        return withWarnings;
    }
}

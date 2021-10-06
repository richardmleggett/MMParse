/*
 * Author: Richard M. Leggett
 * © Copyright 2021 Earlham Institute
 */

package leggett.mmparse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Taxonomy {
    private MMParseOptions options;
    private String nodesFilename;
    private Hashtable<Long, TaxonomyNode> nodesById = new Hashtable();
    private Hashtable<Long, String> nameById = new Hashtable();
    private Hashtable<String, Long> idByName = new Hashtable();
    private Hashtable<String, Long> accessionToTaxon = new Hashtable();
    private TaxonomyNode unclassifiedNode = new TaxonomyNode(0L);
    private long humanId;
    private long bacteriaId;
    private long lambdaId;
    private long vectorsId;
    private long ecoliId;
    private int maxRow = 0;
    private int maxColumn = 0;
    private int plotWidth = 2000;
    private int plotHeight = 2000;
    private int nTrees = 0;
    private boolean warningId = false;
    private int totalAssignedReads = 0;
    private int assignedThreshold = 2;
    private int otherCount = 0;
    private int totalCountCheck = 0;
    private Rectangle bounds;
    private Hashtable<String, Integer> warningTaxa = new Hashtable();
    private Hashtable<Long, Integer> warningTaxaId = new Hashtable();
    private Hashtable<String, Integer> warningRank = new Hashtable();
    private Hashtable<Long, TaxonomyRank> taxonIdToRank = new Hashtable();
    private Hashtable<String, TaxonomyRank> ranksTable = new Hashtable();
    private Hashtable<String, Long> mmPsuedospecies = new Hashtable();
    
    public Taxonomy(MMParseOptions o, String nf, String namesFilename) {
        nodesFilename = nf;
        options = o;
        try {
            System.out.println("Reading "+nodesFilename);
            BufferedReader br = new BufferedReader(new FileReader(nodesFilename));
            String line;                
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                long parentId = Integer.parseInt(fields[2]);
                String rank = fields[4];
                long id = -1;

                if (fields[0].startsWith("x")) {
                    mmPsuedospecies.put(fields[0], parentId);
                } else {
                    id = Long.parseLong(fields[0]);

                    TaxonomyNode n = nodesById.get(id);

                    if (n == null) {               
                        n = new TaxonomyNode(id);
                        nodesById.put(id, n);
                    }

                    n.setRank(this, rank);

                    if (parentId != id) {
                        n.setParent(parentId);   
                        linkParent(n, parentId);                
                    }
                }
            }
            br.close();
            
            System.out.println("Reading "+namesFilename);
            br = new BufferedReader(new FileReader(namesFilename));
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                if (!fields[0].startsWith("x")) {
                    if (fields[6].equals("scientific name")) {
                        long id = Long.parseLong(fields[0]);
                        nameById.put(id, fields[2]);        
                        idByName.put(fields[2], id);

                        if (fields[2].equals("Homo sapiens")) {
                            humanId = id;
                            //System.out.println("Got human ID");
                        }

                        if (fields[2].equals("Bacteria")) {
                            bacteriaId = id;
                            //System.out.println("Got bacteria ID");
                        }

                        if (fields[2].equals("Escherichia coli")) {
                            ecoliId = id;
                        }

                        if (fields[2].equals("Escherichia virus Lambda")) {
                            lambdaId = id;
                            //NanoOKReporter.messageIfRichard("Warning: need to check classification to lambda");
                        }

                        if (fields[2].equals("vectors")) {
                            vectorsId = id;
                            //NanoOKReporter.messageIfRichard("Warning: need to check classification to cloning vectors");
                        }                    
                    } else if (fields[6].equals("synonym")) {
                        long id = Long.parseLong(fields[0]);
                        idByName.put(fields[2], id);                    
                    }
                }
            }
            br.close();
            System.out.println("Processed "+nameById.size()+" nodes");
            
            //if ((lambdaId == 0) || (humanId == 0) || (vectorsId == 0) || (bacteriaId == 0) || (ecoliId == 0)) {
            //    System.out.println("Didn't get one of the Ids");
            //    System.exit(0);
            //}
                     
        } catch (Exception e) {
            System.out.println("Taxonomy exception");
            e.printStackTrace();
            System.exit(1);
        }
        
        nameById.put(0L, "unclassified");
        
        //System.out.println("Node 2 is "+getNameFromTaxonId(2L));
        //outputTaxonIdsFromNode(2L, "/Users/leggettr/Documents/Databases/taxonomy/bacteria_taxonids.txt");
        //System.out.println("Node 2157 is "+getNameFromTaxonId(2157L));
        //outputTaxonIdsFromNode(2157L, "/Users/leggettr/Documents/Databases/taxonomy/archea_taxonids.txt");
        //System.out.println("Node 10239 is "+getNameFromTaxonId(10239L));        
        //outputTaxonIdsFromNode(10239L, "/Users/leggettr/Documents/Databases/taxonomy/viruses_taxonids.txt");        
        //System.exit(1);
        
//        try {
//            FileOutputStream fos = new FileOutputStream("~/Desktop/acc2tax.ser");
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.writeObject(accessionToTaxon);
//            oos.close();
//        } catch (Exception e) {
//            System.out.println("Exception trying to write object:");
//            e.printStackTrace();
//        }
        
        //this.dumpTaxonomyFromName("Arabidopsis thaliana");
        //System.out.println("");
        //this.dumpTaxonomyFromName("Enterobacteriaceae bacterium strain FGI 57");
        
        //System.out.println("Enterobacter aerogenes = " + this.getTaxonIdFromName("Enterobacter aerogenes"));
        //System.out.println("" + );
    }
    
    public Long getMinimapPsuedospecies(String s) {
        long taxon = 0;
        if (mmPsuedospecies.contains(s)) {
            taxon = mmPsuedospecies.get(s);
        }
        
        return taxon;
    }
    
    public void discernRanks() {

        try {
            // Read first time
            System.out.println("Reading "+nodesFilename);
            BufferedReader br = new BufferedReader(new FileReader(nodesFilename));
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");                
                long id = Long.parseLong(fields[0]);
                long parentId = Integer.parseInt(fields[2]);
                String rank = fields[4];
                
                TaxonomyRank tr;
                if (ranksTable.containsKey(rank)) {
                    tr = ranksTable.get(rank);
                } else {
                    tr = new TaxonomyRank(rank);
                    ranksTable.put(rank, tr);
                    System.out.println("Put rank "+rank);
                }
                
                taxonIdToRank.put(id, tr);
 
                count++;                
                if ((count % 1000000) == 0) {
                    System.out.println("Read "+count+" entries");
                }
            }
            br.close();
                
            // Read second time
            System.out.println("Reading "+nodesFilename);
            br = new BufferedReader(new FileReader(nodesFilename));               
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");                
                long id = Long.parseLong(fields[0]);
                long parentId = Integer.parseInt(fields[2]);
                String rank = fields[4];
                
                if (!ranksTable.containsKey(rank)) {
                    System.out.println("Error: not seen rank before");
                    System.exit(1);
                }
                                
                TaxonomyNode n = getNodeFromTaxonId(parentId);                
                if (n != null) {
                    TaxonomyRank thisTr = ranksTable.get(rank);
                    TaxonomyRank parentTr = taxonIdToRank.get(n.getId());
                    if (rank.compareTo("no rank") != 0) {
                        if (thisTr != parentTr) {
                            thisTr.addParent(parentTr);
                            parentTr.addChild(thisTr);
                        }
                        
                    }
                } else {
                    System.out.println("No node for parent "+parentId);
                }
            }
            br.close();
            
            System.out.println("Done");
        } catch (Exception e) {
            System.out.println("Taxonomy exception");
            e.printStackTrace();
            System.exit(1);
        }
        
        Set<String> keys = ranksTable.keySet();
        for (String key: keys) {
            TaxonomyRank tr = ranksTable.get(key);
            System.out.println("\nParents of "+tr.getName()+" are");
            for (int i = 0; i<tr.getNumnberOfParents(); i++) {
                TaxonomyRank parent = tr.getParent(i);
                System.out.println("\t"+parent.getName());
            }
            System.out.println("\nChildren of "+tr.getName()+" are");
            for (int i = 0; i<tr.getNumberOfChildren(); i++) {
                TaxonomyRank child = tr.getChild(i);
                System.out.println("\t"+child.getName());
            }
        }        
        
        //System.out.println("\n\nTaxonomy is:\n");
        //TaxonomyRank tr = ranksTable.get("superkingdom");
        //printTaxonomyRank(tr, 0);      
    }
    
    public void printTaxonomyRank(TaxonomyRank tr, int level) {       
        if (tr != null) {
            for (int i=0; i<level; i++) {
                System.out.print(" ");
            }
            System.out.println(tr.getName() + "\t" + tr.getNumnberOfParents() + "\t" + tr.getNumberOfChildren());
            
            tr.markVisited();
            
            for (int j=0; j<tr.getNumberOfChildren(); j++) {
                if (tr.getChild(j).isVisited() == false) {
                    printTaxonomyRank(tr.getChild(j), level+1);
                }
            }
        }
    }
    
    public void outputTaxonIdsFromNode(long id, String filename) {
        System.out.println("Writing "+filename);
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename)); 
            outputNodeIdAndChildrenToFile(getNodeFromTaxonId(id), pw);
            pw.close();
        } catch (IOException e) {
            System.out.println("outputTaxonIdsFromNode exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void outputNodeIdAndChildrenToFile(TaxonomyNode n, PrintWriter pw) {
        pw.println(n.getId());
        ArrayList<TaxonomyNode> children = n.getChildren();
        for (int i=0; i<children.size(); i++) {
            TaxonomyNode c = children.get(i);
            outputNodeIdAndChildrenToFile(c, pw);
        }
    }

    public void displayMemory() {
        System.out.println("Total memory: "+ (Runtime.getRuntime().totalMemory() / (1024*1024)) + " Mb");
        System.out.println(" Free memory: "+ (Runtime.getRuntime().freeMemory() / (1024*1024)) + " Mb");
        System.out.println(" Used memory: "+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024)) + " Mb");
    }
        
    private void linkParent(TaxonomyNode n, long parentId) {
        TaxonomyNode parentNode = nodesById.get(parentId);
        if (parentNode == null) {
            parentNode = new TaxonomyNode(parentId);
            nodesById.put(parentId, parentNode);
        }
        parentNode.addChild(n);        
    }
    
    public String getNameFromTaxonId(Long id) {
        if (nameById.containsKey(id)) {
            return nameById.get(id);
        } else if (id == -2) {
            return "Not assigned";
        } else {
            return "Taxon ID "+id; 
        }
    }
    
    public Long getTaxonIdFromName(String name) {
        Long id = idByName.get(name);
        return id;
    }
    
    public TaxonomyNode getNodeFromTaxonId(Long id) {
        return nodesById.get(id);
    }
    
    public TaxonomyNode getNodeFromName(String name) {
        Long id = getTaxonIdFromName(name);
        TaxonomyNode n = null;
        if (id != null) {
            n = getNodeFromTaxonId(id);
        }
        return n;
    }
    
    public String getTaxonomyStringFromId(Long id) {
        String taxonString = "";

        if (id != null) {
            TaxonomyNode n = getNodeFromTaxonId(id);

            if (n != null) {
                while (n != null) {
                    String t = getNameFromTaxonId(n.getId());
                    if (t != null) {
                        if (taxonString.length() > 0) {
                            taxonString = t + "," + taxonString;
                        } else {
                            taxonString = t;
                        }
                    }
                    Long parentId = n.getParent();
                    if ((parentId != null) && (parentId != n.getId())) {
                        TaxonomyNode newNode = getNodeFromTaxonId(parentId);
                        
                        if (n == newNode) {
                            System.out.println("Er... something went wrong!");
                            System.out.println(n.getRankString());
                            System.exit(1);
                        }
                        
                        n = newNode;
                    } else {
                        n = null;
                    }
                }
            } else if (id == -2) {
                taxonString="Not assigned";
            } else {
                taxonString="Taxon ID "+Long.toString(id);
            }
        }
        
        return taxonString;
    }

    public ArrayList<Long> getTaxonIdPathFromId(Long id) {
        ArrayList<Long> nodes = new ArrayList<Long>();
        
        if (id == 0) {
            nodes.add(0L);
        } else if (id != null) {
            TaxonomyNode n = getNodeFromTaxonId(id);

            while (n != null) {
                nodes.add(n.getId());
                Long parentId = n.getParent();
                if ((parentId != null) && (parentId != n.getId())) {
                    n = getNodeFromTaxonId(parentId);
                } else {
                    n = null;
                }
            }
        }
        
        return nodes;
    }    
    
    public String getTaxonomyStringFromName(String s) {
        Long id = getTaxonIdFromName(s);
        return getTaxonomyStringFromId(id);
    }
    
    public String dumpTaxonomyFromId(Long id) {
        String taxonString = "";

        if (id != null) {
            TaxonomyNode n = getNodeFromTaxonId(id);

            if (n != null) {
                while (n != null) {
                    String t = getNameFromTaxonId(n.getId());
                    if (t != null) {
                        System.out.println(t + "\t" + n.getRankString());
                    }
                    Long parentId = n.getParent();
                    if ((parentId != null) && (parentId != n.getId())) {
                        n = getNodeFromTaxonId(n.getParent());
                    } else {
                        n = null;
                    }
                }
            }
        }
        
        return taxonString;
    }

    public String dumpTaxonomyFromName(String s) {
        Long id = getTaxonIdFromName(s);
        return dumpTaxonomyFromId(id);
    }
    
    private void countRead(Long id) {
        TaxonomyNode n = getNodeFromTaxonId(id);
        
        totalCountCheck++;
        
        if (n == null) {
            unclassifiedNode.incrementAssigned();
        } else {
            n.incrementAssigned();

            do {
                Long parent = n.getParent();
                n.incrementSummarised();

                if (id != parent) {
                    id = parent;
                } else {
                    id = null;                
                }

                if (id != null) {
                    n = getNodeFromTaxonId(id);
                }
            } while (id != null);
        }
    }
    
    public Long parseTaxonomyToId(String s) {
        Long id = null;
        String[] parts = s.split("(,|\\s)");       
        String species = s;
        String genus = s;
        String triplet = s;
        
        if (parts[0].equals("PREDICTED:") || (parts[0].equals("Uncultured")) || (parts[0].equals("Synthetic"))) {
            if (parts.length >= 4) {
                species = parts[1] + " " + parts[2];
                genus = parts[1];
                triplet = parts[1] + " " + parts[2] + " " + parts[3];
            }
        } else {
            if (parts.length >= 3) {
                species = parts[0] + " " + parts[1];
                genus = parts[0];
                triplet = parts[0] + " " + parts[1] + " " + parts[2];
            }
        }
              
        id = getTaxonIdFromName(species);
        if (id == null) {
            id = getTaxonIdFromName(genus);
            if (id == null) {
                id = getTaxonIdFromName(triplet);
                if (id == null) {
                    if (species.startsWith("Human")) {
                        id = humanId;
                    } else if (species.equals("Artificial cloning")) {
                        id = vectorsId;
                    } else if (species.startsWith("Cloning vectors") || (species.startsWith("Cloning vector"))) {
                        id = vectorsId;
                    } else if (species.equalsIgnoreCase("Unidentified bacterium")) {
                        id = bacteriaId;
                    } else if (s.contains("vector lambda") || (species.startsWith("Lambda genome"))) {
                        id = lambdaId;
                    } else if (s.startsWith("E.coli")) {
                        id = ecoliId;
                    } else {
                        //System.out.println("Couldn't parse "+s+" ("+species+")");
                        //unclassifiedNode.incrementAssigned();
                    }
                }
            }
        }       
        
        //System.out.println("Taxon "+s + " to id " + id);
        
        return id;
    }
    
    public void parseTaxonomyAndCount(String s) {
        System.out.println("ERROR: Shouldn't get to parseTaxonomyAndCount");
        System.exit(1);
        String[] parts = s.split("(,|\\s)");
        String species;
        String genus;
        String triplet;
        //System.out.println(species);
        
        if (parts[0].equals("PREDICTED:") || (parts[0].equals("Uncultured")) || (parts[0].equals("Synthetic"))) {
            species = parts[1] + " " + parts[2];
            genus = parts[1];
            triplet = parts[1] + " " + parts[2] + " " + parts[3];
        } else {
            species = parts[0] + " " + parts[1];
            genus = parts[0];
            triplet = parts[0] + " " + parts[1] + " " + parts[2];
        }
        
        // Note: Could look for 'plasmid' keyword 
        if (s.contains("plasmid")) {
            //System.out.println("Plasmid found");
        }
        
        Long id = getTaxonIdFromName(species);
        if (id != null) {
            countRead(id);            
        } else {
            id = getTaxonIdFromName(genus);
            if (id != null) {
                countRead(id);
            } else {
                id = getTaxonIdFromName(triplet);
                if (id != null) {
                    countRead(id);
                } else {                
                    if (species.startsWith("Human")) {
                        countRead(humanId);
                    } else if (species.equals("Artificial cloning")) {
                        id = vectorsId;
                    } else if (species.startsWith("Cloning vectors") || (species.startsWith("Cloning vector"))) {
                        id = vectorsId;
                    } else if (species.equalsIgnoreCase("Unidentified bacterium")) {
                        id = bacteriaId;
                    } else if (s.contains("vector lambda") || (species.startsWith("Lambda genome"))) {
                        countRead(lambdaId);
                    } else if (s.startsWith("E.coli")) {
                        countRead(ecoliId);
                    } else {
                        //System.out.println("Couldn't parse and count "+s+" ("+species+")");
                        unclassifiedNode.incrementAssigned();
                    }
                }
            }
        }
        
        //System.out.println("["+getTaxonomyStringFromName(species)+"]");
    }
    
//    public long findAncestor(LCAHitSet bhs, int maxToParse, boolean limitToSpecies) {
//        long ancestor = 0;
//        boolean debug = false;       
//        
//        if (bhs.getNumberOfAlignments() == 0) {
//            System.out.println("Er... no alignments in findAncestor...");
//            System.exit(1);
//        }
//        
//        int level = 1;
//        int maxLevel = 1000;
//        int loopTo = bhs.getNumberOfAlignments() < maxToParse ? bhs.getNumberOfAlignments():maxToParse;
//
//        if (debug) {
//            System.out.println("loopTo = "+loopTo);
//        }
//
//        for (int i=0; i<loopTo; i++) {
//            int taxonLevel = bhs.getAlignment(i).getTaxonLevel();
//
//            if (debug) {
//                System.out.print(i + " ");
//                System.out.print(bhs.getAlignment(i).getTaxonId() + " ");
//                System.out.print(this.getTaxonomyStringFromId(bhs.getAlignment(i).getTaxonId()));
//                System.out.println(" "+taxonLevel);
//            }
//
//            if (taxonLevel > 0) {
//                if (taxonLevel < maxLevel) {
//                    maxLevel = taxonLevel;
//                }
//                //ArrayList tip = bhs.getAlignment(i).getTaxonIdPath();
//                //for (int j=0; j<tip.size(); j++) {
//                //    if (j > 0) {
//                //        System.out.print(",");
//                //    }
//                //    System.out.print(tip.get(j));
//                //}
//                //System.out.println("");
//            }
//        }
//
//        if (debug) {
//            System.out.println("maxLevel = "+maxLevel);
//        }
//
//        boolean same = true;
//        boolean stop = false;
//        while ((same == true) && (level <= maxLevel) && (stop == false)) {
//            long common = -1;
//            for (int i=0; i<loopTo; i++) {
//                if (bhs.getAlignment(i).getTaxonLevel() > 0) { 
//                    if (common == -1) {
//                        common = bhs.getAlignment(i).getTaxonNode(level);
//                        if (debug) {
//                            System.out.println("common = "+common);
//                        }
//                    } else if (bhs.getAlignment(i).getTaxonNode(level) != common) {
//                        same = false;
//                    }
//                }
//            }
//
//            if (same == true) {
//                ancestor = common;
//                if (debug) {
//                    System.out.println("Match on " + this.getNameFromTaxonId(common));
//                }
//
//                if (limitToSpecies) {
//                    if (bhs.getNumberOfAlignments() > 0) {
//                        TaxonomyNode n = getNodeFromTaxonId(bhs.getAlignment(0).getTaxonNode(level));
//                        if (n != null) {
//                            if (n.getRank() == TaxonomyNode.RANK_SPECIES) {
//                                stop = true;
//                            }
//                        }
//                    } else {
//                        System.out.println("No alignments!");
//                        System.exit(1);
//                    }
//                }
//                
//                level++;                
//            }         
//        }
//
//        if (debug) {
//            System.out.println("Ancestor " + this.getNameFromTaxonId(ancestor));
//        }
//        
//        //System.out.println("Ancestor is "+ancestor);
//        
//        // Recode to MEGAN's "Not assigned";
//        if (ancestor == -1) {
//            ancestor = -2;
//        }
//        
//        return ancestor;
//    }
    
    private void walkNode(TaxonomyNode n, HashMap counts) {
        ArrayList<TaxonomyNode> children = n.getChildren();
        int childrenIncluded = 0;

        for (int i=0; i<children.size(); i++) {
            TaxonomyNode c = children.get(i);
            if (c.getSummarised() > 0) {
                childrenIncluded++;
                walkNode(c, counts);
            }
        }

        //if (childrenIncluded == 0) {
            if (n.getAssigned() >= assignedThreshold) {
                counts.put(getNameFromTaxonId(n.getId())+ " ("+n.getAssigned()+")", n.getAssigned());
                totalAssignedReads += n.getAssigned();
            } else {
                otherCount++;
            }
        //}
    }
    
    public int getTotalAssignedReads() {
        return totalAssignedReads;
    }
    
    public int registerTree() {
        return nTrees++;
    }
    
    public long getGenus(long taxon) {
        long genus = 0;
        long currentTaxon = taxon;
        boolean failed = false;

        while ((currentTaxon != 1) && (genus == 0) && (failed == false)) {
            TaxonomyNode n = this.getNodeFromTaxonId(currentTaxon);
            
            if (n != null) {
                if (n.getRank() == TaxonomyNode.RANK_GENUS) {
                    genus = currentTaxon;
                } else {
                    Long parent = n.getParent();
                    if (n == null) {
                        System.out.println("WARNING: getGenus " + currentTaxon + " doesn't have a node");
                        failed = true;
                    } else {
                        if (parent == null) {
                            System.out.println(currentTaxon + " doesn't have a parent");
                            System.exit(1);
                        } else {
                            n = getNodeFromTaxonId(parent);
                            currentTaxon = n.getId();
                        }
                    }
                }
            } else {
                failed = true;
            }
        }
        return genus;
    }
    
    public boolean isTaxonAncestor(long taxon, long ancestor) {
        boolean found = false;
        boolean failed = false;
        long currentTaxon = taxon;
        long previousTaxon = currentTaxon;
        
        if (taxon == 0) {
            System.out.println("Error: isTaxonAncestor called with 0");
            System.exit(1);
        }
        
        while ((currentTaxon != ancestor) && (currentTaxon != 1) && (failed==false)) {
            TaxonomyNode n = this.getNodeFromTaxonId(currentTaxon);
            
            if (n == null) {
                warnTaxaId(currentTaxon, "In isTaxonAncestor, taxon doesn't have a node defined: ");
                failed = true;
            } else {
                Long parent = n.getParent();

                if (parent == null) {
                    System.out.println(currentTaxon + " doesn't have a parent");
                    System.exit(1);
                } else {
                    n = getNodeFromTaxonId(parent);
                    previousTaxon = currentTaxon;
                    currentTaxon = n.getId();
                }
            }
        }
        
        if (currentTaxon == ancestor) { 
            found = true;
        }
        
        return found;
    }
    
    public void warnTaxa(String ascession) {
        if (options.showWarnings()) {
            if (!warningTaxa.containsKey(ascession)) {
                warningTaxa.put(ascession, 1);
                System.out.println("Warning: couldn't find taxon for "+ascession);
            }
        }
    }

    public void warnTaxaId(long taxaId, String warningText) {
        if (options.showWarnings()) {
            if (!warningTaxaId.containsKey(taxaId)) {
                warningTaxaId.put(taxaId, 1);
                System.out.println("Warning: " + warningText + " " + taxaId);
            }
        }
    }
    
    public void warnRank(String s) {
        if (options.showWarnings()) {
            if (!warningRank.containsKey(s)) {
                warningRank.put(s, 1);
                System.out.println("Warning: unknown rank "+s);
            }
        }
    }
}

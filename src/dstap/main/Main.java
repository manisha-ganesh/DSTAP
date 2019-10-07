/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dstap.main;

import dstap.main.partitioning.CustomAlgo;
import dstap.main.partitioning.SDDAlgorithm;
import dstap.main.partitioning.SpectralAlgo;
import dstap.network.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jblas.DoubleMatrix;
import Jama.Matrix;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author vp6258
 */
public class Main {
    /**
     * @param args folder location that contains all input files for the code
     */
    public static void main(String[] args) throws InterruptedException, FileNotFoundException{
        /**
         * How to run the code:
         * a) First argument says either "dtsap_heuristic", "dstap_algorithm", "partitioning", or "gp" (for solving full network using gradient projection)
         * b) If "heuristic" or "algorithm" then the next 4 inputs are: network name, subfolder inside /Inputs/ where inputs are located
         * , printVerbosityLevel and whether to run subnets in parallel or not
         * c) If "partitioning", then the inputs are: network name, type of algorithm, which for now includes SDDA (Spectral and others will be coded later),
         * and last, number of partitions to be generated. Running "partitioning" will generate a subfolder inside 
         * Inputs, which can be then used to run DSTAP as heuristic or others
         * d) If "gp", then the inputs are the folder name where the _net and _trips files are stored in BarGera format and the demand factor
          
         * 
         */
               
        String whichAlgorithm = args[1];
        whichAlgorithm = whichAlgorithm.toLowerCase();
        String netName = args[1];
        
        switch(whichAlgorithm){
            case "partitioning":
                String partitioningAlgo = args[2].toLowerCase();
                int noOfClusters = Integer.parseInt(args[3]);
                switch(partitioningAlgo){
                    case "sdda":
                        SDDAlgorithm algo = new SDDAlgorithm(noOfClusters, netName);
                        algo.readAndPrepareNetworkFiles();
                        algo.runAlgorithm();
                        
                        algo.generateSubNetNames(netName, noOfClusters);
                        algo.generateNetworkFileOutput(netName, noOfClusters);
                        algo.generateODTripsOutput(netName, noOfClusters);
                        algo.generatePartitionOutput();
                        algo.printPartitionStats();
                        break;
                    case "spectral":
                        System.out.println("\n\n=====================");
                        System.out.println("Note that current implementation of Spectral in Java is limited by the"
                                + "available heap space on the machine as it requires multiple n by n matrices where n is the number of nodes."
                                + ".\nEven the most efficient Java Matrix implementation fail to work with Networks like Austin/Texas. Use the"
                                + "python file instead.");
                        System.out.println("==================\n\n");
                        SpectralAlgo algo2 = new SpectralAlgo(noOfClusters, netName);
                        algo2.readAndPrepareNetworkFiles();
                        algo2.runAlgorithm();
                        algo2.generateSubNetNames(netName, noOfClusters);
                        algo2.generateNetworkFileOutput(netName, noOfClusters);
                        algo2.generateODTripsOutput(netName, noOfClusters);
                        algo2.generatePartitionOutput();
                        algo2.printPartitionStats();
                        break;
                    case "custom":
                        String fileName = args[4];
                        CustomAlgo algo3 = new CustomAlgo(noOfClusters, netName, fileName);
                        algo3.readAndPrepareNetworkFiles();
                        algo3.readPartitionFile();
                        algo3.generateSubNetNames(netName, noOfClusters);
                        algo3.generateNetworkFileOutput(netName, noOfClusters);
                        algo3.generateODTripsOutput(netName, noOfClusters);
                        algo3.generatePartitionOutput();
                        algo3.printPartitionStats();
                        break;
                    default:
                        System.out.println("For now only SDDA algorithm is supported. Ensure that the 3rd argument is SDDA only. Exiting");
                        System.exit(1);
                }
                
                break;
            case "dstap_heuristic":
                String folderName = "Networks/"+netName+"/";
                String subFolderName=args[2]; //subfolder inside INPUTS that contains the partition files...
                String printVerbosityLevel = args[3]; //LEAST, LOW, MEDIUM, HIGH
                Boolean runSubnetsInParallel = Boolean.parseBoolean(args[4]); //or false
                DSTAPHeuristic optimizer = new DSTAPHeuristic(printVerbosityLevel, 
                runSubnetsInParallel);
        
                // We assume inputs are in the format where all subnetwork trips completely
                //within the subnetwork are in separate file than the trips from one
                //subnetwork to the other. This simplifies creation of artificial links.
                optimizer.readInputsAndInitialize(folderName, subFolderName);
                optimizer.runOptimizer();
                break;
            case "dstap_algorithm":
                System.out.println("DSTAP as an algorithm is not fully coded yet. Sorry for the inconvenience. You may use DSTAP as a heuristic");
                System.exit(0);
                break;
            case "gp":
                Double demandFactor = Double.parseDouble(args[2]);
                CompleteNetwork net = new CompleteNetwork();
                net.readBarGeraLinkInput(netName);
                net.readBarGeraODFile(netName, demandFactor);
                net.printNetworkStatistics();
                net.solver(0.000001, 0.0001, 0);
                File dir = new File("Networks/"+netName+"/Outputs/CompleteNetOutput");
                // attempt to create the directory here
                boolean successful = dir.mkdir();
                if (!successful){
                    System.out.println("failed trying to create the directory");
                    System.exit(1);
                }
                net.writeFlowAndGapOutput("Networks/"+netName+"/Outputs/CompleteNetOutput/");
                break;
//                fullnet.readNetwork(netName);
//                fullnet.updateNodeLists();
//                fullnet.updateODLists();
//                fullnet.printStats();
            default:
                System.out.println("\nFirst argument must either be partitioning, heuristic, or algorithm. Please fix");
                System.exit(1);
                break;
        }
    }
}

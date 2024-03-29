package lsjss.LSJSS.fixRule;

import lsjss.problem.Instance;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainLotsizingFixRule {

    public static double run(Instance currentInstance) throws IOException, InvalidFormatException {

        //Random randomobj = new Random();
        //int seed = RandomSeed; // RandomSeed
        //randomobj.setSeed(seed);

        int numberProducts = currentInstance.NPRODUCTS;
        int numberMachines = currentInstance.NMACHINES;
        int numberPeriods = currentInstance.NPERIODS;
        double capacityTightness = currentInstance.capacityTightness;

        //Instance currentInstance = new Instance(numberProducts, numberMachines, numberPeriods, capacityTightness, RandomSeed);

        // load input data from excel
        //String FILE_PATH = "C:/Users/yanni/OneDrive/PhD Engineering and Management/02_projects_ongoing/" +
        //        "A cooperative coevolutionary hyper heuristic approach to solve lot-sizing and scheduling problems using genetic programming/" +
        //        "Experiment/Experiment1/rs"+RandomSeed+"/"+numberProducts+"x"+numberMachines+"x"+numberPeriods+"x"+capacityTightness+"/results.xlsx";

        //int[][] demands = ExcelReader.readInputDataArrayInt(FILE_PATH, "demands");
        int[][] demands = currentInstance.demands.clone();
        //int[][] routings = ExcelReader.readInputDataArrayInt(FILE_PATH, "routings");
        int[][] routings = currentInstance.routings.clone();
        //double[][] processingTime = ExcelReader.readInputDataArrayDouble(FILE_PATH, "processing_time");
        double[][] processingTime = currentInstance.processingTime.clone();
        int[][] inventory = new int[numberProducts][numberPeriods]; // inventory of each product at the end of each period
        //double[] capacity = ExcelReader.readInputDataListDouble(FILE_PATH, "period_length");
        double[] capacity = currentInstance.capacity.clone();

        //double[] capacity = new double[numberPeriods]; // capacity per period (valid for all machines)
        //double[] productionCosts = new double[numberProducts]; // production costs per product
        double[] productionCosts = currentInstance.productionCosts.clone();
        //int[] holdingCosts = new int[numberProducts]; // holding costs per product
        int[] holdingCosts = currentInstance.holdingCosts.clone();
        //int[] setupCosts = new int[numberProducts]; // setup costs per product
        int[] setupCosts = currentInstance.setupCosts.clone();
        //int[] setupTimes = new int[numberProducts]; // setup times per product
        int[] setupTimes = currentInstance.setupTimes.clone();
        double[] residualCapacity = new double[numberPeriods]; // residual capacity for each period
        //double[][] processingTime = new double[numberProducts][numberMachines]; // processing time per product per machine
        //int[][] demands = new int[numberProducts][numberPeriods]; // demands per product per period
        //int[][] routings = new int[numberProducts][numberMachines]; // full shop, therefore numberMachines is equal to number Operations

        /*
        // initialize demands
        for (int i =0; i<numberProducts; i++){
            for (int l=0; l<numberPeriods; l++){
                demands[i][l] = getRandomNumber(2,5,randomobj);
            }
        }

        // initialize routings
        for (int i =0; i<numberProducts; i++){
            List<Integer> routingsList = makeSequence(0,numberMachines-1);
            List<Integer> routingsListRandomSelection = getRandomElement(routingsList, numberMachines, randomobj);
            for (int r=0; r<numberMachines; r++){
                routings[i][r] = routingsListRandomSelection.get(r);
            }
        }

        // initialize processing times
        for (int i =0; i<numberProducts; i++){
            for (int r=0; r<numberMachines; r++){
                processingTime[i][r] = getRandomNumber(1,10,randomobj);
            }
        }

        // initialize capacities
        for (int l=0; l<numberPeriods; l++){
            double totalCapacity = 0.0;
            for (int i=0; i<numberProducts; i++){
                for (int r=0; r<numberMachines; r++) {
                    totalCapacity += (processingTime[i][r] * demands[i][l]);
                    if (demands[i][l] > 0){
                        totalCapacity+=(setupTimes[i]*numberMachines);
                    }
                }
            }
            capacity[l] = totalCapacity*capacityTightness;
        }

        // initialize production costs
        for (int i =0; i<numberProducts; i++){
            productionCosts[i] = 4;
        }

        // initialize holding costs
        for (int i =0; i<numberProducts; i++){
            holdingCosts[i] = 1;
        }

        // initialize setup times
        for (int i =0; i<numberProducts; i++){
            setupTimes[i] = 10; // for all operations equal 10
        }

        // initialize setup costs
        for (int i =0; i<numberProducts; i++) {
            //setupCosts[i] = getRandomNumber(5,100,randomobj);
            setupCosts[i] = 20;
        }



        // load static data
        int[] staticData = ExcelReader.readInputDataListInt(FILE_PATH, "static_parameters");

        // initialize setup times
        for (int i =0; i<numberProducts; i++){
            setupTimes[i] = staticData[0]; // for all operations equal 10
        }

        // initialize setup costs
        for (int i =0; i<numberProducts; i++){
            //setupCosts[i] = getRandomNumber(5,100,randomobj);
            setupCosts[i] = staticData[1];
        }

        // initialize production costs
        for (int i =0; i<numberProducts; i++){
            productionCosts[i] = staticData[2];
        }

        // initialize holding costs
        for (int i =0; i<numberProducts; i++){
            holdingCosts[i] = staticData[3];
        }

        */


        /*
        System.out.println("demands: " + Arrays.deepToString(demands));
        System.out.println("routings: " + Arrays.deepToString(routings));
        System.out.println("processing times: " + Arrays.deepToString(processingTime));
        System.out.println("capacities: " + Arrays.toString(capacity));
        System.out.println("setup times: " + Arrays.toString(setupTimes));
        System.out.println("setup costs: " + Arrays.toString(setupCosts));
        System.out.println("production costs: " + Arrays.toString(productionCosts));
        System.out.println("holding costs: " + Arrays.toString(holdingCosts));
        */
        //measuring elapsed time using System.nanoTime
        long startTime = System.nanoTime();
        // initialize production quantities array (lotsizes)
        int[][] productionQuantities = new int[numberProducts][numberPeriods];
        // set random seed
        int randomSeed = 100;

        // loop over all periods
        for (int l=0; l<numberPeriods; l++){
            //System.out.println("period: " + l);
            // get initial production quantities
            productionQuantities = LotSizingFunctionsFixRule.generateInitialSolution(numberProducts, l, demands, inventory, productionQuantities);
            // feasibility check -> get residual capacity based on the makespan of the schedule
            residualCapacity[l] = LotSizingFunctionsFixRule.feasibilityCheck(numberMachines, numberProducts,
                    productionQuantities, processingTime, routings, capacity[l], setupTimes, l,
                    false, false);
            //System.out.println("production quantities: " + Arrays.deepToString(productionQuantities));
            //System.out.println("residual capacity: " + residualCapacity);
            if (residualCapacity[l]>0) {
                // LotIncrease
                //System.out.println("Lot increasing procedure");
                productionQuantities = LotSizingFunctionsFixRule.increaseLotsize(numberMachines,numberProducts,productionQuantities,
                        processingTime,routings,capacity,residualCapacity,l,numberPeriods,demands,setupCosts,holdingCosts,inventory,
                        setupTimes);
                // update inventory
                for (int i=0; i<numberProducts;i++){
                    if (l>0){
                        inventory[i][l] = inventory[i][l-1] + productionQuantities[i][l] - demands[i][l];
                    }
                    else {
                        inventory[i][l] =  productionQuantities[i][l] - demands[i][l];
                    }
                }
                //System.out.println("inventorys: " + Arrays.deepToString(inventory));

            } else {
                // backtrack mechanism
                //System.out.println("Backtrack mechanism.");
                productionQuantities = LotSizingFunctionsFixRule.backtrackMechanism(numberMachines,numberProducts,productionQuantities,
                        processingTime,routings,capacity[l],residualCapacity,l,numberPeriods,demands,setupCosts,holdingCosts,inventory,
                        setupTimes);
                // update inventory
                for (int i=0; i<numberProducts;i++){
                    if (l>0){
                        inventory[i][l] = inventory[i][l-1] + productionQuantities[i][l] - demands[i][l];
                    }
                    else {
                        inventory[i][l] =  productionQuantities[i][l] - demands[i][l];
                    }
                }
                //System.out.println("inventorys: " + Arrays.deepToString(inventory));
            }
        }
        //System.out.println("demands: " + Arrays.deepToString(demands));
        //System.out.println("production quantities: " + Arrays.deepToString(productionQuantities));

        // get final results (total costs)
        // production costs, setup costs, and inventory holding costs
        int totalSetupCosts = 0;
        int totalHoldingCosts = 0;
        int totalProductionCosts = 0;
        for (int l=0; l<numberPeriods; l++){
            for (int i =0; i<numberProducts; i++){
                if (productionQuantities[i][l] > 0){
                    totalSetupCosts += setupCosts[i];
                    totalProductionCosts += productionQuantities[i][l] * productionCosts[i];
                }
                if (inventory[i][l] > 0){
                    totalHoldingCosts += inventory[i][l] * holdingCosts[i];
                }
            }
        }

        // loop over all periods to get the final residual capacities
        for (int l=0; l<numberPeriods; l++) {
            //System.out.println("period: " + l);
            // feasibility check -> get residual capacity based on the makespan of the schedule
            residualCapacity[l] = LotSizingFunctionsFixRule.feasibilityCheck(numberMachines, numberProducts,
                    productionQuantities, processingTime, routings, capacity[l], setupTimes, l,
                    true, false);
        }
        /*
        System.out.println("total production costs: " + totalProductionCosts);
        System.out.println("total setup costs: " + totalSetupCosts);
        System.out.println("total holding costs: " + totalHoldingCosts);
        System.out.println("sum of all costs: " + (totalHoldingCosts+totalSetupCosts+totalProductionCosts));
        System.out.println("residual capacities: " + Arrays.toString(residualCapacityList));
         */
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time in sec: "
                + elapsedTime*0.000000001);

        double totalCosts = totalHoldingCosts+totalSetupCosts+totalProductionCosts;

        return totalCosts;
    }
}


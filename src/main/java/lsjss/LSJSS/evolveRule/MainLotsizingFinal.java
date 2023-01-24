package lsjss.LSJSS.evolveRule;

import ec.EvolutionState;
import ec.gp.ADFStack;
import ec.gp.GPIndividual;
import lsjss.main.DoubleData;
import lsjss.problem.Instance;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainLotsizingFinal {
    public static int getRandomNumber(int min, int max, Random randomobj) {
        return (int) ((randomobj.nextDouble(1) * (max - min)) + min);
    }

    public static List<Integer> makeSequence(int begin, int end) {
        List<Integer> ret = new ArrayList<>(end - begin + 1);
        for (int i=begin; i<=end; i++) {
            ret.add(i);
        }
        return ret;
    }

    public static List<Integer> getRandomElement(List<Integer> list, int totalItems, Random randomobj)
    {
        // create a temporary list for storing
        // selected element
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {

            // take a random index between 0 to size
            // of given List
            int randomIndex = randomobj.nextInt(list.size());

            // add element in temporary list
            newList.add(list.get(randomIndex));

            // Remove selected element from original list
            list.remove(randomIndex);
        }
        return newList;
    }

    public static double run(GPIndividual JSSRule, GPIndividual LSSRule, DoubleData input, EvolutionState state,
                             int threadnum, ADFStack stack, ec.Problem problem, Instance currentInstance)
            throws IOException, InvalidFormatException {

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
        int[][] demands = currentInstance.demands;
        //int[][] routings = ExcelReader.readInputDataArrayInt(FILE_PATH, "routings");
        int[][] routings = currentInstance.routings;
        //double[][] processingTime = ExcelReader.readInputDataArrayDouble(FILE_PATH, "processing_time");
        double[][] processingTime = currentInstance.processingTime;
        int[][] inventory = new int[numberProducts][numberPeriods]; // inventory of each product at the end of each period
        //double[] capacity = ExcelReader.readInputDataListDouble(FILE_PATH, "period_length");
        double[] capacity = currentInstance.capacity;

        //double[] capacity = new double[numberPeriods]; // capacity per period (valid for all machines)
        //double[] productionCosts = new double[numberProducts]; // production costs per product
        double[] productionCosts = currentInstance.productionCosts;
        //int[] holdingCosts = new int[numberProducts]; // holding costs per product
        int[] holdingCosts = currentInstance.holdingCosts;
        //int[] setupCosts = new int[numberProducts]; // setup costs per product
        int[] setupCosts = currentInstance.setupCosts;
        //int[] setupTimes = new int[numberProducts]; // setup times per product
        int[] setupTimes = currentInstance.setupTimes;
        double[] residualCapacity = new double[numberPeriods]; // residual capacity for each period
        //double[][] processingTime = new double[numberProducts][numberMachines]; // processing time per product per machine
        //int[][] demands = new int[numberProducts][numberPeriods]; // demands per product per period
        //int[][] routings = new int[numberProducts][numberMachines]; // full shop, therefore numberMachines is equal to number Operations

        /*
        // initialize demands
        for (int i =0; i<numberProducts; i++){
            for (int l=0; l<numberPeriods; l++){
                demands[i][l] = getRandomNumber(3,8,randomobj);
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
        System.out.println("residual capa: " + Arrays.toString(residualCapacity));
        */
        //measuring elapsed time using System.nanoTime
        long startTime = System.nanoTime();
        // initialize production quantities array (lotsizes)
        int[][] productionQuantities = new int[numberProducts][numberPeriods];
        // set random seed
        int randomSeed = 100;

        //System.out.println("number Periods" + numberPeriods);
        // loop over all periods
        first:
        for (int l=0; l<numberPeriods; l++){
            //System.out.println("period: " + l);
            // get initial production quantities
            productionQuantities = LotSizingFunctionsFinal.generateInitialSolution(numberProducts, l, demands, inventory, productionQuantities);
            //System.out.println("finished the initital solution generation");
            //System.out.println("production quantities: " + Arrays.deepToString(productionQuantities));
            // feasibility check -> get residual capacity based on the makespan of the schedule
            residualCapacity[l] = LotSizingFunctionsFinal.feasibilityCheck(numberMachines, numberProducts,
                    productionQuantities, processingTime, routings, capacity[l], setupTimes, l,
                    JSSRule, input, state, threadnum, stack, problem, false, false);
            //System.out.println("residual capa: " + Arrays.toString(residualCapacity));
            //System.out.println("production quantities: " + Arrays.deepToString(productionQuantities));
            //System.out.println("residual capacity: " + residualCapacity[l]);
            if (residualCapacity[l]>0) {
                // LotIncrease
                //System.out.println("Lot increasing procedure");
                productionQuantities = LotSizingFunctionsFinal.increaseLotsize(numberMachines,numberProducts,productionQuantities,
                        processingTime,routings,capacity,residualCapacity,l,numberPeriods,demands,setupCosts,holdingCosts,inventory,
                        setupTimes, JSSRule, LSSRule, input, state, threadnum, stack, problem);
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
                productionQuantities = LotSizingFunctionsFinal.backtrackMechanism(numberMachines,numberProducts,productionQuantities,
                        processingTime,routings,capacity[l],residualCapacity,l,numberPeriods,demands,setupCosts,holdingCosts,inventory,
                        setupTimes, JSSRule, input, state, threadnum, stack, problem,currentInstance);
                // check if the backtrack mechanism leads to infeasible solution (infeasible heuristic)
                if (productionQuantities[0][0] == 9999){
                    break first;
                }
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
        double totalCosts = 0;
        if (productionQuantities[0][0] != 9999) {
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
                residualCapacity[l] = LotSizingFunctionsFinal.feasibilityCheck(numberMachines, numberProducts,
                        productionQuantities, processingTime, routings, capacity[l], setupTimes, l,
                        JSSRule, input, state, threadnum, stack, problem,false, false);
            }
        /*
        System.out.println("total production costs: " + totalProductionCosts);
        System.out.println("total setup costs: " + totalSetupCosts);
        System.out.println("total holding costs: " + totalHoldingCosts);
        System.out.println("sum of all costs: " + (totalHoldingCosts+totalSetupCosts+totalProductionCosts));
        System.out.println("residual capacities: " + Arrays.toString(residualCapacityList));
         */
            long elapsedTime = System.nanoTime() - startTime;
            //System.out.println("Total execution time in sec: "
            //        + elapsedTime*0.000000001);

            totalCosts = totalHoldingCosts+totalSetupCosts+totalProductionCosts;
        } else {
            totalCosts = 99999999;
        }
        if (totalCosts==99999999){
            System.out.println("SOLUTION INFEASIBLE");
            System.out.println("total costs: " +totalCosts);
        }
        return totalCosts;
    }
}


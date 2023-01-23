package lsjss.LSJSS.fixRule;

import lsjss.LSJSS.simElements.Job;
import lsjss.LSJSS.simElements.Operation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class LotSizingFunctionsFixRule {



    public static double protectedDiv(double numerator, double denominator) {
        double result = 0;
        if (denominator == 0){
            result = 1;
        } else {
            result = numerator/denominator;
        }
        return result;
    }

    public static double getMaxNumber(double[][] array) {
        double maxNumber = array[0][0];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] > maxNumber) {
                    maxNumber = array[i][j];
                }
            }
        }
        return maxNumber;
    }
    public static double getMaxNumberInt(int[] array) {
        double maxNumber = array[0];
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxNumber) {
                maxNumber = array[i];
            }
        }
        return maxNumber;
    }
    public static int[][] generateInitialSolution(int numberProducts, int currentPeriod, int[][] demands, int[][] inventory, int[][] productionQuantities)

    {
        // loop over all products
        if (currentPeriod==0){
            for (int i =0; i<numberProducts; i++){
                productionQuantities[i][currentPeriod] = demands[i][currentPeriod];
            }
        } else{
            for (int i =0; i<numberProducts; i++){
                productionQuantities[i][currentPeriod] = Math.max(demands[i][currentPeriod] - inventory[i][currentPeriod-1],0);
            }
        }


        return productionQuantities;
    }

    public static double feasibilityCheck(int numberMachines, int numberProducts, int[][] productionQuantities,
                                          double[][] processingTime, int[][]routings, double currentCapacity,
                                          int[] setupTimes, int currentPeriod, boolean printExcel, boolean verbose) {
        // processing time per product per machine

        if (verbose) {
            System.out.println("Number of Machines: " + numberMachines);
            //System.out.println("Number of Jobs: " + numberJobs);
        }

        // define arrays
        //double[] schedule = new double[numberJobs];
        double[] results = new double[3];
        ArrayList<Job> jobs = new ArrayList<Job>(); // here I store all Job Elements
        ArrayList<Job> jobsVar = new ArrayList<Job>(); // Create an ArrayList object for storing the currently active jobs
        ArrayList<Job> jobsFinished = new ArrayList<Job>();

        // initialize global clock and number of finished jobs
        double globalGlock = 0.0;
        int finishedJobs = 0;
        int releasedJobs = 0;

        // initialize Sum of processing times (SPT) and total remaining number of operations (TRNO)
        double SPT = 0.0;
        int TRNO = 0;

        // initialize the machines
        MachineFixRule[] machines = new MachineFixRule[numberMachines]; // here I store all Job Elements
        for (int i =0; i<numberMachines; i++){
            machines[i] = new MachineFixRule();
        }

        // Release jobs
        int numberJobs = 0; // number of jobs equal to number of product since we assume to produce each product once per period
        for (int i =0; i<numberProducts; i++){
            if (productionQuantities[i][currentPeriod] > 0){
                Job newJob = new Job(numberMachines, releasedJobs);
                newJob.releaseTime = 0;
                for (int j = 0; j < newJob.numberOperations; j++){
                    int currentMachine = routings[i][j];
                    newJob.operations[j] = new Operation(j, ((processingTime[i][currentMachine]*productionQuantities[i][currentPeriod])+setupTimes[i]), currentMachine, newJob.releaseTime);
                }
                newJob.DD = currentCapacity; // set due date as capacity of current period
                newJob.clock = 0;
                //System.out.println(this.clock);
                jobs.add(newJob);
                jobsVar.add(newJob);
                //System.out.println("Job " + firstJob.number + " has been released at time: " + firstJob.releaseTime);
                releasedJobs += 1;
                numberJobs += 1;
            }
        }



        // start the simulation loop
        while (finishedJobs<numberJobs)
        {
            // Step 1: check if there are operations to be released
            for (int i = 0; i < jobsVar.size(); i++) {
                if (jobsVar.get(i).releaseStatus && jobsVar.get(i).clock <= globalGlock) {
                    int operationToRelease = jobsVar.get(i).operationToRelease; // get the number of the next operation
                    int nextMachine = jobsVar.get(i).operations[operationToRelease].machine; // get machine number of the next operation
                    jobsVar.get(i).operations[operationToRelease].releaseTime = globalGlock; // set the release time of the operation equal to the last finished operation time of that job
                    machines[nextMachine].queueJob.add(jobsVar.get(i)); // assign job to the machine waiting queue
                    jobsVar.get(i).releaseStatus = false; // reset the release status of the job to false
                    if (verbose) {
                        System.out.println("Operation " + operationToRelease + " of Job " + jobsVar.get(i).number + " released at time " + globalGlock);
                    }
                }
            }
            // Step 2: check if there are jobs to be released (not needed here)

            // Step 3: check if there are jobs waiting to be processed on the machines
            for (int i = 0; i < numberMachines; i++) {
                if (machines[i].clock <= globalGlock && machines[i].queueJob.size() > 0){
                    Job executedJob = machines[i].execute(machines);
                    TRNO -= 1;
                    SPT -= machines[i].eventTime;
                    if (verbose) {
                        System.out.println("Operation " + (executedJob.currentOperation - 1) + " of Job " + executedJob.number + " has been executed on machine " + i + " and finished at time: " + executedJob.clock);
                    }
                    if (executedJob.finishStatus == true) {
                        finishedJobs += 1;
                        jobsVar.remove(executedJob);
                        jobsFinished.add(executedJob);
                    }
                }
            }
            // Step 4: update next event time
            double nextTime = Double.POSITIVE_INFINITY;  // initialize the next time
            for (int i = 0; i < numberMachines; i++) {
                if (machines[i].clock > globalGlock && machines[i].clock < nextTime) {
                    nextTime = machines[i].clock;
                }
            }
            for (int i = 0; i < jobsVar.size(); i++) {
                if (jobsVar.get(i).clock > globalGlock && jobsVar.get(i).clock < nextTime) {
                    nextTime = jobsVar.get(i).clock;
                }
            }
            globalGlock = nextTime; // update global clock

            // Step 5: Update all clocks
            for (int i = 0; i < numberMachines; i++) {
                machines[i].clock = Math.max(machines[i].clock, globalGlock);
            }
            for (int i = 0; i < jobsVar.size(); i++) {
                jobsVar.get(i).clock = Math.max(jobsVar.get(i).clock, globalGlock);
            }

        }

        //double totalFlowtime = 0.0;
        //double totalTardiness = 0.0;
        //double maxTardiness = 0.0;
        double makespan = 0.0;

        for (int i = 0; i < numberJobs; i++){
            //System.out.println("job number: "+jobsFinished.get(i).number);
            //System.out.println("release time: "+jobsFinished.get(i).releaseTime);
            //System.out.println("end time: "+jobsFinished.get(i).end);
            //System.out.println("due date: "+jobsFinished.get(i).DD);

            //totalFlowtime += (jobsFinished.get(i).end - jobsFinished.get(i).releaseTime);
            //double tardiness = Math.max(0.0, jobsFinished.get(i).end - jobsFinished.get(i).DD);
            //totalTardiness += tardiness;
            //maxTardiness = Math.max(tardiness, maxTardiness);
            makespan = Math.max(jobsFinished.get(i).end, makespan);
        }
        //double meanFlowtime = totalFlowtime/(numberJobs);
        //double meanTardiness = totalTardiness/(numberJobs);

        //results[0] = meanFlowtime;
        //results[1] = meanTardiness;
        //results[2] = maxTardiness;

        double residualCapacity = currentCapacity-makespan;

        if (printExcel) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("schedule");

            int rowCount = 0;

            Row titleRow = sheet.createRow(++rowCount);
            int columnCountTitleRow = 0;
            Cell cellTitleJobNumber = titleRow.createCell(++columnCountTitleRow);
            cellTitleJobNumber.setCellValue("job number");
            Cell cellTitleReleaseTimeJob = titleRow.createCell(++columnCountTitleRow);
            cellTitleReleaseTimeJob.setCellValue("release time job");
            Cell cellTitleDueDateJob = titleRow.createCell(++columnCountTitleRow);
            cellTitleDueDateJob.setCellValue("due date job");
            Cell cellTitleStartTimeJob = titleRow.createCell(++columnCountTitleRow);
            cellTitleStartTimeJob.setCellValue("start time job");
            Cell cellTitleEndTimeJob = titleRow.createCell(++columnCountTitleRow);
            cellTitleEndTimeJob.setCellValue("end time job");
            Cell cellTitleNumberOperationsJob = titleRow.createCell(++columnCountTitleRow);
            cellTitleNumberOperationsJob.setCellValue("number of operations");
            Cell cellTitleOperationsNumber = titleRow.createCell(++columnCountTitleRow);
            cellTitleOperationsNumber.setCellValue("operation number");
            Cell cellTitleReleaseTimeOperation = titleRow.createCell(++columnCountTitleRow);
            cellTitleReleaseTimeOperation.setCellValue("release time operation");
            Cell cellTitleProcessingTimeOperation = titleRow.createCell(++columnCountTitleRow);
            cellTitleProcessingTimeOperation.setCellValue("processing time");
            Cell cellTitleStartTimeOperation = titleRow.createCell(++columnCountTitleRow);
            cellTitleStartTimeOperation.setCellValue("start");
            Cell cellTitleEndTimeOperation = titleRow.createCell(++columnCountTitleRow);
            cellTitleEndTimeOperation.setCellValue("end");
            Cell cellTitleMachineOperation = titleRow.createCell(++columnCountTitleRow);
            cellTitleMachineOperation.setCellValue("machine");

            for (int i = 0; i < jobsFinished.size(); i++) {

                for (int j = 0; j < jobsFinished.get(i).numberOperations; j++) {
                    Row row = sheet.createRow(++rowCount);
                    int columnCount = 0;
                    Cell cellJobNumber = row.createCell(++columnCount);
                    cellJobNumber.setCellValue(jobsFinished.get(i).number);
                    Cell cellReleaseTimeJob = row.createCell(++columnCount);
                    cellReleaseTimeJob.setCellValue(jobsFinished.get(i).releaseTime);
                    Cell cellDueDateJob = row.createCell(++columnCount);
                    cellDueDateJob.setCellValue(jobsFinished.get(i).DD);
                    Cell cellStartTimeJob = row.createCell(++columnCount);
                    cellStartTimeJob.setCellValue(jobsFinished.get(i).start);
                    Cell cellEndTimeJob = row.createCell(++columnCount);
                    cellEndTimeJob.setCellValue(jobsFinished.get(i).end);
                    Cell cellNumberOperationsJob = row.createCell(++columnCount);
                    cellNumberOperationsJob.setCellValue(jobsFinished.get(i).numberOperations);
                    Cell cellOperationsNumber = row.createCell(++columnCount);
                    cellOperationsNumber.setCellValue(jobsFinished.get(i).operations[j].number);
                    Cell cellReleaseTimeOperation = row.createCell(++columnCount);
                    cellReleaseTimeOperation.setCellValue(jobsFinished.get(i).operations[j].releaseTime);
                    Cell cellProcessingTimeOperation = row.createCell(++columnCount);
                    cellProcessingTimeOperation.setCellValue(jobsFinished.get(i).operations[j].PT);
                    Cell cellStartTimeOperation = row.createCell(++columnCount);
                    cellStartTimeOperation.setCellValue(jobsFinished.get(i).operations[j].start);
                    Cell cellEndTimeOperation = row.createCell(++columnCount);
                    cellEndTimeOperation.setCellValue(jobsFinished.get(i).operations[j].end);
                    Cell cellMachineOperation = row.createCell(++columnCount);
                    cellMachineOperation.setCellValue(jobsFinished.get(i).operations[j].machine);
                }
            }

            try {
                //Write the workbook in file system
                FileOutputStream out = new FileOutputStream(new File("schedule_"+currentPeriod+".xlsx"));
                workbook.write(out);
                out.close();
                //System.out.println("schedule.xlsx written successfully on disk.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return residualCapacity;
    }

    public static int[][] increaseLotsize(int numberMachines, int numberProducts, int[][] productionQuantities,
                                          double[][] processingTime, int[][]routings, double[] capacity, double[] residualCapacity,
                                          int currentPeriod, int numberPeriods, int[][] demands, int[] setupCosts,
                                          int[] holdingCosts, int[][] inventory, int[] setupTimes){
        double[][] CSI = new double[numberProducts][numberPeriods];
        for (int l=currentPeriod+1; l<numberPeriods; l++){
            for (int i=0; i<numberProducts; i++){
                //System.out.println("product: " + i);
                if ((demands[i][l]-inventory[i][l-1])>0){   // not needed in our case since we assume demand for each product in every period
                    // calculate coverage period
                    int x = productionQuantities[i][currentPeriod];
                    int coveragePeriod = 1;
                    int y = currentPeriod;
                    while (x > 0){
                        x = x-demands[i][y];
                        if (y > currentPeriod){
                            coveragePeriod+=1;
                        }
                        y += 1;
                    }
                    // calculate CSI
                    // Example: Dixon-Silver
                    // calculate binary setup variable gamma
                    double currentBSV = 0;
                    if (productionQuantities[i][currentPeriod] > 0){currentBSV = 1;} else {currentBSV = 0;}
                    double currentSC = setupCosts[i];
                    double currentCF = residualCapacity[currentPeriod];
                    double currentNPRC = coveragePeriod;

                    double currentAHC = (demands[i][l] * holdingCosts[i]) * (l-currentPeriod);
                    double currentCP = currentPeriod;
                    double currentHCC = (productionQuantities[i][currentPeriod] - demands[i][currentPeriod]) * holdingCosts[i];
                    double currentNPRE = l - currentPeriod;
                    double currentADSHC = (currentBSV*setupCosts[i] - currentHCC);
                    double currentADSHCE = (setupCosts[i] - currentHCC - currentAHC);
                    double PDR = 0;
                    for(int m=currentPeriod+1; m<numberPeriods; m++){
                        PDR += demands[i][m];}
                    double APDR = PDR/(numberPeriods-currentPeriod);
                    double currentAPDR = APDR;
                    double CU = 0;
                    for(int m=0; m<numberMachines; m++){
                        CU += processingTime[i][m];}
                    double currentCU = CU;
                    double currentNI = numberProducts;
                    double currentLPH = numberPeriods;
                    double currentHC = holdingCosts[i];
                    double PD = 0;
                    for(int m=0; m<numberPeriods; m++){
                        PD += demands[i][m];}
                    double APD = PD/numberPeriods;
                    double currentAPD = APD;
                    double PDK = 0;
                    for(int m=0; m<numberPeriods; m++){
                        for(int n=0; n<numberProducts; n++){
                            PDK += demands[n][m];}
                    }
                    double APDK = PDK/(numberPeriods*numberProducts);
                    double currentAPDK = APDK;
                    double currentSHC = currentSC/currentHC;
                    double currentSHCN = currentSHC/currentAPD;
                    double currentC = capacity[currentPeriod];
                    double currentPLE = demands[i][l];
                    double PDAI = 0;
                    for(int m=currentPeriod+1; m<numberPeriods; m++){
                        for(int n=0; n<numberProducts; n++){
                            PDAI += demands[n][m];}
                    }
                    double APDAI = PDAI/((numberPeriods-currentPeriod)*numberProducts);
                    double currentAPDAI = APDAI;
                    double RCC = 0;
                    for(int m=currentPeriod+1; m<numberPeriods; m++){
                        RCC += residualCapacity[m];}
                    double currentRCC = RCC;
                    double currentCLS = productionQuantities[i][currentPeriod];
                    double currentTBO = Math.sqrt((2*currentSC)/(currentHC*currentAPD));

                    double eisenhut = (currentBSV*currentSC - currentHCC - currentAHC) / (Math.pow(currentNPRC,2) * currentPLE);
                    double dixonSilver = (protectedDiv((currentBSV*currentSC + currentHCC), currentNPRC) -
                            (protectedDiv((currentSC + currentHCC + currentAHC),currentNPRE))) / (currentCU*currentPLE);
                    double groff = ((2*currentBSV*currentSC) / (currentHC)) - (currentPLE * currentNPRC * currentNPRE); //check
                    double silverMeal = (protectedDiv((currentBSV*currentSC + currentHCC), currentNPRC)) -
                            ((currentSC + currentHCC + currentAHC)/currentNPRE);
                    double leastUnitCost = (protectedDiv((currentBSV*currentSC + currentHCC), currentCLS)) -
                            ((currentSC + currentHCC + currentAHC)/(currentCLS+currentPLE));
                    double leastTotalCost = currentADSHC-currentADSHCE;
                    double absoluteCost = currentBSV*currentSC-currentAHC;
                    double timeBetweenOrders = currentTBO;
                    double setupOverHoldingCost = currentSC/currentHC;
                    double correctedSetupOverHoldingCost = currentSC/currentHC * currentCU * currentAPD;
                    double expectedAverageCost = (currentSC/currentTBO) + ((currentHC * currentAPD * currentTBO) / 2);
                    double expectedCostSavings = ((currentTBO-1)*currentSC) - ((currentTBO*(currentTBO-1)*currentHC*currentAPD) / 2);
                    double correctedExpectedSavings = ((currentTBO-1)*currentSC) - ((currentTBO*(currentTBO-1)*currentHC*currentAPD) / (2*currentCU*currentAPD));
                    CSI[i][l] = dixonSilver;
                }
            }
        }
        //System.out.println("CSI: " + Arrays.deepToString(CSI));
        while (getMaxNumber(CSI) > 0.0){
            int productToExtend = 9999;
            double maxCSI = 0.0;
            int periodToExtend = 9999;
            for (int l=currentPeriod+1; l<numberPeriods; l++) {
                for (int i = 0; i < numberProducts; i++) {
                    if (CSI[i][l] > maxCSI) {
                        productToExtend = i;
                        periodToExtend = l;
                        maxCSI = CSI[i][l];
                    }
                }
            }
            //System.out.println("csi: " + Arrays.deepToString(CSI));
            //System.out.println("max csi: " + maxCSI);
            //System.out.println("item to extend: " + productToExtend);
            //System.out.println("period to extend: " + periodToExtend);

            // Update the production quantity by extending the max CSI demand
            productionQuantities[productToExtend][currentPeriod] += demands[productToExtend][periodToExtend];
            residualCapacity[currentPeriod] = LotSizingFunctionsFixRule.feasibilityCheck(numberMachines, numberProducts,
                    productionQuantities, processingTime, routings, capacity[currentPeriod], setupTimes, currentPeriod,
                    false, false);
            //System.out.println("production quantities: " + Arrays.deepToString(productionQuantities));
            //System.out.println("residual capacity: " + residualCapacity);
            // If lot extension feasible, extend lot and check next lot, otherwise discard and check next lot
            if (residualCapacity[currentPeriod]<0){
                //System.out.println("lot extension not feasible");
                productionQuantities[productToExtend][currentPeriod] -= demands[productToExtend][periodToExtend];
                //System.out.println("not feasible");
            } else {
                //System.out.println("feasible");
            }
            // discard the tested lot extension for further tests
            CSI[productToExtend][periodToExtend] = 0.0;
        }


        return productionQuantities;
    }

    public static int[][] backtrackMechanism(int numberMachines, int numberProducts, int[][] productionQuantities,
                                             double[][] processingTime, int[][]routings, double currentCapacity,
                                             double[] residualCapacity,
                                             int currentPeriod, int numberPeriods, int[][] demands, int[] setupCosts,
                                             int[] holdingCosts, int[][] inventory, int[] setupTimes){
        // set production quantities of current period to zero
        for (int i=0; i<numberProducts; i++){
            productionQuantities[i][currentPeriod] = 0;
        }
        // get the demand of the product with the highest inventory holding costs per unit and assign it to the period
        int[] productsToCheck = new int[numberProducts];
        for (int i=0; i<numberProducts; i++){
            if (currentPeriod>0){
                if ((demands[i][currentPeriod]-inventory[i][currentPeriod-1]) > 0){
                    productsToCheck[i] = demands[i][currentPeriod]-inventory[i][currentPeriod-1];
                } else {productsToCheck[i] = 0;}
            } else {
                if (demands[i][currentPeriod] > 0){
                    productsToCheck[i] = demands[i][currentPeriod];
                } else {productsToCheck[i] = 0;}
            }
        }
        // loop until all products with net demand have been checked
        // get product to assign next (with the highest inventory holding costs per unit)
        for (int x=0; x<numberProducts; x++){
            //System.out.println("product: " + x);
            int productToAssign = 999;
            int highestHoldingCosts = 0;
            for (int i=0; i<numberProducts; i++){
                if (productsToCheck[i] > 0){
                    if (holdingCosts[i] > highestHoldingCosts){
                        productToAssign = i;
                        highestHoldingCosts = holdingCosts[i];
                    }
                }
            }
            // assign quantities of the selected product
            productionQuantities[productToAssign][currentPeriod] = productsToCheck[productToAssign];
            // feasibility check
            residualCapacity[currentPeriod] = LotSizingFunctionsFixRule.feasibilityCheck(numberMachines, numberProducts,
                    productionQuantities, processingTime, routings, currentCapacity, setupTimes, currentPeriod,
                    false, false);
            // If lot assignment feasible, assign lot and check next lot, otherwise discard and check next lot
            if (residualCapacity[currentPeriod]<0){
                //System.out.println("not feasible");
                productionQuantities[productToAssign][currentPeriod] -= demands[productToAssign][currentPeriod];
            } else {productsToCheck[productToAssign] = 0; System.out.println("feasible");}
        }
        // all remaining quantities in productsToCheck array must be backwarded
        //System.out.println("remaining production quantities (excess): " + Arrays.toString(productsToCheck));
        for (int i=0; i<numberProducts; i++){
            if (productsToCheck[i] > 0){
                for (int l=currentPeriod; l<=0; l--){
                    int periodToCheck = l-1;
                    // backward mechanism missing
                    if (periodToCheck<0){
                        System.out.println("no feasible solution can be obtained for the selected LSSP");
                    } else {

                    }
                }
            }
        }

        return productionQuantities;
    }

}

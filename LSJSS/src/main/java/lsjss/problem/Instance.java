package lsjss.problem;

import ec.util.Parameter;
import lsjss.util.ExcelReader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.Arrays;

public class Instance {
    public int[][] demands;
    public int[][] routings;
    public double[][] processingTime;
    public double[] capacity;
    public double[] productionCosts;
    public int[] holdingCosts;
    public int[] setupCosts;
    public int[] setupTimes;
    public int NPRODUCTS;
    public int NMACHINES;
    public int NPERIODS;
    public double capacityTightness;
    public int RANDOMSEED;
    public double optimum;
    public double[] solution;

    public Object clone()
    {
        try { return super.clone(); }
        catch (CloneNotSupportedException e)
        { throw new InternalError(); } // never happens
    }

    public void setup(int nProducts, int nMachines, int nPeriods, int RandomSeed, String pathInstances) throws IOException, InvalidFormatException {

        NPRODUCTS = nProducts;
        NMACHINES = nMachines;
        NPERIODS = nPeriods;
        RANDOMSEED = RandomSeed;
        //this.capacityTightness = CapacityTightness;

        // load input data from excel
        String FILE_PATH = pathInstances+"rs"+RandomSeed+"/"+nProducts+"x"+nMachines+"x"+nPeriods+"/results.xlsx";

        demands = ExcelReader.readInputDataArrayInt(FILE_PATH, "demands");
        routings = ExcelReader.readInputDataArrayInt(FILE_PATH, "routings");
        processingTime = ExcelReader.readInputDataArrayDouble(FILE_PATH, "processing_time");
        capacity = ExcelReader.readInputDataListDouble(FILE_PATH, "period_length");
        solution = ExcelReader.readInputDataListDouble(FILE_PATH, "solution");
        optimum = solution[0];
        // load static data
        int[] staticData = ExcelReader.readInputDataListInt(FILE_PATH, "static_parameters");
        setupTimes = new int[nProducts];
        setupCosts = new int[nProducts];
        productionCosts = new double[nProducts];
        holdingCosts = new int[nProducts];
        // initialize setup times
        for (int i =0; i<nProducts; i++){
            setupTimes[i] = staticData[0]; // for all operations equal 10
        }

        // initialize setup costs
        for (int i =0; i<nProducts; i++){
            //setupCosts[i] = getRandomNumber(5,100,randomobj);
            setupCosts[i] = staticData[1];
        }

        // initialize production costs
        for (int i =0; i<nProducts; i++){
            productionCosts[i] = staticData[2];
        }

        // initialize holding costs
        for (int i =0; i<nProducts; i++){
            holdingCosts[i] = staticData[3];
        }
    }

    public void print(){
        System.out.println("random seed: " + RANDOMSEED);
        System.out.println("demands: " + Arrays.deepToString(demands));
        System.out.println("routings: " + Arrays.deepToString(routings));
        System.out.println("processing times: " + Arrays.deepToString(processingTime));
        System.out.println("capacities: " + Arrays.toString(capacity));
        System.out.println("setup times: " + Arrays.toString(setupTimes));
        System.out.println("setup costs: " + Arrays.toString(setupCosts));
        System.out.println("production costs: " + Arrays.toString(productionCosts));
        System.out.println("holding costs: " + Arrays.toString(holdingCosts));
        System.out.println("Solution: "+optimum);
    }
}

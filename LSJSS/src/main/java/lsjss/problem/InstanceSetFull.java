package lsjss.problem;

import ec.EvolutionState;
import ec.util.Parameter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

public class InstanceSetFull {
    public static final String P_SIZE = "size";
    public static final String P_INSTANCESIZE = "instance-size";
    public static final String P_RANDOMSEED = "randomseed";
    public static final String P_PRODUCTS = "products-list";
    public static final String P_MACHINES = "machines-list";
    public static final String P_PERIODS = "periods-list";
    public static final String P_PATH_INSTANCES = "path-instances";

    public int size;
    public int instanceSize;
    public int[] nProducts;
    public int[] nMachines;
    public int[] nPeriods;
    public double capacityTightness;
    public int[] randomSeeds;
    public Instance[] instances;
    public String pathInstances;

    public Object clone()
    {
        try { return super.clone(); }
        catch (CloneNotSupportedException e)
        { throw new InternalError(); } // never happens
    }

    public void setup(final EvolutionState state, final Parameter base, String set) throws IOException, InvalidFormatException {
        Parameter p;

        CoevolutionState CCstate = (CoevolutionState) state;

        // get size of training/validation set
        p = base.push(P_SIZE);
        size = state.parameters.getInt(p,null,1);
        if (size==0) // uh oh
            state.output.fatal("training/validation set size must be >0.\n",base.push(P_SIZE));

        // get size of instance set
        p = base.push(P_INSTANCESIZE);
        instanceSize = state.parameters.getInt(p,null,1);
        if (instanceSize==0) // uh oh
            state.output.fatal("instance set size must be >0.\n",base.push(P_INSTANCESIZE));

        // get path name from parameters
        p = new Parameter(P_PATH_INSTANCES);
        pathInstances = state.parameters.getString(p, null);


        // get number of Products from the parameters
        //p = new Parameter(P_PRODUCTS);
        //if (state.parameters.exists(p, null))
        //{
        //    nProducts = state.parameters.getInt(p, null, 1);  // 0 would be UDEFINED
        //    if (nProducts <= 0)
        //        state.output.fatal("If defined, the number of Products must be an integer >= 1.", p, null);
        //}

        // get number of Machines from the parameters
        //p = new Parameter(P_MACHINES);
        //if (state.parameters.exists(p, null))
        //{
        //    nMachines = state.parameters.getInt(p, null, 1);  // 0 would be UDEFINED
        //    if (nMachines <= 0)
        //        state.output.fatal("If defined, the number of Machines must be an integer >= 1.", p, null);
        //}

        // get number of Periods from the parameters
        //p = new Parameter(P_PERIODS);
        //if (state.parameters.exists(p, null))
        //{
        //    nPeriods = state.parameters.getInt(p, null, 1);  // 0 would be UDEFINED
        //    if (nPeriods <= 0)
        //        state.output.fatal("If defined, the number of Periods must be an integer >= 1.", p, null);
        //}

        randomSeeds = new int[size];
        nProducts = new int[instanceSize];
        nMachines = new int[instanceSize];
        nPeriods = new int[instanceSize];
            // assign random seeds to the instances
            if (set == "training") {
                CCstate.trainingSet.instances = new Instance[size*instanceSize];
                for (int y=0; y<instanceSize; y++) {
                    p = base.push(P_PRODUCTS).push("" + y);
                    nProducts[y] = state.parameters.getInt(p, null, 0);
                    p = base.push(P_MACHINES).push("" + y);
                    nMachines[y] = state.parameters.getInt(p, null, 0);
                    p = base.push(P_PERIODS).push("" + y);
                    nPeriods[y] = state.parameters.getInt(p, null, 0);
                    for (int x = 0; x < size; x++) {
                        p = base.push(P_RANDOMSEED).push("" + x);
                        randomSeeds[x] = state.parameters.getInt(p, null, 0);
                        CCstate.trainingSet.instances[x+(size*y)] = new Instance();
                        CCstate.trainingSet.instances[x+(size*y)].setup(nProducts[y], nMachines[y], nPeriods[y], randomSeeds[x], pathInstances);
                    }
                }
            }
            if (set == "validation") {
                CCstate.validationSet.instances = new Instance[size*instanceSize];
                for (int y=0; y<instanceSize; y++) {
                    p = base.push(P_PRODUCTS).push("" + y);
                    nProducts[y] = state.parameters.getInt(p, null, 0);
                    p = base.push(P_MACHINES).push("" + y);
                    nMachines[y] = state.parameters.getInt(p, null, 0);
                    p = base.push(P_PERIODS).push("" + y);
                    nPeriods[y] = state.parameters.getInt(p, null, 0);
                    for (int x = 0; x < size; x++) {
                        p = base.push(P_RANDOMSEED).push("" + x);
                        randomSeeds[x] = state.parameters.getInt(p, null, 0);
                        CCstate.validationSet.instances[x+(size*y)] = new Instance();
                        CCstate.validationSet.instances[x+(size*y)].setup(nProducts[y], nMachines[y], nPeriods[y], randomSeeds[x], pathInstances);
                    }
                }
            }
            if (set != "training" && set != "validation"){
                CCstate.output.fatal("Set must be either training or validation", p, null);
            }
        }
}

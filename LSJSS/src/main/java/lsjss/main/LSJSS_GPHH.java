package lsjss.main;

import ec.*;
import ec.coevolve.*;
import ec.gp.*;
import ec.simple.*;
import lsjss.LSJSS.evolveRule.MainLotsizingFinal;
import lsjss.problem.CoevolutionState;
import lsjss.problem.CoevolutionStatistics;
import lsjss.problem.Instance;
import lsjss.rule.GPProblemCOEV;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.util.ArrayList;


public class LSJSS_GPHH extends GPProblemCOEV implements GroupedProblemForm {
    public LSJSS_GPHH() {
    }
    // here are all the terminals defined; for each terminal we need a separate class within in functions package
    // also in each class we need to define the datatype, for each datatype another class must be created
    // for example DoubleData for double datatype
    // JSS
    public double currentPT; // processing time of an operation
    public double currentRT; // release time of a job
    public double currentRPT; // remaining processing time of a job
    public double currentRNO; // remaining number of uncompleted operations of a job
    public double currentDD; // due date of a job
    public double currentRTO; // ready time of an operation
    public double currentPTN; // processing time of the next operation
    public double currentSL; // slack of the job
    public double currentWT; // waiting time of the operation
    public double currentAPTQ; // average processing time of operations of waiting jobs
    public double currentNJQ; // number of waiting jobs in the queue
    public double currentWINQ; // work in next queue
    public double currentCT; // current time

    // LSS
    public double currentAHC; // 1 additional holding costs #done
    public double currentBSV; // 2 binary setup variable #done
    public double currentSC; // 3 Setup cost of item i #done
    public double currentCF; // 4 Capacity gap in period τ #done
    public double currentNPRC; // 5 Number of periods requirements the current lot of item i satisfies #done
    public double currentHCC; // 7 Holding costs incurred with the current lot of item i #done
    public double currentNPRE; // 8 Number of periods requirements the extended lot of item i satisfies #done
    public double currentCU; // 9 Capacity usage to produce one unit of item i #done
    public double currentADSHC; // 10 Absolute deviation between setup and holding costs incurred #done
    public double currentAPDR; // 11 Average period demand of item i considering remaining periods from τ + 1 to H #done
    public double currentNI; // 12 Number of items #done
    public double currentLPH; // 13 Length of the planning horizon #done
    public double currentCP; // 14 Current Period #done
    public double currentHC; // 15 Holding cost per unit of item i #done
    public double currentAPD; // 16 Average period demand of item i #done
    public double currentAPDK; // 17 Average period demand over all items #done
    public double currentTBO; // 18 iTme between two orders based on the average period demand #done
    public double currentSHC; // 19 Setup over holding costs #done
    public double currentSHCN; // 20 SHC normalized by the average demand #done
    public double currentC; // 24 Capacity in period τ #done
    public double currentCLS; // 25 current lot size #done
    public double currentPLE; // 26 Potential lot extension of item i (i.e. demand in period τ + T #done
    public double currentRCC; // 30 Remaining cumulated capacity from period τ + 1 to H #done
    public double currentADSHCE; // 32 Absolute deviation between setup and holding costs incurred with the extended lot of item i #done


    public double currentATCC; // 6 Average total costs per period incurred with the current lot #notdone
    public double currentFP; // 31 First period where cumulated demand exceeds cumulated capacity #notdone
    public double currentATCE; // 27 Average total costs per period incurred with the extended lot #notdone
    public double currentAPDAI; // 28 Average period demand of all items considering remaining periods from τ + 1 to H #notdone
    public double currentRC; // 29 Remaining capacity in period τ #notdone
    public double currentEC; // 21 Expected costs per period assuming an order cycle of T BO periods #notdone
    public double currentES; // 22 Expected costs savings when combining demands over T BO periods #notdone
    public double currentESC; // 23 ESi normalized by the capacity requirements, ESCi = ESi/ki  ̄d #notone

    public void print() {
        System.out.println("print function running");
    }

    public void preprocessPopulation(EvolutionState state, Population pop,
                                     boolean[] prepareForAssessment, boolean countVictoriesOnly) {
        for(int i = 0; i < pop.subpops.length; i++)
            if (prepareForAssessment[i])
                for(int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    SimpleFitness fit = (SimpleFitness)(pop.subpops[i].individuals[j].fitness);
                    fit.trials = new ArrayList();
                }
    }

    public void postprocessPopulation(EvolutionState state, Population pop,
                                      boolean[] assessFitness, boolean countVictoriesOnly) {
        for(int i = 0; i < pop.subpops.length; i++)
            if (assessFitness[i])
                for(int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    SimpleFitness fit = (SimpleFitness) (pop.subpops[i].individuals[j].fitness);
                    // Let's set the fitness to the average of the trials
                    int len = fit.trials.size();
                    double sum = 0;
                    for(int l = 0; l < len; l++)
                        sum += (Double) (fit.trials.get(l));
                    // Alternatively if we were interested in how *many* times we won rather
                    // than by how much, we might set the fitness to, say, how often the doubleValue()
                    // was positive.
                    fit.setFitness(state, sum/len, false);
                    pop.subpops[i].individuals[j].evaluated = true;
                }
    }

    // here is where I must use the previously defined terminals for the fitness evaluation
    // here is where the simulation must take place
    public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly,
                         int[] subpops, int threadnum) {
        //if (!ind[0].evaluated)  // don't bother reevaluating
        DoubleData input = (DoubleData) (this.input);
        // this I don't need, it's just here for to calculate a comparison value for the fitness evaluation
        // in my case currentPT, currentCT, ... are within the simulation where they are used for the
        // priority calculation
        // I need to do all the following steps within the simulation loop
        //currentX = state.random[threadnum].nextDouble();
        //currentY = state.random[threadnum].nextDouble();

        // this I don't need, it's just here for to compare and evaluate the difference
        //expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;

        double gap = 0.0;
        double result = 0.0;
        CoevolutionState nState = (CoevolutionState)state;
        //System.out.println("training set size:" + nState.trainingSet.instances.length);
        for (int r=0; r<nState.trainingSet.instances.length; r++) {
            Instance currentInstance = nState.trainingSet.instances[r];
            //nState.trainingSet.instances[r].print();
            //System.out.println("instance: " + r);
            //currentInstance.print();
            try {
                result = MainLotsizingFinal.run((GPIndividual) ind[0], (GPIndividual) ind[1], input, state, threadnum, stack, this,
                        currentInstance);
                gap += (result/currentInstance.optimum)-1;
                //System.out.println("result: "+result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        }
        double averageCosts = result/nState.trainingSet.instances.length;
        double averageGap = gap/nState.trainingSet.instances.length;
        //System.out.println("total costs: " + averageCosts);

        //((GPIndividual)ind).trees[0].child.eval(
        //       state,threadnum,input,stack,((GPIndividual)ind),this);

        //result = Math.abs(expectedResult - input.x);
        //if (result <= 0.01) hits++;

        // the fitness better be KozaFitness!
        //KozaFitness f = ((KozaFitness)ind[0].fitness);
        //f.setFitness(state, result);
        //f.hits = hits;
        if( updateFitness[0] )
        {
            SimpleFitness fit1 = ((SimpleFitness)(ind[0].fitness));
            fit1.trials.add(-averageGap);
            // set the fitness because if we're doing Single Elimination Tournament, the tournament
            // needs to know who won this time around.  Don't bother declaring the ideal here.
            fit1.setFitness(state, -averageGap, false);
        }

        if( updateFitness[1] )
        {
            SimpleFitness fit2 = ((SimpleFitness)(ind[1].fitness));
            fit2.trials.add(-averageGap);
            // set the fitness because if we're doing Single Elimination Tournament, the tournament
            // needs to know who won this time around.
            fit2.setFitness(state, -averageGap, false);
        }
        //SimpleFitness f = ((SimpleFitness) ind[0].fitness);
        //f.setFitness(state, result, false);
        //ind[0].evaluated = true;
        //SimpleFitness f1 = ((SimpleFitness) ind[1].fitness);
        //f1.setFitness(state, result, false);
        //ind[1].evaluated = true;
    }

    public void validate(EvolutionState state, Individual[] ind, int threadnum) {
        //if (!ind[0].evaluated)  // don't bother reevaluating
        DoubleData input = (DoubleData) (this.input);
        // this I don't need, it's just here for to calculate a comparison value for the fitness evaluation
        // in my case currentPT, currentCT, ... are within the simulation where they are used for the
        // priority calculation
        // I need to do all the following steps within the simulation loop
        //currentX = state.random[threadnum].nextDouble();
        //currentY = state.random[threadnum].nextDouble();

        // this I don't need, it's just here for to compare and evaluate the difference
        //expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;

        double result = 0.0;
        double gap = 0.0;
        double currentgap= 0.0;
        CoevolutionState nState = (CoevolutionState)state;
        //System.out.println("validation set size:" + nState.validationSet.instances.length);
        for (int r=0; r<(nState.validationSet.instances.length); r++) {
            Instance currentInstance = nState.validationSet.instances[r];
            //System.out.println("instance: " + r);
            //currentInstance.print();
            try {
                result = MainLotsizingFinal.run((GPIndividual) ind[0], (GPIndividual) ind[1], input, state, threadnum, stack, this,
                        currentInstance);
                currentgap = (result/currentInstance.optimum)-1;
                gap += (result/currentInstance.optimum)-1;
                CoevolutionStatistics nStateStatistics = (CoevolutionStatistics) nState.statistics;
                for (int s=0; s<state.population.subpops.length; s++){
                    nStateStatistics.fitnessesPerGenerationValidation[nState.generation][s][r] = currentgap;
                    GPIndividual GPind = (GPIndividual) ind[s];
                    nStateStatistics.individualsPerGenerationValidation[nState.generation][s][r] = GPind.trees[0].child.makeCTree(true,
                            GPind.trees[0].printTerminalsAsVariablesInC, GPind.trees[0].printTwoArgumentNonterminalsAsOperatorsInC);
                }
                //System.out.println("result: "+result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InvalidFormatException e) {
                throw new RuntimeException(e);
            }
        }
        double averageCosts = result/nState.validationSet.instances.length;
        double averageGap = gap/nState.validationSet.instances.length;

        //System.out.println("total costs validation: " + averageCosts);
        CoevolutionStatistics nStateStatistics = (CoevolutionStatistics) nState.statistics;
        for (int s=0; s<state.population.subpops.length; s++){
            nStateStatistics.fitnessesPerGenerationValidation[nState.generation][s][nState.validationSet.instances.length] = averageGap;
            GPIndividual GPind = (GPIndividual) ind[s];
            nStateStatistics.individualsPerGenerationValidation[nState.generation][s][nState.validationSet.instances.length] = GPind.trees[0].child.makeCTree(true,
                    GPind.trees[0].printTerminalsAsVariablesInC, GPind.trees[0].printTwoArgumentNonterminalsAsOperatorsInC);
            //System.out.println(GPind.trees[0].child.makeCTree(true,
            //        GPind.trees[0].printTerminalsAsVariablesInC, GPind.trees[0].printTwoArgumentNonterminalsAsOperatorsInC));
        }
    }
}





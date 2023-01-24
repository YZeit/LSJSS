package lsjss.LSJSS.simElements;

import ec.EvolutionState;
import ec.gp.ADFStack;
import ec.gp.GPIndividual;
import lsjss.main.DoubleData;
import lsjss.main.LSJSS_GPHH;

import java.util.ArrayList;

public class Machine {
    // Instance Variables
    public ArrayList<Job> queueJob = new ArrayList<Job>();
    public int queueOperation;
    public int numberOperationsInSystem;
    public double clock;
    public double eventTime;
    public int currentJobFinish;

    // Constructor Declaration of Class
    public Machine() {
        this.queueOperation = 0;
        this.clock = 0.0;
        this.eventTime = 0.0;
        this.numberOperationsInSystem = 0;
        this.currentJobFinish = 0;

    }

    public Job execute(Machine[] machines, GPIndividual IndividualToEvaluate, DoubleData input, EvolutionState state,
                       int threadnum, ADFStack stack, ec.Problem problem) {
        // Step 1: update priority / goal: find the job to be processed next
        double[] PT_list = new double[queueJob.size()];
        double PTQueue = 0.0;
        for (int i = 0; i<queueJob.size(); i++){
            PT_list[i] = queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;
            PTQueue += queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;
        }
        ((LSJSS_GPHH)problem).currentAPTQ = PTQueue/queueJob.size();   // Average Processing time queue
        ((LSJSS_GPHH)problem).currentNJQ = queueJob.size();          // Number of Jobs waiting in the queue

        double minPriority = Double.POSITIVE_INFINITY;  // initialize min Priority (starting with inf)
        Job nextJob = queueJob.get(0);                  // initialize next job (simply take the first job in the queue)

        for (int i = 0; i<queueJob.size(); i++){
            ((LSJSS_GPHH)problem).currentPT = queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;   // Processing time of the operation
            ((LSJSS_GPHH)problem).currentRT = queueJob.get(i).releaseTime;                                    // release time of the job
            ((LSJSS_GPHH)problem).currentRPT = queueJob.get(i).RPT;                                           // remaining processing time of the job
            ((LSJSS_GPHH)problem).currentRNO = queueJob.get(i).RNO;                                              // remaining number of operations of the job
            ((LSJSS_GPHH)problem).currentDD = queueJob.get(i).DD;                                             // due date of the job
            ((LSJSS_GPHH)problem).currentRTO = queueJob.get(i).operations[queueJob.get(i).currentOperation].releaseTime;  // release time of the operation
            ((LSJSS_GPHH)problem).currentCT = this.clock;                                                     // current time
            ((LSJSS_GPHH)problem).currentSL = ((LSJSS_GPHH)problem).currentDD-(((LSJSS_GPHH)problem).currentCT+((LSJSS_GPHH)problem).currentRPT);                                                    // Slack of the job
            ((LSJSS_GPHH)problem).currentWT = Math.max(0, ((LSJSS_GPHH)problem).currentCT-((LSJSS_GPHH)problem).currentRTO);                                            // waiting time of the operation in the queue
            int nextOperation = queueJob.get(i).nextOperation;                          // next Operation (needed for the WINQ)
            ((LSJSS_GPHH)problem).currentPTN = nextOperation < queueJob.get(i).numberOperations ? queueJob.get(i).operations[queueJob.get(i).nextOperation].PT : 0; // Processing time next operation
            ((LSJSS_GPHH)problem).currentWINQ = 0.0;
            if (queueJob.get(i).nextOperation < queueJob.get(i).numberOperations) {
                for (int k = 0; k < machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.size(); k++) {
                    ((LSJSS_GPHH)problem).currentWINQ += machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.get(k).operations[machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.get(k).currentOperation].PT;
                }
            }

            IndividualToEvaluate.trees[0].child.eval(state,threadnum,input,stack,IndividualToEvaluate,problem);  // calculate priority; here is where the GP must evolve new scheduling rules
            double priority = input.x;

            if (priority < minPriority) {
                nextJob = queueJob.get(i);
                minPriority = priority;
            }
        }
        // Step 2: Update job and operation data
        nextJob.operations[nextJob.currentOperation].start = this.clock;                                                    // start time of the operation
        nextJob.operations[nextJob.currentOperation].end = this.clock + nextJob.operations[nextJob.currentOperation].PT;    // end time of the operation
        nextJob.clock += nextJob.operations[nextJob.currentOperation].PT;                                                   // clock of the job
        nextJob.RPT -= nextJob.operations[nextJob.currentOperation].PT;                                                     // remaining processing time of job
        nextJob.RNO -= 1;                                                                                                   // remaining number of operations
        if (nextJob.operations[nextJob.currentOperation].number == 0) {                                                     // if it is the first operation of that job
            nextJob.start = this.clock;                                                                                     // then update start time of the job
        }
        if (nextJob.operations[nextJob.currentOperation].number == nextJob.numberOperations-1) {                            // if the operation is the last of that job
            nextJob.end = this.clock + nextJob.operations[nextJob.currentOperation].PT;                                     // then update end time of that job
            nextJob.finishStatus = true;                                                                                    // and set finish status to true
        }

        this.clock += nextJob.operations[nextJob.currentOperation].PT;                                                  // clock of the machine

        this.eventTime = nextJob.operations[nextJob.currentOperation].PT;                                                   // event time
        nextJob.operationToRelease += 1;                                                                                    // operations to release
        nextJob.currentOperation += 1;                                                                                      // current operation
        nextJob.nextOperation += 1;                                                                                         // next operation
        nextJob.releaseStatus = true;                                                                                       // release status

        // Step 3: remove operation from the queue
        queueJob.remove(nextJob);


        return nextJob;
    }
}

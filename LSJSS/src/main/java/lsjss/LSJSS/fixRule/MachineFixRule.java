package lsjss.LSJSS.fixRule;

import lsjss.LSJSS.simElements.Job;

import java.util.ArrayList;

public class MachineFixRule {
    // Instance Variables
    ArrayList<Job> queueJob = new ArrayList<Job>();
    int queueOperation;
    int numberOperationsInSystem;
    double clock;
    double eventTime;
    int currentJobFinish;

    // Constructor Declaration of Class
    public MachineFixRule() {
        this.queueOperation = 0;
        this.clock = 0.0;
        this.eventTime = 0.0;
        this.numberOperationsInSystem = 0;
        this.currentJobFinish = 0;

    }

    public Job execute(MachineFixRule[] machines) {
        // Step 1: update priority / goal: find the job to be processed next
        double[] PT_list = new double[queueJob.size()];
        double PTQueue = 0.0;
        for (int i = 0; i<queueJob.size(); i++){
            PT_list[i] = queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;
            PTQueue += queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;
        }
        double currentAPTQ = PTQueue/queueJob.size();   // Average Processing time queue
        double currentNJQ = queueJob.size();          // Number of Jobs waiting in the queue

        double minPriority = Double.POSITIVE_INFINITY;  // initialize min Priority (starting with inf)
        Job nextJob = queueJob.get(0);                  // initialize next job (simply take the first job in the queue)

        for (int i = 0; i<queueJob.size(); i++){
            double currentPT = queueJob.get(i).operations[queueJob.get(i).currentOperation].PT;   // Processing time of the operation
            double currentRT = queueJob.get(i).releaseTime;                                    // release time of the job
            double currentRPT = queueJob.get(i).RPT;                                           // remaining processing time of the job
            double currentRNO = queueJob.get(i).RNO;                                              // remaining number of operations of the job
            double currentDD = queueJob.get(i).DD;                                             // due date of the job
            double currentRTO = queueJob.get(i).operations[queueJob.get(i).currentOperation].releaseTime;  // release time of the operation
            double currentCT = this.clock;                                                     // current time
            double currentSL = currentDD-(currentCT+currentRPT);                                                    // Slack of the job
            double currentWT = Math.max(0, currentCT-currentRTO);                                            // waiting time of the operation in the queue
            int nextOperation = queueJob.get(i).nextOperation;                          // next Operation (needed for the WINQ)
            double currentPTN = nextOperation < queueJob.get(i).numberOperations ? queueJob.get(i).operations[queueJob.get(i).nextOperation].PT : 0; // Processing time next operation
            double currentWINQ = 0.0;
            if (queueJob.get(i).nextOperation < queueJob.get(i).numberOperations) {
                for (int k = 0; k < machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.size(); k++) {
                    currentWINQ += machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.get(k).operations[machines[queueJob.get(i).operations[queueJob.get(i).nextOperation].machine].queueJob.get(k).currentOperation].PT;
                }
            }
            double priority = currentPT;
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

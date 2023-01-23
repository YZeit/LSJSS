package lsjss.LSJSS.simElements;

public class Job {
    // Instance Variables
    public double start;
    public double end;
    public double clock;
    public boolean releaseStatus;
    public boolean finishStatus;
    public int numberOperations;
    public Operation[] operations;
    public double RPT;
    public int RNO;
    public double DD;
    public int operationToRelease;
    public int nextOperation;
    public int currentOperation;
    public double eventTime;
    public int number;
    public double releaseTime;

    // Constructor Declaration of Class
    public Job(int numberOperations, int jobNumber) {
        this.start = 0.0;
        this.end = 0.0;
        this.clock = 0.0;
        this.releaseStatus = true;
        this.finishStatus = false;
        this.numberOperations = numberOperations;
        this.operations = new Operation[numberOperations];
        this.RPT = 0.0;
        this.RNO = 0;
        this.DD = 0.0;
        this.operationToRelease = 0;
        this.currentOperation = 0;
        this.nextOperation = 1;
        this.eventTime = 0.0;
        this.number = jobNumber;
        this.releaseTime = 0.0;
    }
}

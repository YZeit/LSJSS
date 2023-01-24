package lsjss.LSJSS.simElements;

public class Operation {
    // Instance Variables
    public double start;
    public double end;
    public double clock;
    public int machine;
    public double PT;
    public int number;
    public double releaseTime;
    public double priority;


    // Constructor Declaration of Class
    public Operation(int number, double PT, int machine, double releaseTime) {
        this.number = number;
        this.start = 0.0;
        this.end = 0.0;
        this.clock = 0.0;
        this.PT = PT;
        this.machine = machine;
        this.releaseTime = releaseTime;
        this.priority = 0.0;
    }
}

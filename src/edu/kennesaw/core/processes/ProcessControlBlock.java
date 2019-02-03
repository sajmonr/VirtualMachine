package edu.kennesaw.core.processes;

public class ProcessControlBlock {
    //Job identifiers
    public int jobId;
    public int textSize;
    public int priority;
    //Memory control
    public int diskAddress;
    public int inputBufferSize;
    public int outputBufferSize;
    public int temporaryBufferSize;
    public int[] pageTable;
    //Job control
    public int programCounter;
    //Cpu properties
    final public int[] registers = new int[16];
    //Timing
    public int burstTime;
    public int arrivalTime;
    public int timeQuantum;
}

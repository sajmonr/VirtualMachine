package edu.kennesaw.core.execution;

import edu.kennesaw.core.execution.scheduling.LongTermScheduler;
import edu.kennesaw.core.execution.scheduling.policies.FifoPolicy;
import edu.kennesaw.core.memory.*;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.utils.NumberUtils;

import java.io.FileNotFoundException;

public class OsDriver {

    public enum ExecutionResult{OK, Fail}

    //Fields
    private final Loader _loader;
    private final ProcessQueue _jobQueue;
    private final ProcessQueue _readyQueue;
    private final Memory _disk;
    private final PagedMemory _ram;

    private final LongTermScheduler _jobScheduler;

    public OsDriver(int cpus, int ramSize, int diskSize) throws FileNotFoundException, MemoryInitializationException {

        //Setup memories
        _disk = new SimpleMemory(diskSize);
        _ram = new PagedMemory(ramSize, 64);
        //Setup queues
        _jobQueue = new ProcessQueue(new FifoPolicy());
        _readyQueue = new ProcessQueue(new FifoPolicy());

        _loader = new Loader("C:\\Users\\asimo\\Desktop\\program-file.txt", _jobQueue);

        _jobScheduler = new LongTermScheduler(_ram, _disk, _jobQueue, _readyQueue);

    }

    public ExecutionResult powerOn(){
        //Load jobs to disk
        try{
            _loader.loadJobs(_disk);
        }catch(Exception e){
            System.out.println(e.getMessage());

            return ExecutionResult.Fail;
        }

        boolean exit = false;

        while(!exit){
            _jobScheduler.schedule();
        }

        return ExecutionResult.OK;
    }

}

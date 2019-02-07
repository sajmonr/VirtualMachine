package edu.kennesaw.core.execution;

import edu.kennesaw.core.execution.CentralProcessingUnit.Cpu;
import edu.kennesaw.core.execution.scheduling.LongTermScheduler;
import edu.kennesaw.core.execution.scheduling.ShortTermScheduler;
import edu.kennesaw.core.execution.scheduling.policies.FifoPolicy;
import edu.kennesaw.core.memory.*;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.utils.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;

public class OsDriver {

    public enum ExecutionResult{OK, FAIL}
    //Fields
    private final Loader _loader;
    private final ProcessQueue _jobQueue;
    private final ProcessQueue _readyQueue;
    private final Memory _disk;
    private final PagedMemory _ram;
    private final MemoryManagementUnit _ramMmu;
    private final LongTermScheduler _jobScheduler;
    private final ShortTermScheduler _cpuScheduler;
    private final Cpu[] _cpus;
    private final List<Thread> _cpuThreads;

    public OsDriver(int cpus, int ramSize, int diskSize) throws FileNotFoundException, MemoryInitializationException {
        //Start the system stopwatch
        Stopwatch.start();
        //Setup memories
        _disk = new SimpleMemory(diskSize);
        _ram = new PagedMemory(ramSize, 64);
        _ramMmu = new MemoryManagementUnit(_ram);
        //Setup queues
        _jobQueue = new ProcessQueue(new FifoPolicy());
        _readyQueue = new ProcessQueue(new FifoPolicy());

        //Change path to reflect your environment.
        String programFilePath = System.getProperty("user.dir") + "/src/edu/kennesaw/program-file.txt";
        _loader = new Loader(programFilePath, _jobQueue, _disk);

        //Setup CPUs
        _cpus = new Cpu[cpus];
        _cpuThreads = new ArrayList<>();
        spawnCpus();

        _jobScheduler = new LongTermScheduler(_ram, _disk, _jobQueue, _readyQueue);
        _cpuScheduler = new ShortTermScheduler(_readyQueue, _cpus);
    }

    public ExecutionResult powerOn(){
        //Load jobs to disk or fail
        try{
            _loader.loadJobs();
        }catch(Exception e){
            System.out.println("System failed to start.");
            System.out.println(e.getMessage());

            return ExecutionResult.FAIL;
        }
        //Power on the CPUs
        startCpus();

        try{
            //This is the main scheduling loop of the driver.
            do{
                if(!_jobQueue.isEmpty() && _readyQueue.isEmpty())
                    _jobScheduler.schedule();

                _cpuScheduler.schedule();

                //Sleep the thread to stabilize the system.
                //Maybe there is a better way, but for now it is OK.
                Thread.sleep(10);
                //The loop will terminate if all CPUs finish their jobs
                //and there are no jobs waiting to be executed.
            }while(cpusBusy() || !_jobQueue.isEmpty() || !_readyQueue.isEmpty());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return ExecutionResult.FAIL;
        }

        //Try to gracefully terminate CPUs
        try {
            System.out.println("Going for CPU shutdown.");
            stopCpus();
        }catch(Exception e){
            System.out.println("CPU termination failed.");
            return ExecutionResult.FAIL;
        }
        return ExecutionResult.OK;
    }

    private void spawnCpus(){
        for(int i = 0; i < _cpus.length; i++)
            _cpus[i] = new Cpu(i, _ramMmu);
    }
    private void stopCpus() throws Exception{
        for(int i = 0; i < _cpus.length; i++){
            _cpus[i].exit();
            //Wait for all the CPU threads to terminate.
            _cpuThreads.get(i).join();
        }
        //Release all references to the CPU threads.
        _cpuThreads.clear();
    }
    private void startCpus(){
        for(Cpu cpu : _cpus){
            Thread t = new Thread(cpu);
            t.start();
            _cpuThreads.add(t);
        }
    }
    private boolean cpusBusy(){
        for(Cpu cpu : _cpus){
            if(cpu.isBusy())
                return true;
        }
        return false;
    }
}

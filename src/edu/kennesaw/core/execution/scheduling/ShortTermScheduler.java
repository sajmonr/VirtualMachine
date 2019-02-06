package edu.kennesaw.core.execution.scheduling;

import edu.kennesaw.core.execution.CentralProcessingUnit.Cpu;
import edu.kennesaw.core.execution.CentralProcessingUnit.InvalidRegisterAccessException;
import edu.kennesaw.core.execution.Dispatcher;
import edu.kennesaw.core.execution.scheduling.policies.Scheduler;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.processes.ProcessState;

public class ShortTermScheduler implements Scheduler {

    //Fields
    private final ProcessQueue _readyQueue;
    private final Cpu[] _cpus;
    private final Dispatcher _dispatcher;

    public ShortTermScheduler(ProcessQueue readyQueue, Cpu[] cpus){
        _dispatcher = new Dispatcher();
        _readyQueue = readyQueue;
        _cpus = cpus;
    }

    @Override
    public void schedule() throws FailedToScheduleException {
        if(_readyQueue.isEmpty())
            return;

        Cpu cpu = getAvailableCpu(_readyQueue.getSchedulingPolicy().isPreemptive());

        if(cpu != null){
            ProcessControlBlock oldPcb;
            ProcessControlBlock newPcb = _readyQueue.getSchedulingPolicy().select(_readyQueue.getProcesses());
            _readyQueue.remove(newPcb);

            try{
                oldPcb = _dispatcher.switchContext(cpu, newPcb);
            }catch(InvalidRegisterAccessException ira){
                throw new FailedToScheduleException(ira);
            }


            if(oldPcb != null && oldPcb.state != ProcessState.TERMINATED){
                oldPcb.state = ProcessState.READY;
                _readyQueue.add(oldPcb);
            }

            newPcb.state = ProcessState.RUNNING;
        }
    }

    private Cpu getAvailableCpu(boolean preemptive){
        //First try to find cpu that is not busy
        for(Cpu cpu : _cpus){
            if(!cpu.isBusy())
                return cpu;
        }
        //If all cpus are busy & preemtive is set then terminate a cpu
        if(preemptive){
            for(Cpu cpu : _cpus){
                if(cpu.pwait())
                    return cpu;
            }
        }

        return null;
    }

}

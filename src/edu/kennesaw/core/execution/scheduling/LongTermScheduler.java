package edu.kennesaw.core.execution.scheduling;

import edu.kennesaw.core.execution.scheduling.policies.Scheduler;
import edu.kennesaw.core.memory.AllocatableMemory;
import edu.kennesaw.core.memory.Memory;
import edu.kennesaw.core.memory.MemoryOverflowException;
import edu.kennesaw.core.memory.PagedMemory;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessQueue;
import edu.kennesaw.core.utils.Config;

public class LongTermScheduler implements Scheduler {

    //Fields
    private final PagedMemory _primaryMemory;
    private final Memory _secondaryMemory;
    private final ProcessQueue _jobQueue;
    private final ProcessQueue _readyQueue;

    public LongTermScheduler(PagedMemory primaryMemory, Memory secondaryMemory, ProcessQueue jobQueue, ProcessQueue readyQueue) {
        _primaryMemory = primaryMemory;
        _secondaryMemory = secondaryMemory;
        _jobQueue = jobQueue;
        _readyQueue = readyQueue;
    }

    @Override
    public void schedule() {
        ProcessControlBlock pcb = _jobQueue.getSchedulingPolicy().select(_jobQueue.getProcesses());

        _jobQueue.remove(pcb);

        int processSize = (pcb.textSize + pcb.inputBufferSize + pcb.outputBufferSize + pcb.temporaryBufferSize) * Config.WORD_SIZE;

        try{
            int[] pageTable = _primaryMemory.allocate(processSize);
            byte[] job = _secondaryMemory.read(pcb.diskAddress, processSize);

            loadToPrimaryMemory(pageTable, job);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void loadToPrimaryMemory(int[] pageTable, byte[] job) throws MemoryOverflowException {
        int size = job.length;
        int pageSize = _primaryMemory.getPageSize();
        int wordsNeeded;

        for(int i = 0; i < pageTable.length; i++){
            byte[] chunk = new byte[pageSize];
            wordsNeeded = size - (pageSize * i) >= pageSize ? pageSize : size - (pageSize * i);

            System.arraycopy(job, pageSize * i, chunk, 0, wordsNeeded);

            _primaryMemory.write(pageTable[i] * pageSize, chunk);
        }

    }

}

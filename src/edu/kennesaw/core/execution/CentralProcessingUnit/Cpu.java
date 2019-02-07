package edu.kennesaw.core.execution.CentralProcessingUnit;

import edu.kennesaw.core.memory.MemoryManagementUnit;
import edu.kennesaw.core.memory.PagedMemory;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.utils.Config;
import edu.kennesaw.core.utils.Instruction;

public class Cpu implements Runnable{
    //Fields
    private final int _id;
    private boolean _exitFlag = false;
    private boolean _waitFlag = false;
    private ProcessControlBlock _process;
    private int[] _registers;
    private int _programCounter;
    private final MemoryManagementUnit _ram;

    //Getters & setters
    public int getId(){ return _id; }
    public ProcessControlBlock getProcess() { return _process; }
    public void setProcess(ProcessControlBlock value) { _process = value; }

    public int getProgramCounter() { return _programCounter; }
    public void setProgramCounter(int value) { _programCounter = value; }

    public Cpu(int id, MemoryManagementUnit ram){
        _id = id;
        _registers = new int[Config.REGISTER_COUNT];
        _ram = ram;
    }

    /**
     * Indicates whether this CPU is working on a process.
     * @return True if process is running otherwise false.
     */
    public boolean isBusy(){
        return _process != null && _process.state == ProcessState.RUNNING;
    }

    /**
     * Tells the CPU to stop working on current process.
     * @return Boolean indicating whether stop() call succeeded.
     */
    //The method is called pwait() because there is wait() inherited from Object.
    public boolean pwait(){
        //Notify the CPU thread about pending termination
        //and wait for the thread to respond resetting the flag.
        _waitFlag = true;
        while(_waitFlag);
        return true;
    }

    /**
     * Tells the CPU to shut down its execution thread.
     */
    public void exit(){
        _exitFlag = true;
    }

    @Override
    public void run() {
        while(!_exitFlag){
            while(!_exitFlag && (_waitFlag || _process == null || _process.state != ProcessState.RUNNING))
                if (_waitFlag) _waitFlag = false;
            if(_exitFlag) continue;

            System.out.println(String.format("Working on process %d with status %s.", _process.jobId, _process.state));

            executionLoop();
        }
        System.out.println(String.format("Terminating CPU#%d.", _id));
    }

    private void executionLoop(){
        //Just a dummy sleep loop for testing.
        try{
            System.out.println("Cpu is working...");
            Thread.sleep(1000);
            _process.state = ProcessState.TERMINATED;
        }catch(Exception e){

        }

        //fetch();
        //decode();
        //execute();
    }

    //Start register control
    public int getRegisterValue(int register) throws InvalidRegisterAccessException{
        if(register >= 0 && register < _registers.length)
            return _registers[register];
        else
            throw new InvalidRegisterAccessException(register);
    }
    public void setRegisterValue(int register, int value) throws InvalidRegisterAccessException{
        if(register >= 0 && register < _registers.length)
            _registers[register] = value;
        else
            throw new InvalidRegisterAccessException(register);
    }
    //End register control
}

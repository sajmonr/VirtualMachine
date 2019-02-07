package edu.kennesaw.core.execution.CentralProcessingUnit;

import edu.kennesaw.core.memory.IllegalMemoryAccessException;
import edu.kennesaw.core.memory.MemoryManagementUnit;
import edu.kennesaw.core.memory.PagedMemory;
import edu.kennesaw.core.processes.ProcessControlBlock;
import edu.kennesaw.core.processes.ProcessState;
import edu.kennesaw.core.utils.BitUtils;
import edu.kennesaw.core.utils.Config;
import edu.kennesaw.core.utils.Instruction;

public class Cpu implements Runnable{
    //Fields
    private final int _id;
    private volatile boolean _exitFlag = false;
    private volatile int _executionDelay;
    private volatile ProcessControlBlock _process;
    private volatile int[] _registers;
    private volatile int _programCounter;
    private final MemoryManagementUnit _ram;

    //Getters & setters
    public int getId(){ return _id; }
    public ProcessControlBlock getProcess() { return _process; }
    public void setProcess(ProcessControlBlock value) { _process = value; }

    public int getExecutionDelay(){return _executionDelay;}
    public void setExecutionDelay(int value) { _executionDelay = value; }
    public int getProgramCounter() { return _programCounter; }
    public void setProgramCounter(int value) { _programCounter = value; }

    public Cpu(int id, MemoryManagementUnit ram){
        _id = id;
        _registers = new int[Config.REGISTER_COUNT];
        _ram = ram;
    }

    /**
     * Indicates whether this CPU is working on a process.
     * @return True if the CPU is not working otherwise false.
     */
    public boolean isIdle(){
        return _process == null || _process.state != ProcessState.RUNNING;
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
            if(_process == null || _process.state != ProcessState.RUNNING) continue;
            /*
            while(!_exitFlag && (_waitFlag || _process == null || _process.state != ProcessState.RUNNING))
                if (_waitFlag) _waitFlag = false;
            if(_exitFlag) continue;
*/
            System.out.println(String.format("Working on process %d with status %s on CPU#%d.", _process.jobId, _process.state, _id));

            executionLoop();
        }
        _exitFlag = false;
        System.out.println(String.format("Terminating CPU#%d.", _id));
    }

    private void executionLoop(){
        try{
            int instruction = fetch();

            System.out.println("Executing instruction: 0x" + Integer.toHexString(instruction));

            Instruction.Type type = Instruction.getType(instruction);
            Instruction.OpCode opCode = Instruction.getOpCode(instruction);

            execute(type, opCode, instruction);

            //This sleeps the thread after every simulated CPU cycle.
            //This helps to make difference between single-threaded vs. multi-threaded clearer.
            if(_executionDelay > 0)
                Thread.sleep(_executionDelay);

            if(_process.textSize * Config.WORD_SIZE == _programCounter)
                _process.state = ProcessState.TERMINATED;
        }catch(Exception e){
            //If any exception is thrown during execution, then
            //terminate the process.
            System.out.println(String.format("Exception thrown in process %d. Process was terminated. Exception: %s", _process.jobId, e.getMessage()));
            _process.state = ProcessState.TERMINATED;
        }
    }

    private int fetch() throws IllegalMemoryAccessException {
        int instruction = _ram.read(_programCounter, _process.pageTable);

        _programCounter += Config.WORD_SIZE;

        return instruction;
    }

    private void execute(Instruction.Type type, Instruction.OpCode opCode, int instruction) throws IllegalMemoryAccessException{
        int registerOne = 0;
        int registerTwo = 0;
        int data = 0;

        //this switch needs to cover all type enums
        //see Instruction class for enum values and project file for functionality.
        switch (type){
            case IO:
                registerOne = BitUtils.getBits(instruction, 4, 20);
                registerTwo = BitUtils.getBits(instruction, 4, 16);
                data = BitUtils.getBits(instruction, 16);
                break;
        }
        //this switch needs to cover all opCode enums
        //see Instruction class for enum values and project file for functionality.
        //possibly needs to change for caching.
        switch(opCode){
            case RD:
                _registers[registerOne] = _ram.read(data, _process.pageTable);
        }

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
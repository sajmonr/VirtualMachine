package edu.kennesaw.core.execution;

import edu.kennesaw.core.utils.NumberUtils;

public class OsDriver {

    public enum ExecutionResult{OK, Fail}

    public OsDriver(int cpus, int ramSize, int diskSize){
        int address = 92;
        int offsetSize = 4;
        int page = address >> offsetSize;
        int offset = address & ((1 << offsetSize) - 1);

        System.out.println("Page: " + page);
        System.out.println("Offset: " + offset);

        for(int i = 0; i <= 32; i++)
            System.out.println(i + " : " + NumberUtils.isPowerOfTwo(i));

        int x = 0xffffffff;
        int y = 1;
        System.out.println(x);
    }

    public ExecutionResult powerOn(){

        return ExecutionResult.OK;
    }

}

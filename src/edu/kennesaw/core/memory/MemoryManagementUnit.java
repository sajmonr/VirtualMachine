package edu.kennesaw.core.memory;

public class MemoryManagementUnit {

    //Fields
    private final PagedMemory _memory;
    private final int _offsetSize;

    public MemoryManagementUnit(PagedMemory memory){
        _memory = memory;

        _offsetSize = calculateOffsetSize();
    }

    public void write(int address, int[] data, int[] pageTable) throws MemoryOverflowException {

    }

    public int[] read(int address, int length, int[] pageTable) throws MemoryOverflowException {
        return new int[0];
    }

    private int calculateOffsetSize(){
        int pageSizeTemp = _memory.getPageSize();
        int offsetSize = 0;

        while((pageSizeTemp >>= 1) != 0) offsetSize++;

        return offsetSize;
    }

    private int getOffset(int virtualAddress){
        return virtualAddress & ((1 << _offsetSize) - 1);
    }
    private int getPageIndex(int virtualAddress){
        return virtualAddress >> _offsetSize;
    }
}

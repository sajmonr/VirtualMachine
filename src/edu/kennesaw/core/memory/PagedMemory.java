package edu.kennesaw.core.memory;

import edu.kennesaw.core.utils.NumberUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PagedMemory extends SimpleMemory implements AllocatableMemory {
    //Fields
    private final Lock _lock;
    private final Frame[] _pageFile;
    private int _framesAvailable;
    private int _pageSize;
    //Getters & Setters
    public int getPageSize(){ return _pageSize; }
    /**
     * Initializes PagedMemory class with capacity and frame size.
     * @param capacity Capacity of the memory in bytes.
     * @param frameSize Frames size of memory in bytes.
     * @throws MemoryInitializationException If the capacity or frame size are not a power of two.
     */
    public PagedMemory(int capacity, int frameSize) throws MemoryInitializationException{
        super(capacity);

        if(!NumberUtils.isPowerOfTwo(frameSize))
            throw new MemoryInitializationException(String.format("Frame size must be a power of 2 (provided size: %d).", frameSize));

        _lock = new ReentrantLock();
        _pageSize = frameSize;
        _framesAvailable = capacity / frameSize;
        _pageFile = new Frame[_framesAvailable];
        //Initialize the page file with frames
        initializePageFile();
    }

    /**
     * Allocates memory in the memory.
     * @param size Size in bytes to allocate.
     * @return Array of allocated pages.
     * @throws MemoryAllocationException If the memory is full.
     */
    @Override
    public int[] allocate(int size) throws MemoryAllocationException{
        //Calculate frames needed. If it is not evenly divisible
        //then take the next higher number of frames.
        int framesNeeded = size / _pageSize;
        if(size % _pageSize != 0)
            framesNeeded++;

        if(framesNeeded > _framesAvailable)
            throw new MemoryAllocationException(size);

        int[] framesAllocated = new int[framesNeeded];

        int frameCounter = 0;
        //Lock the memory for access so that multiple threads do not
        //try to allocate the same memory.
        _lock.lock();

        while(framesNeeded > 0){
            if (!_pageFile[frameCounter].dirty) {
                _pageFile[frameCounter].dirty = true;
                framesAllocated[framesAllocated.length - framesNeeded] = frameCounter;
                framesNeeded--;
            }
            frameCounter++;
        }

        //Unlock after page table info is collected.
        _lock.unlock();

        return framesAllocated;
    }

    /**
     * Retrieves how much memory is used in bytes.
     * @return Bytes used.
     */
    public int usedCapactiy(){
        return getCapacity() - _pageSize * _framesAvailable;
    }

    @Override
    public void deallocate(int frame) {
        if(frame < _pageFile.length)
            _pageFile[frame].dirty = false;
    }

    private void initializePageFile(){
        for(int i = 0; i < _pageFile.length; i++)
            _pageFile[i] = new Frame();
    }
}

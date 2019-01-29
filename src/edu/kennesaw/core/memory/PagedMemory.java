package edu.kennesaw.core.memory;

import edu.kennesaw.core.utils.NumberUtils;

public class PagedMemory extends SimpleMemory implements AllocatableMemory {
    //Fields
    private final Frame[] _pageFile;
    private int _framesAvailable;
    private int _pageSize;
    //Getters & Setters
    public int getPageSize(){ return _pageSize; }

    public PagedMemory(int capacity, int frameSize) throws MemoryInitializationException{
        super(capacity);

        if(!NumberUtils.isPowerOfTwo(frameSize))
            throw new MemoryInitializationException(String.format("Frame size must be a power of 2 (provided size: %d).", frameSize));

        _pageSize = frameSize;
        _framesAvailable = capacity / frameSize;
        _pageFile = new Frame[_framesAvailable];
        //Initialize the page file with frames
        initializePageFile();
    }

    @Override
    public int[] allocate(int size) throws MemoryAllocationException{
        //Calculate frames needed. If it is not evenly divisible
        //then take the next higher number of frames.
        int framesNeeded = getCapacity() / _pageSize;
        if(getCapacity() % _pageSize != 0)
            framesNeeded++;

        if(framesNeeded > _framesAvailable)
            throw new MemoryAllocationException(size);

        int[] framesAllocated = new int[framesNeeded];

        int frameCounter = 0;
        while(framesNeeded > 0){
            if (!_pageFile[frameCounter].dirty) {
                _pageFile[frameCounter].dirty = true;
                framesAllocated[framesAllocated.length - framesNeeded] = frameCounter;
            }
            frameCounter++;
        }

        return framesAllocated;
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

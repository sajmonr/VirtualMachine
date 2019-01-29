package edu.kennesaw.core.memory;

public interface Memory {
    void write(int address, int[] data) throws MemoryOverflowException;
    byte[] read(int address, int length) throws MemoryOverflowException;
}

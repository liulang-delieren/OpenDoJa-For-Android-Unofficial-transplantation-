package com.nttdocomo.lang;

/**
 * Provides access to Java-memory information managed by the implementation.
 */
public final class MemoryManager {
    /** Partition identifier representing the Java heap (=0). */
    public static final int JAVA_HEAP = 0;
    private static final MemoryManager INSTANCE = new MemoryManager();

    private MemoryManager() {
    }

    /**
     * Gets the singleton memory-manager object.
     *
     * @return the memory-manager object
     */
    public static MemoryManager getMemoryManager() {
        return INSTANCE;
    }

    /**
     * Gets the total size of each memory partition.
     *
     * @return an array containing the total size of each memory partition
     */
    public long[] totalMemory() {
        return new long[]{Runtime.getRuntime().totalMemory()};
    }

    /**
     * Gets the free size of each memory partition.
     *
     * @return an array containing the free size of each memory partition
     */
    public long[] freeMemory() {
        return new long[]{Runtime.getRuntime().freeMemory()};
    }

    /**
     * Gets the largest contiguous allocatable block in each memory partition.
     *
     * @return an array containing the largest contiguous allocatable block in
     *         each memory partition
     */
    public long[] maxContiguousMemory() {
        return new long[]{Runtime.getRuntime().maxMemory()};
    }

    /**
     * Gets the number of memory partitions.
     *
     * @return the number of memory partitions
     */
    public int getNumPartitions() {
        return totalMemory().length;
    }
}

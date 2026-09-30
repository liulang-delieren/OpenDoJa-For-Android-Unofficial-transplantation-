package com.nttdocomo.lang;

/**
 * Exception indicating that iterative processing over an array or similar
 * structure was interrupted because some exception occurred.
 *
 * <p>When the same processing is repeated for each element and an exception is
 * thrown while processing one element, this exception is raised so that the
 * failed element index and the original cause can be obtained.</p>
 */
public class IterationAbortedException extends Exception {
    private final int abortedIndex;

    /**
     * Creates an exception object without a detail message.
     *
     * @param abortedIndex the index value of the element whose processing
     *                     failed
     * @param cause the exception object that caused the failure
     */
    public IterationAbortedException(int abortedIndex, Throwable cause) {
        super(cause);
        this.abortedIndex = abortedIndex;
    }

    /**
     * Creates an exception object with a detail message.
     *
     * @param abortedIndex the index value of the element whose processing
     *                     failed
     * @param cause the exception object that caused the failure
     * @param message the detail message
     */
    public IterationAbortedException(int abortedIndex, Throwable cause, String message) {
        super(message, cause);
        this.abortedIndex = abortedIndex;
    }

    /**
     * Gets the index value of the element whose iterative processing failed.
     *
     * @return the index value of the failed element
     */
    public int getAbortedIndex() {
        return abortedIndex;
    }

    /**
     * Gets the exception object that caused the iterative-processing failure.
     *
     * @return the exception object that caused the failure
     */
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }
}

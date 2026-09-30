package com.nttdocomo.star.system;

/**
 * Star compatibility exception used by contents-management APIs.
 */
public class ContentsException extends Exception {
    public ContentsException() {
    }

    public ContentsException(String message) {
        super(message);
    }
}

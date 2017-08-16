package com.imran.wali.sharetango.Services;

/**
 * Created by junze on 2017-08-16.
 */

public enum NetworkStatus {
    DISCOVERING("Discovering..."),
    HOST("Host"),
    CLIENT("Client"),
    NO_CONNECTION("No connection");

    private String status;

    NetworkStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}

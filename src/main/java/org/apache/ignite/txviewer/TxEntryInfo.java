package org.apache.ignite.txviewer;

import java.io.Serializable;

public class TxEntryInfo implements Serializable {

    private final String cache;
    private final String key;
    private final String value;
    private final String operation;

    public TxEntryInfo(String cache, String key, String value, String operation) {
        this.cache = cache;
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    public String cache() {
        return cache;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    public String operation() {
        return operation;
    }
}

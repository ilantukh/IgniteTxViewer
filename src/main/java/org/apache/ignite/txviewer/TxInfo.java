package org.apache.ignite.txviewer;

import java.io.Serializable;
import java.util.UUID;

public class TxInfo implements Serializable {

    private String nearXidVersion;
    private final UUID nodeId;
    private final long threadId;
    private final long startTime;

    public TxInfo(String nearXidVersion, UUID nodeId, long threadId, long startTime) {
        this.nearXidVersion = nearXidVersion;
        this.nodeId = nodeId;
        this.threadId = threadId;
        this.startTime = startTime;
    }

    public String nearXidVersion() {
        return nearXidVersion;
    }

    public UUID nodeId() {
        return nodeId;
    }

    public long threadId() {
        return threadId;
    }

    public long startTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TxInfo txInfo = (TxInfo) o;

        if (threadId != txInfo.threadId) return false;
        if (startTime != txInfo.startTime) return false;
        if (nearXidVersion != null ? !nearXidVersion.equals(txInfo.nearXidVersion) : txInfo.nearXidVersion != null)
            return false;
        return nodeId != null ? nodeId.equals(txInfo.nodeId) : txInfo.nodeId == null;
    }

    @Override
    public int hashCode() {
        int result = nearXidVersion != null ? nearXidVersion.hashCode() : 0;
        result = 31 * result + (nodeId != null ? nodeId.hashCode() : 0);
        result = 31 * result + (int) (threadId ^ (threadId >>> 32));
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        return result;
    }
}

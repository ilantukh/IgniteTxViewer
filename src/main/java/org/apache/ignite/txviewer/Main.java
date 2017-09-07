package org.apache.ignite.txviewer;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import java.util.Collection;

public class Main {

    public static void main(String[] args) throws Exception {
        String cfgPath = System.getProperty("config");

        boolean serverOnly = System.getProperty("serverOnly") != null && Boolean.parseBoolean(System.getProperty("serverOnly"));
        long timeout = System.getProperty("timeout") == null ? 0 : Long.parseLong(System.getProperty("timeout"));

        Ignite ignite = Ignition.start(cfgPath);

        Collection<TxInfo> txInfos = ignite.compute().execute(new CollectTxInfoTask(serverOnly, timeout), null);

        System.out.println("Active transactions:");

        for (TxInfo txInfo : txInfos) {
            StringBuilder sb = new StringBuilder("[txId=" + txInfo.nearXidVersion() +", node=" +txInfo.nodeString() + ", startTime=" + txInfo.startTime() + ", entries=[");
            for (TxEntryInfo entry : txInfo.entries()) {
                sb.append("[cacheId=" + entry.cache() + ", key=" + entry.key() + ", value=" + entry.value() + ", op=" + entry.operation() + "]");
            }
            sb.append("]");

            System.out.println(sb.toString());
        }

        ignite.close();
    }

}

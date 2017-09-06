package org.apache.ignite.txviewer;

import org.apache.ignite.IgniteException;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskAdapter;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.transactions.IgniteInternalTx;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CollectTxInfoTask extends ComputeTaskAdapter<Void, Collection<TxInfo>> {

    private boolean serverOnly;
    private long timeout;

    /**
     * @param serverOnly If {@code true} only server nodes will be queried for active transactions.
     * @param timeout Transactions that were active for amount of time lower than this value will be filtered out from output.
     *                If parameter has zero or negative value, all transactions will be returned.
     */
    public CollectTxInfoTask(boolean serverOnly, long timeout) {
        this.serverOnly = serverOnly;
        this.timeout = timeout;
    }

    @Nullable
    public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> nodes, @Nullable Void arg) throws IgniteException {
        Map<Job, ClusterNode> map = new HashMap<>();

        for (ClusterNode node : nodes) {
            if (!serverOnly || (!node.isClient() && !node.isDaemon()))
                map.put(new Job(timeout), node);
        }

        return map;
    }

    @Nullable
    public Collection<TxInfo> reduce(List<ComputeJobResult> results) throws IgniteException {
        Set<TxInfo> txInfos = new HashSet<>();

        for (ComputeJobResult result : results)
            txInfos.addAll(result.<Collection<TxInfo>>getData());

        return txInfos;
    }

    public static class Job extends ComputeJobAdapter {
        @IgniteInstanceResource
        private IgniteEx ignite;

        private long timeout;

        public Job(long timeout) {
            this.timeout = timeout;
        }

        public Object execute() throws IgniteException {
            Collection<IgniteInternalTx> activeTxs = ignite.context().cache().context().tm().activeTransactions();

            if (activeTxs.isEmpty())
                return Collections.emptySet();

            long curTime = System.currentTimeMillis();

            Set<TxInfo> txInfos = new HashSet<>();

            for (IgniteInternalTx tx : activeTxs)
                if (timeout <= 0 || (curTime - tx.startTime()) >= timeout)
                    txInfos.add(new TxInfo(tx.nearXidVersion() != null ? tx.nearXidVersion().toString() : "", tx.nodeId(), tx.threadId(), tx.startTime()));

            return txInfos;
        }
    }
}

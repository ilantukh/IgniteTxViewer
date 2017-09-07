package org.apache.ignite.txviewer;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Collection;

public class ExampleTest {

    private static final TcpDiscoveryVmIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    private IgniteConfiguration igniteConfiguration(String name) {
        return new IgniteConfiguration()
                .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(IP_FINDER))
                .setGridName(name);
    }

    @Test
    public void test() {
        final Ignite ignite1 = Ignition.start(igniteConfiguration("ignite1"));
        final Ignite ignite2 = Ignition.start(igniteConfiguration("ignite2"));
        final Ignite ignite3 = Ignition.start(igniteConfiguration("ignite3"));
        final Ignite ignite4 = Ignition.start(igniteConfiguration("ignite4"));

        CacheConfiguration<Integer, Integer> cacheConfiguration = new CacheConfiguration<Integer, Integer>()
                .setName("tx")
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setCacheMode(CacheMode.PARTITIONED)
                .setBackups(1);

        ignite1.createCache(cacheConfiguration);

        ignite1.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        ignite1.cache("tx").put(1, 1);

        ignite2.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        ignite2.cache("tx").put(2, 2);

        ignite3.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        ignite4.cache("tx").put(3, 3);

        ignite4.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        ignite4.cache("tx").put(4, 4);

        final Ignite client = Ignition.start(igniteConfiguration("client").setClientMode(true));

        Collection<TxInfo> txInfos = client.compute().execute(new CollectTxInfoTask(false, 0), null);

        assertEquals(4, txInfos.size());
    }

    @After
    public void tearDown() throws Exception {
        Ignition.stopAll(true);

    }
}

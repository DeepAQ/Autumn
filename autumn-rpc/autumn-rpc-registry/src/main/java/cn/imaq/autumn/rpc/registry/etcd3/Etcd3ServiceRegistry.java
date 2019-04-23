package cn.imaq.autumn.rpc.registry.etcd3;

import cn.imaq.autumn.rpc.registry.ServiceProviderEntry;
import cn.imaq.autumn.rpc.registry.ServiceRegistry;
import cn.imaq.autumn.rpc.registry.exception.RpcRegistryException;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class Etcd3ServiceRegistry implements ServiceRegistry {
    private Etcd3RegistryConfig config;
    private Client etcdClient;
    private KV kvClient;
    private Lease leaseClient;
    private Watch watchClient;
    private volatile long leaseId;

    private Thread keepAliveThread;
    private volatile boolean running;

    private Map<String, Watch.Watcher> watchers = new ConcurrentHashMap<>();
    private Map<String, List<ServiceProviderEntry>> providers = new ConcurrentHashMap<>();

    public Etcd3ServiceRegistry(Etcd3RegistryConfig config) {
        this.config = config;
    }

    @Override
    public synchronized void start() throws RpcRegistryException {
        if (running) {
            return;
        }

        this.etcdClient = Client.builder().endpoints(config.getEndpoints()).build();
        this.kvClient = etcdClient.getKVClient();
        this.leaseClient = etcdClient.getLeaseClient();
        this.watchClient = etcdClient.getWatchClient();

        this.keepAliveThread = new Thread(() -> {
            Lease leaseClient = etcdClient.getLeaseClient();
            while (running) {
                long startTime = 0, endTime = 0;

                if (this.leaseId > 0) {
                    startTime = System.currentTimeMillis();
                    try {
                        leaseClient.keepAliveOnce(leaseId).get();
                    } catch (Exception e) {
                        log.warn("Failed to keep alive id {}: {}", leaseId, String.valueOf(e));
                    }
                    endTime = System.currentTimeMillis();
                }

                try {
                    Thread.sleep(config.getKeepAliveInterval() * 1000 - (endTime - startTime));
                } catch (InterruptedException e) {
                    log.warn(String.valueOf(e));
                    return;
                }
            }
        }, "AutumnRPC-Etcd3ServiceRegistry-KeepAlive");

        this.running = true;
        this.keepAliveThread.start();
    }

    @Override
    public synchronized void stop() {
        this.running = false;
        if (this.keepAliveThread != null) {
            try {
                this.keepAliveThread.interrupt();
                this.keepAliveThread.join();
            } catch (InterruptedException ignored) {
            }
        }
        this.watchers.clear();
        this.providers.clear();
        this.leaseId = 0;
        if (this.etcdClient != null) {
            this.etcdClient.close();
        }
    }

    @Override
    public void register(ServiceProviderEntry provider) throws RpcRegistryException {
        try {
            long leaseId = getLeaseId();
            byte[] key = getKeyForProvider(provider).getBytes();
            byte[] value = provider.getConfigStr().getBytes();
            kvClient.put(ByteSequence.from(key), ByteSequence.from(value), PutOption.newBuilder().withLeaseId(leaseId).build()).get();
            log.info("Registered {}", provider);
        } catch (Exception e) {
            throw new RpcRegistryException(e);
        }
    }

    @Override
    public void deregister(ServiceProviderEntry provider) throws RpcRegistryException {
        byte[] key = getKeyForProvider(provider).getBytes();
        try {
            kvClient.delete(ByteSequence.from(key)).get();
            log.info("Deregistered {}", provider);
        } catch (Exception e) {
            throw new RpcRegistryException(e);
        }
    }

    @Override
    public void subscribe(String serviceName) throws RpcRegistryException {
        byte[] key = getPathForService(serviceName).getBytes();
        ByteSequence keySeq = ByteSequence.from(key);
        Watch.Watcher watcher = watchClient.watch(keySeq, WatchOption.newBuilder().withPrefix(keySeq).build(), watchResponse -> {
            if (watchResponse.getEvents().size() <= 0) {
                return;
            }

            List<ServiceProviderEntry> newList = providers.get(serviceName);
            if (newList == null) {
                newList = new ArrayList<>();
            } else {
                newList = new ArrayList<>(newList);
            }
            for (WatchEvent event : watchResponse.getEvents()) {
                ServiceProviderEntry provider = parseKV(event.getKeyValue());
                switch (event.getEventType()) {
                    case PUT:
                        if (newList.indexOf(provider) < 0) {
                            newList.add(provider);
                        }
                        break;
                    case DELETE:
                        newList.remove(provider);
                        break;
                }
            }
            providers.put(serviceName, newList);
        });

        watchers.put(serviceName, watcher);
        // force update once
        lookup(serviceName, true);
        log.info("Subscribed {}", serviceName);
    }

    @Override
    public void unsubscribe(String serviceName) {
        Watch.Watcher watcher = watchers.get(serviceName);
        if (watcher != null) {
            watcher.close();
        }
        log.info("Unsubscribed {}", serviceName);
    }

    @Override
    public List<ServiceProviderEntry> lookup(String serviceName, boolean forceUpdate) throws RpcRegistryException {
        if (forceUpdate) {
            byte[] key = getPathForService(serviceName).getBytes();
            ByteSequence keySeq = ByteSequence.from(key);
            try {
                GetResponse resp = kvClient.get(keySeq, GetOption.newBuilder().withPrefix(keySeq).build()).get();
                List<ServiceProviderEntry> providerEntryList = resp.getKvs().stream().map(this::parseKV).collect(Collectors.toList());
                providers.put(serviceName, providerEntryList);
                return providerEntryList;
            } catch (Exception e) {
                throw new RpcRegistryException(e);
            }
        } else {
            List<ServiceProviderEntry> providerEntryList = providers.get(serviceName);
            if (providerEntryList == null) {
                return Collections.emptyList();
            } else {
                return providerEntryList;
            }
        }
    }

    private String getKeyForProvider(ServiceProviderEntry provider) {
        return config.getKeyPrefix() + "/" + provider.getServiceName() + "/" + provider.getHost() + "|" + provider.getPort();
    }

    private String getPathForService(String serviceName) {
        return config.getKeyPrefix() + "/" + serviceName + "/";
    }

    private ServiceProviderEntry parseKV(KeyValue kv) {
        String key = kv.getKey().toString(StandardCharsets.UTF_8);
        String value = kv.getValue().toString(StandardCharsets.UTF_8);

        String[] labels = key.split("/");
        String serviceName = labels[labels.length - 2];
        String[] hostPort = labels[labels.length - 1].split("\\|", 2);
        return ServiceProviderEntry.builder()
                .serviceName(serviceName)
                .host(hostPort[0])
                .port(Integer.parseInt(hostPort[1]))
                .configStr(value)
                .build();
    }

    private long getLeaseId() throws ExecutionException, InterruptedException {
        if (leaseId == 0) {
            synchronized (this) {
                if (leaseId == 0) {
                    leaseId = leaseClient.grant(config.getKeepAliveTimeout()).get().getID();
                }
            }
        }

        return leaseId;
    }
}

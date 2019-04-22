package cn.imaq.autumn.rpc.registry;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
public class ServiceProviderEntry {
    private String serviceName;

    private String host;

    private int port;

    @EqualsAndHashCode.Exclude
    private String configStr;
}

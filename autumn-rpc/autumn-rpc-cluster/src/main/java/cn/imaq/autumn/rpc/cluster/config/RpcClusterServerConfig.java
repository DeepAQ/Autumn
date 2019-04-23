package cn.imaq.autumn.rpc.cluster.config;

import cn.imaq.autumn.rpc.server.config.RpcServerConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Getter
@Setter
@SuperBuilder
public class RpcClusterServerConfig extends RpcClusterConfigBase {
    @Builder.Default
    private RpcServerConfig serverConfig = RpcServerConfig.builder().build();

    @Builder.Default
    private String advertiseHost = getFirstNonLoopbackIPv4Address();

    private static String getFirstNonLoopbackIPv4Address() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface i = interfaces.nextElement();
                for (Enumeration<InetAddress> addresses = i.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }

        return null;
    }
}

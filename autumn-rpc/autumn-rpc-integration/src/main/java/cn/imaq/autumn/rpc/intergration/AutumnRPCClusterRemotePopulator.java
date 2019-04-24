package cn.imaq.autumn.rpc.intergration;

import cn.imaq.autumn.core.beans.populator.AnnotatedFieldPopulator;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;
import cn.imaq.autumn.rpc.cluster.AutumnRPCClusterClient;
import cn.imaq.autumn.rpc.intergration.annotation.AutumnRPCClusterRemote;

import java.lang.reflect.Field;

public class AutumnRPCClusterRemotePopulator extends AnnotatedFieldPopulator<AutumnRPCClusterRemote> {
    private AutumnRPCClusterClient clusterClient;

    @Override
    protected Object populate(AutumnContext context, Field field, AutumnRPCClusterRemote anno) throws BeanPopulationException {
        if (this.clusterClient == null) {
            this.clusterClient = context.getBeanByType(AutumnRPCClusterClient.class);
            if (this.clusterClient == null) {
                throw new BeanPopulationException("No AutumnRPCClusterClient instances found in the Autumn context");
            }
        }

        try {
            if (anno.timeoutMs() > 0) {
                return clusterClient.getProxy(field.getType(), anno.timeoutMs(), anno.loadBalancer().newInstance());
            } else {
                return clusterClient.getProxy(field.getType(), anno.loadBalancer().newInstance());
            }
        } catch (Exception e) {
            throw new BeanPopulationException(e);
        }
    }
}

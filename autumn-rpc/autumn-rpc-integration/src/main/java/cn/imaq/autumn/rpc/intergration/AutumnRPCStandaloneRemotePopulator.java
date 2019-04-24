package cn.imaq.autumn.rpc.intergration;

import cn.imaq.autumn.core.beans.populator.AnnotatedFieldPopulator;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;
import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.intergration.annotation.AutumnRPCStandaloneRemote;

import java.lang.reflect.Field;

public class AutumnRPCStandaloneRemotePopulator extends AnnotatedFieldPopulator<AutumnRPCStandaloneRemote> {
    private AutumnRPCClient rpcClient;

    @Override
    protected Object populate(AutumnContext context, Field field, AutumnRPCStandaloneRemote anno) throws BeanPopulationException {
        if (this.rpcClient == null) {
            this.rpcClient = context.getBeanByType(AutumnRPCClient.class);
            if (this.rpcClient == null) {
                throw new BeanPopulationException("No AutumnRPCClient instances found in the Autumn context");
            }
        }

        if (anno.timeoutMs() > 0) {
            return rpcClient.getService(field.getType(), anno.timeoutMs());
        } else {
            return rpcClient.getService(field.getType());
        }
    }
}

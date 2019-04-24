package cn.imaq.autumn.rpc.intergration.annotation;

import cn.imaq.autumn.rpc.cluster.loadbalance.LoadBalancer;
import cn.imaq.autumn.rpc.cluster.loadbalance.RandomLoadBalancer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutumnRPCClusterRemote {
    int timeoutMs() default 0;

    Class<? extends LoadBalancer> loadBalancer() default RandomLoadBalancer.class;
}

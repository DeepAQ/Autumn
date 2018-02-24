package cn.imaq.autumn.aop.processor;

import cn.imaq.autumn.aop.AopContext;
import cn.imaq.autumn.aop.HookModel;
import cn.imaq.autumn.aop.callback.AopMethodInterceptor;
import cn.imaq.autumn.core.beans.BeanWrapper;
import cn.imaq.autumn.core.beans.processor.AfterBeanPopulateProcessor;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

import java.util.List;

@Slf4j
public class AopBeanProcessor implements AfterBeanPopulateProcessor {
    @Override
    public void process(BeanWrapper bean) {
        Class<?> type = bean.getBeanInfo().getType();
        List<HookModel> classHooks = AopContext.getFrom(bean.getContext()).getHooksForClass(type);
        if (!classHooks.isEmpty()) {
            log.info("Creating proxy for {}", bean.getBeanInstance().getClass().getName());
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(type);
            enhancer.setCallback(new AopMethodInterceptor(classHooks, bean.getContext(), bean.getBeanInstance()));
            bean.setBeanInstance(enhancer.create());
        }
    }
}

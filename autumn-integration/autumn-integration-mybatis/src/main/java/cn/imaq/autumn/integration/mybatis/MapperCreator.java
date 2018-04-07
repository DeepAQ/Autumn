package cn.imaq.autumn.integration.mybatis;

import cn.imaq.autumn.core.beans.creator.BeanCreator;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanCreationException;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MapperCreator implements BeanCreator {
    private Class<?> mapperType;

    private AutumnContext context;

    MapperCreator(Class<?> mapperType, AutumnContext context) {
        this.mapperType = mapperType;
        this.context = context;
    }

    @Override
    public Object createBean() throws BeanCreationException {
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) context.getBeanByType(SqlSessionFactory.class);
        if (sqlSessionFactory == null) {
            throw new BeanCreationException("Cannot get SqlSessionFactory from application context");
        }
        try {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{mapperType}, new InvocationHandler() {
                private ThreadLocal<Object> targetLocal = new ThreadLocal<>();

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Object target = targetLocal.get();
                    if (target == null) {
                        target = sqlSessionFactory.openSession(true).getMapper(mapperType);
                        targetLocal.set(target);
                    }
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                }
            });
        } catch (Exception e) {
            throw new BeanCreationException(e);
        }
    }
}

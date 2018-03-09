package cn.imaq.autumn.integration.mybatis;

import cn.imaq.autumn.core.beans.creator.BeanCreator;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanCreationException;
import org.apache.ibatis.session.SqlSessionFactory;

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
            return sqlSessionFactory.openSession().getMapper(mapperType);
        } catch (Exception e) {
            throw new BeanCreationException(e);
        }
    }
}

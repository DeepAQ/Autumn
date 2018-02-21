package cn.imaq.autumn.core.beans.populator;

import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;

import javax.annotation.Resource;
import java.lang.reflect.Field;

public class ResourceFieldPopulator extends AnnotatedFieldPopulator<Resource> {
    @Override
    protected Object populate(AutumnContext context, Field field, Resource anno) throws BeanPopulationException {
        Object result;
        String name = anno.name();
        Class<?> type = anno.type();
        // try name first
        if (!name.isEmpty()) {
            result = context.getBeanByName(name, true);
            if (result != null) {
                return result;
            }
        }
        // try type
        if (type != Object.class) {
            result = context.getBeanByType(type, true);
            if (result != null) {
                return result;
            }
        }
        // default name
        name = field.getName().toLowerCase();
        result = context.getBeanByName(name, true);
        if (result != null) {
            return result;
        }
        // default type
        type = field.getType();
        result = context.getBeanByType(type, true);
        if (result != null) {
            return result;
        }
        return null;
    }
}

package cn.imaq.autumn.core.beans.populator;

import cn.imaq.autumn.core.annotation.Autumnwired;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;

import java.lang.reflect.Field;

public class AutumnwiredFieldPopulator extends AnnotatedFieldPopulator<Autumnwired> {
    @Override
    protected Object populate(AutumnContext context, Field field, Autumnwired anno) throws BeanPopulationException {
        Object result;
        String name = anno.name();
        Class<?> type = anno.type();
        // try type first
        if (type != Object.class) {
            result = context.getBeanByType(type, true);
            if (result != null) {
                return result;
            }
        }
        // try name
        if (!name.isEmpty()) {
            result = context.getBeanByName(name, true);
            if (result != null) {
                return result;
            }
        }
        // default type
        type = field.getType();
        result = context.getBeanByType(type, true);
        if (result != null) {
            return result;
        }
        // default name
        name = field.getName().toLowerCase();
        result = context.getBeanByName(name, true);
        if (result != null) {
            return result;
        }
        if (anno.required()) {
            throw new BeanPopulationException("No suitable bean for field " + field);
        }
        return null;
    }
}

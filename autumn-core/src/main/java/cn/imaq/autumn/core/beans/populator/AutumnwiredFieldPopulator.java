package cn.imaq.autumn.core.beans.populator;

import cn.imaq.autumn.core.annotation.Autumnwired;
import cn.imaq.autumn.core.context.AutumnContext;
import cn.imaq.autumn.core.exception.BeanPopulationException;

import java.lang.reflect.Field;

public class AutumnwiredFieldPopulator extends AnnotatedFieldPopulator<Autumnwired> {
    @Override
    protected Object populate(AutumnContext context, Field field, Autumnwired anno) throws BeanPopulationException {
        Object result;
        // try type first
        Class<?> type = anno.type();
        if (type == Object.class) {
            type = field.getType();
        }
        result = context.getBeanByType(type, true);
        if (result != null) {
            return result;
        }
        // try name
        String name = anno.name();
        if (name.isEmpty()) {
            name = field.getName().toLowerCase();
        }
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

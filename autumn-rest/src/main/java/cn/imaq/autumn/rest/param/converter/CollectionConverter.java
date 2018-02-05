package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.*;

public class CollectionConverter implements TypeConverter {
    @Override
    public List<Class<?>> getTargetTypes() {
        return Arrays.asList(Collection.class, List.class, Set.class, SortedSet.class, NavigableSet.class, Queue.class, Deque.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(Object src, Class<T> targetType) throws ParamConvertException {
        Collection<?> srcCollection;
        if (src instanceof Collection) {
            srcCollection = (Collection) src;
        } else if (src instanceof Object[]) {
            srcCollection = Arrays.asList((Object[]) src);
        } else {
            throw new ParamConvertException(src, targetType);
        }
        if (targetType == Collection.class) {
            return (T) srcCollection;
        } else if (targetType == List.class) {
            return (T) new ArrayList(srcCollection);
        } else if (targetType == Set.class) {
            return (T) new LinkedHashSet(srcCollection);
        } else if (targetType == SortedSet.class || targetType == NavigableSet.class) {
            return (T) new TreeSet(srcCollection);
        } else if (targetType == Queue.class || targetType == Deque.class) {
            return (T) new ArrayDeque(srcCollection);
        }
        throw new ParamConvertException(src, targetType);
    }
}

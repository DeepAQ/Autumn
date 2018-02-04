package cn.imaq.autumn.rest.param.converter;

import cn.imaq.autumn.rest.exception.ParamConvertException;

import java.util.*;

public class CollectionConverter implements TypeConverter<Collection> {
    @Override
    public List<Class<? extends Collection>> getTargetTypes() {
        return Arrays.asList(Collection.class, List.class, Set.class, SortedSet.class, NavigableSet.class, Queue.class, Deque.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends Collection> P convert(Object src, Class<P> targetType) throws ParamConvertException {
        Collection<?> srcCollection;
        if (src instanceof Collection) {
            srcCollection = (Collection) src;
        } else if (src instanceof Object[]) {
            srcCollection = Arrays.asList((Object[]) src);
        } else {
            throw new ParamConvertException(src, targetType);
        }
        if (targetType == Collection.class) {
            return (P) srcCollection;
        } else if (targetType == List.class) {
            return (P) new ArrayList(srcCollection);
        } else if (targetType == Set.class) {
            return (P) new LinkedHashSet(srcCollection);
        } else if (targetType == SortedSet.class || targetType == NavigableSet.class) {
            return (P) new TreeSet(srcCollection);
        } else if (targetType == Queue.class || targetType == Deque.class) {
            return (P) new ArrayDeque(srcCollection);
        }
        throw new ParamConvertException(src, targetType);
    }
}

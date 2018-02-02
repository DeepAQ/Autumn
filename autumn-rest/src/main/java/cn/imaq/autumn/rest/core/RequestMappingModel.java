package cn.imaq.autumn.rest.core;

import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.util.PathUtil;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class RequestMappingModel {
    private Set<String> paths;
    private Set<RequestMethod> methods = Collections.emptySet();
    private Set<String> consumes = Collections.emptySet();
    private String produces;
    private Method method;

    private RequestMappingModel() {
    }

    public static RequestMappingModel fromAnnotation(RequestMapping rm) {
        RequestMappingModel model = new RequestMappingModel();
        model.setPaths(Arrays.stream(rm.value()).map(PathUtil::transform).collect(Collectors.toSet()));
        if (rm.method().length > 0) {
            model.setMethods(EnumSet.copyOf(Arrays.asList(rm.method())));
        }
        if (rm.consumes().length > 0) {
            model.setConsumes(new HashSet<>(Arrays.asList(rm.consumes())));
        }
        model.setProduces(rm.produces());
        return model;
    }

    public void combine(RequestMappingModel parent) {
        Set<String> newPaths = new HashSet<>();
        for (String parentPath : parent.getPaths()) {
            if (parentPath.equals("/")) {
                parentPath = "";
            }
            for (String path : this.getPaths()) {
                newPaths.add(parentPath + path);
            }
        }
        this.setPaths(newPaths);
        if (!parent.getMethods().isEmpty()) {
            this.methods.retainAll(parent.getMethods());
        }
        if (!parent.getConsumes().isEmpty()) {
            this.consumes.retainAll(parent.getConsumes());
        }
    }
}

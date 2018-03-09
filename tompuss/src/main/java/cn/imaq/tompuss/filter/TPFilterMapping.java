package cn.imaq.tompuss.filter;

import cn.imaq.tompuss.util.TPUrlPattern;
import lombok.Getter;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class TPFilterMapping {
    @Getter
    private TPFilterRegistration registration;

    @Getter
    private Set<DispatcherType> dispatcherTypes;

    TPFilterMapping(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes) {
        this.registration = registration;
        if (dispatcherTypes != null) {
            this.dispatcherTypes = dispatcherTypes;
        } else {
            this.dispatcherTypes = Collections.singleton(DispatcherType.REQUEST);
        }
    }

    public abstract boolean match(String path, String servletName);

    static class ByServlet extends TPFilterMapping {
        private Set<String> servletNames;

        ByServlet(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes, String[] servletNames) {
            super(registration, dispatcherTypes);
            this.servletNames = Arrays.stream(servletNames).collect(Collectors.toSet());
        }

        @Override
        public boolean match(String path, String servletName) {
            return servletName != null && this.servletNames.contains(servletName);
        }
    }

    static class ByUrlPattern extends TPFilterMapping {
        private List<TPUrlPattern> urlPatterns;

        ByUrlPattern(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes, String[] patterns) {
            super(registration, dispatcherTypes);
            this.urlPatterns = Arrays.stream(patterns).map(TPUrlPattern::new).collect(Collectors.toList());
        }

        @Override
        public boolean match(String path, String servletName) {
            if (path == null) {
                return false;
            }
            for (TPUrlPattern pattern : urlPatterns) {
                if (pattern.getType() != TPUrlPattern.Type.DEFAULT && pattern.match(path).getPatternLength() >= 0) {
                    return true;
                }
            }
            return false;
        }
    }
}

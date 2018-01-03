package cn.imaq.tompuss.filter;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TPFilterMapping {
    private TPFilterRegistration registration;
    private Set<DispatcherType> dispatcherTypes;

    public TPFilterMapping(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes) {
        this.registration = registration;
        if (dispatcherTypes != null) {
            this.dispatcherTypes = dispatcherTypes;
        } else {
            this.dispatcherTypes = Collections.singleton(DispatcherType.REQUEST);
        }
    }

    static class ByServlet extends TPFilterMapping {
        private Set<String> servletNames = new HashSet<>();

        public ByServlet(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes, String[] servletNames) {
            super(registration, dispatcherTypes);
            this.servletNames.addAll(Arrays.asList(servletNames));
        }
    }

    static class ByUrlPattern extends TPFilterMapping {
        private Set<String> urlPatterns = new HashSet<>();

        public ByUrlPattern(TPFilterRegistration registration, Set<DispatcherType> dispatcherTypes, String[] patterns) {
            super(registration, dispatcherTypes);
            this.urlPatterns.addAll(Arrays.asList(patterns));
        }
    }
}

package cn.imaq.tompuss.servlet;

import cn.imaq.tompuss.core.TPRegistration;

import javax.servlet.*;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TPServletRegistration extends TPRegistration<Servlet> implements ServletRegistration.Dynamic {
    private int loadOnStartup = -1;
    private ServletSecurityElement securityElement;
    private MultipartConfigElement multipartConfig;
    private String runAsRole;
    private Queue<String> mappings = new ConcurrentLinkedQueue<>();

    public TPServletRegistration(TPServletContext context, String name, Servlet instance) {
        super(context, name, instance);
    }

    public void loadAnnotation(WebServlet ws) {
        this.addMapping(ws.value());
        this.addMapping(ws.urlPatterns());
        this.setLoadOnStartup(ws.loadOnStartup());
        this.setAsyncSupported(ws.asyncSupported());
        for (WebInitParam initParam : ws.initParams()) {
            this.setInitParameter(initParam.name(), initParam.value());
        }
    }

    /**
     * Sets the <code>loadOnStartup</code> priority on the Servlet
     * represented by this dynamic ServletRegistration.
     * <p>
     * <p>A <tt>loadOnStartup</tt> value of greater than or equal to
     * zero indicates to the container the initialization priority of
     * the Servlet. In this case, the container must instantiate and
     * initialize the Servlet during the initialization phase of the
     * ServletContext, that is, after it has invoked all of the
     * ServletContextListener objects configured for the ServletContext
     * at their {@link ServletContextListener#contextInitialized}
     * method.
     * <p>
     * <p>If <tt>loadOnStartup</tt> is a negative integer, the container
     * is free to instantiate and initialize the Servlet lazily.
     * <p>
     * <p>The default value for <tt>loadOnStartup</tt> is <code>-1</code>.
     * <p>
     * <p>A call to this method overrides any previous setting.
     *
     * @param loadOnStartup the initialization priority of the Servlet
     * @throws IllegalStateException if the ServletContext from which
     *                               this ServletRegistration was obtained has already been initialized
     */
    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    /**
     * Sets the {@link ServletSecurityElement} to be applied to the
     * mappings defined for this <code>ServletRegistration</code>.
     * <p>
     * <p>This method applies to all mappings added to this
     * <code>ServletRegistration</code> up until the point that the
     * <code>ServletContext</code> from which it was obtained has been
     * initialized.
     * <p>
     * <p>If a URL pattern of this ServletRegistration is an exact target
     * of a <code>security-constraint</code> that was established via
     * the portable deployment descriptor, then this method does not
     * change the <code>security-constraint</code> for that pattern,
     * and the pattern will be included in the return value.
     * <p>
     * <p>If a URL pattern of this ServletRegistration is an exact
     * target of a security constraint that was established via the
     * {@link ServletSecurity} annotation
     * or a previous call to this method, then this method replaces
     * the security constraint for that pattern.
     * <p>
     * <p>If a URL pattern of this ServletRegistration is neither the
     * exact target of a security constraint that was established via
     * the {@link ServletSecurity} annotation
     * or a previous call to this method, nor the exact target of a
     * <code>security-constraint</code> in the portable deployment
     * descriptor, then this method establishes the security constraint
     * for that pattern from the argument
     * <code>ServletSecurityElement</code>.
     * <p>
     * <p>The returned set is not backed by the {@code Dynamic} object,
     * so changes in the returned set are not reflected in the
     * {@code Dynamic} object, and vice-versa.</p>
     *
     * @param constraint the {@link ServletSecurityElement} to be applied
     *                   to the patterns mapped to this ServletRegistration
     * @return the (possibly empty) Set of URL patterns that were already
     * the exact target of a <code>security-constraint</code> that was
     * established via the portable deployment descriptor. This method
     * has no effect on the patterns included in the returned set
     * @throws IllegalArgumentException if <tt>constraint</tt> is null
     * @throws IllegalStateException    if the {@link ServletContext} from
     *                                  which this <code>ServletRegistration</code> was obtained has
     *                                  already been initialized
     */
    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        if (constraint == null) {
            throw new IllegalArgumentException();
        }
        this.securityElement = constraint;
        // TODO security
        return Collections.emptySet();
    }

    /**
     * Sets the {@link MultipartConfigElement} to be applied to the
     * mappings defined for this <code>ServletRegistration</code>. If this
     * method is called multiple times, each successive call overrides the
     * effects of the former.
     *
     * @param multipartConfig the {@link MultipartConfigElement} to be
     *                        applied to the patterns mapped to the registration
     * @throws IllegalArgumentException if <tt>multipartConfig</tt> is
     *                                  null
     * @throws IllegalStateException    if the {@link ServletContext} from
     *                                  which this ServletRegistration was obtained has already been
     *                                  initialized
     */
    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        if (multipartConfig == null) {
            throw new IllegalArgumentException();
        }
        this.multipartConfig = multipartConfig;
    }

    /**
     * Sets the name of the <code>runAs</code> role for this
     * <code>ServletRegistration</code>.
     *
     * @param roleName the name of the <code>runAs</code> role
     * @throws IllegalArgumentException if <tt>roleName</tt> is null
     * @throws IllegalStateException    if the {@link ServletContext} from
     *                                  which this ServletRegistration was obtained has already been
     *                                  initialized
     */
    @Override
    public void setRunAsRole(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException();
        }
        this.runAsRole = roleName;
    }

    /**
     * Adds a servlet mapping with the given URL patterns for the Servlet
     * represented by this ServletRegistration.
     * <p>
     * <p>If any of the specified URL patterns are already mapped to a
     * different Servlet, no updates will be performed.
     * <p>
     * <p>If this method is called multiple times, each successive call
     * adds to the effects of the former.
     * <p>
     * <p>The returned set is not backed by the {@code ServletRegistration}
     * object, so changes in the returned set are not reflected in the
     * {@code ServletRegistration} object, and vice-versa.</p>
     *
     * @param urlPatterns the URL patterns of the servlet mapping
     * @return the (possibly empty) Set of URL patterns that are already
     * mapped to a different Servlet
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null
     *                                  or empty
     * @throws IllegalStateException    if the ServletContext from which this
     *                                  ServletRegistration was obtained has already been initialized
     */
    @Override
    public Set<String> addMapping(String... urlPatterns) {
        Set<String> conflicts = new HashSet<>();
        for (String pattern : urlPatterns) {
            if (this.context.addServletMapping(pattern, this)) {
                this.mappings.add(pattern);
            } else {
                conflicts.add(pattern);
            }
        }
        return conflicts;
    }

    /**
     * Gets the currently available mappings of the
     * Servlet represented by this <code>ServletRegistration</code>.
     * <p>
     * <p>If permitted, any changes to the returned <code>Collection</code> must not
     * affect this <code>ServletRegistration</code>.
     *
     * @return a (possibly empty) <code>Collection</code> of the currently
     * available mappings of the Servlet represented by this
     * <code>ServletRegistration</code>
     */
    @Override
    public Collection<String> getMappings() {
        return Collections.unmodifiableCollection(this.mappings);
    }

    /**
     * Gets the name of the runAs role of the Servlet represented by this
     * <code>ServletRegistration</code>.
     *
     * @return the name of the runAs role, or null if the Servlet is
     * configured to run as its caller
     */
    @Override
    public String getRunAsRole() {
        return this.runAsRole;
    }
}

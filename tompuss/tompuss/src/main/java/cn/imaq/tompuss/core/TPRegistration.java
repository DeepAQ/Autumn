package cn.imaq.tompuss.core;

import cn.imaq.tompuss.servlet.TPServletContext;

import javax.servlet.Registration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TPRegistration<T> implements Registration.Dynamic {
    protected TPServletContext context;
    protected String name;
    protected T instance;

    private boolean asyncSupported;
    private Map<String, String> initParams = new ConcurrentHashMap<>();

    public TPRegistration(TPServletContext context, String name, T instance) {
        this.context = context;
        this.name = name;
        this.instance = instance;
    }

    /**
     * Configures the Servlet or Filter represented by this dynamic
     * Registration as supporting asynchronous operations or not.
     * <p>
     * <p>By default, servlet and filters do not support asynchronous
     * operations.
     * <p>
     * <p>A call to this method overrides any previous setting.
     *
     * @param isAsyncSupported true if the Servlet or Filter represented
     *                         by this dynamic Registration supports asynchronous operations,
     *                         false otherwise
     * @throws IllegalStateException if the ServletContext from which
     *                               this dynamic Registration was obtained has already been
     *                               initialized
     */
    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    /**
     * Gets the name of the Servlet or Filter that is represented by this
     * Registration.
     *
     * @return the name of the Servlet or Filter that is represented by this
     * Registration
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Gets the fully qualified class name of the Servlet or Filter that
     * is represented by this Registration.
     *
     * @return the fully qualified class name of the Servlet or Filter
     * that is represented by this Registration, or null if this
     * Registration is preliminary
     */
    @Override
    public String getClassName() {
        return (this.instance == null ? null : this.instance.getClass().getName());
    }

    /**
     * Sets the initialization parameter with the given name and value
     * on the Servlet or Filter that is represented by this Registration.
     *
     * @param name  the initialization parameter name
     * @param value the initialization parameter value
     * @return true if the update was successful, i.e., an initialization
     * parameter with the given name did not already exist for the Servlet
     * or Filter represented by this Registration, and false otherwise
     * @throws IllegalStateException    if the ServletContext from which this
     *                                  Registration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     *                                  {@code null}
     */
    @Override
    public boolean setInitParameter(String name, String value) {
        if (this.initParams.containsKey(name)) {
            return false;
        }
        this.initParams.put(name, value);
        return true;
    }

    /**
     * Gets the value of the initialization parameter with the given name
     * that will be used to initialize the Servlet or Filter represented
     * by this Registration object.
     *
     * @param name the name of the initialization parameter whose value is
     *             requested
     * @return the value of the initialization parameter with the given
     * name, or {@code null}  if no initialization parameter with the given
     * name exists
     */
    @Override
    public String getInitParameter(String name) {
        return this.initParams.get(name);
    }

    /**
     * Sets the given initialization parameters on the Servlet or Filter
     * that is represented by this Registration.
     * <p>
     * <p>The given map of initialization parameters is processed
     * <i>by-value</i>, i.e., for each initialization parameter contained
     * in the map, this method calls {@link #setInitParameter(String, String)}.
     * If that method would return false for any of the
     * initialization parameters in the given map, no updates will be
     * performed, and false will be returned. Likewise, if the map contains
     * an initialization parameter with a {@code null}  name or value, no
     * updates will be performed, and an IllegalArgumentException will be
     * thrown.
     * <p>
     * <p>The returned set is not backed by the {@code Registration} object,
     * so changes in the returned set are not reflected in the
     * {@code Registration} object, and vice-versa.</p>
     *
     * @param initParameters the initialization parameters
     * @return the (possibly empty) Set of initialization parameter names
     * that are in conflict
     * @throws IllegalStateException    if the ServletContext from which this
     *                                  Registration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     *                                  initialization parameter with a {@code null}  name or value
     */
    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        Set<String> conflicts = new HashSet<>();
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            if (!this.setInitParameter(entry.getKey(), entry.getValue())) {
                conflicts.add(entry.getKey());
            }
        }
        return conflicts;
    }

    /**
     * Gets an immutable (and possibly empty) Map containing the
     * currently available initialization parameters that will be used to
     * initialize the Servlet or Filter represented by this Registration
     * object.
     *
     * @return Map containing the currently available initialization
     * parameters that will be used to initialize the Servlet or Filter
     * represented by this Registration object
     */
    @Override
    public Map<String, String> getInitParameters() {
        return Collections.unmodifiableMap(this.initParams);
    }
}

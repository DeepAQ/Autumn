package cn.imaq.tompuss.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPHttpSession implements HttpSession {
    private ServletContext context;
    private String sessId;
    private long created;
    private long lastAccess;
    private int timeout;
    private volatile boolean invalidated;

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public TPHttpSession(ServletContext context, String sessId) {
        this.context = context;
        this.sessId = sessId;
        this.created = System.currentTimeMillis();
        this.lastAccess = this.created;
        this.timeout = context.getSessionTimeout();
    }

    /**
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return a <code>long</code> specifying
     * when this session was created,
     * expressed in
     * milliseconds since 1/1/1970 GMT
     */
    @Override
    public long getCreationTime() {
        this.checkValid();
        return this.created;
    }

    /**
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the servlet container and is implementation dependent.
     *
     * @return a string specifying the identifier
     * assigned to this session
     */
    @Override
    public String getId() {
        return this.sessId;
    }

    /**
     * Returns the last time the client sent a request associated with
     * this session, as the number of milliseconds since midnight
     * January 1, 1970 GMT, and marked by the time the container received the
     * request.
     * <p>
     * <p>Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access
     * time.
     *
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return a <code>long</code>
     * representing the last time
     * the client sent a request associated
     * with this session, expressed in
     * milliseconds since 1/1/1970 GMT
     */
    @Override
    public long getLastAccessedTime() {
        this.checkValid();
        return this.lastAccess;
    }

    /**
     * Returns the ServletContext to which this session belongs.
     *
     * @return The ServletContext object for the web application
     * @since Servlet 2.3
     */
    @Override
    public ServletContext getServletContext() {
        return this.context;
    }

    /**
     * Specifies the time, in seconds, between client requests before the
     * servlet container will invalidate this session.
     * <p>
     * <p>An <tt>interval</tt> value of zero or less indicates that the
     * session should never timeout.
     *
     * @param interval An integer specifying the number
     *                 of seconds
     */
    @Override
    public void setMaxInactiveInterval(int interval) {
        this.timeout = interval;
    }

    /**
     * Returns the maximum time interval, in seconds, that
     * the servlet container will keep this session open between
     * client accesses. After this interval, the servlet container
     * will invalidate the session.  The maximum time interval can be set
     * with the <code>setMaxInactiveInterval</code> method.
     * <p>
     * <p>A return value of zero or less indicates that the
     * session will never timeout.
     *
     * @return an integer specifying the number of
     * seconds this session remains open
     * between client requests
     * @see        #setMaxInactiveInterval
     */
    @Override
    public int getMaxInactiveInterval() {
        return this.timeout;
    }

    /**
     * @return the {@link HttpSessionContext} for this session.
     * @deprecated As of Version 2.1, this method is
     * deprecated and has no replacement.
     * It will be removed in a future
     * version of the Java Servlet API.
     */
    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name a string specifying the name of the object
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return the object with the specified name
     */
    @Override
    public Object getAttribute(String name) {
        this.checkValid();
        this.updateLastAccess();
        return this.attributes.get(name);
    }

    /**
     * @param name a string specifying the name of the object
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return the object with the specified name
     * @deprecated As of Version 2.2, this method is
     * replaced by {@link #getAttribute}.
     */
    @Override
    public Object getValue(String name) {
        return this.getAttribute(name);
    }

    /**
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session.
     *
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return an <code>Enumeration</code> of
     * <code>String</code> objects specifying the
     * names of all the objects bound to
     * this session
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        this.checkValid();
        this.updateLastAccess();
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @return an array of <code>String</code>
     * objects specifying the
     * names of all the objects bound to
     * this session
     * @deprecated As of Version 2.2, this method is
     * replaced by {@link #getAttributeNames}
     */
    @Override
    public String[] getValueNames() {
        this.checkValid();
        this.updateLastAccess();
        return this.attributes.keySet().toArray(new String[0]);
    }

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     * <p>
     * <p>After this method executes, and if the new object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls
     * <code>HttpSessionBindingListener.valueBound</code>. The container then
     * notifies any <code>HttpSessionAttributeListener</code>s in the web
     * application.
     * <p>
     * <p>If an object was already bound to this session of this name
     * that implements <code>HttpSessionBindingListener</code>, its
     * <code>HttpSessionBindingListener.valueUnbound</code> method is called.
     * <p>
     * <p>If the value passed in is null, this has the same effect as calling
     * <code>removeAttribute()</code>.
     *
     * @param name  the name to which the object is bound;
     *              cannot be null
     * @param value the object to be bound
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     */
    @Override
    public void setAttribute(String name, Object value) {
        this.checkValid();
        this.updateLastAccess();
        this.attributes.put(name, value);
    }

    /**
     * @param name  the name to which the object is bound;
     *              cannot be null
     * @param value the object to be bound; cannot be null
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @deprecated As of Version 2.2, this method is
     * replaced by {@link #setAttribute}
     */
    @Override
    public void putValue(String name, Object value) {
        this.setAttribute(name, value);
    }

    /**
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     * <p>
     * <p>After this method executes, and if the object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls
     * <code>HttpSessionBindingListener.valueUnbound</code>. The container
     * then notifies any <code>HttpSessionAttributeListener</code>s in the web
     * application.
     *
     * @param name the name of the object to
     *             remove from this session
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     */
    @Override
    public void removeAttribute(String name) {
        this.checkValid();
        this.updateLastAccess();
        this.attributes.remove(name);
    }

    /**
     * @param name the name of the object to
     *             remove from this session
     * @throws IllegalStateException if this method is called on an
     *                               invalidated session
     * @deprecated As of Version 2.2, this method is
     * replaced by {@link #removeAttribute}
     */
    @Override
    public void removeValue(String name) {
        this.checkValid();
        this.removeAttribute(name);
    }

    /**
     * Invalidates this session then unbinds any objects bound
     * to it.
     *
     * @throws IllegalStateException if this method is called on an
     *                               already invalidated session
     */
    @Override
    public void invalidate() {
        this.checkValid();
        this.invalidated = true;
    }

    /**
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and
     * the client had disabled the use of cookies, then a session would
     * be new on each request.
     *
     * @return <code>true</code> if the
     * server has created a session,
     * but the client has not yet joined
     * @throws IllegalStateException if this method is called on an
     *                               already invalidated session
     */
    @Override
    public boolean isNew() {
        return (this.lastAccess == this.created);
    }

    private void updateLastAccess() {
        this.lastAccess = System.currentTimeMillis();
    }

    public boolean isValid() {
        if (invalidated || this.timeout < 0) {
            return false;
        }
        if (this.timeout == 0) {
            return true;
        }
        return (this.lastAccess + this.timeout * 1000 > System.currentTimeMillis());
    }

    public void checkValid() {
        if (!this.isValid()) {
            throw new IllegalStateException();
        }
    }
}

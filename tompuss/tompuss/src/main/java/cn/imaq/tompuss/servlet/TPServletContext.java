package cn.imaq.tompuss.servlet;

import cn.imaq.autumn.cpscan.AutumnClasspathScan;
import cn.imaq.autumn.cpscan.ScanResult;
import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.dispatcher.TPNamedDispatcher;
import cn.imaq.tompuss.dispatcher.TPPathDispatcher;
import cn.imaq.tompuss.dispatcher.TPRequestDispatcher;
import cn.imaq.tompuss.filter.TPFilterChain;
import cn.imaq.tompuss.filter.TPFilterMapping;
import cn.imaq.tompuss.filter.TPFilterRegistration;
import cn.imaq.tompuss.session.TPSessionContext;
import cn.imaq.tompuss.util.TPMatchResult;
import cn.imaq.tompuss.util.TPPathUtil;
import cn.imaq.tompuss.util.TPUrlPattern;
import cn.imaq.tompuss.util.TPXmlUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jasper.servlet.JspServlet;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class TPServletContext implements ServletContext {
    public static final String DEFAULT_SERVLET = "default";

    @Getter
    private TPEngine engine;
    private String appName;
    private String contextPath;
    private File resourceRoot;
    private volatile boolean started = false;

    @Getter
    private TPSessionContext sessionContext = new TPSessionContext(this);
    private int sessionTimeout;
    private String requestEncoding = "utf-8";
    private String responseEncoding = "utf-8";
    private Map<String, String> initParams = new ConcurrentHashMap<>();
    private Map<String, Object> attributes = new ConcurrentHashMap<>();
    private Map<String, TPServletRegistration> servletRegistrations = new ConcurrentHashMap<>();
    private Map<TPUrlPattern, TPServletRegistration> servletMappings = new ConcurrentHashMap<>();
    private Map<String, TPFilterRegistration> filterRegistrations = new ConcurrentHashMap<>();
    private Deque<TPFilterMapping> filterMappings = new ConcurrentLinkedDeque<>();
    private Map<Class<? extends EventListener>, Queue<EventListener>> listeners = new ConcurrentHashMap<>();

    public TPServletContext(TPEngine engine, String appName, String contextPath, File resourceRoot) {
        this.engine = engine;
        this.appName = appName;
        this.contextPath = TPPathUtil.transform(contextPath);
        this.resourceRoot = resourceRoot;
        this.addServlet(DEFAULT_SERVLET, TPDefaultServlet.class);
    }

    public synchronized void loadConfigFile(String fileName) {
        TPXmlUtil.parseWebXml(this, new File(this.resourceRoot, fileName));
    }

    @SuppressWarnings("unchecked")
    public synchronized void scanAnnotations() {
        log.info("Scanning annotations ...");
        ScanResult result = AutumnClasspathScan.getGlobalScanResult();
        result.getClassesWithAnnotation(WebServlet.class).forEach(cls -> {
            if (HttpServlet.class.isAssignableFrom(cls)) {
                WebServlet ws = cls.getAnnotation(WebServlet.class);
                TPServletRegistration registration = this.addServlet(
                        ws.name().isEmpty() ? cls.getName() : ws.name(), (Class<? extends Servlet>) cls);
                registration.loadAnnotation(ws);
            }
        });
        result.getClassesWithAnnotation(WebFilter.class).forEach(cls -> {
            if (HttpFilter.class.isAssignableFrom(cls)) {
                WebFilter wf = cls.getAnnotation(WebFilter.class);
                TPFilterRegistration registration = this.addFilter(
                        wf.filterName().isEmpty() ? cls.getName() : wf.filterName(), (Class<? extends Filter>) cls);
                registration.loadAnnotation(wf);
            }
        });
        result.getClassesWithAnnotation(WebListener.class).forEach(cls -> {
            if (EventListener.class.isAssignableFrom(cls)) {
                this.addListener((Class<? extends EventListener>) cls);
            }
        });
    }

    public synchronized void enableJsp() {
        log.info("Enabling JSP support ...");
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
        this.addServlet("JspServlet", JspServlet.class).addMapping("*.jsp");
    }

    public synchronized void startup() {
        log.info("Starting up context ...");
        if (!this.started) {
            this.getListeners(ServletContextListener.class).forEach(x -> x.contextInitialized(new ServletContextEvent(this)));
            Map<Integer, List<TPServletRegistration>> servletStartupMap = new TreeMap<>();
            for (TPServletRegistration servletRegistration : this.servletRegistrations.values()) {
                if (servletRegistration.getLoadOnStartup() >= 0) {
                    servletStartupMap.computeIfAbsent(servletRegistration.getLoadOnStartup(), x -> new LinkedList<>())
                            .add(servletRegistration);
                }
            }
            for (List<TPServletRegistration> servletRegistrations : servletStartupMap.values()) {
                for (TPServletRegistration servletRegistration : servletRegistrations) {
                    servletRegistration.getServletInstance();
                }
            }
            this.started = true;
        }
    }

    public TPMatchResult<TPServletRegistration> matchServletByPath(String path) {
        TPServletRegistration result = null;
        TPUrlPattern.Match bestMatch = TPUrlPattern.Match.NO_MATCH;
        for (Map.Entry<TPUrlPattern, TPServletRegistration> mapEntry : this.servletMappings.entrySet()) {
            TPUrlPattern.Match match = mapEntry.getKey().match(path);
            if (match.compareTo(bestMatch) > 0) {
                bestMatch = match;
                result = mapEntry.getValue();
            }
        }
        if (result != null) {
            return new TPMatchResult<>(bestMatch.getMatched(), result);
        } else {
            return null;
        }
    }

    public TPFilterChain matchFilters(String path, Servlet servlet, DispatcherType dispatcherType) {
        Set<TPFilterRegistration> filters = new LinkedHashSet<>();
        for (TPFilterMapping mapping : filterMappings) {
            if (mapping.getDispatcherTypes().contains(dispatcherType) && mapping.match(path, servlet.getServletConfig().getServletName())) {
                filters.add(mapping.getRegistration());
            }
        }
        return new TPFilterChain(filters, servlet);
    }

    /**
     * Returns the context path of the web application.
     * <p>
     * <p>The context path is the portion of the request URI that is used
     * to select the context of the request. The context path always comes
     * first in a request URI. If this context is the "root" context
     * rooted at the base of the Web server's URL name space, this path
     * will be an empty string. Otherwise, if the context is not rooted at
     * the root of the server's name space, the path starts with a /
     * character but does not end with a / character.
     * <p>
     * <p>It is possible that a servlet container may match a context by
     * more than one context path. In such cases the
     * {@link HttpServletRequest#getContextPath()}
     * will return the actual context path used by the request and it may
     * differ from the path returned by this method.
     * The context path returned by this method should be considered as the
     * prime or preferred context path of the application.
     *
     * @return The context path of the web application, or "" for the
     * root context
     * @see HttpServletRequest#getContextPath()
     * @since Servlet 2.5
     */
    @Override
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns a <code>ServletContext</code> object that
     * corresponds to a specified URL on the server.
     * <p>
     * <p>This method allows servlets to gain
     * access to the context for various parts of the server, and as
     * needed obtain {@link RequestDispatcher} objects from the context.
     * The given path must be begin with <tt>/</tt>, is interpreted relative
     * to the server's document root and is matched against the context
     * roots of other web applications hosted on this container.
     * <p>
     * <p>In a security conscious environment, the servlet container may
     * return <code>null</code> for a given URL.
     *
     * @param uripath a <code>String</code> specifying the context path of
     *                another web application in the container.
     * @return the <code>ServletContext</code> object that
     * corresponds to the named URL, or null if either
     * none exists or the container wishes to restrict
     * this access.
     * @see RequestDispatcher
     */
    @Override
    public ServletContext getContext(String uripath) {
        TPMatchResult<TPServletContext> result = engine.matchContextByPath(uripath);
        if (result == null) {
            return null;
        }
        return result.getObject();
    }

    /**
     * Returns the major version of the Servlet API that this
     * servlet container supports. All implementations that comply
     * with Version 4.0 must have this method return the integer 4.
     *
     * @return 4
     */
    @Override
    public int getMajorVersion() {
        return 4;
    }

    /**
     * Returns the minor version of the Servlet API that this
     * servlet container supports. All implementations that comply
     * with Version 4.0 must have this method return the integer 0.
     *
     * @return 0
     */
    @Override
    public int getMinorVersion() {
        return 0;
    }

    /**
     * Gets the major version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     * <p>
     * <p>The value returned may be different from {@link #getMajorVersion},
     * which returns the major version of the Servlet specification
     * supported by the Servlet container.
     *
     * @return the major version of the Servlet specification that the
     * application represented by this ServletContext is based on
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public int getEffectiveMajorVersion() {
        return this.getMajorVersion();
    }

    /**
     * Gets the minor version of the Servlet specification that the
     * application represented by this ServletContext is based on.
     * <p>
     * <p>The value returned may be different from {@link #getMinorVersion},
     * which returns the minor version of the Servlet specification
     * supported by the Servlet container.
     *
     * @return the minor version of the Servlet specification that the
     * application represented by this ServletContext is based on
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public int getEffectiveMinorVersion() {
        return this.getMinorVersion();
    }

    /**
     * Returns the MIME type of the specified file, or <code>null</code> if
     * the MIME type is not known. The MIME type is determined
     * by the configuration of the servlet container, and may be specified
     * in a web application deployment descriptor. Common MIME
     * types include <code>text/html</code> and <code>image/gif</code>.
     *
     * @param file a <code>String</code> specifying the name of a file
     * @return a <code>String</code> specifying the file's MIME type
     */
    @Override
    public String getMimeType(String file) {
        try {
            return Files.probeContentType(Paths.get(new File(resourceRoot, file).toURI()));
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /**
     * Returns a directory-like listing of all the paths to resources
     * within the web application whose longest sub-path matches the
     * supplied path argument.
     * <p>
     * <p>Paths indicating subdirectory paths end with a <tt>/</tt>.
     * <p>
     * <p>The returned paths are all relative to the root of the web
     * application, or relative to the <tt>/META-INF/resources</tt>
     * directory of a JAR file inside the web application's
     * <tt>/WEB-INF/lib</tt> directory, and have a leading <tt>/</tt>.
     * <p>
     * <p>The returned set is not backed by the {@code ServletContext} object,
     * so changes in the returned set are not reflected in the
     * {@code ServletContext} object, and vice-versa.</p>
     * <p>
     * <p>For example, for a web application containing:
     * <p>
     * <pre>{@code
     *   /welcome.html
     *   /catalog/index.html
     *   /catalog/products.html
     *   /catalog/offers/books.html
     *   /catalog/offers/music.html
     *   /customer/login.jsp
     *   /WEB-INF/web.xml
     *   /WEB-INF/classes/com.acme.OrderServlet.class
     *   /WEB-INF/lib/catalog.jar!/META-INF/resources/catalog/moreOffers/books.html
     * }</pre>
     * <p>
     * <tt>getResourcePaths("/")</tt> would return
     * <tt>{"/welcome.html", "/catalog/", "/customer/", "/WEB-INF/"}</tt>,
     * and <tt>getResourcePaths("/catalog/")</tt> would return
     * <tt>{"/catalog/index.html", "/catalog/products.html",
     * "/catalog/offers/", "/catalog/moreOffers/"}</tt>.
     *
     * @param path the partial path used to match the resources,
     *             which must start with a <tt>/</tt>
     * @return a Set containing the directory listing, or null if there
     * are no resources in the web application whose path
     * begins with the supplied path.
     * @since Servlet 2.3
     */
    @Override
    public Set<String> getResourcePaths(String path) {
        // TODO static
        return null;
    }

    /**
     * Returns a URL to the resource that is mapped to the given path.
     * <p>
     * <p>The path must begin with a <tt>/</tt> and is interpreted
     * as relative to the current context root,
     * or relative to the <tt>/META-INF/resources</tt> directory
     * of a JAR file inside the web application's <tt>/WEB-INF/lib</tt>
     * directory.
     * This method will first search the document root of the
     * web application for the requested resource, before searching
     * any of the JAR files inside <tt>/WEB-INF/lib</tt>.
     * The order in which the JAR files inside <tt>/WEB-INF/lib</tt>
     * are searched is undefined.
     * <p>
     * <p>This method allows the servlet container to make a resource
     * available to servlets from any source. Resources
     * can be located on a local or remote
     * file system, in a database, or in a <code>.war</code> file.
     * <p>
     * <p>The servlet container must implement the URL handlers
     * and <code>URLConnection</code> objects that are necessary
     * to access the resource.
     * <p>
     * <p>This method returns <code>null</code>
     * if no resource is mapped to the pathname.
     * <p>
     * <p>Some containers may allow writing to the URL returned by
     * this method using the methods of the URL class.
     * <p>
     * <p>The resource content is returned directly, so be aware that
     * requesting a <code>.jsp</code> page returns the JSP source code.
     * Use a <code>RequestDispatcher</code> instead to include results of
     * an execution.
     * <p>
     * <p>This method has a different purpose than
     * <code>java.lang.Class.getResource</code>,
     * which looks up resources based on a class loader. This
     * method does not use class loaders.
     *
     * @param path a <code>String</code> specifying
     *             the path to the resource
     * @return the resource located at the named path,
     * or <code>null</code> if there is no resource at that path
     * @throws MalformedURLException if the pathname is not given in
     *                               the correct form
     */
    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (resourceRoot == null) {
            return null;
        }
        File resFile = new File(resourceRoot, path);
        if (resFile.exists()) {
            return resFile.toURI().toURL();
        }
        return null;
    }

    /**
     * Returns the resource located at the named path as
     * an <code>InputStream</code> object.
     * <p>
     * <p>The data in the <code>InputStream</code> can be
     * of any type or length. The path must be specified according
     * to the rules given in <code>getResource</code>.
     * This method returns <code>null</code> if no resource exists at
     * the specified path.
     * <p>
     * <p>Meta-information such as content length and content type
     * that is available via <code>getResource</code>
     * method is lost when using this method.
     * <p>
     * <p>The servlet container must implement the URL handlers
     * and <code>URLConnection</code> objects necessary to access
     * the resource.
     * <p>
     * <p>This method is different from
     * <code>java.lang.Class.getResourceAsStream</code>,
     * which uses a class loader. This method allows servlet containers
     * to make a resource available
     * to a servlet from any location, without using a class loader.
     *
     * @param path a <code>String</code> specifying the path
     *             to the resource
     * @return the <code>InputStream</code> returned to the
     * servlet, or <code>null</code> if no resource
     * exists at the specified path
     */
    @Override
    public InputStream getResourceAsStream(String path) {
        if (resourceRoot == null) {
            return null;
        }
        try {
            return new FileInputStream(new File(resourceRoot, path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns a {@link RequestDispatcher} object that acts
     * as a wrapper for the resource located at the given path.
     * A <code>RequestDispatcher</code> object can be used to forward
     * a request to the resource or to include the resource in a response.
     * The resource can be dynamic or static.
     * <p>
     * <p>The pathname must begin with a <tt>/</tt> and is interpreted as
     * relative to the current context root.  Use <code>getContext</code>
     * to obtain a <code>RequestDispatcher</code> for resources in foreign
     * contexts.
     * <p>
     * <p>This method returns <code>null</code> if the
     * <code>ServletContext</code> cannot return a
     * <code>RequestDispatcher</code>.
     *
     * @param path a <code>String</code> specifying the pathname
     *             to the resource
     * @return a <code>RequestDispatcher</code> object
     * that acts as a wrapper for the resource
     * at the specified path, or <code>null</code> if
     * the <code>ServletContext</code> cannot return
     * a <code>RequestDispatcher</code>
     * @see RequestDispatcher
     * @see ServletContext#getContext
     */
    @Override
    public TPRequestDispatcher getRequestDispatcher(String path) {
        return new TPPathDispatcher(this, path);
    }

    /**
     * Returns a {@link RequestDispatcher} object that acts
     * as a wrapper for the named servlet.
     * <p>
     * <p>Servlets (and JSP pages also) may be given names via server
     * administration or via a web application deployment descriptor.
     * A servlet instance can determine its name using
     * {@link ServletConfig#getServletName}.
     * <p>
     * <p>This method returns <code>null</code> if the
     * <code>ServletContext</code>
     * cannot return a <code>RequestDispatcher</code> for any reason.
     *
     * @param name a <code>String</code> specifying the name
     *             of a servlet to wrap
     * @return a <code>RequestDispatcher</code> object
     * that acts as a wrapper for the named servlet,
     * or <code>null</code> if the <code>ServletContext</code>
     * cannot return a <code>RequestDispatcher</code>
     * @see RequestDispatcher
     * @see ServletContext#getContext
     * @see ServletConfig#getServletName
     */
    @Override
    public TPRequestDispatcher getNamedDispatcher(String name) {
        TPServletRegistration servletRegistration = this.servletRegistrations.get(name);
        if (servletRegistration == null) {
            return null;
        }
        return new TPNamedDispatcher(this, servletRegistration);
    }

    public TPRequestDispatcher getDefaultDispatcher() {
        return this.getNamedDispatcher(DEFAULT_SERVLET);
    }

    /**
     * @param name the servlet name
     * @return the {@code javax.servlet.Servlet Servlet} with the given name
     * @throws ServletException if an exception has occurred that interfaces
     *                          with servlet's normal operation
     * @deprecated As of Java Servlet API 2.1, with no direct replacement.
     * <p>
     * <p>This method was originally defined to retrieve a servlet
     * from a <code>ServletContext</code>. In this version, this method
     * always returns <code>null</code> and remains only to preserve
     * binary compatibility. This method will be permanently removed
     * in a future version of the Java Servlet API.
     * <p>
     * <p>In lieu of this method, servlets can share information using the
     * <code>ServletContext</code> class and can perform shared business logic
     * by invoking methods on common non-servlet classes.
     */
    @Override
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    /**
     * @return an <code>Enumeration</code> of {@code javax.servlet.Servlet Servlet}
     * @deprecated As of Java Servlet API 2.0, with no replacement.
     * <p>
     * <p>This method was originally defined to return an
     * <code>Enumeration</code> of all the servlets known to this servlet
     * context.
     * In this version, this method always returns an empty enumeration and
     * remains only to preserve binary compatibility. This method
     * will be permanently removed in a future version of the Java
     * Servlet API.
     */
    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }

    /**
     * @return an <code>Enumeration</code> of {@code javax.servlet.Servlet Servlet} names
     * @deprecated As of Java Servlet API 2.1, with no replacement.
     * <p>
     * <p>This method was originally defined to return an
     * <code>Enumeration</code>
     * of all the servlet names known to this context. In this version,
     * this method always returns an empty <code>Enumeration</code> and
     * remains only to preserve binary compatibility. This method will
     * be permanently removed in a future version of the Java Servlet API.
     */
    @Override
    public Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }

    /**
     * Writes the specified message to a servlet log file, usually
     * an event log. The name and type of the servlet log file is
     * specific to the servlet container.
     *
     * @param msg a <code>String</code> specifying the
     *            message to be written to the log file
     */
    @Override
    public void log(String msg) {
        log.info(msg);
    }

    /**
     * @param exception the <code>Exception</code> error
     * @param msg       a <code>String</code> that describes the exception
     * @deprecated As of Java Servlet API 2.1, use
     * {@link #log(String message, Throwable throwable)}
     * instead.
     * <p>
     * <p>This method was originally defined to write an
     * exception's stack trace and an explanatory error message
     * to the servlet log file.
     */
    @Override
    public void log(Exception exception, String msg) {
        this.log(msg, exception);
    }

    /**
     * Writes an explanatory message and a stack trace
     * for a given <code>Throwable</code> exception
     * to the servlet log file. The name and type of the servlet log
     * file is specific to the servlet container, usually an event log.
     *
     * @param message   a <code>String</code> that
     *                  describes the error or exception
     * @param throwable the <code>Throwable</code> error
     */
    @Override
    public void log(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * Gets the <i>real</i> path corresponding to the given
     * <i>virtual</i> path.
     * <p>
     * <p>For example, if <tt>path</tt> is equal to <tt>/index.html</tt>,
     * this method will return the absolute file path on the server's
     * filesystem to which a request of the form
     * <tt>http://&lt;host&gt;:&lt;port&gt;/&lt;contextPath&gt;/index.html</tt>
     * would be mapped, where <tt>&lt;contextPath&gt;</tt> corresponds to the
     * context path of this ServletContext.
     * <p>
     * <p>The real path returned will be in a form
     * appropriate to the computer and operating system on
     * which the servlet container is running, including the
     * proper path separators.
     * <p>
     * <p>Resources inside the <tt>/META-INF/resources</tt>
     * directories of JAR files bundled in the application's
     * <tt>/WEB-INF/lib</tt> directory must be considered only if the
     * container has unpacked them from their containing JAR file, in
     * which case the path to the unpacked location must be returned.
     * <p>
     * <p>This method returns <code>null</code> if the servlet container
     * is unable to translate the given <i>virtual</i> path to a
     * <i>real</i> path.
     *
     * @param path the <i>virtual</i> path to be translated to a
     *             <i>real</i> path
     * @return the <i>real</i> path, or <tt>null</tt> if the
     * translation cannot be performed
     */
    @Override
    public String getRealPath(String path) {
        // TODO static
        return null;
    }

    /**
     * Returns the name and version of the servlet container on which
     * the servlet is running.
     * <p>
     * <p>The form of the returned string is
     * <i>servername</i>/<i>versionnumber</i>.
     * For example, the JavaServer Web Development Kit may return the string
     * <code>JavaServer Web Dev Kit/1.0</code>.
     * <p>
     * <p>The servlet container may return other optional information
     * after the primary string in parentheses, for example,
     * <code>JavaServer Web Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86)</code>.
     *
     * @return a <code>String</code> containing at least the
     * servlet container name and version number
     */
    @Override
    public String getServerInfo() {
        return "TomPuss/1.0";
    }

    /**
     * Returns a <code>String</code> containing the value of the named
     * context-wide initialization parameter, or <code>null</code> if
     * the parameter does not exist.
     * <p>
     * <p>This method can make available configuration information useful
     * to an entire web application.  For example, it can provide a
     * webmaster's email address or the name of a system that holds
     * critical data.
     *
     * @param name a <code>String</code> containing the name of the
     *             parameter whose value is requested
     * @return a <code>String</code> containing the value of the
     * context's initialization parameter, or <code>null</code> if the
     * context's initialization parameter does not exist.
     * @throws NullPointerException if the argument {@code name} is
     *                              {@code null}
     * @see ServletConfig#getInitParameter
     */
    @Override
    public String getInitParameter(String name) {
        return this.initParams.get(name);
    }

    /**
     * Returns the names of the context's initialization parameters as an
     * <code>Enumeration</code> of <code>String</code> objects, or an
     * empty <code>Enumeration</code> if the context has no initialization
     * parameters.
     *
     * @return an <code>Enumeration</code> of <code>String</code>
     * objects containing the names of the context's
     * initialization parameters
     * @see ServletConfig#getInitParameter
     */
    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParams.keySet());
    }

    /**
     * Sets the context initialization parameter with the given name and
     * value on this ServletContext.
     *
     * @param name  the name of the context initialization parameter to set
     * @param value the value of the context initialization parameter to set
     * @return true if the context initialization parameter with the given
     * name and value was set successfully on this ServletContext, and false
     * if it was not set because this ServletContext already contains a
     * context initialization parameter with a matching name
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws NullPointerException          if the name parameter is {@code null}
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
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
     * Returns the servlet container attribute with the given name, or
     * <code>null</code> if there is no attribute by that name.
     * <p>
     * <p>An attribute allows a servlet container to give the
     * servlet additional information not
     * already provided by this interface. See your
     * server documentation for information about its attributes.
     * A list of supported attributes can be retrieved using
     * <code>getAttributeNames</code>.
     * <p>
     * <p>The attribute is returned as a <code>java.lang.Object</code>
     * or some subclass.
     * <p>
     * <p>Attribute names should follow the same convention as package
     * names. The Java Servlet API specification reserves names
     * matching <code>java.*</code>, <code>javax.*</code>,
     * and <code>sun.*</code>.
     *
     * @param name a <code>String</code> specifying the name
     *             of the attribute
     * @return an <code>Object</code> containing the value of the
     * attribute, or <code>null</code> if no attribute
     * exists matching the given name.
     * @throws NullPointerException if the argument {@code name} is
     *                              {@code null}
     * @see ServletContext#getAttributeNames
     */
    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /**
     * Returns an <code>Enumeration</code> containing the
     * attribute names available within this ServletContext.
     * <p>
     * <p>Use the {@link #getAttribute} method with an attribute name
     * to get the value of an attribute.
     *
     * @return an <code>Enumeration</code> of attribute
     * names
     * @see #getAttribute
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * Binds an object to a given attribute name in this ServletContext. If
     * the name specified is already used for an attribute, this
     * method will replace the attribute with the new to the new attribute.
     * <p>If listeners are configured on the <code>ServletContext</code> the
     * container notifies them accordingly.
     * <p>
     * If a null value is passed, the effect is the same as calling
     * <code>removeAttribute()</code>.
     * <p>
     * <p>Attribute names should follow the same convention as package
     * names. The Java Servlet API specification reserves names
     * matching <code>java.*</code>, <code>javax.*</code>, and
     * <code>sun.*</code>.
     *
     * @param name   a <code>String</code> specifying the name
     *               of the attribute
     * @param object an <code>Object</code> representing the
     *               attribute to be bound
     * @throws NullPointerException if the name parameter is {@code null}
     */
    @Override
    public void setAttribute(String name, Object object) {
        if (this.attributes.containsKey(name)) {
            this.getListeners(ServletContextAttributeListener.class).forEach(x ->
                    x.attributeReplaced(new ServletContextAttributeEvent(this, name, object))
            );
        } else {
            this.getListeners(ServletContextAttributeListener.class).forEach(x ->
                    x.attributeAdded(new ServletContextAttributeEvent(this, name, object))
            );
        }
        this.attributes.put(name, object);
    }

    /**
     * Removes the attribute with the given name from
     * this ServletContext. After removal, subsequent calls to
     * {@link #getAttribute} to retrieve the attribute's value
     * will return <code>null</code>.
     * <p>
     * <p>If listeners are configured on the <code>ServletContext</code> the
     * container notifies them accordingly.
     *
     * @param name a <code>String</code> specifying the name
     *             of the attribute to be removed
     */
    @Override
    public void removeAttribute(String name) {
        this.getListeners(ServletContextAttributeListener.class).forEach(x ->
                x.attributeRemoved(new ServletContextAttributeEvent(this, name, this.attributes.get(name)))
        );
        this.attributes.remove(name);
    }

    /**
     * Returns the name of this web application corresponding to this
     * ServletContext as specified in the deployment descriptor for this
     * web application by the display-name element.
     *
     * @return The name of the web application or null if no name has been
     * declared in the deployment descriptor.
     * @since Servlet 2.3
     */
    @Override
    public String getServletContextName() {
        return this.appName;
    }

    /**
     * Adds the servlet with the given name and class name to this servlet
     * context.
     * <p>
     * <p>The registered servlet may be further configured via the returned
     * {@link ServletRegistration} object.
     * <p>
     * <p>The specified <tt>className</tt> will be loaded using the
     * classloader associated with the application represented by this
     * ServletContext.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * ServletRegistration for a servlet with the given <tt>servletName</tt>,
     * it will be completed (by assigning the given <tt>className</tt> to it)
     * and returned.
     * <p>
     * <p>This method introspects the class with the given <tt>className</tt>
     * for the {@link ServletSecurity},
     * {@link MultipartConfig},
     * <tt>javax.annotation.security.RunAs</tt>, and
     * <tt>javax.annotation.security.DeclareRoles</tt> annotations.
     * In addition, this method supports resource injection if the
     * class with the given <tt>className</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param servletName the name of the servlet
     * @param className   the fully qualified class name of the servlet
     * @return a ServletRegistration object that may be used to further
     * configure the registered servlet, or <tt>null</tt> if this
     * ServletContext already contains a complete ServletRegistration for
     * a servlet with the given <tt>servletName</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>servletName</code> is null
     *                                       or an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public TPServletRegistration addServlet(String servletName, String className) {
        try {
            Class<?> servletClass = Class.forName(className);
            return this.addServlet(servletName, (Class<? extends Servlet>) servletClass);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registers the given servlet instance with this ServletContext
     * under the given <tt>servletName</tt>.
     * <p>
     * <p>The registered servlet may be further configured via the returned
     * {@link ServletRegistration} object.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * ServletRegistration for a servlet with the given <tt>servletName</tt>,
     * it will be completed (by assigning the class name of the given servlet
     * instance to it) and returned.
     *
     * @param servletName the name of the servlet
     * @param servlet     the servlet instance to register
     * @return a ServletRegistration object that may be used to further
     * configure the given servlet, or <tt>null</tt> if this
     * ServletContext already contains a complete ServletRegistration for a
     * servlet with the given <tt>servletName</tt> or if the same servlet
     * instance has already been registered with this or another
     * ServletContext in the same container
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @throws IllegalArgumentException      if the given servlet instance
     *                                       implements {@link SingleThreadModel}, or <code>servletName</code> is null
     *                                       or an empty String
     * @since Servlet 3.0
     */
    @Override
    public TPServletRegistration addServlet(String servletName, Servlet servlet) {
        if (servletName == null || servletName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        log.info("Adding Servlet {}[{}]", servletName, servlet.getClass().getName());
        TPServletRegistration registration = new TPServletRegistration(this, servletName, servlet);
        this.servletRegistrations.put(servletName, registration);
        return registration;
    }

    /**
     * Adds the servlet with the given name and class type to this servlet
     * context.
     * <p>
     * <p>The registered servlet may be further configured via the returned
     * {@link ServletRegistration} object.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * ServletRegistration for a servlet with the given <tt>servletName</tt>,
     * it will be completed (by assigning the name of the given
     * <tt>servletClass</tt> to it) and returned.
     * <p>
     * <p>This method introspects the given <tt>servletClass</tt> for
     * the {@link ServletSecurity},
     * {@link MultipartConfig},
     * <tt>javax.annotation.security.RunAs</tt>, and
     * <tt>javax.annotation.security.DeclareRoles</tt> annotations.
     * In addition, this method supports resource injection if the
     * given <tt>servletClass</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param servletName  the name of the servlet
     * @param servletClass the class object from which the servlet will be
     *                     instantiated
     * @return a ServletRegistration object that may be used to further
     * configure the registered servlet, or <tt>null</tt> if this
     * ServletContext already contains a complete ServletRegistration for
     * the given <tt>servletName</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>servletName</code> is null
     *                                       or an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public TPServletRegistration addServlet(String servletName, Class<? extends Servlet> servletClass) {
        try {
            return this.addServlet(servletName, this.createServlet(servletClass));
        } catch (ServletException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean addServletMapping(String pattern, TPServletRegistration registration) {
        return (this.servletMappings.putIfAbsent(new TPUrlPattern(pattern), registration) == null);
    }

    /**
     * Adds the servlet with the given jsp file to this servlet context.
     * <p>
     * <p>The registered servlet may be further configured via the returned
     * {@link ServletRegistration} object.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * ServletRegistration for a servlet with the given <tt>servletName</tt>,
     * it will be completed (by assigning the given <tt>jspFile</tt> to it)
     * and returned.
     *
     * @param servletName the name of the servlet
     * @param jspFile     the full path to a JSP file within the web application
     *                    beginning with a `/'.
     * @return a ServletRegistration object that may be used to further
     * configure the registered servlet, or <tt>null</tt> if this
     * ServletContext already contains a complete ServletRegistration for
     * a servlet with the given <tt>servletName</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>servletName</code> is null
     *                                       or an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        // TODO jsp
        return null;
    }

    /**
     * Instantiates the given Servlet class.
     * <p>
     * <p>The returned Servlet instance may be further customized before it
     * is registered with this ServletContext via a call to
     * {@link #addServlet(String, Servlet)}.
     * <p>
     * <p>The given Servlet class must define a zero argument constructor,
     * which is used to instantiate it.
     * <p>
     * <p>This method introspects the given <tt>clazz</tt> for
     * the following annotations:
     * {@link ServletSecurity},
     * {@link MultipartConfig},
     * <tt>javax.annotation.security.RunAs</tt>, and
     * <tt>javax.annotation.security.DeclareRoles</tt>.
     * In addition, this method supports resource injection if the
     * given <tt>clazz</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param clazz the Servlet class to instantiate
     * @return the new Servlet instance
     * @throws ServletException              if the given <tt>clazz</tt> fails to be
     *                                       instantiated
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Gets the ServletRegistration corresponding to the servlet with the
     * given <tt>servletName</tt>.
     *
     * @param servletName the name of a servlet
     * @return the (complete or preliminary) ServletRegistration for the
     * servlet with the given <tt>servletName</tt>, or null if no
     * ServletRegistration exists under that name
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return this.servletRegistrations.get(servletName);
    }

    /**
     * Gets a (possibly empty) Map of the ServletRegistration
     * objects (keyed by servlet name) corresponding to all servlets
     * registered with this ServletContext.
     * <p>
     * <p>The returned Map includes the ServletRegistration objects
     * corresponding to all declared and annotated servlets, as well as the
     * ServletRegistration objects corresponding to all servlets that have
     * been added via one of the <tt>addServlet</tt> and <tt>addJspFile</tt>
     * methods.
     * <p>
     * <p>If permitted, any changes to the returned Map must not affect this
     * ServletContext.
     *
     * @return Map of the (complete and preliminary) ServletRegistration
     * objects corresponding to all servlets currently registered with this
     * ServletContext
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.unmodifiableMap(this.servletRegistrations);
    }

    public TPServletRegistration getDefaultServletRegistration() {
        return this.servletRegistrations.get(DEFAULT_SERVLET);
    }

    /**
     * Adds the filter with the given name and class name to this servlet
     * context.
     * <p>
     * <p>The registered filter may be further configured via the returned
     * {@link FilterRegistration} object.
     * <p>
     * <p>The specified <tt>className</tt> will be loaded using the
     * classloader associated with the application represented by this
     * ServletContext.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * FilterRegistration for a filter with the given <tt>filterName</tt>,
     * it will be completed (by assigning the given <tt>className</tt> to it)
     * and returned.
     * <p>
     * <p>This method supports resource injection if the class with the
     * given <tt>className</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param filterName the name of the filter
     * @param className  the fully qualified class name of the filter
     * @return a FilterRegistration object that may be used to further
     * configure the registered filter, or <tt>null</tt> if this
     * ServletContext already contains a complete FilterRegistration for
     * a filter with the given <tt>filterName</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>filterName</code> is null or
     *                                       an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public TPFilterRegistration addFilter(String filterName, String className) {
        try {
            Class<?> filterClass = Class.forName(className);
            return this.addFilter(filterName, (Class<? extends Filter>) filterClass);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registers the given filter instance with this ServletContext
     * under the given <tt>filterName</tt>.
     * <p>
     * <p>The registered filter may be further configured via the returned
     * {@link FilterRegistration} object.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * FilterRegistration for a filter with the given <tt>filterName</tt>,
     * it will be completed (by assigning the class name of the given filter
     * instance to it) and returned.
     *
     * @param filterName the name of the filter
     * @param filter     the filter instance to register
     * @return a FilterRegistration object that may be used to further
     * configure the given filter, or <tt>null</tt> if this
     * ServletContext already contains a complete FilterRegistration for a
     * filter with the given <tt>filterName</tt> or if the same filter
     * instance has already been registered with this or another
     * ServletContext in the same container
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>filterName</code> is null or
     *                                       an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public TPFilterRegistration addFilter(String filterName, Filter filter) {
        if (filterName == null || filterName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        log.info("Adding Filter {}[{}]", filterName, filter.getClass().getName());
        TPFilterRegistration registration = new TPFilterRegistration(this, filterName, filter);
        this.filterRegistrations.put(filterName, registration);
        return registration;
    }

    /**
     * Adds the filter with the given name and class type to this servlet
     * context.
     * <p>
     * <p>The registered filter may be further configured via the returned
     * {@link FilterRegistration} object.
     * <p>
     * <p>If this ServletContext already contains a preliminary
     * FilterRegistration for a filter with the given <tt>filterName</tt>,
     * it will be completed (by assigning the name of the given
     * <tt>filterClass</tt> to it) and returned.
     * <p>
     * <p>This method supports resource injection if the given
     * <tt>filterClass</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param filterName  the name of the filter
     * @param filterClass the class object from which the filter will be
     *                    instantiated
     * @return a FilterRegistration object that may be used to further
     * configure the registered filter, or <tt>null</tt> if this
     * ServletContext already contains a complete FilterRegistration for a
     * filter with the given <tt>filterName</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws IllegalArgumentException      if <code>filterName</code> is null or
     *                                       an empty String
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public TPFilterRegistration addFilter(String filterName, Class<? extends Filter> filterClass) {
        try {
            return this.addFilter(filterName, this.createFilter(filterClass));
        } catch (ServletException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addFilterMapping(TPFilterMapping mapping, boolean isMatchAfter) {
        if (isMatchAfter) {
            this.filterMappings.addLast(mapping);
        } else {
            this.filterMappings.addFirst(mapping);
        }
    }

    /**
     * Instantiates the given Filter class.
     * <p>
     * <p>The returned Filter instance may be further customized before it
     * is registered with this ServletContext via a call to
     * {@link #addFilter(String, Filter)}.
     * <p>
     * <p>The given Filter class must define a zero argument constructor,
     * which is used to instantiate it.
     * <p>
     * <p>This method supports resource injection if the given
     * <tt>clazz</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param clazz the Filter class to instantiate
     * @return the new Filter instance
     * @throws ServletException              if the given <tt>clazz</tt> fails to be
     *                                       instantiated
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Gets the FilterRegistration corresponding to the filter with the
     * given <tt>filterName</tt>.
     *
     * @param filterName the name of a filter
     * @return the (complete or preliminary) FilterRegistration for the
     * filter with the given <tt>filterName</tt>, or null if no
     * FilterRegistration exists under that name
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return this.filterRegistrations.get(filterName);
    }

    /**
     * Gets a (possibly empty) Map of the FilterRegistration
     * objects (keyed by filter name) corresponding to all filters
     * registered with this ServletContext.
     * <p>
     * <p>The returned Map includes the FilterRegistration objects
     * corresponding to all declared and annotated filters, as well as the
     * FilterRegistration objects corresponding to all filters that have
     * been added via one of the <tt>addFilter</tt> methods.
     * <p>
     * <p>Any changes to the returned Map must not affect this
     * ServletContext.
     *
     * @return Map of the (complete and preliminary) FilterRegistration
     * objects corresponding to all filters currently registered with this
     * ServletContext
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Collections.unmodifiableMap(this.filterRegistrations);
    }

    /**
     * Gets the {@link SessionCookieConfig} object through which various
     * properties of the session tracking cookies created on behalf of this
     * <tt>ServletContext</tt> may be configured.
     * <p>
     * <p>Repeated invocations of this method will return the same
     * <tt>SessionCookieConfig</tt> instance.
     *
     * @return the <tt>SessionCookieConfig</tt> object through which
     * various properties of the session tracking cookies created on
     * behalf of this <tt>ServletContext</tt> may be configured
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        // TODO session
        return null;
    }

    /**
     * Sets the session tracking modes that are to become effective for this
     * <tt>ServletContext</tt>.
     * <p>
     * <p>The given <tt>sessionTrackingModes</tt> replaces any
     * session tracking modes set by a previous invocation of this
     * method on this <tt>ServletContext</tt>.
     *
     * @param sessionTrackingModes the set of session tracking modes to
     *                             become effective for this <tt>ServletContext</tt>
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @throws IllegalArgumentException      if <tt>sessionTrackingModes</tt>
     *                                       specifies a combination of <tt>SessionTrackingMode.SSL</tt> with a
     *                                       session tracking mode other than <tt>SessionTrackingMode.SSL</tt>,
     *                                       or if <tt>sessionTrackingModes</tt> specifies a session tracking mode
     *                                       that is not supported by the servlet container
     * @since Servlet 3.0
     */
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        // TODO session
    }

    /**
     * Gets the session tracking modes that are supported by default for this
     * <tt>ServletContext</tt>.
     * <p>
     * <p>The returned set is not backed by the {@code ServletContext} object,
     * so changes in the returned set are not reflected in the
     * {@code ServletContext} object, and vice-versa.</p>
     *
     * @return set of the session tracking modes supported by default for
     * this <tt>ServletContext</tt>
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Collections.singleton(SessionTrackingMode.COOKIE);
    }

    /**
     * Gets the session tracking modes that are in effect for this
     * <tt>ServletContext</tt>.
     * <p>
     * <p>The session tracking modes in effect are those provided to
     * {@link #setSessionTrackingModes setSessionTrackingModes}.
     * <p>
     * <p>The returned set is not backed by the {@code ServletContext} object,
     * so changes in the returned set are not reflected in the
     * {@code ServletContext} object, and vice-versa.</p>
     *
     * @return set of the session tracking modes in effect for this
     * <tt>ServletContext</tt>
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        // TODO session
        return null;
    }

    /**
     * Adds the listener with the given class name to this ServletContext.
     * <p>
     * <p>The class with the given name will be loaded using the
     * classloader associated with the application represented by this
     * ServletContext, and must implement one or more of the following
     * interfaces:
     * <ul>
     * <li>{@link ServletContextAttributeListener}
     * <li>{@link ServletRequestListener}
     * <li>{@link ServletRequestAttributeListener}
     * <li>{@link HttpSessionAttributeListener}
     * <li>{@link HttpSessionIdListener}
     * <li>{@link HttpSessionListener}
     * </ul>
     * <p>
     * <p>If this ServletContext was passed to
     * {@link ServletContainerInitializer#onStartup}, then the class with
     * the given name may also implement {@link ServletContextListener},
     * in addition to the interfaces listed above.
     * <p>
     * <p>As part of this method call, the container must load the class
     * with the specified class name to ensure that it implements one of
     * the required interfaces.
     * <p>
     * <p>If the class with the given name implements a listener interface
     * whose invocation order corresponds to the declaration order (i.e.,
     * if it implements {@link ServletRequestListener},
     * {@link ServletContextListener}, or
     * {@link HttpSessionListener}),
     * then the new listener will be added to the end of the ordered list of
     * listeners of that interface.
     * <p>
     * <p>This method supports resource injection if the class with the
     * given <tt>className</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param className the fully qualified class name of the listener
     * @throws IllegalArgumentException      if the class with the given name
     *                                       does not implement any of the above interfaces, or if it implements
     *                                       {@link ServletContextListener} and this ServletContext was not
     *                                       passed to {@link ServletContainerInitializer#onStartup}
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public void addListener(String className) {
        try {
            Class<?> listenerClass = Class.forName(className);
            this.addListener((Class<? extends EventListener>) listenerClass);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Adds the given listener to this ServletContext.
     * <p>
     * <p>The given listener must be an instance of one or more of the
     * following interfaces:
     * <ul>
     * <li>{@link ServletContextAttributeListener}
     * <li>{@link ServletRequestListener}
     * <li>{@link ServletRequestAttributeListener}
     * <li>{@link HttpSessionAttributeListener}
     * <li>{@link HttpSessionIdListener}
     * <li>{@link HttpSessionListener}
     * </ul>
     * <p>
     * <p>If this ServletContext was passed to
     * {@link ServletContainerInitializer#onStartup}, then the given
     * listener may also be an instance of {@link ServletContextListener},
     * in addition to the interfaces listed above.
     * <p>
     * <p>If the given listener is an instance of a listener interface whose
     * invocation order corresponds to the declaration order (i.e., if it
     * is an instance of {@link ServletRequestListener},
     * {@link ServletContextListener}, or
     * {@link HttpSessionListener}),
     * then the listener will be added to the end of the ordered list of
     * listeners of that interface.
     *
     * @param t the listener to be added
     * @throws IllegalArgumentException      if the given listener is not
     *                                       an instance of any of the above interfaces, or if it is an instance
     *                                       of {@link ServletContextListener} and this ServletContext was not
     *                                       passed to {@link ServletContainerInitializer#onStartup}
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends EventListener> void addListener(T t) {
        log.info("Adding Listener {}", t.getClass().getName());
        for (Class<?> intf : t.getClass().getInterfaces()) {
            if (EventListener.class.isAssignableFrom(intf)) {
                this.listeners.computeIfAbsent((Class<? extends EventListener>) intf, x -> new ConcurrentLinkedQueue<>()).add(t);
            }
        }
    }

    /**
     * Adds a listener of the given class type to this ServletContext.
     * <p>
     * <p>The given <tt>listenerClass</tt> must implement one or more of the
     * following interfaces:
     * <ul>
     * <li>{@link ServletContextAttributeListener}
     * <li>{@link ServletRequestListener}
     * <li>{@link ServletRequestAttributeListener}
     * <li>{@link HttpSessionAttributeListener}
     * <li>{@link HttpSessionIdListener}
     * <li>{@link HttpSessionListener}
     * </ul>
     * <p>
     * <p>If this ServletContext was passed to
     * {@link ServletContainerInitializer#onStartup}, then the given
     * <tt>listenerClass</tt> may also implement
     * {@link ServletContextListener}, in addition to the interfaces listed
     * above.
     * <p>
     * <p>If the given <tt>listenerClass</tt> implements a listener
     * interface whose invocation order corresponds to the declaration order
     * (i.e., if it implements {@link ServletRequestListener},
     * {@link ServletContextListener}, or
     * {@link HttpSessionListener}),
     * then the new listener will be added to the end of the ordered list
     * of listeners of that interface.
     * <p>
     * <p>This method supports resource injection if the given
     * <tt>listenerClass</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param listenerClass the listener class to be instantiated
     * @throws IllegalArgumentException      if the given <tt>listenerClass</tt>
     *                                       does not implement any of the above interfaces, or if it implements
     *                                       {@link ServletContextListener} and this ServletContext was not passed
     *                                       to {@link ServletContainerInitializer#onStartup}
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.0
     */
    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        try {
            this.addListener(this.createListener(listenerClass));
        } catch (ServletException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Instantiates the given EventListener class.
     * <p>
     * <p>The specified EventListener class must implement at least one of
     * the {@link ServletContextListener},
     * {@link ServletContextAttributeListener},
     * {@link ServletRequestListener},
     * {@link ServletRequestAttributeListener},
     * {@link HttpSessionAttributeListener},
     * {@link HttpSessionIdListener}, or
     * {@link HttpSessionListener}
     * interfaces.
     * <p>
     * <p>The returned EventListener instance may be further customized
     * before it is registered with this ServletContext via a call to
     * {@link #addListener(EventListener)}.
     * <p>
     * <p>The given EventListener class must define a zero argument
     * constructor, which is used to instantiate it.
     * <p>
     * <p>This method supports resource injection if the given
     * <tt>clazz</tt> represents a Managed Bean.
     * See the Java EE platform and JSR 299 specifications for additional
     * details about Managed Beans and resource injection.
     *
     * @param clazz the EventListener class to instantiate
     * @return the new EventListener instance
     * @throws ServletException              if the given <tt>clazz</tt> fails to be
     *                                       instantiated
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @throws IllegalArgumentException      if the specified EventListener class
     *                                       does not implement any of the
     *                                       {@link ServletContextListener},
     *                                       {@link ServletContextAttributeListener},
     *                                       {@link ServletRequestListener},
     *                                       {@link ServletRequestAttributeListener},
     *                                       {@link HttpSessionAttributeListener},
     *                                       {@link HttpSessionIdListener}, or
     *                                       {@link HttpSessionListener}
     *                                       interfaces.
     * @since Servlet 3.0
     */
    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EventListener> Collection<T> getListeners(Class<T> listenerClass) {
        if (!this.listeners.containsKey(listenerClass)) {
            return Collections.emptyList();
        }
        return (Collection<T>) this.listeners.get(listenerClass);
    }

    /**
     * Gets the <code>&lt;jsp-config&gt;</code> related configuration
     * that was aggregated from the <code>web.xml</code> and
     * <code>web-fragment.xml</code> descriptor files of the web application
     * represented by this ServletContext.
     *
     * @return the <code>&lt;jsp-config&gt;</code> related configuration
     * that was aggregated from the <code>web.xml</code> and
     * <code>web-fragment.xml</code> descriptor files of the web application
     * represented by this ServletContext, or null if no such configuration
     * exists
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @see JspConfigDescriptor
     * @since Servlet 3.0
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        // TODO jsp
        return null;
    }

    /**
     * Gets the class loader of the web application represented by this
     * ServletContext.
     * <p>
     * <p>If a security manager exists, and the caller's class loader
     * is not the same as, or an ancestor of the requested class loader,
     * then the security manager's <code>checkPermission</code> method is
     * called with a <code>RuntimePermission("getClassLoader")</code>
     * permission to check whether access to the requested class loader
     * should be granted.
     *
     * @return the class loader of the web application represented by this
     * ServletContext
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @throws SecurityException             if a security manager denies access to
     *                                       the requested class loader
     * @since Servlet 3.0
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    /**
     * Declares role names that are tested using <code>isUserInRole</code>.
     * <p>
     * <p>Roles that are implicitly declared as a result of their use within
     * the {@link ServletRegistration.Dynamic#setServletSecurity
     * setServletSecurity} or {@link ServletRegistration.Dynamic#setRunAsRole
     * setRunAsRole} methods of the {@link ServletRegistration} interface need
     * not be declared.
     *
     * @param roleNames the role names being declared
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @throws IllegalArgumentException      if any of the argument roleNames is
     *                                       null or the empty string
     * @throws IllegalStateException         if the ServletContext has already
     *                                       been initialized
     * @since Servlet 3.0
     */
    @Override
    public void declareRoles(String... roleNames) {
        // TODO security
    }

    /**
     * Returns the configuration name of the logical host on which the
     * ServletContext is deployed.
     * <p>
     * Servlet containers may support multiple logical hosts. This method must
     * return the same name for all the servlet contexts deployed on a logical
     * host, and the name returned by this method must be distinct, stable per
     * logical host, and suitable for use in associating server configuration
     * information with the logical host. The returned value is NOT expected
     * or required to be equivalent to a network address or hostname of the
     * logical host.
     *
     * @return a <code>String</code> containing the configuration name of the
     * logical host on which the servlet context is deployed.
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 3.1
     */
    @Override
    public String getVirtualServerName() {
        return "";
    }

    /**
     * Gets the session timeout in minutes that are supported by default for
     * this <tt>ServletContext</tt>.
     *
     * @return the session timeout in minutes that are supported by default for
     * this <tt>ServletContext</tt>
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public int getSessionTimeout() {
        return this.sessionTimeout;
    }

    /**
     * Sets the session timeout in minutes for this ServletContext.
     *
     * @param sessionTimeout session timeout in minutes
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Gets the request character encoding that are supported by default for
     * this <tt>ServletContext</tt>. This method returns null if no request
     * encoding character encoding has been specified in deployment descriptor
     * or container specific configuration (for all web applications in the
     * container).
     *
     * @return the request character encoding that are supported by default for
     * this <tt>ServletContext</tt>
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public String getRequestCharacterEncoding() {
        return this.requestEncoding;
    }

    /**
     * Sets the request character encoding for this ServletContext.
     *
     * @param encoding request character encoding
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public void setRequestCharacterEncoding(String encoding) {
        this.requestEncoding = encoding;
    }

    /**
     * Gets the response character encoding that are supported by default for
     * this <tt>ServletContext</tt>. This method returns null if no response
     * encoding character encoding has been specified in deployment descriptor
     * or container specific configuration (for all web applications in the
     * container).
     *
     * @return the request character encoding that are supported by default for
     * this <tt>ServletContext</tt>
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public String getResponseCharacterEncoding() {
        return this.responseEncoding;
    }

    /**
     * Sets the response character encoding for this ServletContext.
     *
     * @param encoding response character encoding
     * @throws IllegalStateException         if this ServletContext has already
     *                                       been initialized
     * @throws UnsupportedOperationException if this ServletContext was
     *                                       passed to the {@link ServletContextListener#contextInitialized} method
     *                                       of a {@link ServletContextListener} that was neither declared in
     *                                       <code>web.xml</code> or <code>web-fragment.xml</code>, nor annotated
     *                                       with {@link WebListener}
     * @since Servlet 4.0
     */
    @Override
    public void setResponseCharacterEncoding(String encoding) {
        this.responseEncoding = encoding;
    }
}

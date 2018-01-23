package cn.imaq.tompuss.servlet;

import cn.imaq.autumn.http.protocol.AutumnHttpRequest;
import cn.imaq.tompuss.io.TPInputStream;
import cn.imaq.tompuss.io.TPMultipartParser;
import cn.imaq.tompuss.session.TPSessionContext;
import cn.imaq.tompuss.util.TPMatchResult;
import lombok.Setter;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TPHttpServletRequest implements HttpServletRequest {
    private AutumnHttpRequest httpRequest;
    private TPServletContext context;
    private TPHttpExchange exchange;

    private String encoding = null;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, String[]> params;
    private Map<String, TPFormPart> formParts;

    @Setter
    private TPMatchResult matchResult;
    @Setter
    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    public TPHttpServletRequest(AutumnHttpRequest httpRequest, TPServletContext context, TPHttpExchange exchange) {
        this.httpRequest = httpRequest;
        this.context = context;
        this.exchange = exchange;
    }

    /**
     * Returns the name of the authentication scheme used to protect
     * the servlet. All servlet containers support basic, form and client
     * certificate authentication, and may additionally support digest
     * authentication.
     * If the servlet is not authenticated <code>null</code> is returned.
     * <p>
     * <p>Same as the value of the CGI variable AUTH_TYPE.
     *
     * @return one of the static members BASIC_AUTH,
     * FORM_AUTH, CLIENT_CERT_AUTH, DIGEST_AUTH
     * (suitable for == comparison) or
     * the container-specific string indicating
     * the authentication scheme, or
     * <code>null</code> if the request was
     * not authenticated.
     */
    @Override
    public String getAuthType() {
        // TODO auth
        return null;
    }

    /**
     * Returns an array containing all of the <code>Cookie</code>
     * objects the client sent with this request.
     * This method returns <code>null</code> if no cookies were sent.
     *
     * @return an array of all the <code>Cookies</code>
     * included with this request, or <code>null</code>
     * if the request has no cookies
     */
    @Override
    public Cookie[] getCookies() {
        if (!httpRequest.getHeaders().containsKey("cookie")) {
            return null;
        }
        List<Cookie> cookieList = new ArrayList<>();
        for (String cookieLine : httpRequest.getHeaders().get("cookie")) {
            for (String cookieItem : cookieLine.split(";")) {
                String[] nameAndValue = cookieItem.split("=", 2);
                if (nameAndValue.length == 2) {
                    cookieList.add(new Cookie(nameAndValue[0].trim(), nameAndValue[1].trim()));
                }
            }
        }
        return cookieList.toArray(new Cookie[0]);
    }

    /**
     * Returns the value of the specified request header
     * as a <code>long</code> value that represents a
     * <code>Date</code> object. Use this method with
     * headers that contain dates, such as
     * <code>If-Modified-Since</code>.
     * <p>
     * <p>The date is returned as
     * the number of milliseconds since January 1, 1970 GMT.
     * The header name is case insensitive.
     * <p>
     * <p>If the request did not have a header of the
     * specified name, this method returns -1. If the header
     * can't be converted to a date, the method throws
     * an <code>IllegalArgumentException</code>.
     *
     * @param name a <code>String</code> specifying the
     *             name of the header
     * @return a <code>long</code> value
     * representing the date specified
     * in the header expressed as
     * the number of milliseconds
     * since January 1, 1970 GMT,
     * or -1 if the named header
     * was not included with the
     * request
     * @throws IllegalArgumentException If the header value
     *                                  can't be converted
     *                                  to a date
     */
    @Override
    public long getDateHeader(String name) {
        if (!httpRequest.getHeaders().containsKey(name)) {
            return -1;
        }
        String value = this.getHeader(name);
        try {
            Date date = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss z").parse(value);
            return date.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the value of the specified request header
     * as a <code>String</code>. If the request did not include a header
     * of the specified name, this method returns <code>null</code>.
     * If there are multiple headers with the same name, this method
     * returns the first head in the request.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name a <code>String</code> specifying the
     *             header name
     * @return a <code>String</code> containing the
     * value of the requested
     * header, or <code>null</code>
     * if the request does not
     * have a header of that name
     */
    @Override
    public String getHeader(String name) {
        String lower = name.toLowerCase();
        if (!httpRequest.getHeaders().containsKey(lower)) {
            return null;
        }
        return httpRequest.getHeaders().get(lower).get(0);
    }

    /**
     * Returns all the values of the specified request header
     * as an <code>Enumeration</code> of <code>String</code> objects.
     * <p>
     * <p>Some headers, such as <code>Accept-Language</code> can be sent
     * by clients as several headers each with a different value rather than
     * sending the header as a comma separated list.
     * <p>
     * <p>If the request did not include any headers
     * of the specified name, this method returns an empty
     * <code>Enumeration</code>.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name a <code>String</code> specifying the
     *             header name
     * @return an <code>Enumeration</code> containing
     * the values of the requested header. If
     * the request does not have any headers of
     * that name return an empty
     * enumeration. If
     * the container does not allow access to
     * header information, return null
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        String lower = name.toLowerCase();
        if (!httpRequest.getHeaders().containsKey(lower)) {
            return Collections.emptyEnumeration();
        }
        return Collections.enumeration(httpRequest.getHeaders().get(lower));
    }

    /**
     * Returns an enumeration of all the header names
     * this request contains. If the request has no
     * headers, this method returns an empty enumeration.
     * <p>
     * <p>Some servlet containers do not allow
     * servlets to access headers using this method, in
     * which case this method returns <code>null</code>
     *
     * @return an enumeration of all the
     * header names sent with this
     * request; if the request has
     * no headers, an empty enumeration;
     * if the servlet container does not
     * allow servlets to use this method,
     * <code>null</code>
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(httpRequest.getHeaders().keySet());
    }

    /**
     * Returns the value of the specified request header
     * as an <code>int</code>. If the request does not have a header
     * of the specified name, this method returns -1. If the
     * header cannot be converted to an integer, this method
     * throws a <code>NumberFormatException</code>.
     * <p>
     * <p>The header name is case insensitive.
     *
     * @param name a <code>String</code> specifying the name
     *             of a request header
     * @return an integer expressing the value
     * of the request header or -1
     * if the request doesn't have a
     * header of this name
     * @throws NumberFormatException If the header value
     *                               can't be converted
     *                               to an <code>int</code>
     */
    @Override
    public int getIntHeader(String name) {
        if (!httpRequest.getHeaders().containsKey(name)) {
            return -1;
        }
        return Integer.valueOf(this.getHeader(name));
    }

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return a <code>String</code>
     * specifying the name
     * of the method with which
     * this request was made
     */
    @Override
    public String getMethod() {
        return httpRequest.getMethod();
    }

    /**
     * Returns any extra path information associated with
     * the URL the client sent when it made this request.
     * The extra path information follows the servlet path
     * but precedes the query string and will start with
     * a "/" character.
     * <p>
     * <p>This method returns <code>null</code> if there
     * was no extra path information.
     * <p>
     * <p>Same as the value of the CGI variable PATH_INFO.
     *
     * @return a <code>String</code>, decoded by the
     * web container, specifying
     * extra path information that comes
     * after the servlet path but before
     * the query string in the request URL;
     * or <code>null</code> if the URL does not have
     * any extra path information
     */
    @Override
    public String getPathInfo() {
        String[] pathAndQuery = httpRequest.getPath().split("\\?", 2);
        try {
            String pathInfo = pathAndQuery[0].substring(this.getServletPath().length());
            if (pathInfo.isEmpty()) {
                return null;
            }
            return pathInfo;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns any extra path information after the servlet name
     * but before the query string, and translates it to a real
     * path. Same as the value of the CGI variable PATH_TRANSLATED.
     * <p>
     * <p>If the URL does not have any extra path information,
     * this method returns <code>null</code> or the servlet container
     * cannot translate the virtual path to a real path for any reason
     * (such as when the web application is executed from an archive).
     * <p>
     * The web container does not decode this string.
     *
     * @return a <code>String</code> specifying the
     * real path, or <code>null</code> if
     * the URL does not have any extra path
     * information
     */
    @Override
    public String getPathTranslated() {
        return this.getPathInfo();
    }

    /**
     * Returns the portion of the request URI that indicates the context
     * of the request. The context path always comes first in a request
     * URI. The path starts with a "/" character but does not end with a "/"
     * character. For servlets in the default (root) context, this method
     * returns "". The container does not decode this string.
     * <p>
     * <p>It is possible that a servlet container may match a context by
     * more than one context path. In such cases this method will return the
     * actual context path used by the request and it may differ from the
     * path returned by the
     * {@link ServletContext#getContextPath()} method.
     * The context path returned by
     * {@link ServletContext#getContextPath()}
     * should be considered as the prime or preferred context path of the
     * application.
     *
     * @return a <code>String</code> specifying the
     * portion of the request URI that indicates the context
     * of the request
     * @see ServletContext#getContextPath()
     */
    @Override
    public String getContextPath() {
        return this.context.getContextPath();
    }

    /**
     * Returns the query string that is contained in the request
     * URL after the path. This method returns <code>null</code>
     * if the URL does not have a query string. Same as the value
     * of the CGI variable QUERY_STRING.
     *
     * @return a <code>String</code> containing the query
     * string or <code>null</code> if the URL
     * contains no query string. The value is not
     * decoded by the container.
     */
    @Override
    public String getQueryString() {
        String[] pathAndQuery = httpRequest.getPath().split("\\?", 2);
        if (pathAndQuery.length > 1) {
            return pathAndQuery[1];
        }
        return null;
    }

    /**
     * Returns the login of the user making this request, if the
     * user has been authenticated, or <code>null</code> if the user
     * has not been authenticated.
     * Whether the user name is sent with each subsequent request
     * depends on the browser and type of authentication. Same as the
     * value of the CGI variable REMOTE_USER.
     *
     * @return a <code>String</code> specifying the login
     * of the user making this request, or <code>null</code>
     * if the user login is not known
     */
    @Override
    public String getRemoteUser() {
        // TODO auth
        return null;
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role".  Roles and role membership can be
     * defined using deployment descriptors.  If the user has not been
     * authenticated, the method returns <code>false</code>.
     * <p>
     * <p>The role name "*" should never be used as an argument in calling
     * <code>isUserInRole</code>. Any call to <code>isUserInRole</code> with
     * "*" must return false.
     * If the role-name of the security-role to be tested is "**", and
     * the application has NOT declared an application security-role with
     * role-name "**", <code>isUserInRole</code> must only return true if
     * the user has been authenticated; that is, only when
     * {@link #getRemoteUser} and {@link #getUserPrincipal} would both return
     * a non-null value. Otherwise, the container must check
     * the user for membership in the application role.
     *
     * @param role a <code>String</code> specifying the name
     *             of the role
     * @return a <code>boolean</code> indicating whether
     * the user making this request belongs to a given role;
     * <code>false</code> if the user has not been
     * authenticated
     */
    @Override
    public boolean isUserInRole(String role) {
        // TODO auth
        return false;
    }

    /**
     * Returns a <code>java.security.Principal</code> object containing
     * the name of the current authenticated user. If the user has not been
     * authenticated, the method returns <code>null</code>.
     *
     * @return a <code>java.security.Principal</code> containing
     * the name of the user making this request;
     * <code>null</code> if the user has not been
     * authenticated
     */
    @Override
    public Principal getUserPrincipal() {
        // TODO auth
        return null;
    }

    /**
     * Returns the session ID specified by the client. This may
     * not be the same as the ID of the current valid session
     * for this request.
     * If the client did not specify a session ID, this method returns
     * <code>null</code>.
     *
     * @return a <code>String</code> specifying the session
     * ID, or <code>null</code> if the request did
     * not specify a session ID
     * @see #isRequestedSessionIdValid
     */
    @Override
    public String getRequestedSessionId() {
        Cookie[] cookies = this.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns the part of this request's URL from the protocol
     * name up to the query string in the first line of the HTTP request.
     * The web container does not decode this String.
     * For example:
     * <p>
     * <table summary="Examples of Returned Values">
     * <tr align=left><th>First line of HTTP request      </th>
     * <th>     Returned Value</th>
     * <tr><td>POST /some/path.html HTTP/1.1<td><td>/some/path.html
     * <tr><td>GET http://foo.bar/a.html HTTP/1.0
     * <td><td>/a.html
     * <tr><td>HEAD /xyz?a=b HTTP/1.1<td><td>/xyz
     * </table>
     * <p>
     * <p>To reconstruct an URL with a scheme and host, use
     * {@link HttpUtils#getRequestURL}.
     *
     * @return a <code>String</code> containing
     * the part of the URL from the
     * protocol name up to the query string
     * @see HttpUtils#getRequestURL
     */
    @Override
    public String getRequestURI() {
        String[] pathAndQuery = httpRequest.getPath().split("\\?", 2);
        return pathAndQuery[0];
    }

    /**
     * Reconstructs the URL the client used to make the request.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     * <p>
     * <p>If this request has been forwarded using
     * {@link RequestDispatcher#forward}, the server path in the
     * reconstructed URL must reflect the path used to obtain the
     * RequestDispatcher, and not the server path specified by the client.
     * <p>
     * <p>Because this method returns a <code>StringBuffer</code>,
     * not a string, you can modify the URL easily, for example,
     * to append query parameters.
     * <p>
     * <p>This method is useful for creating redirect messages
     * and for reporting errors.
     *
     * @return a <code>StringBuffer</code> object containing
     * the reconstructed URL
     */
    @Override
    public StringBuffer getRequestURL() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://");
        if (httpRequest.getHeaders().containsKey("host")) {
            buffer.append(httpRequest.getHeaders().get("host").get(0));
        } else {
            buffer.append(httpRequest.getLocalAddress());
        }
        buffer.append(this.getRequestURI());
        return buffer;
    }

    /**
     * Returns the part of this request's URL that calls
     * the servlet. This path starts with a "/" character
     * and includes either the servlet name or a path to
     * the servlet, but does not include any extra path
     * information or a query string. Same as the value of
     * the CGI variable SCRIPT_NAME.
     * <p>
     * <p>This method will return an empty string ("") if the
     * servlet used to process this request was matched using
     * the "/*" pattern.
     *
     * @return a <code>String</code> containing
     * the name or path of the servlet being
     * called, as specified in the request URL,
     * decoded, or an empty string if the servlet
     * used to process the request is matched
     * using the "/*" pattern.
     */
    @Override
    public String getServletPath() {
        if (this.matchResult == null) {
            return "";
        }
        return this.matchResult.getMatched();
    }

    /**
     * Returns the current <code>HttpSession</code>
     * associated with this request or, if there is no
     * current session and <code>create</code> is true, returns
     * a new session.
     * <p>
     * <p>If <code>create</code> is <code>false</code>
     * and the request has no valid <code>HttpSession</code>,
     * this method returns <code>null</code>.
     * <p>
     * <p>To make sure the session is properly maintained,
     * you must call this method before
     * the response is committed. If the container is using cookies
     * to maintain session integrity and is asked to create a new session
     * when the response is committed, an IllegalStateException is thrown.
     *
     * @param create <code>true</code> to create
     *               a new session for this request if necessary;
     *               <code>false</code> to return <code>null</code>
     *               if there's no current session
     * @return the <code>HttpSession</code> associated
     * with this request or <code>null</code> if
     * <code>create</code> is <code>false</code>
     * and the request has no valid session
     * @see #getSession()
     */
    @Override
    public HttpSession getSession(boolean create) {
        String sessId = this.getRequestedSessionId();
        TPSessionContext sessionContext = context.getSessionContext();
        HttpSession session = null;
        if (sessId != null) {
            session = sessionContext.getSession(sessId);
        }
        if (session == null && create) {
            session = sessionContext.createSession(sessId);
            if (!session.getId().equals(sessId)) {
                Cookie cookie = new Cookie("JSESSIONID", session.getId());
                cookie.setPath(this.context.getContextPath());
                cookie.setHttpOnly(true);
                this.exchange.getCookies().add(cookie);
            }
        }
        return session;
    }

    /**
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return the <code>HttpSession</code> associated
     * with this request
     * @see #getSession(boolean)
     */
    @Override
    public HttpSession getSession() {
        return this.getSession(true);
    }

    /**
     * Change the session id of the current session associated with this
     * request and return the new session id.
     *
     * @return the new session id
     * @throws IllegalStateException if there is no session associated
     *                               with the request
     * @since Servlet 3.1
     */
    @Override
    public String changeSessionId() {
        // TODO session
        return null;
    }

    /**
     * Checks whether the requested session ID is still valid.
     * <p>
     * <p>If the client did not specify any session ID, this method returns
     * <code>false</code>.
     *
     * @return <code>true</code> if this
     * request has an id for a valid session
     * in the current session context;
     * <code>false</code> otherwise
     * @see #getRequestedSessionId
     * @see #getSession
     * @see HttpSessionContext
     */
    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO session
        return false;
    }

    /**
     * <p>Checks whether the requested session ID was conveyed to the
     * server as an HTTP cookie.</p>
     *
     * @return <code>true</code> if the session ID
     * was conveyed to the server an an HTTP
     * cookie; otherwise, <code>false</code>
     * @see #getSession
     */
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }

    /**
     * <p>Checks whether the requested session ID was conveyed to the
     * server as part of the request URL.</p>
     *
     * @return <code>true</code> if the session ID was conveyed to the
     * server as part of a URL; otherwise,
     * <code>false</code>
     * @see #getSession
     */
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @return <code>true</code> if the session ID was conveyed to the
     * server as part of a URL; otherwise,
     * <code>false</code>
     * @deprecated As of Version 2.1 of the Java Servlet
     * API, use {@link #isRequestedSessionIdFromURL}
     * instead.
     */
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this.isRequestedSessionIdFromURL();
    }

    /**
     * Use the container login mechanism configured for the
     * <code>ServletContext</code> to authenticate the user making
     * this request.
     * <p>
     * <p>This method may modify and commit the argument
     * <code>HttpServletResponse</code>.
     *
     * @param response The <code>HttpServletResponse</code>
     *                 associated with this <code>HttpServletRequest</code>
     * @return <code>true</code> when non-null values were or have been
     * established as the values returned by <code>getUserPrincipal</code>,
     * <code>getRemoteUser</code>, and <code>getAuthType</code>. Return
     * <code>false</code> if authentication is incomplete and the underlying
     * login mechanism has committed, in the response, the message (e.g.,
     * challenge) and HTTP status code to be returned to the user.
     * @throws IOException           if an input or output error occurred while
     *                               reading from this request or writing to the given response
     * @throws IllegalStateException if the login mechanism attempted to
     *                               modify the response and it was already committed
     * @throws ServletException      if the authentication failed and
     *                               the caller is responsible for handling the error (i.e., the
     *                               underlying login mechanism did NOT establish the message and
     *                               HTTP status code to be returned to the user)
     * @since Servlet 3.0
     */
    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO auth
        return false;
    }

    /**
     * Validate the provided username and password in the password validation
     * realm used by the web container login mechanism configured for the
     * <code>ServletContext</code>.
     * <p>
     * <p>This method returns without throwing a <code>ServletException</code>
     * when the login mechanism configured for the <code>ServletContext</code>
     * supports username password validation, and when, at the time of the
     * call to login, the identity of the caller of the request had
     * not been established (i.e, all of <code>getUserPrincipal</code>,
     * <code>getRemoteUser</code>, and <code>getAuthType</code> return null),
     * and when validation of the provided credentials is successful.
     * Otherwise, this method throws a <code>ServletException</code> as
     * described below.
     * <p>
     * <p>When this method returns without throwing an exception, it must
     * have established non-null values as the values returned by
     * <code>getUserPrincipal</code>, <code>getRemoteUser</code>, and
     * <code>getAuthType</code>.
     *
     * @param username The <code>String</code> value corresponding to
     *                 the login identifier of the user.
     * @param password The password <code>String</code> corresponding
     *                 to the identified user.
     * @throws ServletException if the configured login mechanism
     *                          does not support username
     *                          password authentication, or if a
     *                          non-null caller identity had
     *                          already been established (prior
     *                          to the call to login), or if
     *                          validation of the provided
     *                          username and password fails.
     * @since Servlet 3.0
     */
    @Override
    public void login(String username, String password) throws ServletException {
        // TODO auth
    }

    /**
     * Establish <code>null</code> as the value returned when
     * <code>getUserPrincipal</code>, <code>getRemoteUser</code>,
     * and <code>getAuthType</code> is called on the request.
     *
     * @throws ServletException if logout fails
     * @since Servlet 3.0
     */
    @Override
    public void logout() throws ServletException {
        // TODO auth
    }

    /**
     * Gets all the {@link Part} components of this request, provided
     * that it is of type <code>multipart/form-data</code>.
     * <p>
     * <p>If this request is of type <code>multipart/form-data</code>, but
     * does not contain any <code>Part</code> components, the returned
     * <code>Collection</code> will be empty.
     * <p>
     * <p>Any changes to the returned <code>Collection</code> must not
     * affect this <code>HttpServletRequest</code>.
     *
     * @return a (possibly empty) <code>Collection</code> of the
     * <code>Part</code> components of this request
     * @throws IOException           if an I/O error occurred during the retrieval
     *                               of the {@link Part} components of this request
     * @throws ServletException      if this request is not of type
     *                               <code>multipart/form-data</code>
     * @throws IllegalStateException if the request body is larger than
     *                               <code>maxRequestSize</code>, or any <code>Part</code> in the
     *                               request is larger than <code>maxFileSize</code>, or there is no
     *                               <code>@MultipartConfig</code> or <code>multipart-config</code> in
     *                               deployment descriptors
     * @see MultipartConfig#maxFileSize
     * @see MultipartConfig#maxRequestSize
     * @since Servlet 3.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return (Collection<Part>) (Collection<? extends Part>) this.getFormParts().values();
    }

    /**
     * Gets the {@link Part} with the given name.
     *
     * @param name the name of the requested <code>Part</code>
     * @return The <code>Part</code> with the given name, or
     * <code>null</code> if this request is of type
     * <code>multipart/form-data</code>, but does not
     * contain the requested <code>Part</code>
     * @throws IOException           if an I/O error occurred during the retrieval
     *                               of the requested <code>Part</code>
     * @throws ServletException      if this request is not of type
     *                               <code>multipart/form-data</code>
     * @throws IllegalStateException if the request body is larger than
     *                               <code>maxRequestSize</code>, or any <code>Part</code> in the
     *                               request is larger than <code>maxFileSize</code>, or there is no
     *                               <code>@MultipartConfig</code> or <code>multipart-config</code> in
     *                               deployment descriptors
     * @see MultipartConfig#maxFileSize
     * @see MultipartConfig#maxRequestSize
     * @since Servlet 3.0
     */
    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.getFormParts().get(name);
    }

    private Map<String, TPFormPart> getFormParts() {
        if (this.formParts == null) {
            this.formParts = TPMultipartParser.parse(httpRequest);
        }
        return this.formParts;
    }

    /**
     * Creates an instance of <code>HttpUpgradeHandler</code> for a given
     * class and uses it for the http protocol upgrade processing.
     *
     * @param handlerClass The <code>HttpUpgradeHandler</code> class used for the upgrade.
     * @return an instance of the <code>HttpUpgradeHandler</code>
     * @throws IOException      if an I/O error occurred during the upgrade
     * @throws ServletException if the given <code>handlerClass</code> fails to
     *                          be instantiated
     * @see HttpUpgradeHandler
     * @see WebConnection
     * @since Servlet 3.1
     */
    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        // TODO websocket
        return null;
    }

    /**
     * Returns the value of the named attribute as an <code>Object</code>,
     * or <code>null</code> if no attribute of the given name exists.
     * <p>
     * <p> Attributes can be set two ways.  The servlet container may set
     * attributes to make available custom information about a request.
     * For example, for requests made using HTTPS, the attribute
     * <code>javax.servlet.request.X509Certificate</code> can be used to
     * retrieve information on the certificate of the client.  Attributes
     * can also be set programatically using
     * {@link ServletRequest#setAttribute}.  This allows information to be
     * embedded into a request before a {@link RequestDispatcher} call.
     * <p>
     * <p>Attribute names should follow the same conventions as package
     * names. This specification reserves names matching <code>java.*</code>,
     * <code>javax.*</code>, and <code>sun.*</code>.
     *
     * @param name a <code>String</code> specifying the name of the attribute
     * @return an <code>Object</code> containing the value of the attribute,
     * or <code>null</code> if the attribute does not exist
     */
    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Returns an <code>Enumeration</code> containing the
     * names of the attributes available to this request.
     * This method returns an empty <code>Enumeration</code>
     * if the request has no attributes available to it.
     *
     * @return an <code>Enumeration</code> of strings containing the names
     * of the request's attributes
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    /**
     * Returns the name of the character encoding used in the body of this
     * request. This method returns <code>null</code> if no request encoding
     * character encoding has been specified. The following methods for
     * specifying the request character encoding are consulted, in decreasing
     * order of priority: per request, per web app (using
     * {@link ServletContext#setRequestCharacterEncoding}, deployment
     * descriptor), and per container (for all web applications deployed in
     * that container, using vendor specific configuration).
     *
     * @return a <code>String</code> containing the name of the character
     * encoding, or <code>null</code> if the request does not specify a
     * character encoding
     */
    @Override
    public String getCharacterEncoding() {
        if (this.encoding == null) {
            if (httpRequest.getHeaders().containsKey("content-type")) {
                String[] typeParts = httpRequest.getHeaders().get("content-type").get(0).split(";");
                for (String part : typeParts) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith("charset=")) {
                        this.encoding = trimmed.substring(8);
                    }
                }
            }
            if (this.encoding == null) {
                this.encoding = context.getRequestCharacterEncoding();
            }
        }
        return this.encoding;
    }

    /**
     * Overrides the name of the character encoding used in the body of this
     * request. This method must be called prior to reading request parameters
     * or reading input using getReader(). Otherwise, it has no effect.
     *
     * @param env <code>String</code> containing the name of
     *            the character encoding.
     * @throws UnsupportedEncodingException if this ServletRequest is still
     *                                      in a state where a character encoding may be set,
     *                                      but the specified encoding is invalid
     */
    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.encoding = env;
    }

    /**
     * Returns the length, in bytes, of the request body and made available by
     * the input stream, or -1 if the length is not known ir is greater than
     * Integer.MAX_VALUE. For HTTP servlets,
     * same as the value of the CGI variable CONTENT_LENGTH.
     *
     * @return an integer containing the length of the request body or -1 if
     * the length is not known or is greater than Integer.MAX_VALUE.
     */
    @Override
    public int getContentLength() {
        if (httpRequest.getBody() == null) {
            return -1;
        }
        return httpRequest.getBody().length;
    }

    /**
     * Returns the length, in bytes, of the request body and made available by
     * the input stream, or -1 if the length is not known. For HTTP servlets,
     * same as the value of the CGI variable CONTENT_LENGTH.
     *
     * @return a long containing the length of the request body or -1L if
     * the length is not known
     * @since Servlet 3.1
     */
    @Override
    public long getContentLengthLong() {
        return this.getContentLength();
    }

    /**
     * Returns the MIME type of the body of the request, or
     * <code>null</code> if the type is not known. For HTTP servlets,
     * same as the value of the CGI variable CONTENT_TYPE.
     *
     * @return a <code>String</code> containing the name of the MIME type
     * of the request, or null if the type is not known
     */
    @Override
    public String getContentType() {
        if (httpRequest.getHeaders().containsKey("content-type")) {
            String[] typeParts = httpRequest.getHeaders().get("content-type").get(0).split(";", 2);
            return typeParts[0];
        }
        return null;
    }

    /**
     * Retrieves the body of the request as binary data using
     * a {@link ServletInputStream}.  Either this method or
     * {@link #getReader} may be called to read the body, not both.
     *
     * @return a {@link ServletInputStream} object containing
     * the body of the request
     * @throws IllegalStateException if the {@link #getReader} method
     *                               has already been called for this request
     * @throws IOException           if an input or output exception occurred
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (httpRequest.getBody() == null) {
            return new TPInputStream(new byte[0]);
        }
        return new TPInputStream(httpRequest.getBody());
    }

    /**
     * Returns the value of a request parameter as a <code>String</code>,
     * or <code>null</code> if the parameter does not exist. Request parameters
     * are extra information sent with the request.  For HTTP servlets,
     * parameters are contained in the query string or posted form data.
     * <p>
     * <p>You should only use this method when you are sure the
     * parameter has only one value. If the parameter might have
     * more than one value, use {@link #getParameterValues}.
     * <p>
     * <p>If you use this method with a multivalued
     * parameter, the value returned is equal to the first value
     * in the array returned by <code>getParameterValues</code>.
     * <p>
     * <p>If the parameter data was sent in the request body, such as occurs
     * with an HTTP POST request, then reading the body directly via {@link
     * #getInputStream} or {@link #getReader} can interfere
     * with the execution of this method.
     *
     * @param name a <code>String</code> specifying the name of the parameter
     * @return a <code>String</code> representing the single value of
     * the parameter
     * @see #getParameterValues
     */
    @Override
    public String getParameter(String name) {
        String[] values = this.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return values[0];
    }

    /**
     * Returns an <code>Enumeration</code> of <code>String</code>
     * objects containing the names of the parameters contained
     * in this request. If the request has
     * no parameters, the method returns an empty <code>Enumeration</code>.
     *
     * @return an <code>Enumeration</code> of <code>String</code>
     * objects, each <code>String</code> containing the name of
     * a request parameter; or an empty <code>Enumeration</code>
     * if the request has no parameters
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    /**
     * Returns an array of <code>String</code> objects containing
     * all of the values the given request parameter has, or
     * <code>null</code> if the parameter does not exist.
     * <p>
     * <p>If the parameter has a single value, the array has a length
     * of 1.
     *
     * @param name a <code>String</code> containing the name of
     *             the parameter whose value is requested
     * @return an array of <code>String</code> objects
     * containing the parameter's values
     * @see #getParameter
     */
    @Override
    public String[] getParameterValues(String name) {
        return this.getParameterMap().get(name);
    }

    /**
     * Returns a java.util.Map of the parameters of this request.
     * <p>
     * <p>Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string or
     * posted form data.
     *
     * @return an immutable java.util.Map containing parameter names as
     * keys and parameter values as map values. The keys in the parameter
     * map are of type String. The values in the parameter map are of type
     * String array.
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        if (this.params == null) {
            Map<String, List<String>> paramsMap = new HashMap<>();
            // query string params
            this.parseParamString(this.getQueryString(), paramsMap);
            // POST params
            if (this.getMethod().toLowerCase().equals("post")) {
                switch (this.getContentType()) {
                    case "application/x-www-form-urlencoded":
                        this.parseParamString(new String(httpRequest.getBody()), paramsMap);
                        break;
                    case "multipart/form-data":
                        try {
                            for (Part part : this.getParts()) {
                                if (part.getContentType().equals("text/plain")) {
                                    byte[] buf = new byte[(int) part.getSize()];
                                    part.getInputStream().read(buf);
                                    paramsMap.computeIfAbsent(part.getName(), x -> new ArrayList<>()).add(new String(buf));
                                }
                            }
                        } catch (IOException | ServletException ignored) {
                        }
                        break;
                }
            }
            // Merge
            this.params = new HashMap<>();
            paramsMap.forEach((key, value) -> this.params.put(key, value.toArray(new String[0])));
        }
        return Collections.unmodifiableMap(this.params);
    }

    private void parseParamString(String paramStr, Map<String, List<String>> map) {
        if (paramStr == null) {
            return;
        }
        String[] queries = paramStr.split("&");
        for (String query : queries) {
            String[] nameAndValue = query.split("=", 2);
            if (nameAndValue.length == 2) {
                List<String> values = map.computeIfAbsent(nameAndValue[0], x -> new ArrayList<>());
                try {
                    values.add(URLDecoder.decode(nameAndValue[1], this.getCharacterEncoding()));
                } catch (UnsupportedEncodingException e) {
                    values.add(nameAndValue[1]);
                }
            }
        }
    }

    /**
     * Returns the name and version of the protocol the request uses
     * in the form <i>protocol/majorVersion.minorVersion</i>, for
     * example, HTTP/1.1. For HTTP servlets, the value
     * returned is the same as the value of the CGI variable
     * <code>SERVER_PROTOCOL</code>.
     *
     * @return a <code>String</code> containing the protocol
     * name and version number
     */
    @Override
    public String getProtocol() {
        return httpRequest.getProtocol();
    }

    /**
     * Returns the name of the scheme used to make this request,
     * for example,
     * <code>http</code>, <code>https</code>, or <code>ftp</code>.
     * Different schemes have different rules for constructing URLs,
     * as noted in RFC 1738.
     *
     * @return a <code>String</code> containing the name
     * of the scheme used to make this request
     */
    @Override
    public String getScheme() {
        return "http";
    }

    /**
     * Returns the host name of the server to which the request was sent.
     * It is the value of the part before ":" in the <code>Host</code>
     * header value, if any, or the resolved server name, or the server IP
     * address.
     *
     * @return a <code>String</code> containing the name of the server
     */
    @Override
    public String getServerName() {
        if (httpRequest.getHeaders().containsKey("host")) {
            String host = httpRequest.getHeaders().get("host").get(0);
            int pos = host.lastIndexOf(':');
            if (pos >= 0) {
                return host.substring(0, pos);
            }
            return host;
        }
        try {
            return ((InetSocketAddress) httpRequest.getLocalAddress()).getHostName();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the port number to which the request was sent.
     * It is the value of the part after ":" in the <code>Host</code>
     * header value, if any, or the server port where the client connection
     * was accepted on.
     *
     * @return an integer specifying the port number
     */
    @Override
    public int getServerPort() {
        if (httpRequest.getHeaders().containsKey("host")) {
            String host = httpRequest.getHeaders().get("host").get(0);
            int pos = host.lastIndexOf(':');
            if (pos >= 0) {
                try {
                    return Integer.parseInt(host.substring(pos + 1));
                } catch (Exception ignored) {
                }
            }
            return 80;
        }
        return this.context.getEngine().getPort();
    }

    /**
     * Retrieves the body of the request as character data using
     * a <code>BufferedReader</code>.  The reader translates the character
     * data according to the character encoding used on the body.
     * Either this method or {@link #getInputStream} may be called to read the
     * body, not both.
     *
     * @return a <code>BufferedReader</code> containing the body of the request
     * @throws UnsupportedEncodingException if the character set encoding
     *                                      used is not supported and the text cannot be decoded
     * @throws IllegalStateException        if {@link #getInputStream} method
     *                                      has been called on this request
     * @throws IOException                  if an input or output exception occurred
     * @see #getInputStream
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), this.getCharacterEncoding()));
    }

    /**
     * Returns the Internet Protocol (IP) address of the client
     * or last proxy that sent the request.
     * For HTTP servlets, same as the value of the
     * CGI variable <code>REMOTE_ADDR</code>.
     *
     * @return a <code>String</code> containing the
     * IP address of the client that sent the request
     */
    @Override
    public String getRemoteAddr() {
        try {
            return ((InetSocketAddress) httpRequest.getRemoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the fully qualified name of the client
     * or the last proxy that sent the request.
     * If the engine cannot or chooses not to resolve the hostname
     * (to improve performance), this method returns the dotted-string form of
     * the IP address. For HTTP servlets, same as the value of the CGI variable
     * <code>REMOTE_HOST</code>.
     *
     * @return a <code>String</code> containing the fully
     * qualified name of the client
     */
    @Override
    public String getRemoteHost() {
        try {
            return ((InetSocketAddress) httpRequest.getRemoteAddress()).getHostString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Stores an attribute in this request.
     * Attributes are reset between requests.  This method is most
     * often used in conjunction with {@link RequestDispatcher}.
     * <p>
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     * <br> If the object passed in is null, the effect is the same as
     * calling {@link #removeAttribute}.
     * <br> It is warned that when the request is dispatched from the
     * servlet resides in a different web application by
     * <code>RequestDispatcher</code>, the object set by this method
     * may not be correctly retrieved in the caller servlet.
     *
     * @param name a <code>String</code> specifying
     *             the name of the attribute
     * @param o    the <code>Object</code> to be stored
     */
    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    /**
     * Removes an attribute from this request.  This method is not
     * generally needed as attributes only persist as long as the request
     * is being handled.
     * <p>
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     * @param name a <code>String</code> specifying
     *             the name of the attribute to remove
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Returns the preferred <code>Locale</code> that the client will
     * accept content in, based on the Accept-Language header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns the default locale for the server.
     *
     * @return the preferred <code>Locale</code> for the client
     */
    @Override
    public Locale getLocale() {
        Enumeration<Locale> locales = this.getLocales();
        return locales.nextElement();
    }

    /**
     * Returns an <code>Enumeration</code> of <code>Locale</code> objects
     * indicating, in decreasing order starting with the preferred locale, the
     * locales that are acceptable to the client based on the Accept-Language
     * header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns an <code>Enumeration</code> containing one
     * <code>Locale</code>, the default locale for the server.
     *
     * @return an <code>Enumeration</code> of preferred
     * <code>Locale</code> objects for the client
     */
    @Override
    public Enumeration<Locale> getLocales() {
        if (!httpRequest.getHeaders().containsKey("accept-language")) {
            return Collections.enumeration(Collections.singleton(Locale.getDefault()));
        }
        List<Locale> locales = new ArrayList<>();
        String[] langs = httpRequest.getHeaders().get("accept-language").get(0).split(",");
        for (String lang : langs) {
            locales.add(Locale.forLanguageTag(lang.split(";", 2)[0].trim()));
        }
        return Collections.enumeration(locales);
    }

    /**
     * Returns a boolean indicating whether this request was made using a
     * secure channel, such as HTTPS.
     *
     * @return a boolean indicating if the request was made using a
     * secure channel
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * Returns a {@link RequestDispatcher} object that acts as a wrapper for
     * the resource located at the given path.
     * A <code>RequestDispatcher</code> object can be used to forward
     * a request to the resource or to include the resource in a response.
     * The resource can be dynamic or static.
     * <p>
     * <p>The pathname specified may be relative, although it cannot extend
     * outside the current servlet context.  If the path begins with
     * a "/" it is interpreted as relative to the current context root.
     * This method returns <code>null</code> if the servlet container
     * cannot return a <code>RequestDispatcher</code>.
     * <p>
     * <p>The difference between this method and {@link
     * ServletContext#getRequestDispatcher} is that this method can take a
     * relative path.
     *
     * @param path a <code>String</code> specifying the pathname
     *             to the resource. If it is relative, it must be
     *             relative against the current servlet.
     * @return a <code>RequestDispatcher</code> object that acts as a
     * wrapper for the resource at the specified path,
     * or <code>null</code> if the servlet container cannot
     * return a <code>RequestDispatcher</code>
     * @see RequestDispatcher
     * @see ServletContext#getRequestDispatcher
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            path = this.getServletPath() + "/" + path;
        }
        return this.context.getRequestDispatcher(path);
    }

    /**
     * @param path the path for which the real path is to be returned.
     * @return the <i>real</i> path, or <tt>null</tt> if the
     * translation cannot be performed.
     * @deprecated As of Version 2.1 of the Java Servlet API,
     * use {@link ServletContext#getRealPath} instead.
     */
    @Override
    public String getRealPath(String path) {
        // TODO dispatcher
        return null;
    }

    /**
     * Returns the Internet Protocol (IP) source port of the client
     * or last proxy that sent the request.
     *
     * @return an integer specifying the port number
     * @since Servlet 2.4
     */
    @Override
    public int getRemotePort() {
        try {
            return ((InetSocketAddress) httpRequest.getRemoteAddress()).getPort();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the host name of the Internet Protocol (IP) interface on
     * which the request was received.
     *
     * @return a <code>String</code> containing the host
     * name of the IP on which the request was received.
     * @since Servlet 2.4
     */
    @Override
    public String getLocalName() {
        try {
            return ((InetSocketAddress) httpRequest.getLocalAddress()).getHostString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the Internet Protocol (IP) address of the interface on
     * which the request  was received.
     *
     * @return a <code>String</code> containing the
     * IP address on which the request was received.
     * @since Servlet 2.4
     */
    @Override
    public String getLocalAddr() {
        try {
            return ((InetSocketAddress) httpRequest.getLocalAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns the Internet Protocol (IP) port number of the interface
     * on which the request was received.
     *
     * @return an integer specifying the port number
     * @since Servlet 2.4
     */
    @Override
    public int getLocalPort() {
        try {
            return ((InetSocketAddress) httpRequest.getLocalAddress()).getPort();
        } catch (Exception e) {
            return this.context.getEngine().getPort();
        }
    }

    /**
     * Gets the servlet context to which this ServletRequest was last
     * dispatched.
     *
     * @return the servlet context to which this ServletRequest was last
     * dispatched
     * @since Servlet 3.0
     */
    @Override
    public ServletContext getServletContext() {
        return this.context;
    }

    /**
     * Puts this request into asynchronous mode, and initializes its
     * {@link AsyncContext} with the original (unwrapped) ServletRequest
     * and ServletResponse objects.
     * <p>
     * <p>Calling this method will cause committal of the associated
     * response to be delayed until {@link AsyncContext#complete} is
     * called on the returned {@link AsyncContext}, or the asynchronous
     * operation has timed out.
     * <p>
     * <p>Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on
     * the returned AsyncContext will return <code>true</code>. Any filters
     * invoked in the <i>outbound</i> direction after this request was put
     * into asynchronous mode may use this as an indication that any request
     * and/or response wrappers that they added during their <i>inbound</i>
     * invocation need not stay around for the duration of the asynchronous
     * operation, and therefore any of their associated resources may be
     * released.
     * <p>
     * <p>This method clears the list of {@link AsyncListener} instances
     * (if any) that were registered with the AsyncContext returned by the
     * previous call to one of the startAsync methods, after calling each
     * AsyncListener at its {@link AsyncListener#onStartAsync onStartAsync}
     * method.
     * <p>
     * <p>Subsequent invocations of this method, or its overloaded
     * variant, will return the same AsyncContext instance, reinitialized
     * as appropriate.
     *
     * @return the (re)initialized AsyncContext
     * @throws IllegalStateException if this request is within the scope of
     *                               a filter or servlet that does not support asynchronous operations
     *                               (that is, {@link #isAsyncSupported} returns false),
     *                               or if this method is called again without any asynchronous dispatch
     *                               (resulting from one of the {@link AsyncContext#dispatch} methods),
     *                               is called outside the scope of any such dispatch, or is called again
     *                               within the scope of the same dispatch, or if the response has
     *                               already been closed
     * @see AsyncContext#dispatch()
     * @since Servlet 3.0
     */
    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO async
        return null;
    }

    /**
     * Puts this request into asynchronous mode, and initializes its
     * {@link AsyncContext} with the given request and response objects.
     * <p>
     * <p>The ServletRequest and ServletResponse arguments must be
     * the same instances, or instances of {@link ServletRequestWrapper} and
     * {@link ServletResponseWrapper} that wrap them, that were passed to the
     * {@link Servlet#service service} method of the Servlet or the
     * {@link Filter#doFilter doFilter} method of the Filter, respectively,
     * in whose scope this method is being called.
     * <p>
     * <p>Calling this method will cause committal of the associated
     * response to be delayed until {@link AsyncContext#complete} is
     * called on the returned {@link AsyncContext}, or the asynchronous
     * operation has timed out.
     * <p>
     * <p>Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on
     * the returned AsyncContext will return <code>false</code>,
     * unless the passed in ServletRequest and ServletResponse arguments
     * are the original ones or do not carry any application-provided wrappers.
     * Any filters invoked in the <i>outbound</i> direction after this
     * request was put into asynchronous mode may use this as an indication
     * that some of the request and/or response wrappers that they added
     * during their <i>inbound</i> invocation may need to stay in place for
     * the duration of the asynchronous operation, and their associated
     * resources may not be released.
     * A ServletRequestWrapper applied during the <i>inbound</i>
     * invocation of a filter may be released by the <i>outbound</i>
     * invocation of the filter only if the given <code>servletRequest</code>,
     * which is used to initialize the AsyncContext and will be returned by
     * a call to {@link AsyncContext#getRequest()}, does not contain said
     * ServletRequestWrapper. The same holds true for ServletResponseWrapper
     * instances.
     * <p>
     * <p>This method clears the list of {@link AsyncListener} instances
     * (if any) that were registered with the AsyncContext returned by the
     * previous call to one of the startAsync methods, after calling each
     * AsyncListener at its {@link AsyncListener#onStartAsync onStartAsync}
     * method.
     * <p>
     * <p>Subsequent invocations of this method, or its zero-argument
     * variant, will return the same AsyncContext instance, reinitialized
     * as appropriate. If a call to this method is followed by a call to its
     * zero-argument variant, the specified (and possibly wrapped) request
     * and response objects will remain <i>locked in</i> on the returned
     * AsyncContext.
     *
     * @param servletRequest  the ServletRequest used to initialize the
     *                        AsyncContext
     * @param servletResponse the ServletResponse used to initialize the
     *                        AsyncContext
     * @return the (re)initialized AsyncContext
     * @throws IllegalStateException if this request is within the scope of
     *                               a filter or servlet that does not support asynchronous operations
     *                               (that is, {@link #isAsyncSupported} returns false),
     *                               or if this method is called again without any asynchronous dispatch
     *                               (resulting from one of the {@link AsyncContext#dispatch} methods),
     *                               is called outside the scope of any such dispatch, or is called again
     *                               within the scope of the same dispatch, or if the response has
     *                               already been closed
     * @since Servlet 3.0
     */
    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        // TODO async
        return null;
    }

    /**
     * Checks if this request has been put into asynchronous mode.
     * <p>
     * <p>A ServletRequest is put into asynchronous mode by calling
     * {@link #startAsync} or
     * {@link #startAsync(ServletRequest, ServletResponse)} on it.
     * <p>
     * <p>This method returns <tt>false</tt> if this request was
     * put into asynchronous mode, but has since been dispatched using
     * one of the {@link AsyncContext#dispatch} methods or released
     * from asynchronous mode via a call to {@link AsyncContext#complete}.
     *
     * @return true if this request has been put into asynchronous mode,
     * false otherwise
     * @since Servlet 3.0
     */
    @Override
    public boolean isAsyncStarted() {
        // TODO async
        return false;
    }

    /**
     * Checks if this request supports asynchronous operation.
     * <p>
     * <p>Asynchronous operation is disabled for this request if this request
     * is within the scope of a filter or servlet that has not been annotated
     * or flagged in the deployment descriptor as being able to support
     * asynchronous handling.
     *
     * @return true if this request supports asynchronous operation, false
     * otherwise
     * @since Servlet 3.0
     */
    @Override
    public boolean isAsyncSupported() {
        // TODO async
        return false;
    }

    /**
     * Gets the AsyncContext that was created or reinitialized by the
     * most recent invocation of {@link #startAsync} or
     * {@link #startAsync(ServletRequest, ServletResponse)} on this request.
     *
     * @return the AsyncContext that was created or reinitialized by the
     * most recent invocation of {@link #startAsync} or
     * {@link #startAsync(ServletRequest, ServletResponse)} on
     * this request
     * @throws IllegalStateException if this request has not been put
     *                               into asynchronous mode, i.e., if neither {@link #startAsync} nor
     *                               {@link #startAsync(ServletRequest, ServletResponse)} has been called
     * @since Servlet 3.0
     */
    @Override
    public AsyncContext getAsyncContext() {
        // TODO async
        return null;
    }

    /**
     * Gets the dispatcher type of this request.
     * <p>
     * <p>The dispatcher type of a request is used by the container
     * to select the filters that need to be applied to the request:
     * Only filters with matching dispatcher type and url patterns will
     * be applied.
     * <p>
     * <p>Allowing a filter that has been configured for multiple
     * dispatcher types to query a request for its dispatcher type
     * allows the filter to process the request differently depending on
     * its dispatcher type.
     * <p>
     * <p>The initial dispatcher type of a request is defined as
     * <code>DispatcherType.REQUEST</code>. The dispatcher type of a request
     * dispatched via {@link RequestDispatcher#forward(ServletRequest, ServletResponse)} or {@link RequestDispatcher#include(ServletRequest, ServletResponse)} is given as <code>DispatcherType.FORWARD</code> or
     * <code>DispatcherType.INCLUDE</code>, respectively, while the
     * dispatcher type of an asynchronous request dispatched via
     * one of the {@link AsyncContext#dispatch} methods is given as
     * <code>DispatcherType.ASYNC</code>. Finally, the dispatcher type of a
     * request dispatched to an error page by the container's error handling
     * mechanism is given as <code>DispatcherType.ERROR</code>.
     *
     * @return the dispatcher type of this request
     * @see DispatcherType
     * @since Servlet 3.0
     */
    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }
}

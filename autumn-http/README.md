# AutumnHTTP
> 基于 Java NIO 的简单 HTTP 服务器

## 简介
AutumnHTTP 是一个基于 Java NIO 开发的 HTTP 服务器/客户端组件，支持基本的 HTTP/1.1 协议。
TomPuss 是一个基于 AutumnHTTP 开发的 Servlet 容器，支持基本的 Servlet API。

## TomPuss 支持特性
- [x] HttpServlet
    - [x] Cookies
    - [x] Session
    - [x] File upload
    - [x] Forward
    - [ ] Async context
    - [ ] WebSocket
- [x] HttpFilter
- [x] EventListener
    - [ ] ServletContextListener
    - [ ] ServletContextAttributeListener
    - [x] ServletRequestListener
    - [ ] ServletRequestAttributeListener
    - [x] HttpSessionAttributeListener
    - [ ] HttpSessionIdListener
    - [x] HttpSessionListener
- [ ] Static resources
- [x] JSP
- [x] Configuration by annotations
- [x] Configuration by web.xml

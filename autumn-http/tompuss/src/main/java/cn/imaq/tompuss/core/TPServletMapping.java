package cn.imaq.tompuss.core;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.MappingMatch;
import java.lang.ref.WeakReference;

@Data
@Builder
@Slf4j
public class TPServletMapping implements HttpServletMapping {
    String path;

    Class<? extends HttpServlet> servletClass;

    WeakReference<? extends HttpServlet> servletRef;

    public HttpServlet getServlet() {
        if (servletRef == null || servletRef.get() == null) {
            synchronized (this) {
                if (servletRef == null || servletRef.get() == null) {
                    servletRef = new WeakReference<>(this.newServletInstance());
                }
            }
        }
        return servletRef.get();
    }

    public HttpServlet newServletInstance() {
        try {
            log.info("Init Servlet " + servletClass.getName());
            HttpServlet servlet = servletClass.newInstance();
            servlet.init();
            return servlet;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Error instantiating Servlet " + servletClass.getName(), e);
        } catch (ServletException e) {
            log.error("Error initiating Servlet " + servletClass.getName(), e);
        }
        return null;
    }

    public void destroyServlet() {
        if (servletRef != null) {
            HttpServlet servlet = servletRef.get();
            if (servlet != null) {
                servlet.destroy();
            }
            servletRef = null;
        }
    }

    @Override
    public String getMatchValue() {
        return path.substring(1);
    }

    @Override
    public String getPattern() {
        return path;
    }

    @Override
    public String getServletName() {
        return servletClass.getSimpleName();
    }

    @Override
    public MappingMatch getMappingMatch() {
        return MappingMatch.PATH;
    }
}

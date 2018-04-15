package org.springframework.boot.web.embedded.tompuss;

import cn.imaq.tompuss.core.TPEngine;
import cn.imaq.tompuss.servlet.TPServletContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;

import javax.servlet.ServletException;
import java.io.File;

public class TomPussServletWebServerFactory extends AbstractServletWebServerFactory {
    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        TomPussWebServer webServer = new TomPussWebServer(this.getPort());
        prepareContext(webServer.getEngine(), initializers);
        return webServer;
    }

    private void prepareContext(TPEngine engine, ServletContextInitializer... initializers) {
        File documentRoot = getValidDocumentRoot();
        TPServletContext context = engine.newWebApp(getContextPath(), getContextPath(), documentRoot);
        if (shouldRegisterJspServlet()) {
            context.enableJsp();
        }
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(context);
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
    }
}

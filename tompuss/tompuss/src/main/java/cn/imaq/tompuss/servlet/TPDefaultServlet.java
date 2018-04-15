package cn.imaq.tompuss.servlet;

import cn.imaq.tompuss.util.TPNotFoundException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class TPDefaultServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String resPath = req.getPathInfo();
        if (resPath.startsWith("/WEB-INF/") || resPath.startsWith("/META-INF/")) {
            resp.sendError(403);
            return;
        }
        ServletContext context = req.getServletContext();
        InputStream is = context.getResourceAsStream(resPath);
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(resPath);
        }
        if (is == null) {
            throw new TPNotFoundException();
        }
        String mimeType = context.getMimeType(resPath);
        resp.reset();
        resp.setContentType(mimeType);
        byte[] buf = new byte[is.available()];
        is.read(buf);
        if (resp instanceof TPHttpServletResponse) {
            // fast-path
            ((TPHttpServletResponse) resp).setBody(buf);
        } else {
            resp.getOutputStream().write(buf);
        }
    }
}

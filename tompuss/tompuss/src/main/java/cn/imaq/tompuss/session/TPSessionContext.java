package cn.imaq.tompuss.session;

import cn.imaq.tompuss.servlet.TPServletContext;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TPSessionContext {
    private TPServletContext context;
    private Map<String, TPHttpSession> sessions = new ConcurrentHashMap<>();

    public TPSessionContext(TPServletContext context) {
        this.context = context;
    }

    public TPHttpSession getSession(String sessId) {
        TPHttpSession session = this.sessions.get(sessId);
        if (session == null) {
            return null;
        }
        if (!session.isValid()) {
            context.getListeners(HttpSessionListener.class).forEach(x -> x.sessionDestroyed(new HttpSessionEvent(session)));
            return null;
        }
        return session;
    }

    public TPHttpSession createSession(String oldSessId) {
        String sessId = oldSessId;
        if (sessId == null) {
            do {
                sessId = "tompuss_" + UUID.randomUUID();
            } while (this.sessions.containsKey(sessId));
        }
        TPHttpSession session = new TPHttpSession(context, sessId);
        this.sessions.put(sessId, session);
        context.getListeners(HttpSessionListener.class).forEach(x -> x.sessionCreated(new HttpSessionEvent(session)));
        return session;
    }
}

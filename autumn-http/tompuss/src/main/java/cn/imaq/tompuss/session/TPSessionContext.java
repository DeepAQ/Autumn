package cn.imaq.tompuss.session;

import cn.imaq.tompuss.servlet.TPServletContext;

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
        if (session == null || !session.isValid()) {
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
        return session;
    }
}

package cn.imaq.tompuss.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TPUrlPattern {
    private String pattern;

    private Type type;

    public TPUrlPattern(String path) {
        if (path.endsWith("/*")) {
            this.type = Type.PREFIX;
            this.pattern = path.substring(0, path.length() - 2);
        } else if (path.startsWith("*.")) {
            this.type = Type.SUFFIX;
            this.pattern = path.substring(1);
        } else if (path.equals("/") || path.isEmpty()) {
            this.type = Type.DEFAULT;
            this.pattern = "";
        } else {
            this.type = Type.EXACT;
            this.pattern = path;
        }
    }

    public Match match(String path) {
        boolean matches = false;
        switch (this.type) {
            case EXACT:
                matches = this.pattern.equals(path);
                break;
            case PREFIX:
                matches = path.startsWith(this.pattern);
                break;
            case SUFFIX:
                matches = path.endsWith(this.pattern);
                break;
            case DEFAULT:
                matches = true;
                break;
        }
        if (matches) {
            return new Match(this.type, this.pattern.length());
        } else {
            return Match.NO_MATCH;
        }
    }

    @Getter
    @AllArgsConstructor
    private enum Type {
        DEFAULT(0),
        SUFFIX(10),
        PREFIX(20),
        EXACT(90);

        private int priority;
    }

    @Getter
    @AllArgsConstructor
    public static class Match implements Comparable<Match> {
        public static final Match NO_MATCH = new Match(Type.DEFAULT, -1);

        private Type type;

        private int length;

        @Override
        public int compareTo(Match other) {
            if (this.type.priority > other.type.priority) {
                return 1;
            } else if ((this.type.priority == other.type.priority) && (this.length > other.length)) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}

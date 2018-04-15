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
        String matched = "";
        switch (this.type) {
            case EXACT:
                matches = this.pattern.equals(path);
                matched = this.pattern;
                break;
            case PREFIX:
                matches = path.startsWith(this.pattern);
                matched = this.pattern;
                break;
            case SUFFIX:
                matches = path.endsWith(this.pattern);
                matched = path;
                break;
            case DEFAULT:
                matches = true;
                break;
        }
        if (matches) {
            return new Match(this.type, this.pattern.length(), matched);
        } else {
            return Match.NO_MATCH;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        DEFAULT(0),
        SUFFIX(10),
        PREFIX(20),
        EXACT(90);

        private int priority;
    }

    @Getter
    @AllArgsConstructor
    public static class Match implements Comparable<Match> {
        public static final Match NO_MATCH = new Match(Type.DEFAULT, -1, "");

        private Type patternType;

        private int patternLength;

        private String matched;

        @Override
        public int compareTo(Match other) {
            if (this.patternType.priority > other.patternType.priority) {
                return 1;
            } else if ((this.patternType.priority == other.patternType.priority) && (this.patternLength > other.patternLength)) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}

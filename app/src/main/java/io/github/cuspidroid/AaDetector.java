package io.github.cuspidroid;

final class AaDetector {
    static final float SCORE_PER_LINE_THRESHOLD = 0.75f;
    static final int LINE_COUNT_MIN = 4;

    private AaDetector() {
    }

    static boolean isLikelyAa(String body) {
        return metrics(body).aa;
    }

    static Metrics metrics(String body) {
        if (body == null) {
            return new Metrics(false, "null", 0, 0, 0, 0, 0f);
        }
        String value = body.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = value.split("\\n", -1);
        int lineCount = 0;
        int score = 0;
        int leadingLayoutScore = 0;
        int doubleSpaceScore = 0;
        for (String line : lines) {
            if (isBlankLine(line)) {
                continue;
            }
            lineCount++;
            int bodyStart = leadingLayoutEnd(line);
            if (bodyStart > 0) {
                score += 2;
                leadingLayoutScore += 2;
            }
            if (containsConsecutiveWhitespace(line, bodyStart)) {
                score++;
                doubleSpaceScore++;
            }
        }
        if (lineCount <= 0) {
            return new Metrics(false, "no-lines", 0, 0, 0, 0, 0f);
        }
        float ratio = score / (float) lineCount;
        boolean aa = lineCount >= LINE_COUNT_MIN && ratio > SCORE_PER_LINE_THRESHOLD;
        return new Metrics(aa, aa ? "score-ratio" : "below",
                lineCount, score, leadingLayoutScore, doubleSpaceScore, ratio);
    }

    private static boolean isBlankLine(String line) {
        if (line == null || line.isEmpty()) {
            return true;
        }
        for (int offset = 0; offset < line.length();) {
            int codePoint = line.codePointAt(offset);
            if (!isWhitespace(codePoint)) {
                return false;
            }
            offset += Character.charCount(codePoint);
        }
        return true;
    }

    private static int leadingLayoutEnd(String line) {
        int offset = 0;
        int count = 0;
        while (offset < line.length()) {
            int codePoint = line.codePointAt(offset);
            if (!isLeadingLayoutCharacter(codePoint)) {
                break;
            }
            offset += Character.charCount(codePoint);
            count++;
        }
        return count >= 2 ? offset : 0;
    }

    private static boolean containsConsecutiveWhitespace(String line, int start) {
        boolean previousWhitespace = false;
        for (int offset = Math.max(0, start); offset < line.length();) {
            int codePoint = line.codePointAt(offset);
            boolean whitespace = isWhitespace(codePoint);
            if (whitespace && previousWhitespace) {
                return true;
            }
            previousWhitespace = whitespace;
            offset += Character.charCount(codePoint);
        }
        return false;
    }

    private static boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint)
                || codePoint == 0x200B
                || codePoint == 0xFEFF;
    }

    private static boolean isLeadingLayoutCharacter(int codePoint) {
        if (isWhitespace(codePoint)
                || (codePoint >= 0x2500 && codePoint <= 0x257F)
                || (codePoint >= 0x23BA && codePoint <= 0x23BD)) {
            return true;
        }
        switch (codePoint) {
            case '.':
            case 0xFF0E:
            case ',':
            case 0xFF0C:
            case 0x3002:
            case 0xFF61:
            case 0x3001:
            case 0xFF64:
            case '\'':
            case 0xFF07:
            case 0x2018:
            case 0x2019:
            case '"':
            case 0xFF02:
            case 0x201C:
            case 0x201D:
            case ':':
            case 0xFF1A:
            case ';':
            case 0xFF1B:
            case '/':
            case 0xFF0F:
            case '\\':
            case 0xFF3C:
            case '_':
            case 0xFF3F:
            case 0xFE33:
            case 0xFE34:
            case 0x00AF:
            case 0x203E:
            case 0xFFE3:
            case '|':
            case 0xFF5C:
            case 0xFFE8:
            case 0x2227: // logical and
            case 0x2228: // logical or
            case 0x2229: // intersection
            case 0x222A: // union
                return true;
            default:
                return false;
        }
    }

    static final class Metrics {
        final boolean aa;
        final String reason;
        final int lineCount;
        final int score;
        final int leadingLayoutScore;
        final int doubleSpaceScore;
        final float ratio;

        Metrics(boolean aa, String reason, int lineCount, int score,
                int leadingLayoutScore, int doubleSpaceScore, float ratio) {
            this.aa = aa;
            this.reason = reason;
            this.lineCount = lineCount;
            this.score = score;
            this.leadingLayoutScore = leadingLayoutScore;
            this.doubleSpaceScore = doubleSpaceScore;
            this.ratio = ratio;
        }
    }
}

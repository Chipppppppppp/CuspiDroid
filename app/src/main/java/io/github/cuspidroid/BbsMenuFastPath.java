package io.github.cuspidroid;

/** Cheap cases in a board menu; null delegates unusual input to the full parser. */
final class BbsMenuFastPath {
    private BbsMenuFastPath() {}

    static String boardName(String path) {
        if (path == null || !path.startsWith("/") || !path.endsWith("/") || path.length() < 3) return null;
        String board = path.substring(1, path.length() - 1);
        if (board.indexOf('/') >= 0 || board.indexOf('.') >= 0
                || "cdn-cgi".equalsIgnoreCase(board) || "image".equalsIgnoreCase(board)
                || "sp".equalsIgnoreCase(board)) return null;
        return board;
    }

    static String plainLabel(String html) {
        if (html == null) return null;
        String value = html.trim();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '<' || c == '&' || c < ' ' || c == '\u00a0' || c == '\uE000'
                    || (c == ' ' && i > 0 && value.charAt(i - 1) == ' ')) return null;
        }
        return value;
    }
}

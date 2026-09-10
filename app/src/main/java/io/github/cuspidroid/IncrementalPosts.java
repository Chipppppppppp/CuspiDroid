package io.github.cuspidroid;

import java.util.List;
import java.util.Map;

final class IncrementalPosts {
    interface PostNumber<T> {
        int get(T post);
    }

    /** Validate the entire batch before changing the list used by existing views. */
    static <T> void append(List<T> loaded, Map<Integer, T> byNumber, List<T> incoming,
                           PostNumber<T> number) {
        int last = 0;
        for (T post : loaded) last = Math.max(last, number.get(post));
        for (T post : incoming) {
            int next = number.get(post);
            if (next <= last) throw new IllegalStateException("Invalid incremental post sequence");
            last = next;
        }
        for (T post : incoming) {
            loaded.add(post);
            byNumber.put(number.get(post), post);
        }
    }
}

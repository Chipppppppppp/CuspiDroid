package io.github.cuspidroid;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

final class IncrementalPosts {
    interface PostNumber<T> {
        int get(T post);
    }

    /** Full HTML/DAT reloads may contain old or deleted posts; append only newer numbers. */
    static <T> List<T> newPostsFromSnapshot(List<T> loaded, List<T> snapshot, PostNumber<T> number) {
        int last = 0;
        for (T post : loaded) last = Math.max(last, number.get(post));
        List<T> result = new ArrayList<>();
        int previous = 0;
        for (T post : snapshot) {
            int next = number.get(post);
            if (next <= previous) throw new IllegalStateException("Invalid thread refresh post sequence");
            if (next > last) result.add(post);
            previous = next;
        }
        if (snapshot.isEmpty() || previous < last) {
            throw new IllegalStateException("Incomplete thread refresh response");
        }
        return result;
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

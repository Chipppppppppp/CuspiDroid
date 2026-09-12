package io.github.cuspidroid;

import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Exercises real window attachment/layout/draw ordering without network or thread fixtures. */
public class ThreadScrollRestoreTest extends InstrumentationTestCase {
    private MainActivity activity;

    @Override protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = (MainActivity) getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
    }

    @Override protected void tearDown() throws Exception {
        getInstrumentation().runOnMainSync(() -> activity.finish());
        super.tearDown();
    }

    public void testFirstVisibleDrawRestoresMiddleAfterVisibilityLayout() throws Exception {
        verifyRestore(700, false, false);
    }

    public void testPendingRestoreSurvivesDetachAndReattach() throws Exception {
        verifyRestore(1200, true, false);
    }

    public void testShortenedThreadClampsPositionBeforeFirstDraw() throws Exception {
        verifyRestore(9000, false, true);
    }

    private void verifyRestore(int savedY, boolean reattach, boolean shortThread) throws Exception {
        CountDownLatch drawn = new CountDownLatch(1);
        AtomicInteger firstY = new AtomicInteger(-1);
        AtomicInteger expectedY = new AtomicInteger(-1);
        getInstrumentation().runOnMainSync(() -> {
            try {
                Class<?> tabClass = Class.forName(MainActivity.class.getName() + "$CuspTab");
                Constructor<?> constructor = tabClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object tab = constructor.newInstance();
                Class<?> scrollClass = Class.forName(MainActivity.class.getName() + "$ThreadScrollView");
                Constructor<?> scrollConstructor = scrollClass.getDeclaredConstructor(android.content.Context.class);
                scrollConstructor.setAccessible(true);
                ScrollView scroll = (ScrollView) scrollConstructor.newInstance(activity);
                FrameLayout root = new FrameLayout(activity);
                FrameLayout reader = new FrameLayout(activity) {
                    @Override public void setVisibility(int visibility) {
                        boolean revealing = getVisibility() != visibility && visibility == View.VISIBLE;
                        super.setVisibility(visibility);
                        if (revealing) {
                            // A visibility/focus layout may move the viewport after restoration.
                            scroll.scrollTo(0, 0);
                            requestLayout();
                        }
                    }
                };
                View content = new View(activity) {
                    @Override protected void onDraw(android.graphics.Canvas canvas) {
                        super.onDraw(canvas);
                        if (firstY.compareAndSet(-1, scroll.getScrollY())) {
                            expectedY.set(Math.min(savedY, Math.max(0, getHeight() - scroll.getHeight())));
                            drawn.countDown();
                        }
                    }
                };
                content.setWillNotDraw(false);
                content.setMinimumHeight(shortThread ? 200 : 5000);
                scroll.addView(content, new ScrollView.LayoutParams(-1, shortThread ? 200 : 5000));
                reader.addView(scroll);
                reader.setVisibility(View.INVISIBLE);
                set(tab, "readerView", reader);
                set(tab, "threadScroll", scroll);
                set(tab, "hasPendingThreadRefreshScroll", true);
                set(tab, "pendingThreadRefreshScrollY", savedY);
                Method guard = MainActivity.class.getDeclaredMethod("guardThreadScrollRestore", tabClass, ScrollView.class);
                guard.setAccessible(true);
                guard.invoke(activity, tab, scroll);
                activity.setContentView(root);
                root.addView(reader);
                if (reattach) {
                    set(tab, "threadRendering", true);
                    root.post(() -> {
                        root.removeView(reader);
                        // A detached pending listener must not cancel the replacement window's draw.
                        root.postDelayed(() -> {
                            try { set(tab, "threadRendering", false); }
                            catch (Exception error) { throw new AssertionError(error); }
                            root.addView(reader);
                        }, 100);
                    });
                }
            } catch (Exception error) {
                throw new AssertionError(error);
            }
        });
        assertTrue("Restoration never produced a frame", drawn.await(10, TimeUnit.SECONDS));
        assertEquals("The first visible frame must use the restored position", expectedY.get(), firstY.get());
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}

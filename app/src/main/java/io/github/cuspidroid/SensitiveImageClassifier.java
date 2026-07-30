package io.github.cuspidroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Single on-device classifier for violent or filthy imagery.
 */
final class SensitiveImageClassifier {
    private static final int INPUT_SIZE = 96;
    private static final float SENSITIVE_THRESHOLD = 0.35f;
    private static final float OUTPUT_SCALE = 1f / 256f;
    private static volatile SensitiveImageClassifier instance;

    private final Context context;
    private Interpreter interpreter;
    private boolean loadAttempted;

    static SensitiveImageClassifier get(Context context) {
        SensitiveImageClassifier current = instance;
        if (current == null) {
            synchronized (SensitiveImageClassifier.class) {
                current = instance;
                if (current == null) {
                    current = new SensitiveImageClassifier(context.getApplicationContext());
                    instance = current;
                }
            }
        }
        return current;
    }

    private SensitiveImageClassifier(Context context) {
        this.context = context;
    }

    synchronized boolean isSensitive(Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        Interpreter active = interpreter();
        if (active == null) {
            return false;
        }
        return unsafeScore(bitmap, active) >= SENSITIVE_THRESHOLD;
    }

    private float unsafeScore(Bitmap bitmap, Interpreter active) {
        try {
            byte[][] output = new byte[1][1];
            active.run(input(bitmap), output);
            return (output[0][0] & 0xff) * OUTPUT_SCALE;
        } catch (Throwable ignored) {
            return 0f;
        }
    }

    private Interpreter interpreter() {
        if (interpreter != null) {
            return interpreter;
        }
        if (loadAttempted) {
            return null;
        }
        loadAttempted = true;
        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(Math.max(1,
                    Math.min(2, Runtime.getRuntime().availableProcessors())));
            interpreter = new Interpreter(
                    loadMappedAsset("violence_filth_v1_int8.tflite"), options);
        } catch (Throwable ignored) {
            interpreter = null;
        }
        return interpreter;
    }

    private ByteBuffer input(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(
                bitmap, INPUT_SIZE, INPUT_SIZE, true);
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        if (scaled != bitmap) {
            scaled.recycle();
        }
        ByteBuffer data = ByteBuffer.allocateDirect(pixels.length * 3);
        for (int color : pixels) {
            data.put((byte) Color.red(color));
            data.put((byte) Color.green(color));
            data.put((byte) Color.blue(color));
        }
        data.rewind();
        return data;
    }

    private MappedByteBuffer loadMappedAsset(String name) throws Exception {
        try (AssetFileDescriptor descriptor = context.getAssets().openFd(name);
             FileInputStream input = new FileInputStream(descriptor.getFileDescriptor());
             FileChannel channel = input.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY,
                    descriptor.getStartOffset(), descriptor.getDeclaredLength());
        }
    }
}

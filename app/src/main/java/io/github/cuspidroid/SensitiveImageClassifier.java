package io.github.cuspidroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Single on-device classifier for inappropriate imagery, including NSFW,
 * graphic/NSFL and fecal content.
 */
final class SensitiveImageClassifier {
    private static final int INPUT_SIZE = 224;
    private static final float SENSITIVE_THRESHOLD = 0.64f;
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
        float fullFrameScore = unsafeScore(bitmap, active);
        if (fullFrameScore >= SENSITIVE_THRESHOLD) {
            return true;
        }
        if (fullFrameScore < 0.20f) {
            return false;
        }

        int cropWidth = Math.max(1, Math.round(bitmap.getWidth() * 0.65f));
        int cropHeight = Math.max(1, Math.round(bitmap.getHeight() * 0.65f));
        int[][] origins = {
                {0, 0},
                {bitmap.getWidth() - cropWidth, 0},
                {0, bitmap.getHeight() - cropHeight},
                {bitmap.getWidth() - cropWidth, bitmap.getHeight() - cropHeight}
        };
        for (int[] origin : origins) {
            Bitmap crop = Bitmap.createBitmap(bitmap, origin[0], origin[1],
                    cropWidth, cropHeight);
            float score = unsafeScore(crop, active);
            if (crop != bitmap) {
                crop.recycle();
            }
            if (score >= SENSITIVE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private float unsafeScore(Bitmap bitmap, Interpreter active) {
        try {
            float[][] output = new float[1][1];
            active.run(input(bitmap), output);
            return output[0][0];
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
                    loadMappedAsset("inappropriate_image.tflite"), options);
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
        ByteBuffer data = ByteBuffer.allocateDirect(pixels.length * 3 * 4)
                .order(ByteOrder.nativeOrder());
        for (int channel = 0; channel < 3; channel++) {
            for (int color : pixels) {
                data.putFloat(channel == 0 ? Color.red(color)
                        : channel == 1 ? Color.green(color) : Color.blue(color));
            }
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

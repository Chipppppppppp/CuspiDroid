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

/** Shared wrapper around the original graphic-violence model. */
final class SensitiveImageClassifier {
    private static final int INPUT_SIZE = 320;
    private static final float SENSITIVE_THRESHOLD = 0.995f;
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
                    current = new SensitiveImageClassifier(
                            context.getApplicationContext());
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
        Interpreter active = interpreter();
        if (active == null || bitmap == null) {
            return false;
        }
        try {
            float[][] output = new float[1][3];
            active.run(input(bitmap), output);
            int winner = 0;
            for (int i = 1; i < output[0].length; i++) {
                if (output[0][i] > output[0][winner]) {
                    winner = i;
                }
            }
            return winner != 2 && output[0][winner] >= SENSITIVE_THRESHOLD;
        } catch (Throwable ignored) {
            return false;
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
            interpreter = new Interpreter(loadMappedAsset(
                    "graphic_violence.tflite"));
        } catch (Throwable ignored) {
            interpreter = null;
        }
        return interpreter;
    }

    private ByteBuffer input(Bitmap bitmap) {
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap cropped = Bitmap.createBitmap(bitmap,
                Math.max(0, (bitmap.getWidth() - cropSize) / 2),
                Math.max(0, (bitmap.getHeight() - cropSize) / 2),
                cropSize, cropSize);
        Bitmap scaled = Bitmap.createScaledBitmap(
                cropped, INPUT_SIZE, INPUT_SIZE, true);
        if (cropped != bitmap && cropped != scaled) {
            cropped.recycle();
        }
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0,
                INPUT_SIZE, INPUT_SIZE);
        ByteBuffer data = ByteBuffer.allocateDirect(
                INPUT_SIZE * INPUT_SIZE * 3 * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int color : pixels) {
            data.putFloat(Color.red(color));
            data.putFloat(Color.green(color));
            data.putFloat(Color.blue(color));
        }
        data.rewind();
        if (scaled != bitmap) {
            scaled.recycle();
        }
        return data;
    }

    private MappedByteBuffer loadMappedAsset(String name) throws Exception {
        try (AssetFileDescriptor descriptor = context.getAssets().openFd(name);
             FileInputStream input = new FileInputStream(
                     descriptor.getFileDescriptor());
             FileChannel channel = input.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY,
                    descriptor.getStartOffset(), descriptor.getDeclaredLength());
        }
    }
}

package io.github.cuspidroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * On-device ensemble for graphic violence and feces. The feces detector is the
 * mobile YOLOX-nano model trained from the CC BY 4.0 ScatSpotter dataset.
 */
final class SensitiveImageClassifier {
    private static final int POOP_INPUT_SIZE = 416;
    private static final float POOP_THRESHOLD = 0.25f;
    private static final float POOP_CROP_THRESHOLD = 0.30f;
    private static volatile SensitiveImageClassifier instance;

    private final Context context;
    private Interpreter graphicInterpreter;
    private boolean graphicLoadAttempted;
    private Interpreter poopInterpreter;
    private boolean poopLoadAttempted;

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
        return bitmap != null && (isGraphicViolence(bitmap) || containsFeces(bitmap));
    }

    private boolean isGraphicViolence(Bitmap bitmap) {
        Interpreter interpreter = graphicInterpreter();
        if (interpreter == null) {
            return false;
        }
        try {
            float[][] output = new float[1][3];
            interpreter.run(graphicInput(bitmap), output);
            int winner = 0;
            for (int i = 1; i < output[0].length; i++) {
                if (output[0][i] > output[0][winner]) {
                    winner = i;
                }
            }
            return winner != 2 && output[0][winner] >= 0.995f;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Interpreter graphicInterpreter() {
        if (graphicInterpreter != null) {
            return graphicInterpreter;
        }
        if (graphicLoadAttempted) {
            return null;
        }
        graphicLoadAttempted = true;
        try {
            graphicInterpreter = new Interpreter(loadMappedAsset("graphic_violence.tflite"));
        } catch (Throwable ignored) {
            graphicInterpreter = null;
        }
        return graphicInterpreter;
    }

    private boolean containsFeces(Bitmap bitmap) {
        Interpreter interpreter = poopInterpreter();
        if (interpreter == null) {
            return false;
        }
        float fullFrameScore = poopScore(bitmap, interpreter);
        if (fullFrameScore >= POOP_THRESHOLD) {
            return true;
        }
        if (fullFrameScore < 0.10f) {
            return false;
        }
        // ScatSpotter is trained on relatively small outdoor targets. Overlapping
        // crops also cover close-up human stool photos, where the full-frame
        // detector otherwise tends to under-score the object.
        int cropWidth = Math.max(1, Math.round(bitmap.getWidth() * 0.65f));
        int cropHeight = Math.max(1, Math.round(bitmap.getHeight() * 0.65f));
        int[][] origins = {
                {0, 0},
                {bitmap.getWidth() - cropWidth, 0},
                {0, bitmap.getHeight() - cropHeight},
                {bitmap.getWidth() - cropWidth, bitmap.getHeight() - cropHeight}
        };
        for (int[] origin : origins) {
            Bitmap crop = Bitmap.createBitmap(bitmap, origin[0], origin[1], cropWidth, cropHeight);
            float score = poopScore(crop, interpreter);
            if (crop != bitmap) {
                crop.recycle();
            }
            if (score >= POOP_CROP_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private float poopScore(Bitmap bitmap, Interpreter interpreter) {
        try {
            float[][][] output = new float[1][6][3549];
            interpreter.run(poopInput(bitmap), output);
            float highest = 0f;
            for (int anchor = 0; anchor < output[0][0].length; anchor++) {
                float width = output[0][2][anchor];
                float height = output[0][3][anchor];
                float confidence = output[0][4][anchor] * output[0][5][anchor];
                if (width > 0f && height > 0f && confidence > highest) {
                    highest = confidence;
                }
            }
            return highest;
        } catch (Throwable ignored) {
            return 0f;
        }
    }

    private Interpreter poopInterpreter() {
        if (poopInterpreter != null) {
            return poopInterpreter;
        }
        if (poopLoadAttempted) {
            return null;
        }
        poopLoadAttempted = true;
        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(Math.max(1,
                    Math.min(2, Runtime.getRuntime().availableProcessors())));
            poopInterpreter = new Interpreter(
                    loadMappedAsset("yolox_nano_poop.tflite"), options);
        } catch (Throwable ignored) {
            poopInterpreter = null;
        }
        return poopInterpreter;
    }

    private ByteBuffer graphicInput(Bitmap bitmap) {
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap cropped = Bitmap.createBitmap(bitmap,
                Math.max(0, (bitmap.getWidth() - cropSize) / 2),
                Math.max(0, (bitmap.getHeight() - cropSize) / 2),
                cropSize, cropSize);
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, 320, 320, true);
        if (cropped != bitmap) {
            cropped.recycle();
        }
        ByteBuffer data = ByteBuffer.allocateDirect(320 * 320 * 3 * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        int[] pixels = new int[320 * 320];
        scaled.getPixels(pixels, 0, 320, 0, 0, 320, 320);
        for (int color : pixels) {
            data.putFloat(Color.red(color));
            data.putFloat(Color.green(color));
            data.putFloat(Color.blue(color));
        }
        data.rewind();
        scaled.recycle();
        return data;
    }

    private ByteBuffer poopInput(Bitmap bitmap) {
        float scale = Math.min(POOP_INPUT_SIZE / (float) bitmap.getWidth(),
                POOP_INPUT_SIZE / (float) bitmap.getHeight());
        int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
        int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Bitmap letterboxed = Bitmap.createBitmap(
                POOP_INPUT_SIZE, POOP_INPUT_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(letterboxed);
        canvas.drawColor(Color.rgb(114, 114, 114));
        canvas.drawBitmap(scaled, (POOP_INPUT_SIZE - width) / 2f,
                (POOP_INPUT_SIZE - height) / 2f, new Paint(Paint.FILTER_BITMAP_FLAG));
        if (scaled != bitmap) {
            scaled.recycle();
        }
        int[] pixels = new int[POOP_INPUT_SIZE * POOP_INPUT_SIZE];
        letterboxed.getPixels(pixels, 0, POOP_INPUT_SIZE, 0, 0,
                POOP_INPUT_SIZE, POOP_INPUT_SIZE);
        letterboxed.recycle();
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

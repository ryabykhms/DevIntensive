package com.softdesign.devintensive.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Трансформация округления контуров изображения. Используется в библиотеке
 * {@link com.squareup.picasso.Picasso} и реализует её интерфейс {@link Transformation}
 * @author ryabykh_ms
 */
public class RoundedImageTransformation implements Transformation {

    /**
     * Делаем изображение круглым. Уменьшаем его размер и округляем.
     * recycle() нужно вызывать обязательно, чтобы не произошло OutOfMemory
     * @param source - исходное изображение
     * @return - трансформированное изображение
     */
    @Override
    public Bitmap transform(Bitmap source) {

        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size/2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "Rounded Image";
    }
}

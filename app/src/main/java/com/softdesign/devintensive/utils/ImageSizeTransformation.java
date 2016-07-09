package com.softdesign.devintensive.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Трансформация изменения размера изображения. Необходима, так как при получении изображения
 * с камеры, оно занимает много места. Используется в библиотеке {@link com.squareup.picasso.Picasso}
 * и реализует её интерфейс {@link Transformation}
 * @author ryabykh_ms
 */
public class ImageSizeTransformation implements Transformation {

    private static final int MINIMUM_IMAGE_WIDTH = 500;
    public static final int MINIMUM_IMAGE_HEIGHT = 256;

    /**
     * Уменьшаем размер изображения, пока он не станет меньше MINIMUM_IMGAE_WIDTH и
     * MINIMUM_IMAGE_HEIGHT соответственно
     * @param source - исходное изображение
     * @return - трансформируемое изображение
     */
    @Override
    public Bitmap transform(Bitmap source) {
        int xSize = source.getWidth();
        int ySize = source.getHeight();
        while(xSize > MINIMUM_IMAGE_WIDTH || ySize > MINIMUM_IMAGE_HEIGHT) {
            xSize /= 2;
            ySize /= 2;
        }
        Bitmap result = Bitmap.createScaledBitmap(source, xSize, ySize, false);

        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "Image Size";
    }
}

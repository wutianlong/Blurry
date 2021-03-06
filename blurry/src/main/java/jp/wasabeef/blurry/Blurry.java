package jp.wasabeef.blurry;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import jp.wasabeef.blurry.internal.Blur;
import jp.wasabeef.blurry.internal.BlurFactor;
import jp.wasabeef.blurry.internal.BlurTask;
import jp.wasabeef.blurry.internal.Helper;

/**
 * Copyright (C) 2015 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Blurry {

    private static final String TAG = Blurry.class.getSimpleName();

    public static Composer with(Context context) {
        return new Composer(context);
    }

    public static void delete(ImageView image) {
        image.setImageDrawable(null);
    }

    public static void delete(ViewGroup group) {
        View view = group.findViewWithTag(TAG);
        if (view != null) {
            group.removeView(view);
        }
    }

    public static class Composer {

        private View blurredView;
        private Context context;
        private BlurFactor factor;
        private boolean async;

        public Composer(Context context) {
            this.context = context;
            blurredView = new View(context);
            blurredView.setTag(TAG);
            factor = new BlurFactor();
        }

        public Composer radius(int radius) {
            factor.radius = radius;
            return this;
        }

        public Composer sampling(int sampling) {
            factor.sampling = sampling;
            return this;
        }

        public Composer color(int color) {
            factor.color = color;
            return this;
        }

        public Composer async() {
            this.async = true;
            return this;
        }

        public ImageComposer capture(View capture) {
            return new ImageComposer(context, capture, factor, async);
        }

        public void onto(final View target) {
            if (target instanceof ViewGroup) {
                factor.width = target.getMeasuredWidth();
                factor.height = target.getMeasuredHeight();

                if (async) {
                    BlurTask.execute(target, factor, new BlurTask.Callback() {
                        @Override
                        public void done(BitmapDrawable drawable) {
                            Helper.setBackground(blurredView, drawable);
                            ((ViewGroup) target).addView(blurredView);
                        }
                    });
                } else {
                    Drawable drawable =
                            new BitmapDrawable(context.getResources(), Blur.rs(target, factor));

                    Helper.setBackground(blurredView, drawable);
                    ((ViewGroup) target).addView(blurredView);
                }
            } else {
                throw new IllegalArgumentException("View parent must be ViewGroup");
            }
        }
    }

    public static class ImageComposer {

        private Context context;
        private View capture;
        private BlurFactor factor;
        private boolean async;

        public ImageComposer(Context context, View capture, BlurFactor factor, boolean async) {
            this.context = context;
            this.capture = capture;
            this.factor = factor;
            this.async = async;
        }

        public void into(final ImageView target) {
            factor.width = capture.getMeasuredWidth();
            factor.height = capture.getMeasuredHeight();

            if (async) {
                BlurTask.execute(capture, factor, new BlurTask.Callback() {
                    @Override
                    public void done(BitmapDrawable drawable) {
                        target.setImageDrawable(drawable);
                    }
                });
            } else {
                Drawable drawable =
                        new BitmapDrawable(context.getResources(), Blur.rs(capture, factor));
                target.setImageDrawable(drawable);
            }
        }
    }
}

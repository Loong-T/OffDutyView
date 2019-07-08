/*
 *    Copyright 2019 Xuqiang ZHENG
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.nerd_is.offdutyview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * @author Xuqiang ZHENG on 2015/3/2.
 */
public class Utils {

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static float sp2Px(int sp, DisplayMetrics metrics) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static float dp2Px(int dp, DisplayMetrics metrics) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, metrics);
    }

    public static float px2dp(float px, DisplayMetrics metrics) {
        final float scale = metrics.density;
        return px / scale;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static float px2sp(float px, DisplayMetrics metrics) {
        final float fontScale = metrics.scaledDensity;
        return px / fontScale;
    }

    /**
     * 获得设备的屏幕高度
     */
    public static int getDeviceHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}

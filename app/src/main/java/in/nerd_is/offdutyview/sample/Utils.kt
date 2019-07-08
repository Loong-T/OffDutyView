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

package `in`.nerd_is.offdutyview.sample

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue


/**
 * @author Xuqiang ZHENG on 18/8/26.
 */

fun sp2Px(sp: Int, metrics: DisplayMetrics): Float {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics)
}

fun dp2Px(dp: Int, metrics: DisplayMetrics): Float {
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp.toFloat(), metrics)
}

fun px2dp(px: Float, metrics: DisplayMetrics): Float {
  val scale = metrics.density
  return px / scale
}

/**
 * 将px值转换为sp值，保证文字大小不变
 */
fun px2sp(px: Float, metrics: DisplayMetrics): Float {
  val fontScale = metrics.scaledDensity
  return px / fontScale
}

fun getScreenHeight(context: Context): Int {
  return context.resources.displayMetrics.heightPixels
}
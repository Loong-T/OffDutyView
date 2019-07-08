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

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * @author Xuqiang ZHENG on 18/8/29.
 */
public class PointFEvaluator implements TypeEvaluator<PointF> {

    private PointF result;

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float x = startValue.x + (endValue.x - startValue.x) * fraction;
        float y = startValue.y + (endValue.y - startValue.y) * fraction;

        if (result == null) {
            result = new PointF(x, y);
        } else {
            result.set(x, y);
        }
        return result;
    }
}

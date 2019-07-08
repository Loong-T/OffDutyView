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

import androidx.annotation.ColorInt;

/**
 * @author Xuqiang ZHENG on 18/9/11.
 */
public class Particle {
    public int x;
    public int y;
    @ColorInt public int color;
    public float radius;

    public Particle(int x, int y, @ColorInt int color, float radius) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.radius = radius;
    }
}

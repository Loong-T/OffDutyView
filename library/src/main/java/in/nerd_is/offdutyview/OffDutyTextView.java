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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author Xuqiang ZHENG on 18/8/16.
 */
public class OffDutyTextView extends TextView {

    private OffDutyDelegate delegate;

    public OffDutyTextView(Context context) {
        super(context);
        delegate = new OffDutyDelegate(this, null);
    }

    public OffDutyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        delegate = new OffDutyDelegate(this, attrs);
    }

    public OffDutyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OffDutyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return delegate.onTouchEvent(event);
    }

    public void setOnOffDutyListener(OnOffDutyListener listener) {
        delegate.setOnOffDutyListener(listener);
    }

    public void setOnCancelListener(OnCancelListener listener) {
        delegate.setOnCancelListener(listener);
    }

    public void resetView() {
        delegate.resetView();
    }

    public boolean isBezierColorOverlay() {
        return delegate.isBezierColorOverlay();
    }

    public void setBezierColorOverlay(boolean bezierColorOverlay) {
        delegate.setBezierColorOverlay(bezierColorOverlay);
    }

    public int getBezierColor() {
        return delegate.getBezierColor();
    }

    public void setBezierColor(int bezierColor) {
        delegate.setBezierColor(bezierColor);
    }

    public int getMaxDistance() {
        return delegate.getMaxDistance();
    }

    public void setMaxDistance(int maxDistance) {
        delegate.setMaxDistance(maxDistance);
    }
}

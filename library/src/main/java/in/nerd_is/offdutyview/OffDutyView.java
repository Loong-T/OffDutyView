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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

/**
 * @author Xuqiang ZHENG on 2015/3/2.
 */
public class OffDutyView extends FrameLayout {

    private TextView mMainView;
    private ImageView mAnimView;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private OnOffDutyListener mListener;

    private int mColor;
    private boolean mIsDisplayText;
    private String mText;
    private int mTextColor;
    private float mTextSize;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;
    private Paint mPaint;
    private Path mPath;
    private Rect mRect;

    private boolean mIsTouched;
    private boolean mIsFirst = true;
    private float mOriginCenterX;
    private float mOriginCenterY;
    private float mOriginRadius;
    private float mCurrentRadius;
    private float mDistance;
    private float mTouchedY;
    private float mTouchedX;
    private float mMaxDistance;
    private boolean mWillDrawCircle;

    private View mTarget;
    private float mTargetOffsetX;
    private float mTargetOffsetY;
    private float mLocationX;
    private float mLocationY;

    // 拖动时变小的那个圆上的点
    // coordinate of origin circle
    private float mOriginCoordiAX;
    private float mOriginCoordiAY;
    private float mOriginCoordiBX;
    private float mOriginCoordiBY;
    // 文字处的圆上的点
    // coordinate of moving circle
    private float mMovingCoordiAX;
    private float mMovingCoordiAY;
    private float mMovingCoordiBX;
    private float mMovingCoordiBY;
    // coordinate of control point;
    private float mControlCoordiAX;
    private float mControlCoordiAY;
    private float mControlCoordiBX;
    private float mControlCoordiBY;

    private boolean mDraggable;

    public OffDutyView(Context context) {
        super(context);
    }

    public OffDutyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OffDutyView);
        mColor = a.getColor(R.styleable.OffDutyView_odvColor, res.getColor(R.color.md_red_500));
        mText = a.getString(R.styleable.OffDutyView_odvText);
        if (mText == null) mText = "";
        mIsDisplayText = a.getBoolean(R.styleable.OffDutyView_odvDisplayText, true);
        mTextColor = a.getColor(R.styleable.OffDutyView_odvTextColor, Color.WHITE);
        mTextSize = a.getDimension(R.styleable.OffDutyView_odvTextSize,
                res.getDimension(R.dimen.small_text_size));
        int defHorizontalPadding = (int) Utils.dp2Px(6, res.getDisplayMetrics());
        int defVerticalPadding = (int) Utils.dp2Px(1, res.getDisplayMetrics());
        mPaddingLeft = a.getDimensionPixelSize(R.styleable.OffDutyView_odvPaddingLeft,
                defHorizontalPadding);
        mPaddingTop =
                a.getDimensionPixelSize(R.styleable.OffDutyView_odvPaddingTop, defVerticalPadding);
        mPaddingRight = a.getDimensionPixelSize(R.styleable.OffDutyView_odvPaddingRight,
                defHorizontalPadding);
        mPaddingBottom = a.getDimensionPixelSize(R.styleable.OffDutyView_odvPaddingBottom,
                defVerticalPadding);
        mDraggable = a.getBoolean(R.styleable.OffDutyView_odvDraggable, true);
        a.recycle();

        init();
    }

    public OffDutyView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public void init() {
        setWillNotDraw(false);
        mWillDrawCircle = !mIsDisplayText;

//        Drawable drawable = getResources().getDrawable(R.drawable.off_duty_anim);
//        mDrawableWidth = drawable.getIntrinsicWidth();
//        mDrawableHeight = drawable.getIntrinsicHeight();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
        mPath = new Path();
        mRect = new Rect();
        mMaxDistance = Utils.getDeviceHeight(getContext()) / 8;

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        mAnimView = new ImageView(getContext());
        mAnimView.setLayoutParams(params);
//        mAnimView.setImageResource(R.drawable.off_duty_anim);
        mAnimView.setVisibility(View.GONE);
        addView(mAnimView);

        mMainView = new TextView(getContext());
        mMainView.setLayoutParams(params);
        mMainView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        setTextSize(mTextSize);
        mMainView.setTextColor(mTextColor);
        setMainViewText(mText);
        addView(mMainView);
        mMainView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        if (mIsFirst && mMainView.getWidth() > 0) {
                            if (mTarget != null && mTarget.getWidth() <= 0) return;

                            mIsFirst = false;
                            lockOnTarget();
                            setBackground((mMainView.getHeight()) / 2, mColor);
                            calcRadius();
                        }
                    }
                });
    }

    public boolean isMotionEventInBound(MotionEvent event) {
        mMainView.getDrawingRect(mRect);
        int[] locations = new int[2];
        mMainView.getLocationInWindow(locations);
        mRect.left = locations[0];
        mRect.top = locations[1];
        mRect.right = mRect.right + locations[0];
        mRect.bottom = mRect.bottom + locations[1];
        return mRect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mControlledByForce) {
            return true;
        }

        updateTouchedPosition(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDraggable && isMotionEventInBound(event)) {
                    mIsTouched = mDraggable;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsTouched) {
                    setThisX(mTouchedX);
                    setThisY(mTouchedY);
                    mDistance = calcDistance(event.getX(), event.getY());
                    changeRadiusBaseOnDistance(mDistance);

                    postInvalidate();
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsTouched) {
                    mIsTouched = false;
                    if (!isReachEdge()) {
                        startBackAnimation(500);
                    } else {
                        if (null != mListener) mListener.onOffDuty(this);

                        mWillDrawCircle = false;
                        mMainView.setVisibility(GONE);
                        mAnimView.setVisibility(VISIBLE);

                        AnimationDrawable drawable = (AnimationDrawable) mAnimView.getDrawable();
                        int duration = 0;
                        for (int i = 0; i < drawable.getNumberOfFrames(); ++i) {
                            duration += drawable.getDuration(i);
                        }
                        drawable.setOneShot(true);
                        drawable.start();
                        postDelayed(new Runnable() {
                            @Override public void run() {
                                mAnimView.setVisibility(GONE);
                                OffDutyView.this.setVisibility(GONE);
                                setThisX(mOriginCenterX);
                                setThisY(mOriginCenterY);
                            }
                        }, duration);
                    }
                    return true;
                }
                break;
        }

        return false;
    }

    @Override protected void onDraw(Canvas canvas) {
        if (isReachEdge()) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY);
            canvas.drawCircle(mOriginCenterX, mOriginCenterY, mCurrentRadius, mPaint);

            float sin = (mTouchedY - mOriginCenterY) / mDistance;
            float cos = (mTouchedX - mOriginCenterX) / mDistance;

            float originOffsetX = sin * mCurrentRadius;
            float originOffsetY = cos * mCurrentRadius;
            float movingOffsetX = sin * mOriginRadius;
            float movingOffsetY = cos * mOriginRadius;

            mOriginCoordiAX = mOriginCenterX - originOffsetX;
            mOriginCoordiAY = mOriginCenterY + originOffsetY;
            mOriginCoordiBX = mOriginCenterX + originOffsetX;
            mOriginCoordiBY = mOriginCenterY - originOffsetY;

            mMovingCoordiAX = mTouchedX - movingOffsetX;
            mMovingCoordiAY = mTouchedY + movingOffsetY;
            mMovingCoordiBX = mTouchedX + movingOffsetX;
            mMovingCoordiBY = mTouchedY - movingOffsetY;

            //            mControlCoordiAX = (mOriginCoordiAX + mMovingCoordiBX) / 2;
            //            mControlCoordiAY = (mOriginCoordiAY + mMovingCoordiBY) / 2;
            //            mControlCoordiBX = (mOriginCoordiBX + mMovingCoordiAX) / 2;
            //            mControlCoordiBY = (mOriginCoordiBY + mMovingCoordiAY) / 2;

            mControlCoordiAX = (mTouchedX + mOriginCenterX) / 2;
            mControlCoordiAY = (mTouchedY + mOriginCenterY) / 2;
            mControlCoordiBX = mControlCoordiAX;
            mControlCoordiBY = mControlCoordiAY;

            mPath.reset();
            // start on origin point A
            mPath.moveTo(mOriginCoordiAX, mOriginCoordiAY);
            // bezier arc to moving point A, basing on control point A
            mPath.quadTo(mControlCoordiAX, mControlCoordiAY, mMovingCoordiAX, mMovingCoordiAY);
            // line to moving point B
            mPath.lineTo(mMovingCoordiBX, mMovingCoordiBY);
            // bezier arc to origin point B, basing on control point B
            mPath.quadTo(mControlCoordiBX, mControlCoordiBY, mOriginCoordiBX, mOriginCoordiBY);
            // line to start point A
            mPath.lineTo(mOriginCoordiAX, mOriginCoordiAY);

            canvas.drawPath(mPath, mPaint);
        }

        if (!mIsDisplayText && mWillDrawCircle) {
            canvas.drawCircle(mTouchedX, mTouchedY, mOriginRadius, mPaint);
        }

        super.onDraw(canvas);
    }

    private void setBackground(float radius, int badgeColor) {
        float[] radiusArray = new float[8];
        Arrays.fill(radiusArray, radius);
        RoundRectShape roundRect = new RoundRectShape(radiusArray, null, null);
        ShapeDrawable bgDrawable = new ShapeDrawable(roundRect);
        bgDrawable.getPaint().setColor(badgeColor);
        mMainView.setBackgroundDrawable(bgDrawable);
    }

    private float calcDistance(float x, float y) {
        float edgeX = x - mOriginCenterX;
        float edgeY = y - mOriginCenterY;
        return (float) Math.sqrt(edgeX * edgeX + edgeY * edgeY);
    }

    /**
     * 是否已经到达边界。<br>
     * 如果已经到达边界，则不绘制曲线、圆点，并且可以松开消除
     *
     * @return 是否已经到达边界
     */
    private boolean isReachEdge() {
        return mDistance > mMaxDistance;
    }

    /**
     * 返回动画
     */
    private void startBackAnimation(long duration) {

        ValueAnimator xPositionAnim = ValueAnimator.ofFloat(mTouchedX, mOriginCenterX);
        xPositionAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                mTouchedX = (float) animation.getAnimatedValue();
                setThisX(mTouchedX);
            }
        });

        ValueAnimator yPositionAnim = ValueAnimator.ofFloat(mTouchedY, mOriginCenterY);
        yPositionAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                mTouchedY = (float) animation.getAnimatedValue();
                setThisY(mTouchedY);
            }
        });

        ValueAnimator circleAnim = ValueAnimator.ofFloat(mCurrentRadius, mOriginRadius);
        circleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentRadius = (float) animation.getAnimatedValue();
                mDistance = calcDistance(mTouchedX, mTouchedY);
                postInvalidate();
            }
        });
        circleAnim.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
            }

            @Override public void onAnimationEnd(Animator animation) {
                if (null != mListener) {
                    mListener.onOffDutyCanceled(mTarget);
                }
                mControlledByForce = false;
            }

            @Override public void onAnimationCancel(Animator animation) {
            }

            @Override public void onAnimationRepeat(Animator animation) {
            }
        });

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(xPositionAnim).with(yPositionAnim).with(circleAnim);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(duration);

        animSet.start();
    }

    private void updateTouchedPosition(MotionEvent event) {
        mTouchedX = event.getX();
        mTouchedY = event.getY();
    }

    /**
     * 根据移动的距离来刷新原来的圆半径大小
     */
    private void changeRadiusBaseOnDistance(double distance) {
        if (distance > mMaxDistance) {
            mCurrentRadius = 0;
        } else {
            mCurrentRadius = (float) ((1 - distance / mMaxDistance) * mOriginRadius);
        }
    }

    private float centerViewWidth(View view, float posX) {
        return posX - view.getWidth() / 2;
    }

    private float centerViewHeight(View view, float posY) {
        return posY - view.getHeight() / 2;
    }

    private void calcRadius() {
        int width = mMainView.getWidth();
        int height = mMainView.getHeight();
        mOriginCenterX = mMainView.getX() + width / 2;
        mOriginCenterY = mMainView.getY() + height / 2;
        mTouchedX = mOriginCenterX;
        mTouchedY = mOriginCenterY;
        mOriginRadius = (width > height ? height : width) / 2;
        mCurrentRadius = mOriginRadius;
    }

    private void lockOnTarget() {
        if (mTarget != null) {
            int[] targetLocation = new int[2];
            int[] thisLocation = new int[2];
            mTarget.getLocationInWindow(targetLocation);
            getLocationInWindow(thisLocation);
            mLocationX = getX() - (thisLocation[0] - targetLocation[0]) + mTargetOffsetX;
            mLocationY = getY() - (thisLocation[1] - targetLocation[1]) + mTargetOffsetY;
            setX(mLocationX);
            setY(mLocationY);
            if (null != mListener) {
                mListener.onLockOnFinished();
            }
        }
    }

    private void setThisX(float touchX) {
        mMainView.setX(centerViewWidth(mMainView, touchX));
        mAnimView.setX(touchX - mDrawableWidth / 2);
    }

    private void setThisY(float touchY) {
        mMainView.setY(centerViewHeight(mMainView, touchY));
        mAnimView.setY(touchY - mDrawableHeight / 2);
    }

    public String getText() {
        return mText;
    }

    public void setText(CharSequence text) {
        mText = text.toString();
        setMainViewText(text);
        postInvalidate();
    }

    public int getTextSize() {
        return Math.round(Utils.px2sp(mTextSize, getResources().getDisplayMetrics()));
    }

    public void setTextSize(int textSizeInSp) {
        mTextSize = Utils.sp2Px(textSizeInSp, getResources().getDisplayMetrics());
        mMainView.setTextSize(textSizeInSp);
    }

    public void setTextSize(float textSizeInPx) {
        mMainView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx);
    }

    public boolean isDisplayText() {
        return mIsDisplayText;
    }

    public void setIsDisplayText(boolean b) {
        mIsDisplayText = b;
        mWillDrawCircle = !b;
        if (mIsDisplayText) {
            mMainView.setVisibility(VISIBLE);
        } else {
            mMainView.setVisibility(GONE);
        }
        postInvalidate();
    }

    private void setMainViewText(CharSequence text) {
        if (mIsDisplayText) {
            mMainView.setText(text);
        } else {
            mMainView.setText("");
        }
    }

    public void setColor(int color) {
        mColor = color;
        mPaint.setColor(mColor);
        postInvalidate();
    }

    public void setOffDutyListener(OnOffDutyListener listener) {
        mListener = listener;
    }

    public void aimAt(View target) {
        aimAt(target, 0, 0);
    }

    public void aimAt(View target, int xOffsetDp, int yOffsetDp) {
        mTarget = target;
        mTargetOffsetX = Utils.dp2Px(xOffsetDp, getResources().getDisplayMetrics());
        mTargetOffsetY = Utils.dp2Px(yOffsetDp, getResources().getDisplayMetrics());
    }

    public void reset() {
        mIsDisplayText = true;
        mWillDrawCircle = false;
        mMainView.setVisibility(VISIBLE);
        mAnimView.setVisibility(GONE);
        setVisibility(VISIBLE);
        calcRadius();
        setThisX(mOriginCenterX);
        setThisY(mOriginCenterY);
        postInvalidate();
    }

    private void resetViewsPosition() {
        mMainView.setX(0);
        mMainView.setY(0);
        mAnimView.setX(0);
        mAnimView.setY(0);
    }

    @Override public void setVisibility(int visibility) {
        mMainView.setVisibility(VISIBLE);
        resetViewsPosition();
        super.setVisibility(visibility);
    }

    public interface OnOffDutyListener {
        void onOffDuty(OffDutyView view);

        void onOffDutyCanceled(View target);

        void onLockOnFinished();
    }

    private boolean mControlledByForce;
}

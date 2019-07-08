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
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import java.util.Random;

/**
 * @author Xuqiang ZHENG on 18/8/27.
 */
public class OffDutyDelegate {

    @NonNull
    private View view;
    private BezierView bezierView;
    @Nullable
    private OnOffDutyListener onOffDutyListener;

    private boolean bezierColorOverlay;
    @ColorInt
    private int bezierColor;
    @Px
    private int maxDistance;

    public OffDutyDelegate(@NonNull View view, @Nullable AttributeSet attrs) {
        this.view = view;

        Context context = view.getContext();
        int defMaxDistance = Utils.getDeviceHeight(context) / 8;
        int defBezierColor = context.getResources().getColor(R.color.md_red_500);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OffDutyDelegate);
            bezierColor = a.getColor(R.styleable.OffDutyDelegate_oddBezierColor, defBezierColor);
            bezierColorOverlay = a.getBoolean(
                    R.styleable.OffDutyDelegate_oddBezierColorOverlay, true);
            maxDistance = a.getDimensionPixelSize(
                    R.styleable.OffDutyDelegate_oddMaxDistance, defMaxDistance);
            a.recycle();
        } else {
            bezierColor = defBezierColor;
            maxDistance = defMaxDistance;
            bezierColorOverlay = true;
        }

        bezierView = new BezierView();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!(view.getRootView() instanceof ViewGroup)) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                bezierView.add();
                bezierView.setTouchPoint(event.getX(), event.getY());
                bezierView.updateMovingPosition(event.getRawX(), event.getRawY());
                return true;
            case MotionEvent.ACTION_MOVE:
                bezierView.updateMovingPosition(event.getRawX(), event.getRawY());
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                bezierView.finishDragging();
                return true;
        }
        return view.onTouchEvent(event);
    }

    public void resetView() {
        view.setVisibility(View.VISIBLE);
    }

    public void setOnOffDutyListener(@Nullable OnOffDutyListener onOffDutyListener) {
        this.onOffDutyListener = onOffDutyListener;
    }

    private class BezierView extends View {

        public Bitmap content;

        private Paint viewPaint;
        private Paint bezierPaint;
        private Paint particlePaint;

        private float distance;
        private float halfWidth;
        private float halfHeight;
        private float originRadius;
        private float currentRadius;
        private Point rawStartPoint = new Point();
        private PointF rawMovingPoint = new PointF();
        private PointF rawOriginCenterPoint = new PointF();
        private PointF touchPoint = new PointF();
        private PointFEvaluator evaluator = new PointFEvaluator();

        private Path bezierPath;
        private PointF bezierOriginA = new PointF();
        private PointF bezierOriginB = new PointF();
        private PointF bezierMovingA = new PointF();
        private PointF bezierMovingB = new PointF();
        private PointF bezierControlA = new PointF();
        private PointF bezierControlB = new PointF();
        private float bezierSin;
        private float bezierCos;

        private Particle[][] particles;
        private boolean exploding;

        public BezierView() {
            super(view.getContext());
            setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            bezierPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bezierPaint.setColor(bezierColor);
            viewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            bezierPath = new Path();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (exploding) {
                for (Particle[] row : particles) {
                    for (Particle item : row) {
                        particlePaint.setColor(item.color);
                        canvas.drawCircle(rawMovingPoint.x + item.x, rawMovingPoint.y + item.y,
                                item.radius, particlePaint);
                    }
                }
                return;
            }

            if (!outRanged()) {
                // draw bezier
                // some triangle math
                bezierCos = (rawMovingPoint.x - rawStartPoint.x) / distance;
                bezierSin = (rawMovingPoint.y - rawStartPoint.y) / distance;

                float originOffsetX = bezierSin * currentRadius;
                float originOffsetY = bezierCos * currentRadius;
                float movingOffsetX = bezierSin * originRadius;
                float movingOffsetY = bezierCos * originRadius;

                float rawMovingCenterX = rawMovingPoint.x + halfWidth;
                float rawMovingCenterY = rawMovingPoint.y + halfHeight;
                bezierOriginA.set(rawOriginCenterPoint.x - originOffsetX, rawOriginCenterPoint.y + originOffsetY);
                bezierOriginB.set(rawOriginCenterPoint.x + originOffsetX, rawOriginCenterPoint.y - originOffsetY);
                bezierMovingA.set(rawMovingCenterX - movingOffsetX, rawMovingCenterY + movingOffsetY);
                bezierMovingB.set(rawMovingCenterX + movingOffsetX, rawMovingCenterY - movingOffsetY);

                bezierControlA.x = (rawMovingCenterX + rawOriginCenterPoint.x) / 2;
                bezierControlA.y = (rawMovingCenterY + rawOriginCenterPoint.y) / 2;
                bezierControlB.set(bezierControlA);

                bezierPath.reset();
                bezierPath.moveTo(bezierOriginA.x, bezierOriginA.y);
                bezierPath.quadTo(bezierControlA.x, bezierControlA.y, bezierMovingA.x, bezierMovingA.y);
                bezierPath.lineTo(bezierMovingB.x, bezierMovingB.y);
                bezierPath.quadTo(bezierControlB.x, bezierControlB.y, bezierOriginB.x, bezierOriginB.y);
                bezierPath.lineTo(bezierOriginA.x, bezierOriginA.y);
                canvas.drawPath(bezierPath, bezierPaint);

                Paint paint;
                if (bezierColorOverlay) {
                    paint = bezierPaint;
                } else {
                    paint = viewPaint;
                }
                canvas.drawCircle(rawOriginCenterPoint.x, rawOriginCenterPoint.y, currentRadius, paint);
            }

            canvas.drawBitmap(content, rawMovingPoint.x, rawMovingPoint.y, null);
        }

        private void add() {
            updateViewInfo();
            ((ViewGroup) view.getRootView()).addView(bezierView);
            view.setVisibility(View.INVISIBLE);
        }

        private void remove() {
            ((ViewGroup) getParent()).removeView(bezierView);
            content.recycle();
        }

        private void updateViewInfo() {
            int[] loc = new int[2];
            view.getLocationInWindow(loc);
            halfWidth = view.getWidth() / 2f;
            halfHeight = view.getHeight() / 2f;

            rawStartPoint.set(loc[0], loc[1]);
            rawOriginCenterPoint.x = rawStartPoint.x + halfWidth;
            rawOriginCenterPoint.y = rawStartPoint.y + halfHeight;
            originRadius = Math.min(halfWidth, halfHeight);

            content = Utils.getBitmapFromView(view);
            Matrix matrix = new Matrix();
            Shader shader = new BitmapShader(content, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            viewPaint.setShader(shader);
            matrix.setTranslate(rawStartPoint.x, rawStartPoint.y);
            shader.setLocalMatrix(matrix);
            viewPaint.setShader(shader);
        }

        private void updateMovingPosition(float touchX, float touchY) {
            rawMovingPoint.set(touchX - touchPoint.x, touchY - touchPoint.y);
            updateDistance();
            postInvalidate();
        }

        private void setTouchPoint(float x, float y) {
            touchPoint.set(x, y);
        }

        private void updateDistance() {
            float edgeX = rawMovingPoint.x - rawStartPoint.x;
            float edgeY = rawMovingPoint.y - rawStartPoint.y;
            distance = (float) Math.sqrt(edgeX * edgeX + edgeY * edgeY);
            calcRadius();
        }

        private void finishDragging() {
            if (outRanged()) {
                explodeIntoParticles();
            } else {
                PointF start = new PointF(rawMovingPoint.x + touchPoint.x, rawMovingPoint.y + touchPoint.y);
                PointF end = new PointF(rawOriginCenterPoint.x, rawOriginCenterPoint.y);
                ValueAnimator animator = ValueAnimator.ofObject(evaluator, start, end);

                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        PointF point = (PointF) animation.getAnimatedValue();
                        updateMovingPosition(point.x, point.y);
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        remove();
                        resetView();
                    }
                });

                animator.setInterpolator(new BounceInterpolator());
                animator.setDuration(500);

                animator.start();
            }
        }

        private boolean outRanged() {
            return distance > maxDistance;
        }

        private void calcRadius() {
            if (outRanged()) {
                currentRadius = 0;
            } else {
                currentRadius = (1 - distance / maxDistance) * originRadius;
            }
        }

        private void explodeIntoParticles() {
            particles = generateParticles();
            exploding = true;
            ValueAnimator animator = ValueAnimator.ofFloat(1, 0)
                    .setDuration(500);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (Particle[] row : particles) {
                        for (Particle item : row) {
                            float progress = (float) animation.getAnimatedValue();

                            item.x += (item.x - halfWidth) * (1 - progress);
                            item.y += (item.y - halfHeight) * (1 - progress);
                            item.radius *= progress;

                            postInvalidate();
                        }
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    exploding = false;
                    remove();
                    view.setVisibility(View.GONE);
                    if (onOffDutyListener != null) {
                        onOffDutyListener.onOffDuty(view);
                    }
                }
            });

            animator.start();
        }

        private Particle[][] generateParticles() {
            final int num = 8;
            int partWidth = content.getWidth() / (num + 1);
            int partHeight = content.getHeight() / (num + 1);
            Particle[][] particles = new Particle[num][num];

            Random random = new Random();
            float baseRadius = Utils.dp2Px(4, getContext().getResources().getDisplayMetrics());
            for (int i = 1; i <= num; ++i) {
                for (int j = 1; j <= num; ++j) {
                    int x = i * partWidth;
                    int y = j * partHeight;
                    int color = content.getPixel(x, y);
                    particles[i - 1][j - 1] = new Particle(x, y, color, random.nextFloat() * baseRadius);
                }
            }

            return particles;
        }
    }
}

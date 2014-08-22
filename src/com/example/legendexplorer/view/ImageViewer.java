
package com.example.legendexplorer.view;

import java.io.File;
import java.util.ArrayList;

import com.example.legendutils.Tools.FileUtil;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class ImageViewer extends RelativeLayout {

    TheImage leftImage;
    TheImage middleImage;
    TheImage rightImage;
    int scrollDis = 30;
    boolean scrolling = false;
    boolean resizing = false;

    // 按下时的点
    PointF downPoint = new PointF(0f, 0f);
    // 拖动开始的点
    PointF startPoint = new PointF(0f, 0f);
    PointF lastPoint = new PointF(0f, 0f);

    Point leftPoint;
    Point middlePoint;
    Point rightPoint;

    AnimatorSet animatorSet;

    ArrayList<File> files = new ArrayList<File>();
    int imageIndex = 0;

    VelocityTracker velocityTracker;

    float maxVelovityInDP = 1f;
    float maxVelocityValue = 1f;
    float minValidVelocityInDP = 0.3f;
    float minValidVelocityValue = 0.3f;

    float scaleFrom = 0.8f;

    public ImageViewer(Context context) {
        super(context);
    }

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * test only
     */
    public void test() {
        File[] filess = new File("/storage/emulated/0/androidesk/wallpapers/").listFiles();
        for (int i = 0; i < filess.length; i++) {
            File file = filess[i];
            files.add(file);
        }
        imageIndex = 0;
        setupImages();
    }

    public void setDataSource(String path) {
        File FirstImage = new File(path);
        if (isImageFile(FirstImage)) {
            File parentFile = FirstImage.getParentFile();
            File[] listFiles = parentFile.listFiles();
            for (int i = 0; i < parentFile.listFiles().length; i++) {
                File file = listFiles[i];
                if (isImageFile(file)) {
                    files.add(file);
                    if (file.equals(FirstImage)) {
                        imageIndex = files.size() - 1;
                    }
                }
            }
            setupImages();
        }
    }

    @SuppressLint("Recycle")
    public void setupImages() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        maxVelocityValue = maxVelovityInDP
                * getContext().getResources().getDisplayMetrics().density;
        minValidVelocityValue = minValidVelocityInDP
                * getContext().getResources().getDisplayMetrics().density;

        if (files.size() == 1) {
            leftImage = null;
            middleImage = new TheImage(getContext());
            rightImage = null;
        } else {
            if (imageIndex == 0) {
                leftImage = null;
                middleImage = new TheImage(getContext());
                rightImage = new TheImage(getContext());
            } else if (imageIndex == files.size() - 1) {
                rightImage = null;
                middleImage = new TheImage(getContext());
                leftImage = new TheImage(getContext());
            } else {
                leftImage = new TheImage(getContext());
                middleImage = new TheImage(getContext());
                rightImage = new TheImage(getContext());
            }
        }

        if (rightImage != null) {
            rightImage.load(files.get(imageIndex + 1), getWidth(), getHeight());
            LayoutParams paramsr = new LayoutParams(rightImage.initWidth, rightImage.initHeight);
            rightImage.setLayoutParams(paramsr);
            addView(rightImage);
            rightImage.setX((getWidth() - rightImage.initWidth) / 2);
            rightImage.setY((getHeight() - rightImage.initHeight) / 2);
            rightImage.setAlpha(0f);
            rightImage.setScaleX(scaleFrom);
            rightImage.setScaleY(scaleFrom);
            rightPoint = new Point((getWidth() - rightImage.initWidth) / 2,
                    (getHeight() - rightImage.initHeight) / 2);
        }

        middleImage.load(files.get(imageIndex), getWidth(), getHeight());
        LayoutParams paramsm = new LayoutParams(middleImage.initWidth, middleImage.initHeight);
        middleImage.setLayoutParams(paramsm);
        addView(middleImage);
        middleImage.setX((getWidth() - middleImage.initWidth) / 2);
        middleImage.setY((getHeight() - middleImage.initHeight) / 2);
        middlePoint = new Point((getWidth() - middleImage.initWidth) / 2,
                (getHeight() - middleImage.initHeight) / 2);

        if (leftImage != null) {
            leftImage.load(files.get(imageIndex - 1), getWidth(), getHeight());
            LayoutParams paramsl = new LayoutParams(leftImage.initWidth, leftImage.initHeight);
            leftImage.setLayoutParams(paramsl);
            addView(leftImage);
            leftImage.setX((getWidth() - leftImage.initWidth) / 2 - getWidth());
            leftImage.setY((getHeight() - leftImage.initHeight) / 2);
            leftPoint = new Point((getWidth() - leftImage.initWidth) / 2 - getWidth(),
                    (getHeight() - leftImage.initHeight) / 2);
        }
    }

    public boolean isImageFile(File file) {
        return FileUtil.getFileType(file) == FileUtil.FILE_TYPE_IMAGE;
    }

    public float distance(PointF pointF, PointF pointF2) {
        return PointF.length(pointF.x - pointF2.x, pointF.y - pointF2.y);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean flag = false;
        PointF pointF;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scrolling = false;
                flag = false;
                downPoint = new PointF(ev.getX(), ev.getY());
                startPoint = new PointF(ev.getX(), ev.getY());
                lastPoint = new PointF(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                pointF = new PointF(ev.getX(), ev.getY());
                if (middleImage.TouchMode == TheImage.MODE_NORMAL) {
                    if (scrolling) {
                        flag = true;
                    } else {
                        if (Math.abs(ev.getY() - downPoint.x) > scrollDis) {
                            flag = true;
                            scrolling = true;
                        } else {
                            flag = false;
                        }
                    }
                } else {
                    flag = false;
                }

                lastPoint = pointF;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                flag = scrolling;
                break;

            default:
                break;
        }
        return flag;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        velocityTracker.addMovement(ev);
        PointF pointF;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                pointF = new PointF(ev.getX(), ev.getY());
                if (scrolling) {
                    scrollImageBy(pointF.x - startPoint.x, pointF.y - startPoint.y);
                } else {
                    if (distance(pointF, downPoint) > scrollDis) {
                        scrolling = true;
                        startPoint = lastPoint;
                        scrollImageBy(pointF.x - startPoint.x, pointF.y - startPoint.y);
                    }
                }
                lastPoint = pointF;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                if (scrolling) {
                    scrolling = false;
                }
                final VelocityTracker tmpTracker = velocityTracker;
                tmpTracker.computeCurrentVelocity(1, maxVelocityValue);
                onDragEnd(tmpTracker.getXVelocity(), tmpTracker.getYVelocity());
                break;

            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    float factor = 0.3f;

    public void onDragEnd(float vx, float vy) {
        if (resizing) {

        } else {
            int dura = 200;
            if (Math.abs(vx) > minValidVelocityValue) {
                if (vx < 0) {
                    if (middleImage.getScaleX() < 1) {
                        dura = Math
                                .abs((int) ((1 - middleImage.getScaleX()) / (1 - scaleFrom)
                                        * getWidth() / vx * factor));
                        scrollBack(dura);
                    } else {
                        if (imageIndex < files.size() - 1) {
                            dura = Math
                                    .abs((int) ((getWidth() + middleImage.getX() - middlePoint.x)
                                            / vx * factor));
                            scrollLeft(dura);
                        }
                    }
                } else {
                    if (middleImage.getScaleX() < 1) {
                        if (imageIndex > 0) {
                            dura = Math
                                    .abs((int) ((middleImage.getScaleX() - scaleFrom)
                                            / (1 - scaleFrom) * getWidth() / vx * factor));
                            scrollRight(dura);
                        }
                    } else {
                        dura = Math
                                .abs((int) ((middleImage.getX() - middlePoint.x) / vx * factor));
                        scrollBack(dura);
                    }
                }
            } else {
                if (middleImage.getScaleX() < (1 + scaleFrom) / 2) {
                    scrollRight(dura);
                } else if (middleImage.getX() < middlePoint.x - getWidth() / 2) {
                    scrollLeft(dura);
                } else {
                    scrollBack(dura);
                }
            }
        }
    }

    public void scrollRight(int dura) {
        imageIndex--;
        if (rightImage != null) {
            removeView(rightImage);
        }

        rightImage = middleImage;
        rightPoint = new Point(middlePoint.x, middlePoint.y);

        middleImage = leftImage;
        middlePoint = new Point(leftPoint.x + getWidth(), leftPoint.y);

        if (imageIndex > 0) {
            leftImage = new TheImage(getContext());
            leftImage.load(files.get(imageIndex - 1), getWidth(), getHeight());
            LayoutParams paramsl = new LayoutParams(leftImage.initWidth, leftImage.initHeight);
            leftImage.setLayoutParams(paramsl);
            addView(leftImage);
            leftImage.setX((getWidth() - leftImage.initWidth) / 2 - getWidth());
            leftImage.setY((getHeight() - leftImage.initHeight) / 2);
            leftPoint = new Point((getWidth() - leftImage.initWidth) / 2 - getWidth(),
                    (getHeight() - leftImage.initHeight) / 2);
        } else {
            leftImage = null;
            leftPoint = null;
        }
        scrollBack(dura);
    }

    public void scrollLeft(int dura) {
        imageIndex++;
        if (leftImage != null) {
            removeView(leftImage);
        }

        leftImage = middleImage;
        leftPoint = new Point(middlePoint.x - getWidth(), middlePoint.y);

        middleImage = rightImage;
        middlePoint = new Point(rightPoint.x, rightPoint.y);

        if (imageIndex < files.size() - 1) {
            rightImage = new TheImage(getContext());
            rightImage.load(files.get(imageIndex + 1), getWidth(), getHeight());
            LayoutParams paramsr = new LayoutParams(rightImage.initWidth, rightImage.initHeight);
            rightImage.setLayoutParams(paramsr);
            rightImage.setAlpha(0f);
            addView(rightImage, 0);
            rightImage.setX((getWidth() - rightImage.initWidth) / 2);
            rightImage.setY((getHeight() - rightImage.initHeight) / 2);
            rightImage.setScaleX(scaleFrom);
            rightImage.setScaleY(scaleFrom);
            rightPoint = new Point((getWidth() - rightImage.initWidth) / 2,
                    (getHeight() - rightImage.initHeight) / 2);
        } else {
            rightImage = null;
            rightPoint = null;
        }
        scrollBack(dura);
    }

    public void scrollBack(int dura) {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<Animator>();
        if (leftImage != null) {
            ObjectAnimator animatorLeft = ObjectAnimator.ofFloat(leftImage, "x",
                    leftImage.getX(), leftPoint.x);
            animators.add(animatorLeft);
        }

        ObjectAnimator holderMiddleX = ObjectAnimator.ofFloat(middleImage, "x",
                middleImage.getX(), middlePoint.x);
        ObjectAnimator holderMiddleAlpha = ObjectAnimator.ofFloat(middleImage, "alpha",
                middleImage.getAlpha(), 1f);
        ObjectAnimator holderMiddleScaleX = ObjectAnimator.ofFloat(middleImage, "scaleX",
                middleImage.getScaleX(), 1f);
        ObjectAnimator holderMiddleScaleY = ObjectAnimator.ofFloat(middleImage, "scaleY",
                middleImage.getScaleY(), 1f);

        animators.add(holderMiddleX);
        animators.add(holderMiddleAlpha);
        animators.add(holderMiddleScaleX);
        animators.add(holderMiddleScaleY);

        if (rightImage != null) {
            ObjectAnimator holderRightX = ObjectAnimator.ofFloat(rightImage, "x",
                    rightImage.getX(), rightPoint.x);
            ObjectAnimator holderRightAlpha = ObjectAnimator.ofFloat(rightImage, "alpha",
                    rightImage.getAlpha(), 0f);
            ObjectAnimator holderRightScaleX = ObjectAnimator.ofFloat(rightImage, "scaleX",
                    rightImage.getScaleX(), scaleFrom);
            ObjectAnimator holderRightScaleY = ObjectAnimator.ofFloat(rightImage, "scaleY",
                    rightImage.getScaleY(), scaleFrom);

            animators.add(holderRightX);
            animators.add(holderRightAlpha);
            animators.add(holderRightScaleX);
            animators.add(holderRightScaleY);
        }
        animatorSet.playTogether(animators);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(dura);
        animatorSet.start();
    }

    public void scrollImageBy(float x, float y) {
        if (resizing) {

        } else {
            scrollThreeBodyBy(x, y);
        }
    }

    public void scrollThreeBodyBy(float x, float y) {
        if (x > 0) {
            // 右侧，rightImage不动
            if (leftImage == null) {
                middleImage.setX(middlePoint.x);
            } else {
                leftImage.setX(leftPoint.x + x);
                // middle shrink
                float scale = scaleFrom + (1 - scaleFrom) * (1 - x / getWidth());
                middleImage.setScaleX(scale);
                middleImage.setScaleY(scale);
                middleImage.setAlpha((1 - x / getWidth()));
            }
            if (rightImage != null) {
                rightImage.setAlpha(0f);
            }
        } else {
            // 左侧，leftImage不动
            if (rightImage == null) {
                middleImage.setX(middlePoint.x);
            } else {
                middleImage.setX(middlePoint.x + x);
                // expand right
                float scale = scaleFrom + (1 - scaleFrom) * (-x / getWidth());
                rightImage.setScaleX(scale);
                rightImage.setScaleY(scale);
                rightImage.setAlpha((-x / getWidth()));
            }
            if (leftImage != null) {
                leftImage.setX(leftPoint.x);
            }
        }
    }
}

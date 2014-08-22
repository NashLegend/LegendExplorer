
package com.example.legendexplorer.view;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class TheImage extends ImageView {

    public int initWidth = 0;
    public int initHeight = 0;
    public float initScale = 1f;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_DRAG = 1;
    public static final int MODE_ZOOM = 2;
    int TouchMode = MODE_NORMAL;
    boolean resizing = false;
    private Matrix initMatrix = new Matrix();

    public TheImage(Context context) {
        super(context);
    }

    public TheImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TheImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (!theBitmap.isRecycled()) {
                theBitmap.recycle();
            }
        } catch (Exception e) {

        }
    }

    float dist1 = 10f;
    float dist2 = 10f;
    PointF downPoint = new PointF(0f, 0f);
    PointF lastPoint = new PointF(0f, 0f);
    Bitmap theBitmap;
    float[] values = {
            0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    float[] lastStableValues = {
            0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    float[] initValues = {
            0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    float scrollDis = 36f;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        boolean flag = true;
        int pointIndex = event.getActionIndex();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downPoint = new PointF(event.getX(), event.getY());
                lastPoint = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                dist1 = distance(event);
                getCenter(event);
                getImageMatrix().getValues(lastStableValues);
                TouchMode = MODE_ZOOM;
                break;
            case MotionEvent.ACTION_MOVE:
                if (TouchMode == MODE_NORMAL) {
                    if (Math.abs(event.getX() - downPoint.x) > scrollDis) {
                        flag = false;
                    }
                } else {
                    getImageMatrix().getValues(values);
                    if (TouchMode == MODE_ZOOM) {
                        dist2 = distance(event);
                        if (values[0] * dist2 / dist1 > initValues[0]) {
                            getImageMatrix().postScale((dist2 / dist1), (dist2 / dist1),
                                    centerPoint.x,
                                    centerPoint.y);
                        } else {
                            // do nothing
                        }
                        dist1 = dist2;
                    } else {
                        if (lastPoint != null) {
                            // top left
                            // top right
                            // bottom left
                            // bottom right

                            getImageMatrix().postTranslate(event.getX() - lastPoint.x,
                                    event.getY() - lastPoint.y);
                        } else {
                            lastPoint = new PointF();
                        }
                        lastPoint.set(event.getX(), event.getY());

                    }
                    postInvalidate();
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (resizing) {
                    TouchMode = MODE_DRAG;
                } else {
                    TouchMode = MODE_DRAG;
                }
                break;
            default:
                break;
        }
        detector.onTouchEvent(event);
        return flag;
    }

    public float distance(MotionEvent event) {
        float a = event.getX(1) - event.getX(0);
        float b = event.getY(1) - event.getY(0);
        return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    private Point centerPoint = new Point();

    private void getCenter(MotionEvent event) {
        float a = event.getX(1) + event.getX(0);
        float b = event.getY(1) + event.getY(0);
        centerPoint.set((int) a / 2, (int) b / 2);
    }

    public float distance(PointF pointF, PointF pointF2) {
        return PointF.length(pointF.x - pointF2.x, pointF.y - pointF2.y);
    }

    GestureDetector detector;

    public void load(File file, int w, int h) {
        setBackgroundColor(Color.TRANSPARENT);
        detector = new GestureDetector(getContext(), new GestureListener());

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        options.inJustDecodeBounds = false;
        float hei = options.outHeight;
        float wid = options.outWidth;

        float r = 1;
        if (wid > w || hei > h) {
            float beWidth = wid / w;
            float beHeight = hei / h;
            if (beWidth < beHeight) {
                r = beHeight;
            } else {
                r = beWidth;
            }
        }
        initWidth = (int) (wid / r);
        initHeight = h;
        initScale = 1 / r;
        if (r < 1) {
            r = 1;
        }
        LoadBMPTask task = new LoadBMPTask();
        task.execute(file.getAbsolutePath());
    }

    class LoadBMPTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapFactory.decodeFile(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            theBitmap = result;
            setImageBitmap(theBitmap);
            getImageMatrix().getValues(initValues);
            initMatrix.set(getImageMatrix());
        }
    }

    public void resetMatrix() {
        if (TouchMode == MODE_NORMAL) {
            getImageMatrix().postScale(2, 2, getWidth() / 2, getHeight() / 2);
            TouchMode = MODE_DRAG;
        } else {
            getImageMatrix().set(initMatrix);
            TouchMode = MODE_NORMAL;
        }

        postInvalidate();
    }

    class GestureListener extends SimpleOnGestureListener {
        public GestureListener() {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            resetMatrix();
            return super.onDoubleTap(e);
        }
    }

}

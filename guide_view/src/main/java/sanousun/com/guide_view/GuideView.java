package sanousun.com.guide_view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android Studio.
 * <p>
 * author: dashu
 * date: 2017/12/17
 * time: 下午9:15
 * desc: 引导视图
 */

public class GuideView extends ViewGroup implements ViewTreeObserver.OnGlobalLayoutListener {

    private List<View> mTargetViewList;
    private Rect mTargetRect;
    private RectF mTargetShowRectF;
    private int mTargetPaddingLeft;
    private int mTargetPaddingTop;
    private int mTargetPaddingRight;
    private int mTargetPaddingBottom;
    private int mTargetShape;
    private int mTargetCorner;
    private float mTargetRadio;
    private int mShadowColor;
    private int mGuideAnchor;
    private int mGuideOffsetX;
    private int mGuideOffsetY;

    private int mAnimatorShow;
    private int mAnimatorDismiss;
    private List<OnDismissListener> mOnDismissListeners;
    private OnOutOfRangeListener mOnOutOfRangeListener;

    private Paint mEraser;
    private Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;

    private boolean isAnimatorDoing = false;
    private boolean isTargetDecorView = false;

    public void setTargetViewList(List<View> targetViewList) {
        if (mTargetViewList == null) {
            mTargetViewList = new ArrayList<>();
        }
        mTargetViewList.clear();
        if (targetViewList != null) {
            mTargetViewList.addAll(targetViewList);
        }
    }

    public void setTargetPaddingLeft(int targetPaddingLeft) {
        mTargetPaddingLeft = targetPaddingLeft;
    }

    public void setTargetPaddingTop(int targetPaddingTop) {
        mTargetPaddingTop = targetPaddingTop;
    }

    public void setTargetPaddingRight(int targetPaddingRight) {
        mTargetPaddingRight = targetPaddingRight;
    }

    public void setTargetPaddingBottom(int targetPaddingBottom) {
        mTargetPaddingBottom = targetPaddingBottom;
    }

    public void setTargetShape(int targetShape) {
        mTargetShape = targetShape;
    }

    public void setTargetCorner(int targetCorner) {
        mTargetCorner = targetCorner;
    }

    public void setTargetRadio(float targetRadio) {
        mTargetRadio = targetRadio;
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
    }

    public void setGuideAnchor(int guideAnchor) {
        mGuideAnchor = guideAnchor;
    }

    public void setGuideOffsetX(int guideOffsetX) {
        mGuideOffsetX = guideOffsetX;
    }

    public void setGuideOffsetY(int guideOffsetY) {
        mGuideOffsetY = guideOffsetY;
    }

    public void setAnimatorShow(int animatorShow) {
        mAnimatorShow = animatorShow;
    }

    public void setAnimatorDismiss(int animatorDismiss) {
        mAnimatorDismiss = animatorDismiss;
    }

    public void addOnDismissListener(OnDismissListener onDismissListener) {
        if (mOnDismissListeners == null) {
            mOnDismissListeners = new ArrayList<>();
        }
        mOnDismissListeners.add(onDismissListener);
    }

    public void removeOnDismissListener(OnDismissListener onDismissListener) {
        if (mOnDismissListeners != null) {
            mOnDismissListeners.remove(onDismissListener);
        }
    }

    public void clearOnDismissListener() {
        if (mOnDismissListeners != null) {
            mOnDismissListeners.clear();
        }
    }

    public void setOnOutOfRangeListener(OnOutOfRangeListener onOutOfRangeListener) {
        mOnOutOfRangeListener = onOutOfRangeListener;
    }

    public GuideView(Context context) {
        this(context, null);
    }

    public GuideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GuideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        Point size = new Point();
        size.x = getResources().getDisplayMetrics().widthPixels;
        size.y = getResources().getDisplayMetrics().heightPixels;
        mEraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        mEraserCanvas = new Canvas(mEraserBitmap);
        mEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraser.setColor(0xFFFFFFFF);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mTargetRect = new Rect();
        mTargetShowRectF = new RectF();
        // 设置了颜色才会调用onDraw()，否则不会触发
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            clearFocus();
            mEraserCanvas.setBitmap(null);
            mEraserBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int w = MeasureSpec.getSize(widthMeasureSpec);
        final int h = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(w, h);
        final int count = getChildCount();
        View child;
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            if (child != null) {
                measureChild(child,
                        MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int left, top, right, bottom;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (!isTargetDecorView) {
                switch (mGuideAnchor) {
                    case Configuration.ANCHOR_CENTER:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    case Configuration.ANCHOR_LEFT:
                        left = mTargetRect.left - width;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.left;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    case Configuration.ANCHOR_TOP:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.top - height;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.top;
                        break;
                    case Configuration.ANCHOR_RIGHT:
                        left = mTargetRect.right;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.right + width;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    default:
                    case Configuration.ANCHOR_BOTTOM:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.bottom;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.bottom + height;
                        break;
                }
            } else {
                switch (mGuideAnchor) {
                    case Configuration.ANCHOR_CENTER:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    case Configuration.ANCHOR_LEFT:
                        left = mTargetRect.left;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.left + width;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    case Configuration.ANCHOR_TOP:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.top;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.top + height;
                        break;
                    case Configuration.ANCHOR_RIGHT:
                        left = mTargetRect.right - width;
                        top = mTargetRect.centerY() - height / 2;
                        right = mTargetRect.right;
                        bottom = mTargetRect.centerY() + height / 2;
                        break;
                    default:
                    case Configuration.ANCHOR_BOTTOM:
                        left = mTargetRect.centerX() - width / 2;
                        top = mTargetRect.bottom - height;
                        right = mTargetRect.centerX() + width / 2;
                        bottom = mTargetRect.bottom;
                        break;
                }
            }
            child.layout(
                    left + mGuideOffsetX, top + mGuideOffsetY,
                    right + mGuideOffsetX, bottom + mGuideOffsetY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mEraserBitmap.eraseColor(Color.TRANSPARENT);
        mEraserCanvas.drawColor(mShadowColor);
        if (!isTargetDecorView) {
            mTargetShowRectF.set(mTargetRect);
            switch (mTargetShape) {
                case Configuration.SHAPE_OVAL:
                    mEraserCanvas.drawOval(mTargetShowRectF, mEraser);
                    break;
                case Configuration.SHAPE_RECTANGLE:
                default:
                    mEraserCanvas.drawRoundRect(mTargetShowRectF, mTargetCorner, mTargetCorner, mEraser);
                    break;
            }
        }
        canvas.drawBitmap(mEraserBitmap, 0, 0, null);
    }

    /**
     * 展示引导视图
     */
    public void show() {
        Activity activity = (Activity) getContext();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (mTargetViewList == null) {
            mTargetViewList = new ArrayList<>();
        }
        if (mTargetViewList.size() == 0) {
            mTargetViewList.add(activity.getWindow().getDecorView());
            isTargetDecorView = true;
        }
        View target = mTargetViewList.get(0);
        if (target.getWidth() == 0 && target.getHeight() == 0) {
            getViewTreeObserver().addOnGlobalLayoutListener(this);
        } else {
            fixLayout();
        }
        ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView();
        parent.addView(GuideView.this);
        if (mAnimatorShow > 0) {
            isAnimatorDoing = true;
            Animator animator = AnimatorInflater.loadAnimator(getContext(), mAnimatorShow);
            animator.setTarget(GuideView.this);
            animator.start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isAnimatorDoing = false;
                }
            });
        }
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimatorDoing) {
                    return;
                }
                if (mAnimatorDismiss > 0) {
                    isAnimatorDoing = true;
                    Animator animator = AnimatorInflater.loadAnimator(getContext(), mAnimatorDismiss);
                    animator.setTarget(GuideView.this);
                    animator.start();
                    animator.addListener(new AnimatorListenerAdapter() {
                                             @Override
                                             public void onAnimationEnd(Animator animation) {
                                                 super.onAnimationEnd(animation);
                                                 isAnimatorDoing = false;
                                                 dismiss();
                                             }
                                         }
                    );
                } else {
                    dismiss();
                }
            }
        });
    }

    /**
     * 引导层消失
     */
    public void dismiss() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(GuideView.this);
        }
        if (mOnDismissListeners != null) {
            for (OnDismissListener onDismissListener : mOnDismissListeners) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss();
                }
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        fixLayout();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void fixLayout() {
        // 将该布局提到最前面
        bringToFront();
        Rect border = calculateTargetBorder();
        if (mTargetShape == Configuration.SHAPE_OVAL) {
            int centerX = (border.right + border.left) / 2;
            int centerY = (border.top + border.bottom) / 2;
            int radioX, radioY;
            if ((border.right - border.left) > (border.bottom - border.top)) {
                radioX = (border.right - border.left) / 2;
                radioY = (int) (radioX * mTargetRadio);
            } else {
                radioY = (border.bottom - border.top) / 2;
                radioX = (int) (radioY * mTargetRadio);
            }
            mTargetRect.set(
                    centerX - radioX, centerY - radioY,
                    centerX + radioX, centerY + radioY);
        } else {
            mTargetRect.set(border);
        }

        Log.i("target", "left: " + mTargetRect.left +
                ", top: " + mTargetRect.top +
                ", right: " + mTargetRect.right +
                ", bottom: " + mTargetRect.bottom);

        // 部分可见或者不可见的处理
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        Rect deviceRect = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);

        int oLeft = mTargetRect.left - deviceRect.left;
        int oTop = mTargetRect.top - deviceRect.top;
        int oRight = mTargetRect.right - deviceRect.right;
        int oBottom = mTargetRect.bottom - deviceRect.bottom;
        // 正常应该是一正一负，考虑到目标是大于窗口的请况可能会-负-零或者一零一零
        // 不正常的情况则是两正或者两负
        if (oLeft * oRight <= 0 && oTop * oBottom <= 0 || mOnOutOfRangeListener == null) {
            requestLayout();
        } else {
            int offX, offY;
            offX = Math.min(Math.abs(oLeft), Math.abs(oRight));
            if (oLeft < 0) {
                offX = -offX;
            }
            offY = Math.min(Math.abs(oTop), Math.abs(oBottom));
            if (oTop < 0) {
                offY = -offY;
            }
            mOnOutOfRangeListener.onOutOfRange(this, offX, offY);
        }
    }

    /**
     * 计算目标view的范围
     */
    private Rect calculateTargetBorder() {
        int[] location = new int[2];
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE,
                right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
        for (View view : mTargetViewList) {
            view.getLocationInWindow(location);
            int targetWidth = view.getWidth();
            int targetHeight = view.getHeight();
            left = Math.min(left, location[0] - mTargetPaddingLeft);
            top = Math.min(top, location[1] - mTargetPaddingTop);
            right = Math.max(right, location[0] + targetWidth + mTargetPaddingRight);
            bottom = Math.max(bottom, location[1] + targetHeight + mTargetPaddingBottom);
        }
        return new Rect(left, top, right, bottom);
    }

    public interface OnDismissListener {
        /**
         * 蒙层消失时的回调
         */
        public void onDismiss();
    }

    public interface OnOutOfRangeListener {
        /**
         * 目标超出可视界面
         */
        public void onOutOfRange(GuideView guideView, int offsetX, int offsetY);
    }
}
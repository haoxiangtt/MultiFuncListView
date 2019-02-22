package cn.bfy.multifunclistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Scroller;
import android.widget.Space;

import java.lang.reflect.Field;

/**
 * Created by ouyangjinfu on 2016/5/5.
 */
public class StikkyListView extends XListView {

    private static final int MAX_TRIGGER_VELOCITY = 300;
    private static final int MIN_TRIGGER_VELOCITY = -300;
    protected int MAX_X = 5;

    private View mFakeHeader;

    private View mStikkyHeader;

    private int mMinHeight;

    private int mMaxHeight;

    private VelocityTracker mVelocityTracker;

    private Scroller mStikkyScroller;

    private StikkyOnScrollListener mStikkyOnScrollListener;

    public interface StikkyOnScrollListener{
        public void onStartScroll(View headerView);
        public void onScrolling(View headerView);
        public void onEndScroll(View headerView);
    }

    public StikkyListView(Context context) {
        super(context);
        init(context,null,0);
    }

    public StikkyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs,defStyle);
    }

    public StikkyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0);
    }


    private void init(Context context, AttributeSet attrs, int i) {
        int[] stikkySwipeMenuListView = (int[])getResourceId(context, "StikkyListView", "styleable");
        TypedArray a = context.obtainStyledAttributes(attrs,
                stikkySwipeMenuListView);
        mMaxHeight = a.getDimensionPixelOffset(
            Integer.valueOf(getResourceId(context, "StikkyListView_stikkyMaxHeight", "styleable").toString())
                ,dp2px(context,240));
        mMinHeight =  a.getDimensionPixelOffset(
            Integer.valueOf(getResourceId(context, "StikkyListView_stikkyMinHeight", "styleable").toString())
                ,dp2px(context,50));
        a.recycle();
    }

    private Object getResourceId(Context context,String name, String type) {

        String className = context.getPackageName() +".R";

        try {

            Class<?> cls = Class.forName(className);

            for (Class<?> childClass : cls.getClasses()) {

                String simple = childClass.getSimpleName();

                if (simple.equals(type)) {

                    for (Field field : childClass.getFields()) {

                        String fieldName = field.getName();

                        if (fieldName.equals(name)) {

                            System.out.println(fieldName);

                            return field.get(null);

                        }

                    }

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return 0;

    }

    private int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void initWithContext(Context context){
        mVelocityTracker = VelocityTracker.obtain();
        mStikkyScroller = new Scroller(context);
        mFakeHeader = new Space(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mFakeHeader.setLayoutParams(lp);
        addHeaderView(mFakeHeader);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if(mStikkyHeader != null){
                    ViewGroup.LayoutParams lp = mFakeHeader.getLayoutParams();
                    lp.height = mStikkyHeader.getLayoutParams().height;
                    mFakeHeader.setLayoutParams(lp);
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

            }
        });

        super.initWithContext(context);

    }

    @Override
    public void computeScroll() {
        if(mStikkyScroller.computeScrollOffset()){
            int height = mStikkyScroller.getCurrY();
            setStikkyHeaderHeight(height);
            postInvalidate();
            if(mStikkyOnScrollListener != null){
                mStikkyOnScrollListener.onScrolling(mStikkyHeader);
            }
            if( mStikkyScroller.isFinished()/*(height == mMinHeight || height == mMaxHeight)*/
                    && mStikkyOnScrollListener != null){
                mStikkyOnScrollListener.onEndScroll(mStikkyHeader);
            }
        }else{
            super.computeScroll();
        }

    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            mLastY = lastY = ev.getRawY();
//                downX = ev.getRawX();
            downY = ev.getRawY();
            mVelocityTracker.addMovement(ev);
        }
        return super.dispatchTouchEvent(ev);
    }*/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            downX = ev.getRawX();
            downY = ev.getRawY();
        }else if(ev.getAction() == MotionEvent.ACTION_MOVE) {
            float dy = Math.abs(ev.getRawY() - downY);
            float dx = Math.abs(ev.getRawX() - downX);

            //判断为横向滚动
            if(dy < dx && dx > MAX_X){
                return false;
            }

        }
        return super.onInterceptTouchEvent(ev);
    }

    private float lastY;
    private float downX;
    private float downY;
    private boolean stateStikkyMove = false;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if(mStikkyHeader == null){
            return super.onTouchEvent(ev);
        }

        if (lastY == -1 || mLastY == -1) {
            mLastY = lastY = ev.getRawY();

        }

        if(downY == -1){
            downY = ev.getRawY();
        }

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                mLastY = lastY = ev.getRawY();
                downX = ev.getRawX();
                downY = ev.getRawY();
                mVelocityTracker.addMovement(ev);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float deltaY = ev.getRawY() - lastY;
                lastY = ev.getRawY();
                mVelocityTracker.addMovement(ev);

//                float dy = Math.abs(ev.getY() - downY);
//                float dx = Math.abs(ev.getX() - downX);
//
//                //判断为横向滚动
//                if(dy < dx && dx > MAX_X){
//                    break;
//                }
                //解决与xlistview的事件冲突
                if(getHeaderView().getVisiableHeight() > 0){
                    break;
                }

                int index = getFirstVisiblePosition();
                if (index == 0 && mStikkyScroller.isFinished()) {

                    int sh = mStikkyHeader.getLayoutParams().height;
                    if (deltaY > 0 && sh < mMaxHeight
                            || deltaY < 0 && sh > mMinHeight) {
                        if(!stateStikkyMove && mStikkyOnScrollListener != null){
                            mStikkyOnScrollListener.onStartScroll(mStikkyHeader);
                        }
                        int finalHeight = sh + (int) deltaY;
                        if (finalHeight < mMinHeight) {
                            finalHeight = mMinHeight;
                        } else if (finalHeight > mMaxHeight) {
                            finalHeight = mMaxHeight;
                        }
                        setStikkyHeaderHeight(finalHeight);
                        mLastY = lastY;
                        stateStikkyMove = true;
                        return true;
                    }else if(deltaY == 0){
                        mLastY = lastY;
                        return true;
                    }

                }

                if(stateStikkyMove || !mStikkyScroller.isFinished()){
                    mLastY = lastY;
                    return true;
                }

                break;
            }

            case MotionEvent.ACTION_UP :
            case  MotionEvent.ACTION_CANCEL : {
                mLastY = lastY = -1;//reset
                int startHeight = mStikkyHeader.getLayoutParams().height;
                int index = getFirstVisiblePosition();
                if(index == 0 && mStikkyScroller.isFinished()){
                    mVelocityTracker.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(100);
                    float v = mVelocityTracker.getYVelocity();
                    System.out.println(">>>>>getYVelocity="+v+"<<<<<<<<<");
                    mVelocityTracker.clear();
                    if (v > MAX_TRIGGER_VELOCITY && startHeight != mMaxHeight) {
                        stateStikkyMove = true;
                        mStikkyScroller.startScroll(0, startHeight,
                                0, mMaxHeight - startHeight, SCROLL_DURATION);
                        invalidate();
                    } else if (v < MIN_TRIGGER_VELOCITY && startHeight != mMinHeight) {
                        stateStikkyMove = true;
                        mStikkyScroller.startScroll(0, startHeight,
                                0, mMinHeight - startHeight, SCROLL_DURATION);
                        invalidate();
                    }else {
                        if (ev.getRawY() - downY > 0) {
                            if (startHeight - mMinHeight > (mMaxHeight - mMinHeight) / 3
                                    && startHeight != mMaxHeight) {
                                stateStikkyMove = true;
                                mStikkyScroller.startScroll(0, startHeight,
                                        0, mMaxHeight - startHeight, SCROLL_DURATION);
                                invalidate();
                            } else if(startHeight != mMinHeight && startHeight != mMaxHeight){
                                stateStikkyMove = true;
                                mStikkyScroller.startScroll(0, startHeight,
                                        0, mMinHeight - startHeight, SCROLL_DURATION);
                                invalidate();
                            }else if(mStikkyOnScrollListener != null){
                                mStikkyOnScrollListener.onEndScroll(mStikkyHeader);
                            }
                        } else {
                            if (mMaxHeight - startHeight > (mMaxHeight - mMinHeight) / 3
                                    && startHeight != mMinHeight) {
                                stateStikkyMove = true;
                                mStikkyScroller.startScroll(0, startHeight,
                                        0, mMinHeight - startHeight, SCROLL_DURATION);
                                invalidate();
                            } else if(startHeight != mMaxHeight && startHeight != mMinHeight){
                                stateStikkyMove = true;
                                mStikkyScroller.startScroll(0, startHeight,
                                        0, mMaxHeight - startHeight, SCROLL_DURATION);
                                invalidate();
                            }else if(mStikkyOnScrollListener != null){
                                mStikkyOnScrollListener.onEndScroll(mStikkyHeader);
                            }
                        }
                    }
                }else if(getFirstVisiblePosition() == 0 && mStikkyScroller.isFinished()
                        && mStikkyOnScrollListener != null){
                    mStikkyOnScrollListener.onEndScroll(mStikkyHeader);
                }



//                float dy = Math.abs(ev.getY() - downY);
//                float dx = Math.abs(ev.getX() - downX);
//                //判断为横向滚动
//                if(dy < dx && dx > MAX_X){
//                    return super.onTouchEvent(ev);
//                }
                downY = -1;
                /*if(stateStikkyMove || !mStikkyScroller.isFinished()) {
                    stateStikkyMove = false;
                    xListViewOnTouchEvent(ev);
                    return true;
                }*/

                stateStikkyMove = false;

            }
        }

        return super.onTouchEvent(ev);
    }

    private void setStikkyHeaderHeight(int finalHeight) {
        ViewGroup.LayoutParams lp1 = mFakeHeader.getLayoutParams();
        ViewGroup.LayoutParams lp2 = mStikkyHeader.getLayoutParams();
        lp1.height = finalHeight;
        lp2.height = finalHeight;
        mFakeHeader.setLayoutParams(lp1);
        mStikkyHeader.setLayoutParams(lp2);
        setSelection(0);
        if(stateStikkyMove == true && mStikkyOnScrollListener != null){
            mStikkyOnScrollListener.onScrolling(mStikkyHeader);
        }
    }

    public void setStikkyHeader(View header){
        mStikkyHeader = header;
    }

    public void setStikkyMinHeight(int minHeight){
        mMinHeight = minHeight;
    }

    public void setStikkyMaxHeight(int maxHeight){
        mMaxHeight = maxHeight;
    }

    public void setmStikkyOnScrollListener(StikkyOnScrollListener l){
        mStikkyOnScrollListener = l;
    }

    public void scrollStikkyToMinHeight(){
        if(mStikkyScroller.isFinished()) {
            if(mStikkyOnScrollListener != null){
                mStikkyOnScrollListener.onStartScroll(mStikkyHeader);
            }
            mStikkyScroller.startScroll(0, mMaxHeight,
                    0, mMinHeight - mMaxHeight, SCROLL_DURATION);
            invalidate();
        }
    }

    public void scrollStikkyToMaxHeight(){
        if(mStikkyScroller.isFinished()) {
            if(mStikkyOnScrollListener != null){
                mStikkyOnScrollListener.onStartScroll(mStikkyHeader);
            }
            mStikkyScroller.startScroll(0, mMinHeight,
                    0, mMaxHeight - mMinHeight, SCROLL_DURATION);
            invalidate();
        }
    }

}

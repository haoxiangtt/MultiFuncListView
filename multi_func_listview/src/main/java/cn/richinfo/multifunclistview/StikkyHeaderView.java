package cn.richinfo.multifunclistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by ouyangjinfu on 2016/4/25.
 * @Deprecated 已被StikkyHeaderView2代替
 */
public class StikkyHeaderView extends View{

    protected int mMiniHeight;

    protected int mMaxHeight;

    protected String mTitle;

    protected String mSummary;

    protected Drawable mHeader;

    protected Drawable mButton;

    protected Rect mBtnRect;

    protected Drawable mBackground;

    protected  Drawable mForeground;

    protected Scroller mScroller;

    protected OnClickListener mListener;

    protected GestureDetector mGestureDetector;

    protected class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mBtnRect.contains((int)e.getX(),(int)e.getY())){
                if(mListener != null){
                    mListener.onClick(StikkyHeaderView.this);
                }
            }
            return true;
        }
    }

    public StikkyHeaderView(Context context) {
        this(context,null);
    }

    public StikkyHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StikkyHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int[] stikkyHeaderView = (int[])getResourceId(context, "StikkyHeaderView", "styleable");
        TypedArray a = context.obtainStyledAttributes(attrs, stikkyHeaderView);
        int defMinHeight = dp2px(context,50);
        int delMaxHeight = dp2px(context,240);
        mMiniHeight = a.getDimensionPixelOffset(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_miniHeight", "styleable").toString()),defMinHeight);
        mMaxHeight = a.getDimensionPixelOffset(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_maxHeight", "styleable").toString()), delMaxHeight);
        mTitle = a.getString(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_title", "styleable").toString()));
        mSummary = a.getString(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_summary", "styleable").toString()));
        mHeader = a.getDrawable(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_header", "styleable").toString()));
        mButton = a.getDrawable(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_button", "styleable").toString()));
        mBackground = a.getDrawable(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_bg", "styleable").toString()));
        mForeground = a.getDrawable(
            Integer.valueOf(getResourceId(context, "StikkyHeaderView_fg", "styleable").toString()));
        a.recycle();

        if(TextUtils.isEmpty(mTitle)){
            mTitle = "无标题";
        }
        if(TextUtils.isEmpty(mSummary)){
            mSummary = "无内容";
        }

        mBtnRect = new Rect();
        mScroller = new Scroller(context);
        mGestureDetector = new GestureDetector(context,new MySimpleOnGestureListener());
    }

    /**

     * 对于context.getResources().getIdentifier无法获取的数据,或者数组

     * 资源反射值

     * @paramcontext

     * @param name

     * @param type

     * @return

     */

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

    protected int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    protected int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = mScroller.getCurrY();
            setLayoutParams(lp);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(getMeasuredHeight() > mMaxHeight){
            setMeasuredDimension(getMeasuredWidth(), mMaxHeight);
        }else if(getMeasuredHeight() < mMiniHeight){
            setMeasuredDimension(getMeasuredWidth(),mMiniHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float percent = (getHeight() - mMiniHeight) * 1.0f / (mMaxHeight - mMiniHeight);//整体滚动进度

        //画背景
        if(mBackground != null) {

            int offsetHeight = (mMaxHeight - mMiniHeight) / 3;
            Rect brect = new Rect();
            brect.left = 0;
            brect.bottom = getHeight() + (int)(offsetHeight * (1 - percent));
            brect.top = brect.bottom - mMaxHeight;
            brect.right = getWidth();
            mBackground.setBounds(brect);
            mBackground.draw(canvas);
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#1a000000"));
            canvas.drawRect(new RectF(0,0,getWidth(),getHeight()),paint);
        }

        //画按钮
        Bitmap btnbmp = ((BitmapDrawable)mButton).getBitmap();
        int bWidth = btnbmp.getWidth();
        int bHeight = btnbmp.getHeight();
        int startX = ( getWidth() - bWidth ) / 2;
        int endX = getWidth() - bWidth - dp2px(getContext(),10);
        mBtnRect.left = startX + (int)((endX - startX) * (1.0f - percent));
        mBtnRect.right = mBtnRect.left + bWidth;
        int offsetBottom = (mMiniHeight - bHeight) / 2;
        int standartOffset = dp2px(getContext(),20);
        mBtnRect.bottom = getHeight() - (int)(offsetBottom + (standartOffset - offsetBottom) * percent );
        mBtnRect.top = mBtnRect.bottom - bHeight;
        mButton.setBounds(mBtnRect);
        mButton.draw(canvas);

        //画描述文字

//        paint.setColor(Color.parseColor("#dcdddd"));
//        paint.setAlpha((int)(0xff * percent));
//        paint.setTextSize(ScreenUtils.sp2px(getContext(),14));
//        float tWidth = paint.measureText(mSummary);
//        float tx = (getWidth() - tWidth) * 1.0f / 2;
//        float ty = mBtnRect.top - ScreenUtils.dp2px(getContext(),10);
//        canvas.drawText(mSummary,tx,ty,paint);

        //画标题文字
        float ty = mBtnRect.top - dp2px(getContext(), 10);
        String preDrawTitle = mTitle;
        if(preDrawTitle != null) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor("#ffffff"));
            paint.setAlpha((int)(0xff * percent));
            paint.setTextSize(sp2px(getContext(),16));
            String alphaStr = Integer.toHexString((int)(0xff * 0.6 * percent));
            if(alphaStr.length() == 1){ alphaStr = "0"+alphaStr; }
            String colorStr = "#"+alphaStr+"000000";
            paint.setShadowLayer(2,1,1,Color.parseColor(colorStr));
            if (mTitle.length() > 12) {
                preDrawTitle = mTitle.substring(0, 12) + "...";
            }
            float tWidth = paint.measureText(preDrawTitle);
            float tx = (getWidth() - tWidth) * 1.0f / 2;
            canvas.drawText(preDrawTitle, tx, ty, paint);
        }

        //画头像
        if(mHeader != null) {
            Bitmap hbmp = ((BitmapDrawable) mHeader).getBitmap();

            int hWidth = hbmp.getWidth();
            int hHeight = hbmp.getHeight();
            Rect hRect = new Rect();
            int th = sp2px(getContext(), 16);
            hRect.left = (getWidth() - hWidth) / 2;
            hRect.right = hRect.left + hWidth;
            hRect.bottom = (int) (ty - th - dp2px(getContext(), 15));
            hRect.top = hRect.bottom - hHeight;
            mHeader.setBounds(hRect);
            mHeader.setAlpha((int) (0xff * percent));
            mHeader.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);
        if(rect.contains((int)event.getRawX(),(int)event.getY())){
            mGestureDetector.onTouchEvent(event);
            return true;
        }
        return false;
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    public void scrollToMiniHeight(){
        int sh = getHeight();
        mScroller.startScroll(0,sh,0,mMiniHeight - getHeight(),500);
        invalidate();
    }

    public void scrollToStartHeight(){
        int sh = getHeight();
        mScroller.startScroll(0,sh,0, mMaxHeight - getHeight(),500);
        invalidate();
    }

    public int getmMiniHeight() {
        return mMiniHeight;
    }

    public void setmMiniHeight(int mMiniHeight) {
        this.mMiniHeight = mMiniHeight;
        postInvalidate();
    }

    public int getmMaxHeight() {
        return mMaxHeight;
    }

    public void setmMaxHeight(int mMaxHeight) {
        this.mMaxHeight = mMaxHeight;
        postInvalidate();
    }

    public void setCurrentHeight(int height){
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
        postInvalidate();
    }

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
        postInvalidate();
    }

    public Drawable getmHeader() {
        return mHeader;
    }

    public void setmHeader(Drawable mHeader) {
        this.mHeader = mHeader;
        postInvalidate();
    }

    public Drawable getmButton() {
        return mButton;
    }

    public void setmButton(Drawable mButton) {
        this.mButton = mButton;
        postInvalidate();
    }
}

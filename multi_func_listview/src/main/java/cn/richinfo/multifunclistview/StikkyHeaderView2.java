package cn.richinfo.multifunclistview;

import android.content.Context;
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

/**
 * @author ouyangjinfu
 * @version 1.0
 * @Date 2016年5月24日
 * @Email ouyjf@giiso.com
 * @Description -->粘性标题头
 */
public class StikkyHeaderView2 extends StikkyHeaderView {

    private String mTypeMark;

    public StikkyHeaderView2(Context context) {
        this(context,null);
    }

    public StikkyHeaderView2(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StikkyHeaderView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        //画蒙版1
        /*{
            Paint paint = new Paint();
            paint.setColor(mForeground);
            paint.setAlpha((int) (0xff * (1f - percent)));
            RectF foreRect = new RectF(0, 0, getWidth(), mMiniHeight);
            canvas.drawRect(foreRect, paint);
        }*/
        //画蒙版2
        if (mForeground != null){
            Rect foreRect = new Rect(0, 0, getWidth(), mMiniHeight);
            mForeground.setBounds(foreRect);
            mForeground.setAlpha((int) (0xff * (1f - percent)));
            mForeground.draw(canvas);
        }

        //画按钮
        if(mButton != null) {
            Bitmap btnbmp = ((BitmapDrawable) mButton).getBitmap();
            int bWidth = btnbmp.getWidth();
            int bHeight = btnbmp.getHeight();
            int startX = (getWidth() - bWidth) / 2;
            int endX = getWidth() - bWidth - dp2px(getContext(), 10);
            mBtnRect.left = endX;//startX + (int)((endX - startX) * (1.0f - percent));
            mBtnRect.right = mBtnRect.left + bWidth;
            int offsetBottom = (mMiniHeight - bHeight) / 2;
            int standartOffset = dp2px(getContext(), 35);
            mBtnRect.bottom = getHeight() - (int) (offsetBottom + (standartOffset - offsetBottom) * percent);
            mBtnRect.top = mBtnRect.bottom - bHeight;
            mButton.setBounds(mBtnRect);
            mButton.draw(canvas);
        }

        //画描述文字
        String preDrawSummary = mSummary;
        if(!TextUtils.isEmpty(preDrawSummary)){
            Paint paint1 = new Paint();
            int th = sp2px(getContext(),14);
            paint1.setTextSize(th);
            paint1.setAntiAlias(true);
            if(mSummary.length() > 32){
                preDrawSummary = mSummary.substring(0,32) + "...";
            }
            int tStartX = dp2px(getContext(),10);
            float tx = tStartX;//(getWidth() - tWidth) * 1.0f / 2;
            float ty = getHeight() - dp2px(getContext(),15);//mBtnRect.top - ScreenUtils.dp2px(getContext(),10);
            //画文字背景阴影
//            paint1.setColor(Color.parseColor("#000000"));
//            paint1.setAlpha((int)(0xff * percent * 0.1f));
////            Paint.FontMetrics fm = paint1.getFontMetrics();
//            float fh = th;//(float) Math.ceil(fm.descent - fm.ascent);
//            float fw = paint1.measureText(preDrawSummary);
//            RectF r = new RectF(tx - 5,ty - fh,tx + fw + 5,ty + 10);
//            canvas.drawRoundRect(r,5,5,paint1);

            paint1.setColor(getResources().getColor(android.R.color.white));
            paint1.setAlpha((int)(0xff * percent));
            String alphaStr = Integer.toHexString((int)(0xff * 0.5 * percent));
            if(alphaStr.length() == 1){ alphaStr = "0"+alphaStr; }
            String colorStr = "#"+alphaStr+"000000";
            paint1.setShadowLayer(1,1,1,Color.parseColor(colorStr));
            canvas.drawText(preDrawSummary,tx,ty,paint1);
        }


        //画标记
        String preTypeMark = mTypeMark;
        if(!TextUtils.isEmpty(preTypeMark)){
            Paint paint3 = new Paint();
            int th = sp2px(getContext(),14);
            paint3.setTextSize(th);
            float summaryWidth = paint3.measureText(preDrawSummary);
//            int th = ScreenUtils.sp2px(getContext(),10);
            paint3.setTextSize(sp2px(getContext(),13));
            paint3.setAntiAlias(true);
            paint3.setStyle(Paint.Style.STROKE);
            paint3.setColor(getResources().getColor(android.R.color.white));
            String alphaStr = Integer.toHexString((int)(0xff * 0.5 * percent));
            if(alphaStr.length() == 1){ alphaStr = "0"+alphaStr; }
            String colorStr = "#"+alphaStr+"000000";
            paint3.setShadowLayer(1,1,1,Color.parseColor(colorStr));
            paint3.setAlpha((int)(0xff * percent));
            Paint.FontMetrics fm = paint3.getFontMetrics();

            float startX = dp2px(getContext(),10);
            float tx = startX;
            if(!TextUtils.isEmpty(preDrawSummary)){
                tx = startX + summaryWidth + dp2px(getContext(),16);
            }
//            System.out.println(">>>>>top="+fm.top+";ascent="+fm.ascent+";descent="+fm.descent+";bottom="+fm.bottom+";leading="+fm.leading+"<<<<");
            float ty = getHeight() - dp2px(getContext(),15);
            canvas.drawText(preTypeMark,tx,ty,paint3);

            float rx = tx - dp2px(getContext(),2);
            float ry = ty + (int)Math.abs(fm.leading - fm.bottom);
            float h = Math.abs(fm.bottom - fm.ascent);
            float w = paint3.measureText(preTypeMark) + dp2px(getContext(),4);
//            RectF rect = new RectF(rx,ry - h,rx + w,ry);
            RectF rect = new RectF(rx,ry - h-2,rx + w,ry);
            canvas.drawRoundRect(rect,2,2,paint3);
        }


        //画标题文字
        String preDrawTitle = mTitle;
        if(!TextUtils.isEmpty(preDrawTitle)) {
            Paint paint2 = new Paint();
            int th = sp2px(getContext(),16);
            paint2.setTextSize(th);
            paint2.setAntiAlias(true);
            if (mTitle.length() > 12) {
                preDrawTitle = mTitle.substring(0, 12) + "...";
            }
            int ty = getHeight() - dp2px(getContext(),15)
                    - sp2px(getContext(),14) - dp2px(getContext(),11);
            if(ty < (mMiniHeight + th) / 2 ){
                ty = (mMiniHeight + th) / 2;
            }
            int tStartX = dp2px(getContext(),10);
            int tEndX = (int)(getWidth() - paint2.measureText(preDrawTitle)) / 2;
            int tx = tStartX + (int)((tEndX - tStartX)*(1.0 - percent));//(getWidth() - tWidth) * 1.0f / 2;
            //画文字背景阴影
//            paint2.setColor(Color.parseColor("#1a000000"));
////            Paint.FontMetrics fm = paint2.getFontMetrics();
//            float fh = th;//(float) Math.ceil(fm.descent - fm.ascent);
//            float fw = paint2.measureText(preDrawTitle);
//            RectF r = new RectF(tx - 5,ty - fh,tx + fw + 5,ty + 10);
//            canvas.drawRoundRect(r,5,5,paint2);

            if(ty <= mMiniHeight + th){
                paint2.setColor(0x333333);
            }else{
                paint2.setColor(getResources().getColor(android.R.color.white));
                paint2.setShadowLayer(1,1,1,Color.parseColor("#80000000"));
            }
            canvas.drawText(preDrawTitle, tx, ty, paint2);
        }

        //画头像
        /*if(mHeader != null) {
            Bitmap hbmp = ((BitmapDrawable) mHeader).getBitmap();

            int hWidth = hbmp.getWidth();
            int hHeight = hbmp.getHeight();
            Rect hRect = new Rect();
            float ty = mBtnRect.top - dp2px(getContext(), 10);
            int th = sp2px(getContext(), 16);
            hRect.left = (getWidth() - hWidth) / 2;
            hRect.right = hRect.left + hWidth;
            hRect.bottom = (int) (ty - th - dp2px(getContext(), 15));
            hRect.top = hRect.bottom - hHeight;
            mHeader.setBounds(hRect);
            mHeader.setAlpha((int) (0xff * percent));
            mHeader.draw(canvas);
        }*/
    }

    public void setMBackground(Drawable background){
        mBackground = background;
        postInvalidate();
    }

    /**
     * 添加资讯类型如“直播”、“视频”
     * @param typeMark
     */
    public void setTypeMark(String typeMark){
        mTypeMark = typeMark;
        postInvalidate();
    }

}

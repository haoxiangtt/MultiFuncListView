/**
 * @file XListViewHeader.java
 * @create Apr 18, 2012 5:22:27 PM
 * @author Maxwin
 * @description XListView's header
 */
package cn.bfy.multifunclistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;

public class XListViewHeader extends LinearLayout {
	private LinearLayout mContainer;
	private RelativeLayout mContent;
	private ImageView mArrowImageView;
	private ProgressBar mProgressBar;
	private TextView mHintTextView;
	
	private ViewGroup mllTime;
	private TextView mHeaderTimeView;
	
	
	private int mState = STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	
	private final int ROTATE_ANIM_DURATION = 180;
	
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;
	
	private String mHeaderReadyStr = "";
	private String mHeaderNormalStr = "";
	private String mHeaderLoadStr = "";
	private int mInitHeaderHeight = 0;//头部初始高度
	private Date mRefreshTime = new Date();

	public XListViewHeader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public XListViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		mHeaderReadyStr = context.getResources().getString(
			getIdentifier(context, "xlistview_header_hint_ready", "string"));
		mHeaderNormalStr = context.getResources().getString(
			getIdentifier(context, "xlistview_header_hint_normal", "string"));
		mHeaderLoadStr = context.getResources().getString(
			getIdentifier(context, "xlistview_header_hint_loading", "string"));
		
		
		// 初始情况，设置下拉刷新view高度为0
		LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
			getIdentifier(context, "xlistview_header", "layout"), null);

		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);

		mContent = (RelativeLayout)findViewById(
			getIdentifier(context, "xlistview_header_content", "id"));
		mArrowImageView = (ImageView)findViewById(
			getIdentifier(context, "xlistview_header_arrow", "id"));
		mHintTextView = (TextView)findViewById(
			getIdentifier(context, "xlistview_header_hint_textview", "id"));
		mProgressBar = (ProgressBar)findViewById(
			getIdentifier(context, "xlistview_header_progressbar", "id"));

		mllTime = (ViewGroup)findViewById(
			getIdentifier(context, "xlistview_ll_time", "id"));
		mHeaderTimeView = (TextView)findViewById(
			getIdentifier(context, "xlistview_header_time", "id"));

		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);

		mInitHeaderHeight = dp2px(context, 50);
	}

	private int getIdentifier(Context context, String name, String defType) {
		int id = context.getResources().getIdentifier(name, defType,
				context.getPackageName());
		return id;
	}

	private int dp2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public void setState(int state) {
		if (state == mState) return ;

		if (state == STATE_REFRESHING) {	// 显示进度
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {	// 显示箭头图片
			mArrowImageView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		switch(state){
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}
			if (mState == STATE_REFRESHING) {
				mArrowImageView.clearAnimation();
			}
			mHintTextView.setText(mHeaderNormalStr);
			break;
		case STATE_READY:
			if (mState != STATE_READY) {
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(mRotateUpAnim);
				mHintTextView.setText(mHeaderReadyStr);
			}
			break;
		case STATE_REFRESHING:
			mHintTextView.setText(mHeaderLoadStr);
			break;
			default:
		}

		mState = state;
	}

	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LayoutParams lp = (LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
		refreshTime();
	}

	public int getVisiableHeight() {
//		return mContainer.getHeight();
		ViewGroup.LayoutParams lp = mContainer.getLayoutParams();
		int height = lp.height;
		return height < 0 ? mContainer.getHeight() : height;
	}
	
	public int getInitHeight(){
		return mInitHeaderHeight;
	}
	
	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(Date time) {
		
//		mHeaderTimeView.setText(time);
		
		mRefreshTime = time;
		
	}
	
	private void refreshTime(){
		if(mRefreshTime != null){
			mllTime.setVisibility(View.VISIBLE);
			mHeaderTimeView.setText(time2Interval(mRefreshTime));
		}else{
			mllTime.setVisibility(View.GONE);
		}
	}

	private String time2Interval(Date date){
		String interval = "刚刚";
		if(date == null){ return interval; }
		Date now = new Date();
		long l = now.getTime() - date.getTime();
		long day = l / (24 * 60 * 60 * 1000);
		long hour = (l / (60 * 60 * 1000) - day * 24);
		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		if (day > 0) {
			interval = day + "天前";
		} else if (hour > 0) {
			interval = hour + "小时前";
		} else if (min > 0) {
			interval = min + "分钟前";
		} else if (s > 0) {
			interval = s + "秒前";
		}

		return interval;
	}
	
	public void hide(){
		mContent.setVisibility(View.INVISIBLE);
	}
	
	public void show(){
		mContent.setVisibility(View.VISIBLE);
	}
	
	
	
	
	
	
	
	

}

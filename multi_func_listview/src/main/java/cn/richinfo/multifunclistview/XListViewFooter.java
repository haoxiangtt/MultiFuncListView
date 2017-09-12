/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package cn.richinfo.multifunclistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;
	
	private String mFootReadyStr = "";
	private String mFootNormalStr = "";
	
	public XListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	
	
	public XListViewFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		mContext = context;
//		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
//				, ViewGroup.LayoutParams.WRAP_CONTENT));
//		setOrientation(LinearLayout.VERTICAL);
		mFootReadyStr = context.getResources().getString(
			getIdentifier(context, "xlistview_footer_hint_ready", "string"));
		mFootNormalStr = context.getResources().getString(
				getIdentifier(context, "xlistview_footer_hint_normal", "string"));
		ViewGroup moreView = (ViewGroup)LayoutInflater.from(mContext).
				inflate(getIdentifier(context, "xlistview_footer", "layout"), this, false);
//		moreView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//		mContentView = moreView.findViewById(R.id.xlistview_footer_content);
		mProgressBar = moreView.findViewById(
				getIdentifier(context, "xlistview_footer_progressbar", "id"));
		mHintView = (TextView)moreView.findViewById(
				getIdentifier(context, "xlistview_footer_hint_textview", "id"));
		mContentView = moreView;
		addView(moreView);
		
	}


	private int getIdentifier(Context context, String name, String defType) {
		int id = context.getResources().getIdentifier(name, defType,
				context.getPackageName());
		return id;
	}

	
	public void setState(int state) {
		mHintView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(mFootReadyStr);
		} else if (state == STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
			mHintView.setText(mFootReadyStr);
		} else {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(mFootNormalStr);
		}
	}
	
	public void setBottomMargin(int height) {
		if (height < 0){ height = 0; }
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}

	public int getBottomMargin() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		return lp.bottomMargin;
	}


	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}


	/**
	 * loading status
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}

	/**
	 * show footer
	 */
	public void show() {
		LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}

	public void setHintText(String str){
		mHintView.setText(str);
	}

	public void setHintText(int resid){
		mHintView.setText(resid);
	}
	
	
}

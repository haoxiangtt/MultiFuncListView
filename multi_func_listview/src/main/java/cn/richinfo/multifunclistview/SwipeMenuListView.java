package cn.richinfo.multifunclistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.WrapperListAdapter;

import java.util.Date;

/**
 * 
 * @author baoyz
 * @date 2014-8-18
 * 
 */
public class SwipeMenuListView extends ListView implements OnScrollListener{
	
	private static final String TAG = "SwipeMenuListView";
	private static final boolean DEBUG = false;

	private static final int TOUCH_STATE_NONE = 0;
	private static final int TOUCH_STATE_X = 1;
	private static final int TOUCH_STATE_Y = 2;

	protected int MAX_Y = 5;
	protected int MAX_X = 5;
	private float mDownX;
	private float mDownY;
	private int mTouchState;
	private int mTouchPosition;
	private SwipeMenuLayout mTouchView;
	private OnSwipeListener mOnSwipeListener;

	private SwipeMenuCreator mMenuCreator;
	private OnMenuItemClickListener mOnMenuItemClickListener;
	private Interpolator mCloseInterpolator;
	private Interpolator mOpenInterpolator;
	
	/******************插入下拉刷新上拉加载特效*******************/
	protected float mLastY = -1; // save event y
	protected Scroller mScroller; // used for scroll back
	// -- header view
	protected XListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
//	private RelativeLayout mHeaderViewContent;
	protected int mHeaderViewHeight; // header view's height
	protected boolean mEnablePullRefresh = false;
	protected boolean mPullRefreshing = false; // is refreashing.
	// -- footer view
	protected XListViewFooter mFooterView;
	protected boolean mEnablePullLoad = false ;
	protected boolean mPullLoading = false;

	protected boolean mIsFooterReady = false;
	// total list items, used to detect is at the bottom of listview.
	protected int mTotalItemCount;
	// for mScroller, scroll back from header or footer.
	protected int mScrollBack;
	protected final static int SCROLLBACK_HEADER = 0;
	protected final static int SCROLLBACK_FOOTER = 1;
	protected final static int SCROLL_DURATION = 500; // scroll back duration
	protected final static int PULL_LOAD_MORE_DELTA = 100; // when pull up >= 50px
														// at bottom, trigger
														// load more.
	protected final static float OFFSET_RADIO = 1.8f; // support iOS like pull
														// feature.

	// the interface to trigger refresh and load more.
	protected IXListViewListener mListViewListener;
	protected OnScrollListener mDelegateOnScrollListener;
	protected ShowtoTopFloatButtonListener floatListener;

	protected long mDuration;//开始刷新和结束刷新的时间点

	protected int mTouchSlop = 0;
	
	public interface ShowtoTopFloatButtonListener{
		void showFloatButton();
		void hideFloatButton();
	}

	public void setShowtoTopFloatButtonListener(ShowtoTopFloatButtonListener listener){
		this.floatListener = listener;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(mDelegateOnScrollListener != null){
			mDelegateOnScrollListener.onScrollStateChanged(view,scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (null != floatListener) {
			if (firstVisibleItem > 3) {
				floatListener.showFloatButton();
			}else {
				floatListener.hideFloatButton();
			}
		}

		if(mDelegateOnScrollListener != null){
			mDelegateOnScrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
		}

	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l){
		mDelegateOnScrollListener = l;
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}
	
	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderView.hide();
		} else {
			mHeaderView.show();
		}
	}
	
	public boolean isPullRefreshEnable(){
		return mEnablePullRefresh;
	}
	
	public boolean isPullRefreshing(){
		return mPullRefreshing;
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
//		if(mEnablePullLoad == enable){ return; }
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!isPullRefreshing() && !isPullLoading()) {
						startLoadMore();
					}
				}
			});
		}
	}
	
	/**
	 * 显示底部
	 */
	public void showFooterView(){
		mPullLoading = false;
		mFooterView.show();
		mFooterView.setState(XListViewFooter.STATE_NORMAL);
		// both "pull up" and "click" will invoke load more.
		mFooterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startLoadMore();
			}
		});
	}
	
	/**
	 * 隐藏底部
	 */
	public void hideFootView(){
		mFooterView.hide();
		mFooterView.setOnClickListener(null);
	}
	
	public boolean isPullLoadEnable(){
		return mEnablePullLoad;
	}
	
	public boolean isPullLoading(){
		return mPullLoading;
	}
	

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(Date time) {
		mHeaderView.setRefreshTime(time);
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing && !mPullLoading) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	public XListViewHeader getHeaderView(){
		return mHeaderView;
	}

	public XListViewFooter getmFooterView(){
		return mFooterView;
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0){ // not visible.
			return;
		}
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading && !mPullRefreshing) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load more.
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

//		setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mDuration = System.currentTimeMillis();
		mPullLoading = true;
		mFooterView.setState(XListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}
	
	private void startRefresh() {
		mDuration = System.currentTimeMillis();
		mPullRefreshing = true;
		mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
//						Logger.i("SwipeMenuListView", ">>>>>>>mListener = "+mListViewListener);
		if (mListViewListener != null) {
//							Logger.i("SwipeMenuListView", ">>>>>>invoke onRefresh!!!!!!");
			mListViewListener.onRefresh();
		}
	}


	public void showRefresh(){
		if(isPullRefreshing() || isPullLoading()){
			return;
		}
		startRefresh();
//		int height = mHeaderView.getVisiableHeight();
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, 0, 0, mHeaderViewHeight,
				SCROLL_DURATION);
//		invalidate();
		setSelection(0);
	}


	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		long time = System.currentTimeMillis() - mDuration;
		if(time > SCROLL_DURATION) {
			cancelRefresh();
		}else{
			postDelayed(new Runnable() {
				@Override
				public void run() {
					cancelRefresh();
				}
			},SCROLL_DURATION - time);
		}
	}

	private void cancelRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		long time = System.currentTimeMillis();
		if(time > SCROLL_DURATION) {
			cancelLoadMore();
		}else{
			postDelayed(new Runnable() {
				@Override
				public void run() {
					cancelLoadMore();
				}
			},SCROLL_DURATION - time);
		}
	}

	private void cancelLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
		}
	}


	protected boolean xListViewOnTouchEvent(MotionEvent ev){
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();
				if (getFirstVisiblePosition() == 0
						&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)
						&& Math.abs(deltaY) > mTouchSlop) {
					// the first item is showing, header has shown or pull down.
					updateHeaderHeight(deltaY / OFFSET_RADIO);
					return true;
	//				invokeOnScrolling();
				} else if (getLastVisiblePosition() == mTotalItemCount - 1
						&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)
						&& Math.abs(deltaY) > mTouchSlop) {
					// last item, already pulled up or want to pull up.
					updateFooterHeight(-deltaY / OFFSET_RADIO);
					return true;
				}
				break;
			default:
				mLastY = -1; // reset
				if (getFirstVisiblePosition() == 0) {
					// invoke refresh
//					Logger.i("SwipeMenuListView", ">>>>mEnablePullRefresh="+mEnablePullRefresh+
//							";getVisiableHeight="+mHeaderView.getVisiableHeight()+
//							";mHeaderViewHeight="+mHeaderViewHeight+";mPullRefreshing="+mPullRefreshing);
					if (mEnablePullRefresh
							&& mHeaderView.getVisiableHeight() > mHeaderViewHeight 
							&& !mPullRefreshing && !mPullLoading) {
						startRefresh();
					}
					resetHeaderHeight();
				} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
					// invoke load more.
					if (mEnablePullLoad
					    && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA
					    && !mPullLoading && !mPullRefreshing) {
						startLoadMore();
					}
					resetFooterHeight();
				}
				break;
		}
		
		return false;
	}

	
	
	protected void initWithContext(Context context) {
		
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new XListViewHeader(context);
		mHeaderViewHeight = mHeaderView.getInitHeight();
		addHeaderView(mHeaderView);
		// init footer view
		mFooterView = new XListViewFooter(context);
		setPullLoadEnable(mEnablePullLoad);
		setPullRefreshEnable(mEnablePullRefresh);
	}
	
	/*****************************************************************************/

	public SwipeMenuListView(Context context) {
		super(context);
		init(context);
	}

	public SwipeMenuListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SwipeMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		MAX_X = dp2px(MAX_X);
		MAX_Y = dp2px(MAX_Y);
		mTouchState = TOUCH_STATE_NONE;
		mTouchSlop = 0;
		//add for xlistview
		initWithContext(context);
		//**********************
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		//**********************************************************************
		
		super.setAdapter(new SwipeMenuAdapter(getContext(), adapter) {
			@Override
			public void createMenu(SwipeMenu menu) {
				if (mMenuCreator != null) {
					mMenuCreator.create(menu);
				}
			}

			@Override
			public void onItemClick(SwipeMenuView view, SwipeMenu menu,
					int index) {
				boolean flag = false;
				if (mOnMenuItemClickListener != null) {
					flag = mOnMenuItemClickListener.onMenuItemClick(
							view.getPosition(), menu, index);
				}
				if (mTouchView != null && !flag) {
					mTouchView.smoothCloseMenu();
				}
			}
		});
	}

	@Override
	public ListAdapter getAdapter() {
		ListAdapter adapter = super.getAdapter();
		if (adapter instanceof WrapperListAdapter) {
			return ((WrapperListAdapter)adapter).getWrappedAdapter();
		}
		return adapter;
	}

	public void setCloseInterpolator(Interpolator interpolator) {
		mCloseInterpolator = interpolator;
	}

	public void setOpenInterpolator(Interpolator interpolator) {
		mOpenInterpolator = interpolator;
	}

	public Interpolator getOpenInterpolator() {
		return mOpenInterpolator;
	}

	public Interpolator getCloseInterpolator() {
		return mCloseInterpolator;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent ev) {
		
		/********下拉刷新上拉加载事件监听**************/
		if(xListViewOnTouchEvent(ev)){
			return super.onTouchEvent(ev);
		}
		/********************************************/

		if(mMenuCreator == null){
			return super.onTouchEvent(ev);
		}
		
		if (ev.getAction() != MotionEvent.ACTION_DOWN && mTouchView == null){
			return super.onTouchEvent(ev);
		}
		int action = MotionEventCompat.getActionMasked(ev);
		action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			int oldPos = mTouchPosition;
			mDownX = ev.getX();
			mDownY = ev.getY();
			mTouchState = TOUCH_STATE_NONE;

			mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());

			if (mTouchPosition == oldPos && mTouchView != null
					&& mTouchView.isOpen()) {
				mTouchState = TOUCH_STATE_X;
				mTouchView.onSwipe(ev);
				return true;
			}
			
			//TODO change by haoxiangtt
//			ViewGroup vg = (ViewGroup)getChildAt(mTouchPosition - getFirstVisiblePosition());
			View view = null;
//			if(vg != null){
			view = getChildAt(mTouchPosition - getFirstVisiblePosition());//vg.getChildAt(0);
//			}
			

			if (mTouchView != null && mTouchView.isOpen()) {
				mTouchView.smoothCloseMenu();
				mTouchView = null;
				// return super.onTouchEvent(ev);
				// try to cancel the touch event
				MotionEvent cancelEvent = MotionEvent.obtain(ev);  
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL);    
				onTouchEvent(cancelEvent);
				cancelEvent.recycle();
				return true;
			}
			if (view instanceof SwipeMenuLayout) {
				mTouchView = (SwipeMenuLayout) view;
			}
			if (mTouchView != null) {
				mTouchView.onSwipe(ev);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float dy = Math.abs((ev.getY() - mDownY));
			float dx = Math.abs((ev.getX() - mDownX));
			if (mTouchState == TOUCH_STATE_X) {
				if (mTouchView != null) {
					mTouchView.onSwipe(ev);
				}
				getSelector().setState(new int[] { 0 });
				ev.setAction(MotionEvent.ACTION_CANCEL);
				super.onTouchEvent(ev);
				return true;
			} else if (mTouchState == TOUCH_STATE_NONE) {
				if (Math.abs(dy) > MAX_Y) {
					mTouchState = TOUCH_STATE_Y;
				} else if (dx > MAX_X) {
					mTouchState = TOUCH_STATE_X;
					if (mOnSwipeListener != null) {
						mOnSwipeListener.onSwipeStart(mTouchPosition);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_X) {
				if (mTouchView != null) {
					mTouchView.onSwipe(ev);
					if (!mTouchView.isOpen()) {
						mTouchPosition = -1;
						mTouchView = null;
					}
				}
				if (mOnSwipeListener != null) {
					mOnSwipeListener.onSwipeEnd(mTouchPosition);
				}
				ev.setAction(MotionEvent.ACTION_CANCEL);
				super.onTouchEvent(ev);
				return true;
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	public void smoothOpenMenu(int position) {
		if (position >= getFirstVisiblePosition()
				&& position <= getLastVisiblePosition()) {
			//TODO change by haoxiangtt
//			ViewGroup vg = (ViewGroup)getChildAt(position - getFirstVisiblePosition());
			View view = null;
//			if(vg != null){
			view = getChildAt(position - getFirstVisiblePosition());//vg.getChildAt(0);
//			}
			if (view instanceof SwipeMenuLayout) {
				mTouchPosition = position;
				if (mTouchView != null && mTouchView.isOpen()) {
					mTouchView.smoothCloseMenu();
				}
				mTouchView = (SwipeMenuLayout) view;
				mTouchView.smoothOpenMenu();
			}
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getContext().getResources().getDisplayMetrics());
	}

	public void setMenuCreator(SwipeMenuCreator menuCreator) {
		this.mMenuCreator = menuCreator;
	}

	public void setOnMenuItemClickListener(
			OnMenuItemClickListener onMenuItemClickListener) {
		this.mOnMenuItemClickListener = onMenuItemClickListener;
	}

	public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
		this.mOnSwipeListener = onSwipeListener;
	}

	public interface OnMenuItemClickListener {
		boolean onMenuItemClick(int position, SwipeMenu menu, int index);
	}

	public interface OnSwipeListener {
		void onSwipeStart(int position);

		void onSwipeEnd(int position);
	}
}

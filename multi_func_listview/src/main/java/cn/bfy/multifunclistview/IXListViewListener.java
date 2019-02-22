package cn.bfy.multifunclistview;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/9/8 0008
 * @modifyDate : 2017/9/8 0008
 * @version    : 1.0
 * @desc       : 下拉上拉监听事件
 * </pre>
 */

public interface IXListViewListener {
    /**
     * 下拉刷新回调函数
     */
    void onRefresh();

    /**
     * 上拉加载回调函数
     */
    void onLoadMore();
}

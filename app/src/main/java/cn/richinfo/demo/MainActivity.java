package cn.richinfo.demo;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import cn.richinfo.multifunclistview.IXListViewListener;
import cn.richinfo.multifunclistview.StikkySwipeMenuListView;
import cn.richinfo.multifunclistview.SwipeMenu;
import cn.richinfo.multifunclistview.SwipeMenuCreator;
import cn.richinfo.multifunclistview.SwipeMenuItem;
import cn.richinfo.multifunclistview.SwipeMenuListView;
import cn.richinfo.multifunclistview.XListView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , IXListViewListener {

    ListAdapter mAdapter;
    XListView xListView;
    SwipeMenuListView sListView;
    StikkySwipeMenuListView tListView;
    View header;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_xlistview).setOnClickListener(this);
        findViewById(R.id.btn_swipelistview).setOnClickListener(this);
        findViewById(R.id.btn_stickeylistview).setOnClickListener(this);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
            , android.R.id.text1, getData());
        mHandler = new Handler();

        xListView = (XListView)findViewById(R.id.x_listview);
        xListView.setPullLoadEnable(true);// 开启上拉
        xListView.setPullRefreshEnable(true);//开启下拉
        xListView.setXListViewListener(this);

        sListView = (SwipeMenuListView)findViewById(R.id.s_listview);
        sListView.setPullLoadEnable(false);
        sListView.setPullRefreshEnable(false);
        sListView.setMenuCreator(new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem item = new SwipeMenuItem(MainActivity.this);
                item.setTitle("删除");
                item.setTitleColor(Color.WHITE);
                item.setBackground(new ColorDrawable(0xffff0000));
                item.setWidth(300);
                menu.addMenuItem(item);
            }
        });
        sListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Toast.makeText(MainActivity.this, "侧滑菜单被点击，position = " + position
                    + ", menu = " + menu.getMenuItem(index).getTitle() + ", index = " + index
                    , Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        header = findViewById(R.id.sv_slid);
        tListView = (StikkySwipeMenuListView)findViewById(R.id.t_listview);
        tListView.setStikkyHeader(header);
        tListView.setmStikkyOnScrollListener(new StikkySwipeMenuListView.StikkyOnScrollListener() {
            @Override
            public void onStartScroll(View headerView) {
                Toast.makeText(MainActivity.this, "start scroll...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScrolling(View headerView) {

            }

            @Override
            public void onEndScroll(View headerView) {
                Toast.makeText(MainActivity.this, "end scroll...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String[] getData() {

        String[] data = new String[20];
        String name = "张三李四王五赵六";
        for (int i = 0; i < data.length; i++) {
            data[i] = name + i;
        }
        return data;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_xlistview : {
                xListView.setVisibility(View.VISIBLE);
                sListView.setVisibility(View.GONE);
                tListView.setVisibility(View.GONE);
                header.setVisibility(View.GONE);
                xListView.setAdapter(mAdapter);
                Toast.makeText(this, "切换至XListView...", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.btn_swipelistview : {
                sListView.setVisibility(View.VISIBLE);
                xListView.setVisibility(View.GONE);
                tListView.setVisibility(View.GONE);
                header.setVisibility(View.GONE);
                sListView.setAdapter(mAdapter);
                Toast.makeText(this, "切换至SwipeListView...", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.btn_stickeylistview : {
                tListView.setVisibility(View.VISIBLE);
                header.setVisibility(View.VISIBLE);
                sListView.setVisibility(View.GONE);
                xListView.setVisibility(View.GONE);
                tListView.setAdapter(mAdapter);
                break;
            }
        }
    }


    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    xListView.stopRefresh();
            }
        }, 3000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                xListView.stopLoadMore();
            }
        }, 3000);
    }
}

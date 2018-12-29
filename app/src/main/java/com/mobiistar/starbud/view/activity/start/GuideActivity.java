package com.mobiistar.starbud.view.activity.start;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;
import com.mobiistar.starbud.view.activity.bluetooth.DetailActivity;
import com.mobiistar.starbud.view.activity.bluetooth.SearchActivity;
import com.mobiistar.starbud.view.widget.OnDoubleClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:Guide activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class GuideActivity extends BaseActivity {

    private ViewPager vpGuide;
    private LinearLayout llDot;
    private int latestSelectedIndex = -1;
    private int[] mResIdArray = StarUtil.getQuickGuideResIdArray();

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_guide;
    }

    @Override
    protected void init() {
        vpGuide = findViewById(R.id.vp_guide);
        llDot = findViewById(R.id.ll_dot);

        List<View> viewList = getGuideViewList();
        if (viewList.isEmpty()) {
            return;
        }

        GuidePagerAdapter adapter = new GuidePagerAdapter(viewList);
        vpGuide.setAdapter(adapter);
        vpGuide.addOnPageChangeListener(onPageChangeListener);
        vpGuide.setCurrentItem(0);

        fillDotLay(viewList.size());
        updateDotLayStatus(0);

        StarUtil.checkPermission(this);

        adapter.setOnDoubleClickListener(onDoubleClickListener);
    }

    @Override
    protected void unInit() {

    }

    public void onCloseClick(View view) {
        Intent intent = new Intent();
        if (StarUtil.hasEnterDetail()) {
            intent.setClass(this, DetailActivity.class);
        } else {
            intent.setClass(this, SearchActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void fillDotLay(int count) {
        llDot.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dotLay = View.inflate(this, R.layout.dot_lay, null);
            llDot.addView(dotLay);
        }
    }

    private void updateDotLayStatus(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= llDot.getChildCount() || selectedIndex == latestSelectedIndex) {
            return;
        }
        FrameLayout dotLay;
        if (latestSelectedIndex >= 0 && latestSelectedIndex < llDot.getChildCount()) {
            dotLay = (FrameLayout) llDot.getChildAt(latestSelectedIndex);
            dotLay.getChildAt(0).setSelected(false);
        }
        dotLay = (FrameLayout) llDot.getChildAt(selectedIndex);
        dotLay.getChildAt(0).setSelected(true);
        latestSelectedIndex = selectedIndex;
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            updateDotLayStatus(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private List<View> getGuideViewList() {
        List<View> viewList = new ArrayList<>();

        for (int resId : mResIdArray) {
            View itemLay = View.inflate(this, R.layout.guide_item_lay, null);
            ImageView imageView = itemLay.findViewById(R.id.iv_icon);
            imageView.setImageResource(resId);
            viewList.add(itemLay);
        }
        return viewList;
    }

    private OnDoubleClickListener onDoubleClickListener = new OnDoubleClickListener() {
        @Override
        public void onDoubleClick(View view) {
            Intent intent = new Intent(GuideActivity.this, ImageBrowseActivity.class);
            intent.putExtra(StarUtil.KEY_SELECTED_INDEX, latestSelectedIndex);
            intent.putExtra(StarUtil.KEY_IMAGE_RES_ID_ARRAY, mResIdArray);
            startActivityForResult(intent, StarUtil.IMAGE_BROWSE_REQUEST_CODE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == StarUtil.IMAGE_BROWSE_REQUEST_CODE && data != null) {
            int tempIndex = data.getIntExtra(StarUtil.KEY_SELECTED_INDEX, Integer.MIN_VALUE);
            if (mResIdArray != null && tempIndex >= 0 && tempIndex < mResIdArray.length) {
                vpGuide.setCurrentItem(tempIndex);
                updateDotLayStatus(tempIndex);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}

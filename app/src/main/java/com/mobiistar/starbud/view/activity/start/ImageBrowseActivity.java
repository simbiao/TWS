package com.mobiistar.starbud.view.activity.start;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;
import com.mobiistar.starbud.view.widget.OnDoubleClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Image browse activity.
 * Date：18-11-10-上午11:29
 * Author: black
 */
public class ImageBrowseActivity extends BaseActivity {
    private LinearLayout llDot;
    private int latestSelectedIndex = 0;
    private float latestX, latestY;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_image_browse;
    }

    @Override
    protected void init() {
        final ViewPager vpImage = findViewById(R.id.vp_image);
        llDot = findViewById(R.id.ll_dot);

        List<View> viewList = getImageViewList();
        if (viewList.isEmpty()) {
            return;
        }

        ImageBrowseAdapter adapter = new ImageBrowseAdapter(viewList);
        vpImage.setAdapter(adapter);
        vpImage.addOnPageChangeListener(onPageChangeListener);
        vpImage.setCurrentItem(latestSelectedIndex);

        fillDotLay(viewList.size());
        updateDotLayStatus(latestSelectedIndex);

        adapter.setOnDoubleClickListener(onDoubleClickListener);
    }

    @Override
    protected void unInit() {

    }

    private void fillDotLay(int count) {
        llDot.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dotLay = View.inflate(this, R.layout.dot_lay, null);
            llDot.addView(dotLay);
        }
    }

    private void updateDotLayStatus(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= llDot.getChildCount()) {
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

    private List<View> getImageViewList() {
        List<View> viewList = new ArrayList<>();
        Intent intent = getIntent();
        int[] resIdArray = null;
        if (intent != null) {
            latestSelectedIndex = intent.getIntExtra(StarUtil.KEY_SELECTED_INDEX, Integer.MIN_VALUE);
            resIdArray = intent.getIntArrayExtra(StarUtil.KEY_IMAGE_RES_ID_ARRAY);
        }
        if (resIdArray != null && resIdArray.length > 0) {
            if (latestSelectedIndex < 0 || latestSelectedIndex >= resIdArray.length) {
                latestSelectedIndex = 0;
            }
            for (int resId : resIdArray) {
                View itemLay = View.inflate(this, R.layout.image_browse_item_lay, null);
                ImageView imageView = itemLay.findViewById(R.id.iv_icon);
                imageView.setImageResource(resId);
                imageView.setOnTouchListener(onTouchListener);
                viewList.add(itemLay);
            }
        }
        return viewList;
    }

    private OnDoubleClickListener onDoubleClickListener = new OnDoubleClickListener() {
        @Override
        public void onDoubleClick(View view) {
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                if (ImageView.ScaleType.CENTER_INSIDE == imageView.getScaleType()) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.scrollBy(-imageView.getScrollX(), -imageView.getScrollY());
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        }
    };

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(StarUtil.KEY_SELECTED_INDEX, latestSelectedIndex);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scrollView(v, event);
            return false;
        }
    };

    private void scrollView(View view, MotionEvent event) {
        if (!(view instanceof ImageView)) {
            return;
        }
        ImageView imageView = (ImageView) view;
        boolean isCenterInside = ImageView.ScaleType.CENTER_INSIDE == imageView.getScaleType();
        if (view.getParent() != null) {
            view.getParent().requestDisallowInterceptTouchEvent(!isCenterInside);
        }
        Drawable drawable = imageView.getDrawable();
        int drawableWidth = drawable != null ? drawable.getIntrinsicWidth() : -1;
        int drawableHeight = drawable != null ? drawable.getIntrinsicHeight() : -1;
        if (isCenterInside || drawableWidth < 0 || drawableHeight < 0) {
            return;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                int x = computeScrollValue(latestX, event.getX(), view.getScrollX(), view.getMeasuredWidth(), drawableWidth);
                int y = computeScrollValue(latestY, event.getY(), view.getScrollY(), view.getMeasuredHeight(), drawableHeight);
                view.scrollBy(x, y);
            case MotionEvent.ACTION_DOWN:
                latestX = event.getX();
                latestY = event.getY();
                break;
        }
    }

    private int computeScrollValue(float latestValue, float currentValue, int scrolledValue, int length, int limitValue) {
        boolean canScroll = (currentValue - latestValue) * scrolledValue > 0 || Math.abs(scrolledValue) + length < limitValue;
        return canScroll ? (int) (latestValue - currentValue) : 0;
    }
}

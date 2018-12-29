package com.mobiistar.starbud.view.activity.start;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.widget.OnDoubleClickListener;

import java.util.List;

/**
 * Description: Image browse adapter.
 * Date：18-11-10-上午11:29
 * Author: black
 */
public class ImageBrowseAdapter extends PagerAdapter {
    private List<View> mViewList;
    private OnDoubleClickListener mOnDoubleClickListener;

    public ImageBrowseAdapter(List<View> viewList) {
        mViewList = viewList;
    }


    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemLay = mViewList.get(position);
        container.addView(itemLay);
        itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDoubleClickListener != null && StarUtil.isDoubleClick(v)) {
                    mOnDoubleClickListener.onDoubleClick(v);
                }
            }
        });
        return itemLay;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View itemLay = mViewList.get(position);
        container.removeView(itemLay);
    }

    public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener) {
        mOnDoubleClickListener = onDoubleClickListener;
    }

}

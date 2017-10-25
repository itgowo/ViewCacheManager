/*
 * Copyright (c) 2017.  Lu jianchao
 */



import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lujianchao on 2017/3/29.
 * View缓存框架，减少ViewGroup频繁执行remove和addView操作，从而优化性能。
 *
 * @param <RootView> 泛型，ViewGroup的子类,如果new ViewCacheManager<T>中加入了泛型，则会约束为具体类型，默认是ViewGroup
 */

public class ViewCacheManager<RootView extends ViewGroup> {
    /**
     * 缓存状态回调
     */
    private onCacheListener mOnCacheListener;

    /**
     * 获取缓存回调监听
     *
     * @return
     */
    public onCacheListener getOnCacheListener() {
        return mOnCacheListener;
    }

    /**
     * 设置缓存回调监听
     *
     * @param mOnCacheListener
     * @return
     */
    public ViewCacheManager setOnCacheListener(onCacheListener mOnCacheListener) {
        this.mOnCacheListener = mOnCacheListener;
        return this;
    }

    /**
     * 设置需要被管理的布局，并根据期望View数量与当前ChildView数量进行计算
     * 如果期望大于当前数量，则调用onAddView()方法
     * 如果期望小于当前数量，则调用onRemoveView()方法
     * 如果期望等于当前数量，则不调用相关方法
     * 处理完ChildView数量变化后，对每个ChildView进行数据绑定操作
     * 遍历ChildView并调用onBindView()方法，将ChildView和position作为参数返回
     *
     * @param mViewGroup  需要被管理的布局，ViewGroup子类，如果new ViewCacheManager<T>中加入了泛型，则会约束为具体类型，默认是ViewGroup
     * @param mFixedValue 期望view数量
     */
    public void onRefresh(RootView mViewGroup, int mFixedValue) {
        int mChangeValue;
        //如果期望大于当前数量，则调用onAddView()方法
        if (mFixedValue > mViewGroup.getChildCount()) {
            mChangeValue = mFixedValue - mViewGroup.getChildCount();
            for (int mI = 0; mI < mChangeValue; mI++) {
                mViewGroup.addView(mOnCacheListener.onAddView(mViewGroup.getChildCount()));
            }
        } else {
            //如果期望小于当前数量，则调用onRemoveView()方法
            mChangeValue = mViewGroup.getChildCount() - mFixedValue;
            for (int mValue = mChangeValue; mValue > 0; mValue--) {
                mViewGroup.removeViewAt(mValue);
                mOnCacheListener.onRemoveView(mViewGroup.getChildCount());
            }
        }
        //refresh
        int count = mViewGroup.getChildCount();
        //遍历ChildView并调用onBindView()方法，将ChildView和position作为参数返回
        for (int mI = 0; mI < count; mI++) {
            mOnCacheListener.onBindView(mI, mViewGroup.getChildAt(mI));
        }
    }

    /**
     * 缓存回调类，用于回调缓存状态的方法
     *
     * @param <ChildView> viewGroup内的子View
     */
    public interface onCacheListener<ChildView> {
        /**
         * 添加子view
         * add childview
         *
         * @param position view position
         * @return childview 加入到ViewGroup内的View
         */
        public View onAddView(int position);

        /**
         * 删除View
         * remove childview
         *
         * @param position view position
         */
        public void onRemoveView(int position);

        /**
         * 刷新后对view做数据绑定操作
         *
         * @param position view position
         * @param mView 待处理的View，可进行数据绑定
         */
        public void onBindView(int position, ChildView mView);
    }
}

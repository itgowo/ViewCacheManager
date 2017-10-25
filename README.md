# ViewCacheManager

##开篇


Android在自定义view的时候经常用到一些布局类view，这些布局继承自ViewGroup类，包括LinearLayout、RelativeLayout和GridLayout等,通常用addview和removeView来操作界面。

优点是纯代码构建布局不需要布局文件，没有布局xml解析等过程，相对来讲性能好一些；
缺点是需要一定开发经验，不适合设置大量属性，简单new对象方式加简单几行代码方式最方便。

我们自定义布局类的View时难免要做缓存降低系统损耗，但是每次都new对象确实很简单。。。每次new对象系统消耗大，
所以我写了一个简单的管理工具（献丑了），用法很简单，使用了观察者模式实现（应该是观察者模式）。

加上注释总共不到一百行代码，我不喜欢弄一大堆东西，我写文章不爱写理论，不爱写长篇，不爱写大的框架，喜欢技巧经验的一些细节点分享，世界那么大，我只提供一块砖，自己设计去造房子。
		

QQ:1264957104

CSDN:http://blog.csdn.net/hnsugar

GitHub:https://github.com/hnsugar

个人做测试项目的服务器:

http://lujianchao.com 

http://itgowo.com 

##原理

ViewGroup添加View用addView()方法，当添加到viewgroup里后，viewgroup自身维护了childView（子View），通过getChildCount()和getChildAt()可以实现遍历和拿到指定childview。

明确一点，我们在刷新数据时是知道数量的，我们比较一下getChildCount()就可以动态添加和删除子View了，我写的这个小工具就是做这个的。

##逻辑过程


 1. 联网刷新数据，拿到数据;
 2. 执行onRefresh通知Manager有几个子View要显示;
 3. 根据数量判断增加几个或删除几个，分别回调执行onAddView()和onDeleteView();
 4. 将onAddView()返回的View加入viewgroup里，onDeleteView()只是提醒，可不处理；
 5. childView数量达到要求后，回调onRemoveView(int position, ReturnView mView)将每个childView回调。
 6. 在onBindView(）回调里对childView进行设置，修改属性等；
 7. 处理完成；

##使用方法（完整代码贴后边了）

####说明

rootview 指布局里的gridlayout；mManager指ViewCacheManager的对象；图片为网络图片，使用glide4.0加载；itemview就是gridlayout的childView，itemview就是一个添加了imageview和textview的LinearLayout；效果如图；使用的是我开发的工具箱制作的屏幕Gif录像，Json视图解析，各种编码，二维码等等一堆我需要用的功能（广告）

[存在Github上的开发工具箱下载地址](https://github.com/hnsugar/QKTool)

![这里写图片描述](https://github.com/hnsugar/ViewCacheManager/raw/master/1.gif)

####使用

 1. new一个管理类对象，gridlayout泛型是rootview的类型。
 
	 	ViewCacheManager<GridLayout> mViewCacheManager = new ViewCacheManager();
 2. 设置监听事件。

		 mViewCacheManager.setOnCacheListener(new ViewCacheManager.onCacheListener<LinearLayout>() {
	            @Override
	            public View onAddView(int position) {
	                System.out.println("添加了一个view" + position);
	                return getItemView();
	            }
	
	            @Override
	            public void onRemoveView(int position) {
	                System.out.println("删除了一个view" + position);
	            }
	
	            @Override
	            public void onBindView(int position, LinearLayout mLinearLayout) {
	                System.out.println("绑定了一个view" + position);
	                Glide.with(MainActivity.this).load(res[position%4]).into((ImageView) mLinearLayout.getChildAt(0));
	                ((TextView) mLinearLayout.getChildAt(1)).setText(mRandom.nextInt(400) + "");
	            }
	        });
	 
 3. 触发刷新机制，第一个参数为泛型对应的对象，100是指这次需要的childview的数量，可以换成mRandom.nextInt(400)，去体验一下效果。

		mViewCacheManager.onRefresh(mLayout,100);
 

 4. 简单的demo在不用缓存的时候体现不出闪烁效果（我的手机好╭(╯^╰)╮），但是在我写的项目里是比较明显的,当然页面也非常复杂，原理什么都对，代码也正常，那我也就只能这样了，demo的gif没出现闪烁也没办法，我在项目里直接用是解决闪烁问题了。

  
 
##上代码（完整）

###ViewCacheManager

	  public class ViewCacheManager<ReturnView extends ViewGroup> {
	    private onCacheListener mOnCacheListener;
	
	    public onCacheListener getOnCacheListener() {
	        return mOnCacheListener;
	    }
	
	    public ViewCacheManager setOnCacheListener(onCacheListener mOnCacheListener) {
	        this.mOnCacheListener = mOnCacheListener;
	        return this;
	    }
	
	    /**
	     * 设置父布局，进行管理，并刷新数据
	     *
	     * @param mViewGroup
	     * @param mFixedValue 期望view数量
	     */
	    public void onRefresh(ReturnView mViewGroup, int mFixedValue) {
	        int mChangeValue;
	        if (mFixedValue > mViewGroup.getChildCount()) {
	            mChangeValue = mFixedValue - mViewGroup.getChildCount();
	            for (int mI = 0; mI < mChangeValue; mI++) {
	                mViewGroup.addView(mOnCacheListener.onAddView(mViewGroup.getChildCount()));
	            }
	        } else {
	            mChangeValue = mViewGroup.getChildCount() - mFixedValue;
	            for (int mValue = mChangeValue; mValue > 0; mValue--) {
	                mViewGroup.removeViewAt(mValue);
	                mOnCacheListener.onRemoveView(mViewGroup.getChildCount());
	            }
	        }
	        //refresh
	        int count = mViewGroup.getChildCount();
	        for (int mI = 0; mI < count; mI++) {
	            mOnCacheListener.onBindView(mI, mViewGroup.getChildAt(mI));
	        }
	    }
	
	
	    public interface onCacheListener<ReturnView> {
	        /**
	         * 添加子view
	         * add childview
	         *
	         * @param position view position
	         * @return childview
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
	         * 刷新后对view做操作
	         *
	         * @param position
	         * @param mView
	         */
	        public void onBindView(int position, ReturnView mView);
	    }
	}

###布局

	<?xml version="1.0" encoding="utf-8"?>
	<LinearLayout
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    android:orientation="vertical"
	    tools:context="cn.parteam.pd.viewcachemanager.MainActivity">
	
	    <TextView
	        android:layout_width="match_parent"
	        android:layout_height="wrap_content"
	        android:text="防止因为刷新造成布局重复和多次new View造成闪烁不流畅问题,页面越复杂越容易出现"/>
	
	    <Button
	        android:id="@+id/test1"
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        android:onClick="onClick"
	        android:text="用缓存"
	        />
	
	    <Button
	        android:id="@+id/test2"
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        android:onClick="onClick"
	        android:text="不用缓存"
	        />
	
	    <ScrollView
	        android:layout_width="match_parent"
	        android:layout_height="match_parent">
	
	        <GridLayout
	            android:id="@+id/gridlayout"
	            android:layout_width="match_parent"
	            android:layout_height="match_parent"
	            android:columnCount="8"></GridLayout>
	    </ScrollView>
	</LinearLayout>

###处理逻辑数据

	public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	    private GridLayout mLayout;
	    private String[] res = new String[]{"https://github.com/hnsugar/ViewCacheManager/raw/master/a1.png",
	            "https://github.com/hnsugar/ViewCacheManager/raw/master/a2.png",
	            "https://github.com/hnsugar/ViewCacheManager/raw/master/a3.png", "https://github.com/hnsugar/ViewCacheManager/raw/master/a4.png"};
	    private Random mRandom = new Random(System.currentTimeMillis());
	
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	        mLayout = (GridLayout) findViewById(R.id.gridlayout);
	    }
	
	    @Override
	    public void onClick(View mView) {
	        if (mView.getId() == R.id.test1) {
	            useCache();
	        } else {
	            noUseCache();
	        }
	    }
	
	    private void noUseCache() {
	        mLayout.removeAllViews();
	        for (int mI = 0; mI < 100; mI++) {
	            LinearLayout mLinearLayout = (LinearLayout) getItemView();
	
	            mLayout.addView(mLinearLayout);
	            Glide.with(MainActivity.this).load(res[mI%4]).into((ImageView) mLinearLayout.getChildAt(0));
	
	            ((TextView) mLinearLayout.getChildAt(1)).setText(mRandom.nextInt(400) + "");
	        }
	    }
	
	    private void useCache() {
	        ViewCacheManager<GridLayout> mViewCacheManager = new ViewCacheManager();
	        mViewCacheManager.setOnCacheListener(new ViewCacheManager.onCacheListener<LinearLayout>() {
	            @Override
	            public View onAddView(int position) {
	                System.out.println("添加了一个view" + position);
	                return getItemView();
	            }
	
	            @Override
	            public void onRemoveView(int position) {
	                System.out.println("删除了一个view" + position);
	            }
	
	            @Override
	            public void onBindView(int position, LinearLayout mLinearLayout) {
	                System.out.println("绑定了一个view" + position);
	                Glide.with(MainActivity.this).load(res[position%4]).into((ImageView) mLinearLayout.getChildAt(0));
	                ((TextView) mLinearLayout.getChildAt(1)).setText(mRandom.nextInt(400) + "");
	            }
	        });
	        mViewCacheManager.onRefresh(mLayout,100);
	    }
	
	    private View getItemView() {
	        LinearLayout mItemLayout = new LinearLayout(this);
	        mItemLayout.setGravity(Gravity.CENTER);
	        mItemLayout.setOrientation(LinearLayout.VERTICAL);
	
	
	        ImageView mImageView = new ImageView(this);
	        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(ui_dip2px(50), ui_dip2px(50));
	        mImageView.setLayoutParams(mParams);
	        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	
	
	        TextView mTextView = new TextView(this);
	        mTextView.setGravity(Gravity.CENTER);
	        mTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
	        mTextView.setTextColor(Color.BLUE);
	
	        mItemLayout.addView(mImageView);
	        mItemLayout.addView(mTextView);
	
	
	        return mItemLayout;
	    }
	
	    public int ui_dip2px(int dip) {
	        final float scale = getResources().getDisplayMetrics().density;
	        return (int) (dip * scale + 0.5f);
	    }
	}
	

package cn.parteam.pd.viewcachemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Random;

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
            public void onDelete(int position) {
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

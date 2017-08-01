package com.stfalcon.bottomtablayout;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.ColorRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.stfalcon.buttontablayout.R;

import java.util.ArrayList;

/**
 * Created by Anton Bevza on 5/5/16.
 */
public class BottomTabLayout extends RelativeLayout implements ViewPager.OnPageChangeListener {
    private ArrayList<TabButton> buttons = new ArrayList<>();
    private OnItemSelectedListener listener;
    private int buttonTextStyle;
    private int selectedId;
    private ViewGroup indicatorGroup;
    private View indicator;
    private View indicatorLine;
    private LinearLayout content;
    int selectedTabColor;
    private boolean mIsViewPagerMode = false;
    private ViewPager mViewPager;
    private Menu menu;
    private ActionBar actionBar;

    public BottomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_bottom_tab_layout, this, true);

        content = (LinearLayout) v.findViewById(R.id.content);
        content.setOrientation(LinearLayout.HORIZONTAL);

        indicatorGroup = (ViewGroup) v.findViewById(R.id.group_indicator);
        indicator = v.findViewById(R.id.indicator);
        indicatorLine = v.findViewById(R.id.indicator_line);
        selectedTabColor = getResources().getColor(R.color.green);
    }

    /**
     * Create and configure tab buttons.
     *
     * @param res Menu resource id
     */
    public void setItems(@MenuRes int res) {
        //Need for getting values from menu resource
        PopupMenu p = new PopupMenu(getContext(), null);
        menu = p.getMenu();
        p.getMenuInflater().inflate(res, menu);

        content.setWeightSum(menu.size());
        for (int i = 0; i < menu.size(); i++) {
            final TabButton tabButton = new TabButton(getContext());
            if (menu.getItem(i).getTitle() != null) {
                tabButton.setText(menu.getItem(i).getTitle().toString());
            } else {
                tabButton.setText(null);
            }
            tabButton.setIcon(menu.getItem(i).getIcon());
            tabButton.setTag(i);
            tabButton.setButtonTextStyle(buttonTextStyle);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            tabButton.setLayoutParams(params);
            buttons.add(tabButton);
            content.addView(tabButton);

            tabButton.setListener(new TabButton.ClickListener() {
                @Override
                public void onClick() {
                    int id = (int) tabButton.getTag();
                    selectTab(id);
                    if(mViewPager!=null) {
                        mViewPager.removeOnPageChangeListener(BottomTabLayout.this);
                        mViewPager.setCurrentItem(id,false);
                        mViewPager.addOnPageChangeListener(BottomTabLayout.this);
                    }
                }
            });
        }
        //set indicator width
        content.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = indicator.getLayoutParams();
                layoutParams.width = buttons.get(0).getWidth();
                indicator.setLayoutParams(layoutParams);
            }
        });
    }

    /**
     * Select tab by menu res id
     *
     * @param id Menu item res id
     */
    public void selectTab(int id) {
        selectTab(id, true);
    }

    private void selectTab(int id, boolean animated) {
        if (selectedId != id) {
            for (TabButton b : buttons) {
                b.setSelected(id == (int) b.getTag());
                if(id==(int)b.getTag()) {
                    actionBar.setTitle(menu.getItem((int) b.getTag()).getTitle());
                }
                b.setSelectedBackground(id == (int)b.getTag(),selectedTabColor);
            }
            selectedId = id;
            if (listener != null) {
                listener.onItemSelected(id);
            }
            if (animated) {
                updateIndicator();
            } else {
                updateIndicatorWithoutAnimation();
            }
        }
    }


    /**
     * Set first selected tab after creating tab layout.
     *
     * @param tabId Menu item res id
     */
    public void setSelectedTab(int tabId) {
        if (tabId != 0) {
            selectTab(tabId, true);
        }
    }

    /**
     * Set text button style. Must be call before setItems() method
     *
     * @param res Style res id
     */
    public void setButtonTextStyle(@StyleRes int res) {
        if (buttons.size() > 0) {
            throw new IllegalStateException("Call this before setItem()");
        }
        buttonTextStyle = res;
    }

    /**
     * Set indicator group visibility
     *
     * @param isVisible visibility
     */
    public void setIndicatorVisible(boolean isVisible) {
        indicatorGroup.setVisibility(isVisible ? VISIBLE : GONE);
    }

    /**
     * Set indicator height
     *
     * @param height height
     */
    public void setIndicatorHeight(float height) {
        ViewGroup.LayoutParams layoutParams = indicator.getLayoutParams();
        layoutParams.height = (int) height;
        indicator.setLayoutParams(layoutParams);
    }

    /**
     * Set indicator color
     *
     * @param color Color res id
     */
    public void setIndicatorColor(@ColorRes int color) {
        indicator.setBackgroundResource(color);
    }

    /**
     * Set selected tab color
     *
     * @param color Color res id
     */
    public void setSelectedTabColor(int color) {
        selectedTabColor = color;
    }

    /**
     * Set indicator line color
     *
     * @param color Color res id
     */
    public void setIndicatorLineColor(@ColorRes int color) {
        indicatorLine.setBackgroundResource(color);
    }

    /**
     * Update indicator position
     */
    private void updateIndicator() {
        ObjectAnimator animX = ObjectAnimator.ofFloat(indicator, "x", buttons.get(getCurrentPosition()).getX() + content.getX());
        animX.setDuration(200);
        animX.start();
    }

    /**
     * Update indicator position
     */
    private void updateIndicatorWithoutAnimation() {
        if (buttons.size() > 0) {
            buttons.get(getCurrentPosition()).post(new Runnable() {
                @Override
                public void run() {
                    indicator.setX(buttons.get(getCurrentPosition()).getX() + content.getX());
                }
            });
        }
    }

    /**
     * Get current selected position
     *
     * @return Position
     */
    private int getCurrentPosition() {
        for (int i = 0; i < buttons.size(); i++) {
            if ((int) buttons.get(i).getTag() == selectedId) {
                return i;
            }
        }
        return 1;
    }
    public void setViewPager(final ViewPager viewPager) {
        // Detect whether ViewPager mode
        if (viewPager == null) {
            mIsViewPagerMode = false;
            return;
        }

        if (viewPager.equals(mViewPager)) return;
        if (mViewPager != null) //noinspection deprecation
            mViewPager.setOnPageChangeListener(null);
        if (viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not provide adapter instance.");

        mIsViewPagerMode = true;
        mViewPager = viewPager;
        mViewPager.removeOnPageChangeListener(this);
        mViewPager.addOnPageChangeListener(this);
        postInvalidate();
    }
    /**
     * Set on item select listener
     *
     * @param listener OnItemSelectedListener
     */
    public void setListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }


    /*
        ---------------------------------
        ADAPTERS FOR ANDROID DATA BINDINGS
        ---------------------------------
     */

    @BindingAdapter("app:items")
    public static void bindItems(BottomTabLayout bottomTabLayout, @MenuRes int res) {
        bottomTabLayout.setItems(res);
    }

    @BindingAdapter("app:listener")
    public static void bindListener(BottomTabLayout bottomTabLayout, OnItemSelectedListener listener) {
        bottomTabLayout.setListener(listener);
    }

    @BindingAdapter("app:selectedTab")
    public static void bindSelectedTab(BottomTabLayout bottomTabLayout, int id) {
        bottomTabLayout.setSelectedTab(id);
    }

    @BindingAdapter("app:buttonTextStyle")
    public static void bindButtonTextStyle(BottomTabLayout bottomTabLayout, @StyleRes int res) {
        bottomTabLayout.setButtonTextStyle(res);
    }

    @BindingAdapter("app:indicatorVisible")
    public static void bindIndicatorVisibke(BottomTabLayout bottomTabLayout, boolean isVisible) {
        bottomTabLayout.setIndicatorVisible(isVisible);
    }

    @BindingAdapter("app:indicatorHeight")
    public static void bindIndicatorHeight(BottomTabLayout bottomTabLayout, float height) {
        bottomTabLayout.setIndicatorHeight(height);
    }

    @BindingAdapter("app:indicatorColor")
    public static void bindIndicatorColor(BottomTabLayout bottomTabLayout, @ColorRes int color) {
        bottomTabLayout.setIndicatorColor(color);
    }

    @BindingAdapter("app:indicatorLineColor")
    public static void bindIndicatorLineColor(BottomTabLayout bottomTabLayout, @ColorRes int color) {
        bottomTabLayout.setIndicatorLineColor(color);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        selectTab(position,true);
        actionBar.setTitle(menu.getItem(position).getTitle());
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }
    /*
        ---------------------------------
     */


    /**
     * Interface for on item click listener
     */
    public interface OnItemSelectedListener {
        void onItemSelected(int id);
    }
}

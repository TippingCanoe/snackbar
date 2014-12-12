package com.nispok.snackbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.layouts.SnackbarLayout;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;
import com.nispok.snackbar.listeners.SwipeDismissTouchListener;

/**
 * View that provides quick feedback about an operation in a small popup at the base of the screen
 */
public class Snackbar extends SnackbarLayout {

    public static final String LOG_TAG = Snackbar.class.getSimpleName();


    public enum SnackbarDuration {
        LENGTH_SHORT(2000), LENGTH_LONG(3500);

        private long duration;

        SnackbarDuration(long duration) {
            this.duration = duration;
        }

        public long getDuration() {
            return duration;
        }
    }

    private SnackbarType mType = SnackbarType.SINGLE_LINE;
    private SnackbarDuration mDuration = SnackbarDuration.LENGTH_LONG;
    private CharSequence mText;
    private int mColor = -1;
    private int mTextColor = -1;
    private int mDrawable = -1;
    private int mOffset;
    private int mPosition = -1;
    private int mMarginTop = -1;
    private int mMarginBottom = -1;
    private int mMarginLeft = -1;
    private int mMarginRight = -1;
    private int mContainerLayoutId = -1;
    private long mSnackbarStart;
    private long mSnackbarFinish;
    private long mTimeRemaining = -1;
    private CharSequence mActionLabel;
    private int mActionColor = -1;
    private boolean mAnimated = true;
    private long mCustomDuration = -1;
    private ActionClickListener mActionClickListener;
    private boolean mShouldDismissOnActionClicked = true;
    private EventListener mEventListener;
    private boolean mIsShowing = false;
    private boolean mCanSwipeToDismiss = true;
    private boolean mIsDismissing = false;
    private Runnable mDismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    private Snackbar(Context context) {
        super(context);
    }

    public static Snackbar with(Context context) {
        return new Snackbar(context);
    }

    /**
     * Sets the type of {@link Snackbar} to be displayed.
     *
     * @param type the {@link SnackbarType} of this instance
     * @return
     */
    public Snackbar type(SnackbarType type) {
        mType = type;
        return this;
    }

    /**
     * Sets the text to be displayed in this {@link Snackbar}
     *
     * @param text
     * @return
     */
    public Snackbar text(CharSequence text) {
        mText = text;
        return this;
    }

    /**
     * Sets the text to be displayed in this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar text(@StringRes int resId) {
        return text(getContext().getText(resId));
    }

    /**
     * Sets the background color of this {@link Snackbar}
     *
     * @param color
     * @return
     */
    public Snackbar color(int color) {
        mColor = color;
        return this;
    }


    /**
     * Sets the background color of this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar colorResource(@ColorRes int resId) {
        return color(getResources().getColor(resId));
    }

    /**
     * Sets the background color of this {@link Snackbar}
     *
     * @param drawable
     * @return
     */
    public Snackbar shape(int drawable) {
        mDrawable = drawable;
        return this;
    }


    /**
     * Sets the text color of this {@link Snackbar}
     *
     * @param textColor
     * @return
     */
    public Snackbar textColor(int textColor) {
        mTextColor = textColor;
        return this;
    }

    /**
     * Sets the text color of this {@link Snackbar}
     *
     * @param resId
     * @return
     */
    public Snackbar textColorResource(@ColorRes int resId) {
        return textColor(getResources().getColor(resId));
    }

    /**
     * Sets the action label to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param actionButtonLabel
     * @return
     */
    public Snackbar actionLabel(CharSequence actionButtonLabel) {
        mActionLabel = actionButtonLabel;
        return this;
    }

    /**
     * Set the Gravity of the snackbar. Recomended values Gravity.TOP and Gravity.BOTTOM
     *
     * @param position
     * @return
     */
    public Snackbar position(int position) {
        mPosition = position;
        return this;
    }

    /**
     * @return the specific in animation depending if the gravity is Top or Bottom
     */
    private int getInAnimation() {
        if (mPosition == Snackbar.TOP)
            return R.anim.snackbar_top_in;
        else
            return R.anim.snackbar_bottom_in;
    }

    /**
     * @return the specific out animation depending if the gravity is Top or Bottom
     */
    private int getOutAnimation() {
        if (mPosition == Snackbar.TOP)
            return R.anim.snackbar_top_out;
        else
            return R.anim.snackbar_bottom_out;
    }

    /**
     * Sets all the margins of the snackbar
     *
     * @param margin size of the margins in pixels
     * @return
     */
    public Snackbar margin(int margin) {
        return margin(margin, margin, margin, margin);
    }

    /**
     * Sets all the margin of the snackbar
     *
     * @param marginLR size of the left and right margins in pixels
     * @param marginTB size of the top and bottom margins in pixels
     * @return
     */
    public Snackbar margin(int marginLR, int marginTB) {
        return margin(marginLR, marginTB, marginLR, marginTB);
    }

    /**
     * Sets all the margin of the snackbar
     *
     * @param marginLeft   size of the left margin in pixels
     * @param marginTop    size of the top margin in pixels
     * @param marginRight  size of the right margin in pixels
     * @param marginBottom size of the bottom margin in pixels
     * @return
     */
    public Snackbar margin(int marginLeft, int marginTop, int marginRight, int marginBottom) {
        mMarginLeft = marginLeft;
        mMarginTop = marginTop;
        mMarginBottom = marginBottom;
        mMarginRight = marginRight;
        return this;
    }

    /**
     * Sets the container where the snackbar will be shown. It must be or inherit a Layout
     *
     * @param containerLayoutId the id of the layout where the snackbar will be shown.
     * @return
     */
    public Snackbar containerLayoutId(int containerLayoutId) {
        mContainerLayoutId = containerLayoutId;
        return this;
    }

    /**
     * Sets the action label to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param resId
     * @return
     */
    public Snackbar actionLabel(@StringRes int resId) {
        return actionLabel(getContext().getString(resId));
    }

    /**
     * Sets the color of the action button label. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param actionColor
     * @return
     */
    public Snackbar actionColor(int actionColor) {
        mActionColor = actionColor;
        return this;
    }

    /**
     * Sets the color of the action button label. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param resId
     * @return
     */
    public Snackbar actionColorResource(@ColorRes int resId) {
        return actionColor(getResources().getColor(resId));
    }

    /**
     * Determines whether this {@link Snackbar} should dismiss when the action button is touched
     *
     * @param shouldDismiss
     * @return
     */
    public Snackbar dismissOnActionClicked(boolean shouldDismiss) {
        mShouldDismissOnActionClicked = shouldDismiss;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link Snackbar} action is
     * selected. Note that you must set a button label with
     * {@link Snackbar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param listener
     * @return
     */
    public Snackbar actionListener(ActionClickListener listener) {
        mActionClickListener = listener;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link Snackbar} is dismissed.
     *
     * @param listener
     * @return
     */
    public Snackbar eventListener(EventListener listener) {
        mEventListener = listener;
        return this;
    }

    /**
     * Sets on/off animation for this {@link Snackbar}
     *
     * @param withAnimation
     * @return
     */
    public Snackbar animation(boolean withAnimation) {
        mAnimated = withAnimation;
        return this;
    }

    /**
     * Determines whether this {@link com.nispok.snackbar.Snackbar} can be swiped off from the screen
     *
     * @param canSwipeToDismiss
     * @return
     */
    public Snackbar swipeToDismiss(boolean canSwipeToDismiss) {
        mCanSwipeToDismiss = canSwipeToDismiss;
        return this;
    }

    /**
     * Sets the duration of this {@link Snackbar}. See
     * {@link Snackbar.SnackbarDuration} for available options
     *
     * @param duration
     * @return
     */
    public Snackbar duration(SnackbarDuration duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Sets a custom duration of this {@link Snackbar}
     *
     * @param duration
     * @return
     */
    public Snackbar duration(long duration) {
        mCustomDuration = duration;
        return this;
    }

    /**
     * Attaches this {@link Snackbar} to an AbsListView (ListView, GridView, ExpandableListView) so
     * it dismisses when the list is scrolled
     *
     * @param absListView
     * @return
     */
    public Snackbar attachToAbsListView(AbsListView absListView) {
        absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                dismiss();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        });

        return this;
    }

    /**
     * Attaches this {@link Snackbar} to a RecyclerView so it dismisses when the list is scrolled
     *
     * @param recyclerView
     * @return
     */
    public Snackbar attachToRecyclerView(RecyclerView recyclerView) {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                dismiss();
            }
        });

        return this;
    }


    private void init(Activity parent) {
        SnackbarLayout layout = (SnackbarLayout) LayoutInflater.from(parent)
                .inflate(R.layout.sb__template, this, true);

        setCustomValues();
        setLayoutBasicParameters(layout);
        setTextProperties(layout);
        setAction(layout);
        setSwipe();

    }

    private void setCustomValues() {
        mColor = mColor != -1 ? mColor : getResources().getColor(R.color.sb__background);
        mPosition = mPosition != -1 ? mPosition : Gravity.BOTTOM;
        mOffset = getResources().getDimensionPixelOffset(R.dimen.sb__offset);
    }

    private void setSwipe() {
        Resources res = getResources();
        if (mCanSwipeToDismiss && res.getBoolean(R.bool.is_swipeable)) {
            setOnTouchListener(new SwipeDismissTouchListener(this, null,
                    new SwipeDismissTouchListener.DismissCallbacks() {
                        @Override
                        public boolean canDismiss(Object token) {
                            return true;
                        }

                        @Override
                        public void onDismiss(View view, Object token) {
                            if (view != null) {
                                dismiss(false);
                            }
                        }

                        @Override
                        public void pauseTimer(boolean shouldPause) {
                            if (shouldPause) {
                                removeCallbacks(mDismissRunnable);

                                mSnackbarFinish = System.currentTimeMillis();
                            } else {
                                mTimeRemaining -= (mSnackbarFinish - mSnackbarStart);

                                startTimer(mTimeRemaining);
                            }
                        }
                    }));
        }
    }

    private void setAction(SnackbarLayout layout) {
        TextView snackbarAction = (TextView) layout.findViewById(R.id.sb__action);
        if (!TextUtils.isEmpty(mActionLabel)) {
            requestLayout();
            snackbarAction.setText(mActionLabel);

            if (mActionColor != -1) {
                snackbarAction.setTextColor(mActionColor);
            }

            snackbarAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionClickListener != null) {
                        mActionClickListener.onActionClicked(Snackbar.this);
                    }
                    if (mShouldDismissOnActionClicked) {
                        dismiss();
                    }
                }
            });
            snackbarAction.setMaxLines(mType.getMaxLines());
        } else {
            snackbarAction.setVisibility(GONE);
        }

        setClickable(true);
    }

    private void setTextProperties(SnackbarLayout layout) {
        TextView snackbarText = (TextView) layout.findViewById(R.id.sb__text);
        snackbarText.setText(mText);

        if (mTextColor != -1) {
            snackbarText.setTextColor(mTextColor);
        }
        snackbarText.setMaxLines(mType.getMaxLines());
    }

    private void setLayoutBasicParameters(SnackbarLayout layout) {
        Resources res = getResources();
        float scale = res.getDisplayMetrics().density;
        if (res.getBoolean(R.bool.is_phone)) {
            layout.setMinimumHeight(dpToPx(mType.getMinHeight(), scale));
            layout.setMaxHeight(dpToPx(mType.getMaxHeight(), scale));
            layout.setBackgroundColor(mColor);
        } else {
            mType = SnackbarType.SINGLE_LINE; // Force single-line
            layout.setMinimumWidth(res.getDimensionPixelSize(R.dimen.sb__min_width));
            layout.setMaxWidth(res.getDimensionPixelSize(R.dimen.sb__max_width));
            layout.setBackgroundResource(R.drawable.sb__bg);
            GradientDrawable bg = (GradientDrawable) layout.getBackground();
            bg.setColor(mColor);
        }
        if (mDrawable != -1)
            setBackgroundDrawable(layout, res.getDrawable(mDrawable));
    }

    private ViewGroup.MarginLayoutParams createParamsWithMargins() {
        Resources res = getResources();
        float scale = res.getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams params;
        if (res.getBoolean(R.bool.is_phone)) {
            params = new ViewGroup.MarginLayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = mMarginLeft != -1 ? mMarginLeft : res.getDimensionPixelOffset(R.dimen.sb__offset);
            params.topMargin = mMarginTop != -1 ? mMarginTop : res.getDimensionPixelOffset(R.dimen.sb__offset);
            params.rightMargin = mMarginRight != -1 ? mMarginRight : res.getDimensionPixelOffset(R.dimen.sb__offset);
            params.bottomMargin = mMarginBottom != -1 ? mMarginBottom : res.getDimensionPixelOffset(R.dimen.sb__offset);

        } else {
            params = new ViewGroup.MarginLayoutParams(
                    LayoutParams.WRAP_CONTENT, dpToPx(mType.getMaxHeight(), scale));
            params.leftMargin = mOffset;
            params.bottomMargin = mOffset;
        }
        return params;
    }

    private FrameLayout.LayoutParams initFrameLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(createParamsWithMargins());
        params.gravity = mPosition;
        return params;
    }

    private LinearLayout.LayoutParams initLinearLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(createParamsWithMargins());
        params.gravity = mPosition;
        return params;
    }

    private RelativeLayout.LayoutParams initRelativeLayoutParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(createParamsWithMargins());
        if (mPosition == TOP)
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        else
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        return params;
    }

    private static int dpToPx(int dp, float scale) {
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Displays the {@link Snackbar} at the bottom of the
     * {@link android.app.Activity} provided.
     *
     * @param targetActivity
     */
    public void show(Activity targetActivity) {

        init(targetActivity);
        ViewGroup root = getRootView(targetActivity);

        bringToFront();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            root.requestLayout();
            root.invalidate();
        }

        mIsShowing = true;

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (mEventListener != null) {
                    mEventListener.onShow(Snackbar.this);
                    if (!mAnimated) {
                        mEventListener.onShown(Snackbar.this);
                    }
                }
                return true;
            }
        });

        if (!mAnimated) {
            startTimer();
            return;
        }

        Animation slideIn = AnimationUtils.loadAnimation(getContext(), getInAnimation());
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mEventListener != null) {
                    mEventListener.onShown(Snackbar.this);
                }

                post(new Runnable() {
                    @Override
                    public void run() {
                        mSnackbarStart = System.currentTimeMillis();

                        if (mTimeRemaining == -1) {
                            mTimeRemaining = getDuration();
                        }

                        startTimer();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideIn);
    }

    private ViewGroup getRootView(Activity targetActivity) {
        ViewGroup root = getViewGroup(targetActivity);

        if (targetActivity.findViewById(mContainerLayoutId) instanceof LinearLayout) {
            LinearLayout.LayoutParams params = initLinearLayoutParams();
            root.addView(this, params);
        } else if (targetActivity.findViewById(mContainerLayoutId) instanceof RelativeLayout) {
            RelativeLayout.LayoutParams params = initRelativeLayoutParams();
            root.addView(this, params);
        } else {
            FrameLayout.LayoutParams params = initFrameLayoutParams();
            root.addView(this, params);
        }
        return root;
    }

    private ViewGroup getViewGroup(Activity targetActivity) {
        if (mContainerLayoutId != -1)
            try {

                return (ViewGroup) targetActivity.findViewById(mContainerLayoutId);
            } catch (Exception e) {
                Log.e(LOG_TAG, "ContainerLayoutId must be or inherit a Layout. \n" + e.getLocalizedMessage());
                return (ViewGroup) targetActivity.findViewById(android.R.id.content);
            }
        else
            return (ViewGroup) targetActivity.findViewById(android.R.id.content);
    }


    private void startTimer() {
        postDelayed(mDismissRunnable, getDuration());
    }

    private void startTimer(long duration) {
        postDelayed(mDismissRunnable, duration);
    }

    public void dismiss() {
        dismiss(mAnimated);
    }

    private void dismiss(boolean animate) {
        if (mIsDismissing) {
            return;
        }

        mIsDismissing = true;

        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismiss(Snackbar.this);
        }

        if (!animate) {
            finish();
            return;
        }

        final Animation slideOut = AnimationUtils.loadAnimation(getContext(), getOutAnimation());
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(slideOut);
    }

    private void finish() {
        clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismissed(this);
        }
        mIsShowing = false;
    }

    public int getActionColor() {
        return mActionColor;
    }

    public CharSequence getActionLabel() {
        return mActionLabel;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getColor() {
        return mColor;
    }

    public CharSequence getText() {
        return mText;
    }

    public long getDuration() {
        return mCustomDuration == -1 ? mDuration.getDuration() : mCustomDuration;
    }

    public SnackbarType getType() {
        return mType;
    }

    /**
     * @return the pixel offset of this {@link com.nispok.snackbar.Snackbar} from the left and
     * bottom of the {@link android.app.Activity}.
     */
    public int getOffset() {
        return mOffset;
    }

    public boolean isAnimated() {
        return mAnimated;
    }

    public boolean shouldDismissOnActionClicked() {
        return mShouldDismissOnActionClicked;
    }

    public static int TOP = Gravity.TOP;
    public static int BOTTOM = Gravity.BOTTOM;

    /**
     * @return true if this {@link com.nispok.snackbar.Snackbar} is currently showing
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * @return false if this {@link com.nispok.snackbar.Snackbar} has been dismissed
     */
    public boolean isDismissed() {
        return !mIsShowing;
    }

    /**
     * @return the animation resource used by this {@link com.nispok.snackbar.Snackbar} instance
     * to enter the view
     */
    @AnimRes
    public int getInAnimationResource() {
        return getInAnimation();
    }

    /**
     * @return the animation resource used by this {@link com.nispok.snackbar.Snackbar} instance
     * to exit the view
     */
    @AnimRes
    public int getOutAnimationResource() {
        return getOutAnimation();
    }

    /**
     * Set a Background Drawable using the appropriate Android version api call
     *
     * @param view
     * @param drawable
     */

    public static void setBackgroundDrawable(View view, Drawable drawable) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }
}

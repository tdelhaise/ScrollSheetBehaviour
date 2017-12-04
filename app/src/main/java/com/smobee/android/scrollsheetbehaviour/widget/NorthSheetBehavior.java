package com.smobee.android.scrollsheetbehaviour.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.math.MathUtils;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.smobee.android.scrollsheetbehaviour.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by thierry on 28/11/2017.
 */


/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as
 * a top sheet.
 */
public class NorthSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
{
    
    private static final String LOG_TAG = "NSB";
    
    /**
     * Callback for monitoring events about bottom sheets.
     */
    public interface NorthSheetCallback {
        
        /**
         * Called when the top sheet changes its state.
         *
         * @param northSheet The north sheet view.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        public void onNorthSheetStateChanged(@NonNull View northSheet, @State int newState);
        
        /**
         * Called when the top sheet is being dragged.
         *
         * @param northSheet The north sheet view.
         * @param slideOffset The new offset of this top sheet within [-1,1] range. Offset
         *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         *                    between hidden and collapsed states.
         */
        public void onNorthSheetSlide(@NonNull View northSheet, float slideOffset);
    }
    
    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;
    
    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = 2;
    
    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = 3;
    
    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 4;
    
    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = 5;
    
    public String getStateText(@State int state)
    {
        switch (state)
        {
            case STATE_DRAGGING:
            {
                return "STATE_DRAGGING";
            }
            case STATE_SETTLING:
            {
                return "STATE_SETTLING";
            }
            case STATE_EXPANDED:
            {
                return "STATE_EXPANDED";
            }
            case STATE_COLLAPSED:
            {
                return "STATE_COLLAPSED";
            }
            case STATE_HIDDEN:
            {
                return "STATE_COLLAPSED";
            }
            default:
            {
                return "UNKNOWN";
            }
        }
    }
    
    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}
    
    /**
     * Peek at the 16:9 ratio keyline of its parent.
     *
     * <p>This can be used as a parameter for {@link #setPeekHeight(int)}.
     * {@link #getPeekHeight()} will return this when the value is set.</p>
     */
    public static final int PEEK_HEIGHT_AUTO = -1;
    
    private static final float HIDE_THRESHOLD = 0.5f;
    
    private static final float HIDE_FRICTION = 0.1f;
    
    private float mMaximumVelocity;
    
    private int mPeekHeight;
    
    private boolean mPeekHeightAuto;
    
    private int mPeekHeightMin;
    
    int mOffsetHidden;
    
    int mOffsetExpanded;
    
    int mOffsetCollapsed;
    
    boolean mHideable;
    
    private boolean mSkipCollapsed;
    
    @State
    int mState = STATE_COLLAPSED;
    
    ViewDragHelper mViewDragHelper;
    
    private boolean mIgnoreEvents;
    
    private int mLastNestedScrollDy;
    
    private boolean mNestedScrolled;
    
    int mParentHeight;
    
    WeakReference<V> mViewRef;
    
    WeakReference<View> mNestedScrollingChildRef;
    
    private NorthSheetCallback mCallback;
    
    private VelocityTracker mVelocityTracker;
    
    int mActivePointerId;
    
    private int mInitialY;
    
    boolean mTouchingScrollingChild;
    
    /**
     * Default constructor for instantiating NorthSheetBehavior.
     */
    public NorthSheetBehavior()
    {
    }
    
    /**
     * Default constructor for inflating NorthSheetBehavior from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public NorthSheetBehavior(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Log.d(LOG_TAG,"NorthSheetBehavior ");
        TypedArray a     = context.obtainStyledAttributes(attrs, R.styleable.NorthSheetBehavior_Layout);
        TypedValue value = a.peekValue(R.styleable.NorthSheetBehavior_Layout_north_behavior_peekHeight);
        if (value != null && value.data == PEEK_HEIGHT_AUTO)
        {
            setPeekHeight(value.data);
        }
        else
        {
            setPeekHeight(a.getDimensionPixelSize(R.styleable.NorthSheetBehavior_Layout_north_behavior_peekHeight, PEEK_HEIGHT_AUTO));
        }
        setHideable(a.getBoolean(R.styleable.NorthSheetBehavior_Layout_north_behavior_hideable, false));
        setSkipCollapsed(a.getBoolean(R.styleable.NorthSheetBehavior_Layout_north_behavior_skipCollapsed, false));
        a.recycle();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child)
    {
        Log.d(LOG_TAG,"onSaveInstanceState ");
        return new SavedState(super.onSaveInstanceState(parent, child), mState);
    }
    
    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state)
    {
        Log.d(LOG_TAG,"onRestoreInstanceState ");
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING)
        {
            mState = STATE_COLLAPSED;
        }
        else
        {
            mState = ss.state;
        }
    }
    
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection)
    {
        Log.d(LOG_TAG,"onLayoutChild ");
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child))
        {
            ViewCompat.setFitsSystemWindows(child, true);
        }
    
        Log.d(LOG_TAG,"onLayoutChild BEFORE child top     [" + child.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE child bottom  [" + child.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE child left    [" + child.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE child right   [" + child.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE child width   [" + child.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE child height  [" + child.getHeight() + "]");
    
    
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent top    [" + parent.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent bottom [" + parent.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent left   [" + parent.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent right  [" + parent.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent width  [" + parent.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild BEFORE parent height [" + parent.getHeight() + "]");
        
        
        int savedTop = child.getTop();
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection);
    
        Log.d(LOG_TAG,"onLayoutChild AFTER child top       [" + child.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER child bottom    [" + child.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER child left      [" + child.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER child right     [" + child.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER child width     [" + child.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER child height    [" + child.getHeight() + "]");
    
        Log.d(LOG_TAG,"onLayoutChild AFTER parent top      [" + parent.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER parent bottom   [" + parent.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER parent left     [" + parent.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER parent right    [" + parent.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER parent width    [" + parent.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild AFTER parent height   [" + parent.getHeight() + "]");
        
        
        // Offset the bottom sheet
        mParentHeight = parent.getHeight();
        int peekHeight;
        if (mPeekHeightAuto)
        {
            if (mPeekHeightMin == 0)
            {
                mPeekHeightMin = parent.getResources().getDimensionPixelSize(R.dimen.design_north_sheet_peek_height_min);
            }
            peekHeight = Math.max(mPeekHeightMin, mParentHeight - parent.getWidth() * 9 / 16);
        }
        else
        {
            peekHeight = mPeekHeight;
        }
    
        int leftCollapsedAnchorPoint = child.getLeft();
        int topCollapsedAnchorPoint = -(mParentHeight - peekHeight);
    
        // l'offset résultant doit donc être negatif
        
        mOffsetCollapsed = - (mParentHeight - peekHeight);
        mOffsetExpanded = 0;
        mOffsetHidden = -mParentHeight;
        
        if (mState == STATE_EXPANDED)
        {
            ViewCompat.offsetTopAndBottom(child, mOffsetExpanded);
        }
        else if (mHideable && mState == STATE_HIDDEN)
        {
            ViewCompat.offsetTopAndBottom(child, mOffsetHidden);
        }
        else if (mState == STATE_COLLAPSED)
        {
            ViewCompat.offsetTopAndBottom(child, mOffsetCollapsed);
        }
        else if (mState == STATE_DRAGGING || mState == STATE_SETTLING)
        {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
        }
        if (mViewDragHelper == null)
        {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        Log.d(LOG_TAG,"onInterceptTouchEvent child [" + child + "] event [" + event + "]");
        if (!child.isShown())
        {
            Log.d(LOG_TAG,"onInterceptTouchEvent child not shown. => FALSE");
            mIgnoreEvents = true;
            return false;
        }
        int action = event.getActionMasked();
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN)
        {
            Log.d(LOG_TAG,"onInterceptTouchEvent => RESET");
            reset();
        }
        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action)
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchingScrollingChild = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                // Reset the ignore flag
                if (mIgnoreEvents)
                {
                    mIgnoreEvents = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                int initialX = (int) event.getX();
                mInitialY = (int) event.getY();
                View scroll = mNestedScrollingChildRef != null ? mNestedScrollingChildRef.get() : null;
                Log.d(LOG_TAG,"onInterceptTouchEvent ACTION_DOWN mNestedScrollingChildRef [" + mNestedScrollingChildRef + "] => scroll [" + scroll + "]");
                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY))
                {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                    
                }
                Log.d(LOG_TAG,"onInterceptTouchEvent ACTION_DOWN => scroll [" + scroll + "] mTouchingScrollingChild [" + mTouchingScrollingChild + "]");
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, initialX, mInitialY);
                Log.d(LOG_TAG,"onInterceptTouchEvent ACTION_DOWN => mIgnoreEvents [" + mIgnoreEvents + "]");
                break;
        }
        Log.d(LOG_TAG,"onInterceptTouchEvent mIgnoreEvents [" + mIgnoreEvents + "] dragHelperIntercept [" + mViewDragHelper.shouldInterceptTouchEvent(event) + "] ");
        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event))
        {
            Log.d(LOG_TAG,"onInterceptTouchEvent mIgnoreEvents [" + mIgnoreEvents + "] dragHelperIntercept [" + mViewDragHelper.shouldInterceptTouchEvent(event) + "] => TRUE ");
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
    
        boolean intercepted = action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) && Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
        Log.d(LOG_TAG,"onInterceptTouchEvent mIgnoreEvents [" + mIgnoreEvents + "] => intercepted [" + intercepted + "]");
        return intercepted;
    }
    
    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        Log.d(LOG_TAG,"onTouchEvent mInitialY [" + mInitialY + "] eventY [" + event.getY() + "]");
        if (!child.isShown())
        {
            Log.d(LOG_TAG,"onTouchEvent: child not shown => FALSE ");
            return false;
        }
        int action = event.getActionMasked();
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN)
        {
            Log.d(LOG_TAG,"onTouchEvent: STATE_DRAGGING && ACTION_DOWN => TRUE");
            return true;
        }
        
        if (mViewDragHelper != null)
        {
            mViewDragHelper.processTouchEvent(event);
        }
        
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN)
        {
            Log.d(LOG_TAG,"onTouchEvent: ACTION_DOWN => RESET");
            reset();
        }
        
        if (mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents)
        {
            Log.d(LOG_TAG,"onTouchEvent: ACTION_MOVE && mIgnoreEvents == FALSE => touchSlop [" + mViewDragHelper.getTouchSlop() + "] ours [" + Math.abs(mInitialY - event.getY()) + "]");
            if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop())
            {
                Log.d(LOG_TAG,"onTouchEvent: ACTION_MOVE && mIgnoreEvents == FALSE => captureChildView");
                mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
            }
        }
        return !mIgnoreEvents;
    }
    
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes)
    {
        Log.d(LOG_TAG,"*** DEPRECATED *** onStartNestedScroll ");
        mLastNestedScrollDy = 0;
        mNestedScrolled = false;
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }
    
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed)
    {
        Log.d(LOG_TAG,"*** DEPRECATED *** onNestedPreScroll dx [" + dx + "] dy [" + dy + "] mLastNestedScrollDy [" + mLastNestedScrollDy + "]" );
        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild)
        {
            return;
        }
    
        int currentTop = child.getTop();
        int newTop = currentTop - dy;
        // Log.w(LOG_TAG,"*** DEPRECATED *** onNestedPreScroll currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetExpanded [" + mOffsetExpanded + "] dy [" + dy + "]");
        if (dy > 0)
        {
            // Upward
            if (newTop < mOffsetExpanded)
            {
                consumed[1] = currentTop - mOffsetExpanded;
                Log.w(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll UPWARD moveOffest [" + consumed[1] + "] EXPANDED");
                ViewCompat.offsetTopAndBottom(child, consumed[1]);
                setStateInternal(STATE_EXPANDED);
                // Log.d(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll UPWARD currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetExpanded [" + mOffsetExpanded + "] offset [" + (-consumed[1]) + "] EXPANDED");
            }
            else
            {
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom(child, dy);
                Log.w(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll UPWARD moveOffest [" + dy + "] DRAGGING");
                // Log.d(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll UPWARD currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetExpanded [" + mOffsetExpanded + "] offset [" + (-dy) + "] DRAGGING");
                setStateInternal(STATE_DRAGGING);
            }
        }
        else if (dy < 0)
        {
            // Downward
            if (!target.canScrollVertically(1))
            {
                // Log.d(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetCollapsed [" + mOffsetCollapsed + "]");
                if (newTop <= mOffsetCollapsed || mHideable)
                {
                    // Log.d(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetExpanded [" + mOffsetExpanded + "] offset [" + (-dy) + "] DRAGGING");
                    consumed[1] = dy;
                    ViewCompat.offsetTopAndBottom(child, dy);
                    Log.w(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD moveOffest [" + dy + "] DRAGGING");
                    setStateInternal(STATE_DRAGGING);
                }
                else
                {
                    consumed[1] = currentTop - mOffsetCollapsed;
                    // Log.d(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD currentTop [" + currentTop + "] newTop [" + newTop + "] mOffsetExpanded [" + mOffsetExpanded + "] offset [" + (-consumed[1]) + "] COLLAPSED");
                    Log.w(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD moveOffest [" + consumed[1] + "] DRAGGING");
                    ViewCompat.offsetTopAndBottom(child, consumed[1]);
                    setStateInternal(STATE_COLLAPSED);
                }
            }
            else
            {
                Log.e(LOG_TAG, "*** DEPRECATED *** onNestedPreScroll DOWNWARD IGNORED");
            }
        }
        dispatchOnSlide(child.getTop());
        mLastNestedScrollDy = dy;
        mNestedScrolled = true;
    }
    
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target)
    {
        Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll ");
        if (child.getTop() == mOffsetExpanded)
        {
            setStateInternal(STATE_EXPANDED);
            Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_EXPANDED");
            return;
        }
        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef.get() || !mNestedScrolled)
        {
            return;
        }
        int top;
        int targetState;
        if (mLastNestedScrollDy > 0)
        {
            top = mOffsetExpanded;
            targetState = STATE_EXPANDED;
            Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_EXPANDED");
        }
        else if (mHideable && shouldHide(child, getXVelocity() ,getYVelocity()))
        {
            top = mOffsetHidden;
            targetState = STATE_HIDDEN;
            Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_HIDDEN");
        }
        else if (mLastNestedScrollDy == 0)
        {
            int currentTop = child.getTop();
            if (Math.abs(currentTop - mOffsetExpanded) < Math.abs(currentTop - mOffsetCollapsed))
            {
                top = mOffsetExpanded;
                targetState = STATE_EXPANDED;
                Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_EXPANDED");
            }
            else
            {
                top = mOffsetCollapsed;
                targetState = STATE_COLLAPSED;
                Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_COLLAPSED");
            }
        }
        else
        {
            top = mOffsetCollapsed;
            targetState = STATE_COLLAPSED;
            Log.d(LOG_TAG,"*** DEPRECATED *** onStopNestedScroll => STATE_COLLAPSED");
        }
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top))
        {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
        }
        else
        {
            setStateInternal(targetState);
        }
        mNestedScrolled = false;
    }
    
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY)
    {
        Log.d(LOG_TAG,"onNestedPreFling ");
        return target == mNestedScrollingChildRef.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }
    
    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels, or
     *                   {@link #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically
     *                   at 16:9 ratio keyline.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final void setPeekHeight(int peekHeight)
    {
        Log.d(LOG_TAG,"setPeekHeight ");
        boolean layout = false;
        if (peekHeight == PEEK_HEIGHT_AUTO)
        {
            if (!mPeekHeightAuto)
            {
                mPeekHeightAuto = true;
                layout = true;
            }
        }
        else if (mPeekHeightAuto || mPeekHeight != peekHeight)
        {
            mPeekHeightAuto = false;
            mPeekHeight = Math.max(0, peekHeight);
            mOffsetCollapsed = mParentHeight - peekHeight;
            layout = true;
        }
        if (layout && mState == STATE_COLLAPSED && mViewRef != null)
        {
            V view = mViewRef.get();
            if (view != null)
            {
                view.requestLayout();
            }
        }
    }
    
    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_HEIGHT_AUTO}
     *         if the sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final int getPeekHeight()
    {
        Log.d(LOG_TAG,"getPeekHeight ");
        return mPeekHeightAuto ? PEEK_HEIGHT_AUTO : mPeekHeight;
    }
    
    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public void setHideable(boolean hideable)
    {
        Log.d(LOG_TAG,"setHideable ");
        mHideable = hideable;
    }
    
    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public boolean isHideable()
    {
        Log.d(LOG_TAG,"isHideable ");
        return mHideable;
    }
    
    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public void setSkipCollapsed(boolean skipCollapsed)
    {
        Log.d(LOG_TAG,"setSkipCollapsed ");
        mSkipCollapsed = skipCollapsed;
    }
    
    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public boolean getSkipCollapsed()
    {
        Log.d(LOG_TAG,"getSkipCollapsed ");
        return mSkipCollapsed;
    }
    
    /**
     * Sets a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    public void setNorthSheetCallback(NorthSheetBehavior.NorthSheetCallback callback)
    {
        Log.d(LOG_TAG,"setNorthSheetCallback ");
        mCallback = callback;
    }
    
    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, or
     *              {@link #STATE_HIDDEN}.
     */
    public final void setState(final @State int state)
    {
        Log.d(LOG_TAG,"setState ");
        if (state == mState)
        {
            return;
        }
        if (mViewRef == null)
        {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED || (mHideable && state == STATE_HIDDEN))
            {
                mState = state;
            }
            return;
        }
        final V child = mViewRef.get();
        if (child == null)
        {
            return;
        }
        // Start the animation; wait until a pending layout if there is one.
        ViewParent parent = child.getParent();
        if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child))
        {
            child.post(new Runnable()
            {
                @Override
                public void run()
                {
                    startSettlingAnimation(child, state);
                }
            });
        }
        else
        {
            startSettlingAnimation(child, state);
        }
    }
    
    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_COLLAPSED}, {@link #STATE_DRAGGING},
     * and {@link #STATE_SETTLING}.
     */
    @State
    public final int getState()
    {
        Log.d(LOG_TAG,"getState ");
        return mState;
    }
    
    void setStateInternal(@State int state)
    {
        Log.d(LOG_TAG,"setStateInternal new state[" + getStateText(state) + "] actual state [" + getStateText(mState) + "]");
        if (mState == state)
        {
            return;
        }
        mState = state;
        View northSheet = mViewRef.get();
        if (northSheet != null && mCallback != null)
        {
            mCallback.onNorthSheetStateChanged(northSheet, state);
        }
    }
    
    private void reset()
    {
        Log.d(LOG_TAG,"reset ");
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    boolean shouldHide(View child, float xvel, float yvel)
    {
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] yvel [" + yvel + "] mSkipCollapsed [" + mSkipCollapsed + "]");
        if (mSkipCollapsed)
        {
            Log.d(LOG_TAG,"shouldHide SKIP COLLAPSED => TRUE ");
            return true;
        }
        // si le point haut de la vue (top) est encore supérieur à mOffsetCollapsed et que
        // la vue doit passer par l'état collapsed alors on ne cache pas la vue.
        Log.d(LOG_TAG,"shouldHide child top [" + child.getTop() + "] more than mOffsetCollapsed [" + mOffsetCollapsed + "] ?");
        if (child.getTop() > mOffsetCollapsed)
        {
            Log.d(LOG_TAG,"shouldHide child top [" + child.getTop() + "] more than mOffsetCollapsed [" + mOffsetCollapsed + "] => FALSE ");
            // It should not hide, but collapse.
            return false;
        }
        
        final float newTop = child.getTop() + yvel * HIDE_FRICTION;
        Log.d(LOG_TAG,"shouldHide new top [" + newTop + "]");
        
        float threshold = Math.abs(newTop - mOffsetCollapsed) / (float) mPeekHeight;
        Log.d(LOG_TAG,"shouldHide threshold [" + threshold + "] => result [" + ((boolean) (threshold > HIDE_THRESHOLD)) + "]");
        return threshold > HIDE_THRESHOLD;
    }
    
    @VisibleForTesting
    View findScrollingChild(View view)
    {
        Log.d(LOG_TAG,"findScrollingChild [" + view + "]");
        if (ViewCompat.isNestedScrollingEnabled(view))
        {
            Log.d(LOG_TAG,"findScrollingChild isNestedScrollingEnabled [true] => view [" + view +"]");
            return view;
        }
        if (view instanceof ViewGroup)
        {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++)
            {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null)
                {
                    Log.d(LOG_TAG,"findScrollingChild in child => view [" + view +"]");
                    return scrollingChild;
                }
            }
        }
        Log.d(LOG_TAG,"findScrollingChild in child => view [null]");
        return null;
    }
    
    private float getYVelocity()
    {
        Log.d(LOG_TAG,"getYVelocity ");
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        return mVelocityTracker.getYVelocity(mActivePointerId);
    }
    
    private float getXVelocity()
    {
        Log.d(LOG_TAG,"getXVelocity ");
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        return mVelocityTracker.getXVelocity(mActivePointerId);
    }
    
    
    void startSettlingAnimation(View child, int state)
    {
        Log.d(LOG_TAG,"startSettlingAnimation state [" + state + "] mOffsetCollapsed [" + mOffsetCollapsed +"] mOffsetExpanded [" + mOffsetExpanded + "] mOffsetHidden [" + mOffsetHidden + "]" );
        int top;
        
        if (state == STATE_COLLAPSED)
        {
            Log.d(LOG_TAG,"startSettlingAnimation STATE_COLLAPSED mOffsetCollapsed [" + mOffsetCollapsed +"] mOffsetExpanded [" + mOffsetExpanded + "] mOffsetHidden [" + mOffsetHidden + "]" );
            top = mOffsetCollapsed;
        }
        else if (state == STATE_EXPANDED)
        {
            Log.d(LOG_TAG,"startSettlingAnimation STATE_EXPANDED mOffsetCollapsed [" + mOffsetCollapsed +"] mOffsetExpanded [" + mOffsetExpanded + "] mOffsetHidden [" + mOffsetHidden + "]" );
            top = mOffsetExpanded;
        }
        else if (mHideable && state == STATE_HIDDEN)
        {
            Log.d(LOG_TAG,"startSettlingAnimation STATE_HIDDEN mOffsetCollapsed [" + mOffsetCollapsed +"] mOffsetExpanded [" + mOffsetExpanded + "] mOffsetHidden [" + mOffsetHidden + "]" );
            top = mOffsetHidden;
        }
        else
        {
            throw new IllegalArgumentException("Illegal state argument: " + state);
        }
        
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top))
        {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
        }
        else
        {
            setStateInternal(state);
        }
    }
    
    private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback()
    {
    
        private static final String LOG_TAG_CALLBACK = "NSBCB";
        
        @Override
        public boolean tryCaptureView(View child, int pointerId)
        {
            Log.d(LOG_TAG_CALLBACK,"tryCaptureView ");
            if (mState == STATE_DRAGGING)
            {
                Log.d(LOG_TAG_CALLBACK,"tryCaptureView mState == STATE_DRAGGING => FALSE");
                return false;
            }
            if (mTouchingScrollingChild)
            {
                Log.d(LOG_TAG_CALLBACK,"tryCaptureView mState [" + mState + "] mTouchingScrollingChild [" + mTouchingScrollingChild + "] => FALSE");
                return false;
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId)
            {
                Log.d(LOG_TAG_CALLBACK,"tryCaptureView STATE_EXPANDED");
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && scroll.canScrollVertically(1))
                {
                    // Let the content scroll down
                    return false;
                }
            }
            return mViewRef != null && mViewRef.get() == child;
        }
        
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        {
            Log.d(LOG_TAG_CALLBACK,"onViewPositionChanged left [" +left + "] top [" + top + "] dx [" + dx + "] dy [" + dy + "]");
            dispatchOnSlide(top);
        }
        
        @Override
        public void onViewDragStateChanged(int state)
        {
            Log.d(LOG_TAG_CALLBACK,"onViewDragStateChanged ");
            if (state == ViewDragHelper.STATE_DRAGGING)
            {
                setStateInternal(STATE_DRAGGING);
            }
        }
        
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel)
        {
            Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"]");
            int top;
            @State int targetState;
            if (yvel > 0)
            {
                // Moving up
                Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_EXPANDED");
                top = mOffsetExpanded;
                targetState = STATE_EXPANDED;
            }
            else if (mHideable && shouldHide(releasedChild, xvel, yvel))
            {
                Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_HIDDEN");
                top = mOffsetHidden;
                targetState = STATE_HIDDEN;
            }
            else if (yvel == 0.f)
            {
                int currentTop = releasedChild.getTop();
                if (Math.abs(currentTop - mOffsetExpanded) < Math.abs(currentTop - mOffsetCollapsed))
                {
                    top = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                    Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_EXPANDED");
                }
                else
                {
                    top = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                    Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_COLLAPSED");
                }
            }
            else
            {
                Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_COLLAPSED");
                top = mOffsetCollapsed;
                targetState = STATE_COLLAPSED;
            }
            
            if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top))
            {
                Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => STATE_SETTLING");
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
            }
            else
            {
                Log.d(LOG_TAG_CALLBACK,"onViewReleased xvel [" + xvel +"] yvel [" + yvel +"] => " + getStateText(targetState));
                setStateInternal(targetState);
            }
        }
        
        @Override
        public int clampViewPositionVertical(View child, int top, int dy)
        {
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionVertical top [" + top + "] dy [" + dy + "] mOffsetExpanded [" + mOffsetExpanded + "] hideable [" + mHideable + "] mOffsetHidden [" + mOffsetHidden + "] mOffsetCollapsed [" + mOffsetCollapsed + "]");
            int clampValue = MathUtils.clamp(top + dy, mHideable ? mOffsetHidden : mOffsetCollapsed, mOffsetExpanded);
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionVertical return clamp value [" + clampValue + "]");
            return clampValue;
        }
        
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx)
        {
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionHorizontal ");
            return child.getLeft();
        }
        
        @Override
        public int getViewVerticalDragRange(View child)
        {
            Log.d(LOG_TAG_CALLBACK,"getViewVerticalDragRange ");
            if (mHideable)
            {
                return mParentHeight - mOffsetExpanded;
            }
            else
            {
                return mOffsetCollapsed - mOffsetExpanded;
            }
        }
    };
    
    void dispatchOnSlide(int top)
    {
        View northSheet = mViewRef.get();
        if (northSheet != null && mCallback != null)
        {
            if (top > mOffsetCollapsed)
            {
                mCallback.onNorthSheetSlide(northSheet,(float) (mOffsetCollapsed - top) /(mParentHeight - mOffsetCollapsed));
            }
            else
            {
                mCallback.onNorthSheetSlide(northSheet,(float) (mOffsetCollapsed - top) / ((mOffsetCollapsed - mOffsetExpanded)));
            }
        }
    }
    
    @VisibleForTesting
    int getPeekHeightMin() {
        Log.d(LOG_TAG,"getPeekHeightMin ");
        return mPeekHeightMin;
    }
    
    private class SettleRunnable implements Runnable {
        
        private final View mView;
        
        @State
        private final int mTargetState;
        
        SettleRunnable(View view, @State int targetState)
        {
            mView = view;
            mTargetState = targetState;
        }
        
        @Override
        public void run()
        {
            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true))
            {
                ViewCompat.postOnAnimation(mView, this);
            }
            else
            {
                setStateInternal(mTargetState);
            }
        }
    }
    
    protected static class SavedState extends AbsSavedState
    {
        @State
        final int state;
        
        public SavedState(Parcel source)
        {
            this(source, null);
        }
        
        public SavedState(Parcel source, ClassLoader loader)
        {
            super(source, loader);
            //noinspection ResourceType
            state = source.readInt();
        }
        
        public SavedState(Parcelable superState, @State int state)
        {
            super(superState);
            this.state = state;
        }
        
        @Override
        public void writeToParcel(Parcel out, int flags)
        {
            super.writeToParcel(out, flags);
            out.writeInt(state);
        }
        
        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader)
            {
                return new SavedState(in, loader);
            }
            
            @Override
            public SavedState createFromParcel(Parcel in)
            {
                return new SavedState(in, null);
            }
            
            @Override
            public SavedState[] newArray(int size)
            {
                return new SavedState[size];
            }
        };
    }
    
    /**
     * A utility function to get the {@link NorthSheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link NorthSheetBehavior}.
     * @return The {@link NorthSheetBehavior} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> NorthSheetBehavior<V> from(V view)
    {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams))
        {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
        if (!(behavior instanceof NorthSheetBehavior))
        {
            throw new IllegalArgumentException("The view is not associated with NorthSheetBehavior");
        }
        return (NorthSheetBehavior<V>) behavior;
    }
}


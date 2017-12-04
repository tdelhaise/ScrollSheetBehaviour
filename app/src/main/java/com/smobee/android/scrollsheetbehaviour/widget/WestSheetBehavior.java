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
 * Created by thierry on 23/11/2017.
 */

public class WestSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
{
    
    private static final String LOG_TAG = "WSB";
    /**
     * Callback for monitoring events about bottom sheets.
     */
    public interface WestSheetCallback {
        /**
         * Called when the west sheet changes its state.
         *
         * @param westSheet   The west sheet view.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        public void onWestSheetStateChanged(@NonNull View westSheet, @State int newState);
        /**
         * Called when the west sheet is being dragged.
         *
         * @param westSheet   The west sheet view.
         * @param slideOffset The new offset of this west sheet within [-1,1] range. Offset
         *                    increases as this west sheet is moving rigth. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         *                    between hidden and collapsed states.
         */
        public void onWestSheetSlide(@NonNull View westSheet, float slideOffset);
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
    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}
    
    
    /**
     * Peek at the 16:9 ratio keyline of its parent.
     *
     * <p>This can be used as a parameter for {@link #setPeekWidth(int)} (int)}.
     * {@link #getPeekWidth()} ()} will return this when the value is set.</p>
     */
    public static final int PEEK_WIDTH_AUTO = -1;
    private static final float HIDE_THRESHOLD = 0.5f;
    private static final float HIDE_FRICTION = 0.1f;
    private float mMaximumVelocity;
    private int mPeekWidth;
    private boolean mPeekWidthAuto;
    private int mPeekWidthtMin;
    int mMinOffset; // offset of top,left view corner of the child view when collapsed
    int mMaxOffset; // offset of top,left view corner of the child view when expanded
    boolean mHideable;
    private boolean mSkipCollapsed;
    @State
    int mState = STATE_COLLAPSED;
    ViewDragHelper mViewDragHelper;
    private boolean mIgnoreEvents;
    private int mLastNestedScrollDx;
    private boolean mNestedScrolled;
    int                 mParentWidth;
    WeakReference<V>    mViewRef;
    WeakReference<View> mNestedScrollingChildRef;
    private WestSheetCallback mCallback;
    private VelocityTracker   mVelocityTracker;
    int mActivePointerId;
    private int mInitialX;
    boolean mTouchingScrollingChild;
    
    
    /**
     * Default constructor for instantiating BottomSheetBehaviors.
     */
    public WestSheetBehavior()
    {
    }
    
    /**
     * Default constructor for inflating BottomSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public WestSheetBehavior(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Log.d(LOG_TAG,"WestSheetBehavior attributes [" + attrs + "]");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WestSheetBehavior_Layout);
        Log.d(LOG_TAG,"WestSheetBehavior typedArray [" + a + "]");
        TypedValue value = a.peekValue(R.styleable.WestSheetBehavior_Layout_west_behavior_peekWidth);
        int peekWidth = 0;
        if (value != null && value.data == PEEK_WIDTH_AUTO)
        {
            peekWidth = value.data;
            setPeekWidth(peekWidth);
        }
        else
        {
            peekWidth = a.getDimensionPixelSize(R.styleable.WestSheetBehavior_Layout_west_behavior_peekWidth, PEEK_WIDTH_AUTO);
            setPeekWidth(peekWidth);
        }
        boolean hideable = a.getBoolean(R.styleable.WestSheetBehavior_Layout_west_behavior_hideable, false);
        setHideable(hideable);
        boolean skipCollapsed = a.getBoolean(R.styleable.WestSheetBehavior_Layout_west_behavior_skipCollapsed,false);
        setSkipCollapsed(skipCollapsed);
        a.recycle();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }
    
    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child)
    {
        Log.d(LOG_TAG,"onSaveInstanceState ...");
        return new SavedState(super.onSaveInstanceState(parent, child), mState);
    }
    
    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state)
    {
        Log.d(LOG_TAG,"onRestoreInstanceState ...");
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }
    }
    
    
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection)
    {
        Log.d(LOG_TAG,"onLayoutChild layoutDirection [" + layoutDirection + "]");
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
        
        
        int savedLeft = child.getLeft();
        Log.d(LOG_TAG,"onLayoutChild savedLeft [" + savedLeft + "]");
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
        

        // Offset the left sheet
        mParentWidth = parent.getWidth();
        Log.d(LOG_TAG,"onLayoutChild #### parentWidth      [" + mParentWidth + "]");
        int peekWidth;
        if (mPeekWidthAuto)
        {
            if (mPeekWidthtMin == 0)
            {
                mPeekWidthtMin = parent.getResources().getDimensionPixelSize(R.dimen.design_west_sheet_peek_width_min);
                Log.d(LOG_TAG,"onLayoutChild #### mPeekWidthtMin [" + mPeekWidthtMin + "]");
            }
            Log.d(LOG_TAG,"onLayoutChild #### mPeekWidthtMin [" + mPeekWidthtMin + "]");
            
            int peekWidhtAuto = parent.getWidth() * 9 / 16;
            Log.d(LOG_TAG,"onLayoutChild #### peekWidthAuto [" + peekWidhtAuto + "]");
            peekWidth = Math.max(mPeekWidthtMin, mParentWidth - peekWidhtAuto);
            Log.d(LOG_TAG,"onLayoutChild #### peekWidth [" + peekWidth + "]");
        }
        else
        {
            peekWidth = mPeekWidth;
            Log.d(LOG_TAG,"onLayoutChild #### peekWidth [" + peekWidth + "]");
        }
    
        // les fonctions ViewCompat.offsetXXX prennent un offset positif ou négatif
        // et déplace la vue en conséquence en appliquant une translation à tous les points remarquables de la vue : les coins.
        
        // Dans le cas d'un WestSheetBehavior, initialement la vue est cachée à la gauche de la vue centrale. La vue centrale
        // à son origine top, left, qui se situe au point de coordonnées 0,0 de contentView d'une fenêtre.
        // Le leftSheet quand à lui à donc son origine décalée vers la gauche (x négatif) par rapport à l'origine de la vue parente et égal
        // à la largeur de la vue enfant (width).
        
        // faire apparaitre la vue de gauche, c'est déplacer le point d'orgine de la vue enfant pour l'amener vers le point d'origine
        // de la fenetre:
        // - quand la vue enfant couvre la totalité de la vue parente (EXPANDED) alors le point d'origine de la vue enfant coincide
        // avec le point d'origine de la vue parente. Les top, left se supperposent
        // - quand la vue enfant couvre partiellement la vue parente (COLLAPSED) c'est le collapsedAnchorPoint qui est rapproché de
        // l'orgine de la vue parente.
        //
        // Dans un LeftSheetBeaviour le "top" du collapsedAnchorPoint est égal au top de la vue parente.
        // Le "left" est lui forcément supérieur au point d'origine de la vue enfant mais inférieur à la largeur de la vue enfant.
        
        int leftCollapsedAnchorPoint = child.getLeft() + child.getWidth() - peekWidth;
        int topCollapsedAnchorPoint = child.getTop();
        
            // l'offset résultant doit donc être négatif
        mMinOffset = - leftCollapsedAnchorPoint;
        mMaxOffset = - child.getLeft();
        
        
        Log.d(LOG_TAG,"onLayoutChild #### mOffsetExpanded [" + mMinOffset + "]");
        Log.d(LOG_TAG,"onLayoutChild #### mOffsetCollapsed [" + mMaxOffset + "]");
    
        if (mState == STATE_EXPANDED)
        {
            Log.d(LOG_TAG,"===> onLayoutChild STATE_EXPANDED offsetLeftAndRight [" + mMaxOffset + "]");
            ViewCompat.offsetLeftAndRight(child, mMaxOffset);
        }
        else if (mHideable && mState == STATE_HIDDEN)
        {
            Log.d(LOG_TAG,"===> onLayoutChild STATE_HIDDEN offsetLeftAndRight [" + -mParentWidth + "]");
            ViewCompat.offsetLeftAndRight(child, -mParentWidth);
        }
        else if (mState == STATE_COLLAPSED)
        {
            Log.d(LOG_TAG,"===> onLayoutChild STATE_COLLAPSED offsetLeftAndRight [" + mMinOffset + "]");
            ViewCompat.offsetLeftAndRight(child, mMinOffset);
        }
        else if (mState == STATE_DRAGGING || mState == STATE_SETTLING)
        {
            int offset = savedLeft - child.getLeft();
            Log.d(LOG_TAG,"===> onLayoutChild STATE_DRAGGING | STATE_SETTLING offsetLeftAndRight [" + offset + "]");
            // ViewCompat.offsetLeftAndRight(child, savedTop - child.getTop());
            ViewCompat.offsetLeftAndRight(child, offset);
        }
        if (mViewDragHelper == null)
        {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
    
        Log.d(LOG_TAG,"onLayoutChild FINAL child top       [" + child.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL child bottom    [" + child.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL child left      [" + child.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL child right     [" + child.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL child width     [" + child.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL child height    [" + child.getHeight() + "]");
    
        Log.d(LOG_TAG,"onLayoutChild FINAL parent top      [" + parent.getTop() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL parent bottom   [" + parent.getBottom() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL parent left     [" + parent.getLeft() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL parent right    [" + parent.getRight() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL parent width    [" + parent.getWidth() + "]");
        Log.d(LOG_TAG,"onLayoutChild FINAL parent height   [" + parent.getHeight() + "]");
        
        
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        Log.e(LOG_TAG,"onInterceptTouchEvent event [" + event + "]");
        if (!child.isShown())
        {
            mIgnoreEvents = true;
            Log.e(LOG_TAG,"onInterceptTouchEvent ### Child Not Shown. IgnoreEvents [" + mIgnoreEvents + "]. Return [false]");
            return false;
        }
        int action = event.getActionMasked();
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN)
        {
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
                    Log.e(LOG_TAG,"onInterceptTouchEvent ### ignoreEvents [" + mIgnoreEvents + "]. Return [false]");
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mInitialX = (int) event.getX();
                int initialY = (int) event.getY();
                Log.e(LOG_TAG,"onInterceptTouchEvent ### mInitialX [" + mInitialX + "]");
                Log.e(LOG_TAG,"onInterceptTouchEvent ### initialY  [" + initialY + "]");
                View scroll = mNestedScrollingChildRef != null ? mNestedScrollingChildRef.get() : null;
                Log.e(LOG_TAG,"onInterceptTouchEvent ### scroll  [" + scroll + "]");
                if (scroll != null && parent.isPointInChildBounds(scroll, mInitialX, initialY))
                {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                    Log.e(LOG_TAG,"onInterceptTouchEvent ### scroll  [" + scroll + "] mTouchingScrollingChild ["+ mTouchingScrollingChild + "]" );
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, mInitialX, initialY);
                Log.e(LOG_TAG,"onInterceptTouchEvent ### ignoreEvents [" + mIgnoreEvents + "].");
                /*
                if(mIgnoreEvents && mInitialX < mPeekWidth && mHideable && mState == STATE_HIDDEN)
                {
                    setState(STATE_EXPANDED);
                }
                */
                break;
        }
        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event))
        {
            Log.e(LOG_TAG,"onInterceptTouchEvent ### ignoreEvents [" + mIgnoreEvents + "]. Return [true]");
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
        
        boolean handled = action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) && Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop();
        Log.e(LOG_TAG,"onInterceptTouchEvent ### ignoreEvents [" + mIgnoreEvents + "]. Return [" + handled + "]");
        return handled;
    }
    
    
    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        Log.d(LOG_TAG,"onTouchEvent event [" + event + "]");
        if (!child.isShown())
        {
            Log.d(LOG_TAG,"onTouchEvent. Return [false]");
            return false;
        }
        
        int action = event.getActionMasked();
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN)
        {
            Log.d(LOG_TAG,"onTouchEvent. Return [true]");
            return true;
        }
        
        if (mViewDragHelper != null)
        {
            mViewDragHelper.processTouchEvent(event);
        }
        
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN)
        {
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
            if (Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop())
            {
                mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
            }
        }
        Log.d(LOG_TAG,"onTouchEvent. Return [" + mIgnoreEvents +"]");
        return !mIgnoreEvents;
    }
    
    
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes)
    {
        Log.d(LOG_TAG,"onStartNestedScroll nestedScrollAxes [" + nestedScrollAxes + "]");
        mLastNestedScrollDx = 0;
        mNestedScrolled = false;
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }
    
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed)
    {
    
        Log.d(LOG_TAG,"onNestedPreScroll dx [" + dx + "] dy [" + dy + "] consumed [" + consumed + "]");
        
        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild)
        {
            return;
        }
        int currentRight = child.getRight();
        int newRight = currentRight - dx;
        if (dx > 0)
        { // To the Rigth
            if (newRight < mMinOffset)
            {
                consumed[1] = newRight - mMinOffset;
                ViewCompat.offsetLeftAndRight(child, -consumed[1]);
                setStateInternal(STATE_EXPANDED);
            }
            else
            {
                consumed[1] = dx;
                ViewCompat.offsetLeftAndRight(child, -dx);
                setStateInternal(STATE_DRAGGING);
            }
        }
        else if (dx < 0)
        { // To the Left
            if (!target.canScrollHorizontally(-1))
            {
                if (newRight <= mMaxOffset || mHideable)
                {
                    consumed[1] = dx;
                    ViewCompat.offsetLeftAndRight(child, -dx);
                    setStateInternal(STATE_DRAGGING);
                }
                else
                {
                    consumed[1] = currentRight - mMaxOffset;
                    ViewCompat.offsetLeftAndRight(child, -consumed[1]);
                    setStateInternal(STATE_COLLAPSED);
                }
            }
        }
        dispatchOnSlide(child.getLeft(), child.getTop(),dx,dy);
        mLastNestedScrollDx = dx;
        mNestedScrolled = true;
    }
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target)
    {
        Log.d(LOG_TAG,"onStopNestedScroll ");
        if (child.getRight() == mMinOffset)
        {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        
        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef.get() || !mNestedScrolled)
        {
            return;
        }
        
        
        int right;
        int targetState;
        if (mLastNestedScrollDx > 0)
        {
            right = mMinOffset;
            targetState = STATE_EXPANDED;
        }
        else if (mHideable && shouldHide(child, getYVelocity()))
        {
            right = mParentWidth;
            targetState = STATE_HIDDEN;
        }
        else if (mLastNestedScrollDx == 0)
        {
            int currentRigth = child.getRight();
            if (Math.abs(currentRigth - mMinOffset) < Math.abs(currentRigth - mMaxOffset))
            {
                right = mMinOffset;
                targetState = STATE_EXPANDED;
            }
            else
            {
                right = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
        }
        else
        {
            right = mMaxOffset;
            targetState = STATE_COLLAPSED;
        }
        if (mViewDragHelper.smoothSlideViewTo(child, child.getRight(), right))
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
        Log.d(LOG_TAG,"onNestedPreFling velocityX [" + velocityX + "] velocityY [" + velocityY + "] ");
        return target == mNestedScrollingChildRef.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }
    /**
     * Sets the width of the bottom sheet when it is collapsed.
     *
     * @param peekWidth The width of the collapsed left sheet in pixels, or
     *                   {@link #PEEK_WIDTH_AUTO} to configure the sheet to peek automatically
     *                   at 16:9 ratio keyline.
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_peekWidth
     */
    public final void setPeekWidth(int peekWidth)
    {
        Log.d(LOG_TAG,"setPeekWidth peekWidth [" + peekWidth + "]");
        boolean layout = false;
        if (peekWidth == PEEK_WIDTH_AUTO) {
            if (!mPeekWidthAuto) {
                mPeekWidthAuto = true;
                layout = true;
            }
        } else if (mPeekWidthAuto || mPeekWidth != peekWidth) {
            mPeekWidthAuto = false;
            mPeekWidth = Math.max(0, peekWidth);
            mMaxOffset = mParentWidth - peekWidth;
            layout = true;
        }
        if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
            V view = mViewRef.get();
            if (view != null) {
                view.requestLayout();
            }
        }
    }
    /**
     * Gets the width of the left sheet when it is collapsed.
     *
     * @return The width of the collapsed left sheet in pixels, or {@link #PEEK_WIDTH_AUTO}
     *         if the sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_peekWidth
     */
    public final int getPeekWidth() {
        Log.d(LOG_TAG,"getPeekWidth ");
        return mPeekWidthAuto ? PEEK_WIDTH_AUTO : mPeekWidth;
    }
    /**
     * Sets whether this left sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this left sheet hideable.
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_hideable
     */
    public void setHideable(boolean hideable) {
        mHideable = hideable;
    }
    /**
     * Gets whether this left sheet can hide when it is swiped down.
     *
     * @return {@code true} if this left sheet can hide.
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_hideable
     */
    public boolean isHideable() {
        Log.d(LOG_TAG,"isHideable ");
        return mHideable;
    }
    /**
     * Sets whether this left sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the left sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_skipCollapsed
     */
    public void setSkipCollapsed(boolean skipCollapsed)
    {
        mSkipCollapsed = skipCollapsed;
    }
    /**
     * Sets whether this left sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#LeftSheetBehavior_Layout_behavior_skipCollapsed
     */
    public boolean getSkipCollapsed()
    {
        return mSkipCollapsed;
    }
    /**
     * Sets a callback to be notified of left sheet events.
     *
     * @param callback The callback to notify when left sheet events occur.
     */
    public void setWestSheetCallback(WestSheetCallback callback)
    {
        mCallback = callback;
    }
    /**
     * Sets the state of the left sheet. The left sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, or
     *              {@link #STATE_HIDDEN}.
     */
    public final void setState(final @State int state)
    {
        Log.d(LOG_TAG,"setState state [" + state + "]");
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
     * Gets the current state of the left sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_COLLAPSED}, {@link #STATE_DRAGGING},
     * and {@link #STATE_SETTLING}.
     */
    @State
    public final int getState()
    {
        return mState;
    }
    void setStateInternal(@State int state)
    {
        Log.d(LOG_TAG,"setStateInternal state [" + state + "]");
        if (mState == state)
        {
            return;
        }
        mState = state;
        View leftSheet = mViewRef.get();
        if (leftSheet != null && mCallback != null)
        {
            mCallback.onWestSheetStateChanged(leftSheet, state);
        }
    }
    
    private void reset()
    {
        Log.d(LOG_TAG,"reset");
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    boolean shouldHide(View child, float xvel)
    {
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "]");
        if (mSkipCollapsed)
        {
            Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => TRUE");
            return true;
        }
        if (mMinOffset < child.getLeft() && child.getLeft() < mMaxOffset)
        {
            Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => left........[" + child.getLeft() + "]");
            Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => mOffsetCollapsed..[" + mMinOffset + "]");
            // It should not hide, but collapse.
            Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => FALSE");
            return false;
        }
        
        final float newLeft = child.getLeft() + xvel * HIDE_FRICTION;
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => newLeft .............................................[" + newLeft + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => HIDE_THRESHOLD ......................................[" + HIDE_THRESHOLD + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => mPeekWidth ..........................................[" + mPeekWidth + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => mOffsetCollapsed ..........................................[" + mMaxOffset + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => newLeft - mOffsetCollapsed ................................[" + (newLeft - mMaxOffset) + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => Math.abs(newLeft - mOffsetCollapsed) ......................[" + Math.abs(newLeft - mMaxOffset) + "]");
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => Math.abs(newLeft - mOffsetCollapsed) / (float) mPeekWidth .[" + Math.abs(newLeft - mMaxOffset) / (float) mPeekWidth + "]");
        boolean result = Math.abs(newLeft - mMaxOffset) / (float) mPeekWidth > HIDE_THRESHOLD;
        Log.d(LOG_TAG,"shouldHide xvel [" + xvel + "] => result ..............................................[" + result + "]");
        return result;
    }
    @VisibleForTesting
    View findScrollingChild(View view)
    {
        Log.d(LOG_TAG,"findScrollingChild view [" + view + "]");
        if (ViewCompat.isNestedScrollingEnabled(view))
        {
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
                    return scrollingChild;
                }
            }
        }
        return null;
    }
    private float getYVelocity()
    {
        Log.d(LOG_TAG,"getYVelocity ");
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        return mVelocityTracker.getYVelocity(mActivePointerId);
    }
    
    void startSettlingAnimation(View child, int state)
    {
        Log.w(LOG_TAG,"startSettlingAnimation state        [" + state + "]");
        Log.w(LOG_TAG,"startSettlingAnimation mOffsetCollapsed   [" + mMaxOffset + "]");
        Log.w(LOG_TAG,"startSettlingAnimation mOffsetExpanded   [" + mMinOffset + "]");
        Log.w(LOG_TAG,"startSettlingAnimation mParentWidth [" + mParentWidth + "]");
        Log.w(LOG_TAG,"startSettlingAnimation child top    [" + child.getTop() + "]");
        
        int left;
        if (state == STATE_COLLAPSED)
        {
            left = mMinOffset;
            
        }
        else if (state == STATE_EXPANDED)
        {
            left = mMaxOffset;
        }
        else if (mHideable && state == STATE_HIDDEN)
        {
            left = - mParentWidth;
        }
        else
        {
            throw new IllegalArgumentException("Illegal state argument: " + state);
        }
        
        if (mViewDragHelper.smoothSlideViewTo(child, left, child.getTop()))
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
        private static final String LOG_TAG_CALLBACK = "LSBCB";
        
        @Override
        public boolean tryCaptureView(View child, int pointerId)
        {
            Log.d(LOG_TAG_CALLBACK,"tryCaptureView pointerId [" + pointerId + "]");
            if (mState == STATE_DRAGGING)
            {
                return false;
            }
            if (mTouchingScrollingChild)
            {
                return false;
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId)
            {
                View scroll = mNestedScrollingChildRef.get();
                // TODO ...
                if (scroll != null && scroll.canScrollHorizontally(1))
                {
                    // Let the content scroll up
                    return false;
                }
            }
            return mViewRef != null && mViewRef.get() == child;
        }
        
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        {
            // Log.d(LOG_TAG_CALLBACK,"onViewPositionChanged left [" + left + "] top [" + top + "] dx [" + dx  + "] dy [" + dy + "]");
            dispatchOnSlide(left, top, dx, dy);
        }
        
        @Override
        public void onViewDragStateChanged(int state)
        {
            Log.d(LOG_TAG_CALLBACK,"onViewDragStateChanged state [" + state + "]" );
            if (state == ViewDragHelper.STATE_DRAGGING)
            {
                setStateInternal(STATE_DRAGGING);
            }
        }
        
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel)
        {
            Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "]" );
            int left;
            @State int targetState;
            
            if (xvel > 0)
            {
                // Moving left
                left = mMaxOffset;
                targetState = STATE_EXPANDED;
                Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "] targetState STATE_EXPANDED" );
            }
            else if (mHideable && shouldHide(releasedChild, xvel))
            {
                left = -mParentWidth;
                targetState = STATE_HIDDEN;
                Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "] targetState STATE_HIDDEN" );
            }
            else if (xvel == 0.f)
            {
                int currentLeft = releasedChild.getLeft();
                if (Math.abs(currentLeft - mMinOffset) < Math.abs(currentLeft - mMaxOffset))
                {
                    left = mMinOffset;
                    targetState = STATE_EXPANDED;
                    Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "] targetState STATE_EXPANDED" );
                }
                else
                {
                    left = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                    Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "] targetState STATE_COLLAPSED" );
                }
            }
            else
            {
                left = mMinOffset;
                targetState = STATE_COLLAPSED;
                Log.d(LOG_TAG_CALLBACK,"***** onViewReleased xvel [" + xvel + "] yvel [" + yvel + "] targetState STATE_COLLAPSED" );
            }
            
            
            // TODO
            if (mViewDragHelper.settleCapturedViewAt(left, releasedChild.getTop()))
            {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
            }
            else
            {
                setStateInternal(targetState);
            }
        }
        
        @Override
        public int clampViewPositionVertical(View child, int top, int dy)
        {
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionVertical: top [" + top + "] dy [" + dy + "]" );
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionVertical: clamp position for top [" + child.getTop() + "]" );
            return child.getTop();
        }
        
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx)
        {
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionHorizontal: left [" + left + "] dx [" + dx + "] mOffsetExpanded    [" + mMinOffset + "]" );
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionHorizontal: left [" + left + "] dx [" + dx + "] mOffsetCollapsed    [" + mMaxOffset + "]" );
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionHorizontal: left [" + left + "] dx [" + dx + "] mParentWidth  [" + mParentWidth + "]" );
            
            int newPos = left + dx;
            int clampPositionForHorizontal = MathUtils.clamp(newPos, mHideable ? -mParentWidth : mMinOffset, mMaxOffset );
            Log.d(LOG_TAG_CALLBACK,"clampViewPositionHorizontal: left [" + left + "] dx [" + dx + "] clampPosition [" + clampPositionForHorizontal + "]" );
            return clampPositionForHorizontal;
        }
        
        @Override
        // public int getViewVerticalDragRange(View child)
        public int getViewHorizontalDragRange(View child)
        {
            int dragRange = 0;
            Log.d(LOG_TAG_CALLBACK,"getViewHorizontalDragRange mParentWidth [" + mParentWidth + "]" );
            Log.d(LOG_TAG_CALLBACK,"getViewHorizontalDragRange mOffsetExpanded   [" + mMinOffset + "]" );
            Log.d(LOG_TAG_CALLBACK,"getViewHorizontalDragRange mOffsetCollapsed   [" + mMaxOffset + "]" );
            if (mHideable)
            {
                dragRange = mParentWidth - mMinOffset;
            }
            else
            {
                dragRange = mMaxOffset - mMinOffset;
            }
            Log.d(LOG_TAG_CALLBACK,"getViewHorizontalDragRange dragRange    [" + dragRange + "]" );
            return dragRange;
        }
    };
    
    void dispatchOnSlide(int left, int top, int dx, int dy)
    {
        Log.d(LOG_TAG,"dispatchOnSlide left [" + left + "] top [" + top + "] dx [" + dx + "] dy [" + dy + "]" );
        View leftSheet = mViewRef.get();
        if (leftSheet != null && mCallback != null)
        {
            if (left > mMaxOffset)
            {
                float slideOffset = (float) (mMaxOffset - left) / (mParentWidth - mMaxOffset);
                Log.d(LOG_TAG,"dispatchOnSlide slideOffset [" + slideOffset + "]" );
                mCallback.onWestSheetSlide(leftSheet, slideOffset);
            }
            else
            {
                float slideOffset = (float) (mMaxOffset - left) / ((mMaxOffset - mMinOffset));
                Log.d(LOG_TAG,"dispatchOnSlide slideOffset [" + slideOffset + "]" );
                mCallback.onWestSheetSlide(leftSheet,slideOffset);
            }
        }
    }
    
    @VisibleForTesting
    int getPeekWidthMin()
    {
        return mPeekWidthtMin;
    }
    
    private class SettleRunnable implements Runnable
    {
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
        
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    /**
     * A utility function to get the {@link WestSheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link WestSheetBehavior}.
     * @return The {@link WestSheetBehavior} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> WestSheetBehavior<V> from(V view)
    {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams))
        {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
        if (!(behavior instanceof WestSheetBehavior))
        {
            throw new IllegalArgumentException("The view is not associated with WestSheetBehavior");
        }
        return (WestSheetBehavior<V>) behavior;
    }
}




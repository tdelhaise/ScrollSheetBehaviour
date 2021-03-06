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
import java.util.UUID;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by thierry on 01/12/2017.
 */

public class SheetBehavior <V extends View> extends CoordinatorLayout.Behavior<V>
{
    private static final String LOG_TAG = "SHEETBVIOR";
    
    private static final int SCROLL_UP =  -1;
    private static final int SCROLL_DOWN =  1;
    private static final int SCROLL_LEFT =  -1;
    private static final int SCROLL_RIGHT =  1;
    
    private String getLogTag()
    {
        return LOG_TAG + "-" + mIdentifierName;
    }
    
    /**
     * Callback for monitoring events about bottom sheets.
     */
    public interface SheetCallback
    {
        /**
         * Called when the sheet changes its state.
         *
         * @param sheet    The sheet view.
         * @param newState The new state. This will be one of {@link #STATE_DRAGGING},
         *                 {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                 {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         * @param nickName The nickName string of the sheet
         */
        public void onSheetStateChanged(@NonNull View sheet, @SheetBehavior.State int newState, final String nickName);
        
        // TODO : complete documentation
    
        /**
         * Called when the sheet is being dragged.
         *
         * @param sheet       The east sheet view.
         * @param slideOffset The new offset of this sheet within [-1,1] range. Offset
         *                    increases as this sheet is moving left. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         *                    between hidden and collapsed states.
         * @param nickName    The nickName string of the sheet
         */
        public void onSheetSlide(@NonNull View sheet, float slideOffset, final String nickName);
    }
    
    /**
     * The sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;
    
    /**
     * The sheet is settling.
     */
    public static final int STATE_SETTLING = 2;
    
    /**
     * The sheet is expanded.
     */
    public static final int STATE_EXPANDED = 3;
    
    /**
     * The sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 4;
    
    /**
     * The sheet is hidden.
     */
    public static final int STATE_HIDDEN = 5;
    
    private String getActionString(int action)
    {
        switch (action)
        {
            case MotionEvent.ACTION_BUTTON_PRESS:
            {
                return "ACTION_BUTTON_PRESS";
            }
            case MotionEvent.ACTION_BUTTON_RELEASE:
            {
                return "ACTION_BUTTON_RELEASE";
            }
            case MotionEvent.ACTION_CANCEL:
            {
                return "ACTION_CANCEL";
            }
            case MotionEvent.ACTION_DOWN:
            {
                return "ACTION_DOWN";
            }
            case MotionEvent.ACTION_HOVER_ENTER:
            {
                return "ACTION_HOVER_ENTER";
            }
            case MotionEvent.ACTION_HOVER_EXIT:
            {
                return "ACTION_HOVER_EXIT";
            }
            case MotionEvent.ACTION_HOVER_MOVE:
            {
                return "ACTION_HOVER_MOVE";
            }
            case MotionEvent.ACTION_MOVE:
            {
                return "ACTION_MOVE";
            }
            case MotionEvent.ACTION_OUTSIDE:
            {
                return "ACTION_OUTSIDE";
            }
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                return "ACTION_POINTER_DOWN";
            }
            case MotionEvent.ACTION_POINTER_UP:
            {
                return "ACTION_POINTER_UP";
            }
            case MotionEvent.ACTION_SCROLL:
            {
                return "ACTION_SCROLL";
            }
            case MotionEvent.ACTION_UP:
            {
                return "ACTION_UP";
            }
            default:
            {
                return "UNKNOWN ACTION ID [" + action + "]";
            }
        }
    }
    
    private String getDragHelperStateString(int state)
    {
        switch (state)
        {
            case ViewDragHelper.STATE_DRAGGING:
            {
                return "DRAGGING";
            }
            case ViewDragHelper.STATE_IDLE:
            {
                return "IDLE";
            }
            case ViewDragHelper.STATE_SETTLING:
            {
                return "SETTLING";
            }
            default:
            {
                return "UNKNOWN [" + state + "]";
            }
        }
    }
    
    private String getStateString(@SheetBehavior.State int state)
    {
        switch (state)
        {
            case STATE_DRAGGING:
            {
                return "DRAGGING";
            }
            case STATE_SETTLING:
            {
                return "SETTLING";
            }
            case STATE_EXPANDED:
            {
                return "EXPANDED";
            }
            case STATE_COLLAPSED:
            {
                return "COLLAPSED";
            }
            case STATE_HIDDEN:
            {
                return "HIDDEN";
            }
            default:
            {
                return "UNKNOWN [" + state + "]";
            }
        }
    }
    
    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State
    {
    }
    
    
    /**
     * The sheet is at north position.
     */
    public static final int POSITION_NORTH = 1;
    
    /**
     * The sheet is at south position.
     */
    public static final int POSITION_SOUTH = 2;
    
    /**
     * The sheet is at east position.
     */
    public static final int POSITION_EAST = 3;
    
    /**
     * The sheet is at west position.
     */
    public static final int POSITION_WEST = 4;
    
    @RestrictTo(LIBRARY_GROUP)
    @IntDef({POSITION_NORTH, POSITION_SOUTH, POSITION_EAST, POSITION_WEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Position
    {
    }
    
    public enum NearestOffsetPosition
    {
        OVER_HIDDEN,
        HIDDEN,
        COLLAPSED,
        EXPANDED,
        OVER_EXPANDED
    }
    
    
    /**
     * Peek at the 16:9 ratio keyline of its parent.
     * <p>
     * <p>This can be used as a parameter for {@link #setPeekSize(int)}.
     * {@link #getPeekSize()} will return this when the value is set.</p>
     */
    public static final int PEEK_SIZE_AUTO = -1;
    
    private static final float HIDE_THRESHOLD = 0.5f;
    
    private static final float HIDE_FRICTION = 0.1f;
    
    private float mMaximumVelocity;
    
    private int mPeekSize;
    
    private boolean mPeekSizeAuto = false;
    
    private int mPeekSizeMin;
    
    private int mOffsetHidden;
    
    private int mOffsetExpanded;
    
    private int mOffsetCollapsed;
    
    private boolean mHideable = false;
    
    private boolean mSkipCollapsed = false;
    
    @SheetBehavior.State
    private int mState = STATE_COLLAPSED;
    
    @SheetBehavior.Position
    private int mPosition = POSITION_SOUTH; // like a bottom sheet.
    
    // wether the sheet move horizontaly or verticaly
    private boolean mSheetMoveHorizontaly = false;
    // wheter the sheet when it's hidden, is place before top,left or after top,left
    private boolean mSheetHiddenBeforeOrigin = false;
    
    private ViewDragHelper mViewDragHelper;
    
    private boolean mIgnoreEvents;
    
    private int mLastNestedScrollDy;
    
    private int mLastNestedScrollDx;
    
    private boolean mNestedScrolled;
    
    private int mParentHeight;
    
    private int mParentWidth;
    
    private WeakReference<V> mViewRef;
    
    private WeakReference<View> mNestedScrollingChildRef;
    
    private SheetCallback mCallback;
    
    private VelocityTracker mVelocityTracker;
    
    private int mActivePointerId;
    
    private int mInitialY;
    
    private int mInitialX;
    
    private boolean mTouchingScrollingChild;
    
    private String mIdentifierName;
    
    
    
    /**
     * Default constructor for instantiating SheetBehavior.
     */
    public SheetBehavior()
    {
        super();
        Log.d(LOG_TAG, "SheetBehavior initializing from default constructor ...");
        setHideable(false);
        setSkipCollapsed(false);
        setPosition(POSITION_SOUTH);
        setIdentifierName(UUID.randomUUID().toString());
        Log.d(LOG_TAG, "SheetBehavior initialized.");
    }
    
    /**
     * Default constructor for inflating SheetBehavior from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public SheetBehavior(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Log.d(LOG_TAG, "SheetBehavior initializing from ressources ...");
        TypedArray a     = context.obtainStyledAttributes(attrs, R.styleable.SheetBehavior_Layout);
        TypedValue value = a.peekValue(R.styleable.SheetBehavior_Layout_sheet_behavior_peekSize);
        if (value != null && value.data == PEEK_SIZE_AUTO)
        {
            setPeekSize(value.data);
        }
        else
        {
            setPeekSize(a.getDimensionPixelSize(R.styleable.SheetBehavior_Layout_sheet_behavior_peekSize, PEEK_SIZE_AUTO));
        }
        setHideable(a.getBoolean(R.styleable.SheetBehavior_Layout_sheet_behavior_hideable, false));
        setSkipCollapsed(a.getBoolean(R.styleable.SheetBehavior_Layout_sheet_behavior_skipCollapsed, false));
        setPosition(a.getInt(R.styleable.SheetBehavior_Layout_sheet_behavior_position,POSITION_SOUTH));
        setIdentifierName(a.getString(R.styleable.SheetBehavior_Layout_sheet_behavior_identifier_name));
        a.recycle();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        Log.d(LOG_TAG, "SheetBehavior initialized.");
    }
    
    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child)
    {
        Log.d(getLogTag(),"onSaveInstanceState parent [" + parent + "] child [" + child + "]");
        return new SheetBehavior.SavedState(super.onSaveInstanceState(parent, child), mState, mPosition,mIdentifierName);
    }
    
    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state)
    {
        Log.d(getLogTag(),"onRestoreInstanceState ");
        SheetBehavior.SavedState ss = (SheetBehavior.SavedState) state;
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
    
    /**
     * Sets the name identifier of the sheet.
     *
     * @param identifierName  A string that you provide and that will passed back to you in callback in order to let
     *                  you identified which sheet is given you back informations
     * @attr            ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_behavior_identifier_name
     */
    public final void setIdentifierName(final String identifierName)
    {
        mIdentifierName = identifierName;
    }
    
    /**
     * Get the identifier name of the sheet
     *
     * @return   The identifier name of the sheet
     * @attr     ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_behavior_identifier_name
     */
    public final String getIdentifierName()
    {
        return mIdentifierName;
    }
    
    /**
     * Sets the size of the sheet when it is collapsed.
     *
     * @param peekSize  The size of the collapsed sheet in pixels, or
     *                  {@link #PEEK_SIZE_AUTO} to configure the sheet to peek automatically
     *                  at 16:9 ratio keyline.
     * @attr            ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_behavior_peekSize
     */
    public final void setPeekSize(int peekSize)
    {
        Log.d(getLogTag(),"setPeekSize [" + peekSize + "]");
        boolean layout = false;
        if (peekSize == PEEK_SIZE_AUTO)
        {
            if (!mPeekSizeAuto)
            {
                mPeekSizeAuto = true;
                layout = true;
            }
        }
        else if (mPeekSizeAuto || mPeekSize != peekSize)
        {
            mPeekSizeAuto = false;
            mPeekSize = Math.max(0, peekSize);
            // TODO : depends of the postion of the view ...
            mOffsetCollapsed = mParentHeight - mPeekSize;
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
     * Gets the height of the sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_SIZE_AUTO}
     *         if the sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr   ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_sheet_behavior_peekSize
     */
    public final int getPeekSize()
    {
        Log.d(getLogTag(),"getPeekSize ");
        return mPeekSizeAuto ? PEEK_SIZE_AUTO : mPeekSize;
    }
    
    /**
     * Sets whether this sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_sheet_behavior_hideable
     */
    public void setHideable(boolean hideable)
    {
        Log.d(getLogTag(),"setHideable [" + hideable + "]");
        mHideable = hideable;
    }
    
    /**
     * Gets whether this sheet can hide when it is swiped to its hideable position.
     *
     * @return {@code true} if this sheet can hide.
     * @attr ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_sheet_behavior_hideable
     */
    public boolean isHideable()
    {
        Log.d(getLogTag(),"isHideable ");
        return mHideable;
    }
    
    /**
     * Sets whether this sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the sheet should skip the collapsed state.
     * @attr ref com.smobee.android.scrollsheetbehaviour.R.styleable#SheetBehavior_Layout_sheet_behavior_skipCollapsed
     */
    public void setSkipCollapsed(boolean skipCollapsed)
    {
        Log.d(getLogTag(),"setSkipCollapsed [" + skipCollapsed + "]");
        mSkipCollapsed = skipCollapsed;
    }
    
    /**
     * Sets whether this sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public boolean getSkipCollapsed()
    {
        Log.d(getLogTag(),"getSkipCollapsed ");
        return mSkipCollapsed;
    }
    
    /**
     * Sets a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    public void setSheetCallback(SheetBehavior.SheetCallback callback)
    {
        Log.d(getLogTag(),"setSheetCallback ");
        mCallback = callback;
    }
    
    /**
     * Sets the state of the sheet. The sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, or
     *              {@link #STATE_HIDDEN}.
     */
    public final void setState(final @SheetBehavior.State int state)
    {
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
     * Gets the current state of the sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_COLLAPSED}, {@link #STATE_DRAGGING},
     * and {@link #STATE_SETTLING}.
     */
    @SheetBehavior.State
    public final int getState()
    {
        return mState;
    }
    
    
    /**
     * Sets the starting position of the sheet: the side where the sheet is initialy hidden or collapsed.
     * For example, setting the starting postion to {@link #POSITION_SOUTH} give the same behavior as a
     * standard BottomSheetBehavior.
     *
     * @param position One of {@link #POSITION_NORTH}, {@link #POSITION_SOUTH}, {@link #POSITION_EAST} or
     *              {@link #POSITION_WEST}.
     */
    public final void setPosition(final @SheetBehavior.Position int position)
    {
        Log.d(getLogTag(),"setPosition [" + position + "]");
        mPosition = position;
        switch(mPosition)
        {
            case POSITION_EAST:
            {
                mSheetMoveHorizontaly = true;
                // the sheet will be hide by pushing it after the width of the contentView
                mSheetHiddenBeforeOrigin = false;
                break;
            }
            case POSITION_WEST:
            {
                mSheetMoveHorizontaly = true;
                // the sheet will be hide by pushing it before the width of the contentView
                mSheetHiddenBeforeOrigin = true;
                break;
            }
            case POSITION_NORTH:
            {
                mSheetMoveHorizontaly = false;
                // the sheet will be hide by pushing it before the height of the contentView
                mSheetHiddenBeforeOrigin = true;
                break;
            }
            case POSITION_SOUTH:
            {
                mSheetMoveHorizontaly = false;
                // the sheet will be hide by pushing it after the height of the contentView
                mSheetHiddenBeforeOrigin = false;
                break;
            }
        }
    }
    
    /**
     * Gets the current starting position of the sheet.
     *
     * @return One of {@link #POSITION_NORTH}, {@link #POSITION_SOUTH}, {@link #POSITION_EAST} or
     *              {@link #POSITION_WEST}.
     */
    @SheetBehavior.Position
    public final int getPosition()
    {
        Log.d(getLogTag(),"getPosition ");
        return mPosition;
    }
    
    
    void setStateInternal(@SheetBehavior.State int state)
    {
        Log.d(getLogTag(),"setStateInternal from state [" + getStateString(mState) + "] => [" + getStateString(state) + "]");
        if (mState == state)
        {
            return;
        }
        mState = state;
        View sheet = mViewRef.get();
        if (sheet != null && mCallback != null)
        {
            mCallback.onSheetStateChanged(sheet, state, mIdentifierName);
        }
    }
    
    protected static class SavedState extends AbsSavedState
    {
        @SheetBehavior.State
        final int state;
    
        @SheetBehavior.Position
        final int position;
        
        final String identifierName;
        
        
        
        public SavedState(Parcel source)
        {
            this(source, null);
        }
        
        public SavedState(Parcel source, ClassLoader loader)
        {
            super(source, loader);
            //noinspection ResourceType
            state = source.readInt();
            position = source.readInt();
            identifierName = source.readString();
        }
        
        public SavedState(Parcelable superState, @SheetBehavior.State int state, @SheetBehavior.Position int position, String identifierName)
        {
            super(superState);
            this.state = state;
            this.position = position;
            this.identifierName = identifierName;
        }
        
        @Override
        public void writeToParcel(Parcel out, int flags)
        {
            super.writeToParcel(out, flags);
            out.writeInt(state);
            out.writeInt(position);
            out.writeString(identifierName);
        }
        
        public static final Creator<SheetBehavior.SavedState> CREATOR = new ClassLoaderCreator<SheetBehavior.SavedState>()
        {
            @Override
            public SheetBehavior.SavedState createFromParcel(Parcel in, ClassLoader loader)
            {
                return new SheetBehavior.SavedState(in, loader);
            }
            
            @Override
            public SheetBehavior.SavedState createFromParcel(Parcel in)
            {
                return new SheetBehavior.SavedState(in, null);
            }
            
            @Override
            public SheetBehavior.SavedState[] newArray(int size)
            {
                return new SheetBehavior.SavedState[size];
            }
        };
    }
    
    private void startSettlingAnimation(V child, final @SheetBehavior.State int state)
    {
        int top = child.getTop();
        int left = child.getLeft();
    
        if(mSheetMoveHorizontaly)
        {
            // ce que l'on fait varier ici donc c'est left ...
            if (state == STATE_COLLAPSED)
            {
                left = mOffsetCollapsed;
            }
            else if (state == STATE_EXPANDED)
            {
                left = mOffsetExpanded;
            }
            else if (mHideable && state == STATE_HIDDEN)
            {
                left = mOffsetHidden;
            }
            else
            {
                throw new IllegalArgumentException("Illegal state argument: " + state);
            }
        }
        else
        {
            // ce que l'on fait varier ici donc c'est top ...
    
            if (state == STATE_COLLAPSED)
            {
                top = mOffsetCollapsed;
            }
            else if (state == STATE_EXPANDED)
            {
                top = mOffsetExpanded;
            }
            else if (mHideable && state == STATE_HIDDEN)
            {
                top = mOffsetHidden;
            }
            else
            {
                throw new IllegalArgumentException("Illegal state argument: " + state);
            }
        }
    
        if (mViewDragHelper.smoothSlideViewTo(child, left, top))
        {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SheetBehavior.SettleRunnable(child, state));
        }
        else
        {
            setStateInternal(state);
        }
    }
    
    @VisibleForTesting
    int getPeekSizeMin() {
        Log.d(getLogTag(),"getPeekSizeMin => [" + mPeekSizeMin + "]");
        return mPeekSizeMin;
    }
    
    private class SettleRunnable implements Runnable {
        
        private final View mView;
        
        @SheetBehavior.State
        private final int mTargetState;
        
        SettleRunnable(View view, @SheetBehavior.State int targetState)
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
    
    
    void dispatchOnSlide(int left, int top)
    {
        View sheet = mViewRef.get();
        // TODO : update code here
        if (sheet != null && mCallback != null)
        {
            if (top > mOffsetCollapsed)
            {
                mCallback.onSheetSlide(sheet,(float) (mOffsetCollapsed - top) / (mParentHeight - mOffsetCollapsed),mIdentifierName);
            }
            else
            {
                mCallback.onSheetSlide(sheet,(float) (mOffsetCollapsed - top) / (mOffsetCollapsed - mOffsetExpanded),mIdentifierName);
            }
        }
    }
    
    private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback()
    {
        
        /**
         * Called when the user's input indicates that they want to capture the given child view
         * with the pointer indicated by pointerId. The callback should return true if the user
         * is permitted to drag the given view with the indicated pointer.
         * <p>
         * <p>ViewDragHelper may call this method multiple times for the same view even if
         * the view is already captured; this indicates that a new pointer is trying to take
         * control of the view.</p>
         * <p>
         * <p>If this method returns true, a call to {@link #onViewCaptured(View, int)}
         * will follow if the capture is successful.</p>
         *
         * @param child     Child the user is attempting to capture
         * @param pointerId ID of the pointer attempting the capture
         * @return true if capture should be allowed, false otherwise
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId)
        {
            // si le sheet est déjà en train de dragger, on ne permet pas la capture.
            if (mState == STATE_DRAGGING)
            {
                return false;
            }
            
            // si un scroll est en cour sur la scroll view enfant, on ne permet pas la capture.
            if (mTouchingScrollingChild)
            {
                return false;
            }
            
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId)
            {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null )
                {
                    // ok on a une vue enfant qui peut scroller ...
                    // on regarde dans quel sens elle peut scroller en fonction de notre type de sheet
                    
                    if(mSheetMoveHorizontaly)
                    {
                        // j'ai un sheet qui peut bouger horizontalement
                        // comme je suis en état EXPANDED, je ne vais capturer que le drag qui permet de "rentrer/fermer" le sheet ...
                        // sinon je n'interviens pas ... et je laisse les vues enfants prendre éventuellement le drag
                        //
                        // Donc si j'ai un sheet qui "arrive de gauche" donc mSheetHiddenBeforeOrigin == true
                        // et que la vue peut scroller horizontallement vers la droite (SCROLL_RIGHT)
                        // je n'inteviens pas ...
                        if (scroll.canScrollHorizontally(SCROLL_RIGHT) && mSheetHiddenBeforeOrigin == true)
                        {
                            // on laisse le contenue scroller vers la droite
                            return false;
                        }
                        // si j'ai un sheet qui "arrive de droite" donc mSheetHiddenBeforeOrigin == false
                        // et que la vue peut scroller horizontallement vers la gauche (SCROLL_LEFT)
                        // je n'inteviens pas ...
                        else if (scroll.canScrollHorizontally(SCROLL_LEFT) && mSheetHiddenBeforeOrigin == false)
                        {
                            // on laisse le contenue scroller vers la gauche
                            return false;
                        }
                    }
                    else
                    {
                        // j'ai un sheet qui bouge verticalement
                        // comme je suis en état EXPANDED, je ne vais capturer que le drag qui permet de "rentrer/fermer" le sheet ...
                        // sinon je n'interviens pas ... et je laisse les vues enfants prendre éventuellement le drag
                        //
                        // Donc si j'ai un sheet qui "arrive du bas" donc mSheetHiddenBeforeOrigin == false
                        // et que la vue enfant peut scroller vers le haut (SCROLL_UP) je n'interviens pas
                        if (scroll.canScrollVertically(SCROLL_UP) && mSheetHiddenBeforeOrigin == false)
                        {
                            // on laisse le contenue scroller vers le haut
                            return false;
                        }
                        // sinon si la vue enfant peut scroller vers le bas (SCROLL_DOWN) et que le sheet "vient du haut" ( mSheetHiddenBeforeOrigin == true )
                        // je n'interviens pas ...
                        else if (scroll.canScrollVertically(SCROLL_DOWN) && mSheetHiddenBeforeOrigin == true)
                        {
                            // on laisse le contenue scroller vers le bas
                            return false;
                        }
                    }
                }
                else
                {
                    Log.w(getLogTag(),"tryCaptureView STATE_EXPANDED scroll view is null.");
                }
            }
            
            // finalement si la weak reference dont on dispose est égale à la vue passée en paramètre, alors on capture ...
            boolean captured =  mViewRef != null && mViewRef.get() == child;
            return captured;
        }
    
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId)
        {
            Log.d(getLogTag(),"onViewCaptured child [" + capturedChild + "] activePointerId [" + activePointerId + "]");
        }
        
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy)
        {
            dispatchOnSlide(left, top);
        }
        
        @Override
        public void onViewDragStateChanged(int state)
        {
            // la seule chose qui nous intéresse ici , c'est de noter l'état STATE_DRAGGING
            if (state == ViewDragHelper.STATE_DRAGGING)
            {
                setStateInternal(STATE_DRAGGING);
            }
        }
        
        private void onViewReleasedWithHorizontaleGesture(View releasedChild, float xVelocity, float yVelocity)
        {
    
            // le but de la methode est comme le documente le ViewDragHelper d'appeler une des 2 méthodes suivantes du ViewDragHelper:
            // - settleCaptureViewAt()
            // - flingCaturedView()
            // Pour notre part, nous ne cherchons a appeler que settleCaptureViewAt()
            // Notre but est de déterminer donc le topX et topY que nous allons passer à settleCaptureView().
            //
            // Comme le sheet bouge verticalement nous allons donc déterminer le topY en fonction des différents états du sheet :
            //
            // - COLLAPSED
            // - HIDDEN
            // - EXPANDED
            // - etc
            //
            // Le topY parcontre dans ce cas ne bouge pas : c'est le Y actuel de la vue capturée : releasedChild.getTop().
    
            int topX ;
            int topY = releasedChild.getTop();
            @SheetBehavior.State int targetState;
            if (xVelocity > 0)
            {
                // C'est un déplacement vers la droite
                if(mSheetHiddenBeforeOrigin)
                {
                    // si c'est un sheet qui vient de gauche
                    // cela signifie que l'utilisateur veut "sortir" le sheet => STATE_EXPANDED
                    topX = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else
                {
                    // si c'est un sheet qui vient de droite
                    // cela signifie que l'utilisateur veut faire "rentrer" le sheet => STATE_COLLAPSED
                    topX = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
            else if (mHideable && shouldHide(releasedChild, xVelocity,yVelocity))
            {
                topX = mOffsetHidden;
                targetState = STATE_HIDDEN;
            }
            else if (xVelocity == 0.f)
            {
                // velocity null : l'utilisateur a arreter son geste à la présente position de la vue enfant : il veut la laisser à cette endroit !
                // Nous allons donc regarder la position et en fonction de la position déterminer si le sheet
                // doit finir à la position STATE_EXPANDED ou STATE_COLLAPSED
        
                int currentLeft = releasedChild.getLeft();
                if (Math.abs(currentLeft - mOffsetExpanded) < Math.abs(currentLeft - mOffsetCollapsed))
                {
                    topX = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else
                {
                    topX = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
            else
            {
                // velocity négative
                // C'est un déplacement vers la gauche
                if(mSheetHiddenBeforeOrigin)
                {
                    // si c'est un sheet qui vient de gauche
                    // cela signifie que l'utilisateur veut faire "rentrer" le sheet => STATE_COLLAPSED
            
                    topX = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
                else
                {
                    // si c'est un sheet qui vient de droite
                    // cela signifie que l'utilisateur veut faire "sortir" le sheet => STATE_EXPANDED
                    topX = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
            }
    
            if (mViewDragHelper.settleCapturedViewAt(topX, topY))
            {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SheetBehavior.SettleRunnable(releasedChild, targetState));
            }
            else
            {
                setStateInternal(targetState);
            }
        }
    
        private void onViewReleasedWithVerticaleGesture(View releasedChild, float xVelocity, float yVelocity)
        {
            
            // le but de la methode est comme le documente le ViewDragHelper d'appeler une des 2 méthodes suivantes du ViewDragHelper:
            // - settleCaptureViewAt()
            // - flingCaturedView()
            // Pour notre part, nous ne cherchons a appeler que settleCaptureViewAt()
            // Notre but est de déterminer donc le topX et topY que nous allons passer à settleCaptureView().
            //
            // Comme le sheet bouge verticalement nous allons donc déterminer le topY en fonction des différents états du sheet :
            //
            // - COLLAPSED
            // - HIDDEN
            // - EXPANDED
            // - etc
            //
            // Le topX parcontre dans ce cas ne bouge pas : c'est le X actuel de la vue capturée: releasedChild.getLeft().
    
            int topX = releasedChild.getLeft();
            int topY;
            @SheetBehavior.State int targetState;
            if (yVelocity > 0)
            {
                // C'est un déplacement vers le haut
                if(mSheetHiddenBeforeOrigin)
                {
                    // si c'est un sheet qui vient du haut
                    // cela signifie que l'utilisateur veut "rentrer" le sheet => STATE_COLLAPSED
                    topY = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
                else
                {
                    // si c'est un sheet qui vient du bas
                    // cela signifie que l'utilisateur veut faire apparaitre le sheet => STATE_EXPANDED
                    topY = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
            }
            else if (mHideable && shouldHide(releasedChild, xVelocity, yVelocity))
            {
                topY = mOffsetHidden;
                targetState = STATE_HIDDEN;
            }
            else if (yVelocity == 0.f)
            {
                // velocity null : l'utilisateur a arreter son geste à la présente position de la vue enfant : il veut la laisser à cette endroit !
                // Nous allons donc regarder la position et en fonction de la position déterminer si le sheet
                // doit finir à la position STATE_EXPANDED ou STATE_COLLAPSED
                
                int currentTop = releasedChild.getTop();
                if (Math.abs(currentTop - mOffsetExpanded) < Math.abs(currentTop - mOffsetCollapsed))
                {
                    topY = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else
                {
                    topY = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
            else
            {
                // velocity négative
                
                if(mSheetHiddenBeforeOrigin)
                {
                    // si c'est un sheet qui vient du haut
                    // cela signifie que l'utilisateur veut faire apparaitre le sheet => STATE_EXPANDED
    
                    topY = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else
                {
                    // si c'est un sheet qui vient du bas
                    // cela signifie que l'utilisateur veut faire "rentrer" le sheet => STATE_COLLAPSED
                    topY = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
    
            if (mViewDragHelper.settleCapturedViewAt(topX, topY))
            {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SheetBehavior.SettleRunnable(releasedChild, targetState));
            }
            else
            {
                setStateInternal(targetState);
            }
        }
    
    
        @Override
        public void onViewReleased(View releasedChild, float xVelocity, float yVelocity)
        {
            // on vient de relacher une vue ...
            if(mSheetMoveHorizontaly)
            {
                // et notre sheet bouge horizontalement
                // ce qui nous intéresse donc c'est la velocité horizontale (xVelocity)
                onViewReleasedWithHorizontaleGesture(releasedChild, xVelocity, yVelocity);
                
            }
            else
            {
    
                // et notre sheet bouge verticalement
                // ce qui nous intéresse donc c'est la velocité verticale (yVelocity)
                onViewReleasedWithVerticaleGesture(releasedChild, xVelocity, yVelocity);
            }
        }
        
        @Override
        public int clampViewPositionVertical(View child, int top, int dy)
        {
            int clampValue = 0;
            if(!mSheetMoveHorizontaly)
            {
                if(!mSheetHiddenBeforeOrigin)
                {
                    clampValue = MathUtils.clamp(top + dy, mHideable ? mOffsetHidden : mOffsetCollapsed, mOffsetExpanded);
                }
                else
                {
                    clampValue = MathUtils.clamp(top + dy, mOffsetExpanded, mHideable ? mOffsetHidden : mOffsetCollapsed );
                }
            }
            else
            {
                clampValue = child.getTop();
            }
    
            return clampValue;
        }
        
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx)
        {
            int clampValue = 0;
            if(!mSheetMoveHorizontaly)
            {
                clampValue = child.getLeft();
            }
            else
            {
                if(!mSheetHiddenBeforeOrigin)
                {
                    clampValue = MathUtils.clamp(left + dx, mOffsetExpanded, mHideable ? mOffsetHidden : mOffsetCollapsed);
                }
                else
                {
                    clampValue = MathUtils.clamp(left + dx, mHideable ? mOffsetHidden : mOffsetCollapsed, mOffsetExpanded );
                }
            }
    
            return clampValue;
        }
        
        @Override
        public int getViewVerticalDragRange(View child)
        {
            int dragRange = 0;
            if(mSheetMoveHorizontaly)
            {
                // si la vue bouge horizontalement, on retourne 0 : elle ne peut pas être draggée.
                dragRange = 0;
            }
            else
            {
                if (mHideable)
                {
                    dragRange = mParentHeight - mOffsetExpanded;
                }
                else
                {
                    dragRange = mOffsetCollapsed - mOffsetExpanded;
                }
            }
    
            return dragRange;
        }
    
        @Override
        public int getViewHorizontalDragRange(View child)
        {
            int dragRange = 0;
            if(!mSheetMoveHorizontaly)
            {
                // si la vue bouge verticalement, on retourne 0 : elle ne peut pas être draggée.
                dragRange = 0;
            }
            else
            {
                if (mHideable)
                {
                    dragRange = mParentWidth - mOffsetExpanded;
                }
                else
                {
                    dragRange = mOffsetCollapsed - mOffsetExpanded;
                }
            }
    
            return dragRange;
        }
    };
    
    
    private float getXVelocity()
    {
        float xVelocity = 0;
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
        return xVelocity;
    }
    
    private float getYVelocity()
    {
        float yVelocity = 0;
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        yVelocity = mVelocityTracker.getYVelocity(mActivePointerId);
    
        return yVelocity;
    }
    
    private boolean shouldHide(View child, float xvel, float yvel)
    {
        if (mSkipCollapsed)
        {
            return true;
        }
        
        if(!mSheetMoveHorizontaly)
        {
            // si le point haut de la vue (top) est encore supérieur à mOffsetCollapsed et que
            // la vue doit passer par l'état collapsed alors on ne cache pas la vue.
            if (!mSheetHiddenBeforeOrigin)
            {
                if(child.getTop() > mOffsetCollapsed)
                {
                    // It should not hide, but collapse.
                    return false;
                }
    
                final float newTop = child.getTop() + yvel * HIDE_FRICTION;
    
                float threshold = Math.abs(newTop - mOffsetCollapsed) / (float) mPeekSize;
                return threshold > HIDE_THRESHOLD;
            }
            else
            {
                if(child.getTop() < mOffsetCollapsed)
                {
                    // It should not hide, but collapse.
                    return false;
                }
    
                final float newTop = child.getTop() + yvel * HIDE_FRICTION;
    
                float threshold = Math.abs(newTop - mOffsetCollapsed) / (float) mPeekSize;
                return threshold > HIDE_THRESHOLD;
            }
        }
        else
        {
            // si le point haut de la vue (top) est encore supérieur à mOffsetCollapsed et que
            // la vue doit passer par l'état collapsed alors on ne cache pas la vue.
            if (!mSheetHiddenBeforeOrigin)
            {
                if(child.getLeft() > mOffsetCollapsed)
                {
                    // It should not hide, but collapse.
                    return false;
                }
        
                final float newLeft = child.getLeft() + xvel * HIDE_FRICTION;
        
                float threshold = Math.abs(newLeft - mOffsetCollapsed) / (float) mPeekSize;
                return threshold > HIDE_THRESHOLD;
            }
            else
            {
                if(child.getLeft() < mOffsetCollapsed)
                {
                    // It should not hide, but collapse.
                    return false;
                }
    
                final float newLeft = child.getLeft() + xvel * HIDE_FRICTION;
    
                float threshold = Math.abs(newLeft - mOffsetCollapsed) / (float) mPeekSize;
                return threshold > HIDE_THRESHOLD;
            }
        }
    }
    
    
    /**
     * A utility function to get the {@link SheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link SheetBehavior}.
     * @return The {@link SheetBehavior} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> SheetBehavior<V> from(V view)
    {
        Log.d("SheetBehavior","from viewId [" + view.getId() + "]");
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams))
        {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
        if (!(behavior instanceof SheetBehavior))
        {
            throw new IllegalArgumentException("The view is not associated with SheetBehavior");
        }
        return (SheetBehavior<V>) behavior;
    }
    
    
    private void reset()
    {
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    
    @VisibleForTesting
    View findScrollingChild(View view)
    {
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
    
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection)
    {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child))
        {
            ViewCompat.setFitsSystemWindows(child, true);
        }
        
        int savedTop = child.getTop();
        int savedLeft = child.getLeft();
        
        // Pour commencer on laisse la vue parent faire le layout ...
        parent.onLayoutChild(child, layoutDirection);
        
        // on détermine la taille de la partie collapsed en fonction de la valeur de peekSize.
        // Cette méthode doit être appelée avant computeRemarquableOffsets() car
        // computeRemarquableOffsets() dépend de la valeur de PeekSize.
        int peekSize = this.getComputePeekSize(parent);
        
        // on recalcul tous les offsets ...
        // ces derniers ne pourront être considérés fiables qu'après l'opération
        // la méthode suivante ne peut et ne doit être appelée que lorsque les layout de la vue parent et enfant
        // sont effectifs.
        this.computeRemarquableOffsets(parent,child,layoutDirection, peekSize);
        
        if(mSheetMoveHorizontaly)
        {
            if (mState == STATE_EXPANDED)
            {
                ViewCompat.offsetLeftAndRight(child, mOffsetExpanded);
            }
            else if (mHideable && mState == STATE_HIDDEN)
            {
                ViewCompat.offsetLeftAndRight(child, mOffsetHidden);
            }
            else if (mState == STATE_COLLAPSED)
            {
                ViewCompat.offsetLeftAndRight(child, mOffsetCollapsed);
            }
            else if (mState == STATE_DRAGGING || mState == STATE_SETTLING)
            {
                int moveOffset = 0;
                if (mSheetMoveHorizontaly)
                {
                    if (mSheetHiddenBeforeOrigin)
                    {
                        moveOffset = savedLeft + child.getLeft();
                    }
                    else
                    {
                        moveOffset = savedLeft - child.getLeft();
                    }
                }
                else
                {
                    if (mSheetHiddenBeforeOrigin)
                    {
                        moveOffset = savedTop + child.getTop();
                    }
                    else
                    {
                        moveOffset = savedTop - child.getTop();
                    }
                }
                ViewCompat.offsetLeftAndRight(child, moveOffset);
            }
        }
        else
        {
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
                int moveOffset = 0;
                if (mSheetMoveHorizontaly)
                {
                    if (mSheetHiddenBeforeOrigin)
                    {
                        moveOffset = savedLeft + child.getLeft();
                    }
                    else
                    {
                        moveOffset = savedLeft - child.getLeft();
                    }
                }
                else
                {
                    if (mSheetHiddenBeforeOrigin)
                    {
                        moveOffset = savedTop + child.getTop();
                    }
                    else
                    {
                        moveOffset = savedTop - child.getTop();
                    }
                }
                ViewCompat.offsetTopAndBottom(child, moveOffset);
            }
        }
        if (mViewDragHelper == null)
        {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }
    
    
    private void computeRemarquableOffsets(CoordinatorLayout parent, V child, int layoutDirection, int peekSize)
    {
        Log.d(getLogTag(),"computeRemarquableOffsets peekSize [" + peekSize + "]");
        // ici on met à jour trois offsets :
        //
        // - mOffsetHidden
        // - mOffsetCollapsed
        // - mOffsetEpanded
        //
        // Ces offset sont toujours exprimés en unités de coordonnées parent.
        // Le point d'origine de la vue parent est toujours top = 0, left = 0 (O sur le schéma ci dessous)
        //
        // Tous les offsets sont exprimés par rapport à ce point. En effet, toutes les méthodes standards
        // Android déplacent une vue par rapport à ce point d'origine (top,left) y compris quand le mode layoutDirection change.
        //
        // C'est le point matérialisé par la lettre "O" dans le shéma ci dessous.
        // Les points notés "H" sont les points d'orgines des sheets lorsqu'ils (les sheets) sont "Hidden"
        // Un sheet à l'état EXPANDED a son point "H" qui se supperpose avec "O".
        //
        //
        //                  H================+
        //                  |                |
        //                  |   HIDDEN       |
        //                  |   A            |
        //                  |   |            |
        //                  |   |   Sheet    |
        //                  |   |   NORTH    |
        //                  |   V            |
        //                  |   COLLAPSED    |
        //                  |   EXPANDED     |
        //                  |                |
        // H----------------O================H-----------------+
        // |                |                |                 |
        // | COLLAPSED ---> |                | <--- COLLAPSED  |
        // | EXPANDED       |                |      EXPANDED   |
        // |                |                |                 |
        // |     Sheet      |                |      Sheet      |
        // |     WEST       |                |      EAST       |
        // |                |    Parent      |                 |
        // |                |                |                 |
        // |                |                |                 |
        // |                |                |                 |
        // | <-- HIDDEN     |                |   HIDDEN -->    |
        // |                |                |                 |
        // +----------------H================+-----------------+
        //                  |                |
        //                  |  COLLAPSED     |
        //                  |  EXPANDED      |
        //                  |   A            |
        //                  |   |            |
        //                  |   |  Sheet     |
        //                  |   |  SOUTH     |
        //                  |   |            |
        //                  |   V            |
        //                  |  HIDDEN        |
        //                  |                |
        //                  +================+
        //
        // Les variables importantes :
        // - mSheetHiddenBeforeOrigin : est une propriété d'un sheet qui est "vraie" (true) si l'origine du sheet (son "top,left") quand il
        //   est caché (Hidden) , est placé avant l'origine (top,left) du parent : c'est le cas pour un sheet en position NORTH et WEST.
        //   La propriété est fausse (false) si l'origine du sheet est placée "après" l'origine du parent : c'est le cas des sheet en position EAST
        //   SOUTH.
        // - mSheetMoveHorizontaly : est une propriété d'un sheet qui est "vraie" (true) si l'origine du sheet (le sheet lui même) se déplace
        //   horizontalement. Elle est fausse si l'origine du sheet se déplace verticalement. Elle est donc vraie pour "WEST" et "EAST", fausse pour
        //   NORTH et SOUTH.
        //
        // Pour un sheet WEST, les expressions suivantes sont vérifiées :
        // - mOffsetHidden < mOffsetCollapsed < mOffsetExpanded ( mOffestExpanded == 0 )
        // - mSheetMoveHorizontaly == true
        // - mSheetHiddenBeforeOrigin == true
        //
        // Pour un sheet EAST, les expressions suivantes sont vérifiées :
        // - mOffsetExpanded (== 0) < mOffsetCollapsed < mOffsetHidden
        // - mSheetMoveHorizontaly == true
        // - mSheetHiddenBeforeOrigin == false
        //
        // Pour un sheet NORTH, les expressions suivantes sont vérifiées :
        // - mOffsetHidden < mOffsetCollapsed < mOffsetExpanded ( mOffestExpanded == 0 )
        // - mSheetMoveHorizontaly == false
        // - mSheetHiddenBeforeOrigin == true
        //
        // Pour un sheet SOUTH, les expressions suivantes sont vérifiées :
        // - mOffsetExpanded (== 0) < mOffsetCollapsed < mOffsetHidden
        // - mSheetMoveHorizontaly == false
        // - mSheetHiddenBeforeOrigin == false
        
        if(mSheetMoveHorizontaly)
        {
            // WEST and EAST
            if(mSheetHiddenBeforeOrigin)
            {
                // WEST
                mOffsetHidden = parent.getLeft() - parent.getWidth();
                mOffsetCollapsed = mOffsetHidden + Math.abs(peekSize);
                mOffsetExpanded = parent.getLeft();
            }
            else
            {
                // EAST
                mOffsetHidden = parent.getLeft() + parent.getWidth();
                mOffsetCollapsed = mOffsetHidden - Math.abs(peekSize) ;
                mOffsetExpanded = parent.getLeft();
            }
        }
        else
        {
            // NORTH and SOUTH
            if(mSheetHiddenBeforeOrigin)
            {
                // NORTH
                mOffsetHidden = parent.getTop() - parent.getHeight();
                mOffsetCollapsed = mOffsetHidden + Math.abs(peekSize) ;
                mOffsetExpanded = parent.getTop();
            }
            else
            {
                // SOUTH
                mOffsetHidden = parent.getTop() + parent.getHeight();;
                mOffsetCollapsed = mOffsetHidden - Math.abs(peekSize) ;
                mOffsetExpanded = parent.getTop();
            }
        }
    
        Log.d(getLogTag(),"computeRemarquableOffsets mOffsetHidden .....[" + mOffsetHidden + "]");
        Log.d(getLogTag(),"computeRemarquableOffsets mOffsetCollapsed ..[" + mOffsetCollapsed + "]");
        Log.d(getLogTag(),"computeRemarquableOffsets mOffsetExpanded ...[" + mOffsetExpanded + "]");
    }
    
    private int getComputePeekSize(CoordinatorLayout parent)
    {
        mParentHeight = parent.getHeight();
        mParentWidth = parent.getWidth();
        
        
        int peekSize;
        if (mPeekSizeAuto)
        {
            Log.d(getLogTag(),"getComputePeekSize peekSize set in AUTO mode.");
            if (mPeekSizeMin == 0)
            {
                mPeekSizeMin = parent.getResources().getDimensionPixelSize(R.dimen.design_sheet_peek_size_min);
                Log.d(getLogTag(),"getComputePeekSize mPeekSizeMin read from default [" + mPeekSizeMin + "]");
            }
            
            // on met peekSize à 1/3 de
            if(mSheetMoveHorizontaly)
            {
                peekSize = Math.max( mPeekSizeMin, Math.abs( mParentWidth * 1 / 4 ) );
            }
            else
            {
                peekSize = Math.max( mPeekSizeMin, Math.abs( mParentHeight * 1 / 4 ) );
            }
            Log.d(getLogTag(),"getComputePeekSize compute peekSize [" + peekSize + "]");
        }
        else
        {
            peekSize = mPeekSize;
            Log.d(getLogTag(),"getComputePeekSize peekSize from configuration [" + peekSize + "]");
        }
    
        Log.d(getLogTag(),"getComputePeekSize peekSize ...[" + peekSize + "]");
        
        return peekSize;
    }
    
    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        boolean intercepted = false;
    
        View scroll = mNestedScrollingChildRef != null ? mNestedScrollingChildRef.get() : null;
    
        if (!child.isShown())
        {
            mIgnoreEvents = true;
            intercepted = false;
            return intercepted;
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
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mInitialX = (int) event.getX();
                mInitialY = (int) event.getY();
                if (scroll != null && parent.isPointInChildBounds(scroll, mInitialX, mInitialY))
                {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, mInitialX, mInitialY);
                break;
        }
    
        boolean shouldInterceptTouchEvent = mViewDragHelper.shouldInterceptTouchEvent(event);
        if (!mIgnoreEvents && shouldInterceptTouchEvent)
        {
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        if(!mSheetMoveHorizontaly)
        {
            intercepted = action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) && Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
        }
        else
        {
            intercepted = action == MotionEvent.ACTION_MOVE && scroll != null && !mIgnoreEvents && mState != STATE_DRAGGING && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) && Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop();
        }
        return intercepted;
    }
    
    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event)
    {
        if (!child.isShown())
        {
            return false;
        }
        int action = event.getActionMasked();
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN)
        {
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
            if(!mSheetMoveHorizontaly)
            {
                if(!mSheetHiddenBeforeOrigin)
                {
                    if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop())
                    {
                        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
                    }
                }
                else
                {
                    if (Math.abs(event.getY() - mInitialY) > mViewDragHelper.getTouchSlop())
                    {
                        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
                    }
                }
            }
            else
            {
                if(!mSheetHiddenBeforeOrigin)
                {
                    if (Math.abs(mInitialX - event.getX()) > mViewDragHelper.getTouchSlop())
                    {
                        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
                    }
                }
                else
                {
                    if (Math.abs( event.getX() - mInitialX) > mViewDragHelper.getTouchSlop())
                    {
                        mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
                    }
                }
            }
        }
        return !mIgnoreEvents;
    }
    
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes)
    {
        // on autorise les nestedScroll dans toutes les directions... quelques soit le sheet ... si la vue supporte les nested scroll ...
        boolean nestedScroll = false;
        if(!mSheetMoveHorizontaly)
        {
            mLastNestedScrollDy = 0;
            mNestedScrolled = false;
            nestedScroll = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 || (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
        }
        else
        {
            mLastNestedScrollDx = 0;
            mNestedScrolled = false;
            nestedScroll = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 || (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
        }
        return nestedScroll;
    }
    
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed)
    {
        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild)
        {
            return;
        }
        int currentTop = child.getTop();
        int currentLeft = child.getLeft();
        int newTop = currentTop - dy;
        int newLeft = currentLeft - dx;
        
        if(!mSheetMoveHorizontaly)
        {
            if(!mSheetHiddenBeforeOrigin)
            {
                if (dy > 0)
                {
                    // Upward
                    // Si on tente de dépasser mOffsetExpanded ...
                    // on déplace le sheet pour qu'il arrive à mOffsetExpanded...
                    // on clamp le sheet à cette valeur.
                    if (newTop < mOffsetExpanded)
                    {
                        consumed[1] = currentTop - mOffsetExpanded;
                        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                        setStateInternal(STATE_EXPANDED);
                    }
                    else
                    {
                        consumed[1] = dy;
                        ViewCompat.offsetTopAndBottom(child, -dy);
                        setStateInternal(STATE_DRAGGING);
                    }
                }
                else if (dy < 0)
                {
                    // Downward
                    if (!target.canScrollVertically(-1))
                    {
                        if (newTop <= mOffsetCollapsed || mHideable)
                        {
                            consumed[1] = dy;
                            ViewCompat.offsetTopAndBottom(child, -dy);
                            setStateInternal(STATE_DRAGGING);
                        }
                        else
                        {
                            consumed[1] = currentTop - mOffsetCollapsed;
                            ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                            setStateInternal(STATE_COLLAPSED);
                        }
                    }
                    else
                    {
                        Log.e(getLogTag(), "onNestedPreScroll DOWNWARD moveOffset IGNORED");
                    }
                }
            }
            else
            {
                if (dy < 0)
                {
                    // Downward
                    
                    if (newTop > mOffsetExpanded)
                    {
                        consumed[1] = currentTop - mOffsetExpanded;
                        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                        setStateInternal(STATE_EXPANDED);
                    }
                    else
                    {
                        consumed[1] = dy;
                        ViewCompat.offsetTopAndBottom(child, -dy);
                        setStateInternal(STATE_DRAGGING);
                    }
                }
                else if (dy > 0)
                {
                    // Upward
                    if (!target.canScrollVertically(1))
                    {
            
                        if (newTop <= mOffsetCollapsed || mHideable)
                        {
                            consumed[1] = dy;
                            ViewCompat.offsetTopAndBottom(child, -dy);
                            setStateInternal(STATE_DRAGGING);
                        }
                        else
                        {
                            consumed[1] = currentTop - mOffsetCollapsed;
                            ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                            setStateInternal(STATE_COLLAPSED);
                        }
                    }
                    else
                    {
                        Log.e(getLogTag(), "onNestedPreScroll DOWNWARD moveOffset IGNORED");
                    }
                }
            }
            mLastNestedScrollDy = dy;
        }
        else
        {
            if(!mSheetHiddenBeforeOrigin)
            {
                if (dx > 0)
                {
                    //
                    // Si on tente de dépasser mOffsetExpanded ...
                    // on déplace le sheet pour qu'il arrive à mOffsetExpanded...
                    // on clamp le sheet à cette valeur.
                    if (newLeft < mOffsetExpanded)
                    {
                        consumed[0] = currentLeft - mOffsetExpanded;
                        ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                        setStateInternal(STATE_EXPANDED);
                    }
                    else
                    {
                        consumed[0] = dx;
                        ViewCompat.offsetLeftAndRight(child, -dx);
                        setStateInternal(STATE_DRAGGING);
                    }
                }
                else if (dx < 0)
                {
                    // Downward
                    if (!target.canScrollHorizontally(-1))
                    {
                        if (newLeft <= mOffsetCollapsed || mHideable)
                        {
                            consumed[0] = dx;
                            ViewCompat.offsetLeftAndRight(child, -dx);
                            setStateInternal(STATE_DRAGGING);
                        }
                        else
                        {
                            consumed[0] = currentLeft - mOffsetCollapsed;
                            ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                            setStateInternal(STATE_COLLAPSED);
                        }
                    }
                    else
                    {
                        Log.e(getLogTag(), "onNestedPreScroll DOWNWARD moveOffset IGNORED");
                    }
                }
            }
            else
            {
                if (dx < 0)
                {
                    // Downward
            
                    if (newLeft > mOffsetExpanded)
                    {
                        consumed[0] = currentLeft - mOffsetExpanded;
                        ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                        setStateInternal(STATE_EXPANDED);
                    }
                    else
                    {
                        consumed[0] = dx;
                        ViewCompat.offsetLeftAndRight(child, -dx);
                        setStateInternal(STATE_DRAGGING);
                    }
                }
                else if (dx > 0)
                {
                    // Upward
                    if (!target.canScrollHorizontally(1))
                    {
                
                        if (newLeft <= mOffsetCollapsed || mHideable)
                        {
                            consumed[0] = dx;
                            ViewCompat.offsetLeftAndRight(child, -dx);
                            setStateInternal(STATE_DRAGGING);
                        }
                        else
                        {
                            consumed[0] = currentLeft - mOffsetCollapsed;
                            ViewCompat.offsetLeftAndRight(child, -consumed[0]);
                            setStateInternal(STATE_COLLAPSED);
                        }
                    }
                    else
                    {
                        Log.e(getLogTag(), "onNestedPreScroll DOWNWARD moveOffset IGNORED");
                    }
                }
            }
            mLastNestedScrollDx = dx;
        }
        
        dispatchOnSlide(child.getLeft(),child.getTop());
        mNestedScrolled = true;
    }
    
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target)
    {
        if (!mSheetMoveHorizontaly && child.getTop() == mOffsetExpanded || (mSheetMoveHorizontaly && child.getLeft() == mOffsetExpanded))
        {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        
        if (mNestedScrollingChildRef == null || target != mNestedScrollingChildRef.get() || !mNestedScrolled)
        {
            return;
        }
        
        int top = child.getTop();
        int left = child.getLeft();
        int targetState;
        
        if(!mSheetMoveHorizontaly)
        {
            if(!mSheetHiddenBeforeOrigin)
            {
                if (mLastNestedScrollDy > 0)
                {
                    top = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else if (mHideable && shouldHide(child, getXVelocity(), getYVelocity()))
                {
                    top = mOffsetHidden;
                    targetState = STATE_HIDDEN;
                }
                else if (mLastNestedScrollDy == 0)
                {
                    int currentTop = child.getTop();
                    if (Math.abs(currentTop - mOffsetExpanded) < Math.abs(currentTop - mOffsetCollapsed))
                    {
                        top = mOffsetExpanded;
                        targetState = STATE_EXPANDED;
                    }
                    else
                    {
                        top = mOffsetCollapsed;
                        targetState = STATE_COLLAPSED;
                    }
                }
                else
                {
                    top = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
            else
            {
                if (mLastNestedScrollDy < 0)
                {
                    top = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else if (mHideable && shouldHide(child, getXVelocity(), getYVelocity()))
                {
                    top = mOffsetHidden;
                    targetState = STATE_HIDDEN;
                }
                else if (mLastNestedScrollDy == 0)
                {
                    int currentTop = child.getTop();
                    if (Math.abs(currentTop - mOffsetExpanded) < Math.abs(currentTop - mOffsetCollapsed))
                    {
                        top = mOffsetExpanded;
                        targetState = STATE_EXPANDED;
                    }
                    else
                    {
                        top = mOffsetCollapsed;
                        targetState = STATE_COLLAPSED;
                    }
                }
                else
                {
                    top = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
        }
        else
        {
            if(!mSheetHiddenBeforeOrigin)
            {
                if (mLastNestedScrollDx > 0)
                {
                    left = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else if (mHideable && shouldHide(child, getXVelocity(), getYVelocity()))
                {
                    left = mOffsetHidden;
                    targetState = STATE_HIDDEN;
                }
                else if (mLastNestedScrollDx == 0)
                {
                    int currentLeft = child.getLeft();
                    if (Math.abs(currentLeft - mOffsetExpanded) < Math.abs(currentLeft - mOffsetCollapsed))
                    {
                        left = mOffsetExpanded;
                        targetState = STATE_EXPANDED;
                    }
                    else
                    {
                        left = mOffsetCollapsed;
                        targetState = STATE_COLLAPSED;
                    }
                }
                else
                {
                    left = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
            else
            {
                if (mLastNestedScrollDx < 0)
                {
                    left = mOffsetExpanded;
                    targetState = STATE_EXPANDED;
                }
                else if (mHideable && shouldHide(child, getXVelocity(), getYVelocity()))
                {
                    left = mOffsetHidden;
                    targetState = STATE_HIDDEN;
                }
                else if (mLastNestedScrollDx == 0)
                {
                    int currentLeft = child.getLeft();
                    if (Math.abs(currentLeft - mOffsetExpanded) < Math.abs(currentLeft - mOffsetCollapsed))
                    {
                        left = mOffsetExpanded;
                        targetState = STATE_EXPANDED;
                    }
                    else
                    {
                        left = mOffsetCollapsed;
                        targetState = STATE_COLLAPSED;
                    }
                }
                else
                {
                    left = mOffsetCollapsed;
                    targetState = STATE_COLLAPSED;
                }
            }
        }
        
        if (mViewDragHelper.smoothSlideViewTo(child, left, top))
        {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SheetBehavior.SettleRunnable(child, targetState));
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
        return target == mNestedScrollingChildRef.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }
}
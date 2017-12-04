package com.smobee.android.scrollsheetbehaviour;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.smobee.android.scrollsheetbehaviour.widget.SheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.SouthSheetBehavior;
import android.view.GestureDetector.OnGestureListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnGestureListener
{
    public static final String LOG_TAG = "Main";
    FrameLayout southSheet;
    FrameLayout westSheet;
    FrameLayout eastSheet;
    FrameLayout northSheet;
    
    private RecyclerView northRecyclerView;
    private RecyclerView.Adapter northAdapter;
    private RecyclerView.LayoutManager northLayoutManager;
    
    private RecyclerView southRecyclerView;
    private RecyclerView.Adapter southAdapter;
    private RecyclerView.LayoutManager southLayoutManager;
    
    private RecyclerView eastRecyclerView;
    private RecyclerView.Adapter eastAdapter;
    private RecyclerView.LayoutManager eastLayoutManager;
    
    private RecyclerView westRecyclerView;
    private RecyclerView.Adapter westAdapter;
    private RecyclerView.LayoutManager westLayoutManager;
    
    
    private CoordinatorLayout  coordinatorLayout;
    
    private SheetsManager sheetsManager = new SheetsManager();
    
    private GestureDetector gestureDetector;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        String[] values = new String[]{
                "Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Android", "iPhone", "WindowsMobile", "OpenStep", "NeXTStep", "HP-UX", "AIX", "Mach III", "BeOS", "Heroku"
        };
    
        final ArrayList<String> listEast = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
        {
            listEast.add(values[i]);
        }
    
        final ArrayList<String> listWest = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
        {
            listWest.add(values[i]);
        }
    
        final ArrayList<String> listNorth = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
        {
            listNorth.add(values[i]);
        }
    
        final ArrayList<String> listSouth = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
        {
            listSouth.add(values[i]);
        }
    
        this.southSheet = (FrameLayout) findViewById(R.id.south_sheet);
        this.westSheet = (FrameLayout) findViewById(R.id.west_sheet);
        this.eastSheet = (FrameLayout) findViewById(R.id.east_sheet);
        this.northSheet = (FrameLayout) findViewById(R.id.north_sheet);
    
        this.northRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_north);
        this.northRecyclerView.setHasFixedSize(true);
        this.northLayoutManager = new LinearLayoutManager(this);
        this.northRecyclerView.setLayoutManager(this.northLayoutManager);
        this.northAdapter = new NorthAdapter(listNorth);
        this.northRecyclerView.setAdapter(this.northAdapter);
    
        this.southRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_south);
        this.southRecyclerView.setHasFixedSize(true);
        this.southLayoutManager = new LinearLayoutManager(this);
        this.southRecyclerView.setLayoutManager(this.southLayoutManager);
        this.southAdapter = new SouthAdapter(listSouth);
        this.southRecyclerView.setAdapter(this.southAdapter);
    
        this.eastRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_east);
        this.eastRecyclerView.setHasFixedSize(true);
        this.eastLayoutManager = new LinearLayoutManager(this);
        this.eastRecyclerView.setLayoutManager(this.eastLayoutManager);
        this.eastAdapter = new EastAdapter(listSouth);
        this.eastRecyclerView.setAdapter(this.eastAdapter);
    
        this.westRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_west);
        this.westRecyclerView.setHasFixedSize(true);
        this.westLayoutManager = new LinearLayoutManager(this);
        this.westRecyclerView.setLayoutManager(this.westLayoutManager);
        this.westAdapter = new WestAdapter(listSouth);
        this.westRecyclerView.setAdapter(this.westAdapter);
    
    
        this.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    
        gestureDetector = new GestureDetector(this, this);
    
        // SOUTH
        SheetBehavior southSheetBehavior = SheetBehavior.from(southSheet);
        this.sheetsManager.setSouthSheetBehavior(southSheetBehavior);
    
        // NORTH
        SheetBehavior northSheetBehavior = SheetBehavior.from(northSheet);
        this.sheetsManager.setNorthSheetBehavior(northSheetBehavior);
    
        // WEST
        SheetBehavior westSheetBehavior = SheetBehavior.from(westSheet);
        this.sheetsManager.setWestSheetBehavior(westSheetBehavior);
    
        // EAST
        SheetBehavior eastSheetBehavior = SheetBehavior.from(eastSheet);
        this.sheetsManager.setEastSheetBehavior(eastSheetBehavior);
    
        this.sheetsManager.setup();
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    @Override
    public boolean onDown(MotionEvent e)
    {
        Log.w(LOG_TAG, "onDown event [" + e + "]");
        return false;
    }
    
    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    @Override
    public void onShowPress(MotionEvent e)
    {
        Log.w(LOG_TAG, "onShowPress event [" + e + "]");
    }
    
    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        Log.w(LOG_TAG, "onSingleTapUp event [" + e + "]");
        return false;
    }
    
    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1        The first down motion event that started the scrolling.
     * @param e2        The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last
     *                  call to onScroll. This is NOT the distance between {@code e1}
     *                  and {@code e2}.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        Log.w(LOG_TAG, "onScroll e1 [" + e1 + "] e2 [" + e2 + "] distanceX [" + distanceX + "] distanceY [" + distanceY + "]");
        return false;
    }
    
    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    @Override
    public void onLongPress(MotionEvent e)
    {
        Log.w(LOG_TAG, "onLongPress e [" + e + "]");
    }
    
    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1        The first down motion event that started the fling.
     * @param e2        The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second
     *                  along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second
     *                  along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        Log.w(LOG_TAG, "onFling e1 [" + e1 + "] e2 [" + e2 + "] velocityX [" + velocityX + "] velocityY [" + velocityY + "]");
    
        if(e1.getY() - e2.getY() > 50){
            return this.sheetsManager.handleSwipe(SheetsManager.SwipeDirection.SWIPE_UP);
        }
        else if(e2.getY() - e1.getY() > 50){
            return this.sheetsManager.handleSwipe(SheetsManager.SwipeDirection.SWIPE_DOWN);
        }
        else if(e1.getX() - e2.getX() > 50){
            return this.sheetsManager.handleSwipe(SheetsManager.SwipeDirection.SWIPE_LEFT);
        }
        else if(e2.getX() - e1.getX() > 50) {
            return this.sheetsManager.handleSwipe(SheetsManager.SwipeDirection.SWIPE_RIGHT);
        }
        else {
            return false;
        }
    }
    
    
    private static class SheetsManager implements SheetBehavior.SheetCallback
    {
        
        public enum SwipeDirection
        {
            SWIPE_LEFT,
            SWIPE_RIGHT,
            SWIPE_DOWN,
            SWIPE_UP
        }
    
        private SheetBehavior southSheetBehavior;
        private SheetBehavior westSheetBehavior;
        private SheetBehavior northSheetBehavior;
        private SheetBehavior eastSheetBehavior;
    
    
        public SheetBehavior getSouthSheetBehavior()
        {
            return southSheetBehavior;
        }
    
        public void setSouthSheetBehavior(SheetBehavior southSheetBehavior)
        {
            if(this.southSheetBehavior != null)
            {
                this.southSheetBehavior.setSheetCallback(null);
            }
            
            this.southSheetBehavior = southSheetBehavior;
            if(this.southSheetBehavior != null)
            {
                this.southSheetBehavior.setSheetCallback(this);
            }
        }
    
        public SheetBehavior getWestSheetBehavior()
        {
            return westSheetBehavior;
        }
    
        public void setWestSheetBehavior(SheetBehavior westSheetBehavior)
        {
            if(this.westSheetBehavior != null)
            {
                this.westSheetBehavior.setSheetCallback(null);
            }
    
            this.westSheetBehavior = westSheetBehavior;
            if(this.westSheetBehavior != null)
            {
                this.westSheetBehavior.setSheetCallback(this);
            }
        }
    
        public SheetBehavior getNorthSheetBehavior()
        {
            return northSheetBehavior;
        }
    
        public void setNorthSheetBehavior(SheetBehavior northSheetBehavior)
        {
            if(this.northSheetBehavior != null)
            {
                this.northSheetBehavior.setSheetCallback(null);
            }
    
            this.northSheetBehavior = northSheetBehavior;
            if(this.northSheetBehavior != null)
            {
                this.northSheetBehavior.setSheetCallback(this);
            }
        }
    
        public SheetBehavior getEastSheetBehavior()
        {
            return eastSheetBehavior;
        }
    
        public void setEastSheetBehavior(SheetBehavior eastSheetBehavior)
        {
            if(this.eastSheetBehavior != null)
            {
                this.eastSheetBehavior.setSheetCallback(null);
            }
    
            this.eastSheetBehavior = eastSheetBehavior;
            if(this.eastSheetBehavior != null)
            {
                this.eastSheetBehavior.setSheetCallback(this);
            }
        }
    
        /**
         * Called when the west sheet changes its state.
         *
         * @param westSheet The west sheet view.
         * @param newState  The new state. This will be one of {@link #STATE_DRAGGING},
         *                  {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                  {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        @Override
        public void onSheetStateChanged(@NonNull View westSheet, int newState, String identifierName)
        {
            // React to state change
            switch (newState)
            {
                case SheetBehavior.STATE_EXPANDED:
                {
                    Log.w(LOG_TAG, "onStateChanged: STATE_EXPANDED idenfifier [" + identifierName + "]");
                    break;
                }
                case SheetBehavior.STATE_SETTLING:
                {
                    Log.w(LOG_TAG, "onStateChanged: STATE_SETTLING idenfifier [" + identifierName + "]");
                    break;
                }
                case SheetBehavior.STATE_COLLAPSED:
                {
                    Log.w(LOG_TAG, "onStateChanged: STATE_COLLAPSED idenfifier [" + identifierName + "]");
                    break;
                }
                case SheetBehavior.STATE_DRAGGING:
                {
                    Log.w(LOG_TAG, "onStateChanged: STATE_DRAGGING idenfifier [" + identifierName + "]");
                    break;
                }
                case SheetBehavior.STATE_HIDDEN:
                {
                    Log.w(LOG_TAG, "onStateChanged: STATE_HIDDEN idenfifier [" + identifierName + "]");
                    break;
                }
                default:
                {
                    Log.e(LOG_TAG, "onStateChanged: default (Should not be called) idenfifier [" + identifierName + "]");
                    break;
                }
            }
        }
    
        /**
         * Called when the west sheet is being dragged.
         *
         * @param westSheet   The west sheet view.
         * @param slideOffset The new offset of this west sheet within [-1,1] range. Offset
         *                    increases as this west sheet is moving rigth. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         */
        @Override
        public void onSheetSlide(@NonNull View westSheet, float slideOffset, String identifierName)
        {
            Log.w(LOG_TAG, "onSheetSlide slideOffset [" + slideOffset + "] idenfifier [" + identifierName + "]");
        }
    
        
        public void setup()
        {
            southSheetBehavior.setState(SheetBehavior.STATE_HIDDEN);
            // southSheetBehavior.setPeekHeight(150);
            southSheetBehavior.setHideable(true);
            southSheetBehavior.setSkipCollapsed(true);
    
            northSheetBehavior.setState(SheetBehavior.STATE_HIDDEN);
            // northSheetBehavior.setPeekHeight(150);
            northSheetBehavior.setHideable(true);
            northSheetBehavior.setSkipCollapsed(true);
    
            westSheetBehavior.setState(SheetBehavior.STATE_HIDDEN);
            // westSheetBehavior.setPeekWidth(150);
            westSheetBehavior.setHideable(true);
            westSheetBehavior.setSkipCollapsed(true);
    
            eastSheetBehavior.setState(SheetBehavior.STATE_HIDDEN);
            // eastSheetBehavior.setPeekWidth(150);
            eastSheetBehavior.setHideable(true);
            eastSheetBehavior.setSkipCollapsed(true);
        }
        
        private boolean shouldHandleSwipe()
        {
            if(eastSheetBehavior.getState() == SheetBehavior.STATE_HIDDEN && westSheetBehavior.getState() == SheetBehavior.STATE_HIDDEN && southSheetBehavior.getState() == SheetBehavior.STATE_HIDDEN && northSheetBehavior.getState() == SheetBehavior.STATE_HIDDEN )
                return true;
            else
                return false;
        }
        
        public boolean handleSwipe(SwipeDirection direction)
        {
            if(this.shouldHandleSwipe())
            {
                switch (direction)
                {
                    case SWIPE_UP:
                    {
                        southSheetBehavior.setState(SheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_DOWN:
                    {
                        northSheetBehavior.setState(SheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_LEFT:
                    {
                        eastSheetBehavior.setState(SheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_RIGHT:
                    {
                        westSheetBehavior.setState(SheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                }
                return false;
            }
            else
                return false;
        }
    }
}

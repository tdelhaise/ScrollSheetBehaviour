package com.smobee.android.scrollsheetbehaviour;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import com.smobee.android.scrollsheetbehaviour.widget.EastSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.NorthSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.SouthSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.WestSheetBehavior;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnGestureListener
{
    public static final String LOG_TAG = "Main";
    FrameLayout southSheet;
    FrameLayout westSheet;
    FrameLayout eastSheet;
    FrameLayout northSheet;
    
    ListView northListView;
    ListView southListView;
    ListView eastListView;
    ListView westListView;
    
    
    private CoordinatorLayout  coordinatorLayout;
    
    private SheetsManager sheetsManager = new SheetsManager();
    
    private GestureDetector gestureDetector;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        this.southSheet = (FrameLayout) findViewById(R.id.south_sheet);
        this.westSheet = (FrameLayout) findViewById(R.id.west_sheet);
        this.eastSheet = (FrameLayout) findViewById(R.id.east_sheet);
        this.northSheet = (FrameLayout) findViewById(R.id.north_sheet);
        
        this.northListView = (ListView) findViewById(R.id.list_view_north);
        this.southListView = (ListView) findViewById(R.id.list_view_south);
        this.eastListView = (ListView) findViewById(R.id.list_view_east);
        this.westListView = (ListView) findViewById(R.id.list_view_west);
    
    
        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile", "OpenStep", "NeXTStep", "HP-UX", "AIX", "Mach III", "BeOS", "Heroku" };
    
        final ArrayList<String> listEast = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            listEast.add(values[i]);
        }
    
        final ArrayList<String> listWest = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            listWest.add(values[i]);
        }
    
        final ArrayList<String> listNorth = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            listNorth.add(values[i]);
        }
    
        final ArrayList<String> listSouth = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            listSouth.add(values[i]);
        }
        
        final StableArrayAdapter northAdapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listNorth);
        final StableArrayAdapter southAdapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listSouth);
        final StableArrayAdapter eastAdapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listEast);
        final StableArrayAdapter westAdapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listWest);
    
    
        this.northListView.setAdapter(northAdapter);
    
        this.northListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                listNorth.remove(item);
                                northAdapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
    
    
        this.southListView.setAdapter(southAdapter);
    
        this.southListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                listSouth.remove(item);
                                southAdapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
    
        this.eastListView.setAdapter(eastAdapter);
    
        this.eastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                listEast.remove(item);
                                eastAdapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
    
        this.westListView.setAdapter(westAdapter);
    
        this.westListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                listWest.remove(item);
                                westAdapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }
        });
        
        this.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    
        gestureDetector = new GestureDetector(this, this);
        
        // SOUTH
        SouthSheetBehavior southSheetBehavior = SouthSheetBehavior.from(southSheet);
        this.sheetsManager.setSouthSheetBehavior(southSheetBehavior);
        
        // NORTH
        NorthSheetBehavior northSheetBehavior = NorthSheetBehavior.from(northSheet);
        this.sheetsManager.setNorthSheetBehavior(northSheetBehavior);
        
        // WEST
        WestSheetBehavior westSheetBehavior = WestSheetBehavior.from(westSheet);
        this.sheetsManager.setWestSheetBehavior(westSheetBehavior);
        
        // EAST
        EastSheetBehavior eastSheetBehavior = EastSheetBehavior.from(eastSheet);
        this.sheetsManager.setEastSheetBehavior(eastSheetBehavior);
    
        this.sheetsManager.setup();
    }
    
    private class StableArrayAdapter extends ArrayAdapter<String>
    {
    
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
    
        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects)
        {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i)
            {
                mIdMap.put(objects.get(i), i);
            }
        }
    
        @Override
        public long getItemId(int position)
        {
            String item = getItem(position);
            return mIdMap.get(item);
        }
    
        @Override
        public boolean hasStableIds()
        {
            return true;
        }
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
    
    
    private static class SheetsManager implements SouthSheetBehavior.SouthSheetCallback, NorthSheetBehavior.NorthSheetCallback, WestSheetBehavior.WestSheetCallback, EastSheetBehavior.EastSheetCallback
    {
        
        public enum SwipeDirection
        {
            SWIPE_LEFT,
            SWIPE_RIGHT,
            SWIPE_DOWN,
            SWIPE_UP
        }
    
        private SouthSheetBehavior southSheetBehavior;
        private WestSheetBehavior  westSheetBehavior;
        private NorthSheetBehavior northSheetBehavior;
        private EastSheetBehavior  eastSheetBehavior;
    
    
        public SouthSheetBehavior getSouthSheetBehavior()
        {
            return southSheetBehavior;
        }
    
        public void setSouthSheetBehavior(SouthSheetBehavior southSheetBehavior)
        {
            if(this.southSheetBehavior != null)
            {
                this.southSheetBehavior.setSouthSheetCallback(null);
            }
            
            this.southSheetBehavior = southSheetBehavior;
            if(this.southSheetBehavior != null)
            {
                this.southSheetBehavior.setSouthSheetCallback(this);
            }
        }
    
        public WestSheetBehavior getWestSheetBehavior()
        {
            return westSheetBehavior;
        }
    
        public void setWestSheetBehavior(WestSheetBehavior westSheetBehavior)
        {
            if(this.westSheetBehavior != null)
            {
                this.westSheetBehavior.setWestSheetCallback(null);
            }
    
            this.westSheetBehavior = westSheetBehavior;
            if(this.westSheetBehavior != null)
            {
                this.westSheetBehavior.setWestSheetCallback(this);
            }
        }
    
        public NorthSheetBehavior getNorthSheetBehavior()
        {
            return northSheetBehavior;
        }
    
        public void setNorthSheetBehavior(NorthSheetBehavior northSheetBehavior)
        {
            if(this.northSheetBehavior != null)
            {
                this.northSheetBehavior.setNorthSheetCallback(null);
            }
    
            this.northSheetBehavior = northSheetBehavior;
            if(this.northSheetBehavior != null)
            {
                this.northSheetBehavior.setNorthSheetCallback(this);
            }
        }
    
        public EastSheetBehavior getEastSheetBehavior()
        {
            return eastSheetBehavior;
        }
    
        public void setEastSheetBehavior(EastSheetBehavior eastSheetBehavior)
        {
            if(this.eastSheetBehavior != null)
            {
                this.eastSheetBehavior.setEastSheetCallback(null);
            }
    
            this.eastSheetBehavior = eastSheetBehavior;
            if(this.eastSheetBehavior != null)
            {
                this.eastSheetBehavior.setEastSheetCallback(this);
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
        public void onWestSheetStateChanged(@NonNull View westSheet, int newState)
        {
            // React to state change
            switch (newState)
            {
                case WestSheetBehavior.STATE_EXPANDED:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST STATE_EXPANDED");
                    break;
                }
                case WestSheetBehavior.STATE_SETTLING:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST STATE_SETTLING");
                    break;
                }
                case WestSheetBehavior.PEEK_WIDTH_AUTO:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST PEEK_WIDTH_AUTO");
                    break;
                }
                case WestSheetBehavior.STATE_COLLAPSED:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST STATE_COLLAPSED");
                    break;
                }
                case WestSheetBehavior.STATE_DRAGGING:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST STATE_DRAGGING");
                    break;
                }
                case WestSheetBehavior.STATE_HIDDEN:
                {
                    Log.w(LOG_TAG, "onStateChanged: WEST STATE_HIDDEN");
                    break;
                }
                default:
                {
                    Log.e(LOG_TAG, "onStateChanged: WEST default (Should not be called)");
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
        public void onWestSheetSlide(@NonNull View westSheet, float slideOffset)
        {
            Log.w(LOG_TAG, "onWestSheetSlide");
        }
    
        /**
         * Called when the east sheet changes its state.
         *
         * @param eastSheet The east sheet view.
         * @param newState  The new state. This will be one of {@link #STATE_DRAGGING},
         *                  {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                  {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        @Override
        public void onEastSheetStateChanged(@NonNull View eastSheet, int newState)
        {
            // React to state change
            switch (newState)
            {
                case EastSheetBehavior.STATE_EXPANDED:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST STATE_EXPANDED");
                    break;
                }
                case EastSheetBehavior.STATE_SETTLING:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST STATE_SETTLING");
                    break;
                }
                case EastSheetBehavior.PEEK_WIDTH_AUTO:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST PEEK_WIDTH_AUTO");
                    break;
                }
                case EastSheetBehavior.STATE_COLLAPSED:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST STATE_COLLAPSED");
                    break;
                }
                case EastSheetBehavior.STATE_DRAGGING:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST STATE_DRAGGING");
                    break;
                }
                case EastSheetBehavior.STATE_HIDDEN:
                {
                    Log.w(LOG_TAG, "onStateChanged: EAST STATE_HIDDEN");
                    break;
                }
                default:
                {
                    Log.e(LOG_TAG, "onStateChanged: EAST default (Should not be called)");
                    break;
                }
            }
        }
    
        /**
         * Called when the east sheet is being dragged.
         *
         * @param eastSheet   The east sheet view.
         * @param slideOffset The new offset of this east sheet within [-1,1] range. Offset
         *                    increases as this right sheet is moving left. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         */
        @Override
        public void onEastSheetSlide(@NonNull View eastSheet, float slideOffset)
        {
            Log.w(LOG_TAG, "onEastSheetSlide");
        }
    
        /**
         * Called when the north sheet changes its state.
         *
         * @param northSheet The north sheet view.
         * @param newState   The new state. This will be one of {@link #STATE_DRAGGING},
         *                   {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                   {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        @Override
        public void onNorthSheetStateChanged(@NonNull View northSheet, int newState)
        {
            // React to state change
            switch (newState) {
                case NorthSheetBehavior.STATE_EXPANDED: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH STATE_EXPANDED");
                    break;
                }
                case NorthSheetBehavior.STATE_SETTLING: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH STATE_SETTLING");
                    break;
                }
                case NorthSheetBehavior.PEEK_HEIGHT_AUTO: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH PEEK_HEIGHT_AUTO");
                    break;
                }
                case NorthSheetBehavior.STATE_COLLAPSED: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH STATE_COLLAPSED");
                    break;
                }
                case NorthSheetBehavior.STATE_DRAGGING: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH STATE_DRAGGING");
                    break;
                }
                case NorthSheetBehavior.STATE_HIDDEN: {
                    Log.w(LOG_TAG, "onStateChanged: NORTH STATE_HIDDEN");
                    break;
                }
                default: {
                    Log.e(LOG_TAG, "onStateChanged: NORTH default (Should not be called)");
                    break;
                }
            }
        }
    
        /**
         * Called when the north sheet is being dragged.
         *
         * @param northSheet  The north sheet view.
         * @param slideOffset The new offset of this top sheet within [-1,1] range. Offset
         *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         */
        @Override
        public void onNorthSheetSlide(@NonNull View northSheet, float slideOffset)
        {
            Log.w(LOG_TAG, "onNorthSheetSlide");
        }
    
        /**
         * Called when the south sheet changes its state.
         *
         * @param southSheet The south sheet view.
         * @param newState   The new state. This will be one of {@link #STATE_DRAGGING},
         *                   {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                   {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        @Override
        public void onSouthSheetStateChanged(@NonNull View southSheet, int newState)
        {
            switch (newState) {
                case SouthSheetBehavior.STATE_EXPANDED: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH STATE_EXPANDED");
                    break;
                }
                case SouthSheetBehavior.STATE_SETTLING: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH STATE_SETTLING");
                    break;
                }
                case SouthSheetBehavior.PEEK_HEIGHT_AUTO: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH PEEK_HEIGHT_AUTO");
                    break;
                }
                case SouthSheetBehavior.STATE_COLLAPSED: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH STATE_COLLAPSED");
                    break;
                }
                case SouthSheetBehavior.STATE_DRAGGING: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH STATE_DRAGGING");
                    break;
                }
                case SouthSheetBehavior.STATE_HIDDEN: {
                    Log.w(LOG_TAG, "onStateChanged: SOUTH STATE_HIDDEN");
                    break;
                }
                default: {
                    Log.e(LOG_TAG, "onStateChanged: SOUTH default (Should not be called)");
                    break;
                }
            }
        }
    
        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param southSheet  The south sheet view.
         * @param slideOffset The new offset of this south sheet within [-1,1] range. Offset
         *                    increases as this south sheet is moving upward. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         */
        @Override
        public void onSouthSheetSlide(@NonNull View southSheet, float slideOffset)
        {
            Log.w(LOG_TAG, "onSouthSheetSlide");
        }
        
        public void setup()
        {
            southSheetBehavior.setState(SouthSheetBehavior.STATE_HIDDEN);
            // southSheetBehavior.setPeekHeight(150);
            southSheetBehavior.setHideable(true);
            southSheetBehavior.setSkipCollapsed(true);
    
            northSheetBehavior.setState(NorthSheetBehavior.STATE_HIDDEN);
            // northSheetBehavior.setPeekHeight(150);
            northSheetBehavior.setHideable(true);
            northSheetBehavior.setSkipCollapsed(true);
    
            westSheetBehavior.setState(WestSheetBehavior.STATE_HIDDEN);
            // westSheetBehavior.setPeekWidth(150);
            westSheetBehavior.setHideable(true);
            westSheetBehavior.setSkipCollapsed(true);
    
            eastSheetBehavior.setState(EastSheetBehavior.STATE_HIDDEN);
            // eastSheetBehavior.setPeekWidth(150);
            eastSheetBehavior.setHideable(true);
            eastSheetBehavior.setSkipCollapsed(true);
        }
        
        private boolean shouldHandleSwipe()
        {
            if(eastSheetBehavior.getState() == EastSheetBehavior.STATE_HIDDEN && westSheetBehavior.getState() == WestSheetBehavior.STATE_HIDDEN && southSheetBehavior.getState() == SouthSheetBehavior.STATE_HIDDEN && northSheetBehavior.getState() == NorthSheetBehavior.STATE_HIDDEN )
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
                        southSheetBehavior.setState(SouthSheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_DOWN:
                    {
                        northSheetBehavior.setState(NorthSheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_LEFT:
                    {
                        eastSheetBehavior.setState(EastSheetBehavior.STATE_EXPANDED);
                        return true;
                    }
                    case SWIPE_RIGHT:
                    {
                        westSheetBehavior.setState(WestSheetBehavior.STATE_EXPANDED);
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

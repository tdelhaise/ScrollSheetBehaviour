package com.smobee.android.scrollsheetbehaviour;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.smobee.android.scrollsheetbehaviour.widget.EastSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.NorthSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.SouthSheetBehavior;
import com.smobee.android.scrollsheetbehaviour.widget.WestSheetBehavior;

public class MainActivity extends AppCompatActivity
{
    public static final String LOG_TAG = "Main";
    FrameLayout southSheet;
    FrameLayout westSheet;
    FrameLayout eastSheet;
    FrameLayout northSheet;
    
    private SouthSheetBehavior southSheetBehavior;
    private WestSheetBehavior  westSheetBehavior;
    private NorthSheetBehavior northSheetBehavior;
    private EastSheetBehavior  eastSheetBehavior;
    private CoordinatorLayout  coordinatorLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        this.southSheet = (FrameLayout) findViewById(R.id.south_sheet);
        this.westSheet = (FrameLayout) findViewById(R.id.west_sheet);
        this.eastSheet = (FrameLayout) findViewById(R.id.east_sheet);
        this.northSheet = (FrameLayout) findViewById(R.id.north_sheet);
    
        this.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    
        // final GestureDetector swipeGestureDetector = new GestureDetector(new SwipePermissionGestureListener());
        /*
        this.coordinatorLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                return swipeGestureDetector.onTouchEvent(event);
            }
        });
    */
    
    
        southSheetBehavior = SouthSheetBehavior.from(southSheet);
        southSheetBehavior.setSouthSheetCallback(new SouthSheetBehavior.SouthSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                switch (newState) {
                    case SouthSheetBehavior.STATE_EXPANDED: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_EXPANDED");
                        break;
                    }
                    case SouthSheetBehavior.STATE_SETTLING: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_SETTLING");
                        break;
                    }
                    case SouthSheetBehavior.PEEK_HEIGHT_AUTO: {
                        Log.w(LOG_TAG, "onStateChanged: PEEK_HEIGHT_AUTO");
                        break;
                    }
                    case SouthSheetBehavior.STATE_COLLAPSED: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_COLLAPSED");
                        break;
                    }
                    case SouthSheetBehavior.STATE_DRAGGING: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_DRAGGING");
                        break;
                    }
                    case SouthSheetBehavior.STATE_HIDDEN: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_HIDDEN");
                        break;
                    }
                    default: {
                        Log.e(LOG_TAG, "onStateChanged: default (Should not be called)");
                        break;
                    }
                }
            }
        
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events (if any)
            }
        });
    
        southSheetBehavior.setState(SouthSheetBehavior.STATE_COLLAPSED);
        southSheetBehavior.setPeekHeight(150);
        southSheetBehavior.setHideable(true);
        // southSheetBehavior.setSkipCollapsed(true);
    
        northSheetBehavior = NorthSheetBehavior.from(northSheet);
        northSheetBehavior.setNorthSheetCallback(new NorthSheetBehavior.NorthSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                switch (newState) {
                    case NorthSheetBehavior.STATE_EXPANDED: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_EXPANDED");
                        break;
                    }
                    case NorthSheetBehavior.STATE_SETTLING: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_SETTLING");
                        break;
                    }
                    case NorthSheetBehavior.PEEK_HEIGHT_AUTO: {
                        Log.w(LOG_TAG, "onStateChanged: PEEK_HEIGHT_AUTO");
                        break;
                    }
                    case NorthSheetBehavior.STATE_COLLAPSED: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_COLLAPSED");
                        break;
                    }
                    case NorthSheetBehavior.STATE_DRAGGING: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_DRAGGING");
                        break;
                    }
                    case NorthSheetBehavior.STATE_HIDDEN: {
                        Log.w(LOG_TAG, "onStateChanged: STATE_HIDDEN");
                        break;
                    }
                    default: {
                        Log.e(LOG_TAG, "onStateChanged: default (Should not be called)");
                        break;
                    }
                }
            }
        
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events (if any)
            }
        });
    
        northSheetBehavior.setState(NorthSheetBehavior.STATE_COLLAPSED);
        northSheetBehavior.setPeekHeight(150);
        northSheetBehavior.setHideable(true);
        // northSheetBehavior.setSkipCollapsed(true);
        
        westSheetBehavior = WestSheetBehavior.from(westSheet);
        westSheetBehavior.setWestSheetCallback(new WestSheetBehavior.LeftSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                // React to state change
                switch (newState)
                {
                    case WestSheetBehavior.STATE_EXPANDED:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_EXPANDED");
                        break;
                    }
                    case WestSheetBehavior.STATE_SETTLING:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_SETTLING");
                        break;
                    }
                    case WestSheetBehavior.PEEK_WIDTH_AUTO:
                    {
                        Log.w(LOG_TAG, "onStateChanged: PEEK_WIDTH_AUTO");
                        break;
                    }
                    case WestSheetBehavior.STATE_COLLAPSED:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_COLLAPSED");
                        break;
                    }
                    case WestSheetBehavior.STATE_DRAGGING:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_DRAGGING");
                        break;
                    }
                    case WestSheetBehavior.STATE_HIDDEN:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_HIDDEN");
                        break;
                    }
                    default:
                    {
                        Log.e(LOG_TAG, "onStateChanged: default (Should not be called)");
                        break;
                    }
                }
            }
        
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                // React to dragging events (if any)
                Log.d(LOG_TAG, "onSlide: slideOffset [" + slideOffset + "]");
            }
        });
    
        westSheetBehavior.setState(WestSheetBehavior.STATE_COLLAPSED);
        westSheetBehavior.setPeekWidth(150);
        westSheetBehavior.setHideable(true);
        // westSheetBehavior.setSkipCollapsed(true);
    
        eastSheetBehavior = EastSheetBehavior.from(eastSheet);
        eastSheetBehavior.setEastSheetCallback(new EastSheetBehavior.EastSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                // React to state change
                switch (newState)
                {
                    case EastSheetBehavior.STATE_EXPANDED:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_EXPANDED");
                        break;
                    }
                    case EastSheetBehavior.STATE_SETTLING:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_SETTLING");
                        break;
                    }
                    case EastSheetBehavior.PEEK_WIDTH_AUTO:
                    {
                        Log.w(LOG_TAG, "onStateChanged: PEEK_WIDTH_AUTO");
                        break;
                    }
                    case EastSheetBehavior.STATE_COLLAPSED:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_COLLAPSED");
                        break;
                    }
                    case EastSheetBehavior.STATE_DRAGGING:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_DRAGGING");
                        break;
                    }
                    case EastSheetBehavior.STATE_HIDDEN:
                    {
                        Log.w(LOG_TAG, "onStateChanged: STATE_HIDDEN");
                        break;
                    }
                    default:
                    {
                        Log.e(LOG_TAG, "onStateChanged: default (Should not be called)");
                        break;
                    }
                }
            }
        
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                // React to dragging events (if any)
                Log.d(LOG_TAG, "onSlide: slideOffset [" + slideOffset + "]");
            }
        });
    
        eastSheetBehavior.setState(EastSheetBehavior.STATE_COLLAPSED);
        eastSheetBehavior.setPeekWidth(150);
        eastSheetBehavior.setHideable(true);
        //eastSheetBehavior.setSkipCollapsed(true);
    }
    
    public void moveToRight()
    {
        Log.d("###","moveToRight");
        
    
    }
    
    public void moveToLeft()
    {
        Log.d("###","moveToLeft");
        this.westSheetBehavior.setState(WestSheetBehavior.STATE_EXPANDED);
    }
    
    public void moveToBottom()
    {
        Log.d("###","moveToBottom");
        this.southSheetBehavior.setState(SouthSheetBehavior.STATE_EXPANDED);
    
    }
    
    public void moveToTop()
    {
        Log.d("###","moveToTop");
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
    
    class SwipePermissionGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "CHAT_SWIPE_GESTURE";
        
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        
        @Override
        public boolean onDown(MotionEvent event) {
            
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean managed = false;
            
            
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // *******************
                // Swipe Right to left
                // We launch Video Chat
                // *******************
                Log.d("###","Swipe RIGHT to LEFT");
                // on veut donc voir la vue de droite ...
                MainActivity.this.moveToRight();
            }
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // *******************
                // Swipe Left to Right
                // We launch Friends
                // *******************
                Log.d("###","Swipe LEFT to RIGHT");
                // on veut donc voir la vue de gauche ...
                MainActivity.this.moveToLeft();
            }
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // *******************
                // Swipe Top to Bottom
                // We launch MAP
                // *******************
                Log.d("###","Swipe BOTTOM to TOP");
                // on veut donc voir la vue du bas ...
                MainActivity.this.moveToBottom();
            }
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // *******************
                // Swipe Bottom to Top
                // We launch Profile
                // *******************
                Log.d("###","Swipe TOP to BOTTOM");
                // on veut donc voir la vue du haut ...
                MainActivity.this.moveToTop();
            }
            
            return managed;
        }
    }
    
}

package com.zhongzewei.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * This is an expandable grid view which extends GridView. By using this ExpandableGridView, you can get
 * OSX/iOS folder expand like experience. The whole idea of the "expand" functionality is to add a cover
 * layer beyond the grid view and insert a sub grid view in the cover layer. This can achieve the experience
 * of split/expand the grid view.
 */
public class ExpandableGridView extends GridView {

    private WindowManager.LayoutParams mLayoutParams;
    private LinearLayout mCoverView;
    private ViewGroup mParentViewGroup;
    private boolean hasScrolled = false;
    private int scrollY = 0;
    private OnExpandItemClickListener mListener;

    public ExpandableGridView(Context context) {
        this(context, null);
    }

    public ExpandableGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set listener for sub grid view item. When sub grid view item is clicked, it will invoke
     * the listener's onItemClick function.
     *
     * @param listener
     */
    public void setOnExpandItemClickListener(OnExpandItemClickListener listener){
        mListener = listener;
    }

    /**
     * Expand the grid view under the clicked item.
     * @param clickedView The clicked item.
     * @param expandGridAdapter Adapter set to sub grid view.
     */
    public void expandGridViewAtView(View clickedView, final BaseAdapter expandGridAdapter){

        // 0. Init the cover layer
        mCoverView = new LinearLayout(getContext());
        mCoverView.setOrientation(LinearLayout.VERTICAL);

        // 1. Init the up, middle and down part views for the cover layer
        ImageView imageViewUp = new ImageView(getContext());
        ImageView imageViewDown = new ImageView(getContext());
        HorizontalScrollView middleView = new HorizontalScrollView(getContext());

        // bottom position of clicked item view
        int touchBottom = clickedView.getBottom();
        // when the clicked item view is not fully showed, scroll up to make it fully show
        if (touchBottom > (getMeasuredHeight()-getPaddingBottom()-getVerticalSpacing())){
            hasScrolled = true;
            scrollY = touchBottom-getMeasuredHeight()+getPaddingBottom()+getVerticalSpacing()/2;
            scrollBy(0, scrollY);
        }
        // 2. Take snapshot of current grid view
        this.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        this.destroyDrawingCache();// clear the draw cache, so that next time will not get the same drawing

        int heightUp = 1;// height of up cover image
        int heightDown = 1;// height of down cover image
        int middleViewHeight = clickedView.getHeight()+20;// height of middle sub grid view
        int bottomPos = bitmap.getHeight() - touchBottom - getVerticalSpacing()/2 - middleViewHeight;
        int upY = 0; // y position where up image start to split the image
        int downY = touchBottom;// y position where down image start to split the image
        // if the middle sub grid view cannot fully display, decrease up cover image's height of middleViewHeight
        // so that the cover layer can scroll up to make middle sub grid view display
        if (bottomPos <= 0){
            heightUp = touchBottom+getVerticalSpacing()/2 - middleViewHeight;
            upY = middleViewHeight;
            // down image height, decrease middle view height so that cover layer height matches the grid view height
            heightDown = bitmap.getHeight() - heightUp - middleViewHeight;
            // when down image view cannot fully display, set it's height to the bottom padding height
            if (heightDown<0){
                heightUp+=heightDown;
                heightDown = getPaddingBottom();
                heightUp-=heightDown;
            }
            downY = upY + heightUp;
        } else {
            // when the middle view can fully display, decrease down image view's height of middle view height
            heightUp = touchBottom+getVerticalSpacing()/2;
            heightDown = bottomPos;
        }
        // 3. Split the snapshot image to up/down image
        Bitmap bitmapUp = Bitmap.createBitmap(bitmap, 0, upY, bitmap.getWidth(), heightUp);
        Bitmap bitmapDown = Bitmap.createBitmap(bitmap, 0, downY, bitmap.getWidth(), heightDown);
        // add click handler to the up/down image view: collapse the expand grid view
        imageViewUp.setImageBitmap(bitmapUp);
        imageViewUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseGridView();
            }
        });
        imageViewDown.setImageBitmap(bitmapDown);
        imageViewDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseGridView();
            }
        });

        // 4. Middle sub grid view, set it's to one row, horizontal scrollable grid view
        LinearLayout linearLayout = new LinearLayout(getContext());
        GridView gridView = new GridView(getContext());
        int count = expandGridAdapter.getCount();
        int vSpace = getVerticalSpacing();
        int hSpace = getHorizontalSpacing();
        LayoutParams gridParams = new LayoutParams(count*(getColumnWidth()+hSpace),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(gridParams);
        gridView.setColumnWidth(getColumnWidth());
        gridView.setHorizontalSpacing(hSpace);
        gridView.setVerticalSpacing(vSpace);
        gridView.setNumColumns(count);
        gridView.setPadding(hSpace, vSpace, hSpace, vSpace);
        gridView.setAdapter(expandGridAdapter);

        // 5. Set sub grid view's item click listener
        gridView.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (mListener!=null){
                    mListener.onItemClick( position , expandGridAdapter.getItem(position));
                }
            }
        });
        linearLayout.addView(gridView);
        middleView.addView(linearLayout);
        // Triangle arrow up
        int touchX = clickedView.getLeft()+getColumnWidth()/2;
        int touchY = heightUp;
        Canvas canvas = new Canvas(bitmapUp);//use Canvas to draw triangle in the up cover image
        Path path = new Path();
        path.moveTo(touchX-15, touchY);
        path.lineTo(touchX+15, touchY);
        path.lineTo(touchX, touchY-15);
        path.lineTo(touchX-15, touchY);
        ShapeDrawable circle = new ShapeDrawable(new PathShape(path, getWidth(), getHeight()));
        circle.getPaint().setColor(Color.DKGRAY);
        circle.setBounds(0, 0, getWidth(), getHeight());
        circle.draw(canvas);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(getWidth(), middleViewHeight);
        middleView.setLayoutParams(params);
        middleView.setBackgroundColor(Color.DKGRAY);

        // 6. Add the up/middle/down sub views to the cover layer
        mCoverView.addView(imageViewUp);
        mCoverView.addView(middleView);
        mCoverView.addView(imageViewDown);

        // 7. Add the cover layer to the grid view and bring it to the front
        mParentViewGroup = (ViewGroup)getParent();
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = getLeft();//start x
        mLayoutParams.y = getTop(); //start y
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;
        mParentViewGroup.addView(mCoverView, 0, mLayoutParams);
        mCoverView.bringToFront();

    }

    /**
     * Collapse the grid view and remove the cover layer
     */
    public void collapseGridView(){
        // remove the cover layer
        if(mParentViewGroup!=null && mCoverView != null){
            mCoverView.removeAllViews();
            mParentViewGroup.removeView(mCoverView);
            mLayoutParams = null;
            mCoverView = null;
            mParentViewGroup = null;
        }
        // if the grid view has scrolled before expand, scroll it back
        if (hasScrolled){
            scrollBy(0, -scrollY);
            hasScrolled = false;
            scrollY = 0;
        }
    }

    /**
     * Sub item click listener interface
     */
    public interface OnExpandItemClickListener{
        public void onItemClick(int position, Object clickPositionData);
    }

}

package com.demondevelopers.emoji.input.lib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;
import static android.widget.AdapterView.INVALID_POSITION;


public class EmojiGridView extends View
{
	private int mEmojiHighlighted = INVALID_POSITION;
	private Rect mEmojiHighlightedRect = new Rect();
	private Rect mVisibleRect = new Rect();
	private Rect mIconDrawingRect = new Rect();
	
	private final int mMinColumnWidth;
	private final int mVerticalSpacing;
	private final int mIconSize;
	private final int mRowHeight;
	
	private final GestureDetector mDetector;
	
	private final Paint mForegroundPaint;
	private final Paint mBackgroundPaint;
	
	private int mEmojiCount;
	private int mColumnWidth;
	private int mEmojiPerRow;
	private int mIconLeftRightPad;
	
	private EmojiGridViewListener mListener;
	
	private ScrollViewWithNotifier mScrollView;
	
	private BaseAdapter mAdapter;
	private EmojiAdapterObserver mDataSetObserver;
	
	private static LruCache<Integer, Bitmap> sCache = new LruCache<Integer, Bitmap>(1024 * 1024 * 4) // 4MiB
	{
		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected int sizeOf(Integer key, Bitmap value)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				return value.getAllocationByteCount();
			}
			else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
				return value.getByteCount();
			}
			else{
				return value.getRowBytes() * value.getHeight();
			}
		}
	};
	
	public EmojiGridView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EmojiGridView, 0, 0);
		mMinColumnWidth  = a.getDimensionPixelSize(R.styleable.EmojiGridView_columnWidth,     1);
		mVerticalSpacing = a.getDimensionPixelSize(R.styleable.EmojiGridView_verticalSpacing, 1);
		mIconSize        = a.getDimensionPixelSize(R.styleable.EmojiGridView_iconSize,        1);
		mRowHeight       = mIconSize + mVerticalSpacing;
		a.recycle();
		
		mDetector = new GestureDetector(context, new EmojiGestureListener());
		mForegroundPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint.setStyle(Paint.Style.FILL);
		mBackgroundPaint.setColor(0x9A33B5E5);
	}
	
	public void setOnEmojiClickedListener(EmojiGridViewListener listener)
	{
		mListener = listener;
	}
	
	public void setScrollViewWithNotifier(ScrollViewWithNotifier scrollView)
	{
		mScrollView = scrollView;
		if(scrollView != null){
			scrollView.setScrollListener(new ScrollViewWithNotifier.Listener()
			{
				@Override
				public void onScrollChanged(int l, int t, int oldl, int oldt)
				{
					invalidate();
				}
			});
		}
	}
	
	public void setAdapter(BaseAdapter adapter)
	{
		if(mAdapter != null && mDataSetObserver != null){
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		mAdapter = adapter;
		if(mAdapter != null){
			mEmojiCount = mAdapter.getCount();
			mDataSetObserver = new EmojiAdapterObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}
		else{
			mEmojiCount = 0;
		}
		setHighlightedEmoji(INVALID_POSITION);
		requestLayout();
	}
	
	private void emojiSelected()
	{
		if(mListener != null && mEmojiHighlighted != INVALID_POSITION){
			mListener.onEmojiClicked(mEmojiHighlighted);
		}
		setHighlightedEmoji(INVALID_POSITION);
	}
	
	private int getEmojiAtPoint(int x, int y)
	{
		int row = y / mRowHeight;
		return (row * mEmojiPerRow) + (x / mColumnWidth);
	}
	
	private void getEmojiRect(int position, Rect rect)
	{
		int x = (position % mEmojiPerRow) * mColumnWidth;
		int y = (position / mEmojiPerRow) * mRowHeight;
		rect.set(x, y, x + mColumnWidth, y + mRowHeight);
	}
	
	private void setHighlightedEmoji(int position)
	{
		if(mEmojiHighlighted != INVALID_POSITION){
			invalidate(mEmojiHighlightedRect);
		}
		mEmojiHighlighted = position;
		if(mEmojiHighlighted != INVALID_POSITION){
			getEmojiRect(mEmojiHighlighted, mEmojiHighlightedRect);
			invalidate(mEmojiHighlightedRect);
		}
	}
	
	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean result = mDetector.onTouchEvent(event);
		if(!result){
			if(event.getAction() == MotionEvent.ACTION_CANCEL){
				setHighlightedEmoji(INVALID_POSITION);
				result = true;
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				emojiSelected();
				result = true;
			}
		}
		return result;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int cols = width / mMinColumnWidth;
		setMeasuredDimension(width, (1 + (-1 + mEmojiCount) / cols) * (mIconSize + mVerticalSpacing));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		mEmojiPerRow = w / mMinColumnWidth;
		int cols = w % mMinColumnWidth;
		mColumnWidth = mMinColumnWidth + cols / mEmojiPerRow;
		mIconLeftRightPad = (mColumnWidth - mIconSize) / 2;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if(mAdapter == null){
			return;
		}
		
		mScrollView.getHitRect(mVisibleRect);
		getLocalVisibleRect(mVisibleRect);
		
		int startRow = mVisibleRect.top / mRowHeight;
		int endRow   = 1 + mVisibleRect.bottom / mRowHeight;
		int startPos = startRow * mEmojiPerRow;
		int count = mAdapter.getCount();
		int endPos = endRow * mEmojiPerRow;
		if(endPos > count){
			endPos = count;
		}
		int y = mVerticalSpacing / 2 + startRow * mRowHeight;
		mIconDrawingRect.set(mIconLeftRightPad, y, mIconLeftRightPad + mIconSize, y + mIconSize);
		for(int pos = startPos, rowIndex = 0; pos < endPos; pos++, rowIndex++){
			if(rowIndex >= mEmojiPerRow){
				y += mRowHeight;
				mIconDrawingRect.set(mIconLeftRightPad, y, mIconLeftRightPad + mIconSize, y + mIconSize);
				rowIndex = 0;
			}
			if(pos == mEmojiHighlighted){
				canvas.drawRect(mEmojiHighlightedRect, mBackgroundPaint);
			}
			Integer drawableResId = (Integer)mAdapter.getItem(pos);
			Bitmap bitmap = sCache.get(drawableResId);
			if(bitmap == null){
				BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(drawableResId.intValue());
				if(drawable != null){
					bitmap = drawable.getBitmap();
					sCache.put(drawableResId, bitmap);
				}
			}
			if(bitmap != null){
				canvas.drawBitmap(bitmap, null, mIconDrawingRect, mForegroundPaint);
			}
			mIconDrawingRect.offset(mColumnWidth, 0);
		}
	}
	
	private class EmojiGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDown(MotionEvent e)
		{
			int position = getEmojiAtPoint((int)e.getX(), (int)e.getY());
			if(position >= 0 && position < mEmojiCount){
				setHighlightedEmoji(position);
			}
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			emojiSelected();
			return true;
		}
	}
	
	private class EmojiAdapterObserver extends DataSetObserver
	{
		@Override
		public void onChanged()
		{
			mEmojiCount = mAdapter.getCount();
			setHighlightedEmoji(INVALID_POSITION);
			requestLayout();
		}
		
		@Override
		public void onInvalidated()
		{
			mEmojiCount = mAdapter.getCount();
			setHighlightedEmoji(INVALID_POSITION);
			requestLayout();
		}
	}
	
	public static interface EmojiGridViewListener
	{
		public abstract void onEmojiClicked(int position);
	}
}

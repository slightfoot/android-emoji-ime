package com.demondevelopers.emoji.input.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;


public class ScrollViewWithNotifier extends ScrollView
{
	Listener mScrollListener;
	
	
	public ScrollViewWithNotifier(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		mScrollListener.onScrollChanged(l, t, oldl, oldt);
	}
	
	void setScrollListener(Listener paramListener)
	{
		mScrollListener = paramListener;
	}
	
	
	public static abstract interface Listener
	{
		public abstract void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}

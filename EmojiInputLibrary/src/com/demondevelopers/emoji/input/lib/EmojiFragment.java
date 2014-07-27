package com.demondevelopers.emoji.input.lib;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class EmojiFragment extends Fragment
{
	private EmojiCategoryAdapter mEmojiAdapter;
	
	private HashMap<String, Integer> mTabIdToCategory = new HashMap<String, Integer>();
	private int mCurrentCategory = -1;
	
	private TabHost   mTabHost;
	private ViewPager mEmojiPager;
	
	private Listener mListener;
	
	public static interface Listener
	{
		public abstract void onInsertEmoji(CharSequence charsequence);
		public abstract void onBackspace();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	// HACK
	public void setListener(Listener listener)
	{
		mListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.emoji_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		
		mTabHost = (TabHost)view.findViewById(R.id.emoji_category_tabhost);
		mTabHost.setup();
		addTab(mTabHost, "Recent",  R.id.recentEmojiFakeGrid,  R.drawable.ic_emoji_recent_light,  0);
		addTab(mTabHost, "Faces",   R.id.facesEmojiFakeGrid,   R.drawable.ic_emoji_people_light,  1);
		addTab(mTabHost, "Objects", R.id.objectsEmojiFakeGrid, R.drawable.ic_emoji_objects_light, 2);
		addTab(mTabHost, "Nature",  R.id.natureEmojiFakeGrid,  R.drawable.ic_emoji_nature_light,  3);
		addTab(mTabHost, "Places",  R.id.placesEmojiFakeGrid,  R.drawable.ic_emoji_places_light,  4);
		addTab(mTabHost, "Symbols", R.id.symbolsEmojiFakeGrid, R.drawable.ic_emoji_symbols_light, 5);
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener()
		{
			@Override
			public void onTabChanged(String tabId)
			{
				setCurrentCategory(((Integer)mTabIdToCategory.get(tabId)).intValue(), false);
			}
		});
		mTabHost.getTabWidget().setStripEnabled(true);
		
		mEmojiAdapter = new EmojiCategoryAdapter(getActivity(), mListener);
		mEmojiPager = (ViewPager)view.findViewById(R.id.emoji_pager);
		mEmojiPager.setAdapter(mEmojiAdapter);
		mEmojiPager.setOnPageChangeListener(mPageChangeListener);
		mEmojiPager.setOffscreenPageLimit(0);
		
		setCurrentCategory(1, true);
		//setCurrentCategory(getActivity().getSharedPreferences(EMOJI_PREFS_FILENAME, 0).getInt(EMOJI_LAST_CATEGORY_KEY, 1), true);
		
		view.findViewById(R.id.emoji_keyboard_backspace).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mListener.onBackspace();
			}
		});
	}
	
	private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener()
	{
		@Override
		public void onPageSelected(int position)
		{
			setCurrentCategory(position, false);
		}
	};
	
	private void addTab(TabHost tabhost, String s, int i, int j, int k)
	{
		android.widget.TabHost.TabSpec tabspec = tabhost.newTabSpec(s);
		tabspec.setContent(i);
		ImageView imageview = new ImageView(tabhost.getContext());
		imageview.setImageResource(j);
		imageview.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, -2, 1.0F));
		int l = getActivity().getResources().getDimensionPixelOffset(R.dimen.emoji_gallery_tab_image_padding);
		imageview.setPadding(l, l, l, l);
		tabspec.setIndicator(imageview);
		tabhost.addTab(tabspec);
		mTabIdToCategory.put(s, Integer.valueOf(k));
	}
	
	private void setCurrentCategory(int i, boolean flag)
	{
		if(flag || mEmojiPager.getCurrentItem() != i){
			mEmojiPager.setCurrentItem(i, true);
		}
		if(flag || mTabHost.getCurrentTab() != i){
			mTabHost.setCurrentTab(i);
		}
		mCurrentCategory = i;
		//writeCurrentCategoryToPref(mCurrentCategory);
	}
	
	public void onConfigurationChanged(Configuration configuration)
	{
		View view = getView();
		if(view != null){
			int galleryHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.emoji_gallery_height);
			view.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, galleryHeight));
		}
	}
	
	
	private static class EmojiCategoryAdapter extends PagerAdapter
	{
		private Listener mListener;
		
		
		public EmojiCategoryAdapter(Context context, Listener listener)
		{
			mListener = listener;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			View view = LayoutInflater.from(container.getContext())
				.inflate(R.layout.emoji_grid, container, false);
			EmojiGridView grid = (EmojiGridView)view.findViewById(R.id.emoji_gridview);
			final EmojiGridAdapter adapter = EmojiGridAdapter.newInstance(container.getContext(), position);
			grid.setOnEmojiClickedListener(new EmojiGridView.EmojiGridViewListener()
			{
				public void onEmojiClicked(int position)
				{
					int codePoint = adapter.getCodePoint(position);
					mListener.onInsertEmoji(new String(Character.toChars(codePoint)));
				}
			});
			grid.setAdapter(adapter);
			grid.setScrollViewWithNotifier((ScrollViewWithNotifier)view);
			container.addView(view);
			return view;
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return (view == object);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			container.removeView((View)object);
		}
		
		@Override
		public int getCount()
		{
			return 6;
		}
	}
	
	private static class CategoryEmojiGridAdapter extends EmojiGridAdapter
	{
		private static final int[] GROUPS = {
			R.array.emoji_group_faces, 
			R.array.emoji_group_objects,
			R.array.emoji_group_nature,
			R.array.emoji_group_places,
			R.array.emoji_group_symbols
		};
		
		private int[] mCodePoints;
		private int[] mDrawables;
		
		
		public CategoryEmojiGridAdapter(Context context, int categoryNum)
		{
			if(categoryNum > 0){
				StringBuilder sb = new StringBuilder("emoji_u");
				String[] codePoints = context.getResources().getStringArray(GROUPS[categoryNum - 1]);
				mCodePoints = new int[codePoints.length];
				mDrawables  = new int[codePoints.length];
				for(int i = 0; i < codePoints.length; i++){
					mCodePoints[i] = Integer.parseInt(codePoints[i], 16);
					sb.setLength(7);
					sb.append(codePoints[i]);
					mDrawables[i] = context.getResources().getIdentifier(sb.toString(), "drawable", context.getPackageName());
				}
			}
			else{
				mCodePoints = new int[0];
			}
		}
		
		@Override
		protected int getDrawableResId(int position)
		{
			return mDrawables[position];
		}
		
		@Override
		protected int getCodePoint(int position)
		{
			return mCodePoints[position];
		}
		
		@Override
		protected int getEmojiCount()
		{
			return mCodePoints.length;
		}
	}
	
	/*
	private static class CategoryEmojiGridAdapter extends EmojiGridAdapter
	{
		private final Integer mArrayIds[];
		private TypedArray mIcons;
	
		public CategoryEmojiGridAdapter(Context context, int i)
	    {
	        mIcons = null;
	        Integer ainteger[] = new Integer[5];
	        ainteger[0] = Integer.valueOf(R.array.emoji_faces);
	        ainteger[1] = Integer.valueOf(R.array.emoji_objects);
	        ainteger[2] = Integer.valueOf(R.array.emoji_nature);
	        ainteger[3] = Integer.valueOf(R.array.emoji_places);
	        ainteger[4] = Integer.valueOf(R.array.emoji_symbols);
	        mArrayIds = ainteger;
	        if(i < 1 || i > 5){
	            LogUtil.e("Babel", "Expected category between 1 and 5. Got " + i);
	            i = 1;
	        }
	        mIcons = context.getResources().obtainTypedArray(mArrayIds[i - 1].intValue());
	    }
		
	    public int getCodePoint(int i)
	    {
	        return Integer.parseInt(mIcons.getString(i), 16);
	    }
	
	    public int getEmojiCount()
	    {
	        return mIcons.length();
	    }
	
	    
	}
	*/
	
	public static abstract class EmojiGridAdapter extends BaseAdapter
	{
		private int mPosition;
		
		public static EmojiGridAdapter newInstance(Context context, int categoryNum)
		{
			/*EmojiGridAdapter adapter = (categoryNum == 0) ? 
				new RecentEmojiGridAdapter(recentemojibuffer) : 
				new CategoryEmojiGridAdapter(context, categoryNum);*/
			EmojiGridAdapter adapter = new CategoryEmojiGridAdapter(context, categoryNum);
			adapter.mPosition = categoryNum;
			return adapter;
		}
		
		public EmojiGridAdapter()
		{
			mPosition = -1;
		}
		
		protected abstract int getDrawableResId(int position);
		protected abstract int getCodePoint(int position);
		protected abstract int getEmojiCount();
		
		public View getView(int i, View view, ViewGroup viewgroup)
		{
			return null;
		}
		
		public Object getItem(int position)
		{
			//int codePoint = (position < getEmojiCount()) ? getCodePoint(position) : 0x2002;
			// FIXME: convert codePoint to drawableResId
			return Integer.valueOf(getDrawableResId(position));
		}
		
		@Override
		public long getItemId(int position)
		{
			return 0;
		}
		
		@Override
		public boolean areAllItemsEnabled()
		{
			return false;
		}
		
		@Override
		public boolean isEnabled(int position)
		{
			return position < getEmojiCount();
		}
		
		@Override
		public int getCount()
		{
			return getEmojiCount();
		}
	}
}

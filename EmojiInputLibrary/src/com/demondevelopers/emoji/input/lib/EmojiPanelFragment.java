package com.demondevelopers.emoji.input.lib;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


public class EmojiPanelFragment extends Fragment
{
	private static final String TAG = EmojiPanelFragment.class.getSimpleName();
	
	private EmojiPagerAdapter mEmojiPagerAdapter;
	private ViewGroup mTabsHost;
	private ViewPager mViewPager;
	
	private EmojiParser mEmojiParser = new EmojiParser();
	
	private int mEmojiRows, mEmojiCols;
	
	private EditText mEditText;
	
	
	public static EmojiPanelFragment initFragment(FragmentActivity activity, 
		Bundle savedInstanceState, int containerId)
	{
		final EmojiPanelFragment frag;
		if(savedInstanceState == null){
			frag = new EmojiPanelFragment();
			activity.getSupportFragmentManager().beginTransaction()
				.add(containerId, frag, TAG)
				.hide(frag)
				.commit();
		}
		else{
			frag = (EmojiPanelFragment)activity.getSupportFragmentManager()
				.findFragmentById(containerId);
		}
		return frag;
	}
	
	public EditText initEditText(EditText editText)
	{
		editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		editText.setImeActionLabel(
			editText.getContext().getResources().getText(R.string.emoji_actionLabel),
			editText.getContext().getResources().getInteger(R.integer.emoji_actionId));
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				final int emojiActionId = v.getContext().getResources().getInteger(R.integer.emoji_actionId);
				if(actionId == emojiActionId){
					InputMethodManager imm = (InputMethodManager)v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					setEditText((EditText)v);
					((FragmentActivity)v.getContext()).getSupportFragmentManager()
						.beginTransaction()
						.show(EmojiPanelFragment.this)
						.commit();
				}
				
				return true;
			}
		});
		
		return editText;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mEmojiRows = getResources().getInteger(R.integer.emoji_rows);
		mEmojiCols = getResources().getInteger(R.integer.emoji_cols);
		mEmojiPagerAdapter = new EmojiPagerAdapter();
	}
	
	public void setEditText(EditText editText)
	{
		mEditText = editText;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.emoji_frag_panel, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		
		int emojiSize = getResources().getDimensionPixelSize(R.dimen.emoji_size);
		
		mTabsHost = (ViewGroup)view.findViewById(R.id.emoji_tabs_host);
		
		mViewPager = (ViewPager)view.findViewById(R.id.emoji_pager);
		mViewPager.getLayoutParams().height = emojiSize * mEmojiRows;
		mViewPager.setAdapter(mEmojiPagerAdapter);
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mViewPager = null;
	}
	
	private View.OnClickListener mEmojiClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Integer drawableResId = (Integer)v.getTag(R.id.emoji_drawableResId);
			if(drawableResId != null){
				Editable editable = mEditText.getText();
				int start = Selection.getSelectionStart(editable);
				int end = Selection.getSelectionEnd(editable);
				editable.replace(start, end, mEmojiParser.drawableToEmoji(drawableResId.intValue()));
				mEmojiParser.updateEmojiSpans(v.getContext(), editable);
			}
			//hideMe();
		}
	};
	
	private void hideMe()
	{
		getFragmentManager().beginTransaction()
			.hide(EmojiPanelFragment.this)
			.commit();
	}
	
	private class EmojiPagerAdapter extends PagerAdapter
	{
		public EmojiPagerAdapter()
		{
			
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			LayoutInflater inflater = LayoutInflater.from(container.getContext());
			GridView gridView = (GridView)inflater.inflate(R.layout.emoji_grid_view, container, false);
			gridView.setAdapter(new EmojiGridAdapter(mEmojiParser.getEmojiCharLUT(), 
				position * mEmojiRows * mEmojiCols));
			container.addView(gridView);
			return gridView;
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
			return 5;
		}
	}
	
	private class EmojiGridAdapter extends BaseAdapter
	{
		private SparseIntArray mEmoji;
		private int mOffset;
		
		
		public EmojiGridAdapter(SparseIntArray emoji, int offset)
		{
			mEmoji  = emoji;
			mOffset = offset;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null){
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				convertView = inflater.inflate(R.layout.emoji_grid_item, parent, false);
			}
			int drawableResId = getEmojiDrawable(position);
			((ImageView)convertView).setImageResource(drawableResId);
			convertView.setTag(R.id.emoji_drawableResId, Integer.valueOf(drawableResId));
			convertView.setOnClickListener(mEmojiClickListener);
			return convertView;
		}
		
		public int getEmojiDrawable(int position)
		{
			return mEmoji.get(mEmoji.keyAt(mOffset + position));
		}
		
		@Override
		public Integer getItem(int position)
		{
			return Integer.valueOf(getEmojiDrawable(position));
		}
		
		@Override
		public long getItemId(int position)
		{
			return position;
		}
		
		@Override
		public boolean hasStableIds()
		{
			return true;
		}
		
		@Override
		public int getCount()
		{
			return mEmojiRows * mEmojiCols;
		}
	}
}
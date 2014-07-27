package com.demondevelopers.emoji.input.lib;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.util.SparseArrayCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseIntArray;


public class EmojiParser
{
	private static final String TAG = EmojiParser.class.getSimpleName();
	
	// private final SparseArrayCompat<Drawable> mArtifacts;
	
	private final SparseIntArray mEmojiCharLUT = new SparseIntArray();
	private final SparseIntArray mEmojiCharTUL = new SparseIntArray();
	
	
	public EmojiParser()
	{
		Field[] fields = R.drawable.class.getFields();
		for(int i = 0; i < fields.length; i++){
			Field field = fields[i];
			if(!field.getName().startsWith("emoji_u")){
				continue;
			}
			try{
				int drawableResId = ((Integer)field.get(null)).intValue();
				int emojiCodePoint = Integer.parseInt(field.getName().substring(7), 16);
				mEmojiCharLUT.put(emojiCodePoint, drawableResId);
				mEmojiCharTUL.put(drawableResId, emojiCodePoint);
			}
			catch(IllegalAccessException e){
				Log.e(TAG, "EmojiParser", e);
			}
			catch(IllegalArgumentException e){
				Log.e(TAG, "EmojiParser", e);
			}
		}
	}
	
	public SparseIntArray getEmojiCharLUT()
	{
		return mEmojiCharLUT;
	}
	
	public final Editable updateEmojiSpans(Context context, Editable editable)
	{
		for(int start = 0, count = editable.length(); start < count;){
			int codePoint = Character.codePointAt(editable, start);
			int drawableResId = mEmojiCharLUT.get(codePoint);
			int end = start + Character.charCount(codePoint);
			if(drawableResId != 0 && ((ImageSpan[])editable.getSpans(start, end, ImageSpan.class)).length == 0){
				editable.setSpan(new ImageSpan(context, drawableResId), 
					start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			start = end;
		}
		return editable;
	}
	
	public final String drawableToEmoji(int drawableResId)
	{
		int codePoint = mEmojiCharTUL.get(drawableResId);
		if(codePoint != 0){
			return new String(Character.toChars(codePoint));
		}
		return null;
	}
}

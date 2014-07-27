package com.demondevelopers.emoji.input.example;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.widget.EditText;

import com.demondevelopers.emoji.input.lib.EmojiFragment;
import com.demondevelopers.emoji.input.lib.EmojiPanelFragment;


public class MainActivity extends FragmentActivity
{
	private EmojiPanelFragment mEmojiPanel;
	
	private EditText mInput;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mInput = (EditText)findViewById(R.id.input);
		
		//mEmojiPanel = EmojiPanelFragment.initFragment(this, savedInstanceState, R.id.emoji_panel_container);
		
		//mEmojiPanel.initEditText((EditText)findViewById(R.id.input));
		
		if(savedInstanceState == null){
			EmojiFragment frag = new EmojiFragment();
			frag.setListener(mListener);
			getSupportFragmentManager().beginTransaction()
				.add(R.id.emoji_panel_container, frag)
				.commit();
		}
	}
	
	private EmojiFragment.Listener mListener = new EmojiFragment.Listener()
	{
		@Override
		public void onInsertEmoji(CharSequence emojiSequence)
		{
			Editable editable = mInput.getText();
			editable.append(emojiSequence);
		}
		
		@Override
		public void onBackspace()
		{
			Editable editable = mInput.getText();
			int len = editable.length();
			if(len > 0){
				int count = 1;
				if(len > 1){
					int codePoint = Character.codePointAt(editable, len - 2);
					if(Character.charCount(codePoint) == 2){
						count = 2;
					}
				}
				editable.delete(len - count, len);
			}
		}
	};
}

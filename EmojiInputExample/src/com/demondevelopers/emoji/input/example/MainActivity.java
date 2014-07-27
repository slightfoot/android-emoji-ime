package com.demondevelopers.emoji.input.example;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.demondevelopers.emoji.input.lib.EmojiFragment;
import com.demondevelopers.emoji.input.lib.EmojiPanelFragment;


public class MainActivity extends FragmentActivity
{
	private EmojiPanelFragment mEmojiPanel;
	
	private EditText mInput;
    private FrameLayout mEmojiContainer;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mInput = (EditText)findViewById(R.id.input);
		
		//mEmojiPanel = EmojiPanelFragment.initFragment(this, savedInstanceState, R.id.emoji_panel_container);
		
		//mEmojiPanel.initEditText((EditText)findViewById(R.id.input));
        mEmojiContainer = (FrameLayout) findViewById(R.id.emoji_panel_container);

		if(savedInstanceState == null){
			EmojiFragment frag = new EmojiFragment();
			frag.setListener(mListener);
			getSupportFragmentManager().beginTransaction()
				.add(R.id.emoji_panel_container, frag)
				.commit();
		}
        hideEmoji();
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    showEmoji();
                    hideKeyboard();
                    handled = true;
                }
                return handled;
            }
        });
	}

    private void showEmoji(){
        mEmojiContainer.setVisibility(View.VISIBLE);
    }

    private void hideEmoji(){
        mEmojiContainer.setVisibility(View.GONE);
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(
          Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    private void showKeyboard(){

    }

	private EmojiFragment.Listener mListener = new EmojiFragment.Listener()
	{
		@Override
		public void onInsertEmoji(CharSequence emojiSequence)
		{
			Editable editable = mInput.getText();
			editable.replace(mInput.getSelectionStart(), mInput.getSelectionEnd(), emojiSequence);
		}
		
		@Override
		public void onBackspace()
		{
			Editable editable = mInput.getText();
			int selectionLength = mInput.getSelectionEnd() - mInput.getSelectionStart();
            int start;
            if (selectionLength > 0) {
                start = mInput.getSelectionStart();
            } else {
                int count = 1;
                if (mInput.getSelectionStart() > 1) {
                    int codePoint = Character.codePointAt(editable, mInput.getSelectionStart() - 2);
                    if (Character.charCount(codePoint) == 2) {
                        count = 2;
                    }
                }
                start = Math.max(0, mInput.getSelectionStart() - count);
            }
            editable.replace(start, mInput.getSelectionEnd(), "");
		}
	};
}

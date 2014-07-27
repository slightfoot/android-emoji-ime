package com.demondevelopers.emoji.input.example;

import android.app.Application;
import android.content.Context;


public class EmojiApp extends Application
{
	private ToxContext mToxContext;
	
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mToxContext = new ToxContext();
	}
	
	public static EmojiApp get(Context context)
	{
		return (EmojiApp)context.getApplicationContext();
	}
	
	public ToxContext tox()
	{
		return mToxContext;
	}
	
	public static class ToxContext
	{
		public void doSomething()
		{
			
		}
	}
}

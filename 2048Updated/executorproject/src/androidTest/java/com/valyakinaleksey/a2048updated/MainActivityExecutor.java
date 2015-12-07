package com.valyakinaleksey.a2048updated;

import android.app.Activity;
import com.robotium.recorder.executor.Executor;

@SuppressWarnings("rawtypes")
public class MainActivityExecutor extends Executor {

	@SuppressWarnings("unchecked")
	public MainActivityExecutor() throws Exception {
		super((Class<? extends Activity>) Class.forName("com.valyakinaleksey.a2048updated.MainActivity"),  "com.valyakinaleksey.a2048updated.R.id.", new android.R.id(), true, true, "1448967655699");
	}

	public void setUp() throws Exception { 
		super.setUp();
	}
}

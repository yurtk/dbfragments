/*
 * Copyright (C) 2013 Yuriy Tkachenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package db.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * DBAction makes an item in 'Actions' menu for additional service procedures 
 * such data synchronization, data cleaning, etc.
 */
public class DBAction extends Fragment {
	public String title;
	public LinearLayout layout;
	protected Activity parent;
	protected String classname;
	protected SQLiteDatabase conn = G.conn;

	public static DBAction newInstance(String class_name, String title) {
		DBAction f = new DBAction();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("classname", class_name);
		f.setArguments(args);
		return f;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		parent = getActivity();
		title = getArguments().getString("title");
		classname = getArguments().getString("classname");
		layout = new LinearLayout(parent);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(layoutParams);
		layout.setOrientation(LinearLayout.VERTICAL);
		return layout;
	}

}

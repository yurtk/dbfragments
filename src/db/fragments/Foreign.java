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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The Foreign class implements viewing fields from other DBFragment
 * implementations (defined as column.foreign).
 */
public class Foreign extends LinearLayout implements Control,
		DialogItemsFragment.onDlgListClick {

	TextView edit;
	Button button;
	DialogItemsFragment _dialog;
	Long keyid;
	boolean readonly;
	Column.Foreign foreign;
	Activity activity;
	DBFragment parentFragment = null;

	public Foreign(Context context) {
		super(context);
		this.setGravity(Gravity.FILL);
	}

	public static Foreign newInstance(Context parent, boolean readonly,
			Column.Foreign foreign, DBFragment parentFragment) {
		Foreign f = newInstance(parent, readonly, foreign);
		f.parentFragment = parentFragment;
		return f;
	}

	public static Foreign newInstance(Context parent, boolean readonly,
			Column.Foreign foreign) {
		Foreign f = new Foreign(parent);
		f.activity = (Activity) parent;
		f.foreign = foreign;

		f.edit = new TextView(parent);
		f.edit.setTextColor(Color.BLACK);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		f.edit.setLayoutParams(params);

		f.button = new Button(parent, null, android.R.attr.buttonStyleSmall);
		f.button.setText(" .. ");
		if (readonly) {
			f._dialog = null;
		} else {
			f.button.setOnClickListener(f.on_click);
			f._dialog = DialogItemsFragment.newInstance(f, foreign);
			f._dialog.setCancelable(true);
		}
		f.keyid = (long) 0;
		f.addView(f.edit);
		f.addView(f.button);
		f.foreign = foreign;

		return f;
	}

	private View.OnClickListener on_click = new View.OnClickListener() {
		@SuppressLint("CommitTransaction")
		public void onClick(View v) {
			DBFragment fdbfragment = _dialog.dbfragment;
			if (foreign.filter != null) {
			}

			_dialog.search_column_number = fdbfragment.fld.get(foreign.key_fld);
			_dialog.search_column_value = keyid.toString();

			_dialog.parentFragment = parentFragment;

			FragmentTransaction ft = activity.getFragmentManager()
					.beginTransaction();

			_dialog.show(ft, "listdialog");
			//ft.commit();
		}
	};

	@Override
	public void onLstItemSelected(ArrayList<String> selection) {// list dialog
																// fragment
																// interface
		DBFragment fdba = foreign.dbactivity;
		int key_pos = fdba.fld.get(foreign.key_fld);
		int str_pos = fdba.fld.get(foreign.str_fld);
		edit.setText(selection.get(key_pos) + " " + selection.get(str_pos));
	}

	@Override
	public CharSequence getText() {
		return edit.getText();
	}

	@Override
	public void setText(CharSequence text) {
		String itxt;
		if (text == null) {
			itxt = "null";
		} else {
			String[] tl = text.toString().split(" ");
			itxt = (tl.length == 0) ? "null" : tl[0];
		}
		edit.setText(text);
		keyid = (itxt.equals("null") || itxt.equals("")) ? 0 : Long
				.parseLong(itxt);
	}
}

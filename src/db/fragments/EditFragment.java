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
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the secondary fragment, displaying the details of a particular item.
 */
public class EditFragment extends Fragment {

	DBFragment dbfragment;
	boolean visible = false;
	boolean editable = false;

	/**
	 * Create a new instance of EditFragment, initialized to show edit form for
	 * 'classname'.
	 */
	public static EditFragment newInstance(String class_name) {
		EditFragment f = new EditFragment();

		// Supply classname input as an argument.
		Bundle args = new Bundle();
		args.putString("classname", class_name);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		return makeScroller();
	}

	protected View makeScroller() {
		if (getActivity() == null) {
			visible = false;
			return null;
		}
		String class_name = getArguments().getString("classname");
		dbfragment = G.objects.get(class_name);
		visible = true;
		Activity currContext = getActivity();

		ScrollView scroller = new ScrollView(currContext);
		LinearLayout layout = new LinearLayout(currContext);
		layout.setOrientation(LinearLayout.VERTICAL);

		boolean is_choose, is_readonly;
		for (Column col : dbfragment.columns) {
			TextView label = new TextView(currContext);
			label.setText(col.title);
			col.l_ctrl = label;
			is_choose = false;
			is_readonly = false;
			if (col.choose != null)
				is_choose = true;
			if (col.readonly != null)
				is_readonly = col.readonly.getBool(dbfragment);
			if (col.type == G.INTEGER) {
				if (col.foreign != null) {
					col.e_ctrl = Foreign.newInstance(currContext, false,
							col.foreign, dbfragment);
					col.e_ctrl.setEnabled(is_readonly);
				} else {
					if (is_choose) {
						col.e_ctrl = Chooser.newInstance(currContext,
								col.choose);
						col.e_ctrl.setEnabled(is_readonly);
					} else {
						col.e_ctrl = new Edit(currContext);
						((Edit) col.e_ctrl)
								.setInputType(InputType.TYPE_CLASS_NUMBER);
						col.e_ctrl.setEnabled(is_readonly);
						((TextView) col.e_ctrl).setSelectAllOnFocus(true);
					}
				}
			} else if (col.type == G.REAL) {
				col.e_ctrl = new Edit(currContext);
				((Edit) col.e_ctrl).setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_FLAG_DECIMAL);
				col.e_ctrl.setEnabled(is_readonly);
			} else if (col.type == G.DATE) {
				col.e_ctrl = new DateChooser(currContext);
				col.e_ctrl.setEnabled(is_readonly);
			} else { // col["type"] in (TEXT, TIMESTAMP)
				col.e_ctrl = new Edit(currContext);
				col.e_ctrl.setEnabled(is_readonly);
			}
			if (col.show != null && !col.show) {
				continue;
			}
			layout.addView(col.l_ctrl);
			layout.addView((View) col.e_ctrl);
		}

		scroller.addView(layout);
		return scroller;
	}

	@Override
	public void onStart() {
		super.onStart();
		refresh();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (dbfragment != null && !dbfragment.readonly.getBool(dbfragment)
				&& editable && dbfragment.crow_edit) {
			boolean cec = dbfragment.editable.getBool(dbfragment);
			dbfragment.crow_edit = cec;
			if (cec) {
				dbfragment._on_ok();
			} else {
				Toast toast = Toast.makeText(getActivity(),
						G.lstr.get("Changes not allowed"), Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	}

	/**
	 * Refresh data in form controls.
	 */
	public void refresh() {
		editable = false;
		if (!visible) {
			makeScroller();
		}

		String cval;
		Control e_ctrl;
		if (dbfragment == null) {
			return;
		}
		Cursor c = dbfragment.cursor_adapter.getCursor();
		if (c.getCount() == 0) {
			if (getView() != null) {
				getView().setVisibility(View.INVISIBLE);
			}
			return;
		}
		if (getView() != null) {
			getView().setVisibility(View.VISIBLE);
		}
		c.moveToPosition(dbfragment.crow_gui);
		boolean is_readonly;
		int i = 0;
		for (Column col : dbfragment.columns) {
			e_ctrl = col.e_ctrl;
			is_readonly = false;
			if (col.readonly != null) {
				is_readonly = col.readonly.getBool(dbfragment);
			}
			cval = c.getString(++i);
			e_ctrl.setText(cval);
			e_ctrl.setEnabled(!is_readonly);
		}
		editable = true;
	}

}

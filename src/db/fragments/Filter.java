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

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Base class for filter definitions
 */
public class Filter extends LinearLayout {

	DBFragment dbactivity;
	TextView label;
	String fld_name;
	Spinner combo;
	Control e_ctrl = null;

	public Filter(Context context) {
		super(context);
	}

	public static Filter newInstance(Context parent, DBFragment dba, Column col) {
		Filter f = new Filter(parent);
		f.setOrientation(LinearLayout.VERTICAL);
		f.dbactivity = (DBFragment) dba;
		f.label = new TextView(parent);
		f.label.setText(col.title);
		f.fld_name = col.name;
		String[] choices = new String[] { "", "=", "<=", ">=", "<", ">", "like" };
		f.combo = new Spinner(parent);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent,
				android.R.layout.simple_spinner_item, choices);
		f.combo.setAdapter(adapter);

		boolean is_choose = (col.choose != null) ? true : false;

		if (col.type == G.INTEGER) {
			if (col.foreign != null) {
				f.e_ctrl = Foreign.newInstance(parent, false, col.foreign);
			} else {
				if (is_choose) {
					f.e_ctrl = Chooser.newInstance(parent, col.choose);
				} else {
					f.e_ctrl = new Edit(parent);
					((TextView) f.e_ctrl)
							.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				}
			}
		} else if (col.type == G.REAL) {
			f.e_ctrl = new Edit(parent);
			((TextView) f.e_ctrl).setRawInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		} else if (col.type == G.DATE) {
			f.e_ctrl = new DateChooser(parent);
		} else
			f.e_ctrl = new Edit(parent);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.weight = 1;

		((View) f.e_ctrl).setLayoutParams(params);

		f.addView(f.label);
		LinearLayout layout = new LinearLayout(parent);
		layout.addView(f.combo);
		layout.addView((View) f.e_ctrl);
		f.addView(layout);
		return f;
	}

	/**
	 * flst = {title, name, expression, value}
	 */
	public void set_flist(String[] flst) {
		label.setText(flst[0]);
		fld_name = flst[1];
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adap = (ArrayAdapter<String>) combo.getAdapter();
		int pos = adap.getPosition(flst[2]);
		combo.setSelection(pos);
		if (e_ctrl instanceof Foreign) {
			((Foreign) e_ctrl).setText(flst[3]
					+ " "
					+ ((Foreign) e_ctrl).foreign.dbactivity.get_by_value(
							((Foreign) e_ctrl).foreign.key_fld, flst[3],
							((Foreign) e_ctrl).foreign.str_fld));
		} else {
			e_ctrl.setText(flst[3]);
		}
	}

	public String[] get_flist() {
		String v = e_ctrl.getText().toString();
		int lv = v.length();
		if (e_ctrl instanceof Foreign && lv > 0) {
			v = v.split(" ")[0];
		}
		return new String[] { (String) label.getText(), fld_name,
				combo.getSelectedItem().toString(), v };
	}
}

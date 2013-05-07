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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * The chooser control.
 */
public class Chooser extends Spinner implements Control {

	public Chooser(Context context) {
		super(context);
	}

	public static Chooser newInstance(Context parent, String[] values) {
		Chooser choose = new Chooser(parent);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(parent,
				android.R.layout.simple_spinner_item, values);
		choose.setAdapter(adapter);
		return choose;
	}

	@Override
	public CharSequence getText() {
		return String.valueOf(getSelectedItemPosition());
	}

	@Override
	public void setText(CharSequence text) {
		int sel = 0;
		try {
			sel = Integer.parseInt(text.toString());
		} catch (NumberFormatException e) {
			sel = 0;
		}
		setSelection(sel);
	}
}

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

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The date chooser control.
 */
public class DateChooser extends LinearLayout implements Control {

	TextView edit;
	Button button;
	boolean readonly;
	Activity activity;

	private int mYear;
	private int mMonth;
	private int mDay;

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
		button.setEnabled(!readonly);
	}

	public DateChooser(Context context) {
		super(context);
		activity = (Activity) context;
		edit = new TextView(context);
		edit.setTextColor(Color.BLACK);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		edit.setLayoutParams(params);

		button = new Button(context, null, android.R.attr.buttonStyleSmall);
		button.setText(" .. ");
		button.setOnClickListener(on_click);
		addView(edit);
		addView(button);
	}

	private void updateDisplay() {
		this.edit.setText(new StringBuilder()
				// Month is 0-based so add 1
				.append(mYear).append("-")
				.append(String.format("%02d", mMonth + 1)).append("-")
				.append(String.format("%02d", mDay)));
	}

	private View.OnClickListener on_click = new View.OnClickListener() {
		public void onClick(View v) {
			DialogFragment newFragment = new DatePickerFragment();
			newFragment.show(activity.getFragmentManager(), "datePicker");
		}
	};

	@Override
	public CharSequence getText() {
		return edit.getText();
	}

	@Override
	public void setText(CharSequence text) {
		if (text.length() < 10) {
			text = "2000-01-01";
		}
		edit.setText(text);
	}

	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String[] dmy = ((String) edit.getText()).split("\\-");
			mYear = Integer.parseInt(dmy[0]);
			mMonth = Integer.parseInt(dmy[1]) - 1;
			mDay = Integer.parseInt(dmy[2]);
			return new DatePickerDialog(activity, this, mYear, mMonth, mDay);
		}

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			updateDisplay();
		}
	}

}

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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * The simple edit control.
 */
public class Edit extends EditText implements Control {

	private boolean changed = false;
	private boolean changedBySetText = false;

	private TextWatcher textWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			changed = changedBySetText ? false: true;
			changedBySetText = false;
			//changed = isInputMethodTarget() ? true : false;
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}
	};

	public Edit(Context context) {
		super(context);
		// setImeOptions(EditorInfo.IME_ACTION_DONE);
		addTextChangedListener(textWatcher);
	}

	@Override
	public Editable getText() {
		return super.getText();
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		changedBySetText = true;
	}

	@Override
	public boolean isChanged() {
		return changed;
	}

}

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

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Show filter settings in DialogFragment.
 */
public class FilterFragment extends DialogFragment {

	DBFragment dbfragment;

	/**
	 * Create a new instance of DialogFragment, initialized to show edit form
	 * for 'classname'.
	 */
	public static FilterFragment newInstance(String class_name) {
		FilterFragment f = new FilterFragment();

		// Supply classname input as an argument.
		Bundle args = new Bundle();
		args.putString("classname", class_name);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		String class_name = getArguments().getString("classname");
		dbfragment = G.objects.get(class_name);
		dbfragment._filter_dlg = this;

		getDialog().setTitle(G.lstr.get("Filter") + ": " + dbfragment.title);

		ScrollView scroller = new ScrollView(getActivity());
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);

		for (Column col : dbfragment.columns) {
			if (col.filter != null) {
				col.f_ctrl = Filter.newInstance(getActivity(), dbfragment, col);
				layout.addView(col.f_ctrl);
				for (String[] fs : dbfragment.filter_lst) {
					if (fs[1].equals(col.name)) {
						col.f_ctrl.set_flist(fs);
					}
				}
			}
		}
		scroller.addView(layout);
		return scroller;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		dbfragment.filter_lst.clear();
		for (Column col : dbfragment.columns) {
			if (col.f_ctrl != null) {
				dbfragment.filter_lst.add(col.f_ctrl.get_flist());
			}
		}
		dbfragment.refresh_data();

		dbfragment.cursor_adapter.changeCursor(dbfragment.cursor);
		dbfragment.cursor_adapter.notifyDataSetChanged();

		FragmentTransaction ft = getActivity().getFragmentManager()
				.beginTransaction();
		ft.remove(this);
		ft.commit();

	}

}

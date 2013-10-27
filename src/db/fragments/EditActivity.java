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
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This is a secondary activity to show what the user has selected when the
 * screen is not large enough to show it all in one activity.
 */
public class EditActivity extends Activity {

	EditFragment edfragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
		}

		if (savedInstanceState == null) {
			// During initial setup plug in the edfragment fragment.
			edfragment = new EditFragment();
			edfragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, edfragment).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return false;
		}
		DBFragment dbfragment = edfragment.dbfragment;
		setTitle(edfragment.dbfragment.title);

		int idOffset = DBFragment.ID_MENU_DETAILS_FIRST;
		if (dbfragment.details != null) {
			for (int i=0; i < dbfragment.details.length; i++) {
				menu.add(0, idOffset++, Menu.NONE, dbfragment.details[i][2]);
			}
			/*menu.add(0, DBFragment.ID_MENU_DETAIL, Menu.NONE,
					G.lstr.get("Detail"));*/
		}

		idOffset = DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST;
		if (dbfragment.actions != null) {
			for (String title : dbfragment.actions.values()) {
				menu.add(0, idOffset++, Menu.NONE, title);
			}
		}

		menu.add(0, DBFragment.ID_MENU_DELETE, Menu.NONE, G.lstr.get("Delete"));

		MenuItem menuItem = menu.findItem(DBFragment.ID_MENU_DELETE);
		menuItem.setEnabled(dbfragment.menu_enabled
				.get(DBFragment.ID_MENU_DELETE));

		int ms = menu.size();
		for (int i = 0; i < ms; i++) {
			menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		DBFragment dbfragment = edfragment.dbfragment;

		if (dbfragment != null && !dbfragment.readonly.getBool(dbfragment)
				&& edfragment.editable && dbfragment.crow_edit) {
			dbfragment._on_ok();
		}

		int id = item.getItemId();
		switch (id) {
		/*case DBFragment.ID_MENU_DETAIL:

			Cursor c = dbfragment.cursor_adapter.getCursor();
			c.moveToPosition(dbfragment.crow_gui);
			String title = c.getString(dbfragment.columns
					.indexOf(dbfragment.listfields[0]));

			intent = new Intent();
			intent.setClass(dbfragment.getActivity(), DetailActivity.class);
			intent.putExtra("className", dbfragment.detailFragments.getClass()
					.getSimpleName());
			intent.putExtra("parentClassName", dbfragment.getClass()
					.getSimpleName());
			intent.putExtra("rowtitle", title);
			startActivity(intent);
			return true;*/
		case DBFragment.ID_MENU_DELETE:
			dbfragment._on_delete();
			this.finish();
			return true;
		default:
			if (id >= DBFragment.ID_MENU_DETAILS_FIRST && id < DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST) {
				int indx = id - DBFragment.ID_MENU_DETAILS_FIRST;
				Cursor c = dbfragment.cursor_adapter.getCursor();
				c.moveToPosition(dbfragment.crow_gui);
				String title = c.getString(dbfragment.columns
						.indexOf(dbfragment.listfields[0]));

				intent = new Intent();
				intent.setClass(dbfragment.getActivity(), DetailActivity.class);
				intent.putExtra("className", dbfragment.detailFragments[indx].getClass()
						.getSimpleName());
				intent.putExtra("parentClassName", dbfragment.getClass()
						.getSimpleName());
				intent.putExtra("rowtitle", title);
				startActivity(intent);
				return true;
			}
			else if (id >= DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST) {
				int idOffset = DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST;
				intent = new Intent();
				String className = (String) dbfragment.actions.keySet()
						.toArray()[id - idOffset];
				intent.setClass(dbfragment.getActivity(), ActionActivity.class);
				intent.putExtra("classname", className);
				intent.putExtra("title", item.getTitle());
				startActivity(intent);
				return true;
			} else {
				return super.onContextItemSelected(item);
			}
		}
	}

}

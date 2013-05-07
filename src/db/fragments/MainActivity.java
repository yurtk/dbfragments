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
import java.util.List;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import db.fragments.R;

/**
 * The main application activity.
 */
public class MainActivity extends Activity {

	DBFragment current_fragment = null;
	int lastsel = 0;
	ArrayAdapter<CharSequence> adapter;
	int lastOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_layout);

		for (String oName : G.initorder) {
			G.objects.get(oName).__init__();
		}
		for (String oName : G.initorder) {
			DBFragment m = G.objects.get(oName);
			if (m.detail != null) {
				DBFragment d = G.objects.get(m.detail[0]);
				String f = m.detail[1];
				set_master_detail(m, d, f);
				d.makeSql();
				d.refresh_data();
			}

			if (G.menuorder.contains(oName)) {
				m.makeSql();
				m.refresh_data();
				m.init();
			}
		}

		List<String> listItems = new ArrayList<String>();
		for (String oName : G.menuorder) {
			listItems.add(G.objects.get(oName).title);
		}
		String[] li = listItems.toArray(new String[listItems.size()]);
		adapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item, li);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Restore the last state for selected position.
		if (savedInstanceState != null) {
			lastsel = savedInstanceState.getInt("curChoice", 0);
			lastOrientation = savedInstanceState.getInt("lastOrientation");
			current_fragment = G.objects.get(G.menuorder.get(lastsel));
		} else {
			lastOrientation = getResources().getConfiguration().orientation;
			current_fragment = G.objects.get(G.menuorder.get(0));
		}

		String oname = G.menuorder.get(lastsel);
		int currentOrientation = getResources().getConfiguration().orientation;
		if (lastOrientation != currentOrientation) {
			current_fragment = (DBFragment) getFragmentManager()
					.findFragmentByTag(oname);
			lastOrientation = currentOrientation;
		} else {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			current_fragment = G.objects.get(oname);
			ft.replace(R.id.emblist, current_fragment, oname);
			ft.commit();
		}
		setTitle(current_fragment.title);

	}

	private void set_master_detail(DBFragment master, DBFragment detail,
			String field) {
		master.details = detail;
		detail.masterform = master;
		detail.masterfield = field;
		// Master table ROWID as default value for details
		detail.setColumn(field, "defaultValue", new G.Lambda() {
			public int getInt(DBFragment self) {
				return self.get_master_key();
			}
		});
		detail.filter_lst.add(new String[] { "detail", field, "=",
				Long.toString(master.crow_db) });
		detail.menu_enabled.put(DBFragment.ID_MENU_FILTER, false);
		boolean is_master = detail._master_check();
		detail.menu_enabled.put(DBFragment.ID_MENU_ADD, is_master);
		detail.menu_enabled.put(DBFragment.ID_MENU_DELETE, is_master);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", lastsel);
		outState.putInt("lastOrientation", lastOrientation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu dbaMenu = menu.addSubMenu(0, DBFragment.ID_MENU_MENU,
				Menu.NONE, G.lstr.get("Menu"));
		int id = DBFragment.ID_MENU_MENU_FIRST;
		for (String title : G.menuorder) {
			dbaMenu.add(0, id++, Menu.NONE, G.objects.get(title).title);
		}

		menu.add(1, DBFragment.ID_MENU_ADD, Menu.NONE, G.lstr.get("Add"));

		SubMenu actMenu = menu.addSubMenu(2, DBFragment.ID_MENU_ACTIONS,
				Menu.NONE, G.lstr.get("Actions"));

		id = DBFragment.ID_MENU_ACTIONS_FIRST;
		for (String title : G.actions.values()) {
			actMenu.add(2, id++, Menu.NONE, title);
		}

		// Calculate total
		String label_total = G.lstr.get("Sum: ");
		menu.add(3, DBFragment.ID_MENU_TOTAL, Menu.NONE, label_total);
		menu.add(4, DBFragment.ID_MENU_FILTER, Menu.NONE, G.lstr.get("Filter"));

		// Set visibility
		menu.findItem(DBFragment.ID_MENU_MENU).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.findItem(DBFragment.ID_MENU_ADD).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.findItem(DBFragment.ID_MENU_ACTIONS).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.findItem(DBFragment.ID_MENU_TOTAL).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.findItem(DBFragment.ID_MENU_FILTER).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM);

		if (lastOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (current_fragment.detail != null) {
				menu.add(5, DBFragment.ID_MENU_DETAIL, Menu.NONE,
						G.lstr.get("Detail")).setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM);
			}

			if (current_fragment.actions != null) {
				id = DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST;
				for (String title : current_fragment.actions.values()) {
					menu.add(5, id++, Menu.NONE, title).setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM);
				}
			}

			menu.add(5, DBFragment.ID_MENU_DELETE, Menu.NONE,
					G.lstr.get("Delete")).setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);

			MenuItem menuItem = menu.findItem(DBFragment.ID_MENU_DELETE);
			menuItem.setEnabled(current_fragment.menu_enabled
					.get(DBFragment.ID_MENU_DELETE));

		}

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.findItem(DBFragment.ID_MENU_ADD);
		menuItem.setEnabled(current_fragment.menu_enabled
				.get(DBFragment.ID_MENU_ADD));
		menuItem = menu.findItem(DBFragment.ID_MENU_FILTER);
		menuItem.setEnabled(current_fragment.menu_enabled
				.get(DBFragment.ID_MENU_FILTER));
		// Calculate total
		menuItem = menu.findItem(DBFragment.ID_MENU_TOTAL);
		String label_total = G.lstr.get("Sum: ");
		if (current_fragment.total != null)
			label_total += current_fragment.sum_col(current_fragment.total,
					false, false);
		menuItem.setTitle(label_total);

		if (lastOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			menuItem = menu.findItem(DBFragment.ID_MENU_DELETE);
			menuItem.setEnabled(current_fragment.menu_enabled
					.get(DBFragment.ID_MENU_DELETE));
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// Get offset for actions ids
		if (id >= DBFragment.ID_MENU_MENU_FIRST
				&& id < DBFragment.ID_MENU_ACTIONS_FIRST) {
			// Start selected menu item
			int pos = id - DBFragment.ID_MENU_MENU_FIRST;
			if (lastsel == pos) {
				return false;
			}
			String oname = G.menuorder.get(pos);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			DBFragment fselect = G.objects.get(oname);
			ft.replace(R.id.emblist, fselect, oname);
			ft.commit();
			lastsel = pos;
			current_fragment = fselect;
			setTitle(fselect.title);
		} else if (id >= DBFragment.ID_MENU_ACTIONS_FIRST
				&& id < DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST) {
			// Action start
			Intent intent = new Intent();
			String className = (String) G.actions.keySet().toArray()[id
					- DBFragment.ID_MENU_ACTIONS_FIRST];
			intent.setClass(this, ActionActivity.class);
			intent.putExtra("classname", className);
			intent.putExtra("title", item.getTitle());
			startActivity(intent);

		} else if (id >= DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST) {
			// Local action start
			Intent intent = new Intent();
			String className = (String) current_fragment.actions.keySet()
					.toArray()[id - DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST];
			intent.setClass(this, ActionActivity.class);
			intent.putExtra("classname", className);
			intent.putExtra("title", item.getTitle());
			startActivity(intent);

		} else if (id == DBFragment.ID_MENU_FILTER) {
			// Filter open
			current_fragment._on_filter();
		} else if (id == DBFragment.ID_MENU_ADD) {
			// New record
			current_fragment._on_add();
		} else if (id == DBFragment.ID_MENU_DETAIL) {
			// Details
			Intent intent = new Intent();
			intent.setClass(this, DetailActivity.class);
			Cursor c = current_fragment.cursor_adapter.getCursor();
			String retValue;
			if (c.getCount() > 0) {
				c.moveToPosition(current_fragment.crow_gui);
				retValue = c.getString(c.getColumnIndex(current_fragment.listfields[0]));
			} else {
				return false;
			}
			intent.putExtra("className", current_fragment.details.getClass()
					.getSimpleName());
			intent.putExtra("parentClassName", current_fragment.getClass()
					.getSimpleName());
			intent.putExtra("rowtitle", retValue);
			startActivity(intent);
		} else if (id == DBFragment.ID_MENU_DELETE) {
			// Delete
			current_fragment._on_delete();
		}

		return super.onOptionsItemSelected(item);
	}

}

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
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Spinner;
import android.widget.TextView;
import db.fragments.R;

/**
 * If DBFragment.details is not null, the DetailActivity implements details view.
 */
public class DetailActivity extends Activity {

	DBFragment current_fragment = null;
	Spinner spinner_menu;
	String oname; // current DBFragment tag (class name)
	int lastOrientation;
	DBFragment parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detail_layout);
		oname = getIntent().getExtras().getString("className");
		String parentOName = getIntent().getExtras().getString(
				"parentClassName");
		String rowTitle = getIntent().getExtras().getString("rowtitle");
		current_fragment = G.objects.get(oname);
		parent = G.objects.get(parentOName);
		TextView viewTitle = (TextView) findViewById(R.id.textViewTitle);
		viewTitle.setText(rowTitle);
		this.setTitle(current_fragment.title);

		set_master_detail(parent, current_fragment); // , parent.details[1]);

		if (savedInstanceState != null) {
			lastOrientation = savedInstanceState.getInt("lastOrientation");
		} else {
			lastOrientation = getResources().getConfiguration().orientation;
		}

		int currentOrientation = getResources().getConfiguration().orientation;
		if (lastOrientation != currentOrientation) {
			current_fragment = (DBFragment) getFragmentManager()
					.findFragmentByTag(oname);
			lastOrientation = currentOrientation;
		} else {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.emblist, current_fragment, oname);
			ft.commit();
		}
	}

	private void set_master_detail(DBFragment master, DBFragment detail) {
			//String field) 
		detail.masterform = master;
		for (int i=0; i < master.details.length; i++) {
			if (master.details[i][2].equals(oname)) {
				detail.masterfield = master.details[i][1];
			}
		}
		// Master table ROWID as default value for details
		detail.setColumn(detail.masterfield, "defaultValue", new G.Lambda() {
			public String getString(DBFragment self) {
				return self.get_master_key().toString();
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("lastOrientation", lastOrientation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		int id = DBFragment.ID_MENU_ACTIONS_FIRST;
		menu.add(0, DBFragment.ID_MENU_ADD, Menu.NONE, G.lstr.get("Add"));

		SubMenu actMenu = menu.addSubMenu(0, DBFragment.ID_MENU_ACTIONS,
				Menu.NONE, G.lstr.get("Actions"));

		// Continue previous id count.
		for (String title : G.actions.values()) {
			actMenu.add(1, id++, Menu.NONE, title);
		}

		// Calculate total
		String label_total = G.lstr.get("Sum: ");
		menu.add(2, DBFragment.ID_MENU_TOTAL, Menu.NONE, label_total);
		menu.add(3, DBFragment.ID_MENU_FILTER, Menu.NONE, G.lstr.get("Filter"));

		menu.findItem(DBFragment.ID_MENU_ADD).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.findItem(DBFragment.ID_MENU_ACTIONS).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.findItem(DBFragment.ID_MENU_TOTAL).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_IF_ROOM);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.findItem(DBFragment.ID_MENU_ADD);
		menuItem.setEnabled(parent.menu_enabled.get(DBFragment.ID_MENU_ADD));
		// Calculate total
		menuItem = menu.findItem(DBFragment.ID_MENU_TOTAL);
		String label_total = G.lstr.get("Sum: ");
		if (current_fragment.total != null)
			label_total += current_fragment.sum_col(current_fragment.total,
					false, false);
		menuItem.setTitle(label_total);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// Get offset for actions ids
		int idOffset = DBFragment.ID_MENU_ACTIONS_FIRST;
		if (id >= idOffset) {
			Intent intent = new Intent();
			String className = (String) G.actions.keySet().toArray()[id
					- idOffset];
			intent.setClass(this, ActionActivity.class);
			intent.putExtra("classname", className);
			intent.putExtra("title", item.getTitle());
			startActivity(intent);
		} else if (id == DBFragment.ID_MENU_ADD) {
			// Add new record
			current_fragment._on_add();
		}

		return super.onOptionsItemSelected(item);
	}

}

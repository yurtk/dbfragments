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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import db.fragments.R;

/**
 * The base class for building user defined classes which help to show and edit 
 * user's data. It consists of database table definition, database fields 
 * definition and their GUI controls description.<br>
 * Class member descriptors:<br>
 * <ul>
 * <li>[Configuration] - this field is visible both in class definitions and 
 *  runtime</li>
 * <li>[Runtime] - runtime visible field</li>
 * <li>[Runtime readonly] - this runtime visible field will be private in future 
 *  and be accessed with getter</li>
 * </ul>
 */
public abstract class DBFragment extends ListFragment {
	boolean mDualPane;
	int lastsel = 0;

	/**
	 * [Configuration] The database table name.
	 */
	public String tablename = "";
	
	/**
	 * [Runtime readonly] SQL expression in use.
	 */
	public String sql = "";

	/**
	 * [Configuration] The title of activity for this DBFragment.
	 */
	public String title = "";
	
	/**
	 * [Configuration] The list of column definitions.
	 */
	public List<Column> columns = new ArrayList<Column>();

	/**
	 * [Configuration] 'Lambda' function that returns <i>true</i> if data in 
	 * this class are readonly (default: <i>false</i>).
	 */
	public G.Lambda readonly = G.BooleanFalse;
	
	public G.Lambda editable = G.BooleanTrue;

	/**
	 * [Configuration] List of lists of strings, used to fill up database table 
	 * directly after the table creation in G.renewstruc() function.
	 */
	public List<List<String>> initvalues = null;

	/**
	 * [Configuration] String array of two elements {<i>DBFragment_class</i>, 
	 * <i>field_name_with_key_from_DBFragment_class</i>}. <i>'detail'</i> allows
	 * binding of different DBFragments in master-detail way. The detail 
	 * DBFragment will always be filtered by 
	 * <i>'field_name_with_key_from_DBFragment_class</i> value which is currently 
	 * selected in parent DBFragment.
	 */
	public String[] detail;

	/**
	 * [Configuration] Database field name for total sum calculating.
	 */
	public String total = null;

	/**
	 * [Configuration] 'Lambda' function that returs array of 2-items array 
	 * {<i>field_name</i>, <i>asc|desc</i>}, where <i>field_name</i> is a 
	 * database table field name, <i>order_type</i> is either 'asc' or 'desc'.
	 */
	public String[][] orderby() {
		return new String[][] { new String[] { "ROWID", "asc" } };
	}

	public DBFragment details;
	
	/**
	 * [Runtime readonly] Master frame of detail DBFragment instance in 
	 * master-detail related frames.
	 */
	protected DBFragment masterform;
	
	protected String masterfield;
	public ArrayList<String[]> filter_lst;
	protected FilterFragment _filter_dlg;
	
	/**
	 * [Runtime readonly] Current row index in current DBFragment list view.
	 */
	protected int crow_gui = 0;
	
	/**
	 * [Runtime readonly] Current row index (ROWID) in database table.
	 */	
	public int crow_db = 0;

	// Field index by field name
	protected Map<String, Integer> fld = new HashMap<String, Integer>();

	public Map<String, Integer> get_fld() {
		return fld;
	}

	public SQLiteCursor cursor;
	public SimpleCursorAdapter cursor_adapter;

	/**
	 * [Runtime readonly] EditFragment object for current DBFragment instance.
	 */	
	public EditFragment editform;
	
	/**
	 * [Runtime readonly] <i>true</i> if the current record is in editable state.
	 */	
	public boolean crow_edit;

	// Menu IDs
	public static final int ID_MENU_MENU = 1;
	public static final int ID_MENU_ACTIONS = 2;
	public static final int ID_MENU_TOTAL = 3;
	public static final int ID_MENU_FILTER = 4;
	public static final int ID_MENU_ADD = 5;
	public static final int ID_MENU_DELETE = 6;
	public static final int ID_MENU_DETAIL = 7;
	public static final int ID_MENU_SEPARATOR = 8;
	// First element menu IDs
	public static final int ID_MENU_MENU_FIRST = 100;
	public static final int ID_MENU_ACTIONS_FIRST = 200;
	public static final int ID_MENU_ACTIONS_LOCAL_FIRST = 300;

	// Menu enable/disable map by menu id
	protected SparseBooleanArray menu_enabled = new SparseBooleanArray();

	protected SimpleCursorAdapter.ViewBinder viewbinder = null;
	public Map<String, String> actions = new HashMap<String, String>();

	// Fields shown in ListView
	public String[] listfields;
	
	// Restore list selection helper variables
	int restoreIndex, restoreTop;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// All menus enabled
		menu_enabled.put(ID_MENU_FILTER, true);
		menu_enabled.put(ID_MENU_ADD, true);
		menu_enabled.put(ID_MENU_DELETE, true);
		menu_enabled.put(ID_MENU_TOTAL, true);

		makeSql();

		if (savedInstanceState != null) {
			fld = (Map<String, Integer>) savedInstanceState
					.getSerializable("fld");
			filter_lst = (ArrayList<String[]>) savedInstanceState
					.getSerializable("filter_lst");
		}

		// crow_gui - int - GUI table row
		crow_gui = 0;
		// crow_db - int - DB table key
		crow_db = 0;
		// crow_edit - bool - is edited
		crow_edit = false;

		// Initial cursor
		cursor = (SQLiteCursor) G.conn.rawQuery("SELECT 1 FROM sqlite_master",
				null);

		// Replace itself in object collection
		G.objects.put(this.getClass().getSimpleName(), this);
	}

	protected void makeSql() {
		String s = String.format("SELECT %s.ROWID _id,", tablename);
		for (Column col : columns) {
			if (col.foreign != null) {
				s += String.format("%s.%s||' '||%s.%s %s,", tablename,
						col.name, col.foreign.dbactivity.tablename,
						col.foreign.str_fld, col.name);
			} else {
				s += String.format("%s.%s %s,", tablename, col.name, col.name);
			}
		}
		s = s.substring(0, s.length() - 1); // remove trailing comma
		s += String.format(" FROM %s", tablename);

		// For 'foreign'
		for (Column col : columns) {
			if (col.foreign != null) {
				s += String.format(" LEFT JOIN %s ON %s.%s=%s.%s",
						col.foreign.dbactivity.tablename, tablename, col.name,
						col.foreign.dbactivity.tablename, col.foreign.key_fld);
				if (col.foreign.extra_keys != null) {
					for (String[] ek : col.foreign.extra_keys) {
						s += String.format(" AND %s.%s=%s.%s", tablename,
								ek[0], col.foreign.dbactivity.tablename, ek[1]);
					}
				}
			}
		}
		s += " WHERE 1=1";
		sql = s;
	}

	@Override
	public void onDestroy() {
		cursor.close();
		super.onDestroy();
	}

	/**
	 * Set initial values for the class members. Called from DBApplication
	 */
	protected void __init__() {
		int i = 0;
		for (Column col : columns) {
			fld.put(col.name, ++i); // assume ROWID is the first column
		}
		filter_lst = new ArrayList<String[]>();
		for (Column col : columns) {
			if (col.filter != null) {
				ArrayList<String> f = col.filter.getArrayListOfString(this);
				if (f == null) {
					filter_lst
							.add(new String[] { col.title, col.name, "", "" });
				} else {
					filter_lst.add(new String[] { col.title, col.name,
							f.get(0), f.get(1) });
				}
			}
		}
	};

	public void init() {
	};

	/*
	 * Public event handlers
	 */
	
	/**
	 * On confirm changes after data edit.
	 */
	public void on_ok() {
	};

	/**
	 * On cancel changes after data edit.
	 */
	public void on_cancel() {
	};

	/**
	 * On add new record.
	 */
	public void on_add() {
	};

	/**
	 * On delete record.
	 */
	public void on_delete() {
	};

	/*
	 * End of public event handlers
	 */

	/**
	 * Return map of <i>field name : value</i> pairs from selected row.
	 * @return <i>field name : value</i> map
	 */
	public HashMap<String, String> get_selected_dict() {
		HashMap<String, String> res = new HashMap<String, String>();
		SQLiteCursor c = (SQLiteCursor) cursor_adapter.getCursor();
		if (c.moveToPosition(crow_gui)) {
			Set<String> fields = fld.keySet();
			for (String f : fields) {
				res.put(f, c.getString(fld.get(f)));
			}
		}
		return res;
	}

	/**
	 * Search all records by field <i>by_field</i> for value <i>by_value</i> in 
	 * dictionary <i>by_field_value_dict</i> {<i>by_field</i> : <i>by_value</i>} 
	 * and returns found record value of field <i>get_field</i>
	 * @param by_field_value_dict Map of strings {<i>by_field</i> : <i>by_value</i>}
	 * @param get_field field name to get value
	 * @return Found value.
	 */
	public String get_by_values(Map<String, String> by_field_value_dict,
			String get_field) {
		int fpos = fld.get(get_field);
		String s = sql;
		Set<String> fields = by_field_value_dict.keySet();
		for (String k : fields) {
			s += String.format(" and %s = ?", k);
		}
		Cursor c = cursor.getDatabase().rawQuery(s,
				by_field_value_dict.values().toArray(new String[0]));
		String res = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			res = c.getString(fpos);
		}
		c.close();
		return res;
	}

	/**
	 * Search all records by field <i>by_field</i> for value <i>by_value</i> and return
	 * the found record value of the <i>get_field</i> field.
	 * @param by_field field name to search
	 * @param by_value value to search
	 * @param get_field field name to get value
	 * @return Found value.
	 */
	public String get_by_value(String by_field, String by_value,
			String get_field) {
		String res = null;
		int fpos = fld.get(get_field);
		Cursor c = cursor.getDatabase().rawQuery(
				String.format(sql + " and %s.%s = ?", tablename, by_field),
				new String[] { by_value });
		if (c.getCount() > 0) {
			c.moveToFirst();
			res = c.getString(fpos);
		}
		c.close();
		return res;
	}

	/**
	 * Returns value from selected table row for field name <field>
	 * @param field
	 * @return
	 */
	public String get_field_value(String field) {
		String res = null;
		int fpos = fld.get(field);
		String s = String.format(sql + " and %s.ROWID = ?", tablename);

		Cursor c = ((SQLiteCursor) cursor_adapter.getCursor()).getDatabase()
				.rawQuery(s, new String[] { String.valueOf(crow_db) });

		if (c.getCount() > 0) {
			c.moveToFirst();
			res = c.getString(fpos);
		}
		c.close();

		Column col = columns.get(fpos - 1);
		if (col.foreign != null && res != null) {
			String[] tl = res.split(" ");
			res = (tl.length == 0) ? "null" : tl[0];
		}

		return res;
	}

	/**
	 * Set value in selected table row for the field name <field>
	 */
	public void set_field_value(String field, String value) {
		ContentValues vals = new ContentValues();
		vals.put(field, value);
		cursor.getDatabase().update(tablename, vals, "rowid=?",
				new String[] { String.valueOf(crow_db) });
	}

	/**
	 * Set value in selected table row for the field name <field> and requery
	 * cursor
	 */
	public void set_field_value(String field, String value, boolean requery) {
		set_field_value(field, value);
		if (requery) {
			cursor.requery();
		}
	}

	/**
	 * Set the column dictionary parameter <i>par</i> to value <i>val</i> for 
	 * field <i>fname</i>.
	 * @param fname field name
	 * @param par column parameter name
	 * @param val value to set
	 */
	protected void setColumn(String fname, String par, Object val) {
		Column col = columns.get(fld.get(fname) - 1);
		Field fld;
		try {
			fld = Column.class.getField(par);
			fld.set(col, val);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return value from EditFragment Control for field <i>fname</i>
	 * @param fname field name.
	 * @return Value from Control for given field name. 
	 */
	public String get_edit_value(String fname) {
		String res = null;
		Column col = columns.get(fld.get(fname) - 1);
		if (col.e_ctrl == null) {
			return res;
		}
		res = col.e_ctrl.getText().toString();
		if (col.foreign != null) {
			String[] tl = res.split(" ");
			res = (tl.length == 0) ? "null" : tl[0];
		}
		return res;
	}

	/**
	 * Return list of values for ROWID=<i>id</i>
	 * @param id
	 * @return
	 */
	public ArrayList<String> get_values_by_id(int id) {
		ArrayList<String> res = new ArrayList<String>();
		String s = String.format(sql + " and %s.ROWID = ?", tablename);
		Cursor cur = cursor.getDatabase().rawQuery(s,
				new String[] { String.valueOf(id) });
		if (cur.getCount() > 0) {
			cur.moveToFirst();
			int cc = cur.getColumnCount();
			for (int i = 0; i < cc; i++) {
				res.add(cur.getString(i));
			}
		}
		cur.close();
		return res;
	}

	/**
	 * Returns master DBFragment object's current database table ROWID value from
	 * detail DBFragment object
	 * @return Currently selected ROWID of master table.
	 */
	public Integer get_master_key() {
		Integer res = null;
		if (masterform != null)
			res = masterform.crow_db;
		return res;
	}

	/**
	 * Return master DBFragment object's field <i>fld</i> value from detail DBFragment
	 * object.
	 * @param fld Detail field name
	 * @return Master field name
	 */
	public String get_master_field(String fld) {
		String res = null;
		if (masterform != null) {
			res = masterform.get_field_value(fld);
		}
		return res;
	}

	public void _on_ok() {
		CharSequence v;
		SQLiteDatabase conn;
		crow_edit = false;
		SQLiteCursor c = (SQLiteCursor) cursor_adapter.getCursor();
		int numRows = c.getCount();
		if (numRows == 0 || c.isClosed()) {
			return;
		}
		/*if (!editable.getBool(this)) {
			Toast toast = Toast.makeText(getActivity(),
					G.lstr.get("Changes not allowed"), Toast.LENGTH_SHORT);
			toast.show();
			return;
		}*/
		c.moveToPosition(crow_gui);
		String[] cval = new String[c.getColumnCount()];
		cval[0] = c.getString(0);

		// Update, if any control exists (prevents errors if calling from
		// detail)
		if (columns.get(0).e_ctrl != null) {
			ContentValues vals = new ContentValues();
			for (Column col : columns) {
				if (col.type == G.INTEGER) {
					v = col.e_ctrl.getText();
					if (col.foreign != null) {
						String[] tl = ((String) v).split(" ");
						String itxt = (tl.length == 0) ? "null" : tl[0];
						vals.put(col.name, itxt);
					} else {
						vals.put(col.name, v.toString());
					}
				} else {
					v = col.e_ctrl.getText();
					vals.put(col.name, v.toString());
				}
			}
			conn = c.getDatabase();
			conn.update(tablename, vals, "rowid=?", new String[] { cval[0] });
		}

		on_ok();

		c.requery();
		cursor_adapter.changeCursor(c);
		cursor_adapter.notifyDataSetChanged();

		if (masterform != null) {
			masterform._on_ok();
		}

		menu_enabled.put(ID_MENU_ADD, editable.getBool(this));
		menu_enabled.put(ID_MENU_DELETE, editable.getBool(this));
		
		_set_buttons_state();
	}

	protected void _on_add() {
		// Table columns as a comma separated string
		String scol = "";
		for (Column col : columns) {
			scol += String.format("%s,", col.name);
		}
		scol = scol.substring(0, scol.length() - 1); // remove trailing comma
		String s = String.format("insert into %s (%s) values (", tablename,
				scol);
		// Not supported in older SQLite versions
		// String s = String.format("insert into %s default values (",
		// tablename);
		for (Column col : columns) {
			if (col.defaultValue != null) {
				s += String.format("'%s',", col.defaultValue.getString(this));
			} else {
				s += "null,";
			}
		}
		s = s.substring(0, s.length() - 1); // remove trailing comma
		s += ")";
		SQLiteDatabase conn = cursor.getDatabase();
		conn.execSQL(s);
		on_add();

		refresh_data();
		cursor_adapter.changeCursor(cursor);
		// ?
		cursor_adapter.notifyDataSetChanged();

		int position = cursor_adapter.getCount() - 1;
		Cursor c = cursor_adapter.getCursor();
		c.moveToPosition(position);
		crow_gui = position;
		crow_db = c.getInt(0);
		showDetails(position);
		_set_buttons_state();
		crow_edit = true;
		getListView().setSelection(position);

	}

	protected void _on_delete() {
		Activity parent = getActivity();

		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(parent).create();
		alertDialog.setTitle(G.lstr.get("Delete"));
		alertDialog.setMessage(G.lstr.get("Delete this record?"));
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				G.lstr.get("No"), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// parent.finish();
					}
				});
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				G.lstr.get("Yes"), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						doDelete();
					}
				});

		alertDialog.show();
	}

	private void doDelete() {
		String s;
		if (detail != null) {
			s = String.format("delete from %s where %s=?",
					G.objects.get(detail[0]).tablename, detail[1]);
			cursor.getDatabase().execSQL(s,
					new String[] { String.valueOf(crow_db) });
		}
		s = String.format("delete from %s where ROWID=?", tablename);
		cursor.getDatabase().execSQL(s,
				new String[] { String.valueOf(crow_db) });
		on_delete();

		refresh_data();
		cursor_adapter.changeCursor(cursor);
		// ?
		cursor_adapter.notifyDataSetChanged();
		if (mDualPane) {
			showDetails(crow_gui > 0 ? crow_gui - 1 : 0);
		}
	}

	protected void _on_filter() {
		FragmentTransaction ft = getActivity().getFragmentManager()
				.beginTransaction();
		// Create and show the dialog.
		FilterFragment newFragment = FilterFragment.newInstance(this.getClass()
				.getSimpleName());
		newFragment.show(ft, "filter_dialog");
	}

	/**
	 * Refresh filters for master DBFragment object from detail DBFragment object.
	 */
	public void refresh_master_filter() {
		filter_lst.clear();
		filter_lst.add(new String[] { "detail", masterfield, "=",
				Long.toString(masterform.crow_db) });
	}

	/**
	 * Refresh dataset for DBFragment object.
	 */
	public void refresh_data() {
		String s = sql;
		if (masterform != null) {
			refresh_master_filter();
		}
		if (filter_lst.size() != 0) {
			for (String[] f : filter_lst) {
				if (f[2] != "" && f[3] != "") {
					s += String.format(" and %s %s '%s'", f[1], f[2], f[3]);
				}
			}
		}
		s += " order by ";
		for (String[] ordr : orderby()) {
			s += String.format("%s.%s %s,", tablename, ordr[0], ordr[1]);
		}
		s = s.substring(0, s.length() - 1);
		s += String.format(" limit %s", G.maxrows);
		if (cursor != null) {
			cursor.close();
		}
		try {
			cursor = (SQLiteCursor) G.conn.rawQuery(s, null);
		} catch (RuntimeException e) {
			G.renewstruc();
			cursor = (SQLiteCursor) G.conn.rawQuery(s, null);
		}
	}

	/**
	 * Return sum of values for the field <i>fld_name</i> as string.
	 * @param fld_name Field name to calculate
	 * @param format if <i>format</i> is <i>true</i> then the result value will 
	 * be left-justified.
	 * @param refresh if <i>refresh</i> is <i>true</i> then master DBFragment 
	 * will be refreshed.
	 * @return Sum of values for the field <i>fld_name</i>
	 */
	public String sum_col(String fld_name, Boolean format, Boolean refresh) {
		String res = "0";
		if (refresh && masterform != null)
			refresh_master_filter();
		String s = String.format("select sum(%s) from %s", fld_name, tablename);
		if (filter_lst.size() != 0) {
			s += " where 1=1";
			for (String[] f : filter_lst) {
				if (f[2] != "" && f[3] != "") {
					s += String.format(" and %s %s '%s'", f[1], f[2], f[3]);
				}
			}
		}
		Cursor cur = cursor.getDatabase().rawQuery(s, null);
		if (cur.getCount() > 0) {
			cur.moveToFirst();
			res = cur.getString(0);
			res = (res == null || res.equals("")) ? "0" : res;
		}
		if (format) {
			res = String.format("%12s", res);
		}
		cur.close();
		return res;
	}

	/**
	 * Prevent from creating orphan detail records
	 */
	protected boolean _master_check() {
		boolean ret = true;
		Integer m = get_master_key();
		if (m == null || m == 0) {
			ret = false;
		}
		return ret;
	}

	protected void _set_buttons_state() {
		boolean fro = readonly.getBool(this);
		if (!fro && masterform != null) {
			fro = !_master_check();
		}
		boolean cec = editable.getBool(this);
        menu_enabled.put(ID_MENU_ADD, !fro);
		menu_enabled.put(ID_MENU_DELETE, !fro && cec);
		if (masterform != null) {
			((DetailActivity) getActivity()).invalidateOptionsMenu();
		} else {
			((MainActivity) getActivity()).invalidateOptionsMenu();
		}
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		refresh_data();
		
		// Default adapter parameters
		int adaptLayout = R.layout.simple_list_item_checkable_2;
		int[] adaptTo = new int[] { android.R.id.text1, android.R.id.text2 };

		if (listfields.length == 2) {
			adaptLayout = R.layout.simple_list_item_checkable_2;
			adaptTo = new int[] { android.R.id.text1, android.R.id.text2 };
		} else if (listfields.length == 3) {
			adaptLayout = R.layout.simple_list_item_checkable_1and2;
			adaptTo = new int[] { R.id.text1, R.id.text2, R.id.text3 };
		} else if (listfields.length == 4) {
			adaptLayout = R.layout.simple_list_item_checkable_1and3;
			adaptTo = new int[] { R.id.text1, R.id.text2, R.id.text3, R.id.text4 };
		}

		cursor_adapter = new SimpleCursorAdapter(getActivity(), adaptLayout,
				cursor, listfields, adaptTo, 0);

		if (viewbinder != null) {
			cursor_adapter.setViewBinder(viewbinder);
		}

		setListAdapter(cursor_adapter);

		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.

		View detailsFrame = getActivity().findViewById(R.id.embedit);
		mDualPane = detailsFrame != null
				&& detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			lastsel = savedInstanceState.getInt("curChoice", 0);
			Cursor c = cursor_adapter.getCursor();
			if (c.getCount() > 0) {
				c.moveToPosition(lastsel);
				crow_gui = lastsel;
				crow_db = c.getInt(0);
			}
			if (mDualPane) {
				setSelection(crow_gui);
			}
		}

		// Moved from following 'if' block
		// In dual-pane mode, the list view highlights the selected item.
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (mDualPane) {
			// Make sure our UI is in the correct state.
			showDetails(lastsel);
		}

		registerForContextMenu(getListView());
		_set_buttons_state();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (mDualPane) {
			getListView().setItemChecked(crow_gui, true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", lastsel);
		outState.putSerializable("fld", (Serializable) fld);
		outState.putSerializable("filter_lst", filter_lst);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mDualPane & crow_edit) {
			_on_ok();
		}
		Cursor c = cursor_adapter.getCursor();
		c.moveToPosition(position);
		crow_gui = position;
		crow_db = c.getInt(0);
		showDetails(position);
		_set_buttons_state();
		if (!readonly.getBool(this) && editable.getBool(this)) {
			crow_edit = true;
		}
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails(int index) {
		lastsel = index;
		crow_gui = index;
		String className = this.getClass().getSimpleName();
		if (mDualPane) {
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			getListView().setItemChecked(index, true);

			// Check what fragment is currently shown, replace if needed.
			editform = (EditFragment) getFragmentManager()
					.findFragmentByTag(className + "_d");
			
			Cursor c = cursor_adapter.getCursor();
			if (c.getCount() > 0) {
				c.moveToPosition(crow_gui);
				crow_db = c.getInt(0);
			} else {
				crow_gui = 0;
				crow_db = 0;
			}

			if (editform == null) {
				// Make new fragment to show this selection.
				editform = EditFragment.newInstance(className);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				try {
					ft.remove(editform);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				ft.replace(R.id.embedit, editform, className + "_d");
				ft.commit();
			}
			editform.refresh();
		} else {
			// Otherwise we need to launch a new activity to display
			// the dialog fragment with selected text.
			Intent intent = new Intent();
			intent.setClass(getActivity(), EditActivity.class);
			intent.putExtra("classname", className);
			startActivity(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if (detail != null) {
			menu.add(0, DBFragment.ID_MENU_DETAIL, Menu.NONE,
					G.lstr.get("Detail"));
		}

		int idOffset = DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST;
		if (actions != null) {
			for (String title : actions.values()) {
				menu.add(0, idOffset++, Menu.NONE, title);
			}
		}

		menu.add(0, DBFragment.ID_MENU_DELETE, Menu.NONE, G.lstr.get("Delete"));

		MenuItem menuItem = menu.findItem(DBFragment.ID_MENU_DELETE);
		menuItem.setEnabled(menu_enabled.get(DBFragment.ID_MENU_DELETE));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		if (mDualPane) {
			showDetails(info.position);
		} else {		
			Cursor c = cursor_adapter.getCursor();
			c.moveToPosition(info.position);
			crow_gui = info.position;
			crow_db = c.getInt(0);
		}
		int id = item.getItemId();
		switch (id) {
		case DBFragment.ID_MENU_DETAIL:
			intent = new Intent();
			intent.setClass(getActivity(), DetailActivity.class);
			CheckableFrameLayout cfl = ((CheckableFrameLayout) info.targetView);
			TextView tv = ((TextView) cfl.getChildAt(0));
			intent.putExtra("className", this.details.getClass()
					.getSimpleName());
			intent.putExtra("parentClassName", this.getClass().getSimpleName());
			intent.putExtra("rowtitle", tv.getText());
			startActivity(intent);
			return true;
		case DBFragment.ID_MENU_SEPARATOR:
			return false;
		case DBFragment.ID_MENU_DELETE:
			_on_delete();
			return true;
		default:
			if (id >= DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST) {
				int idOffset = DBFragment.ID_MENU_ACTIONS_LOCAL_FIRST;
				intent = new Intent();
				String className = (String) actions.keySet().toArray()[id
						- idOffset];
				intent.setClass(getActivity(), ActionActivity.class);
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

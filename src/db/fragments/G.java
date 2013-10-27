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
 */package db.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * The global class. It contains various static variables.<br>
 * Class member descriptors:<br>
 * <ul>
 * <li>[Configuration] - this field is visible both in class definitions and
 * runtime</li>
 * <li>[Runtime] - runtime visible field</li>
 * <li>[Runtime readonly] - this runtime visible field will be private in future
 * and be accessed with getter</li>
 * </ul>
 */
public class G {

	/**
	 * DBFragments' date data type
	 */
	public static final DataType DATE = new DataType("DATE", Date.class);

	/**
	 * DBFragments' integer data type
	 */
	public static final DataType INTEGER = new DataType("INTEGER",
			Integer.class);

	/**
	 * DBFragments' real data type
	 */
	public static final DataType REAL = new DataType("REAL", Double.class);

	/**
	 * DBFragments' text data type
	 */
	public static final DataType TEXT = new DataType("TEXT", String.class);

	/**
	 * DBFragments' timestamp data type
	 */
	public static final DataType TIMESTAMP = new DataType("TIMESTAMP",
			Date.class);

	/**
	 * [Configuration] SQLite database file name, default 'dbfile.db'.
	 */
	public static String dbfilename = "dbfile.db";

	/**
	 * [Runtime readonly] Sqlite connection object.
	 */
	public static SQLiteDatabase conn;

	/**
	 * [Configuration] Maximal number of rows listed in tables, default -1 (no
	 * limits).
	 */
	public static int maxrows = -1;

	/**
	 * [Runtime readonly] Application frames map.
	 */
	public static Map<String, DBFragment> objects = new HashMap<String, DBFragment>();

	/**
	 * [Configuration] Application objects in the initialization order.
	 * 'Parameters' like objects always must be first. Objects joined as
	 * 'foreign' to other objects must be initialized first than the objects
	 * they joined to.
	 */
	public static List<String> initorder = new ArrayList<String>();

	/**
	 * [Configuration] Application objects in menu order.
	 */
	public static List<String> menuorder = new ArrayList<String>();

	/**
	 * [Configuration] Title and message strings dictionary for localization
	 * purposes.
	 */
	public static Map<String, String> lstr = new HashMap<String, String>();

	/**
	 * Application's package name.
	 */
	public static String packname = null;

	/**
	 * If true, database file is on SD card.
	 */
	public static boolean externalDatabaseFile = false;

	/**
	 * [Configuration] 'Action' is the non-DBFragments fragment which allows to
	 * implement any functionality you need and will be put in 'Actions'
	 * application menu.
	 */
	public static Map<String, String> actions = new LinkedHashMap<String, String>();

	/**
	 * [Runtime] Restart application flag.
	 */
	public static boolean app_restart = true;

	/**
	 * [Configuration] The application parameters.
	 */
	public static Map<String, String> p = new HashMap<String, String>();

	/**
	 * Helper class to imitate Python lambda function execution.
	 */
	public static class Lambda {
		public boolean getBool(DBFragment self) {
			return true;
		}

		public int getInt(DBFragment self) {
			return 0;
		}

		public String getString(DBFragment self) {
			return "";
		}

		public ArrayList<String> getArrayListOfString(DBFragment self) {
			return null;
		}

		public Date getDate(DBFragment self) {
			return null;
		}
	}

	// Most common functions

	/**
	 * Return boolean value 'true'
	 */
	public static Lambda BooleanTrue = new Lambda() {
		public boolean getBool(DBFragment f) {
			return true;
		}
	};

	/**
	 * Return boolean value 'false'
	 */
	public static Lambda BooleanFalse = new Lambda() {
		public boolean getBool(DBFragment f) {
			return false;
		}
	};

	/**
	 * Return integer value 0
	 */
	public static Lambda IntZero = new Lambda() {
		public int getInt(DBFragment self) {
			return 0;
		}
	};

	/**
	 * Return empty string
	 */
	public static Lambda StringEmpty = new Lambda() {
		public String getString(DBFragment self) {
			return "";
		}
	};

	/**
	 * Return string "0"
	 */
	public static Lambda StringZero = new Lambda() {
		public String getString(DBFragment self) {
			return "0";
		}
	};

	/**
	 * Return string "DEFAULT 0"
	 */
	public static Lambda StringDefaultZero = new G.Lambda() {
		public String getString(DBFragment self) {
			return "DEFAULT 0";
		}
	};

	/**
	 * Return ArrayList null value
	 */
	public static Lambda ArrayListNull = new G.Lambda() {
		public ArrayList<String> getArrayListOfString(DBFragment self) {
			return null;
		}
	};

	/**
	 * Helper procedure for application start.
	 */
	protected static void start() {
		lstr.put("Application Title", "Application Title");
		lstr.put("List", "List");
		lstr.put("Form", "Form");
		lstr.put("Record changed. Save?", "Record changed. Save?");
		lstr.put("Delete this record?", "Delete this record?");
		lstr.put("Filter", "Filter");
		lstr.put("Actions", "Actions");
		lstr.put("Menu", "Menu");
		lstr.put("Add", "Add");
		lstr.put("Delete", "Delete");
		//lstr.put("Detail", "Detail");
		lstr.put("Sum: ", "Sum: ");
		lstr.put("Please wait...", "Please wait...");
		lstr.put("Yes", "Yes");
		lstr.put("No", "No");
		lstr.put("Changes not allowed", "Changes not allowed");
		lstr.put("Do you really want to close the program?",
				"Do you really want to close the program?");

		Class<?> classConf = getClassByName(packname + ".Config");
		Method met;
		try {
			met = classConf.getMethod("init");
			met.invoke(classConf);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		G.app_restart = false;

		if (externalDatabaseFile) {
			try {
				// Try to use database file on sdcard
				DBApplication.getAppContext().getExternalFilesDir(null)
						.mkdirs();
				String dbPath = DBApplication.getAppContext()
						.getExternalFilesDir(null) + "/" + dbfilename;
				conn = SQLiteDatabase.openDatabase(dbPath, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
			} catch (Exception e) {
				// Else use standard file path
				DatabaseHelper dbHelper = new DatabaseHelper(
						DBApplication.getAppContext(), dbfilename, null, 1);
				conn = dbHelper.getWritableDatabase();
			}

		} else {
			DatabaseHelper dbHelper = new DatabaseHelper(
					DBApplication.getAppContext(), dbfilename, null, 1);
			conn = dbHelper.getWritableDatabase();
		}
	}

	/**
	 * Get class object by its name.
	 * 
	 * @param className
	 *            String with class name.
	 * @return Class object.
	 */
	public static Class<?> getClassByName(String className) {
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return cls;
	}

	/**
	 * Get ISO day of the week number
	 * 
	 * @param d
	 *            Date
	 * @return Week day number
	 */
	public static int isoWeekDay(Date d) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		int twday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		return (twday == 0) ? 7 : twday;
	}

	/**
	 * Create or update database structure defined in custom DBFragments classes
	 */
	protected static void renewstruc() {
		String s = "SELECT name FROM sqlite_master WHERE type='table'";
		Cursor c = conn.rawQuery(s, null);
		c.moveToFirst();
		List<String> tt = new ArrayList<String>();
		while (!c.isAfterLast()) {
			tt.add(c.getString(0));
			c.moveToNext();
		}
		c.close();
		DBFragment o;
		ContentValues cv = new ContentValues();
		for (String oName : initorder) {
			o = objects.get(oName);
			if (!tt.contains(o.tableName)) {
				s = String.format("CREATE TABLE %s (", o.tableName);
				for (Column col : o.columns) {
					if (o.columns.indexOf(col) == 0) {
						continue;
					}
					if (col.dbfragment.equals(o)) {
						s += String
								.format(" %s %s", col.name, col.dataType.sql);
						if (col.constr != null)
							s += " " + col.constr.getString(o);
						s += ",";
					}
				}
				s = s.substring(0, s.length() - 1) + ")";
				conn.execSQL(s);
				s = String.format("SELECT ROWID FROM %s LIMIT 1", o.tableName);
				c = conn.rawQuery(s, null);
				if (c.getCount() == 0 && o.initvalues != null) {
					cv.clear();
					conn.beginTransaction();
					for (List<String> val : o.initvalues) {
						for (int i = 1; i < o.columns.size(); i++) { // i == 0
																		// for
																		// ROWID
							cv.put(o.columns.get(i).name, val.get(i - 1));
						}
						conn.insertOrThrow(o.tableName, null, cv);
					}
					conn.setTransactionSuccessful();
					conn.endTransaction();
				}
				c.close();
			} else {
				s = "SELECT sql FROM sqlite_master WHERE type='table' AND name=?";
				String[] param = { o.tableName };
				c = conn.rawQuery(s, param);
				c.moveToFirst();
				String ss = c.getString(0);
				int i = 0;
				for (Column col : o.columns) {
					if (o.columns.indexOf(col) == 0) {
						continue;
					}
					if ((col.dbfragment.equals(o))
							&& (!ss.contains(col.name + " "))
							&& (!ss.contains(col.name + "]"))
							&& (!ss.contains(String.format("\"%s\"", col.name)))) {
						s = String.format("ALTER TABLE %s ADD COLUMN %s %s",
								o.tableName, col.name, col.dataType.sql);
						conn.execSQL(s);
						if (o.initvalues != null) {
							s = String.format(
									"UPDATE %s SET %s=? WHERE ROWID=?",
									o.tableName, col.name);
							int vi = 0;
							conn.beginTransaction();
							for (List<String> iv : o.initvalues) {
								param[0] = (String) iv.toArray()[i++];
								param[1] = Integer.toString(++vi);
								conn.execSQL(s, param);
							}
							conn.setTransactionSuccessful();
							conn.endTransaction();
						}
					}
				}
				c.close();
			}
		}
	}
}

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

import android.view.View;

/**
 * This helper class defines a field of a database table and its UI properties.
 * <p/>
 * Typical usage example:<br>
 * ArrayList<Column> columns = new ArrayList<Column>();<br>
 * columns.add(new Column().name("f_name").type(G.TEXT).title("Name");<br>
 * columns.add(new
 * Column().name("f_price").type(G.REAL).title("Price").constr(G.IntZero));<br>
 * <br>
 * Class member descriptors:<br>
 * <ul>
 * <li>[Mandatory] - mandatory function for column definition</li>
 * <li>[Optional] - optional function for column definition</li>
 * </ul>
 */
public class Column {
	// Using Builder Design Pattern

	/**
	 * This helper class describes left joined field like this:<br>
	 * new Column.Foreign().dbfragment("JoinedDBFragment")<br>
	 * .key_fld("id")<br>
	 * .str_fld("name")<br>
	 * 
	 */
	public static class Foreign {
		public DBFragment dbfragment = null;
		public String keyField = null;
		public String showField = null;
		public String[][] extra_keys = null;

		public Foreign() {
		};

		/**
		 * DBFragment class name that corresponds 'LEFT JOIN table' in SQL
		 * expression.
		 * 
		 * @param _dbfragment
		 *            Class name
		 * @return Foreign object with given class name
		 */
		public Foreign dbfragment(String _dbfragment) {
			dbfragment = G.objects.get(_dbfragment);
			return this;
		}

		/**
		 * Key field in DBFragment class that corresponds 'field2' in 'LEFT JOIN
		 * table ON field1 = field2' SQL expression.
		 * 
		 * @param _key_fld
		 *            Key field name
		 * @return Foreign object with given key field name
		 */
		public Foreign keyField(String _key_fld) {
			keyField = _key_fld;
			return this;
		}

		/**
		 * The field of the TEXT type in DBFragment class that represents joined
		 * text with the table column defined in DBFragment's <i>columns</i>
		 * field.
		 * 
		 * @param _str_fld
		 *            Text field name
		 * @return Foreign object with given text field name
		 */
		public Foreign showField(String _str_fld) {
			showField = _str_fld;
			return this;
		}

		/**
		 * [Optional] If you have more than one field to join as defined in
		 * "keyField", define the arrays of extra field names like this:<br>
		 * new String[][] { { "this_dbfragment_field", "joined_field" }, }
		 * 
		 * @param _extra_keys
		 *            Array of extra keys
		 * @return Foreign object with given extra keys
		 */
		public Foreign extra_keys(String[][] _extra_keys) {
			extra_keys = _extra_keys;
			return this;
		}
	}

	public String name = null;
	public DataType dataType = null;
	public String title = null;
	public G.Lambda readonly = null;
	public G.Lambda defaultValue = null;
	public String[] choose = null;
	public G.Lambda constr = null;
	public Foreign foreign = null;
	public G.Lambda filter = null;
	public Boolean show = true;
	public DBFragment dbfragment = null;

	protected View l_ctrl = null;
	protected Control e_ctrl = null;
	protected Filter f_ctrl = null;

	/**
	 * Constructor
	 * @param _dbfragment DBFragment object that this column belongs to.
	 */
	public Column(DBFragment _dbfragment) {
		dbfragment = _dbfragment;
	}
	
	/*
	 * Check whether control linked to database field changed.
	 * @return <i>true</i> or <i>false</i>
	 */
	public boolean isControlChanged() {
		return e_ctrl.isChanged();
	}

	/**
	 * [Mandatory] Set field name.
	 * 
	 * @param name
	 *            The field name.
	 * @return Column object with given name.
	 */
	public Column name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * [Mandatory] Set field type (currently supported types: DATE, INTEGER,
	 * REAL, TEXT, TIMESTAMP).
	 * 
	 * @param type
	 *            The field type.
	 * @return Column object with given type.
	 */
	public Column dataType(DataType type) {
		this.dataType = type;
		return this;
	}

	/**
	 * [Mandatory] Set column title.
	 * 
	 * @param title
	 *            The column title.
	 * @return Column object with given title.
	 */
	public Column title(String title) {
		this.title = title;
		return this;
	}

	/**
	 * [Optional] 'Lambda' function which returns <i>true</i> if this field need
	 * to be set as readonly.
	 * 
	 * @param readonly
	 *            <i>G.Lambda</i> object with <i>getBool</i> function.
	 * @return Column object with given <i>readonly</i> function.
	 */
	public Column readonly(G.Lambda readonly) {
		this.readonly = readonly;
		return this;
	}

	/**
	 * [Optional] Set default value for the field.
	 * 
	 * @param defaultValue
	 *            Default value.
	 * @return Column object with default value.
	 */
	public Column defaultValue(G.Lambda defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	/**
	 * [Optional] Array of strings which means that this field will be displayed
	 * as a spinner control with values: {<i>value</i>, <i>displayed text</i>}.
	 * 
	 * @param choose
	 *            Choose values.
	 * @return Column object with choose values.
	 */
	public Column choose(String[] choose) {
		this.choose = choose;
		return this;
	}

	public Column constr(G.Lambda constr) {
		this.constr = constr;
		return this;
	}

	/**
	 * [Optional] Set current column as 'foreign'. Not compatible with other DBFragment than 'this'.
	 * 
	 * @param foreign
	 *            The foreign object.
	 * @return Column object with foreign feature.
	 */
	public Column foreign(Foreign foreign) {
		this.foreign = foreign;
		return this;
	}

	/**
	 * [Optional] 'Lambda' function that returns ArrayList<String> with two
	 * values: ('relational_operator', filter_value) where<br>
	 * 'relational_operator' is one of ["", "=", "<=", ">=", "<", ">", "like"]; <br>
	 * filter_value is a value the field is compared to.<br>
	 * If <i>'filter'</i> is defined, it will also appear in filter dialog
	 * called by 'Filter' button on the application's top panel. If the filter
	 * function is defined, but return value is <i>null</i>, it will appear in
	 * the filter dialog with empty relational operator and conditional value.
	 * 
	 * @param filter
	 *            <i>G.Lambda</i> object with <i>getArrayListOfString</i>
	 *            function.
	 * @return Column object with given <i>filter</i> function.
	 */
	public Column filter(G.Lambda filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * [Optional] Set <i>true</i> to show the column (default), <i>false</i> to
	 * hide the column,
	 * 
	 * @param show
	 *            Boolean value.
	 * @return Column object with foreign feature.
	 */
	public Column show(Boolean show) {
		this.show = show;
		return this;
	}

}

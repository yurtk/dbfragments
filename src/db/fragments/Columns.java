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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The table columns collection
 */
public class Columns implements Iterable<Column> {

	private List<Column> columns = new ArrayList<Column>();

	private int currentSize;

	private Map<String, Integer> fld = new LinkedHashMap<String, Integer>();

	public Columns(DBFragment dbfragment) {
	 columns.add(new Column(dbfragment).name("ROWID").dataType(G.INTEGER).title("ROWID").show(false));
	 fld.put("ROWID", 0);
	 currentSize = 1;
	}

	public boolean add(Column col) {
		boolean res = columns.add(col);
		//currentSize = columns.size();
		if (res) {
			fld.put(col.name, currentSize++);
		}
		return res;
	}

	public Column get(int colnum) {
		return columns.get(colnum);
	}

	public Column get(String colname) {
		return columns.get(fld.get(colname));
	}

	public int size() {
		return currentSize;
	}
	
	public Set<String> keySet() {
		return fld.keySet();
	}
	
	public int indexOf(Column col) {
		return columns.indexOf(col);
	}
	
	public int indexOf(String colname) {
		return fld.get(colname);
	}

	@Override
	public Iterator<Column> iterator() {
		Iterator<Column> it = new Iterator<Column>() {

			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return currentIndex < currentSize
						&& columns.get(currentIndex) != null;
			}

			@Override
			public Column next() {
				return columns.get(currentIndex++);
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
			}
		};
		return it;
	}

}

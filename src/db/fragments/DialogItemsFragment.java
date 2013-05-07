package db.fragments;

/*
 * Taken from http://stackoverflow.com/a/12129075 and refactored to fit the task
 */

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.DialogFragment;

/**
 * This class implements a list dialog.
 */
public class DialogItemsFragment extends DialogFragment {
	onDlgListClick mCallback;

	protected String title; // Dialog title
	private String[] list;// the list you want to show with the dialog
	private int selected; // default selection
	DBFragment dbfragment;
	ArrayList<Integer> id_list;
	String[][] extra_keys = null;

	DBFragment parentFragment = null;

	protected Integer search_column_number;
	protected String search_column_value;

	public static DialogItemsFragment newInstance(onDlgListClick cb,
			Column.Foreign foreign) {
		DialogItemsFragment lstFrag = new DialogItemsFragment();
		lstFrag.dbfragment = foreign.dbactivity;
		lstFrag.title = foreign.dbactivity.title;// the title of the list
		lstFrag.extra_keys = foreign.extra_keys;

		lstFrag.mCallback = cb;
		lstFrag.setCancelable(false);

		return lstFrag;
	}

	public interface onDlgListClick {
		public void onLstItemSelected(ArrayList<String> arrayList);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		fill_with_data(search_column_number, search_column_value);

		return new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setCancelable(false)
				.setSingleChoiceItems(list, selected,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								mCallback.onLstItemSelected(dbfragment
										.get_values_by_id(id_list.get(item)));
								getDialog().dismiss();
								DialogItemsFragment.this.dismiss();
							}
						}).create();
	}

	/**
	 * Fill the fragment with data. This metod is called automatically during creation of the fragment object.
	 * @param search_column_number if <i>search_column_number</i> defined, the row selector will be positioned on row with index <i>search_column_value</i>
	 * @param search_column_value search this value to set selected position 
	 */
	public void fill_with_data(Integer search_column_number,
			String search_column_value) {

		// Array for DialogItemsFragment
		ArrayList<String> items_list = new ArrayList<String>();
		id_list = new ArrayList<Integer>();

		String row_text;
		items_list.clear();

		String s = dbfragment.sql;
		if (extra_keys != null) {
			for (String[] ek : extra_keys) {
				s += String.format(" AND %s.%s='%s'", dbfragment.tablename,
						ek[1], parentFragment.get_edit_value(ek[0]));
			}
		}
		Cursor c = G.conn.rawQuery(s, null);

		c.moveToFirst();
		int sel = -1;
		while (!c.isAfterLast()) {
			row_text = c.getString(c.getColumnIndex(dbfragment.listfields[0]));
			row_text += "\n";
			row_text += c.getString(c.getColumnIndex(dbfragment.listfields[1]));

			id_list.add(c.getInt(0));
			items_list.add(row_text);

			if (sel == -1
					&& c.getString(search_column_number).equals(
							search_column_value)) {
				sel = c.getPosition();
			}

			c.moveToNext();
		}
		if (sel != -1) {
			selected = sel;
		}

		list = items_list.toArray(new String[items_list.size()]);
		c.close();
	}

}
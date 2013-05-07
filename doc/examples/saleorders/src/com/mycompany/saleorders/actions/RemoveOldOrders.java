package com.mycompany.saleorders.actions;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import db.fragments.DBAction;
import db.fragments.DBFragment;
import db.fragments.DateChooser;
import db.fragments.G;

public class RemoveOldOrders extends DBAction {
	DateChooser dctrl;
	Button bclean;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		dctrl = new DateChooser(parent);
		dctrl.setText(DateFormat.format("yyyy-MM-dd", new Date()).toString());

		bclean = new Button(parent);
		bclean.setText("Delete all orders before the date");
		bclean.setOnClickListener(bstartClickListener);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(30, 20, 30, 0);

		layout.addView(dctrl, layoutParams);
		layout.addView(bclean, layoutParams);
		return layout;
	}

	OnClickListener bstartClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(parent).create();
			alertDialog.setTitle("Warning");
			alertDialog.setMessage(String.format("ALL orders before %s "
					+ "will be deleted. Continue?", dctrl
					.getText().toString()));
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							parent.finish();
						}
					});
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							doStart();
						}
					});

			alertDialog.show();
		}
	};

	void doStart() {
		SQLiteCursor cursor;
		ArrayList<String> orderList = new ArrayList<String>();

		String sql = "select f_code_id from tb_order where f_date < ?";
		String sql2;

		cursor = (SQLiteCursor) conn.rawQuery(sql, new String[] { dctrl
				.getText().toString() });
		int numOrders = cursor.getCount();
		if (numOrders > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				orderList.add(cursor.getString(cursor
						.getColumnIndex("f_code_id")));
				cursor.moveToNext();
			}
			sql = "delete from tb_order_details where f_code_order=?";
			sql2 = "delete from tb_orders where f_code_id=?";
			for (String ord : orderList) {
				conn.execSQL(sql, new String[] { ord });
				conn.execSQL(sql2, new String[] { ord });
			}
		}

		DBFragment order = G.objects.get("Order");
		order.refresh_data();
		order.cursor_adapter.changeCursor(order.cursor);
		order.cursor_adapter.notifyDataSetChanged();
		if (order.editform != null) {
			order.editform.refresh();
		}

		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(parent).create();
		alertDialog.setTitle("Message");
		alertDialog.setMessage(String.format(
				"Deleting completed.\nOrders deleted: %s.\n", numOrders));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						parent.finish();
					}
				});
		alertDialog.show();

		cursor.close();
	}

}

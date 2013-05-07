package com.mycompany.saleorders.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import db.fragments.DateChooser;

public class Totals extends DBAction {
	DateChooser dctrl;
	Button bclean;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		dctrl = new DateChooser(parent);
		dctrl.setText(DateFormat.format("yyyy-MM-dd", new Date()).toString());

		bclean = new Button(parent);
		bclean.setText("Calculate totals on date");
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
			List<Map<String, String>> totals = new ArrayList<Map<String, String>>();

			Map<String, String> qz = new HashMap<String, String>();
			qz.put("text", "Number of orders: %s\n");
			qz.put("sql", "select count(*) from tb_orders where "
					+ "f_date=? and f_sum > 0");
			qz.put("result", "0");
			totals.add(qz);

			Map<String, String> sz = new HashMap<String, String>();
			sz.put("text", "Total amount of orders: %s\n");
			sz.put("sql", "select sum(f_sum) from tb_orders where "
					+ "f_date=?");
			sz.put("result", "0");
			totals.add(sz);

			SQLiteCursor c;
			String messtxt = "";
			for (Map<String, String> t : totals) {
				c = (SQLiteCursor) conn.rawQuery(t.get("sql"),
						new String[] { dctrl.getText().toString() });
				if (c.getCount() > 0) {
					c.moveToFirst();
					if (c.getString(0) != null) {
						t.put("result", c.getString(0));
						messtxt += String
								.format(t.get("text"), t.get("result"));
					}
				}
				c.close();
			}

			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(parent).create();
			alertDialog.setTitle("Message");
			alertDialog.setMessage(messtxt);
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							parent.finish();
						}
					});
			alertDialog.show();
		}
	};

}

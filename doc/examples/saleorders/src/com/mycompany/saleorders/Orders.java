package com.mycompany.saleorders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import db.fragments.Column;
import db.fragments.Columns;
import db.fragments.DBFragment;
import db.fragments.G;

public class Orders extends DBFragment {

	// Custom 'lambda' object example.
	// Returns string value "PRIMARY KEY AUTOINCREMENT"
	G.Lambda lambdaPKA = new G.Lambda() {
		public String getString(DBFragment self) {
			return "PRIMARY KEY AUTOINCREMENT";
		}
	};

	public Orders() {
		tableName = "tb_orders";
		title = "Orders";

		columns = new Columns(this);
		columns.add(new Column(this).name("f_code_id").dataType(G.INTEGER)
				.title("No").constr(lambdaPKA).readonly(G.BooleanTrue));
		columns.add(new Column(this).name("f_date").dataType(G.DATE)
				.title("Date").defaultValue( /* Today */new G.Lambda() {
					public String getString(DBFragment self) {
						return DateFormat.format("yyyy-MM-dd", new Date())
								.toString();
					}
				}).filter( /* Today */new G.Lambda() {
					public ArrayList<String> getArrayListOfString(
							DBFragment self) {
						SimpleDateFormat dformat = new SimpleDateFormat(
								"yyyy-MM-dd", Locale.US);
						String ds = dformat.format(new Date());
						return new ArrayList<String>(Arrays.asList("=", ds));
					}
				}));
		columns.add(new Column(this)
				.name("f_code_outlet")
				.dataType(G.INTEGER)
				.title("Outlet")
				.defaultValue(G.StringZero)
				.foreign(
						new Column.Foreign().dbfragment("Outlets")
								.keyField("f_code_id").showField("f_name"))
				.filter(G.ArrayListNull));
		columns.add(new Column(this).name("f_sum").dataType(G.REAL)
				.title("Amount").defaultValue(G.StringZero)
				.readonly(G.BooleanTrue).filter(G.ArrayListNull));

		total = "f_sum";
		details = new String[][] { { "OrderDetails", "f_code_order", "Detail" } };
		listfields = new String[] { "f_code_outlet", "f_sum" };
	}

	public void on_ok() {
		// Calculate total amount
		set_field_value("f_sum",
				detailFragments[0].sum_col("f_sum", false, true));

		// Check outlet selection
		String sendPoint = get_field_value("f_code_outlet");
		if (sendPoint == null || sendPoint.equals("0")) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(this.getActivity()).create();
			alertDialog.setTitle("Warning");
			alertDialog.setMessage("Outlet not selected");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// parent.finish();
						}
					});
			alertDialog.show();
		}
	}

	@Override
	public String[][] orderby() {
		return new String[][] { new String[] { "f_code_id", "asc" } };
	}

}

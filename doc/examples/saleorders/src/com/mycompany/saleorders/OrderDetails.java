package com.mycompany.saleorders;

import db.fragments.Column;
import db.fragments.Columns;
import db.fragments.DBFragment;
import db.fragments.G;

public class OrderDetails extends DBFragment {

	public OrderDetails() {
		tableName = "tb_order_details";
		title = "OrderDetails";

		columns = new Columns(this);
		columns.add(new Column(this).name("f_code_order").dataType(G.INTEGER)
				.title("Code").show(false));
		columns.add(new Column(this)
				.name("f_code_prod")
				.dataType(G.INTEGER)
				.title("Product")
				.defaultValue(G.StringZero)
				.foreign(
						new Column.Foreign().dbfragment("Products")
								.keyField("f_code_prod").showField("f_name")));
		columns.add(new Column(this).name("f_num").dataType(G.INTEGER)
				.title("Quantity").defaultValue(G.StringZero));
		columns.add(new Column(this).name("f_sum").dataType(G.REAL).title("Amount")
				.defaultValue(G.StringZero).readonly(G.BooleanTrue));
		columns.add(new Column(this).name("f_price").dataType(G.REAL).title("Price")
				.defaultValue(G.StringZero).readonly(G.BooleanTrue));

		total = "f_sum";

		listfields = new String[] { "f_code_prod", "f_num" };

	}

	/*
	 * Save price and amount
	 */
	@Override
	public void on_ok() {
		set_field_value(
				"f_price",
				G.objects.get("Products").get_by_value("f_code_prod",
						get_field_value("f_code_prod"), "f_price"));
		set_field_value(
				"f_sum",
				Double.toString(Double.parseDouble(get_field_value("f_price"))
						* Double.parseDouble(get_field_value("f_num"))));
	}

}

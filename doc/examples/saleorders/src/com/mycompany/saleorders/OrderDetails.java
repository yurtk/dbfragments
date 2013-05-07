package com.mycompany.saleorders;

import java.util.ArrayList;

import db.fragments.Column;
import db.fragments.DBFragment;
import db.fragments.G;

public class OrderDetails extends DBFragment {

	public OrderDetails() {
		tablename = "tb_order_details";
		title = "OrderDetails";

		columns = new ArrayList<Column>();
		columns.add(new Column().name("f_code_order").type(G.INTEGER)
				.title("Code").show(false));
		columns.add(new Column()
				.name("f_code_prod")
				.type(G.INTEGER)
				.title("Product")
				.defaultValue(G.StringZero)
				.foreign(
						new Column.Foreign().dbfragment("Products")
								.key_fld("f_code_prod").str_fld("f_name")));
		columns.add(new Column().name("f_num").type(G.INTEGER)
				.title("Quantity").defaultValue(G.StringZero));
		columns.add(new Column().name("f_sum").type(G.REAL).title("Amount")
				.defaultValue(G.StringZero).readonly(G.BooleanTrue));
		columns.add(new Column().name("f_price").type(G.REAL).title("Price")
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

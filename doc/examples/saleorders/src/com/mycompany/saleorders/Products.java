package com.mycompany.saleorders;

import java.util.ArrayList;

import db.fragments.Column;
import db.fragments.DBFragment;
import db.fragments.G;

public class Products extends DBFragment {

	public Products() {
		tablename = "tb_products";
		title = "Products";

		columns = new ArrayList<Column>();
		columns.add(new Column().name("f_code_prod").type(G.INTEGER)
				.title("Code").defaultValue(G.StringZero));
		columns.add(new Column().name("f_name").type(G.TEXT).title("Name")
				.defaultValue(G.StringEmpty));
		columns.add(new Column().name("f_price").type(G.REAL).title("Price")
				.defaultValue(G.IntZero));

		listfields = new String[] { "f_code_prod", "f_name", "f_price" };

	}

}

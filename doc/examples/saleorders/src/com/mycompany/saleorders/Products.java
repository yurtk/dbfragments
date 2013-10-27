package com.mycompany.saleorders;

import db.fragments.Column;
import db.fragments.Columns;
import db.fragments.DBFragment;
import db.fragments.G;

public class Products extends DBFragment {

	public Products() {
		tableName = "tb_products";
		title = "Products";

		columns = new Columns(this);
		columns.add(new Column(this).name("f_code_prod").dataType(G.INTEGER)
				.title("Code").defaultValue(G.StringZero));
		columns.add(new Column(this).name("f_name").dataType(G.TEXT)
				.title("Name").defaultValue(G.StringEmpty));
		columns.add(new Column(this).name("f_price").dataType(G.REAL)
				.title("Price").defaultValue(G.IntZero));

		listfields = new String[] { "f_code_prod", "f_name", "f_price" };

	}

}

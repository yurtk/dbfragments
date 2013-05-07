package com.mycompany.saleorders;

import java.util.ArrayList;

import db.fragments.Column;
import db.fragments.DBFragment;
import db.fragments.G;

public class Outlets extends DBFragment {

	public Outlets() {
		tablename = "tb_outlets";
		title = "Outlets";

		columns = new ArrayList<Column>();
		columns.add(new Column().name("f_code_id").type(G.INTEGER)
				.title("Code").defaultValue(G.StringZero));
		columns.add(new Column().name("f_name").type(G.TEXT).title("Name"));

		listfields = new String[] { "f_code_id", "f_name" };

	}

}

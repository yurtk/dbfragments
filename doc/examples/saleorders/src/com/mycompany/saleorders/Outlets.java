package com.mycompany.saleorders;

import db.fragments.Column;
import db.fragments.Columns;
import db.fragments.DBFragment;
import db.fragments.G;

public class Outlets extends DBFragment {

	public Outlets() {
		tableName = "tb_outlets";
		title = "Outlets";

		columns = new Columns(this);
		columns.add(new Column(this).name("f_code_id").dataType(G.INTEGER)
				.title("Code").defaultValue(G.StringZero));
		columns.add(new Column(this).name("f_name").dataType(G.TEXT).title("Name"));

		listfields = new String[] { "f_code_id", "f_name" };

	}

}

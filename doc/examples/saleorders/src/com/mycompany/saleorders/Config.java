package com.mycompany.saleorders;

import java.util.Arrays;

import db.fragments.G;

public class Config {

	public static void init() {
		// Database file name
		G.dbfilename = "orders.db";

		// Put database file to SD card
		G.externalDatabaseFile = true;

		/*
		 * Application objects in initialization order. 'Products' and 'Outlets'
		 * are joined as 'foreign' to other objects, so they must be initialized
		 * first than objects they joined to.
		 */
		G.initorder = Arrays.asList("Products", "Outlets", "Orders",
				"OrderDetails");

		/*
		 * Application objects in menu position order
		 */
		G.menuorder = Arrays.asList("Orders", "Products", "Outlets");

		// Actions (class name, description)
		G.actions.put("Totals", "Totals");
		G.actions.put("RemoveOldOrders", "Delete old orders");

	}

}

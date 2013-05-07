package com.mycompany.saleorders;

import db.fragments.DBApplication;

public class Application extends DBApplication {

	public void onCreate() {
		setPackageName(getApplicationContext().getPackageName());
		super.onCreate();
	}

}

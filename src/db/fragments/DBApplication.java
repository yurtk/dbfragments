/*
 * Copyright (C) 2013 Yuriy Tkachenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package db.fragments;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.app.Application;
import android.content.Context;

/**
 * The main application class.
 */
public class DBApplication extends Application {
	private static Context context;
	private static String packName;
	
	public static void setPackageName(String packageName) {
		packName = packageName;		
	}

	public void onCreate() {
		super.onCreate();
		DBApplication.context = getApplicationContext();

		// Create instances of the DBFragment child classes
		G.packname = packName;
		G.start();
		Object o;
		Constructor<?> constr;
		for (String oName : G.initorder) {
			try {
				constr = G.getClassByName(packName + "." + oName)
						.getConstructor();
				o = constr.newInstance();
				G.objects.put(oName, (DBFragment) o);
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		G.renewstruc();
	}

	public static Context getAppContext() {
		return DBApplication.context;
	}
}

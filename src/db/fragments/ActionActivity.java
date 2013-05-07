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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import db.fragments.R;

/**
 * Activity class for DBAction.
 */
public class ActionActivity extends Activity {

	int lastOrientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Object o;
		setContentView(R.layout.dbactivity_layout);
		String actionClass = getIntent().getExtras().getString("classname");
		String actionTitle = getIntent().getExtras().getString("title");
		setTitle(actionTitle);
		Constructor<?> constr;
		if (savedInstanceState != null) {
			lastOrientation = savedInstanceState.getInt("lastOrientation");
		} else {
			lastOrientation = getResources().getConfiguration().orientation;
		}
		int currentOrientation = getResources().getConfiguration().orientation;
		try {
			constr = G.getClassByName(G.packname + ".actions." + actionClass)
					.getConstructor();
			o = constr.newInstance();
			((DBAction) o).setArguments(getIntent().getExtras());
			((DBAction) o).setTitle(actionTitle);

			if (lastOrientation != currentOrientation) {
				lastOrientation = currentOrientation;
			} else {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.actlist, (Fragment) o);
				ft.commit();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("lastOrientation", lastOrientation);
	}

}

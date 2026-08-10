/*
* Copyright (C) 2016 The OmniROM Project
* Copyright (C) 2021 The dot X Project
* Copyright (C) 2018-2021 crDroid Android Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.neoteric.device.DeviceExtras;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import org.neoteric.device.DeviceExtras.FileUtils;
import org.neoteric.device.DeviceExtras.R;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

public class DeviceExtras extends SettingsBasePreferenceFragment {
    public static final String KEY_OTG_SWITCH = "otg";

    private boolean bound = false;
    private OTGModeSwitch service;

    private static SwitchPreferenceCompat mOTGModeSwitch;
    private final OTGModeSwitch.Listener chargingListener = charging -> setAvailable();

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((OTGModeSwitch.LocalBinder) binder).getService();
            bound = true;
            service.addListener(chargingListener);
            setAvailable();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            service = null;
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        addPreferencesFromResource(R.xml.main);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        // OTG
        mOTGModeSwitch = findPreference(KEY_OTG_SWITCH);
        if (OTGModeSwitch.isSupported()) {
            setAvailable();
            mOTGModeSwitch.setChecked(OTGModeSwitch.isCurrentlyEnabled());
            mOTGModeSwitch.setOnPreferenceChangeListener(new OTGModeSwitch());
        } else {
            getPreferenceScreen().removePreference(mOTGModeSwitch);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(Constants.CONTEXT, OTGModeSwitch.class);
        Constants.CONTEXT.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        if (bound) {
            service.removeListener(chargingListener);
            Constants.CONTEXT.unbindService(connection);
            bound = false;
        }
        super.onPause();
    }

    private void setAvailable() {
        mOTGModeSwitch.setEnabled(OTGModeSwitch.isAvailable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

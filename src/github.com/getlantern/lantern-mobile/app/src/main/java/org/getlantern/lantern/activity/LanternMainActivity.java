package org.getlantern.lantern.activity;

import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.ViewGroup;

import android.net.VpnService;

import org.getlantern.lantern.config.LanternConfig;
import org.getlantern.lantern.R;
import org.getlantern.lantern.service.LanternVpn;


public class LanternMainActivity extends ActionBarActivity implements Handler.Callback {

    private static final String TAG = "LanternMainActivity";
    private static final String PREFS_NAME = "LanternPrefs";
    private SharedPreferences mPrefs = null;

    private ToggleButton powerLantern;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getSharedPrefs(getApplicationContext());

        setContentView(R.layout.activity_lantern_main);

        powerLantern = (ToggleButton)findViewById(R.id.powerLantern);
        setupLanternSwitch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // we check if mPrefs has been initialized before
        // since onCreate and onResume are always both called
        if (mPrefs != null) {
            setBtnStatus();
        }
    }

    // START/STOP button to enable full-device VPN functionality
    private void setupLanternSwitch() {

        setBtnStatus();

        // START/STOP button to enable full-device VPN functionality
        powerLantern.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean useVpn;
                if (isChecked) {
                    enableVPN();
                    useVpn = true;
                } else {
                    stopLantern();
                    useVpn = false;
                }
                // display status message at bottom of screen
                displayStatus(useVpn);

                // store the updated preference 
                mPrefs.edit().putBoolean(LanternConfig.PREF_USE_VPN, useVpn).commit();

            }
        });
    } 

    private void displayStatus(boolean useVpn) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.status_layout,
                (ViewGroup) findViewById(R.id.status_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.status_text);
        if (useVpn) {
            text.setText(R.string.on_text);
        } else {
            text.setText(R.string.off_text);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    // update START/STOP power Lantern button
    // according to our stored preference
    public void setBtnStatus() {
        boolean useVPN = useVpn();
        powerLantern.setChecked(useVPN);
    }

    public SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
    }

    public boolean useVpn() {
        return mPrefs.getBoolean(LanternConfig.PREF_USE_VPN, false);
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            //Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    // Prompt the user to enable full-device VPN mode
    protected void enableVPN() {
        Log.d(TAG, "Load VPN configuration");
        Intent intent = new Intent(LanternMainActivity.this, PromptVpnActivity.class);
        if (intent != null) {
            startActivity(intent);
        }
    }

    protected void stopLantern() {
        Log.d(TAG, "Stopping Lantern...");
        try {
            Intent service = new Intent(LanternMainActivity.this, LanternVpn.class);
            service.setAction(LanternConfig.DISABLE_VPN);
            startService(service);
        } catch (Exception e) {
            Log.d(TAG, "Got an exception trying to stop Lantern: " + e);
        }
    }
}
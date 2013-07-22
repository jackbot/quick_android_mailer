package org.ajack.quick_email;

import java.util.List;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity {

    private EditText textInput;
    private Button sendButton;
    private Spinner emailSpinner;

    private SharedPreferences prefs;
    private final String PREFS_KEY = "chosen_email";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getSharedPreferences("org.ajack.quick_email", Context.MODE_PRIVATE);

        initWidgets();

        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String content = textInput.getText().toString();
                String selectedEmail = emailSpinner.getSelectedItem().toString();
                setStickyEmail(selectedEmail);
                sendEmail(content, selectedEmail);
            }
        });
    }

    protected void onPause() {
        finish();
        super.onPause();
    }

    private void setStickyEmail(String email) {
    	prefs.edit().putString(PREFS_KEY, email).commit();
    }

    private String getStickyEmail() {
    	return prefs.getString(PREFS_KEY, availableEmails()[0]);
    }

    private void initWidgets() {
        textInput = (EditText) findViewById(R.id.input_field);
        sendButton = (Button) findViewById(R.id.send_button);
        emailSpinner = (Spinner) findViewById(R.id.email_spinner);

        String[] availableEmails = availableEmails();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availableEmails);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emailSpinner.setAdapter(dataAdapter);

        int selectedIndex = indexOf(availableEmails, getStickyEmail());
        if (selectedIndex == -1) { selectedIndex = 0; }
        emailSpinner.setSelection(selectedIndex);
    }

    private boolean sendEmail(String content, String address) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{ address });
        i.putExtra(Intent.EXTRA_SUBJECT, content);
        i.putExtra(Intent.EXTRA_TEXT, content);

        // From http://stackoverflow.com/a/3936323
        final List<ResolveInfo> matches = getPackageManager().queryIntentActivities(i, 0);
        ResolveInfo best = null;

        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase(Locale.getDefault()).contains("gmail")) {
              best = info;
            }
        }

        if (best != null) {
            i.setClassName(best.activityInfo.packageName, best.activityInfo.name);
            startActivity(i);
        } else {
            startActivity(Intent.createChooser(i, getString(R.string.choose_email_client)));
        }

        return true;
    }

    private String[] availableEmails() {
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType("com.google");

        String[] emails = new String[accounts.length];

        for (int i = 0; i < accounts.length; i++) {
            emails[i] = accounts[i].name;
        }

        return emails;
    }

    private int indexOf(String[] array, String needle) {
    	for (int i = 0; i < array.length; i++) {
    		if (array[i].equals(needle)) {
    			return i;
    		}
    	}
    	return -1;
    }

}

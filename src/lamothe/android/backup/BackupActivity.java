package lamothe.android.backup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BackupActivity extends Activity {
    private boolean isBound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startService();
            }
        });

        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	stopService();
            }
        });
        
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        final TextView editTextBackupToDirectory = (TextView) findViewById(R.id.editTextBackupToDirectory);        		
        final TextView editTextDomain = (TextView) findViewById(R.id.editTextDomain);        		
        final TextView editTextUsername = (TextView) findViewById(R.id.editTextUsername);        		
        final TextView editTextPassword = (TextView) findViewById(R.id.editTextPassword);        		

        editTextBackupToDirectory.setText(preferences.getString("backupToDirectory", ""));
        editTextDomain.setText(preferences.getString("backupDomain", ""));
        editTextUsername.setText(preferences.getString("backupUsername", ""));
        editTextPassword.setText(preferences.getString("backupPassword", ""));
    }

    void startService() {
        final TextView editTextBackupToDirectory = (TextView) findViewById(R.id.editTextBackupToDirectory);        		
        final TextView editTextDomain = (TextView) findViewById(R.id.editTextDomain);        		
        final TextView editTextUsername = (TextView) findViewById(R.id.editTextUsername);        		
        final TextView editTextPassword = (TextView) findViewById(R.id.editTextPassword);        		

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("backupToDirectory", editTextBackupToDirectory.getText().toString());        
        editor.putString("backupDomain", editTextDomain.getText().toString());
        editor.putString("backupUsername", editTextUsername.getText().toString());
        editor.putString("backupPassword", editTextPassword.getText().toString());
        editor.commit();
        
        Intent intent = new Intent(this, BackupService.class);
        Bundle bundle = intent.getExtras();
    	bundle = new Bundle();    	
        bundle.putString("backupToDirectory", editTextBackupToDirectory.getText().toString());        
        bundle.putString("backupDomain", editTextDomain.getText().toString());
        bundle.putString("backupUsername", editTextUsername.getText().toString());
        bundle.putString("backupPassword", editTextPassword.getText().toString());
    	intent.putExtras(bundle);
        
        startService(intent);
        isBound = true;
    }

    void stopService() {
		if (isBound) {
            stopService(new Intent(this, BackupService.class));
            isBound = false;
        }
    }
}
package lamothe.android.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BackupService extends Service {
	private NotificationManager notificationManager;
	private Thread thread;

	public class LocalBinder extends Binder {
		BackupService getService() {
			return BackupService.this;
		}
	}

	private final IBinder binder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	private void copyFile(File from, SmbFile to) throws IOException {
		String copyMessage = "Copying '" + from.getPath() + "' to '" + to.getUncPath() + "'";
		Thread currentThread = Thread.currentThread();
		
		Log.i("BackupService", copyMessage);

		showNotification("Copying " + from.getName());
		
		SmbFileOutputStream out = new SmbFileOutputStream(to);
		try {
			byte[] buffer = new byte[1024];
			FileInputStream in = new FileInputStream(from);
			try {
				int len = 0;
				while ((len = in.read(buffer)) >= 0) {
					if (currentThread.isInterrupted()) {
						return;
					}
					
					out.write(buffer, 0, len);
				}
			} finally {
				in.close();
			}
		} finally {
			out.close();
		}
	}

	private void copyFolder(File from, SmbFile to) throws IOException {

		Log.i("BackupService", "Copying folder '" + from.getPath() + "' to '"
				+ to.getUncPath() + "'");

		File[] fromFiles = from.listFiles();
		Thread currentThread = Thread.currentThread();

		if (fromFiles != null) {
			for (int i = 0; i < fromFiles.length; i++) {
				if (currentThread.isInterrupted()) {
					Log.i("BackupService", "Thread interrupted");
					return;
				}

				File fromChild = fromFiles[i];
				String fromChildFilename = fromChild.getName();
				if (fromChildFilename.startsWith(".")) {
					Log.i("BackupService",
							String.format("Ignoring %s", fromChildFilename));
				} else {
					if (fromChild.isFile()) {
						SmbFile toChild = new SmbFile(to, fromChildFilename);
						if (!toChild.exists()) {
							copyFile(fromChild, toChild);
						}

						else {
							Log.i("BackupService",
									String.format("%s already exists",
											toChild.getName()));
						}
					} else if (from.isDirectory()) {
						SmbFile toChild = new SmbFile(to, fromChildFilename + "/");
						if (!toChild.exists()) {
							Log.i("BackupService", String.format(
									"Creating directory %s", toChild.getName()));
							toChild.mkdir();
						} else {
							Log.i("BackupService", String.format(
									"Directory %s already exists",
									toChild.getName()));
						}

						copyFolder(fromChild, toChild);
					} else {
						Log.i("BackupService", String.format(
								"Don't know what to do with %s",
								fromChildFilename));
					}
				}
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (thread != null) {
			showNotification("Service already running");
		} else {
			showToast(R.string.backup_service_started);
			showNotification("Service running");

			final Bundle bundle = intent.getExtras();
			final String backupToDirectory = bundle.getString("backupToDirectory");
			
			thread = new Thread() {
				@Override
				public void run() {
					try {
						String toDirectory = backupToDirectory;
						if (!toDirectory.endsWith("/"))
						{
							toDirectory += "/";
						}

						SmbFile to = new SmbFile(
								backupToDirectory,
								new NtlmPasswordAuthentication(
										bundle.getString("backupDomain"),
										bundle.getString("backupUsername"),
										bundle.getString("backupPassword")));
						File from = new File("/sdcard/DCIM");
						copyFolder(from, to);
					} catch (Exception e) {
						String message = e.getMessage();
						Log.e("BackupService", message);
					}
					thread = null;
					cancelNotification();

					Log.i("BackupService", "Complete!");
				}
			};
			thread.start();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException ex) {
				Log.i("BackupService", ex.getMessage());
				showNotification(ex.getMessage());
			}

			thread = null;
			cancelNotification();
		}
		showToast(R.string.backup_service_stopped);
	}
	
	private void cancelNotification()
	{
		notificationManager.cancel(0);
	}
	
	private void showToast(int id)
	{
		Toast.makeText(this, id, Toast.LENGTH_SHORT).show();		
	}

	private void showNotification(CharSequence text) {
		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, BackupActivity.class), 0);
		notification.setLatestEventInfo(this, getText(R.string.backup_service_label), text, contentIntent);
		notificationManager.notify(0, notification);
	}

}

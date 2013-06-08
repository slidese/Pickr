
package se.slide.pickr;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.widget.ListView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.xml.atom.AtomParser;

import se.slide.pickr.db.DatabaseManager;
import se.slide.pickr.model.Path;
import se.slide.pickr.picasa.model.AlbumEntry;
import se.slide.pickr.picasa.model.PicasaUrl;
import se.slide.pickr.picasa.model.UserFeed;
import se.slide.pickr.picasa.model.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    
    private static final int REQUEST_AUTHENTICATE = 0;
    private static final int REQUEST_ACCOUNT_PICKER = 2;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 3;
    
    private static final String AUTH_TOKEN_TYPE = "lh2";
    
    private HttpTransport transport;
    private String token;
    
    MyFileObserver fileOb;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        DatabaseManager.init(this);
        
        setLogging(true);
        
        createTransport();
        
        //fileOb = new MyFileObserver("/storage/emulated/0/Pictures/Screenshots/");
        //fileOb.startWatching();
        
        setupSimplePreferencesScreen();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        //fileOb.stopWatching();
    }

    private void setLogging(boolean logging) {
        Logger.getLogger("com.google.api.client").setLevel(logging ? Level.ALL : Level.OFF);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        
        addPreferencesFromResource(R.xml.pref_data_sync);
        
        //bindPreferenceSummaryToValue(findPreference("example_text"));
        //bindPreferenceSummaryToValue(findPreference("example_list"));
        //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        
        // Google
        Preference GoogleAccountPref = findPreference("provider_google");
        GoogleAccountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                String accountName = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "");

                AccountManager manager = AccountManager.get(preference.getContext());
                Account[] accounts = manager.getAccountsByType("com.google");
                
                int initialindex = 0;
                final int size = accounts.length;
                final String[] names = new String[size];
                for (int i = 0; i < size; i++) {
                  names[i] = accounts[i].name;
                  
                  if (accounts[i].name.equalsIgnoreCase(accountName))
                      initialindex = i;
                }
                
                AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                builder.setTitle(R.string.choose_account_title)
                        .setSingleChoiceItems(names, initialindex, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int item) {
                                // dialogInterface.dismiss();
                            }
                        });
                builder.setPositiveButton(getString(R.string.ok), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                        ListView lw = ((AlertDialog) dialog).getListView();
                        
                        preference.getEditor().putString("provider_google", names[lw.getCheckedItemPosition()]).commit();
                        preference.setSummary(names[lw.getCheckedItemPosition()]);
                        
                        new RunAuthenticationTask(names[lw.getCheckedItemPosition()], getBaseContext()).execute();
                        
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
                builder.create().show();

                return false;
            }
        });
        
        final String accountName = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(GoogleAccountPref.getKey(), "");
        GoogleAccountPref.setSummary(accountName);
        
        // Dropbox
        Preference DropboxAccountPref = findPreference("provider_dropbox");
        DropboxAccountPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
           
            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                //new RunAuthenticationTask(accountName, preference.getContext()).execute();
                //new GetAlbumsTask(token).execute();
                
                //new UploadTask(token).execute();
                
                List<Path> paths = DatabaseManager.getInstance().getAllPaths();
                for (Path path : paths) {
                    
                    MyFileObserver o = new MyFileObserver(null, path.getPath(), path.getId());
                    o.startWatching();
                    
                }
                
                return false;
            }
        });
    }
    
    private class RunAuthenticationTask extends AsyncTask<Void, Void, String> {

        private String accountName;
        private WeakReference<Context> weakContext;

        public RunAuthenticationTask(String accountName, Context context) {
            this.accountName = accountName;
            weakContext = new WeakReference<Context>(context);
        }

        @Override
        protected String doInBackground(Void... params) {
            
            final AccountManager manager = AccountManager.get(weakContext.get());
            
            Account account = getAccount(manager, accountName);
            
            try {
                final Bundle bundle = manager.getAuthToken(account, AUTH_TOKEN_TYPE, new Bundle(), true, null, null).getResult();
                
                if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                    Intent intent =
                        bundle.getParcelable(AccountManager.KEY_INTENT);
                    int flags = intent.getFlags();
                    flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                    startActivityForResult(intent, REQUEST_AUTHENTICATE);
                  } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                      // We don't do anything with the token now
                      return bundle.getString(AccountManager.KEY_AUTHTOKEN);
                  }
                
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            saveToken(result);
        }
        
        
    }
    
    private void saveToken(String token) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("token", token).commit();
    }
    
    private Account getAccount(AccountManager manager, String accountName) {
        Account[] accounts = manager.getAccountsByType("com.google");
        
        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName))
                return account;
        }
        
        return null;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     * 
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
    
    /*
     * (non-Javadoc)
     * @see android.preference.PreferenceActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK
                && data != null && data.getExtras() != null) {
            String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);

            Preference syncGoogleAccountPref = findPreference("provider_google");
            syncGoogleAccountPref.getEditor()
                    .putString("provider_google", accountName).commit();
            syncGoogleAccountPref.setSummary(accountName);
            
            new RunAuthenticationTask(accountName, getBaseContext()).execute();
        }
    }

    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, SettingsActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }
    
    private void createTransport() {
        
        HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
        transport = GoogleTransport.create();
        GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
        headers.setApplicationName("google-picasaandroidsample-1.0");
        headers.gdataVersion = "2";
        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
        transport.addParser(parser);
    }
    
    private class UploadTask extends AsyncTask<Void, Void, Void> {
        
        private String token;
        
        public UploadTask(String token) {
            this.token = token;
        }

        @Override
        protected Void doInBackground(Void... params) {
            
            ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(token);
            
            File f = new File("/storage/emulated/0/Pictures/Screenshots/Screenshot_2012-12-26-00-18-46.png");
            
            //Uri path = Uri.parse("/storage/emulated/0/Pictures/Screenshots/Screenshot_2012-12-26-00-18-46.png");
            Uri path = Uri.fromFile(f);
            
            InputStreamContent content = new InputStreamContent();
            try {
                content.inputStream = getContentResolver().openInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            HttpRequest request = transport.buildPostRequest();
            //request.url = PicasaUrl.relativeToRoot("feed/api/user/default/albumid/default"); //5874594657436139953
            request.url = PicasaUrl.relativeToRoot("feed/api/user/default/albumid/5874594657436139953");
            ((GoogleHeaders) request.headers).setSlugFromFileName("the_filename.png");
            //InputStreamContent content = new InputStreamContent();
            //content.inputStream = getContentResolver().openInputStream(sendData.uri);
            content.type = "image/png"; //path.contentType;
            content.length =  f.length(); //sendData.contentLength;
            request.content = content;
            
            try {
                request.execute().ignore();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
    }
    
    private class GetAlbumsTask extends AsyncTask<Void, Void, Void> {
        
        private String token;
        
        public GetAlbumsTask(String token) {
            this.token = token;
        }

        @Override
        protected Void doInBackground(Void... params) {
            
            ((GoogleHeaders) transport.defaultHeaders).setGoogleLogin(token);
            
            /*
            HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
            transport = GoogleTransport.create();
            GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
            headers.setApplicationName("google-picasaandroidsample-1.0");
            headers.gdataVersion = "2";
            AtomParser parser = new AtomParser();
            parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
            transport.addParser(parser);
            */
            
            String[] albumNames;
            List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
            albums.clear();
            try {
              PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");
              // page through results
              while (true) {
                UserFeed userFeed = UserFeed.executeGet(transport, url);
                //this.postLink = userFeed.getPostLink();
                if (userFeed.albums != null) {
                  albums.addAll(userFeed.albums);
                }
                String nextLink = userFeed.getNextLink();
                if (nextLink == null) {
                  break;
                }
              }
              int numAlbums = albums.size();
              albumNames = new String[numAlbums];
              for (int i = 0; i < numAlbums; i++) {
                albumNames[i] = albums.get(i).title;
              }
            } catch (IOException e) {
              //handleException(e);
              albumNames = new String[] {e.getMessage()};
              albums.clear();
            }
            
            return null;
        }
        
    }
    
}

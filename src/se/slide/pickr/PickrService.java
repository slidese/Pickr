
package se.slide.pickr;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.xml.atom.AtomParser;

import se.slide.pickr.db.DatabaseManager;
import se.slide.pickr.model.Path;
import se.slide.pickr.picasa.model.PicasaUrl;
import se.slide.pickr.picasa.model.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PickrService extends Service {

    private static final int REQUEST_AUTHENTICATE = 0;
    private static final String AUTH_TOKEN_TYPE = "lh2";
    
    private static final int NOTIFICATION_TYPE_NEED_AUTHORIZATION = 0;
    private static final int NOTIFICATION_TYPE_REACHED_RETRY_LIMIT = 1;

    private List<MyFileObserver> mObservers;
    private HttpTransport mTransport;
    private String mToken;
    private String mAccountName;
    private List<Photo> mQueue;
    private int mRetries;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.init(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // If we have observers already setup stop them before creating a new
        // collection
        if (mObservers != null && !mObservers.isEmpty()) {
            for (MyFileObserver observer : mObservers)
                observer.stopWatching();
        }

        mObservers = new ArrayList<MyFileObserver>();

        List<Path> paths = DatabaseManager.getInstance().getAllPaths();
        for (Path path : paths) {

            MyFileObserver o = new MyFileObserver(this, path.getPath(), path.getId());
            o.startWatching();
            mObservers.add(o);

        }

        mAccountName = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("provider_google", "");

        createTransport();
        
        mToken = PreferenceManager.getDefaultSharedPreferences(this).getString("token", null);

        /*
         * AlarmManager mgr=(AlarmManager)
         * getSystemService(Context.ALARM_SERVICE); Intent i=new Intent(this,
         * OnAlarmReceiver.class); PendingIntent
         * pi=PendingIntent.getBroadcast(this, 0, i, 0);
         * mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         * SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
         * pi);
         */

        // Reset retries
        mRetries = 0;

        return super.onStartCommand(intent, flags, startId);
    }

    private void createTransport() {

        HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
        mTransport = GoogleTransport.create();
        GoogleHeaders headers = (GoogleHeaders) mTransport.defaultHeaders;
        headers.setApplicationName("google-picasaandroidsample-1.0");
        headers.gdataVersion = "2";
        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
        mTransport.addParser(parser);
    }

    /**
     * Put a newly created photo on the queue
     * 
     * @param pathId
     */
    public void queuePhoto(int pathId) {
        List<Path> paths = DatabaseManager.getInstance().getPath(pathId);

        for (Path p : paths) {
            String contenttype = "";
            String suffix = p.getPath().substring(p.getPath().lastIndexOf(".") + 1);
            if (suffix.equalsIgnoreCase("png"))
                contenttype = "image/png";
            else if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("jpeg"))
                contenttype = "image/jpeg";
            else if (suffix.equalsIgnoreCase("png"))
                contenttype = "image/gif";

            String filename = p.getPath().substring(p.getPath().lastIndexOf("/") + 1);
            
            Photo photo = new Photo();
            photo.path = p.getPath();
            photo.filename = filename;
            photo.albumUrl = p.getGoogleAlbumUrl();
            photo.contentType = contenttype;
            photo.uploaded = false;

            mQueue.add(photo);
        }
    }

    /**
     * Start to upload queue
     */
    public void uploadQueue() {
        if (mToken == null || mToken.length() < 1) {
            
            if (mRetries++ < 10)
                new RunAuthenticationTask(mAccountName, this).execute();
            else
                showNotification(NOTIFICATION_TYPE_REACHED_RETRY_LIMIT);
            
            return;
        }

    }

    /**
     * Re-queue all photos that was noy successfully uploaded
     * 
     * @param photos
     */
    public void handleUploadedPhotos(List<Photo> photos) {
        if (photos == null)
            return;

        for (Photo photo : photos) {

            if (!photo.uploaded)
                mQueue.add(photo);

        }
    }

    private Account getAccount(AccountManager manager, String accountName) {
        Account[] accounts = manager.getAccountsByType("com.google");

        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName))
                return account;
        }

        return null;
    }
    
    private void showNotification(int type) {
        if (type == NOTIFICATION_TYPE_REACHED_RETRY_LIMIT) {
            
        }
        else if (type == NOTIFICATION_TYPE_NEED_AUTHORIZATION) {
            
        }
    }
    
    private void saveToken(String token) {
        mToken = token;
        ((GoogleHeaders) mTransport.defaultHeaders).setGoogleLogin(mToken);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("token", token).commit();
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
                final Bundle bundle = manager.getAuthToken(account, AUTH_TOKEN_TYPE, new Bundle(),
                        true, null, null).getResult();

                if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                    showNotification(NOTIFICATION_TYPE_NEED_AUTHORIZATION);
                } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
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

    private class UploadGoogleTask extends AsyncTask<Photo, Void, List<Photo>> {

        @Override
        protected List<Photo> doInBackground(Photo... params) {

            List<Photo> photos = new ArrayList<Photo>();

            for (Photo photo : params) {
                File f = new File(photo.path); // File f = new
                                               // File("/storage/emulated/0/Pictures/Screenshots/Screenshot_2012-12-26-00-18-46.png");

                Uri path = Uri.fromFile(f);

                InputStreamContent content = new InputStreamContent();
                try {
                    content.inputStream = getContentResolver().openInputStream(path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                HttpRequest request = mTransport.buildPostRequest();
                request.url = new PicasaUrl(photo.albumUrl); // request.url =
                                                             // PicasaUrl.relativeToRoot("feed/api/user/default/albumid/5874594657436139953");
                ((GoogleHeaders) request.headers).setSlugFromFileName(photo.filename);
                content.type = photo.contentType;
                content.length = f.length();
                request.content = content;

                try {
                    request.execute().ignore();

                    photo.uploaded = true;
                    photos.add(photo);

                } catch (IOException e) {
                    e.printStackTrace();

                    photo.uploaded = false;
                    photos.add(photo);
                }
            }

            return photos;
        }

        @Override
        protected void onPostExecute(List<Photo> result) {
            super.onPostExecute(result);

            handleUploadedPhotos(result);
        }

    }

    private class Photo {
        String path;
        String filename;
        String albumUrl;
        String contentType;
        boolean uploaded;
    }
}

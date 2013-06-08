package se.slide.pickr;

import android.os.FileObserver;
import android.util.Log;

import java.lang.ref.WeakReference;

public class MyFileObserver extends FileObserver {
    
    private final String TAG = "MyFileObserver";
    
    private WeakReference<PickrService> weakPickrService;
    private int mPathId;
    private String mPath;
    
    public MyFileObserver(PickrService service, String path, int pathId) {
        super(path, FileObserver.CREATE);
        
        weakPickrService = new WeakReference<PickrService>(service);
        mPath = path;
        mPathId = pathId;
    }

    @Override
    public void onEvent(int event, String path) {
        Log.d(TAG, "Event fired");
        
        if (path == null) {
            return;
        }
        
        if ((FileObserver.CREATE & event)!=0) {
            Log.d(TAG, mPath + "/" + path + " is created");
            
            // Let our service know we have a new file
            PickrService service = weakPickrService.get();
            if (service != null)
                service.queuePhoto(mPathId);
            else
                Log.e(TAG, "Service is null");
            
        }
        

    }

}

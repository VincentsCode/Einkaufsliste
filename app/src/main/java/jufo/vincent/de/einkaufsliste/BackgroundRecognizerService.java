package jufo.vincent.de.einkaufsliste;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class BackgroundRecognizerService extends Service {

    private SpeechRecognizerManager mSpeechManager;
    static String finalResult;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf("Logger", "BackgroundRecognizerService: onStartCommand");
        return Service.START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) {
        Log.wtf("Logger", "BackgroundRecognizerService: onBind");
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.wtf("Logger", "BackgroundRecognizerService: onCreate 0");
        if (mSpeechManager == null) {
            setSpeechListener();
            Log.wtf("Logger", "BackgroundRecognizerService: onCreate X");
        } else if (!mSpeechManager.ismIsListening()) {
            Log.wtf("Logger", "BackgroundRecognizerService: onCreate Y");
            mSpeechManager.destroy();
            setSpeechListener();
        }
        Log.wtf("Logger", "BackgroundRecognizerService: onCreate 1");
    }

    public void onDestroy() {
        Log.wtf("Logger", "BackgroundRecognizerService: onDestroy 0");
        if (mSpeechManager != null) {
            mSpeechManager.destroy();
            mSpeechManager = null;
        }
        super.onDestroy();
        Log.wtf("Logger", "BackgroundRecognizerService: onDestroy 1");
    }

    private void setSpeechListener() {
        Log.wtf("Logger", "BackgroundRecognizerService: setSpeechRecognizer 0");
        mSpeechManager = new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            public void onResults(ArrayList<String> results) {
                Log.wtf("Logger", "BackgroundRecognizerService: onResults 0");
                if (results != null && results.size() > 0) {
                    Log.wtf("Logger", "GOT RESULTS");
                    boolean found = false;
                    for (int i = 0; i < results.size() && !found; i++) {
                        Log.wtf("Logger", "RESULTS: " + results.toString());
                        if (results.get(i).toLowerCase().contains("hinzufügen".toLowerCase())) {
                            found = true;
                            String result = results.get(i);
                            Log.d("Result", result);
                            finalResult = result.replace("hinzufügen", "");
                            finalResult = finalResult.trim();
                            Log.d("Final Result", finalResult);
                            MainActivity.addItem(finalResult);
                        }

                        if (results.get(i).toLowerCase().contains("entfernen".toLowerCase())) {
                            found = true;
                            String result = results.get(i);
                            Log.d("Result", result);
                            finalResult = result.replace("entfernen", "");
                            finalResult = finalResult.trim();
                            Log.d("Final Result", finalResult);
                            if (finalResult.toLowerCase().equals("alles")) {
                                MainActivity.removeAll();
                            } else {
                                MainActivity.removeItem(finalResult);
                            }
                        }
                    }
                }
            }
        });
    }

}

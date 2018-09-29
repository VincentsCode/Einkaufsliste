package jufo.vincent.de.einkaufsliste;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

class SpeechRecognizerManager {

    private AudioManager mAudioManager;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private onResultsReady mListener;

    private boolean mIsListening;
    private boolean mIsStreamSolo;


    SpeechRecognizerManager(Context context, onResultsReady listener) {
        Log.wtf("Logger", "SpeechRecognizerManager 0");
        mListener = listener;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        startListening();
        Log.wtf("Logger", "SpeechRecognizerManager 1");
    }


    private void listenAgain() {
        Log.wtf("Logger", "SpeechRecognizerManager: listenAgain 0");
        if (mIsListening) {
            mIsListening = false;
            mSpeechRecognizer.cancel();
            startListening();
            Log.wtf("Logger", "SpeechRecognizerManager: listenAgain X");
        }
        Log.wtf("Logger", "SpeechRecognizerManager: listenAgain 1");
    }

    private void startListening() {
        Log.wtf("Logger", "SpeechRecognizerManager: startListening 0");
        if (!mIsListening) {
            Log.wtf("Logger", "SpeechRecognizerManager: startListening X");
            mIsListening = true;
            if (!mIsStreamSolo) {

                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

                mIsStreamSolo = true;
            }
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            Log.wtf("Logger", "SpeechRecognizerManager: startListening 1");
        }
    }


    void destroy() {
        Log.wtf("Logger", "SpeechRecognizerManager: destroy 0");
        mIsListening = false;
        if (!mIsStreamSolo) {

            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

            mIsStreamSolo = true;
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
        Log.wtf("Logger", "SpeechRecognizerManager: destroy 1");
    }

    boolean ismIsListening() {
        Log.wtf("Logger", "SpeechRecognizerManager: ismIsListening");
        return mIsListening;
    }

    private class SpeechRecognitionListener implements RecognitionListener {

        public void onBeginningOfSpeech() {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onBeginningOfSpeech");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onEndOfSpeech");
        }

        public synchronized void onError(int error) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onError " + error);
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                if (mListener != null) {
                    ArrayList<String> errorList = new ArrayList<>(1);
                    errorList.add("ERROR RECOGNIZER BUSY");
                    if (mListener != null)
                        mListener.onResults(errorList);
                }
                return;
            }

            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                if (mListener != null)
                    mListener.onResults(null);
            }

            if (error == SpeechRecognizer.ERROR_NETWORK) {
                ArrayList<String> errorList = new ArrayList<>(1);
                errorList.add("STOPPED LISTENING");
                if (mListener != null)
                    mListener.onResults(errorList);
            }
            Log.wtf("Logger", String.valueOf(error));
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    listenAgain();
                }
            }, 100);
        }

        public void onEvent(int eventType, Bundle params) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onEvent");
        }

        public void onPartialResults(Bundle partialResults) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onPartialResults");
        }

        public void onReadyForSpeech(Bundle params) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onReadyForSpeech");
        }

        public void onResults(Bundle results) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onResults");
            if (results != null && mListener != null)
                mListener.onResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            listenAgain();
        }

        public void onRmsChanged(float rmsdB) {
            Log.wtf("Logger", "SpeechRecognizerManager: SpeechRecognitionListener: onRmsChanged");
        }

    }

    interface onResultsReady {

        void onResults(ArrayList<String> results);

    }

}

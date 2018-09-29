package jufo.vincent.de.einkaufsliste;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> arrayList;
    static ArrayAdapter<String> arrayAdapter;
    String[]  items = {};

    EditText editText;
    ListView listView;
    Button addBtn;
    FloatingActionButton fab;

    TextToSpeech tts;
    boolean ttsReady = false;

    long lastClick;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    static NotificationManager Manager;
    static NotificationCompat.Builder Builder;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Einkaufsliste");

        context = this;

        pref = getSharedPreferences("Einkaufsliste", 0);
        editor = pref.edit();
        editor.apply();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.GERMAN);
                    ttsReady = true;
                }
            }
        });

        Builder = new NotificationCompat.Builder(context);
        Manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Builder.setPriority(Notification.PRIORITY_MAX);
        Builder.setSmallIcon(R.drawable.add_btn);

        listView = (ListView) findViewById(R.id.list);
        editText = (EditText) findViewById(R.id.topText);
        addBtn = (Button) findViewById(R.id.add_btn);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        arrayList = new ArrayList<>(Arrays.asList(items));
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.txtitem, arrayList);
        listView.setAdapter(arrayAdapter);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newItem = editText.getText().toString();
                if (!newItem.trim().toLowerCase().equals("") && !newItem.isEmpty()){
                    if (!arrayList.contains(newItem) && !arrayList.contains(newItem.toLowerCase())) {
                        arrayList.add(0, newItem);
                        arrayAdapter.notifyDataSetChanged();
                        notificate();
                        editText.setText("");
                    }
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayList.size() != 0){
                    String textToSay = "Sie brauchen noch: ";
                    for(int i = 0; i < arrayList.size(); i++) {
                        textToSay = textToSay + arrayAdapter.getItem(i) + ", ";
                        if (i == arrayList.size() - 2) {
                            textToSay = textToSay + " und ";
                        }
                    }
                    speak(textToSay);
                } else {
                    speak("Sie haben alles gekauft, was sie brauchen!");
                }
            }
        });

        int len = pref.getInt("length", 1);

        for (int i = 0; i < len; i++) {
            arrayList.add(0, pref.getString(String.valueOf(i), "Error"));
        }

        Collections.reverse(arrayList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if(lastClick < System.currentTimeMillis() - 1000) {
                    speak(selectedItem);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                arrayList.remove(i);
                arrayAdapter.notifyDataSetChanged();
                lastClick = System.currentTimeMillis();
                notificate();
                return false;
            }
        });

        notificate();
        startService();

    }

    private void notificate() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Einkaufsliste");
        for(int i = 0; i < arrayList.size(); i++) {
            inboxStyle.addLine(String.valueOf(i + 1) + ". " + arrayAdapter.getItem(i));
        }
        Builder.setStyle(inboxStyle);

        if(arrayList.size() != 0) {
            Manager.notify(1, Builder.build());
        } else {
            Manager.cancel(1);
        }

    }

    private void speak(String text) {
        if(ttsReady) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);

        } else {
            Toast.makeText(this, "TTS not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void startService() {
        Intent i = new Intent(this, BackgroundRecognizerService.class);
        startService(i);
        Log.wtf("Logger", "MainActivity: SERVICE STARTED");
    }

    public static void addItem(String item) {
        if (!item.trim().toLowerCase().equals("") && !item.isEmpty()){
            if (!arrayList.contains(item) && !arrayList.contains(item.toLowerCase())) {
                arrayList.add(0, item);
                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(Builder.mContext, item + " hinzugefüt.", Toast.LENGTH_SHORT).show();
            }
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Einkaufsliste");
        for(int i = 0; i < arrayList.size(); i++) {
            inboxStyle.addLine(String.valueOf(i + 1) + ". " + arrayAdapter.getItem(i));
        }
        Builder.setStyle(inboxStyle);

        if(arrayList.size() != 0) {
            Manager.notify(1, Builder.build());
        } else {
            Manager.cancel(1);
        }
    }

    public static void removeItem(String item) {
        if(arrayList.contains(item) || arrayList.contains(item.toLowerCase())) {
            int index = arrayList.indexOf(item);
            arrayList.remove(index);
            arrayAdapter.notifyDataSetChanged();
            Toast.makeText(Builder.mContext, item + " entfernt.", Toast.LENGTH_SHORT).show();
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Einkaufsliste");
        for(int i = 0; i < arrayList.size(); i++) {
            inboxStyle.addLine(String.valueOf(i + 1) + ". " + arrayAdapter.getItem(i));
        }
        Builder.setStyle(inboxStyle);

        if(arrayList.size() != 0) {
            Manager.notify(1, Builder.build());
        } else {
            Manager.cancel(1);
        }
    }

    public static void removeAll() {
        arrayList.clear();
        arrayAdapter.notifyDataSetChanged();
        Toast.makeText(Builder.mContext, "Liste geleert", Toast.LENGTH_SHORT).show();
        Manager.cancel(1);

    }

    public void saveItems() {
        for(int i = 0; i < arrayList.size(); i++) {
            editor.putString(String.valueOf(i), arrayAdapter.getItem(i));
        }
        editor.putInt("length", arrayList.size());
        editor.apply();
        editor.commit();
        notificate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveItems();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        saveItems();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveItems();
    }
}

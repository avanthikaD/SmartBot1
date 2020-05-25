package com.softwareproject.SmartBot;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Parcelable;
import android.provider.CalendarContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;



import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;



public class HomeActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Intent intent;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO =0;

    DBHelper dBHelper;
    SQLiteDatabase database;

    protected EditText message;
    protected ListView lv;
    protected ArrayList<ChatMessage> messages;
    protected ChatAdapter adapter;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.setTitle("");


        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_ALARM}, PackageManager.PERMISSION_GRANTED);


        message = findViewById(R.id.et_message);
        lv = findViewById(R.id.listView);
        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages);

        dBHelper = new DBHelper(this);
        database = dBHelper.getWritableDatabase();

        Cursor cursor = database.query(DBHelper.TABLE_MESSAGES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int messageIndex = cursor.getColumnIndex(DBHelper.FIELD_MESSAGE);
            int sendIndex = cursor.getColumnIndex(DBHelper.FIELD_SEND);

            do {
                MessageEntity entity = new MessageEntity(
                        cursor.getString(messageIndex),
                        cursor.getInt(sendIndex));
                ChatMessage text = null;
                try {
                    text = new ChatMessage(entity);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                messages.add(text);
            } while (cursor.moveToNext());
        }
        cursor.close();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    textToSpeech.speak("Hello! I am SmartBot How Can I help you?", TextToSpeech.QUEUE_FLUSH, null);
                    String s = "Hello! I am SmartBot, how can i help you?";
                    ReceivedMessage(lv, adapter, messages, s);

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });


        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {
                    message.setText(matches.get(0));
                    SendMessage(lv, adapter, messages, message);
                    BotAction(lv, adapter, messages, matches);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        findViewById(R.id.btn_mic).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_UP:
                        message.setHint("");
                        speechRecognizer.stopListening();
                        break;
                    case  MotionEvent.ACTION_DOWN :
                        message.setText("");
                        if (ContextCompat.checkSelfPermission(HomeActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(HomeActivity.this,"Need permission to record audio for voice input",Toast.LENGTH_LONG).show();
                        }else {
                            speechRecognizer.startListening(intent);
                            message.setHint("listening...");
                        }
                        break;

                }
                return false;
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> matches = new ArrayList<>();
                matches.add(message.getText().toString().trim());
                message.setText(matches.get(0));
                SendMessage(lv, adapter, messages, message);
                BotAction(lv, adapter, messages, matches);
            }
        });

    }

    public void SendMessage(ListView lv, ChatAdapter adapter, ArrayList<ChatMessage> messages, EditText message) {

        if (!TextUtils.isEmpty(String.valueOf(message.getText()))) {
            messages.add(new ChatMessage(String.valueOf(message.getText()), true));

        }
        lv.setAdapter(adapter);
        message.setText("");
        lv.setSelection(adapter.getCount() - 1);
    }

    public void ReceivedMessage(ListView lv, ChatAdapter adapter, ArrayList<ChatMessage> messages, String message) {

        messages.add(new ChatMessage(String.valueOf(message), false));
        lv.setAdapter(adapter);
        lv.setSelection(adapter.getCount() - 1);
    }

    private void BotAction(ListView lv, ChatAdapter adapter, ArrayList<ChatMessage> messages, ArrayList<String> matches) {
        if (matches.get(0).contains("hi") || matches.get(0).contains("hello") || matches.get(0).contains("hey") || matches.get(0).contains("howdy")) {
            textToSpeech.speak("hello lovely human!", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "hello lovely human!");
        } else if (matches.get(0).contains("how are you") || matches.get(0).contains("how do you do")) {
            textToSpeech.speak("I'm doing great! thanks for asking. How about you", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "I'm doing great! thanks for asking. How about you?");
        } else if (matches.get(0).contains("am good") || matches.get(0).contains("am fine") || matches.get(0).contains("am great") || matches.get(0).contains("amazing") || matches.get(0).contains("super")) {
            textToSpeech.speak("Wow.I wish you to stay as amazing as you are", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "Wow.I wish you to stay as amazing as you are");
        } else if (matches.get(0).contains("am not good") || matches.get(0).contains("am not feeling well") || matches.get(0).contains("am not fine")) {
            textToSpeech.speak("Its Okay not to be okay some times,Be Strong and Keep Going", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "Its Okay not to be okay some times,Be Strong and Keep Going!");
        } else if (matches.get(0).contains("thank you") || matches.get(0).contains("thanks")) {
            textToSpeech.speak("Its my pleasure serving you", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "Its my pleasure serving you");
        } else if (matches.get(0).contains("sorry")) {
            textToSpeech.speak("Its Aright,not a problem", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "Its Aright,not a problem");
        } else if (matches.get(0).contains("love you") || matches.get(0).contains("love")) {
            textToSpeech.speak("I love you too honey!", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "I love you too honey!");
        } else if (matches.get(0).contains("miss you") || matches.get(0).contains("miss")) {
            textToSpeech.speak("I am right here, dont miss me!", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "I am right here, dont miss me!");

        }else if(matches.get(0).contains("tell me a joke")){
            textToSpeech.speak(" how do you throw a party in space?..You plan it",TextToSpeech.QUEUE_FLUSH,null);
            ReceivedMessage(lv,adapter,messages,"how do you throw a party in space?..You planet(plan-it)");

        }else if(matches.get(0).contains("what is your name")||matches.get(0).contains("tell me your name")||matches.get(0).contains("who are you")||matches.get(0).contains("what's your name")){
            textToSpeech.speak(" I am SmartBot, your personal assistant",TextToSpeech.QUEUE_FLUSH,null);
            ReceivedMessage(lv,adapter,messages,"I am SmartBot, your personal assistant");

        }else if(matches.get(0).contains("what can you do")||matches.get(0).contains("what will you do")||matches.get(0).contains("what are your capabilities")){
            textToSpeech.speak(" i can assist you in making a call, send text message,create event,browse, set an alarm or open any application",TextToSpeech.QUEUE_FLUSH,null);
            ReceivedMessage(lv,adapter,messages,"i can assist you in making a call, send text message,create event,browse, set an alarm or open any application");

        }else if (matches.get(0).contains("call")) {

            String string = matches.get(0);
            string= string.replace("call","");

            textToSpeech.speak("Okay! dial the number here to call "+ string, TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "OK! Dial the number here to call" + string);

            Intent search = new Intent(Intent.ACTION_DIAL);
            search.putExtra(SearchManager.QUERY, string);
            startActivity(search);

        }else if (matches.get(0).contains("camera")) {

            String string = matches.get(0);
            string = string.replace("camera","");

            textToSpeech.speak("Opening Camera", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"Opening camera");

            Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivity(camera);

        }else if((matches.get(0).contains("open settings")) ){
            textToSpeech.speak("Opening Settings", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"Opening Settings");
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);


        } else if (matches.get(0).contains("event")) {

            String string = matches.get(0);
            string = string.replace("create an event","");

            textToSpeech.speak("Done! Lets Create Event "+string , TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"Done! Lets Create Event "+string);

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra(CalendarContract.Events.TITLE, string);
            intent.setData(CalendarContract.Events.CONTENT_URI);
            startActivity(intent);
        }
        else if (matches.get(0).contains("alarm")) {

            String string = matches.get(0);
            string = string.replace("alarm","");

            textToSpeech.speak("ok! lets set your alarm. choose time " , TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv, adapter, messages, "Ok! Let's set your alarm, choose time");

            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(),"time picker");
        }

        else  if(matches.get(0).contains("message")||matches.get(0).contains("text message")||matches.get(0).contains("SMS")){

            String string = matches.get(0);
            string = string.replace("message","");

            textToSpeech.speak("Sure Lets compose the message" , TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"Sure! Lets compose the message");

            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("sms:"));
            smsIntent.putExtra("sms_body",string);
            startActivity(smsIntent);

        } else if (matches.get(0).contains("open")){

            String string = matches.get(0);
            string = string.replace("open","");
            string =string.replace(" ","");
            textToSpeech.speak("Searching for application "+string, TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"Searching for application "+string);

            List<ApplicationInfo> applicationsInfo;
            PackageManager manager = getPackageManager();
            boolean isMatch = false;
            ApplicationInfo appInfo =null;
            String appName = null;

            try {
                applicationsInfo = manager.getInstalledApplications(0);
                if (applicationsInfo == null)
                    throw new PackageManager.NameNotFoundException();

                for(int i=0;i<applicationsInfo.size();i++) {
                    appInfo = applicationsInfo.get(i);

                    appName = (String) manager.getApplicationLabel(appInfo);

                    if(appName.toLowerCase().contains(string.toLowerCase())){
                        isMatch =true;
                        break;
                    }
                }
                if(isMatch){
                    textToSpeech.speak("Opening application "+appName , TextToSpeech.QUEUE_FLUSH, null);
                    ReceivedMessage(lv,adapter,messages,"Opening Application"+appName);

                    Intent i = manager.getLaunchIntentForPackage(appInfo.packageName);
                    if (i != null) {
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                    }
                    startActivity(i);
                }else{
                    textToSpeech.speak("I'm sorry, I didn't find application "+string , TextToSpeech.QUEUE_FLUSH, null);
                    ReceivedMessage(lv,adapter,messages,"I'm sorry I didn't find application"+string);

                }


            } catch (PackageManager.NameNotFoundException e) {
                textToSpeech.speak("I'm sorry, I didn't find application "+string , TextToSpeech.QUEUE_FLUSH, null);
                ReceivedMessage(lv,adapter,messages,"I'm sorry I didn't find application"+string);

            }
        }else if (matches.get(0).contains("search") || matches.get(0).contains("what")
                || matches.get(0).contains("who") || matches.get(0).contains("how") ||
                matches.get(0).contains("browse") || matches.get(0).contains("find")||matches.get(0).contains("show")){
            if(checkConnection() == true) {
                String string = matches.get(0);

                string = string.replace("search", "");

                textToSpeech.speak("searching " + string, TextToSpeech.QUEUE_FLUSH, null);
                ReceivedMessage(lv, adapter, messages, "searching " + string);

                Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                search.putExtra(SearchManager.QUERY, string);
                startActivity(search);
            }
        }

        else{

            textToSpeech.speak("I'm sorry I don't understand, can you please try again", TextToSpeech.QUEUE_FLUSH, null);
            ReceivedMessage(lv,adapter,messages,"I'm sorry I don't understand, can you please try again");

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hourOfDay);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND,0);
        updateTimeText(c);
        startAlarm(c);
    }
    private void updateTimeText(Calendar c){
        String timeText = "Alarm set for: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        ReceivedMessage(lv,adapter,messages,timeText);
        textToSpeech.speak(timeText,TextToSpeech.QUEUE_FLUSH,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        if(c.before(Calendar.getInstance())){
            c.add(Calendar.DATE,1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
    }

    public boolean checkConnection(){
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if(null!=activeNetwork){
            if(activeNetwork.getType()== ConnectivityManager.TYPE_WIFI){
                Toast.makeText(this, "Wifi Enabled", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if(activeNetwork.getType()== ConnectivityManager.TYPE_MOBILE){
                Toast.makeText(this, "Mobile Data Enabled", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        else{
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        item = findViewById(R.id.logout_menu);
        SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("remember","false");
        editor.apply();
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            return;

    }

    @Override
    public void onDestroy() {
        database.close();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    @Override
    public void onStop() {
        super.onStop();
        database.delete(DBHelper.TABLE_MESSAGES, null, null);
        for (ChatMessage message : messages) {
            MessageEntity entity = new MessageEntity(message);

            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_MESSAGE, entity.text);
            contentValues.put(DBHelper.FIELD_SEND, entity.isSend);

            database.insert(DBHelper.TABLE_MESSAGES, null, contentValues);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) messages);
        Log.i("LOG", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("LOG", "onRestoreInstanceState");
        messages = savedInstanceState.getParcelableArrayList("list");
        adapter.notifyDataSetChanged();
        lv.setSelection(adapter.getCount() - 1);
    }

}

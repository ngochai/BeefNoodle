package net.phamngochai.beefnoodle;

import java.util.ArrayList;

import net.phamngochai.beefnoodle.dictionary.SunDict;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private String TAG = "MainActivity";    
    private SunDict dict = null;
    private ArrayList<String> wordList = null;
    
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    
    BroadcastReceiver mediaActionReceiver;
    SharedPreferences settings;
    
    public static final String DICT_LOCATION = "DICT_LOCATION";
    public static final String STORAGE_LOCATION = "STORAGE_LOCATION";

    private String dictLoc = null;
    private String storeLoc = null;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Log.i(TAG, Environment.getDataDirectory().getAbsolutePath());
        //dict = new StarDict(Environment.getDataDirectory().getAbsolutePath() +"/net.phamngochai.beefnoodle/dictd_anh-viet");
        settings = getPreferences(0);
        storeLoc = settings.getString(STORAGE_LOCATION, null);
        Log.d(TAG, "storeLoc: " + storeLoc);
        if (storeLoc == null) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("You don't have any dictionary, do you want to download some from internet?")
        	       .setCancelable(false)
        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                
        	           }
        	       })
        	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                MainActivity.this.finish();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
        	Log.d(TAG, "AlertDialog created");
        }
        
        
        dictLoc = settings.getString(DICT_LOCATION, null);
        if (dictLoc == null) {
        	
        }
        
        
        checkStorage();
        IntentFilter filterMediaMounted = new IntentFilter (Intent.ACTION_MEDIA_MOUNTED);
        IntentFilter filterMediaRemoved = new IntentFilter (Intent.ACTION_MEDIA_REMOVED);        
        mediaActionReceiver  = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent intent) {
            	checkStorage();
            }
        };        
        registerReceiver(this.mediaActionReceiver, new IntentFilter(filterMediaMounted));
        registerReceiver(this.mediaActionReceiver, new IntentFilter(filterMediaRemoved));
        
//        if (dict == null) {
//            dict = new SunDict("/sdcard/data/net.phamngochai.beefnoodle/dictd_anh-viet");
//            wordList = dict.getWordList();
//        }
    }
    
    @Override
    public void onStop() {
    	unregisterReceiver(mediaActionReceiver);
    }
    
    public void searchForWord(View view) {
        StringBuffer sb = new StringBuffer();
        EditText wordText = (EditText)findViewById(R.id.Word);
        Log.i(TAG, "Looking for: " + wordText.getText().toString());
        int wordPos = wordList.indexOf(wordText.getText().toString());
        if (wordPos == -1) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("No such word");
            alert.show();
        } else {
            dict.getWord(wordPos, sb);
            TextView meaning = (TextView)findViewById(R.id.Meanning);
            meaning.setText(sb.toString());
            meaning.setMovementMethod(new ScrollingMovementMethod());            
        }
        
    }
    
    private void checkStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }
    
}
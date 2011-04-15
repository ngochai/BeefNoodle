package net.phamngochai.beefnoodle;

import java.util.ArrayList;

import net.phamngochai.beefnoodle.dictionary.SunDict;

import android.app.Activity;
import android.app.AlertDialog;
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Log.i(TAG, Environment.getDataDirectory().getAbsolutePath());
        //dict = new StarDict(Environment.getDataDirectory().getAbsolutePath() +"/net.phamngochai.beefnoodle/dictd_anh-viet");
        if (dict == null) {
            dict = new SunDict("/sdcard/data/net.phamngochai.beefnoodle/dictd_anh-viet");
            wordList = dict.getWordList();
        }
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
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
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
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }        
    }
    
}
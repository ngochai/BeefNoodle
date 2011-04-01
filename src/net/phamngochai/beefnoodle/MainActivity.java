package net.phamngochai.beefnoodle;

import java.util.ArrayList;

import com.googlecode.toolkits.stardict.StarDict;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class MainActivity extends Activity {
    
    private String TAG = "MainActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG, Environment.getDataDirectory().getAbsolutePath());
        StarDict dict = new StarDict("/data/data/net.phamngochai.beefnoodle/dictd_anh-viet");
        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < 1; i++) {            
//            dict.getWord(i, sb);
//            Log.i(TAG, "Word " + i + " is: " + sb.toString());
//        }
        ArrayList<String> wordList = dict.getWordList();
        dict.getWord(wordList.indexOf("amazing"), sb);
        Log.i(TAG, "amazing means" + sb.toString());
        dict.getWord(wordList.indexOf("wow"), sb);
        Log.i(TAG, "wow means" + sb.toString());
    }
    
}
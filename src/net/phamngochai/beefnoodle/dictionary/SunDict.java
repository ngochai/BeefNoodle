package net.phamngochai.beefnoodle.dictionary;

import java.io.IOException;
import java.util.ArrayList;

import com.googlecode.toolkits.stardict.StarDict;


public class SunDict extends StarDict {
        
    public SunDict(String dictionaryName) {
        super(dictionaryName);
    }
    
    public ArrayList<String> getWordList() {
        ArrayList<String> words = new ArrayList<String>();
        long offset = 0;
        int maxReadSize = 8192;
        //System.out.println("Total words: " + words.size());
        byte [] buffer = new byte[maxReadSize];
        int readSize = maxReadSize;
        int wordStart;
        StringBuffer word = new StringBuffer();
        int pos;        
        try {
            wordStart = 0;
            pos = 0;            
            do {
                this.index.seek(offset);
                readSize = this.index.read(buffer);
                //System.out.println(readSize);
                if (readSize == -1)
                    break;
                do {
                    //System.out.println("readSize " + readSize + " i " + i + " ws " + wordStart);
                    if (buffer[pos] == '\0') {
                        word.append(new String(buffer, wordStart, pos - wordStart, "UTF-8"));
                        words.add(word.toString());
                        //word.delete(0, word.length());
                        //System.out.println(word);
                        word = new StringBuffer();
                        if (pos + 9 < readSize) {
                            pos = wordStart = pos + 9;
                        } else {
                            //System.out.println("Breaking readSize " + readSize + " pos " + pos + " ws " + wordStart);
                            pos = wordStart = pos + 9 - readSize;                            
                            break;
                        }
                    } else {
                        pos++;
                        if (pos == readSize) {
                            //System.out.println("Data in the tail readSize " + readSize + " pos " + pos + " ws " + wordStart);
                            word.append(new String(buffer, wordStart, pos - wordStart, "UTF-8"));
                            pos = wordStart = 0;
                            break;
                        }
                    }
                } while(pos < readSize);
                offset += readSize;
            } while (true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Total words: " + words.size());
        
        return words;
    }
    
}

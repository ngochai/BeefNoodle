/*
Copyright 2009 http://code.google.com/p/toolkits/. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials provided
    with the distribution.
  * Neither the name of http://code.google.com/p/toolkits/ nor the names of its
    contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.googlecode.toolkits.stardict;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class StarDict {
    
    RandomAccessFile index;
    RandomAccessFile yaindex;
    DictZipFile dz;
    String dictname;
    
    
    public StarDict() {
        this("g:\\stardict\\dict");
    }
    
    /**
     * 
     * @param dictname
     */
    public StarDict(String dictname) {
        try {
            this.dictname = dictname;
            this.index = new RandomAccessFile(dictname+".idx","r");
            this.dz = new DictZipFile(dictname+".dict.dz");
            this.yaindex = new RandomAccessFile(dictname+".yaidx","r");
            
            //this.dz.runtest();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param p
     * @return the p-th word
     */
    public String getWord(int p, StringBuffer exp) {
        String word = null;
        byte [] buffer = new byte[1024];
        int dataoffset = 0;
        int datasize = 0;
        int offset = 0; // the offset of the p-th word in this.index
        try {
            this.yaindex.seek(p*4);
            int size = this.yaindex.read(buffer, 0, 4);
            if (size!=4) {
                throw new Exception("Read Index Error");
            }
            for(int i=0;i<4;i++) {
                offset<<=8;
                offset|=buffer[i]&0xff;
            }
            this.index.seek(offset);
            size = this.index.read(buffer, 0, 1024);
            for(int i=0;i<size;i++) {
                if (buffer[i]==0) {
                    word = new String(buffer, 0, i, "UTF8");
                    dataoffset = 0;
                    datasize = 0;
                    for (int j=i+1;j<i+5;j++) {
                        dataoffset<<=8;
                        dataoffset|=buffer[j]&0xff;
                    }
                    for (int j=i+5;j<i+9;j++) {
                        datasize<<=8;
                        datasize|=buffer[j]&0xff;
                    }
                    break;
                }
            }
            System.out.println(datasize);
            buffer = new byte[datasize];
            this.dz.seek(dataoffset);
            System.out.println(this.dz.read(buffer, datasize));
            exp.append(new String(buffer,"UTF-8"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return word;
    }
    
    public ArrayList<String> getWordList() {
        ArrayList<String> words = new ArrayList<String>();
        long offset = 0;
        int maxReadSize = 8192;
        System.out.println("Total words: " + words.size());
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
        System.out.println("Total words: " + words.size());
        
        return words;
    }
    
    public String getVersion() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.dictname+".ifo"));
            String line = br.readLine();
            while(line != null) {
                String [] version = line.split("=");
                if (version.length == 2 && version[0].equals("version")) {
                    return version[1];
                }
                line = br.readLine();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return "UNKNOWN VERSION";
    }
    
    /**
     * @param args
     */
//    public static void main(String[] args) {
//        StarDict dict = new StarDict();
//        StringBuffer sb = new StringBuffer();
//        //System.out.println(dict.getVersion());
//        System.out.println(dict.getWord(400000, sb));
//        //System.out.println(sb.toString());
//    }
}

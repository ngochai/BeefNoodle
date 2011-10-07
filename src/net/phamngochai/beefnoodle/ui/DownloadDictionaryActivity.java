package net.phamngochai.beefnoodle.ui;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.phamngochai.beefnoodle.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

public class DownloadDictionaryActivity extends ListActivity {
	
	private static final String TAG = "DownloadDictionaryActivity";
	private static final String DICTIONARY_LIST_URL = "http://phamngochai.net/beefnoodle/list.xml"; 
	
	ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloaddictionary);
    }
    
    
    public void downloadAndShowDictionaryList(View view) {
    	String xml = getDictionaryListFromInternet();
    	if (xml == null) {
    		showAlertDialog("Cannot load the list from internet");
    		return;
    	}
    	Document doc = stringToXMLDoc(xml);
    	if (doc == null) {
    		showAlertDialog("Cannot parse the list");
    		return;
    	}
    	
    	
    	NodeList nodes = doc.getElementsByTagName("dictionary");

		//fill in the list items from the XML document
		for (int i = 0; i < nodes.getLength(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			Element e = (Element)nodes.item(i);			
			map.put("Name", e.getElementsByTagName("name").item(0).getTextContent());
			Log.d(TAG, "Name: " + e.getElementsByTagName("name").item(0).getTextContent());
        	map.put("Description", e.getElementsByTagName("description").item(0).getTextContent());
        	Log.d(TAG, "Description: " + e.getElementsByTagName("description").item(0).getTextContent());
        	map.put("License", e.getElementsByTagName("license").item(0).getTextContent());
        	Log.d(TAG, "License: " + e.getElementsByTagName("license").item(0).getTextContent());
        	mylist.add(map);
		}

		//Make a new listadapter
		
        ListAdapter adapter = new SimpleAdapter(this, mylist , R.layout.dictionarylistrow,
                        new String[] { "Name", "Description", "License" },
                        new int[] { R.id.dictionary_name, R.id.dictionary_description, R.id.dictionary_license });

        setListAdapter(adapter);
            	
    	
    }
    
    private String getDictionaryListFromInternet() {
    	String xml = null;
    	try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(DICTIONARY_LIST_URL);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);
    	} catch (IOException e) {
    		Log.d(TAG, "Exception while loading xml: " + e.getMessage() + ", caused by: " + e.getCause());
    		return null;
    	}
    	Log.d(TAG, xml);
    	return xml;
    }
    
    
	public Document stringToXMLDoc(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.d(TAG, "XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.d(TAG, "Wrong XML file structure: " + e.getMessage());
			return null;
		} catch (IOException e) {
			Log.d(TAG, "I/O exeption: " + e.getMessage());
			return null;
		}
		return doc;
	}
	
	public String getElementValue(Element e, String str) {		       
		NodeList n = e.getElementsByTagName(str);
		return n.item(0).getTextContent();		
	}
	
	public void showAlertDialog(String str) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(str)
    	       .setCancelable(false)
    	       .setNeutralButton("OK", null);
    	AlertDialog alert = builder.create();
    	alert.show();  		
	}
}

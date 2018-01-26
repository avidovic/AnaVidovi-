package com.example.avidovic.anavidovic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {

    private int MY_PERM=1;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBAdapter db = new DBAdapter(this);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                R.id.list);
        //setListAdapter(adapter); ?? ne radi?!

        //ask for permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, MY_PERM);

        //inseert u bazu
        db.open();
        long id = db.insertPage("https://web.math.pmf.unizg.hr/~karaga/android/images_2/permission1.txt", "android");
        id = db.insertPage("https://web.math.pmf.unizg.hr/~karaga/android/images_2/staticdatoteka.txt", "programiranje");
        id = db.insertPage("https://web.math.pmf.unizg.hr/~karaga/android/images_2/permissionsmanifest.txt", "android");
        id = db.insertPage("https://web.math.pmf.unizg.hr/~karaga/android/images_2/permissionsdopuna.txt", "android");
        id = db.insertPage("https://web.math.pmf.unizg.hr/~karaga/android/images_2/smsxml.txt", "programiranje");
        db.close();

        db.open();
        Cursor cu = db.getPage(3);
        if (cu.moveToFirst()) {
            EditText text = (EditText) findViewById(R.id.link);
            text.setText(cu.getString(1));
        }
        else
            Toast.makeText(this, "No contact found", Toast.LENGTH_LONG).show();
        db.close();

        //ispis iz baze
        db.open();
        Cursor c = db.getAllPages();
        if (c.moveToFirst())
        {
            do {
                adapter.add(c.getString(1));
            } while (c.moveToNext());
        }
        db.close();

    }

    public void onClickDownload(View view)
    {
        String link = ((EditText)findViewById(R.id.link)).getText().toString();
        new DownloadTextTask().execute(link);
    }

    private void sendSMS(String phoneNumber, String message)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private InputStream OpenHttpConnection(String urlString)
            throws IOException
    {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex)
        {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }

    private String DownloadText(String URL)
    {
        int BUFFER_SIZE = 2000;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
        } catch (IOException e) {
            Log.d("NetworkingActivity", e.getLocalizedMessage());
            return "";
        }

        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];
        try {
            while ((charRead = isr.read(inputBuffer))>0) {
                //---convert the chars to a String---
                String readString =
                        String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            Log.d("NetworkingActivity", e.getLocalizedMessage());
            return "";
        }
        return str;
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return DownloadText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            TextView mTitle = (TextView) findViewById(R.id.tekst);

            mTitle.append(result); // append string like a variable value

            //posalji sms
//            String link = ((EditText)findViewById(R.id.link)).getText().toString();
//            sendSMS("5556", "File s linka: " + link + " je downloadan.");

        }
    }
}

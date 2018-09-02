package com.hptestapp.liju_assignment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ListView lv_images;
    MyCustomAdapter dataAdapter;
    ProgressDialog pd;
    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactList=new ArrayList<>();
        lv_images=(ListView)findViewById(R.id.lv_images);

        new getdata().execute();

    }


    public class getdata extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading Data...Please Wait");
            pd.setCancelable(false);
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "https://picsum.photos/list";
            String jsonStr = sh.makeServiceCall(url);


            if (jsonStr != null) {
                try {

                    // Getting JSON Array node
                    JSONArray contacts = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < 19; i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String id = c.getString("id");
                        String filename = c.getString("filename");
                        String post_url = c.getString("post_url");



                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("filename", filename);
                        contact.put("post_url", post_url);


                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            pd.dismiss();

            dataAdapter = new MyCustomAdapter(MainActivity.this, contactList);
            lv_images.setAdapter(dataAdapter);
            dataAdapter.notifyDataSetChanged();

            super.onPostExecute(s);
        }
    }


    public class MyCustomAdapter extends ArrayAdapter<String> {
        Context context;
        private ArrayList<HashMap<String, String>> approve;


        public MyCustomAdapter(Context context, ArrayList<HashMap<String, String>> approvals) {
            super(context, R.layout.list_nonvisit_approval);
            this.context = context;
            this.approve = approvals;
        }

        @Override
        public int getCount() {
            return approve.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.list_nonvisit_approval, null);

                holder = new ViewHolder();
                holder.imageid = (TextView) convertView.findViewById(R.id.imageid);
                holder.btn_download = (Button) convertView.findViewById(R.id.btn_download);



                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final HashMap<String, String> nva = approve.get(position);

            holder.imageid.setText(approve.get(position).get("filename"));

            holder.btn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str=approve.get(position).get("post_url");
                    new Downloader().execute(str);

                    approve.remove(position);
                    dataAdapter = new MyCustomAdapter(MainActivity.this, contactList);
                    lv_images.setAdapter(dataAdapter);
                    dataAdapter.notifyDataSetChanged();

                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView imageid;
            Button btn_download;
        }
    }
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Download");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File(myDir, fname);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class Downloader extends AsyncTask<String,Void,Bitmap>{
        @Override
        protected void onPostExecute(Bitmap bitmap) {

            SaveImage(bitmap);

            //DISMISS
            pd.dismiss();

            super.onPostExecute(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... url) {

            String myurl=url[0];
            Bitmap bm=null;
            InputStream is=null;

            try
            {
                is=new URL(myurl).openStream();

                //DECODE
                bm=BitmapFactory.decodeStream(is);

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return bm;

        }

        @Override
        protected void onPreExecute() {
            pd=new ProgressDialog(MainActivity.this);
            pd.setTitle("Image Downloader");
            pd.setMessage("Downloading...");
            pd.setIndeterminate(false);
            pd.show();
            super.onPreExecute();
        }
    }

}

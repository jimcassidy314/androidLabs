package com.example.jimcassidy.androidlabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.jimcassidy.androidlabs.StartActivity.ACTIVITY_NAME;

public class WeatherActivity extends Activity {
    public ProgressBar progress;
    public TextView current_temp;
    public TextView min_temp;
    public TextView max_temp;
    public TextView wind_speed;
    public ImageView weatherImage;
    protected static final String ACTIVITY_NAME="WeatherActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        progress = (ProgressBar) findViewById(R.id.progress);

        progress.setVisibility(View.VISIBLE);

        current_temp = (TextView) findViewById(R.id.current);
        min_temp = (TextView) findViewById(R.id.min);
        max_temp = (TextView) findViewById(R.id.max);
        wind_speed = (TextView) findViewById(R.id.wind_speed);
        weatherImage = (ImageView) findViewById(R.id.weather_image);

        ForecastQuery fq = new ForecastQuery();
        String url = ("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");

        fq.execute(url);
    }


    public class ForecastQuery extends AsyncTask<String, Integer, String> {
        String wind;
        String speed;
        String min;
        String max;
        String current;
        String icon_name;
        String weather;
        Bitmap icon;

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream stream = conn.getInputStream();

                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG){
                        continue;
                    }
                    if (parser.getName().equals("temperature")) {
                        current = parser.getAttributeValue(null, "value");
                        publishProgress(33);
                        min = parser.getAttributeValue(null, "min");
                        publishProgress(66);
                        max = parser.getAttributeValue(null, "max");
                        publishProgress(100);
                    }

                    if (parser.getName().equals("speed")) {
                        wind = parser.getAttributeValue(null, "name");
                        publishProgress(50);
                        speed = parser.getAttributeValue(null, "value");
                        publishProgress(100);

                    }


                    if (parser.getName().equals("weather")) {
                        icon_name = parser.getAttributeValue(null, "icon");
                        weather = parser.getAttributeValue(null, "value");
                        String icon_location = icon_name + ".png";
                        Log.i(ACTIVITY_NAME,"looking for "+icon_location);
                        if (fileExistance(icon_location)) {
                            Log.i(ACTIVITY_NAME,"image found locally");
                            FileInputStream fstream = null;
                            File icon_file= getBaseContext().getFileStreamPath(icon_name);
                            try {
                                fstream = new FileInputStream(icon_file);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            icon = BitmapFactory.decodeStream(fstream);
                        } else {
                            Log.i(ACTIVITY_NAME,"image downloaded");
                            icon = getImage("http://openweathermap.org/img/w/" + icon_name + ".png");
                            FileOutputStream outputStream = openFileOutput(icon_name + ".png", Context.MODE_PRIVATE);
                            icon.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                            outputStream.flush();
                            outputStream.close();

                        }

                        publishProgress(100);

                    }
                }

            } catch (Exception exc) {
                exc.getMessage();
            }

            return null;
        }

        @Override
        public void onProgressUpdate(Integer... value) {
            progress.setVisibility(View.VISIBLE);
            progress.setProgress(value[0]);

        }

        @Override
        public void onPostExecute(String value) {
            current_temp.setText(current_temp.getText()+" "+current + " C");
            min_temp.setText(min_temp.getText()+" "+min + " C");
            max_temp.setText(max_temp.getText()+" "+ max + " C");
            wind_speed.setText(wind_speed.getText()+" "+ speed + " KM/h");
            weatherImage.setImageBitmap(icon);
            progress.setVisibility(View.INVISIBLE);

        }

        public boolean fileExistance(String fname) {
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }

        public Bitmap getImage(URL url) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return BitmapFactory.decodeStream(connection.getInputStream());
                } else
                    return null;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        public Bitmap getImage(String urlString) {
            try {
                URL url = new URL(urlString);
                return getImage(url);
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }
}

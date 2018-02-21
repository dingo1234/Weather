package com.example.emir.theweatherapp2;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import Util.Utils;
import data.CityPreferences;
import data.JSONWeatherParser;
import data.WeatherHttpClient;
import model.Weather;

public class MainActivity extends AppCompatActivity {

    private TextView cityName;
    private TextView temp;
    private ImageView iconView;
    private TextView description;
    private TextView humidity;
    private TextView pressure;
    private TextView wind;
    private TextView sunrise;
    private TextView sunset;
    private TextView updated;

    Weather weather=new Weather();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cityName=(TextView) findViewById(R.id.cityText);
        iconView=(ImageView) findViewById(R.id.thumbNail);
        temp=(TextView) findViewById(R.id.tempText);
        description=(TextView) findViewById(R.id.cloudText);
        humidity=(TextView) findViewById(R.id.humidText);
        pressure=(TextView) findViewById(R.id.pressureText);
        wind=(TextView) findViewById(R.id.windText);
        sunrise=(TextView) findViewById(R.id.riseText);
        sunset=(TextView) findViewById(R.id.setText);
        updated=(TextView) findViewById(R.id.updateText);
        CityPreferences cityPreferences=new CityPreferences(MainActivity.this);

        //renderWeatherData(cityPreferences.getCity());
        //String city="Brcko,BA";

        //WeatherTask task = new WeatherTask();
        //task.execute(new String[]{city  +"appid=21760d1e02badf3a7ff2d2f07628e2c"+ "&units=metric"});;
    }
 public void renderWeatherData(String city){
     WeatherTask weatherTask=new WeatherTask();
     weatherTask.execute(city  + Utils.APP_ID+ "&units=metric");


 }
    private void showInputDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change City");
        final EditText cityInput=new EditText(MainActivity.this);
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        cityInput.setHint("Brcko,BA");
        builder.setView(cityInput);
        builder.setPositiveButton("Submit",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                CityPreferences cityPreferences=new CityPreferences(MainActivity.this);
                cityPreferences.setCity(cityInput.getText().toString());

                String newCity=cityPreferences.getCity();
                //renderWeatherData(newCity);
            }
        });
        builder.show();
    }

 @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        if( id==R.id.change_CityID){
            showInputDialog();
        }
        return super.onOptionsItemSelected(item);
    }

         private class DownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap>{


            @Override
            protected Bitmap doInBackground(String... params) {

                return downloadImage(params[0]);
            }
             @Override
             protected void onPostExecute(Bitmap bitmap) {
                 iconView.setImageBitmap(bitmap);

             }
            private Bitmap downloadImage(String code){
                final DefaultHttpClient client=new DefaultHttpClient();
              //  final HttpGet getRequest=new HttpGet(Utils.ICON_URL+code+".png");
                final HttpGet getRequest=new HttpGet("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRcjo8jw7cQ4Akjtr80OUtx4sPGe1rO_L4uyntqJwiM3t2Mr1D-");
                try {
                    HttpResponse response=client.execute(getRequest);
                    final int statusCode=response.getStatusLine().getStatusCode();
                    if(statusCode!= HttpStatus.SC_OK){
                        Log.e("DownloadImage","Error"+statusCode);
                    }
                    final HttpEntity entity=response.getEntity();
                    if (entity !=null) {
                        InputStream inputStream=null;
                        inputStream=entity.getContent();

                        final Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        return  bitmap;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    return null;
            }
        }

    private class WeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {

            String data = ((new WeatherHttpClient()).getWeatherData(params[0]));
                weather.iconData=weather.currentCondition.getIcon();
               weather = JSONWeatherParser.getWeather(data);
            Log.v("Data:",weather.currentCondition.getDescription());
            new DownloadImageAsyncTask().execute(weather.iconData);



           return weather;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Weather weather) {
            DateFormat df=DateFormat.getDateInstance();
            String sunriseData=df.format(new Date(weather.place.getSunrise()));
            String sunsetData=df.format(new Date(weather.place.getSunset()));
            String updateData=df.format(new Date(weather.place.getLastupdate()));
            DecimalFormat decimalFormat=new DecimalFormat("#.#");
            String tempFormat=decimalFormat.format(weather.currentCondition.getTemperature());
            super.onPostExecute(weather);
            cityName.setText(weather.place.getCity()+","+weather.place.getCountry());
            temp.setText(""+tempFormat+"C");
            humidity.setText("Humidity "+weather.currentCondition.getHumidity()+"%");
            pressure.setText("Pressure "+weather.currentCondition.getPressure()+"hPa");
            wind.setText("Wind "+weather.wind.getSpeed()+"mps");
            sunrise.setText("Sunrise "+sunriseData);
            sunset.setText("Sunset "+sunsetData);
            updated.setText("Last Updated "+updateData);
            description.setText("Condition "+weather.currentCondition.getCondition()+"("+  weather.currentCondition.getDescription());

        }

    }

}

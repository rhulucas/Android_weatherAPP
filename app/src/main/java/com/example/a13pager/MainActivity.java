package com.example.a13pager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Toast;

import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private MyModel[] cv_pages = {
            new MyModel("Ann Arbor", "Clear", R.color.custom_green, 0,21,15,
                    "01d","Mon","Tue","Wed","01d","13d","13d"),
            new MyModel("Yuma", "Cloud", R.color.custom_orange,0,21,15,
                    "01d","Mon","Tue","Wed","01d","13d","13d"),
            new MyModel("Fairbanks", "Rain", R.color.custom_blue,0,21,15,
                    "01d","Mon","Tue","Wed","02d","13d","13d"),
            new MyModel("Key West", "Rain", R.color.custom_purple,0,21,15,
                    "01d","Mon","Tue","Wed","02d","13d","13d"),
            new MyModel("Austin", "Rain", R.color.custom_blue,0,21,15,
                    "01d","Mon","Tue","Wed","02d","13d","13d")
    };

    private ViewPager2 viewPager2;
    private MyViewPager2Adapter adapter;

    String sunIcon, monIcon, tueIcon;

    String cityName;
    double temp, tempHigh, tempLow;

    String description, icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new MyViewPager2Adapter(cv_pages);
        viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(adapter);

        SpringDotsIndicator springDotsIndicator = findViewById(R.id.spring_dots_indicator);
        springDotsIndicator.setViewPager2(viewPager2);

        updateDayTextViews();


        fetchWeatherAndUpdate("48197", 0); // Ann Arbor
        fetchWeatherAndUpdate("85365", 1); // Yuma
        fetchWeatherAndUpdate("99703", 2); // Fairbanks
        fetchWeatherAndUpdate("33040", 3); // Key West
        fetchWeatherAndUpdate("73301", 4); // Austin
    }


    public static double[] geocodeZipCode(String zipCode, String owmApiKey) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            String base = "https://api.openweathermap.org/geo/1.0/zip";
            String q = "zip=" + URLEncoder.encode(zipCode + ",US", "UTF-8")
                    + "&appid=" + URLEncoder.encode(owmApiKey, "UTF-8");
            URL url = new URL(base + "?" + q);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            String body = sb.toString();

            Log.i("GeocodeHTTP", "code=" + code + " body=" + body);
            if (code != 200 || body.isEmpty()) return null;

            JSONObject json = new JSONObject(body);
            if (!json.has("lat") || !json.has("lon")) return null;
            return new double[]{ json.getDouble("lat"), json.getDouble("lon") };

        } catch (Exception e) {
            Log.e("OWM_GEOCODE", "Exception", e);
            return null;
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }


    private void updateDayTextViews() {
        Calendar calendar = Calendar.getInstance();
        String[] days = new String[3];
        for (int i = 0; i < 3; i++) {
            calendar.add(Calendar.DATE, 1);
            days[i] = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());
        }
        for (MyModel model : cv_pages) {
            model.setSun(days[0]);
            model.setMon(days[1]);
            model.setTue(days[2]);
        }
    }


    private void fetchWeatherAndUpdate(String zipCode, int index) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                if (index < 0 || index >= cv_pages.length) {
                    Log.w("fetchWeather", "index out of bounds: " + index);
                    return;
                }

                // 1) Geocoding
                String geocodingKey = "a17e8a9e138c4a7858e74396cc31dc99";
                double[] latLng = geocodeZipCode(zipCode, geocodingKey);
                if (latLng == null) {
                    Log.w("fetchWeather", "Geocode failed for zip=" + zipCode);
                    runOnUiThread(() ->
                            Toast.makeText(this, "no 「" + zipCode + "」", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                String lat = String.valueOf(latLng[0]);
                String lon = String.valueOf(latLng[1]);
                Log.i("GeoOK", "zip=" + zipCode + " -> lat=" + lat + ", lon=" + lon);

                // 2) One Call 3.0（units=metric；可选 exclude 降负载）
                String owmKey = "a17e8a9e138c4a7858e74396cc31dc99"; //
                URL url = new URL(
                        "https://api.openweathermap.org/data/3.0/onecall"
                                + "?lat=" + lat
                                + "&lon=" + lon
                                + "&units=metric"
                                + "&exclude=minutely,alerts"
                                + "&appid=" + owmKey
                );
                Log.i("OWM_REQ", "GET " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);

                int code = urlConnection.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? new BufferedInputStream(urlConnection.getInputStream())
                        : new BufferedInputStream(urlConnection.getErrorStream());
                String response1 = convertStreamToString(is);
                Log.i("OWM", "code=" + code + " body=" + response1);

                if (code != 200 || response1.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "One Call : HTTP " + code, Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // 3) 解析 JSON
                JSONObject jsonObject = new JSONObject(response1);

                MyModel model = cv_pages[index];

                // timezone
                cityName = jsonObject.optString("timezone", zipCode);
                //model.setCity(cityName);

                JSONObject current = jsonObject.getJSONObject("current");
                temp = current.getDouble("temp"); // 已是摄氏度（units=metric）
                model.setTemp(temp);

                JSONArray weatherArray = current.getJSONArray("weather");
                if (weatherArray.length() > 0) {
                    JSONObject weather = weatherArray.getJSONObject(0);
                    description = weather.optString("description", "");
                    icon = weather.optString("icon", "");
                    model.setCond(description);
                    model.setWeatherIcon(getWeatherIcon(icon));
                }

                JSONArray dailyArray = jsonObject.getJSONArray("daily");
                if (dailyArray.length() > 0) {
                    JSONObject today = dailyArray.getJSONObject(0).getJSONObject("temp");
                    tempHigh = today.getDouble("max");
                    tempLow = today.getDouble("min");
                    model.setTempHigh(tempHigh);
                    model.setTempLow(tempLow);
                }
                if (dailyArray.length() > 1) {
                    String s = dailyArray.getJSONObject(1)
                            .getJSONArray("weather").getJSONObject(0)
                            .optString("icon", "");
                    model.setSunIcon(getWeatherIcon(s));
                }
                if (dailyArray.length() > 2) {
                    String m = dailyArray.getJSONObject(2)
                            .getJSONArray("weather").getJSONObject(0)
                            .optString("icon", "");
                    model.setMonIcon(getWeatherIcon(m));
                }
                if (dailyArray.length() > 3) {
                    String t = dailyArray.getJSONObject(3)
                            .getJSONArray("weather").getJSONObject(0)
                            .optString("icon", "");
                    model.setTusIcon(getWeatherIcon(t));
                }

                // 4) 刷新 UI（只刷新当前项）
                runOnUiThread(() -> adapter.notifyItemChanged(index));

            } catch (Exception e) {
                Log.e("fetchWeather", "Exception", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "fail：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
        }).start();
    }

    private String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void parseWeatherData(String response, int index) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            cityName = jsonObject.optString("timezone", "");
            JSONObject current = jsonObject.getJSONObject("current");
            temp = current.getDouble("temp");
            JSONArray weatherArray = current.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weather = weatherArray.getJSONObject(0);
                description = weather.optString("description", "");
                icon = weather.optString("icon", "");
            }

            JSONArray dailyArray = jsonObject.getJSONArray("daily");
            JSONObject todayWeather = dailyArray.getJSONObject(0);
            JSONObject temperatureDetails = todayWeather.getJSONObject("temp");
            tempHigh = temperatureDetails.getDouble("max");
            tempLow = temperatureDetails.getDouble("min");

            // 明日/后日/大后日的图标
            String s = (dailyArray.length() > 1)
                    ? dailyArray.getJSONObject(1).getJSONArray("weather").getJSONObject(0).optString("icon", "")
                    : "";
            String m = (dailyArray.length() > 2)
                    ? dailyArray.getJSONObject(2).getJSONArray("weather").getJSONObject(0).optString("icon", "")
                    : "";
            String t = (dailyArray.length() > 3)
                    ? dailyArray.getJSONObject(3).getJSONArray("weather").getJSONObject(0).optString("icon", "")
                    : "";

            MyModel model = cv_pages[index];
            model.setCity(cityName);
            model.setTemp(temp);
            model.setTempHigh(tempHigh);
            model.setTempLow(tempLow);
            model.setCond(description);
            model.setWeatherIcon(getWeatherIcon(icon));
            model.setSunIcon(getWeatherIcon(s));
            model.setMonIcon(getWeatherIcon(m));
            model.setTusIcon(getWeatherIcon(t));

            runOnUiThread(() -> adapter.notifyItemChanged(index));

        } catch (JSONException e) {
            Log.e("parseWeatherData", "JSON error", e);
        }
    }

    private String getWeatherIcon(String iconCode) {
        switch (iconCode) {
            // Daytime
            case "01d": return String.valueOf(Climacons.ClimaconSun.getCharacter());
            case "02d": return String.valueOf(Climacons.ClimaconCloudSun.getCharacter());
            case "03d": return String.valueOf(Climacons.ClimaconCloud.getCharacter());
            case "04d": return String.valueOf(Climacons.ClimaconCloud.getCharacter());
            case "09d": return String.valueOf(Climacons.ClimaconRain.getCharacter());
            case "10d": return String.valueOf(Climacons.ClimaconRainSun.getCharacter());
            case "11d": return String.valueOf(Climacons.ClimaconLightningSun.getCharacter());
            case "13d": return String.valueOf(Climacons.ClimaconSnowSun.getCharacter());
            case "50d": return String.valueOf(Climacons.ClimaconFogSun.getCharacter());
            // Nighttime
            case "01n": return String.valueOf(Climacons.ClimaconMoon.getCharacter());
            case "02n": return String.valueOf(Climacons.ClimaconCloudMoon.getCharacter());
            case "03n": return String.valueOf(Climacons.ClimaconCloud.getCharacter());
            case "04n": return String.valueOf(Climacons.ClimaconCloud.getCharacter());
            case "09n": return String.valueOf(Climacons.ClimaconRain.getCharacter());
            case "10n": return String.valueOf(Climacons.ClimaconRainMoon.getCharacter());
            case "11n": return String.valueOf(Climacons.ClimaconLightningMoon.getCharacter());
            case "13n": return String.valueOf(Climacons.ClimaconSnowMoon.getCharacter());
            case "50n": return String.valueOf(Climacons.ClimaconFogMoon.getCharacter());
            default:    return "";
        }
    }

    public enum Climacons {
        ClimaconCloud                   ('!'),
        ClimaconCloudSun                ('"'),
        ClimaconCloudMoon               ('#'),
        ClimaconRain                    ('$'),
        ClimaconRainSun                 ('%'),
        ClimaconRainMoon                ('&'),
        ClimaconRainAlt                 ('\''),
        ClimaconRainSunAlt              ('('),
        ClimaconRainMoonAlt             (')'),
        ClimaconDownpour                ('*'),
        ClimaconDownpourSun             ('+'),
        ClimaconDownpourMoon            (','),
        ClimaconDrizzle                 ('-'),
        ClimaconDrizzleSun              ('.'),
        ClimaconDrizzleMoon             ('/'),
        ClimaconSleet                   ('0'),
        ClimaconSleetSun                ('1'),
        ClimaconSleetMoon               ('2'),
        ClimaconHail                    ('3'),
        ClimaconHailSun                 ('4'),
        ClimaconHailMoon                ('5'),
        ClimaconFlurries                ('6'),
        ClimaconFlurriesSun             ('7'),
        ClimaconFlurriesMoon            ('8'),
        ClimaconSnow                    ('9'),
        ClimaconSnowSun                 (':'),
        ClimaconSnowMoon                (';'),
        ClimaconFog                     ('<'),
        ClimaconFogSun                  ('='),
        ClimaconFogMoon                 ('>'),
        ClimaconHaze                    ('?'),
        ClimaconHazeSun                 ('@'),
        ClimaconHazeMoon                ('A'),
        ClimaconWind                    ('B'),
        ClimaconWindCloud               ('C'),
        ClimaconWindCloudSun            ('D'),
        ClimaconWindCloudMoon           ('E'),
        ClimaconLightning               ('F'),
        ClimaconLightningSun            ('G'),
        ClimaconLightningMoon           ('H'),
        ClimaconSun                     ('I'),
        ClimaconSunset                  ('J'),
        ClimaconSunrise                 ('K'),
        ClimaconSunLow                  ('L'),
        ClimaconSunLower                ('M'),
        ClimaconMoon                    ('N'),
        ClimaconMoonNew                 ('O'),
        ClimaconMoonWaxingCrescent      ('P'),
        ClimaconMoonWaxingQuarter       ('Q'),
        ClimaconMoonWaxingGibbous       ('R'),
        ClimaconMoonFull                ('S'),
        ClimaconMoonWaningGibbous       ('T'),
        ClimaconMoonWaningQuarter       ('U'),
        ClimaconMoonWaningCrescent      ('V'),
        ClimaconSnowflake               ('W'),
        ClimaconTornado                 ('X'),
        ClimaconThermometer             ('Y'),
        ClimaconThermometerLow          ('Z'),
        ClimaconThermometerMediumLow    ('['),
        ClimaconThermometerMediumHigh   ('\\'),
        ClimaconThermometerHigh         (']'),
        ClimaconThermometerFull         ('^'),
        ClimaconCelsius                 ('_'),
        ClimaconFahrenheit              ('`'),
        ClimaconCompass                 ('a'),
        ClimaconCompassNorth            ('b'),
        ClimaconCompassEast             ('c'),
        ClimaconCompassSouth            ('d'),
        ClimaconCompassWest             ('e'),
        ClimaconUmbrella                ('f'),
        ClimaconSunglasses              ('g'),
        ClimaconCloudRefresh            ('h'),
        ClimaconCloudUp                 ('i'),
        ClimaconCloudDown               ('j');

        private final char character;
        Climacons(char character) { this.character = character; }
        public char getCharacter() { return character; }
    }
}

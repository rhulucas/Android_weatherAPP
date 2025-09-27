An Android mobile application built with Java and Android Studio that uses the OpenWeatherMap API to retrieve weather information based on a user-provided ZIP code.
The app displays the current weather conditions, today’s high and low temperatures, and a three-day weather forecast, and it dynamically changes the background color according to the current temperature.


https://github.com/user-attachments/assets/adc66c3d-047c-4206-b9bf-6947a142555f



## Using Your Own API Key
1. Sign up at [OpenWeatherMap](https://openweathermap.org/api) to get a free API key.
2. Open `app/src/main/java/com/example/a13pager/MainActivity.java`.
3. Find this line:
   ```java
   String owmKey = "your_real_key_here";
   Replace "your_real_key_here" with your personal API key:

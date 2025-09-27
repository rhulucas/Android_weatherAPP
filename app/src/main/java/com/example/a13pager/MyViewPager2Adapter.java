package com.example.a13pager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

public class MyViewPager2Adapter extends RecyclerView.Adapter<MyViewPager2Adapter.MyViewHolder> {
    private MyModel[] cv_models;

    MyViewPager2Adapter(MyModel[] models) {
        this.cv_models = models;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyModel model = cv_models[position];
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return cv_models.length;
    }

    public static int adjustBrightness(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(hsv[2] * factor, 1.0f); // 亮度
        return Color.HSVToColor(hsv);
    }
    public static int interpolateColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        int mixedColor = ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));

        return adjustBrightness(mixedColor, 2.2f);
    }
    public static int getColorForTemperature(Context context, double temp) {
        int startColor = Color.parseColor("#04ABF6"); //  Blue
        int endColor = Color.parseColor("#F70202"); //  Red

        // Normalize temperature to range 0 to 1
        float normalizedTemp = (float) ((temp - 0) / 40.0); // Example range from 0 to 40
        normalizedTemp = Math.max(0, Math.min(1, normalizedTemp)); // Clamp between 0 and 1

        return interpolateColor(normalizedTemp, startColor, endColor);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView cv_city, cv_cond, cv_temp, cv_temp_high, cv_temp_low;
        TextView cv_sun, cv_mon, cv_tue, cv_sunIcon, cv_monIcon, cv_tueIcon,cv_Icon;
        TextView cv_tempF;  // Toggle TextView for temperature unit
        ConstraintLayout constraintLayout;
        boolean isCelsius = true;  // Default to Celsius
        MyModel model;  // Add this line to hold the model

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
            cv_tempF.setOnClickListener(v -> toggleTemperatureUnit());
        }

        private void initializeViews() {
            cv_city = itemView.findViewById(R.id.vv_tvCity);
            cv_cond = itemView.findViewById(R.id.vv_tvCond);
            cv_temp = itemView.findViewById(R.id.textTemperature);
            cv_temp_high = itemView.findViewById(R.id.text_H);
            cv_temp_low = itemView.findViewById(R.id.text_L);
            cv_sun = itemView.findViewById(R.id.text_Sun);
            cv_mon = itemView.findViewById(R.id.text_Mon);
            cv_tue = itemView.findViewById(R.id.text_Tue);
            cv_Icon = itemView.findViewById(R.id.vv_cond);
            cv_sunIcon = itemView.findViewById(R.id.text_fontSun);
            cv_monIcon = itemView.findViewById(R.id.text_fontMon);
            cv_tueIcon = itemView.findViewById(R.id.text_fontTue);
            cv_tempF = itemView.findViewById(R.id.text_F);
            constraintLayout = itemView.findViewById(R.id.constraintlayout);
        }

        private void toggleTemperatureUnit() {
            isCelsius = !isCelsius;
            updateTemperatureDisplay(Double.parseDouble(cv_temp.getText().toString().replace("°", "")));
        }

        public void updateTemperatureDisplay(double originalTemperature) {
            double highTemp = model.mf_getTempHigh();
            double lowTemp = model.mf_getTempLow();
            double textTemperature = model.mf_getTemp();
            String temperatureText, highText, lowText;

            if (isCelsius) {
                // 如果是摄氏度，直接显示
                temperatureText = String.format(Locale.getDefault(), "%.1f°", textTemperature);
                highText = String.format(Locale.getDefault(), "H%.1f°", highTemp);
                lowText = String.format(Locale.getDefault(), "L%.1f°", lowTemp);
                cv_tempF.setText("C");
            } else {
                // 如果是华氏度，转换后显示
                double fahrenheitTemp = celsiusToFahrenheit(textTemperature);
                double fahrenheitHigh = celsiusToFahrenheit(highTemp);
                double fahrenheitLow = celsiusToFahrenheit(lowTemp);

                temperatureText = String.format(Locale.getDefault(), "%.1f°", fahrenheitTemp);
                highText = String.format(Locale.getDefault(), "H%.1f°", fahrenheitHigh);
                lowText = String.format(Locale.getDefault(), "L%.1f°", fahrenheitLow);
                cv_tempF.setText("F");
            }
            cv_temp.setText(temperatureText);
            cv_temp_high.setText(highText);
            cv_temp_low.setText(lowText);
        }

        private double celsiusToFahrenheit(double celsius) {
            return (celsius * 9 / 5) + 32;
        }


        public void bind(MyModel model) {
            cv_city.setText(model.mf_getCity());
            cv_cond.setText(model.mf_getCond());
            this.model = model; // 保存模型以便后续使用
            double temperature = model.mf_getTemp();
            updateTemperatureDisplay(temperature); // Make sure to display correct unit on bind
            cv_temp_high.setText(String.format(Locale.getDefault(), "H%.1f°", model.mf_getTempHigh()));
            cv_temp_low.setText(String.format(Locale.getDefault(), "L%.1f°", model.mf_getTempLow()));
            cv_sun.setText(model.mf_getSun());
            cv_mon.setText(model.mf_getMon());
            cv_tue.setText(model.mf_getTue());
            cv_Icon.setText(model.mf_getWeatherIcon());
            cv_sunIcon.setText(model.mf_getSunIcon());
            cv_monIcon.setText(model.mf_getMonIcon());
            cv_tueIcon.setText(model.mf_getTueIcon());

            int color = getColorForTemperature(itemView.getContext(), model.mf_getTemp());
            constraintLayout.setBackgroundColor(color);
        }
    }

}

package com.example.thesender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SmsActivity extends AppCompatActivity {

        Button timeButton, datePickerButton;
        Calendar selectedDate, dateTime;
        int year, month, day, hour, minute;
        EditText phoneNumbers, smsMessage;
        Button sendSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        phoneNumbers = findViewById(R.id.phone_numbers);
        smsMessage = findViewById(R.id.sms_message);
        timeButton = findViewById(R.id.timeButton);
        datePickerButton = findViewById(R.id.datePickerButton);
        sendSms = findViewById(R.id.send_sms);
        dateTime = Calendar.getInstance();

        Toast.makeText(SmsActivity.this, "In order to separate the cell phone numbers you must to use the char ','", Toast.LENGTH_SHORT).show();


        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(SmsActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                    sendSMS();
                }
                else{
                    ActivityCompat.requestPermissions(SmsActivity.this, new String[]{Manifest.permission.SEND_SMS},100);
                }
            }
        });


        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            sendSMS();
        }
        else{
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS() {
        String phone = phoneNumbers.getText().toString();
        String message = smsMessage.getText().toString();

        if(!phone.isEmpty() && !message.isEmpty()){
            String[] phoneNumbers = phone.split(",");
            if (validatePhoneNumbers(phoneNumbers)) {
                Toast.makeText(this, "SMS will be set on the selected date and time!", Toast.LENGTH_SHORT).show();
                waitToTime(phoneNumbers, message);
            } else {
                Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Please enter phone and message", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePhoneNumbers(String[] phoneNumbers){
        for (String number : phoneNumbers) {
            if (!isValidPhoneNumber(number)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPhoneNumber(String number){
        return number.matches("^\\+\\d+$");
    }

    private void waitToTime(String[] phoneNumbers, String message) {
        Calendar scheduledTime = (Calendar) selectedDate.clone();
        scheduledTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduledTime.set(Calendar.MINUTE, minute);
        scheduledTime.set(Calendar.SECOND, 0);
        scheduledTime.set(Calendar.MILLISECOND, 0);

        long scheduledTimeMillis = scheduledTime.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();
        long delayMillis = scheduledTimeMillis - currentTimeMillis;

        if (delayMillis <= 0) {
            // The selected time is in the past or very close, handle accordingly
            sendDelayedSms(phoneNumbers, message);
            return;
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendDelayedSms(phoneNumbers, message);
            }
        }, delayMillis);

        Toast.makeText(this, "SMS will be sent on the selected date and time!", Toast.LENGTH_SHORT).show();
    }




    private void sendDelayedSms(String[] phoneNumbers, String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SmsManager smsManager = SmsManager.getDefault();
                for(String phone : phoneNumbers){
                    smsManager.sendTextMessage(phone, null, message, null, null);
                }
            }
        });
        thread.start();

        Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
    }

    public void showDatePickerDialog(View view) {
        Calendar currentDate = Calendar.getInstance();
        year = currentDate.get(Calendar.YEAR);
        month = currentDate.get(Calendar.MONTH);
        day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int dayOfMonth) {
                selectedDate = Calendar.getInstance();
                selectedDate.set(selectedYear, selectedMonth, dayOfMonth);


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDateString = dateFormat.format(selectedDate.getTime());

                datePickerButton.setText(selectedDateString);
            }
        },year, month, day);

        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow

        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    public void popTimePicker(View view) {
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                selectedTime.set(Calendar.MINUTE, selectedMinute);

                if (selectedTime.after(currentTime)) {
                    hour = selectedHour;
                    minute = selectedMinute;
                    timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                } else {
                    // Display an error message or take appropriate action
                    Toast.makeText(SmsActivity.this, "Please select a future time", Toast.LENGTH_SHORT).show();
                }
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, currentHour, currentMinute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }


}
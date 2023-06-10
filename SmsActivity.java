package com.example.thesender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
            Toast.makeText(this, "SMS will be set on the selected date and time!", Toast.LENGTH_SHORT).show();
            String[] phoneNumbers = phone.split(",");
            waitToTime(phoneNumbers, message);
        }
        else{
            Toast.makeText(this, "Please enter phone and message", Toast.LENGTH_SHORT).show();
        }
    }

    private void waitToTime(String[] phoneNumbers, String message){
        long delayMillis = selectedDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() +
                (hour - Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) * 3600000L +
                (minute - Calendar.getInstance().get(Calendar.MINUTE)) * 60000L;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendDelayedSms(phoneNumbers, message);
            }
        }, delayMillis);
    }

    private void sendDelayedSms(String[] phoneNumbers, String message){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SmsManager smsManager = SmsManager.getDefault();
                for (String phone : phoneNumbers){
                    smsManager.sendTextMessage(phone, null, message, null, null);

                }
            }
        });
        thread.start();

        Toast.makeText(this,"SMS sent successfully!", Toast.LENGTH_SHORT).show();
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

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Restrict to current date or future dates
        datePickerDialog.show();
        datePickerDialog.show();
    }

    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }


}
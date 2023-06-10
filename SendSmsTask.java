package com.example.thesender;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SendSmsTask extends AsyncTask<Void, Void, Void> {
    private final String[] phoneNumbers;
    private final String message;
    private final Context context;

    public SendSmsTask(String[] phoneNumbers, String message, Context context){
        this.phoneNumbers = phoneNumbers;
        this.message = message;
        this.context = context;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        SmsManager smsManager = SmsManager.getDefault();
        for(String phone : phoneNumbers){
            smsManager.sendTextMessage(phone, null, message, null, null);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
    }
}

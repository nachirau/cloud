package com.google.android.gms.plus.sample.quickstart;
import android.os.AsyncTask;
import android.util.Log;


import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

class SendMailTLS extends AsyncTask<Void, Void, Boolean> {
    Mail m = new Mail();

    public SendMailTLS(String user_name) {
        if (BuildConfig.DEBUG) Log.v(SendMailTLS.class.getName(), "SendEmailAsyncTask()");
        String[] toArr = new String[] {user_name};
        m.setTo(toArr);
        m.setFrom("carparkny@gmail.com");
        m.setSubject("Thank you for your booking");
        m.setBody("Android Test Mail");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (BuildConfig.DEBUG) Log.v(SendMailTLS.class.getName(), "doInBackground()");
        try {
            m.send();
            return true;
        } catch (AuthenticationFailedException e) {
            Log.e(SendMailTLS.class.getName(), "Bad account details");
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            Log.e(SendMailTLS.class.getName(), "failed");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

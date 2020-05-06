package com.example.whereiam40;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private PendingIntent resultPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch aSwitch = findViewById(R.id.switch1);
        final EditText editText = findViewById(R.id.ra);

//        Constraints constraints = new Constraints.Builder()
//                .setRequiresCharging(false)
//                .build();

        LocationManager service = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        boolean gpsEnabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        /* VERIFICA SE O GPS ESTÁ LIGADO */
        if (!gpsEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editText.setEnabled(false);

                    Data.Builder data = new Data.Builder();
                    data.putString("ra", editText.getText().toString());

                    PeriodicWorkRequest testWork = new PeriodicWorkRequest.Builder(TestWorker.class, 15, TimeUnit.MINUTES)
                            .addTag("APIPosting")
                            .setInputData(data.build())
                           // .setConstraints(constraints)
                            .build();

                    WorkManager.getInstance(getApplicationContext())
                            .enqueue(testWork);

                    switchOnMsg();
                    Log.d("DEBUG", "Service Status: "+ (WorkManager.getInstance().getWorkInfoById(testWork.getId())));

                }else{
                    switchOffMsg();
                    editText.setEnabled(true);
                    //Log.d("DEBUG","Testwork: "+testWork);
                    WorkManager.getInstance().cancelAllWorkByTag("APIPosting");
                    //WorkManager.getInstance().cancelAllWork();

                }
            }
        });

//        To not auto open keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        To not turn in horizontal mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void switchOffMsg(){
        Toast.makeText(this, "Localização desativada!", Toast.LENGTH_SHORT).show();
    }

    private void switchOnMsg(){
        Toast.makeText(this, "Localização Ativada!", Toast.LENGTH_SHORT).show();
    }

    protected void notifyMe() {
        int idNotificacao = 12345;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_menu_info_details);
        mBuilder.setContentTitle("Notificação PDM");
        mBuilder.setContentText("Hora de Estudar!");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(idNotificacao, mBuilder.build());
    }

    private void cancelNotify() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12345);
    }
}

package com.matrix_maeny.internetspeedchecker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.matrix_maeny.internetspeedchecker.databinding.ActivityMainBinding;

import org.jetbrains.annotations.Contract;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ConnectivityManager manager;
    NetworkCapabilities networkCapabilities;

    double downSpeed = 0;
    double upSpeed = 0;
    long timeForPing = 0L;
    InetAddress address = null;

    final Handler handler = new Handler();

    private Thread networkThread;

    int width = 0, height = 0;
    boolean spin = false;
    final Random random = new Random();
    float pX, pY;
    int stoppedDegrees = 1000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        networkCapabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());


        new Thread() {
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startRotations();
                        handler.postDelayed(this, 100);
                    }
                }, 100);
            }

        }.start();

        handler.post(this::checkSpeed);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSpeed();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
//        networkThread.start();

    }

    private void startRotations() {
        if (!spin) try {

            int num = random.nextInt((int) timeForPing);

            pX = binding.gearIv.getMeasuredWidth() / 2.0f;
            pY = binding.gearIv.getMeasuredHeight() / 2.0f;

            Animation animation = new RotateAnimation(stoppedDegrees, num, pX, pY);
            animation.setDuration(2000);

            animation.setFillAfter(true);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    spin = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    spin = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            stoppedDegrees = num;

            binding.gearIv.startAnimation(animation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkSpeed() {


        downSpeed = networkCapabilities.getLinkDownstreamBandwidthKbps();
        upSpeed = networkCapabilities.getLinkUpstreamBandwidthKbps();

        String unit1 = getUnits(downSpeed);
        String unit2 = getUnits(upSpeed);
        setSpeedValue();

        handler.post(() -> {
            binding.downSpeedTv.setText(downSpeed + " " + unit1);
            binding.upSpeedTv.setText(upSpeed + " " + unit2);
        });


        new Thread() {
            @SuppressLint("SetTextI18n")
            public void run() {
                try {
                    address = InetAddress.getByName("www.google.com");
                    String ip = address.getHostAddress();
                    timeForPing = getPing(ip);
                    Log.i("networkSpeed2", "Ping: " + timeForPing);
                    handler.post(() -> binding.pingTv.setText(timeForPing + " ms"));
                } catch (Exception ignored) {
                }
            }
        }.start();


    }

    private long getPing(String domain) {
        Runtime runtime = Runtime.getRuntime();

        try {
            long a = System.currentTimeMillis();// % 100000;
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + domain);
            ipProcess.waitFor();
            long b = System.currentTimeMillis();// % 100000;

            return b - a;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;

    }

    @NonNull
    @Contract(pure = true)
    private String getUnits(double speed) {
        String units = "";

        // the speed is in bits
        speed /= 1024;

        if (speed >= 1) {
            units = "Kbps";
        } else {
            units = "bps";
        }

        speed /= 1024;

        if (speed >= 1024) {
            units = "Gbps";
        }

        if (speed >= 1) {
            units = "Mbps";
        }

        return units;
    }

    private void setSpeedValue() {

        downSpeed = downSpeed * 0.001;
        upSpeed = upSpeed * 0.001;

        if (downSpeed >= 1024) {
            downSpeed /= 1024;

            if (downSpeed >= 1024) {
                downSpeed /= 1024;
                if (downSpeed >= 1024) {
                    downSpeed /= 1024;
                }
            }
        }

        if (upSpeed >= 1024) {
            upSpeed /= 1024;

            if (upSpeed >= 1024) {
                upSpeed /= 1024;
                if (upSpeed >= 1024) {
                    upSpeed /= 1024;
                }
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // go to about activity

        startActivity(new Intent(MainActivity.this,AboutActivity.class));

        return super.onOptionsItemSelected(item);
    }
}
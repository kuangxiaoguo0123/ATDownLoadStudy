package com.asiatravel.atdownloadstudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.asiatravel.atdownloadstudy.entities.FileInfo;
import com.asiatravel.atdownloadstudy.service.DownloadService;

public class MainActivity extends AppCompatActivity {

    public static final String DOWNLOAD_URL = "http://shouji.360tpcdn.com/161103/51b91e36b6556dd4c043cfbe501b3d0c/com.achievo.vipshop_559.apk";
    public static final String GOOGLE_PINYIN_URL = "http://dlsw.baidu.com/sw-search-sp/soft/e0/13545/GooglePinyinInstaller.1419846448.exe";

    private TextView mFileNameTextView;
    private ProgressBar mProgressBar;
    private Button mStartButton;
    private Button mStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findItemId();
        initFile();
        mProgressBar.setMax(100);
    }

    private void initFile() {
        final FileInfo fileInfo = new FileInfo(0, DOWNLOAD_URL, "test.apk", 0, 0);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void findItemId() {
        mFileNameTextView = (TextView) findViewById(R.id.file_name_textView);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mStartButton = (Button) findViewById(R.id.start_button);
        mStopButton = (Button) findViewById(R.id.stop_button);
    }

    /**
     * 更新ui的广播接受器
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                mProgressBar.setProgress(finished);
            }
        }
    };
}

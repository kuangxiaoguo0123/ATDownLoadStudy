package com.asiatravel.atdownloadstudy.service;

import android.content.Context;
import android.content.Intent;

import com.asiatravel.atdownloadstudy.db.ThreadDAO;
import com.asiatravel.atdownloadstudy.db.ThreadDAOImpl;
import com.asiatravel.atdownloadstudy.entities.FileInfo;
import com.asiatravel.atdownloadstudy.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 * Created by asiatravel on 2016/11/14.
 */

public class DownloadTask {

    private Context context;
    private FileInfo fileInfo;
    private ThreadDAO mDao;
    private int mFinished = 0;
    public boolean isPause = false;

    public DownloadTask(Context context, FileInfo fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
        mDao = new ThreadDAOImpl(context);
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threadList = mDao.getThread(fileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadList.size() == 0) {
            threadInfo = new ThreadInfo(0, fileInfo.getUrl(), 0, fileInfo.getLength(), 0);
        } else {
            threadInfo = threadList.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {

        private ThreadInfo threadInfo;
        private RandomAccessFile raf;
        private InputStream input;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            //向数据库插入线程信息
            if (mDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
                mDao.insertThread(threadInfo);
            }
            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");

                //设置下载位置
                int start = threadInfo.getStart() + threadInfo.getFinished();

                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += threadInfo.getFinished();

                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] bytes = new byte[4 * 1024];
                    int len = -1;
                    long time = System.currentTimeMillis();

                    while ((len = input.read(bytes)) != -1) {
                        //写入文件
                        raf.write(bytes, 0, len);
                        //把下载进度发送广播给activity

                        if (System.currentTimeMillis() - 500 > time) {
                            mFinished += len;
                            intent.putExtra("finished", mFinished * 100 / fileInfo.getLength());
                            context.sendBroadcast(intent);
                        }
                    }
                    //下载暂停时，保存下载进度
                    if (isPause) {
                        mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), mFinished);
                        return;
                    }
                }
                //删除线程信息
                mDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());

            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                try {
                    if (raf != null) {

                        raf.close();
                    }
                    if (input != null) {

                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

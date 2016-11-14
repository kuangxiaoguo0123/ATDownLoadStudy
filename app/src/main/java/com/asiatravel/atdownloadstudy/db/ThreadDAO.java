package com.asiatravel.atdownloadstudy.db;

import com.asiatravel.atdownloadstudy.entities.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 * <p>
 * Created by asiatravel on 2016/11/14.
 */

public interface ThreadDAO {

    /**
     * 插入线程信息
     */
    void insertThread(ThreadInfo threadInfo);

    void deleteThread(String url, int threadId);

    void updateThread(String url, int threadId, int finished);

    List<ThreadInfo> getThread(String url);

    boolean isExists(String url, int threadId);
}

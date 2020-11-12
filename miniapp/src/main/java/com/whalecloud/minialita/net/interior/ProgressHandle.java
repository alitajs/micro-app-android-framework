package com.whalecloud.minialita.net.interior;

/**
 * 进度控制，用于反馈和控制进度
 */
public interface ProgressHandle {
    /**
     * 当前进度
     * @param total 总量
     * @param done 当前进度值
     */
    void onProgress(long total, long done);

    /**
     * 是否取消
     * @return true 操作被取消
     */
    boolean revoked();
}

package com.zsy;

import java.util.concurrent.TimeUnit;

/**
 * 优化的雪花算法ID生成器（解决时钟回拨问题）
 */
public class SnowflakeIdGenerator {
    // 起始时间戳（2020-01-01 00:00:00）
    private final long epoch = 1577836800000L;

    // 机器ID位数
    private final long workerIdBits = 5L;
    // 数据中心ID位数
    private final long datacenterIdBits = 5L;
    // 序列号位数
    private final long sequenceBits = 12L;

    // 最大机器ID (2^5-1 = 31)
    private final long maxWorkerId = ~(-1L << workerIdBits);
    // 最大数据中心ID (2^5-1 = 31)
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);

    // 各部分的位移
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 序列号掩码 (0b111111111111=0xfff=4095)
    private final long sequenceMask = ~(-1L << sequenceBits);

    // 工作机器ID(0-31)
    private final long workerId;
    // 数据中心ID(0-31)
    private final long datacenterId;
    // 毫秒内序列(0-4095)
    private long sequence = 0L;
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 最大容忍时钟回拨毫秒数
    private static final long MAX_BACKWARD_MS = 1000;

    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获得下一个ID (线程安全)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 处理时钟回拨
        if (timestamp < lastTimestamp) {
            handleClockBackwards(timestamp);
            timestamp = timeGen(); // 重新获取当前时间
        }

        // 如果是同一毫秒内生成的
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 序列号溢出
            if (sequence == 0) {
                // 阻塞到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID
        return ((timestamp - epoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 处理时钟回拨
     * @param currentTimestamp 当前时间戳
     */
    private void handleClockBackwards(long currentTimestamp) {
        long offset = lastTimestamp - currentTimestamp;

        // 如果在容忍范围内，等待时钟追上
        if (offset <= MAX_BACKWARD_MS) {
            try {
                // 等待两倍的偏移时间
                wait(offset << 1);

                // 检查等待后是否仍然存在时钟回拨
                if (timeGen() < lastTimestamp) {
                    throw new RuntimeException("Clock moved backwards after waiting");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while handling clock backwards", e);
            }
        } else {
            // 超过最大容忍范围，抛出异常
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", offset));
        }
    }

    /**
     * 阻塞到下一毫秒
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回当前时间(毫秒)
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 解析Snowflake ID
     * @param id 要解析的ID
     * @return 包含各部分的数组 [timestamp, datacenterId, workerId, sequence]
     */
    public static long[] parseId(long id) {
        long[] parts = new long[4];
        parts[0] = (id >> 22) + 1577836800000L; // 时间戳
        parts[1] = (id >> 17) & 0x1F;          // 数据中心ID
        parts[2] = (id >> 12) & 0x1F;          // 机器ID
        parts[3] = id & 0xFFF;                 // 序列号
        return parts;
    }

    /**
     * 获取ID的生成时间
     * @param id Snowflake ID
     * @return 生成时间的毫秒时间戳
     */
    public static long getGenerateTime(long id) {
        return (id >> 22) + 1577836800000L;
    }
}
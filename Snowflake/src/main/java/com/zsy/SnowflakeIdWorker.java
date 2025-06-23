package com.zsy;

public class SnowflakeIdWorker {
    // 起始时间戳（2020-01-01）
    private final long epoch = 1577836800000L;

    // 机器ID位数
    private final long workerIdBits = 5L;
    // 数据中心ID位数
    private final long datacenterIdBits = 5L;
    // 序列号位数
    private final long sequenceBits = 12L;

    // 最大机器ID (2^5-1)
    private final long maxWorkerId = ~(-1L << workerIdBits);
    // 最大数据中心ID (2^5-1)
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);

    // 机器ID左移位数
    private final long workerIdShift = sequenceBits;
    // 数据中心ID左移位数
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间戳左移位数
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 序列号掩码 (0b111111111111=0xfff=4095)
    private final long sequenceMask = ~(-1L << sequenceBits);

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param workerId 机器ID (0-31)
     * @param datacenterId 数据中心ID (0-31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("机器ID (0-31)", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("数据中心ID (0-31)", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个ID
     * @return Snowflake ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
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
     * 获取当前毫秒数
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
}

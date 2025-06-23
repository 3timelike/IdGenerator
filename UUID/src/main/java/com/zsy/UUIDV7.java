package com.zsy;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * UUIDv7 工具类
 * 基于RFC 9562标准实现
 */
public class UUIDV7 {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成UUIDv7
     * @return UUIDv7实例
     */
    public static UUID generate() {
        // 获取当前时间戳（48位，毫秒精度）
        long timestamp = Instant.now().toEpochMilli();

        // 生成随机数（80位）
        byte[] randomBytes = new byte[10];
        SECURE_RANDOM.nextBytes(randomBytes);

        // 构建UUIDv7
        long msb = ((timestamp & 0xFFFFFFFFFFFFL) << 16) |
                ((randomBytes[0] & 0xFFL) << 8) |
                (randomBytes[1] & 0xFFL);

        long lsb = ((randomBytes[2] & 0xFFL) << 56) |
                ((randomBytes[3] & 0xFFL) << 48) |
                ((randomBytes[4] & 0xFFL) << 40) |
                ((randomBytes[5] & 0xFFL) << 32) |
                ((randomBytes[6] & 0xFFL) << 24) |
                ((randomBytes[7] & 0xFFL) << 16) |
                ((randomBytes[8] & 0xFFL) << 8) |
                (randomBytes[9] & 0xFFL);

        // 设置版本号(7)和变体号(2)
        msb = (msb & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000007000L;
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(msb, lsb);
    }

    /**
     * 从UUIDv7提取时间戳
     * @param uuid UUIDv7实例
     * @return 时间戳(毫秒)
     */
    public static long extractTimestamp(UUID uuid) {
        return (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;
    }

    /**
     * 检查是否为有效的UUIDv7
     * @param uuid 要检查的UUID
     * @return true如果是有效的UUIDv7
     */
    public static boolean isUUIDv7(UUID uuid) {
        return (uuid.version() == 7) && (uuid.variant() == 2);
    }

    /**
     * 生成可排序的UUIDv7字符串
     * @return 按时间排序的UUIDv7字符串
     */
    public static String generateSortable() {
        return generate().toString();
    }
}
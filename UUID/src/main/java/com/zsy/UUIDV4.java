package com.zsy;

/**
 * UUID 工具类
 */
public class UUIDV4 {

    /**
     * 生成带横线的标准 UUID 字符串
     * 格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     * @return 标准 UUID 字符串
     */
    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * 生成不带横线的 UUID 字符串
     * 格式：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     * @return 不带横线的 UUID 字符串
     */
    public static String generateUUIDWithoutDash() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 生成指定数量的 UUID 字符串数组
     * @param count 生成数量
     * @return UUID 字符串数组
     */
    public static String[] generateUUIDs(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("生成的数量必须>0");
        }

        String[] uuids = new String[count];
        for (int i = 0; i < count; i++) {
            uuids[i] = generateUUID();
        }
        return uuids;
    }

    /**
     * 从字符串生成 UUID 对象
     * @param uuidStr UUID 字符串
     * @return UUID 对象
     * @throws IllegalArgumentException 如果字符串不是有效的 UUID
     */
    public static java.util.UUID fromString(String uuidStr) {
        return java.util.UUID.fromString(uuidStr);
    }

    /**
     * 检查字符串是否是有效的 UUID
     * @param uuidStr 要检查的字符串
     * @return true 如果是有效的 UUID，否则 false
     */
    public static boolean isValidUUID(String uuidStr) {
        if (uuidStr == null) {
            return false;
        }

        try {
            java.util.UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 生成短 UUID (8位)
     * @return 8位的短 UUID
     */
    public static String generateShortUUID() {
        return Long.toHexString(java.util.UUID.randomUUID().getMostSignificantBits()).substring(0, 8);
    }

    /**
     * 生成指定长度的短 UUID
     * @param length 长度 (1-32)
     * @return 指定长度的短 UUID
     */
    public static String generateShortUUID(int length) {
        if (length <= 0 || length > 32) {
            throw new IllegalArgumentException("长度 在 (1-32)");
        }

        String fullUUID = generateUUIDWithoutDash();
        return fullUUID.substring(0, length);
    }
}
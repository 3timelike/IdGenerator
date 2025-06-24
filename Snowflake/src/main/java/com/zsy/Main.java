package com.zsy;

import java.util.Date;

public class Main {
    public static void main(String[] args) {

        f1();//有时钟回拨问题
        f2();//没有指针回拨问题
            //时钟回拨处理：
        //
        //当检测到时钟回拨时，根据回拨时间长短采取不同策略
        //
        //小范围回拨（<1秒）：线程等待两倍回拨时间后重试
        //
        //大范围回拨（>1秒）：直接抛出异常，防止数据混乱
    }
    public static void f1(){
        // 初始化参数为机器ID和数据中心ID
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(1, 1);

        for (int i = 0; i < 10; i++) {
            long id = idWorker.nextId();
            System.out.println("生成的ID: " + id + " 长度: " + String.valueOf(id).length());
        }

        // 解析ID
        long sampleId = idWorker.nextId();
        System.out.println("\n解析ID: " + sampleId);
        System.out.println("二进制表示: " + Long.toBinaryString(sampleId));
        System.out.println("生成时间: " + new Date(((sampleId >> 22) + 1577836800000L)));
        System.out.println("数据中心ID: " + ((sampleId >> 17) & 0x1F));
        System.out.println("机器ID: " + ((sampleId >> 12) & 0x1F));
        System.out.println("序列号: " + (sampleId & 0xFFF));
    }
    public static void f2(){
        // 1. 创建ID生成器 (参数为机器ID和数据中心ID)
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

        // 2. 生成10个ID
        System.out.println("生成10个ID:");
        for (int i = 0; i < 10; i++) {
            long id = idGenerator.nextId();
            System.out.println(id);
        }

        // 3. 解析ID
        long sampleId = idGenerator.nextId();
        System.out.println("\n解析ID: " + sampleId);

        long[] parts = SnowflakeIdGenerator.parseId(sampleId);
        System.out.println("生成时间: " + new Date(parts[0]));
        System.out.println("数据中心ID: " + parts[1]);
        System.out.println("机器ID: " + parts[2]);
        System.out.println("序列号: " + parts[3]);

        // 4. 模拟时钟回拨测试
        try {
            System.out.println("\n模拟时钟回拨测试:");
            long currentId = idGenerator.nextId();
            System.out.println("正常ID: " + currentId);

            // 模拟时钟回拨500ms
            System.out.println("模拟500ms时钟回拨...");
            Thread.sleep(500);
            long newId = idGenerator.nextId();
            System.out.println("回拨后ID: " + newId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.canon.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: baseproject
 * @Auther: canon
 * @Date: 2019/10/31 13:44
 * @Description:流水号生成器
 */
public class SerialNumberGenerator {

    private static Logger logger = LoggerFactory.getLogger(SerialNumberGenerator.class);

    /**
     * 默认前缀
     */
    private static final String PERFIX_DEFAULT = "DEFAULT_";

    /**
     * 获取流水号序号
     */
    private static ConcurrentHashMap<String, AtomicInteger> atomicIntegerMap;

    /**
     * 主题序号生成对象
     */
    private static AtomicInteger atomicIntegerSub = new AtomicInteger(0);

    /**
     * 最大编码
     */
    private static final int MAX_NO = 999;

    /**
     * 最大长度
     */
    private static final int NO_LEN = 3;

    /**
     * 集群编号
     */
    private static int clusterNo;

    /**
     * 集群数
     */
    private static int clusters;


    static {
        // dubbo工具类获取本地ip地址
        String ip = NetUtils.getLocalHost();
        clusters = Integer.parseInt(System.getProperty("cluster", "2"));
        try {
            int code = Integer.parseInt(ip.substring(ip.lastIndexOf(".") + 1, ip.length()));
            clusterNo = code % clusters;
            atomicIntegerMap = new ConcurrentHashMap<String, AtomicInteger>();
        } catch (Exception ex) {
            logger.warn("初始化code失败");
        }
    }

    /**
     * 获取流水号
     *
     *   prefix
     *            前缀
     *   deptName
     *            部门名称
     *   例：PAB-1101-20160706-001
     */
    public  static synchronized int generateSub() {
        int value = atomicIntegerSub.incrementAndGet();
        if (value == 9999) {
            atomicIntegerSub.set(0);
        }
        return value ;
    }
    /**
     * 获取流水号
     *
     *   prefix
     *            前缀
     *   deptName
     *            部门名称
     *   例：PAB-1101-20160706-001
     */
    public static String generate(String prefix, String deptName) {
        if (!StringUtils.isEmpty(deptName) && deptName.length() >= 4) {
            prefix = prefix + "-" + deptName.substring(0, 4);
        }
        String key = prefix;
        if (org.apache.commons.lang3.StringUtils.isEmpty(key)) {
            key = "default";
        }
        AtomicInteger atomicInteger = atomicIntegerMap.get(key);
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger(0);
            AtomicInteger tmp = atomicIntegerMap.put(key, atomicInteger);
            if (tmp != null) {
                atomicInteger = tmp;
            }
        }
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
        prefix = prefix + "-" + sdf.format(c.getTime());
        int serno = atomicInteger.incrementAndGet();
        if (serno == MAX_NO) {
            atomicInteger.set(0);
            serno = 0;
        }
        int value = serno * clusters + clusterNo;
        String valueString = String.valueOf(value);
        int len = NO_LEN - valueString.length();
        StringBuilder zeroString = new StringBuilder();
        for (int i = 0; i < len; i++) {
            zeroString.append("0");
        }
        return prefix + "-" + zeroString.toString() + value;
    }




    public static void main(String[] args) throws InterruptedException {
        logger.info("输出结果是:" + generateSub());
        logger.info("输出结果是:" + generate("wocao", "1100"));


        final CountDownLatch countDownLatch = new CountDownLatch(5);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    logger.info(SerialNumberGenerator.generate("oh-my-god", "1101 深圳地区"));
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
    }
}

package cn.chenny.redis.lock;

import java.util.List;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

/**
 * 基于Redis的分布式锁的实现，适用于普通操作
 */
public class JedisDistributedNormalLock {
    private static ThreadLocal<Jedis> threadLocal = new ThreadLocal<Jedis>();

    /**
     * 获取分布式锁的核心通用方法
     *
     * @param key         指定setnx作用的对象，来实现锁
     * @param isTry       是否尝试获取锁。false 尝试，获取不到立即返回;true获取不到会进入死循环不断获取
     * @param acquireTime 获取锁等待时间的时间 单位/ms
     * @param expireTime  key过期时间 单位/s
     * @return
     */
    public static Jedis lock(String key, boolean isTry, long acquireTime, int expireTime) {
        //获取redis连接
        Jedis jedis = JedisPoolUtil.get();
        threadLocal.set(jedis);

        String value = UUID.randomUUID().toString();
        long nonaTime = System.currentTimeMillis();

        if (acquireTime < 0) {
            throw new RuntimeException("acquireTime获取锁的尝试时间 不能为负数");
        }

        while (true) {
            // 成功获取了分布式锁
            if (jedis.setnx(key, value) == 1) {
                // 设置过期时间
                jedis.expire(key, expireTime);

                return jedis;
            }
            // 获取锁的时间是否超时，超时则停止获取，直接返回
            if (acquireTime != 0) {
                if (System.currentTimeMillis() - nonaTime > acquireTime * 1000) {
                    return null;
                }
            }
            //是否能尝试获取锁，能就继续进行获取锁操作。
            if (isTry) {
                // 休眠一秒，节省cpu资源
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    jedis.close();
                    threadLocal.remove();
                    return null;
                }
            }
        }
    }


    public static void realeaseLock(String key) {
        Jedis jedis = threadLocal.get();
        if (jedis != null) {
            jedis.del(key);
            jedis.close();
            threadLocal.remove();
        }
    }
}

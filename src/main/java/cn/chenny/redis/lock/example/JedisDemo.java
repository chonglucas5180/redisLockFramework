package cn.chenny.redis.lock.example;

import cn.chenny.redis.lock.JedisPoolUtil;
import cn.chenny.redis.lock.JedisDistributedTransactionLock;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class JedisDemo {
    @Test
    public void test() {
        Jedis jedis = null;
        String key = null;
        try {
            // 设置分布式锁的key
            key = "lock.item.1";
            // 开始事务
            Transaction tran = JedisDistributedTransactionLock.start(key, true, 10, 0);
            // 获取成功
            if (tran != null) {

                System.out.println("开始操作");
                tran.set("message", "helloworld");

                // 提交事务
                System.out.println("提交");

                List<Object> result = JedisDistributedTransactionLock.commit(key);
                System.out.println(result);
            }else{
                System.out.println("获取锁失败");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("回滚");
            // 回滚事务
            JedisDistributedTransactionLock.rollback(key);
        } finally {
            JedisPoolUtil.release(jedis);
        }

    }

    @Test
    public void test2() {
        // 获取连接
        Jedis jedis = JedisPoolUtil.get();

        Transaction multi = jedis.multi();
        multi.set("msg", "helloworld");
        multi.get("msg");
        List<Object> response = multi.exec();
        System.out.println(response);
        JedisPoolUtil.release(jedis);
    }
}

package cn.chenny.redis.lock;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class JedisDemo {
	@Test
	public void test() {
		Jedis jedis = null;
		String key = null;
		try {
			// 获取连接
			jedis = JedisPooUtill.get();
			// 设置分布式锁的key
			key = "lock.item.1";
			// 开始事务
			Transaction tran = JedisTransactionLock.startTransaction(jedis, key, 1000, 20);
			//Transaction tran = jedis.multi();
			// 获取成功
			if (tran != null) {

				System.out.println("开始操作");
				tran.set("message", "helloworld");
				
				// 提交事务
				System.out.println("提交");
				
				List<Object> result = JedisTransactionLock.commitTransaction(key);
				System.out.println(result);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("回滚");
			// 回滚事务
			JedisTransactionLock.rollbackTransaction(jedis, key);
		} finally {
			if (jedis != null) {
				JedisPooUtill.release(jedis);
			}
		}

	}

	@Test
	public void test2() {
		// 获取连接
		Jedis jedis = JedisPooUtill.get();

		Transaction multi = jedis.multi();
		multi.set("msg", "helloworld");
		List<Object> response = multi.exec();
		System.out.println(response);
		JedisPooUtill.release(jedis);
	}
}

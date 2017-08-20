package cn.chenny.redis.lock;

import java.util.List;
import java.util.UUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class JedisTransactionLock {
	private static ThreadLocal<Transaction> threadLocal = new ThreadLocal<Transaction>();

	/**
	 * 获取超时的分布式锁
	 * 
	 * @param jedis
	 * @param key
	 * @param acquireTime
	 *            获取锁的时间
	 * @param expireTime
	 *            key过期时间 单位/s
	 * @Param notTry 是否尝试获取锁。false 尝试，获取不到立即返回;true获取不到会进入死循环不断获取
	 * @return
	 */
	public static Transaction startTransaction(Jedis jedis, String key, long acquireTime, int expireTime,
			boolean noTry) {
		String value = UUID.randomUUID().toString();
		long nonaTime = System.currentTimeMillis();

		if (acquireTime < 0) {
			throw new RuntimeException("acquireTime获取锁的尝试时间 不能为负数");
		}

		while (noTry) {
			// 成功获取了分布式锁
			if (jedis.setnx(key, value) == 1) {
				// 监听key
				jedis.watch(key);
				// 开启事务
				Transaction tran = jedis.multi();
				// 设置过期时间
				tran.expire(key, expireTime);
				threadLocal.set(tran);
				return tran;
			}
			// 获取锁超时
			if (acquireTime != 0) {
				if (System.currentTimeMillis() - nonaTime > acquireTime * 1000) {
					return null;
				}
			}
			//不是尝试获取锁，才进入休眠状态
			if(noTry){
				// 休眠一秒，节省cpu资源
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * 获取阻塞的分布式锁 如果获取不到会循环获取锁
	 * 
	 * @param jedis
	 * @param key
	 * @param expireTime
	 * @return
	 */
	public static Transaction startTransaction(Jedis jedis, String key, int expireTime) {
		return startTransaction(jedis, key, 0, expireTime, true);
	}
	/**
	 * 尝试性的获取分布式锁
	 * @param jedis
	 * @param key
	 * @param expireTime
	 * @return
	 */
	public static Transaction tryStartTransaction(Jedis jedis, String key, int expireTime) {
		return startTransaction(jedis, key, 0, expireTime, false);
	}

	public static List<Object> commitTransaction(String key) {
		Transaction transaction = threadLocal.get();
		// 删除掉key
		transaction.del(key);
		// 提交事务
		List<Object> response = transaction.exec();
		return response;
	}

	public static void rollbackTransaction(Jedis jedis, String key) {
		Transaction transaction = threadLocal.get();
		// 删除掉key
		transaction.del(key);
		transaction.discard();
	}
}

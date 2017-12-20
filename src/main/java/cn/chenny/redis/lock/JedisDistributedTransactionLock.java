package cn.chenny.redis.lock;

import java.util.List;
import java.util.UUID;

import cn.chenny.redis.lock.model.WrapObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * 基于Redis的分布式锁，获取锁成功会返回一个事务，适用于事务操作
 */
public class JedisDistributedTransactionLock {
	private static ThreadLocal<WrapObject> threadLocal = new ThreadLocal<WrapObject>();


	/**
	 * 获取分布式锁的核心通用方法,用户可以组合参数来自定义锁的获取规则
	 * 
	 * @param key 指定setnx作用的对象，来实现锁
	 * @param acquireTime 获取锁等待时间的时间 单位/ms，如果等于0，表示只有获取成功线程才会被唤醒
	 * @param expireTime  key过期时间 单位/s，如果等于0，表示key一直有效
	 * @param isTry  是否尝试获取锁。true 尝试，获取不到立即返回;false获取不到会进入死循环不断获取
	 * @return
	 */
	public static Transaction start(String key,boolean isTry, long acquireTime, int expireTime) {
		//获取redis连接
		Jedis jedis=JedisPoolUtil.get();
		WrapObject wrapObject = new WrapObject();
		wrapObject.setJedis(jedis);
		threadLocal.set(wrapObject);

		String value = UUID.randomUUID().toString();
		long nonaTime = System.currentTimeMillis();

		if (acquireTime < 0) {
			throw new IllegalArgumentException("acquireTime不能为负数");
		}

		if (acquireTime < 0) {
			throw new IllegalArgumentException("exprieTime不能为负数");
		}

		while (true) {
			// 成功获取了分布式锁
			if (jedis.setnx(key, value) == 1) {
				// 监听key
				jedis.watch(key);
				// 开启事务
				Transaction tran = jedis.multi();
				// 设置过期时间
				if(expireTime!=0){
					tran.expire(key, expireTime);  //因为之前对key进行了监视，所以对key的一切修改都需放在事务内执行，否则事务会失效
				}
				threadLocal.get().setTransaction(tran);
				return tran;
			}
			// 获取锁的时间是否超时，超时则停止获取，直接返回
			if (acquireTime != 0) {
				if (System.currentTimeMillis() - nonaTime > acquireTime * 1000) {
					return null;
				}
			}
			//是否能尝试获取锁，能就继续进行获取锁操作。
			if(!isTry){
				// 休眠一秒，节省cpu资源
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return null;
				}
			}else{
				return null;
			}
		}
	}



	public static List<Object> commit(String key) {
		Transaction transaction = threadLocal.get().getTransaction();
		Jedis jedis = threadLocal.get().getJedis();
		if(transaction!=null){
			transaction.del(key);
			// 提交事务
			List<Object> response = transaction.exec();
			return response;
		}

		return null;
	}

	public static void rollback(String key) {
		Transaction transaction = threadLocal.get().getTransaction();
		if(transaction!=null){
			transaction.del(key);
			transaction.discard();
		}
	}

	public static Jedis getCurJedis(){
		return threadLocal.get().getJedis();
	}
}

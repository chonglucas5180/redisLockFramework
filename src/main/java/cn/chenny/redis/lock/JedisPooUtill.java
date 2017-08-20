package cn.chenny.redis.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPool;

public class JedisPooUtill {
	private static JedisPool jedisPool;
	static {
		// 构建连接池配置信息
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		// 设置最大连接数
		jedisPoolConfig.setMaxTotal(50);
		// 构建连接池
		jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
	}

	public static Jedis get() {
		// 从连接池中获取连接
		Jedis jedis = jedisPool.getResource();
		return jedis;
	}

	public static void release(Jedis jedis) {
		// 将连接还回到连接池中
		jedisPool.returnResource(jedis);
	}

	public static void closePool() {
		// 释放连接池
		jedisPool.close();
	}

}

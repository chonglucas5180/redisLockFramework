package cn.chenny.redis.lock.example;

import java.util.concurrent.CountDownLatch;

import cn.chenny.redis.lock.JedisPoolUtil;
import cn.chenny.redis.lock.JedisDistributedTransactionLock;
import  redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class ShopKillDemo {
	private static final int thread_num = 1000;
	private static final int split_node = 500;

	private static CountDownLatch startLatch = new CountDownLatch(1);
	private static CountDownLatch endLatch = new CountDownLatch(thread_num);

	/**
	 * 完成商品秒杀工作
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// 秒杀第一件商品
		for (int i = 0; i < split_node; i++) {
			new Thread(new ShopKillThread(ShopKillThread.shop1)).start();
		}
		
		//秒杀第二件商品
		for (int i = 0; i < split_node; i++) {
			new Thread(new ShopKillThread(ShopKillThread.shop2)).start();
		}
		
		//开始秒杀
		startLatch.countDown();
		//等待线程秒杀结束
		try {
			endLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Jedis jedis = JedisPoolUtil.get();
		String shop1 = jedis.get(ShopKillThread.shop1);
		String shop2 = jedis.get(ShopKillThread.shop2);
		System.out.println(shop1+"  "+shop2);
		
	}
	
	
	static class ShopKillThread implements Runnable{
		
		public static final String shop1="shop1";
		public static final String shop2="shop2";
		private String killShop;
		private String lockKey;
		
		
		public ShopKillThread(String killShop){
			this.killShop=killShop;
			this.lockKey="kill."+killShop;
		}
		
		public void run() {
			Jedis jedis=null;
			try {
				startLatch.await();

				// 开启事务
				Transaction tran = JedisDistributedTransactionLock.start(lockKey,false, 500L, 30);
				Response<String> num = tran.get(killShop);
				tran.decr(killShop);
				// 提交事务
				JedisDistributedTransactionLock.commit(lockKey);
				System.out.println(Thread.currentThread().toString()+" 秒杀【"+killShop+"】 第"+(500-Integer.parseInt(num.get())+1)+"件");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// 回滚事务
				JedisDistributedTransactionLock.rollback( lockKey);
			} finally {
				if(JedisDistributedTransactionLock.getCurJedis()!=null){
					JedisPoolUtil.release(JedisDistributedTransactionLock.getCurJedis());
				}
				endLatch.countDown();
			}

		}
	}
}

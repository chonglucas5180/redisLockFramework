# redisLockFramework
这是一个基于java的redis的分布式锁框架，通过锁来实现同步，并整合了事务操作。
能够实现在多线程下 对redis中的共享资源进行同步访问。


## 涉及的Redis指令
主要使用了redis中的watch,multi,exec,expire等指令

## 功能说明
核心代码请看JedisDistributedNormalLock.java和JedisDistributedTransactionLock.java

两者的都是分布式锁的实现，只不过后者在分布式锁的基础上实现了事务操作

读者可以对参数自由组合，实现各式各样的规则获取锁

以下列举支持的常用功能，

|功能|
|---|
|获取超时的锁|
|获取阻塞的分布式锁 如果获取不到会循环获取锁|
|尝试性的获取分布式锁|

## 代码演示

JedisDistributedNormalLock
```
Jedis jedis = null;
String key = null;
try {
    key = "lock.item.2";
    jedis = JedisDistributedNormalLock.lock(key, true, 10, 0);//获取分布式锁
    if (jedis != null) { // 获取成功
        jedis.set("message", "helloworld");
        String message = jedis.get("message");
        System.out.println(message);
    } else {
        System.out.println("获取锁失败");
    }
} catch (Exception e) {
    e.printStackTrace();
} finally {
    JedisDistributedNormalLock.realeaseLock(key);//操作完毕，释放锁
}
```


JedisDistributedTransactionLock
```
String key = null;
try {
    key = "lock.item.1";// 设置分布式锁的key
    Transaction tran = JedisDistributedTransactionLock.start(key, true, 10, 0);//获取分布式锁，成功返回事务实例
    if (tran != null) {// 获取成功
        tran.set("message", "helloworld");
        tran.get("message");
        List<Object> result = JedisDistributedTransactionLock.commit(key);
} catch (Exception e) {
    e.printStackTrace();
    JedisDistributedTransactionLock.rollback(key);// 回滚事务
} finally {
    JedisPoolUtil.release(jedis);//释放Jedis连接
}
```


##示例
基于这个项目做了一个关于并发环境下商品秒杀的demo
具体看ShopKillDemo.class

# redisTransactionLockFramework
这是一个基于java的redis的分布式锁框架，通过锁来实现同步，并整合了事务操作。
能够实现在多线程下 对redis中的共享资源进行同步访问

## redis指令说明
主要使用了redis中的watch,multi,exec,expire等指令

## 功能说明
根据Lock接口API来设计功能，具体看JedisTransactionLock.java

|功能|
|---|
|获取超时的锁|
|获取阻塞的分布式锁 如果获取不到会循环获取锁|
|尝试性的获取分布式锁|

##示例
基于这个项目做了一个关于并发环境下商品秒杀的demo
具体看ShopKillDemo.class

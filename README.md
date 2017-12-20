# csdn2Hexomd
CSDN博客迁移至Hexo,同步CSDN博文到本地MD文件。

功能：
- 利用爬虫爬取指定用户的CSDN博客文章
- 把爬取到的html文章内容转化为符合Hexo风格的markdown文件

通过这个项目得到了所有markdown文件，可以让我们很轻松的把文章同步到Hexo博客中，完成博客的迁移！

技术关键词：Java,网络爬虫

| 主要技术        | 工具           |
| ------------- |:-------------:| 
| 爬虫     | Httpclient,Jsoup | 
| Html转markdown      | [html2markdown](https://github.com/pnikosis/jHTML2Md)     |  

# redisTransactionLockFramework
这是一个基于java的redis的分布式锁框架，通过锁来实现同步，并整合了事务操作。
能够实现在多线程下 对redis中的共享资源进行同步访问
##redis指令说明
主要使用了redis中的watch,multi,exec,expire等指令
##功能说明
根据Lock接口API来设计功能，具体看JedisTransactionLock.java
|功能|
|---|
|获取超时的锁|
|获取阻塞的分布式锁 如果获取不到会循环获取锁|
|尝试性的获取分布式锁|
##示例
基于这个项目做了一个关于并发环境下商品秒杀的demo
具体看ShopKillDemo.class

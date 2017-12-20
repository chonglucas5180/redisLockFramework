package cn.chenny.redis.lock.model;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class WrapObject {
    private Jedis jedis;
    private Transaction transaction;


    public WrapObject() {
    }

    public WrapObject(Jedis jedis, Transaction transaction) {
        this.jedis = jedis;
        this.transaction = transaction;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

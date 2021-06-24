package com.xugd.jedisdemo;

import com.xugd.jedisdemo.pojo.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.params.SetParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JedisdemoApplicationTests {
    private Jedis jedis = null;
    @Autowired
    JedisPool jedisPool;

    @Before
    public void getCon() {
        jedis = jedisPool.getResource();
    }

    @After
    public void jedisClose() {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Test
    public void conPool() {
        String ping = jedis.ping();
        System.out.println(ping);
        jedis.set("name", "xiaoli");
        String name = jedis.get("name");
        System.out.println(name);
    }

    /**
     * 操作 String
     */
    @Test
    public void testString() {
        //设置单条值
        jedis.set("name", "小胡");
        //获取单条值
        String name = jedis.get("name");
        System.out.println(name);
        //设置多条值
        jedis.mset("age", "18", "addr", "nanjing");
        List<String> stringList = jedis.mget("name", "age", "addr");
        stringList.forEach(System.out::println);
        //通用删除
        jedis.del("name");

        //层级形式，目录结构存储数据
        jedis.set("china:jiangsu", "南京");
        String s = jedis.get("china:jiangsu");
        System.out.println(s);
    }

    /**
     * 操作 hash
     */
    @Test
    public void testHash() {
        jedis.hset("user", "name", "zhangboxing");
        String hget = jedis.hget("user", "name");
        System.out.println(hget);
        Map<String, String> map = new HashMap<>();
        map.put("age", "116");
        map.put("addr", "jiangning");
        jedis.hmset("user", map);
        List<String> hmget = jedis.hmget("user", "age", "addr");
        hmget.forEach(System.out::println);
        jedis.del("user", "age");
    }

    /**
     * 操作list
     */
    @Test
    public void testList() {
        //左添加
        jedis.lpush("student", "zhangsan", "lisi", "wangwu");
        //右添加数据
        jedis.rpush("student", "zhangsliu", "wangwu");
        List<String> student = jedis.lrange("student", 0, 5);
        student.forEach(System.out::println);
        //长度
        Long studentLength = jedis.llen("student");
        System.out.println(studentLength);
        //删除
        jedis.lrem("student", 2, "wangwu");
        System.out.println(jedis.llen("student"));

    }

    /**
     * 操作set
     */
    @Test
    public void testSet() {
        //添加数据
        jedis.sadd("letters", "aaa", "bbb", "ccc", "ddd");
        //获取数据
        Set<String> letters = jedis.smembers("letters");
        letters.forEach(System.out::println);
        //长度
        Long lettersLength = jedis.scard("letters");
        System.out.println(lettersLength);
        //删除
        jedis.srem("letters", "ccc");
        System.out.println(jedis.scard("letters"));
    }

    /**
     * 操作 Sorted set
     */
    @Test
    public void testSortedSet() {
        Map<String, Double> map = new HashMap<>();
        map.put("sanxing", 2D);
        map.put("huawei", 3D);
        map.put("apple", 1D);
        jedis.zadd("price", map);
        Set<String> priceSet = jedis.zrange("price", 0, 4);
        priceSet.forEach(System.out::println);
        Long price = jedis.zcard("price");
        System.out.println(price);
        jedis.zrem("price", "huawei");
        System.out.println(jedis.zcard("price"));
        Set<String> priceSet2 = jedis.zrange("price", 0, 3);
        priceSet2.forEach(System.out::println);

    }

    /**
     * 失效时间
     */
    @Test
    public void testExpire() throws InterruptedException {
        //设置key失效时间:秒（Ex）
        jedis.setex("code", 20, "test");
        Thread.sleep(1000);
        Long code = jedis.ttl("code");
        System.out.println(code);
        //给已存在的key 失效时间:秒
        jedis.expire("name", 10);
        Long name = jedis.pttl("name");
        System.out.println(name);
        //nx key不存在设置
        SetParams setParams = new SetParams();
        setParams.ex(20).nx();
        jedis.set("code1", "test", setParams);
    }

    /**
     * 获取所有key
     */
    @Test
    public void testKeys() {
        Set<String> keys = jedis.keys("*");
        keys.forEach(System.out::println);
        //获取数据库大小
        System.out.println(jedis.dbSize());
    }

    /**
     * 事务
     */
    @Test
    public void testTX() {
        //开启事务
        Transaction tx = jedis.multi();
        //存值
        tx.set("codess","test2");
        //提交事务
//        tx.exec();
        //取消事务
        tx.discard();

        User user=new User();
        user.setId("1");
        user.setUsername("admin");
        user.setPassword("123456");



    }
    /**
     * 序列化、反序列化
     */
    @Test
    public void testServ() {
        User user=new User();
        user.setId("1");
        user.setUsername("admin");
        user.setPassword("123456");
        jedis.set(SerializationUtils.serialize("user"),SerializationUtils.serialize(user));
        byte[] users = jedis.get(SerializationUtils.serialize("user"));
        System.out.println(SerializationUtils.deserialize(users));



    }
}

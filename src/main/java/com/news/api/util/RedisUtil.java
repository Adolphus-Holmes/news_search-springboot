package com.news.api.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;
import java.util.Map;

public class RedisUtil {
    static JedisPoolConfig poolConfig = new JedisPoolConfig();
    static JedisPool pool = new JedisPool(poolConfig, "127.0.0.1", 6379, 2000);

    public static String GenerateToken(String username,Boolean longtime) throws Exception{
        Jedis jedis = pool.getResource();
        Date date = new Date();
        String token = MD5Util.getMD5(username + date);
        jedis.set(token,username);
        if(longtime){
            jedis.expire(token,60*60*24*14);
        }else{
            jedis.expire(token,60*60);
        }
        jedis.close();
        return token;
    }

    public static String GetToken(String token) throws Exception{
        Jedis jedis = pool.getResource();
        String username = jedis.get(token);
        if(jedis.ttl(token) <= 1800){
            jedis.expire(token, 1800);
        }
        jedis.close();
        return username;
    }

    public static boolean DelToken(String token) throws Exception{
        Jedis jedis = pool.getResource();
        jedis.del(token);
        jedis.close();
        return true;
    }

    public static String GetPublicKey() throws Exception{
        Jedis jedis = pool.getResource();
        String publicKey = jedis.get("publicKey");
        if(publicKey == null){
            publicKey = GenerateKey();
        }
        jedis.close();
        return publicKey;
    }

    public static String GenerateKey() throws Exception{
        Jedis jedis = pool.getResource();
        Map<String,String> result = RSAUtil.generateRsaKey(2048);
        jedis.set("publicKey",result.get("publicKey"));//对已有key进行set操作会重新赋值
        jedis.expire("publicKey",60*5);
        jedis.set(result.get("publicKey"),result.get("privateKey"));
        jedis.expire(result.get("publicKey"),60*5);
        jedis.close();
        return result.get("publicKey");
    }

    public static String GetPrivateKey(String publicKey) throws Exception{
        Jedis jedis = pool.getResource();
        return jedis.get(publicKey);
    }
}

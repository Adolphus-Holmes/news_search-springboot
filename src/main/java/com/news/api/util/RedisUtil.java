package com.news.api.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;
import java.util.Map;

public class RedisUtil {
    static JedisPoolConfig poolConfig = new JedisPoolConfig();
    static JedisPool pool = new JedisPool(poolConfig, "127.0.0.1", 6379, 2000);
    static Jedis jedis = null;

    public static String GenerateToken(String username,Boolean longtime) throws Exception{
        jedis = pool.getResource();
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
        jedis = pool.getResource();
        String username = jedis.get(token);
        if(jedis.ttl(token) <= 1800){
            jedis.expire(token, 1800);
        }
        jedis.close();
        return username;
    }

    public static boolean DelToken(String token) throws Exception{
        jedis = pool.getResource();
        jedis.del(token);
        jedis.close();
        return true;
    }

    public static String GetPublicKey() throws Exception{
        jedis = pool.getResource();
        String publicKey = jedis.get("publicKey");
        if(publicKey == null){
            Map<String,String> result = RSAUtil.generateRsaKey(2048);
            jedis.set("publicKey",result.get("publicKey"));
            jedis.expire("publicKey",60*5);
            jedis.set(result.get("publicKey"),result.get("privateKey"));
            jedis.expire(result.get("publicKey"),60*5);
            publicKey = result.get("publicKey");
        }
        jedis.close();
        return publicKey;
    }

    public static String GetPrivateKey(String publicKey) throws Exception{
        jedis = pool.getResource();
        return jedis.get(publicKey);
    }
}

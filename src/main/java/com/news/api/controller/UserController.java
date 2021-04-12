package com.news.api.controller;

import com.news.api.entity.User;
import com.news.api.service.UserService;
import com.news.api.util.RSAUtil;
import com.news.api.util.RedisUtil;
import com.news.api.util.MD5Util;
import com.news.api.util.XSRFException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    ElasticsearchTemplate<User, String> elasticsearchTemplate;

    @GetMapping("/namesake")
    public boolean namesake(@RequestParam String username) throws Exception{
        return elasticsearchTemplate.exists(username, User.class);
    }

    @PostMapping("/register")
    public boolean register(@RequestBody Map<String,Object> map) throws Exception {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String publickey = (String) map.get("key");
        String privatekey = RedisUtil.GetPrivateKey(publickey);
        Assert.notNull(privatekey,"公钥已过期");
        password = RSAUtil.decrypt(password,privatekey);
        username = RSAUtil.decrypt(username,privatekey);
        if(namesake(username)){
            return false;
        }else{
            User user = new User();
            user.setUsername(username);
            user.setPassword(MD5Util.getMD5(password));
            return elasticsearchTemplate.save(user);
        }
    }

    @PostMapping("/login")
    public boolean login(@RequestBody Map<String,Object> map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String publickey = (String) map.get("key");
        Boolean longtime = (Boolean) map.get("longtime");
        String privatekey = RedisUtil.GetPrivateKey(publickey);
        Assert.notNull(privatekey,"公钥已过期");
            password = RSAUtil.decrypt(password,privatekey);
            username = RSAUtil.decrypt(username,privatekey);
        if(elasticsearchTemplate.exists(username, User.class)){
            User user = elasticsearchTemplate.getById(username,User.class);
            if(user.getPassword().equals(MD5Util.getMD5(password))){
                Cookie logeed = new Cookie("logeed",RedisUtil.GenerateToken(user.getUsername(),longtime));
                Cookie xsrf = new Cookie("XSRF-TOKEN",MD5Util.getMD5(String.valueOf(new Date())));
                if(longtime){
                    logeed.setMaxAge(60*60*24*14);
                    xsrf.setMaxAge(60*60*24*14);
                }
                logeed.setPath("/");
                xsrf.setPath("/");
                logeed.setHttpOnly(true);
                response.addCookie(logeed);
                response.addCookie(xsrf);
                return true;
            }
        }
        response.setStatus(401);
        return false;
    }

    @GetMapping("/getkey")
    public String key() throws Exception{
        return RedisUtil.GetPublicKey();
    }

    @GetMapping("/userinfo")
    public User userinfo(@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF,@CookieValue("logeed") String logeed,HttpServletResponse response) throws Exception{
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        User user = elasticsearchTemplate.getById(username,User.class);
        if(user != null){
            user.setPassword("");
        }else{
            RedisUtil.DelToken(logeed);
            response.setStatus(401);
        }
        return user;
    }

    @DeleteMapping("/logout")
    public boolean logout(@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF,@CookieValue("logeed") String logeed,HttpServletRequest request, HttpServletResponse response) throws Exception{
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        Cookie cookie = new Cookie("logeed",null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);//具有http-only的cookie只能由后端删除
        return RedisUtil.DelToken(logeed);
    }

    @PatchMapping("/setsubscribe")
    public boolean subscribe(@RequestBody List<String> subscribe,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF,@CookieValue("logeed") String logeed) throws Exception{
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        User user = new User();
        user.setUsername(username);
        user.setSubscribe(subscribe);
        return elasticsearchTemplate.update(user);
    }

    @PatchMapping("/setpetname")
    public boolean changepetname(@RequestBody Map<String,String> map,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF,@CookieValue("logeed") String logeed) throws Exception{
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String petname = map.get("petname");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        User user = new User();
        user.setUsername(username);
        user.setPetname(petname);
        return elasticsearchTemplate.update(user);
    }

    @PatchMapping("/setpassword")
    public boolean setpassword(@RequestBody Map<String,String> map,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF,@CookieValue("logeed") String logeed,HttpServletResponse response) throws Exception{
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String password = map.get("password");
        String newpassword = map.get("newpassword");
        String publickey = map.get("key");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        String privatekey = RedisUtil.GetPrivateKey(publickey);
        Assert.notNull(privatekey,"公钥已过期");
        User user = elasticsearchTemplate.getById(username, User.class);
        password = RSAUtil.decrypt(password,privatekey);
        newpassword = RSAUtil.decrypt(newpassword,privatekey);
        if(user.getPassword().equals(MD5Util.getMD5(password))){
            user.setPassword(MD5Util.getMD5(newpassword));
            return elasticsearchTemplate.update(user);
        }
        response.setStatus(401);
        return false;
    }

}

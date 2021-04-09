package com.news.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.news.api.entity.Article;
import com.news.api.entity.Records;
import com.news.api.entity.User;
import com.news.api.util.MD5Util;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.index.query.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.repository.*;
import org.zxp.esclientrhl.util.JsonUtils;
import org.zxp.esclientrhl.util.Tools;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootApplicationTests {

    @Autowired
    ElasticsearchTemplate<Article, String> elasticsearchTemplate;

    @Autowired
    ElasticsearchTemplate<User,String> UserTemplate;

    @Autowired
    ElasticsearchTemplate<Records, String> recordTemplate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd'T'HH:mm:ss.SSSZ",timezone="GMT+8")

    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    JedisPool pool = new JedisPool(poolConfig, "127.0.0.1", 6379, 2000);
    Jedis jedis = null;

    @Test
    public void testQuery() {
        System.out.println("测试：");
        try {
            //matchQuery分词匹配 .operator(Operator.AND)则改变逻辑，要求每个关键词都出现
            QueryBuilder queryBuilder1= QueryBuilders.matchQuery("text","番茄 水果");
            //termQuery不分词 fuzzyQuery不分词，且可以修正字母错误
            QueryBuilder queryBuilder2 = QueryBuilders.termQuery("text","烹饪");
            QueryBuilder queryBuilder3 = QueryBuilders.multiMatchQuery("番茄水果","title","text");
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(queryBuilder1);
            queryBuilder.must(queryBuilder2);
            new MultiMatchQueryBuilder("text","烹饪");
            //分页
            int currentPage = 1;
            int pageSize = 10;
            PageSortHighLight psh = new PageSortHighLight(currentPage, pageSize);
            //多字段高亮
            HighLight highlight = new HighLight();
            highlight.field("title");
            System.out.println(highlight.getHighLightList());
            highlight.field("text");
            psh.setHighLight(highlight);
            System.out.println(highlight.getHighLightList());
            System.out.println(queryBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test void md5(){
        jedis = pool.getResource();
        String username = "admin";
        Date date = new Date();
        String token = MD5Util.getMD5(username + "%oj5afk$" +date);
        jedis.set(token,username);
        jedis.expire(token,60*60*24*14);
        System.out.println(token);
    }
    @Test
    public void testredis(){
        jedis = pool.getResource();
        System.out.println(jedis.get("15679"));
        jedis.close();

    }
    @Test
    public void testrecord(){
        Records re = new Records();
        re.setKeyword("人民");
        re.setUsername("admin");
        Long l = (long) (1000 * 1616169600);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(1000 * 1616169600)));
        re.setDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(1000 * 1616169600)));
        try {
            recordTemplate.save(re);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test9() throws IllegalAccessException {
        String str = "";
        boolean n = StringUtils.isEmpty(str);
        Records t = new Records();
        t.setId("main");
        Field[] fields = t.getClass().getDeclaredFields();
        System.out.println(Arrays.toString(fields));
        for(Field f : fields){
            f.setAccessible(true);
            ESID esid = f.getAnnotation(ESID.class);
            System.out.println(esid);
            if(esid != null){
                Object value = f.get(t);
                System.out.println("classname:"+f);
                System.out.println(esid.getClass());
            }
        }
        String source = JsonUtils.obj2String(t);
        //indexRequest.source(source, XContentType.JSON);
        try {
            String id = Tools.getESId(t);
            //System.out.println(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test void updateuser() throws Exception{
        User user = new User();
        user.setUsername("admin");
        boolean b =UserTemplate.exists("user", User.class);
        System.out.println(b);
    }

    @Test
    public void testagg() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date from= sdf.parse("2021-03-21");//早的日期
        Date to= sdf.parse("2021-03-23");
        String usename = "admin";
        BoolQueryBuilder bool_queryBuilder = new BoolQueryBuilder();
        bool_queryBuilder.must(QueryBuilders.rangeQuery("date").from("2021-03-22").to("2021-03-23"));
        bool_queryBuilder.must(QueryBuilders.termQuery("username",usename));
        List<Records> list = recordTemplate.searchMore(bool_queryBuilder,100,Records.class);
        System.out.println(list);
    }

    @Test
    public void testhot(){
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("date").from("now-7d/d").to("now/d");//7天内被搜索次数
        Map map = null;
        try {
            map = recordTemplate.aggs("keyword.keyword", AggsType.count,queryBuilder, Records.class,"keyword.keyword");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //map.forEach((k,v) -> System.out.println(k+"     "+v));
        int i = 0;
        List<String> list = new ArrayList<>();
        for(Object k : map.keySet()){
            list.add(k.toString());
            if(i >= 9){
                break;
            }else{
                i++;
            }
        }
        System.out.println(list);
    }

    @Test void test564(){
        int num = 14 * 24 * 60 * 60;
        System.out.println(num);
    }
    @Test
    public void testSuggest() {
        ElasticsearchTemplateImpl.PhraseSuggestParam param = new ElasticsearchTemplateImpl.PhraseSuggestParam(1,1,"ik_smart","always");
        try {
            //List<String> s = recordTemplate.phraseSuggest("keyword","人民群众的汪洋大海",param, Records.class);
            List<String> list = recordTemplate.completionSuggest("keyword", "群", Records.class);
            //System.out.println(s);
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void usertest(){
        User user = new User();
        //user.setId("user");
        user.setUsername("admin");
        boolean exists = false;
        try {
            exists = UserTemplate.exists("user", User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!exists){
            user.setPassword(MD5Util.getMD5("123456"));
            user.setPetname("影尘");
            user.setRoot(true);
            try {
                UserTemplate.save(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            user = UserTemplate.getById("admin",User.class);
            System.out.println(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void GetQuery() throws IOException {
        Pattern r = null;
        Matcher m = null;
        String keyword = "手机|网络 -群星 \"法律\" 天津群众 \"主任\" 公平|公正|法制";
        //es并不会索引符号
        String match_keyword = keyword.replaceAll("\"(?<=\\\")(\\S+)(?=\\\")\"","");//需加上匹配内容两端的字符
        match_keyword = match_keyword.replaceAll("\\-(?<=\\-)(\\S+)","");
        match_keyword = match_keyword.replaceAll("(\\S+)\\|(\\S+)","");
        System.out.println("match_keyword:"+match_keyword);
        List<String> must_keyword = new ArrayList<>();
        List<String> no_keyword = new ArrayList<>();
        System.out.println("no_keyword:"+no_keyword);
        List<List<String>> or_keyword = new ArrayList<>();
        System.out.println(or_keyword.isEmpty());
        r = Pattern.compile("(?<=\\\")(\\S+)(?=\\\")");
        m = r.matcher(keyword);
        while(m.find()) {
            must_keyword.add(m.group());
        }
        System.out.println("must_keyword:"+must_keyword);
        r = Pattern.compile("(?<=\\-)(\\S+)");
        m = r.matcher(keyword);
        while(m.find()) {
            no_keyword.add(m.group());
        }
        System.out.println("no_keyword:"+no_keyword);
        r = Pattern.compile("(\\S+)\\|(\\S+)");
        m = r.matcher(keyword);
        while(m.find()) {
            or_keyword.add(Arrays.asList(m.group().split("\\|")));
        }
        System.out.println("no_keyword:"+or_keyword);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.multiMatchQuery(match_keyword,"title","text").type(MultiMatchQueryBuilder.Type.CROSS_FIELDS));
        BoolQueryBuilder bool_queryBuilder;
        for (String s:must_keyword){
            bool_queryBuilder = QueryBuilders.boolQuery();
            bool_queryBuilder.should(QueryBuilders.termQuery("title",s));
            bool_queryBuilder.should(QueryBuilders.termQuery("text",s));
            queryBuilder.must(bool_queryBuilder);
        }
        for (String s:no_keyword){
            queryBuilder.mustNot(QueryBuilders.termQuery("title",s));
            queryBuilder.mustNot(QueryBuilders.termQuery("text",s));
        }
        for (List<String> ls:or_keyword){
            bool_queryBuilder = QueryBuilders.boolQuery();
            for(String s:ls){
                bool_queryBuilder.should(QueryBuilders.termQuery("title",s));
                bool_queryBuilder.should(QueryBuilders.termQuery("text",s));
            }
            queryBuilder.must(bool_queryBuilder);
        }
        Gson gson = new Gson();
        String str = gson.toJson(queryBuilder);
        BoolQueryBuilder jsonquery = gson.fromJson(str, BoolQueryBuilder.class);
        System.out.println(jsonquery);
    }
}

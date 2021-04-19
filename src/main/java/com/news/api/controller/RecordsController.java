package com.news.api.controller;

import com.news.api.entity.Records;
import com.news.api.util.RedisUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.zxp.esclientrhl.enums.AggsType;
import org.zxp.esclientrhl.repository.ElasticsearchTemplate;
import org.zxp.esclientrhl.repository.PageSortHighLight;
import org.zxp.esclientrhl.repository.Sort;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class RecordsController {

    @Autowired
    ElasticsearchTemplate<Records, String> elasticsearchTemplate;

    @PostMapping("/pushrecord")
    public boolean pushrecord(@RequestBody Records record,@CookieValue(value = "logeed",required = false) String logeed) throws Exception {
        if(!logeed.isEmpty()){
            record.setUsername(RedisUtil.GetToken(logeed));
        }
        record.setDate(new Date());
        return elasticsearchTemplate.save(record);
    }

    @GetMapping("/completion")
    public List<String> completion(@RequestParam String keyword) throws Exception {
        return elasticsearchTemplate.completionSuggest("keyword.suggest", keyword, Records.class);
    }

    @GetMapping("/hotkey")
    public List<String> hotkey() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("date").from("now-7d/d").to("now/d");//7天内被搜索次数
        Map map = elasticsearchTemplate.aggs("keyword.keyword", AggsType.count,queryBuilder, Records.class,"keyword.keyword");
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
        return list;
    }

    @GetMapping("/history")
    public List<Records> history(@RequestParam Integer page,@RequestParam Integer size,@RequestParam String from,@RequestParam String to,@CookieValue("logeed") String logeed,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF) throws Exception {
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        BoolQueryBuilder bool_queryBuilder = new BoolQueryBuilder();
        if(!from.isEmpty() && !to.isEmpty()){
            bool_queryBuilder.must(QueryBuilders.rangeQuery("date").from(from).to(to));
        }
        PageSortHighLight psh = new PageSortHighLight(page, size);
        String sorter = "date";
        Sort.Order order = new Sort.Order(SortOrder.DESC,sorter);
        psh.setSort(new Sort(order));
        bool_queryBuilder.must(QueryBuilders.termQuery("username",username));
        return elasticsearchTemplate.search(bool_queryBuilder,psh,Records.class).getList();
    }

    @GetMapping("/historycount")
    public long historycount(@RequestParam String from,@RequestParam String to,@CookieValue("logeed") String logeed,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF) throws Exception {
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        BoolQueryBuilder bool_queryBuilder = new BoolQueryBuilder();
        if(!from.isEmpty() && !to.isEmpty()){
            System.out.println(from);
            System.out.println(to);
            bool_queryBuilder.must(QueryBuilders.rangeQuery("date").from(from).to(to));
        }
        return elasticsearchTemplate.count(bool_queryBuilder,Records.class);
    }

    @DeleteMapping("/delrecord")
    public boolean delrecord(@RequestBody List<String> idlist,@CookieValue("logeed") String logeed,@RequestHeader("X-XSRF-TOKEN") String TOKEN,@CookieValue("XSRF-TOKEN") String XSRF) throws Exception {
        Assert.isTrue(TOKEN.equals(XSRF),"请求头与XSRF-TOKEN不一致");
        String username = RedisUtil.GetToken(logeed);
        Assert.notNull(username,"Token已过期");
        List<Records> relist = elasticsearchTemplate.mgetById(new String[idlist.size()],Records.class);
        relist.forEach(
                (item) -> {
                    try {
                        Assert.isTrue(username.equals(item.getUsername()),"恶意请求");//forEach实质是一个Lambda，不能对外部变量赋值
                        String id = item.getId();
                        elasticsearchTemplate.deleteById(id,Records.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        return true;
    }
}

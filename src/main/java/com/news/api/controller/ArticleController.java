package com.news.api.controller;

import com.google.gson.Gson;
import com.news.api.entity.Article;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zxp.esclientrhl.repository.*;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ArticleController {

    @Autowired
    ElasticsearchTemplate<Article, String> elasticsearchTemplate;
    /*/
    @GetMapping("/phrase")
    public List
    /*/
    @GetMapping("/search")
    public List<Article> search(@RequestParam String keyword,@RequestParam Integer page,@RequestParam Integer size,@RequestParam String sort,@RequestParam String start,@RequestParam String end) throws Exception{
        PageSortHighLight psh = new PageSortHighLight(page, size);
        //多字段高亮
        HighLight highlight = new HighLight();
        highlight.field("title");
        highlight.field("text");
        psh.setHighLight(highlight);
        PageList<Article> res;
        BoolQueryBuilder queryBuilder = GenerateQuery(keyword).minimumShouldMatch("100%");
        if(!start.isEmpty() && !end.isEmpty()){
            queryBuilder.must(QueryBuilders.rangeQuery("release_date").from(start).to(end));
        }
        if(sort.equals("time_desc")){
            String sorter = "release_date";
            Sort.Order order = new Sort.Order(SortOrder.DESC,sorter);
            psh.setSort(new Sort(order));
        }
        res = elasticsearchTemplate.search(queryBuilder,psh,Article.class);
        return res.getList();
    }

    @GetMapping("/find")
    public Article find(@RequestParam String id) throws Exception{
        return elasticsearchTemplate.getById(id,Article.class);
    }

    @GetMapping("/count")
    public Map<String, Double> count(@RequestParam String keyword,@RequestParam String start,@RequestParam String end) throws Exception{
        Map<String,Double> map = new HashMap();
        //这个数据无需重复获取
        long startTime = System.currentTimeMillis();
        BoolQueryBuilder queryBuilder = GenerateQuery(keyword);
        if(!start.isEmpty() && !end.isEmpty()){
            queryBuilder.must(QueryBuilders.rangeQuery("release_date").from(start).to(end));
        }
        long count = elasticsearchTemplate.count(queryBuilder, Article.class);
        map.put("count", (double) count);
        map.put("time", ((System.currentTimeMillis() - startTime) / 1000d));
        return map;
    }

    @PostMapping("/subscribe")
    public List<Article> subscribe(@RequestBody List<String> subscribe,@RequestParam int page) throws Exception {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        for (String s:subscribe){
            bool.should(GenerateQuery(s));
        }
        PageSortHighLight psh = new PageSortHighLight(page, 20);
        String sorter = "release_date";
        Sort.Order order = new Sort.Order(SortOrder.DESC,sorter);
        psh.setSort(new Sort(order));
        Attach attach = new Attach();
        String[] includes = {"id","title","release_date"};
        attach.setIncludes(includes);
        attach.setPageSortHighLight(psh);
        return elasticsearchTemplate.search(bool,attach,Article.class).getList();
    }
    @PostMapping("searchid")
    public Map searchid(@RequestBody Map data) throws Exception{
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        String str = gson.toJson(data);
        Map map2 = gson.fromJson(str, map.getClass());
        System.out.println(str.hashCode());
        System.out.println(map2);
        return map;
    }
    @PostMapping("/smart")
    public List<Article> smart(@RequestBody List<List<Map>> vec) throws Exception {
        BoolQueryBuilder doc = QueryBuilders.boolQuery();
        BoolQueryBuilder wd;
        for(List<Map> word:vec){
            wd = QueryBuilders.boolQuery();
            for(Map map:word){
                String w = (String) map.get("word");
                float f = Float.parseFloat(map.get("factor").toString());
                wd.should(QueryBuilders.termQuery("text",w)).boost(f);
                System.out.println("word:"+w+"    "+"factor:"+f+"\n");
            }
            doc.should(wd);
        }
        return  elasticsearchTemplate.searchMore(doc.minimumShouldMatch("45%"),10,Article.class);
    }

    //构建高级搜索所用query
    protected BoolQueryBuilder GenerateQuery(String keyword) {
        //long startTime = System.currentTimeMillis();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder bool_queryBuilder;
        Pattern r;
        Matcher m;
        List<String> must_keyword = new ArrayList<>();
        List<String> no_keyword = new ArrayList<>();
        List<List<String>> or_keyword = new ArrayList<>();

        String match_keyword = keyword.replaceAll("\"(?<=\\\")(\\S+)(?=\\\")\"","");//需加上匹配内容两端的字符
        match_keyword = match_keyword.replaceAll("\\-(?<=\\-)(\\S+)","");
        match_keyword = match_keyword.replaceAll("(\\S+)\\|(\\S+)","");
        r = Pattern.compile("[\\u4e00-\\u9fa5_a-zA-Z0-9]+");//分离符号，仅取中文、英文字母和数字
        m = r.matcher(match_keyword);
        StringBuilder mk = new StringBuilder();
        boolean match = false;
        while(m.find()) {
            mk.append(" ").append(m.group());
            match = true;
        }
        if(match){
            queryBuilder.must(QueryBuilders.multiMatchQuery(mk.toString(),"title","text").type(MultiMatchQueryBuilder.Type.CROSS_FIELDS).minimumShouldMatch("45%"));
        }
        r = Pattern.compile("(?<=\\\")(\\S+)(?=\\\")");
        m = r.matcher(keyword);
        while(m.find()) {
            must_keyword.add(m.group());
        }
        if(!must_keyword.isEmpty()){
            for (String s:must_keyword){
                bool_queryBuilder = QueryBuilders.boolQuery();
                bool_queryBuilder.should(QueryBuilders.termQuery("title",s));
                bool_queryBuilder.should(QueryBuilders.termQuery("text",s));
                queryBuilder.must(bool_queryBuilder);
            }
        }

        r = Pattern.compile("(?<=\\-)(\\S+)");
        m = r.matcher(keyword);
        while(m.find()) {
            no_keyword.add(m.group());
        }
        if(!no_keyword.isEmpty()){
            for (String s:no_keyword){
                queryBuilder.mustNot(QueryBuilders.termQuery("title",s));
                queryBuilder.mustNot(QueryBuilders.termQuery("text",s));
            }
        }

        r = Pattern.compile("(\\S+)\\|(\\S+)");
        m = r.matcher(keyword);
        while(m.find()) {
            or_keyword.add(Arrays.asList(m.group().split("\\|")));
        }
        if(!or_keyword.isEmpty()){
            for (List<String> ls:or_keyword){
                bool_queryBuilder = QueryBuilders.boolQuery();
                for(String s:ls){
                    bool_queryBuilder.should(QueryBuilders.termQuery("title",s));
                    bool_queryBuilder.should(QueryBuilders.termQuery("text",s));
                }
                queryBuilder.must(bool_queryBuilder);
            }
        }
        //System.out.println((System.currentTimeMillis() - startTime) / 1000d);
        return queryBuilder;
    }
}

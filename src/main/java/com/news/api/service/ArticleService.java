package com.news.api.service;

import com.news.api.entity.Article;
import org.zxp.esclientrhl.auto.intfproxy.ESCRepository;


import java.util.List;
import java.util.Optional;

public interface ArticleService extends ESCRepository<Article, String> {
}

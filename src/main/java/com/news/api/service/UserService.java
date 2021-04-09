package com.news.api.service;

import com.news.api.entity.Article;
import com.news.api.entity.User;
import org.elasticsearch.index.query.QueryBuilder;
import org.zxp.esclientrhl.auto.intfproxy.ESCRepository;

public interface UserService extends ESCRepository<User, String> {
}

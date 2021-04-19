package com.news.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.elasticsearch.search.DocValueFormat;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

@Data
@ESMetaData(indexName = "search_records",number_of_shards = 2,number_of_replicas = 0,printLog = true)
public class Records implements Serializable {
    private static final long serialVersionUID = 7167693614180349771L;
    @ESID
    private String id;
    @ESMapping(datatype = DataType.text_type,suggest = true)
    private String keyword;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    @ESMapping(datatype = DataType.date_type)
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ESMapping(keyword = true)
    private String username;
}

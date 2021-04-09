package com.news.api.entity;

import lombok.Data;
import org.elasticsearch.search.DocValueFormat;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.text.DateFormat;
import java.util.Date;

@Data
@ESMetaData(indexName = "search_records",number_of_shards = 2,number_of_replicas = 0,printLog = true)
public class Records {
    @ESID
    private String id;
    @ESMapping(datatype = DataType.text_type,suggest = true)
    private String keyword;
    @ESMapping(datatype = DataType.date_type)
    private Object date;
    @ESMapping(keyword = true)
    private String username;
}

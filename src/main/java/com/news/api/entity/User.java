package com.news.api.entity;

import lombok.Data;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.util.List;

@Data
@ESMetaData(indexName = "user_info",number_of_shards = 2,number_of_replicas = 0,printLog = true)
public class User {
    @ESID
    private String username;
    @ESMapping(keyword = true)
    private String password;
    @ESMapping(keyword = true)
    private String petname;
    @ESMapping(keyword = true,datatype = DataType.boolean_type)
    private boolean root;
    @ESMapping(datatype = DataType.text_type)
    private List<String> subscribe;
}

package com.news.api.entity;

import lombok.Data;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.io.Serializable;
import java.util.List;

@Data
@ESMetaData(indexName = "user_info",number_of_shards = 2,number_of_replicas = 0,printLog = true)
public class User implements Serializable {
    private static final long serialVersionUID = -9023263845303252847L;
    @ESID
    private String username;
    @ESMapping(keyword = true)
    private String password;
    @ESMapping(keyword = true)
    private String petname;
    @ESMapping(keyword = true,datatype = DataType.boolean_type)
    private Boolean root;
    @ESMapping(datatype = DataType.text_type)
    private List<String> subscribe;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPetname() {
        return petname;
    }

    public void setPetname(String petname) {
        this.petname = petname;
    }

    public Boolean isRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public List<String> getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(List<String> subscribe) {
        this.subscribe = subscribe;
    }
}

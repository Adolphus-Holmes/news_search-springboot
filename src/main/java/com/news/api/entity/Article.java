package com.news.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.DataType;

import java.io.Serializable;
import java.util.Date;

@Data
@ESMetaData(indexName = "news_data",number_of_shards = 2,number_of_replicas = 0,printLog = true)
public class Article implements Serializable {
    private static final long serialVersionUID = 7653349994587477388L;
    @ESID
    private String id;

    @ESMapping(datatype = DataType.text_type)
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssss",timezone = "GMT+8")
    @ESMapping(datatype = DataType.date_type)
    private Date crawl_date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss",timezone = "GMT+8")
    @ESMapping(datatype = DataType.date_type)
    private Date release_date;

    @ESMapping(datatype = DataType.text_type)
    private String subtitle;

    @ESMapping(datatype = DataType.text_type)
    private String author;

    @ESMapping(datatype = DataType.text_type)
    private String source;

    @ESMapping(datatype = DataType.text_type)
    private String editor;

    @ESMapping(datatype = DataType.text_type)
    private String text;

    @ESMapping(datatype = DataType.text_type)
    private String domain;

    @ESMapping(keyword = true)
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getCrawl_date() {
        return crawl_date;
    }

    public void setCrawl_date(Date crawl_date) {
        this.crawl_date = crawl_date;
    }

    public Object getRelease_date() {
        return release_date;
    }

    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", crawl_date=" + crawl_date +
                ", release_date=" + release_date +
                ", subtitle='" + subtitle + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", editor='" + editor + '\'' +
                ", text='" + text + '\'' +
                ", domain='" + domain + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

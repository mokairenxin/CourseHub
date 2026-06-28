package com.hnit.coursehub.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PageBean<T> implements Serializable {
    private List<T> records = Collections.emptyList();
    private int page;
    private int pageSize;
    private int total;

    public PageBean() {
    }

    public PageBean(List<T> records, int page, int pageSize, int total) {
        this.records = records;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPages() {
        return total == 0 ? 1 : (int) Math.ceil(total * 1.0 / pageSize);
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }
}

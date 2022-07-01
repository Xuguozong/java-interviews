package com.work.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class StockDao implements IStockDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getStockList() {
        final String sql = "select id as sku_id, title, images, stock, price, indexes, own_spec from tb_sku";
        return jdbcTemplate.queryForList(sql);
    }
}

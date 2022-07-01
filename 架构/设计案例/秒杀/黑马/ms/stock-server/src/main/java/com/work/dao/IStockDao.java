package com.work.dao;

import java.util.List;
import java.util.Map;

public interface IStockDao {

    List<Map<String, Object>> getStockList();
}

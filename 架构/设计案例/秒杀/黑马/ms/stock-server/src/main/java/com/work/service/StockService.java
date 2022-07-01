package com.work.service;

import com.work.dao.IStockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class StockService implements IStockService {

    @Autowired
    private IStockDao stockDao;

    @Override
    public Map<String, Object> getStockList() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> stockList = stockDao.getStockList();
        if (stockList.isEmpty()){
            resultMap.put("result", false);
            resultMap.put("msg", "there is no data");
            return resultMap;
        }
        // 从redis中取出数据
        // todo
        resultMap.put("sku_list", stockList);
        return resultMap;
    }
}

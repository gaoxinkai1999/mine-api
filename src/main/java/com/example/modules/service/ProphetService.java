package com.example.modules.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProphetService {

    @Value("${prophet.api.url:http://localhost:5000}")
    private String prophetApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ProphetService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 批量训练多个商品的Prophet模型并获取预测结果
     *
     * @param productSalesDataMap 商品ID到销售数据的映射，每个销售数据包含 ds (日期) 和 y (销量)
     * @param periods 预测天数
     * @return Map<Integer, List<Map<String, Object>>> 商品ID到预测结果的映射
     */
    public Map<Integer, List<Map<String, Object>>> batchTrainAndForecast(
            Map<Integer, List<Map<String, Object>>> productSalesDataMap,
            int periods) {
        try {
            // 1. 准备批量训练数据
            List<Map<String, Object>> modelRequests = new ArrayList<>();
            
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : productSalesDataMap.entrySet()) {
                Integer productId = entry.getKey();
                List<Map<String, Object>> salesData = entry.getValue();
                
                // 创建模型请求
                Map<String, Object> modelRequest = new HashMap<>();
                modelRequest.put("model_name", "product_" + productId);
                modelRequest.put("sales", salesData); // 销售数据已经是正确的格式，不需要转换
                modelRequests.add(modelRequest);
            }

            // 2. 准备批量训练请求
            Map<String, Object> batchTrainRequest = new HashMap<>();
            batchTrainRequest.put("models", modelRequests);
            batchTrainRequest.put("max_workers", 4);

            // 3. 发送批量训练请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> trainEntity = new HttpEntity<>(batchTrainRequest, headers);
            restTemplate.postForObject(prophetApiUrl + "/api/batch-train", trainEntity, Map.class);

            // 4. 准备批量预测请求
            List<Map<String, Object>> forecastRequests = productSalesDataMap.keySet().stream()
                    .map(productId -> {
                        Map<String, Object> request = new HashMap<>();
                        request.put("model_name", "product_" + productId);
                        request.put("periods", periods);
                        request.put("freq", "D");
                        return request;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> batchForecastRequest = new HashMap<>();
            batchForecastRequest.put("forecasts", forecastRequests);
            batchForecastRequest.put("max_workers", 4);

            // 5. 发送批量预测请求
            HttpEntity<Map<String, Object>> forecastEntity = new HttpEntity<>(batchForecastRequest, headers);
            Map<String, Object> response = restTemplate.postForObject(prophetApiUrl + "/api/batch-forecast", forecastEntity, Map.class);

            // 6. 解析预测结果
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            
            // 7. 转换结果为商品ID到预测结果的映射
            Map<Integer, List<Map<String, Object>>> forecastResults = new HashMap<>();
            for (Map<String, Object> result : results) {
                String modelName = (String) result.get("model_name");
                Integer productId = Integer.parseInt(modelName.replace("product_", ""));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> forecast = (List<Map<String, Object>>) result.get("forecast");
                if (forecast != null) {
                    forecastResults.put(productId, forecast);
                }
            }

            return forecastResults;

        } catch (Exception e) {
            throw new RuntimeException("批量Prophet预测失败: " + e.getMessage(), e);
        }
    }


} 
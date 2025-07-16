package com.example.www.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/sparkai")
@CrossOrigin(origins = {
    "http://localhost:3000", "http://127.0.0.1:3000",
    "http://localhost:5173", "http://127.0.0.1:5173"
})
public class SparkAIProxyController {

    // 模型配置 - 直接使用APIPassword
    private static final Map<String, ModelConfig> MODEL_MAP = new HashMap<>();
    static {
        // X1模型 - 使用v2 API
        MODEL_MAP.put("x1", new ModelConfig(
            "https://spark-api-open.xf-yun.com/v2/chat/completions",
            "x1",
            "wGYXgZuQOlUJyJtqqqCg:PMpYwqLITpiwcZbjJxXt" // APIPassword
        ));

        // Pro模型 - 使用v1 API
        MODEL_MAP.put("pro", new ModelConfig(
            "https://spark-api-open.xf-yun.com/v1/chat/completions",
            "generalv3.5",
            "gzGHiXgjxwIenNsYbkPT:YEKfhLsfwqkNZLXsIGkj" // APIPassword
        ));

        // 其他模型 - 暂时使用Pro的配置
        MODEL_MAP.put("lite", new ModelConfig(
            "https://spark-api-open.xf-yun.com/v1/chat/completions",
            "generalv3.5",
            "EOJwZyzFvceLOzCbsSCR:uUotsLkUvqbLZGDBKSFT" // APIPassword
        ));

        MODEL_MAP.put("max", new ModelConfig(
            "https://spark-api-open.xf-yun.com/v1/chat/completions",
            "generalv3.5",
            "uxHwFeFbNkseyeQxieCg:JAzfnEuhcVgqjWfCBrEX" // APIPassword
        ));

        MODEL_MAP.put("ultra", new ModelConfig(
            "https://spark-api-open.xf-yun.com/v1/chat/completions",
            "generalv3.5",
            "bxOIfKYlLozCzDDtDRAr:SkSnxeIHKkYkLNUJCDLu" // APIPassword
        ));
    }

    @PostMapping
    public ResponseEntity<String> proxySparkAI(@RequestBody Map<String, Object> body) {
        String model = (String) body.getOrDefault("model", "x1");
        ModelConfig config = MODEL_MAP.get(model);

        if (config == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"不支持的模型: " + model + "\"}");
        }

        // 构建请求体格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.modelName);

        // 处理消息格式
        List<Map<String, Object>> originalMessages = (List<Map<String, Object>>) body.get("messages");
        if (originalMessages == null || originalMessages.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"消息不能为空\"}");
        }
        
        // 过滤掉assistant角色中包含错误信息的消息，只保留有效的对话内容
        List<Map<String, Object>> validMessages = new ArrayList<>();
        for (Map<String, Object> msg : originalMessages) {
            String role = (String) msg.get("role");
            String content = (String) msg.get("content");
            
            // 只保留用户消息和不包含错误信息的助手消息
            if ("user".equals(role) || 
                ("assistant".equals(role) && content != null && 
                 !content.contains("服务器错误") && 
                 !content.contains("error") && 
                 !content.isEmpty())) {
                validMessages.add(msg);
            }
        }
        
        // 如果过滤后没有有效消息，至少保留最后一条用户消息
        if (validMessages.isEmpty()) {
            for (int i = originalMessages.size() - 1; i >= 0; i--) {
                Map<String, Object> msg = originalMessages.get(i);
                if ("user".equals(msg.get("role"))) {
                    validMessages.add(msg);
                    break;
                }
            }
        }
        
        requestBody.put("messages", validMessages);

        // 添加可选参数 - 根据星火API文档设置
        if (config.apiUrl.contains("/v2/")) {
            // X1模型参数
            requestBody.put("temperature", 1.0); // 默认值为1.0
            requestBody.put("top_p", 0.95);      // 默认值为0.95
            requestBody.put("top_k", 6);         // 默认值为6
        } else {
            // V1模型参数
            requestBody.put("temperature", 0.5); // 默认值为1.0
            requestBody.put("top_p", 1.0);       // 默认值为1.0
            requestBody.put("top_k", 4);         // 默认值为4
        }
        
        requestBody.put("max_tokens", 4096);  // 默认值为4096
        requestBody.put("stream", false);     // 非流式响应
        
        // 添加用户标识
        requestBody.put("user", "user_" + System.currentTimeMillis());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 根据文档，两个版本的API都使用相同的认证格式：Bearer APIPassword
        headers.set("Authorization", "Bearer " + config.apiPassword);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("=== 请求信息 ===");
            System.out.println("发送请求到: " + config.apiUrl);
            System.out.println("使用模型: " + config.modelName);
            System.out.println("消息数量: " + validMessages.size());
            System.out.println("请求体: " + requestBody);
            System.out.println("认证头: Bearer " + config.apiPassword);
            System.out.println("===============");

            ResponseEntity<String> response = restTemplate.postForEntity(config.apiUrl, entity, String.class);

            System.out.println("响应状态: " + response.getStatusCode());
            System.out.println("响应体: " + response.getBody());

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            // 改进错误处理
            String errorMessage = e.getMessage();
            
            // 处理特定错误码
            if (errorMessage.contains("10003")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"ClientMsgError: 请检查请求格式和参数\"}");
            } else if (errorMessage.contains("10007")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("{\"error\":\"用户流量受限: 服务正在处理用户当前问题，请等待处理完成\"}");
            } else if (errorMessage.contains("10013")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"输入内容审核不通过，涉嫌违规，请重新调整输入内容\"}");
            } else if (errorMessage.contains("10014")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"输出内容涉及敏感信息，审核不通过\"}");
            } else if (errorMessage.contains("10907")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"token数量超过上限，对话历史+问题的字数太多，需要精简输入\"}");
            } else if (errorMessage.contains("11200") || errorMessage.contains("11201") || 
                      errorMessage.contains("11202") || errorMessage.contains("11203")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"授权错误: 请检查API密钥权限或流量限制\"}");
            }
            
            // 处理HTTP状态码
            else if (errorMessage.contains("401")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"认证失败: API密钥无效\"}");
            } else if (errorMessage.contains("403")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"权限不足: 请检查API密钥权限\"}");
            } else if (errorMessage.contains("429")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("{\"error\":\"请求过于频繁，请稍后再试\"}");
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"请求失败: " + errorMessage + "\"}");
        }
    }

    // 更新模型配置类
    static class ModelConfig {
        String apiUrl;       // API端点
        String modelName;    // 官方模型名称
        String apiPassword;  // API密钥 (格式为 apiKey:apiSecret)
        
        ModelConfig(String apiUrl, String modelName, String apiPassword) {
            this.apiUrl = apiUrl;
            this.modelName = modelName;
            this.apiPassword = apiPassword;
        }
    }
} 
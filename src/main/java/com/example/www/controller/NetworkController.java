package com.example.www.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173", "http://127.0.0.1:5173"})
public class NetworkController {

    // 1. 通用HTTP代理接口
    @GetMapping("/proxy")
    public ResponseEntity<?> proxy(@RequestParam String url, @RequestParam(defaultValue = "GET") String method) {
        Instant start = Instant.now();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.connect();
            int status = conn.getResponseCode();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            Map<String, String> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                if (entry.getKey() != null && !entry.getValue().isEmpty()) {
                    headers.put(entry.getKey(), String.join(",", entry.getValue()));
                }
            }
            InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
            byte[] body = is != null ? is.readAllBytes() : new byte[0];
            Instant end = Instant.now();
            Map<String, Object> result = new HashMap<>();
            result.put("status", status);
            result.put("headers", headers);
            result.put("duration", Duration.between(start, end).toMillis());
            // 尝试以文本返回，否则base64
            String contentType = conn.getContentType();
            if (contentType != null && contentType.startsWith("text")) {
                result.put("body", new String(body, StandardCharsets.UTF_8));
            } else {
                result.put("body_base64", Base64.getEncoder().encodeToString(body));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 带宽测速接口 - 下载
    @GetMapping("/speedtest/download")
    public void download(HttpServletResponse response, @RequestParam(defaultValue = "10485760") int size) throws IOException {
        // 默认10MB
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=download.bin");
        byte[] buffer = new byte[1024 * 1024]; // 1MB
        Random random = new Random();
        int written = 0;
        OutputStream os = response.getOutputStream();
        while (written < size) {
            random.nextBytes(buffer);
            int toWrite = Math.min(buffer.length, size - written);
            os.write(buffer, 0, toWrite);
            written += toWrite;
        }
        os.flush();
    }

    // 2. 带宽测速接口 - 上传
    @PostMapping("/speedtest/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Instant start = Instant.now();
        long size = file.getSize();
        // 读取全部内容
        file.getBytes();
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        return Map.of("size", size, "duration", duration);
    }

    // 3. 公网IP接口
    @GetMapping("/public-ip")
    public Map<String, Object> publicIp() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String ip = restTemplate.getForObject("https://api.ipify.org", String.class);
            return Map.of("ip", ip);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // 4. DNS信息接口
    @GetMapping("/dns-info")
    public Map<String, Object> dnsInfo(@RequestParam String domain) {
        Instant start = Instant.now();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            Instant end = Instant.now();
            List<String> ips = new ArrayList<>();
            for (InetAddress addr : addresses) {
                ips.add(addr.getHostAddress());
            }
            // 获取本机DNS服务器（仅限部分系统，Java无直接API）
            List<String> dnsServers = getLocalDnsServers();
            return Map.of(
                "ips", ips,
                "duration", Duration.between(start, end).toMillis(),
                "dnsServers", dnsServers
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // 5. 端口连通性检测接口
    @GetMapping("/port-test")
    public Map<String, Object> portTest(@RequestParam String host, @RequestParam int port) {
        Instant start = Instant.now();
        boolean reachable = false;
        String error = null;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            reachable = true;
        } catch (Exception e) {
            error = e.getMessage();
        }
        Instant end = Instant.now();
        Map<String, Object> result = new HashMap<>();
        result.put("host", host);
        result.put("port", port);
        result.put("reachable", reachable);
        result.put("duration", Duration.between(start, end).toMillis());
        if (error != null) result.put("error", error);
        return result;
    }

    // 辅助：获取本机DNS服务器（仅支持部分系统）
    private List<String> getLocalDnsServers() {
        List<String> servers = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Process p = Runtime.getRuntime().exec("ipconfig /all");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("DNS Servers") || line.trim().startsWith("DNS 服务器")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) servers.add(parts[1].trim());
                        while ((line = reader.readLine()) != null && line.startsWith("   ")) {
                            servers.add(line.trim());
                        }
                    }
                }
            } else {
                Process p = Runtime.getRuntime().exec("cat /etc/resolv.conf");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("nameserver")) {
                        servers.add(line.split(" ")[1]);
                    }
                }
            }
        } catch (Exception ignored) {}
        return servers;
    }
} 
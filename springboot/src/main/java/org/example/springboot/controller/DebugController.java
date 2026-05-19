package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.entity.House;
import org.example.springboot.mapper.HouseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "调试接口")
@RestController
public class DebugController {

    @Resource
    private HouseMapper houseMapper;

    @GetMapping("/api/debug/house-count")
    public Map<String, Object> getHouseCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            long count = houseMapper.selectCount(null);
            result.put("code", "200");
            result.put("msg", "成功");
            result.put("data", count);
        } catch (Exception e) {
            result.put("code", "500");
            result.put("msg", "错误: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/api/debug/simple-house")
    public Map<String, Object> getSimpleHouse() {
        Map<String, Object> result = new HashMap<>();
        try {
            House house = houseMapper.selectById(1L);
            if (house == null) {
                result.put("code", "404");
                result.put("msg", "房屋不存在");
                result.put("data", null);
            } else {
                // 手动构建简单的响应，避免序列化问题
                Map<String, Object> houseData = new HashMap<>();
                houseData.put("id", house.getId());
                houseData.put("title", house.getTitle());
                houseData.put("price", house.getPrice());
                houseData.put("address", house.getAddress());

                result.put("code", "200");
                result.put("msg", "成功");
                result.put("data", houseData);
            }
        } catch (Exception e) {
            result.put("code", "500");
            result.put("msg", "错误: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
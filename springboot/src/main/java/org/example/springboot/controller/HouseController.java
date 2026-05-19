package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.House;
import org.example.springboot.entity.User;
import org.example.springboot.service.HouseService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;

@Tag(name="房屋管理接口")
@RestController
@RequestMapping("/houses")
public class HouseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HouseController.class);
    
    @Resource
    private HouseService houseService;
    
    @Operation(summary = "分页查询房屋信息")
    @GetMapping("/page")
    public Result<?> getHousesByPage(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long landLordId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        if (status == null) {
            status = 1; // 默认查询待出租状态
        }
        Page<House> page = houseService.getHousesByPage(title,landLordId, minPrice, maxPrice, typeId, status, currentPage, size);

        // 转换为简化的数据结构避免序列化问题
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords().stream().map(this::convertToSimpleMap).collect(java.util.stream.Collectors.toList()));
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());

        return Result.success(result);
    }
    
    @Operation(summary = "房东获取自己的房屋列表")
    @GetMapping("/landlord")
    public Result<?> getLandlordHouses(
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        // 检查当前用户是否登录以及是否是房东
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("获取当前用户信息失败，请重新登录");
        }

        Page<House> page = houseService.getLandlordHouses(title, status, currentPage, size);

        // 转换为简化的数据结构避免序列化问题
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords().stream().map(this::convertToSimpleMap).collect(java.util.stream.Collectors.toList()));
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());

        return Result.success(result);
    }
    
    @Operation(summary = "根据ID获取房屋详情")
    @GetMapping("/{id}")
    public Result<?> getHouseById(@PathVariable Long id) {
        House house = houseService.getHouseById(id);
        return Result.success(house);
    }
    
    @Operation(summary = "发布新房屋")
    @PostMapping
    public Result<?> createHouse(@RequestBody House house) {
        // 检查当前用户是否登录以及角色权限
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("获取当前用户信息失败，请重新登录");
        }
        
        // 检查用户是否为房东或管理员
        if (!"LANDLORD".equals(currentUser.getRoleCode()) && !"ADMIN".equals(currentUser.getRoleCode())) {
            return Result.error("只有房东或管理员可以发布房屋");
        }
        
        houseService.createHouse(house);
        return Result.success("发布成功");
    }
    
    @Operation(summary = "更新房屋信息")
    @PutMapping("/{id}")
    public Result<?> updateHouse(@PathVariable Long id, @RequestBody House house) {
        // 检查当前用户是否登录
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("获取当前用户信息失败，请重新登录");
        }
        
        houseService.updateHouse(id, house);
        return Result.success("更新成功");
    }
    
    @Operation(summary = "修改房屋状态")
    @PutMapping("/{id}/status")
    public Result<?> updateHouseStatus(
            @PathVariable Long id, 
            @RequestParam Integer status) {
        // 检查当前用户是否登录
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("获取当前用户信息失败，请重新登录");
        }
        
        houseService.updateHouseStatus(id, status);
        return Result.success("状态更新成功");
    }
    
    @Operation(summary = "删除房屋")
    @DeleteMapping("/{id}")
    public Result<?> deleteHouse(@PathVariable Long id) {
        // 检查当前用户是否登录
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("获取当前用户信息失败，请重新登录");
        }
        
        houseService.deleteHouse(id);
        return Result.success("删除成功");
    }

    /**
     * 将House对象转换为简化的Map结构，避免JSON序列化问题
     */
    private Map<String, Object> convertToSimpleMap(House house) {
        Map<String, Object> map = new HashMap<>();
        if (house != null) {
            map.put("id", house.getId());
            map.put("title", house.getTitle());
            map.put("description", house.getDescription());
            map.put("area", house.getArea());
            map.put("price", house.getPrice());
            map.put("address", house.getAddress());
            map.put("typeId", house.getTypeId());
            map.put("landlordId", house.getLandlordId());
            map.put("status", house.getStatus());
            map.put("images", house.getImages());
            map.put("facilities", house.getFacilities());
            map.put("createTime", house.getCreateTime());
            map.put("updateTime", house.getUpdateTime());
            map.put("typeName", house.getTypeName());
            map.put("landlordName", house.getLandlordName());
            map.put("landlordImg", house.getLandlordImg());
        }
        return map;
    }
} 
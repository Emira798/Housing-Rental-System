<template>
  <div class="house-list-container">
    <div class="search-banner">
      <div class="banner-content">
        <h1>寻找理想住所，开启美好生活</h1>
        <div class="search-box">
          <el-input
            v-model="searchForm.title"
            placeholder="搜索房屋关键词"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div class="filter-container">
      <el-card shadow="hover">
        <div class="filter-section">
          <div class="filter-item">
            <span class="filter-label">房屋类型：</span>
            <div class="filter-options">
              <el-radio-group v-model="searchForm.typeId" @change="handleSearch">
                <el-radio :label="undefined" >全部</el-radio>
                <el-radio v-for="item in houseTypeOptions" :key="item.id" :label="item.id">{{ item.name }}</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="filter-item">
            <span class="filter-label">价格区间：</span>
            <div class="filter-options">
              <el-select 
                :model-value="selectedPriceOption" 
                placeholder="选择价格区间" 
                @change="selectPriceOption"
              >
                <el-option label="全部" value="" />
                <el-option label="1000元以下" :value="0" />
                <el-option label="1000-2000元" :value="1" />
                <el-option label="2000-3000元" :value="2" />
                <el-option label="3000-5000元" :value="3" />
                <el-option label="5000元以上" :value="4" />
                <el-option label="自定义" :value="5" disabled />
              </el-select>
              <div class="price-input">
                <el-input-number
                  v-model="minPriceTemp"
                  :min="0"
                  :precision="0"
                  :step="100"
                  placeholder="最低价"
                  @change="handleMinPriceChange"
                />
                <span class="range-separator">-</span>
                <el-input-number
                  v-model="maxPriceTemp"
                  :min="0"
                  :precision="0"
                  :step="100"
                  placeholder="最高价"
                  @change="handleMaxPriceChange"
                />
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <el-button type="primary" @click="handleSearch">筛选</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div class="house-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
        <el-skeleton :rows="5" animated />
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else>
        <el-empty v-if="houseList.length === 0" description="暂无符合条件的房源" />
        <div v-else class="house-grid">
          <el-card
            v-for="house in houseList"
            :key="house.id"
            class="house-card"
            shadow="hover"
            @click="goToDetail(house.id)"
          >
            <div class="house-image">
              <el-image
                :src="getHouseImage(house)"
                fit="cover"
                :preview-src-list="formatImageList(house.images)"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="house-price">¥{{ house.price }} / 月</div>
            </div>
            <div class="house-info">
              <h3 class="house-title">{{ house.title }}</h3>
              <p class="house-address">{{ house.address }}</p>
              <div class="house-tags">
                <el-tag size="small" type="info">{{ house.area }}㎡</el-tag>
                <el-tag size="small" type="success">{{ house.typeName }}</el-tag>
                <el-tag 
                  v-for="(value, key) in parseFacilities(house.facilities)" 
                  :key="key"
                  v-show="value && house.facilities"
                  size="small" 
                  type="primary"
                >
                  {{ getFacilityLabel(key) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <div class="pagination-container">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由实例
const router = useRouter()

// 基础API路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 数据列表
const houseList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const houseTypeOptions = ref([])

// 价格区间处理
const minPriceTemp = ref(null)
const maxPriceTemp = ref(null)
const priceRangeOptions = [
  null,
  [0, 1000],
  [1000, 2000],
  [2000, 3000],
  [3000, 5000],
  [5000, null]
]

// 计算当前选择的价格选项
const selectedPriceOption = computed(() => {
  if (searchForm.minPrice === null && searchForm.maxPrice === null) {
    return ''  // 全部选项
  }
  
  for (let i = 0; i < priceRangeOptions.length; i++) {
    const range = priceRangeOptions[i]
    if (range && 
        searchForm.minPrice === range[0] && 
        searchForm.maxPrice === range[1]) {
      return i
    }
  }
  
  // 如果不匹配任何预定义范围，但有自定义价格
  if (searchForm.minPrice !== null || searchForm.maxPrice !== null) {
    return 5 // 自定义选项
  }
  
  return ''
})

// 选择价格选项
const selectPriceOption = (index) => {
  if (index === '') {
    // 选择"全部"选项
    searchForm.minPrice = null
    searchForm.maxPrice = null
    minPriceTemp.value = null
    maxPriceTemp.value = null
  } else {
    // 选择预定义价格范围
    const range = priceRangeOptions[index]
    if (range) {
      searchForm.minPrice = range[0]
      searchForm.maxPrice = range[1]
      minPriceTemp.value = range[0]
      maxPriceTemp.value = range[1]
    }
  }
  // 触发搜索
  handleSearch()
}

// 处理最小价格变化
const handleMinPriceChange = (val) => {
  if (val !== searchForm.minPrice) {
    searchForm.minPrice = val
    // 如果最小价格大于最大价格，自动调整最大价格
    if (searchForm.maxPrice !== null && val > searchForm.maxPrice) {
      searchForm.maxPrice = val
      maxPriceTemp.value = val
    }
    setTimeout(handleSearch, 300)
  }
}

// 处理最大价格变化
const handleMaxPriceChange = (val) => {
  if (val !== searchForm.maxPrice) {
    searchForm.maxPrice = val
    // 如果最大价格小于最小价格，自动调整最小价格
    if (searchForm.minPrice !== null && val < searchForm.minPrice) {
      searchForm.minPrice = val
      minPriceTemp.value = val
    }
    setTimeout(handleSearch, 300)
  }
}

// 搜索表单
const searchForm = reactive({
  title: '',
  minPrice: null,
  maxPrice: null,
  typeId: undefined,
  status: 1 // 默认只查询待出租状态的房屋
})

// 初始化
onMounted(() => {
  // 初始化临时价格字段
  minPriceTemp.value = searchForm.minPrice
  maxPriceTemp.value = searchForm.maxPrice
  
  // 获取房屋类型选项
  fetchHouseTypeList()
  // 获取房屋列表
  fetchHouseList()
})

// 获取房屋类型列表
const fetchHouseTypeList = async () => {
  try {
    await request.get('/house-types', {}, {
      onSuccess: (res) => {
        houseTypeOptions.value = res||[]
      }
    })
  } catch (error) {
    console.error('获取房屋类型列表失败:', error)
  }
}

// 获取房屋列表
const fetchHouseList = async () => {
  loading.value = true
  try {
    const params = {
      title: searchForm.title,
      minPrice: searchForm.minPrice,
      maxPrice: searchForm.maxPrice,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    // 只有当typeId有值时才添加到请求参数
    if (searchForm.typeId !== undefined && searchForm.typeId !== null) {
      params.typeId = searchForm.typeId
    }
    
    await request.get('/houses/page', params, {
      onSuccess: (res) => {
        houseList.value = res.records || []
        total.value = res.total || 0
        // 确保当前页码正确
        if (res.current) {
          currentPage.value = res.current
        }
      }
    })
  } catch (error) {
    console.error('获取房屋列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 设置为第一页并获取数据
  if (currentPage.value !== 1) {
    currentPage.value = 1
  } else {
    // 直接获取数据
    fetchHouseList()
  }
}

// 重置搜索
const resetSearch = () => {
  // 重置所有筛选条件
  searchForm.title = ''
  searchForm.minPrice = null
  searchForm.maxPrice = null
  searchForm.typeId = undefined
  
  // 重置价格临时字段
  minPriceTemp.value = null
  maxPriceTemp.value = null
  
  if (currentPage.value !== 1) {
    currentPage.value = 1
  } else {
    // 直接获取数据
    fetchHouseList()
  }
}

// 每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 切换每页显示数量时重置为第一页
  fetchHouseList()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchHouseList()
}

// 获取房屋图片
const getHouseImage = (house) => {
  if (!house.images) return ''
  try {
    const imageArray = JSON.parse(house.images)
    if (imageArray.length === 0) return ''
    const imagePath = imageArray[0]
    return formatImageUrl(imagePath)
  } catch (e) {
    return ''
  }
}

// 格式化图片URL
const formatImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return baseAPI + path
}

// 格式化图片列表
const formatImageList = (images) => {
  if (!images) return []
  try {
    const imageArray = JSON.parse(images)
    return imageArray.map(img => formatImageUrl(img))
  } catch (e) {
    return []
  }
}

// 解析房屋配套设施
const parseFacilities = (facilities) => {
  if (!facilities) return {}
  try {
    return JSON.parse(facilities)
  } catch (e) {
    return {}
  }
}

// 获取设施标签
const getFacilityLabel = (facilityKey) => {
  const facilityLabels = {
    wifi: 'WiFi',
    elevator: '电梯',
    airConditioner: '空调',
    washer: '洗衣机',
    refrigerator: '冰箱',
    television: '电视',
    waterHeater: '热水器'
  }
  return facilityLabels[facilityKey] || facilityKey
}

// 跳转到房屋详情页
const goToDetail = (id) => {
  router.push(`/houses/${id}`)
}
</script>

<style lang="scss" scoped>
.house-list-container {
  min-height: calc(100vh - 64px); // 调整高度以适应导航栏高度
  height: auto;
  overflow-y: auto;
  position: relative;
  
  .search-banner {
    height: 300px;
    background-image: linear-gradient(120deg, #3B82F6, #1D4ED8);
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    
    .banner-content {
      text-align: center;
      width: 80%;
      
      h1 {
        font-size: 32px;
        margin-bottom: 30px;
        font-weight: 700;
      }
      
      .search-box {
        width: 600px;
        max-width: 100%;
        margin: 0 auto;
        
        :deep(.el-input__inner) {
          height: 50px;
          font-size: 16px;
        }
        
        :deep(.el-input-group__append) {
          .el-button {
            height: 50px;
            padding: 0 20px;
            font-size: 16px;
          }
        }
      }
    }
  }
  
  .filter-container {
    margin-top: -30px;
    padding: 0 20px;
    
    .filter-section {
      .filter-item {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .filter-label {
          width: 100px;
          flex-shrink: 0;
          color: #64748b;
          font-weight: 600;
        }
        
        .filter-options {
          flex: 1;
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          
          .price-input {
            display: flex;
            align-items: center;
            margin-left: 20px;
            
            .range-separator {
              margin: 0 10px;
            }
          }
        }
      }
      
      .filter-actions {
        display: flex;
        justify-content: flex-end;
        margin-top: 20px;
      }
    }
  }
  
  .house-content {
    padding: 20px;
    margin-top: 20px;
    
    .loading-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }
    
    .house-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      
      .house-card {
        cursor: pointer;
        transition: transform 0.3s;
        
        &:hover {
          transform: translateY(-5px);
        }
        
        .house-image {
          position: relative;
          height: 200px;
          overflow: hidden;
          border-radius: 4px;
          
          :deep(.el-image) {
            height: 100%;
            width: 100%;
          }
          
          .image-placeholder {
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: #f5f7fa;
            font-size: 40px;
            color: #c0c4cc;
          }
          
          .house-price {
            position: absolute;
            bottom: 0;
            right: 0;
            background-color: rgba(59, 130, 246, 0.9);
            color: white;
            padding: 5px 10px;
            font-weight: bold;
            border-top-left-radius: 4px;
          }
        }
        
        .house-info {
          padding: 15px 0;
          
          .house-title {
            margin: 0 0 10px;
            font-size: 18px;
            font-weight: 600;
            color: #1d4ed8;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .house-address {
            margin: 0 0 10px;
            color: #64748b;
            font-size: 14px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .house-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 5px;
          }
        }
      }
    }
    
    .pagination-container {
      margin-top: 30px;
      display: flex;
      justify-content: center;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .house-list-container {
    .search-banner {
      height: 200px;
      
      .banner-content {
        width: 90%;
        
        h1 {
          font-size: 24px;
          margin-bottom: 20px;
        }
        
        .search-box {
          width: 100%;
        }
      }
    }
    
    .filter-container {
      margin-top: -20px;
      
      .filter-section {
        .filter-item {
          flex-direction: column;
          align-items: flex-start;
          
          .filter-label {
            width: 100%;
            margin-bottom: 10px;
          }
          
          .filter-options {
            width: 100%;
            
            .price-input {
              margin-left: 0;
              margin-top: 10px;
              width: 100%;
            }
          }
        }
      }
    }
  }
}
</style> 
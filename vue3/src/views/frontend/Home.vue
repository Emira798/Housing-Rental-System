<template>
  <div class="home-container">
    <!-- 顶部轮播图 -->
    <div class="hero-section">
      <carousel-banner />
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <h2>寻找您的理想住所</h2>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="输入关键词搜索房源"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="quick-tags">
          <span>热门搜索：</span>
          <el-tag 
            v-for="tag in quickSearchTags" 
            :key="tag"
            @click="quickSearch(tag)"
            class="quick-tag"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 公告区域 -->
    <div class="announcement-section">
      <div class="section-header center">
        <h2>最新公告</h2>
        <p>了解平台最新动态和活动信息</p>
      </div>
      <div class="announcement-container">
        <el-row :gutter="20">
          <el-col v-if="loadingAnnouncements" :span="24" class="text-center">
            <el-skeleton :rows="3" animated />
          </el-col>
          <template v-else>
            <el-col v-if="announcements.length === 0" :span="24" class="text-center">
              <el-empty description="暂无公告" />
            </el-col>
            <el-col v-for="item in announcements" :key="item.id" :xs="24" :sm="24" :md="8">
              <el-card class="announcement-card" shadow="hover" @click="viewAnnouncementDetail(item)">
                <div class="announcement-card-content">
                  <h3 class="announcement-title">{{ item.title }}</h3>
                  <p class="announcement-excerpt">{{ getExcerpt(item.content) }}</p>
                  <div class="announcement-footer">
                    <span class="announcement-date">{{ formatDate(item.createTime) }}</span>
                    <el-button type="text" class="read-more">查看详情 <el-icon><ArrowRight /></el-icon></el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </template>
        </el-row>
        <div class="view-all-btn">
          <el-button type="primary" @click="navigateTo('/announcements')">查看全部公告</el-button>
        </div>
      </div>
    </div>

    <!-- 热门房源区域 -->
    <!-- <div class="featured-section">
      <div class="section-header">
        <h2>热门房源推荐</h2>
        <el-link :underline="false" @click="navigateTo('/houses')">查看更多 <el-icon><ArrowRight /></el-icon></el-link>
      </div>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
        <el-skeleton :rows="5" animated />
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else>
        <el-row :gutter="20" class="featured-houses">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="house in featuredHouses" :key="house.id">
            <el-card class="house-card" shadow="hover" @click="viewHouseDetail(house.id)">
              <div class="house-image">
                <el-image :src="getHouseImage(house)" fit="cover">
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
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div> -->

    <!-- 系统特色介绍 -->
    <div class="features-section">
      <div class="section-header center">
        <h2>我们的优势</h2>
        <p>为您提供高品质的房屋租赁服务</p>
      </div>
      
      <el-row :gutter="30" class="feature-list">
        <el-col :xs="24" :sm="12" :md="8" v-for="(feature, index) in systemFeatures" :key="index">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><component :is="feature.icon" /></el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 注册引导区 -->
    <div class="cta-section" v-if="!isLoggedIn">
      <div class="cta-content">
        <h2>立即注册，开始寻找您的理想住所</h2>
        <p>注册成为会员，享受更多专属服务与优惠</p>
        <div class="cta-buttons">
          <el-button type="primary" size="large" @click="navigateTo('/register')">立即注册</el-button>
          <el-button size="large" @click="navigateTo('/login')">已有账号？立即登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'
import CarouselBanner from '@/components/CarouselBanner.vue'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 基础API路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 搜索相关
const searchKeyword = ref('')
const quickSearchTags = ['两室一厅', '近地铁', '精装修', '拎包入住', '学区房']

// 热门房源
const featuredHouses = ref([])
const loading = ref(false)

// 系统特色
const systemFeatures = [
  {
    icon: 'HomeFilled',
    title: '丰富优质房源',
    description: '精选各类房源，满足不同需求，为您提供多样化的居住选择'
  },
  {
    icon: 'Connection',
    title: '便捷预约看房',
    description: '在线预约看房，高效便捷，节省您的宝贵时间'
  },
  {
    icon: 'Lock',
    title: '安全可靠保障',
    description: '房东实名认证，房源真实可靠，保障您的租房安全'
  },
  {
    icon: 'Document',
    title: '规范租赁合同',
    description: '标准化合同模板，明确双方权责，避免不必要的纠纷'
  },
  {
    icon: 'UserFilled',
    title: '专业客服团队',
    description: '专业的客服团队，随时为您解答疑问，提供贴心服务'
  },
  {
    icon: 'TrendCharts',
    title: '透明价格体系',
    description: '房源价格公开透明，无隐藏费用，租房更放心'
  }
]

// 公告相关
const announcements = ref([])
const loadingAnnouncements = ref(false)

// 初始化
onMounted(() => {
  // fetchFeaturedHouses()
  fetchLatestAnnouncements()
})

// 获取热门房源
const fetchFeaturedHouses = async () => {
  loading.value = true
  try {
    await request.get('/houses/featured', {
      limit: 8
    }, {
      onSuccess: (res) => {
        featuredHouses.value = res||[]
      }
    })
  } catch (error) {
    console.error('获取热门房源失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取房屋图片
const getHouseImage = (house) => {
  if (!house.images) return '/default-house.jpg'
  try {
    const imageArray = JSON.parse(house.images)
    if (imageArray.length === 0) return '/default-house.jpg'
    const imagePath = imageArray[0]
    return formatImageUrl(imagePath)
  } catch (e) {
    return '/default-house.jpg'
  }
}

// 格式化图片URL
const formatImageUrl = (path) => {
  if (!path) return '/default-house.jpg'
  if (path.startsWith('http')) return path
  return baseAPI + path
}

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({
    path: '/houses',
    query: {
      title: searchKeyword.value.trim()
    }
  })
}

// 快速搜索
const quickSearch = (tag) => {
  router.push({
    path: '/houses',
    query: {
      title: tag
    }
  })
}

// 路由跳转
const navigateTo = (path) => {
  router.push(path)
}

// 查看房屋详情
const viewHouseDetail = (id) => {
  router.push(`/houses/${id}`)
}

// 获取最新公告
const fetchLatestAnnouncements = async () => {
  loadingAnnouncements.value = true
  try {
    await request.get('/announcements', {
      status: 1, // 只获取已发布的公告
      currentPage: 1,
      size: 3
    }, {
      onSuccess: (res) => {
        announcements.value = res.records||[]
      }
    })
  } catch (error) {
    console.error('获取公告失败:', error)
  } finally {
    loadingAnnouncements.value = false
  }
}

// 查看公告详情
const viewAnnouncementDetail = (announcement) => {
  router.push(`/announcements/${announcement.id}`)
}

// 获取内容摘要
const getExcerpt = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return DateUtils.formatDate(date)
}
</script>

<style lang="scss" scoped>
.home-container {
  .hero-section {
    margin-bottom: 40px;
  }
  
  .search-section {
    margin-bottom: 60px;
    
    .search-container {
      max-width: 800px;
      margin: 0 auto;
      text-align: center;
      
      h2 {
        color: #1d4ed8;
        margin-bottom: 20px;
        font-size: 28px;
        font-weight: 600;
      }
      
      .search-box {
        margin-bottom: 15px;
        
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
      
      .quick-tags {
        color: #64748b;
        
        .quick-tag {
          margin: 0 5px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            transform: translateY(-2px);
          }
        }
      }
    }
  }
  
  .featured-section {
    margin-bottom: 60px;
    padding: 0 20px;
    
    .loading-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
    }
    
    .featured-houses {
      .house-card {
        height: 100%;
        margin-bottom: 20px;
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
          
          .el-image {
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
            font-size: 16px;
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
  }
  
  .features-section {
    margin-bottom: 60px;
    padding: 40px 20px;
    background-color: #f8fafc;
    
    .feature-list {
      margin-top: 40px;
      
      .feature-item {
        text-align: center;
        padding: 20px;
        margin-bottom: 20px;
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
        height: 100%;
        transition: transform 0.3s;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
        }
        
        .feature-icon {
          margin-bottom: 20px;
          font-size: 40px;
          color: #3b82f6;
          
          .el-icon {
            width: 1em;
            height: 1em;
          }
        }
        
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: #1d4ed8;
          margin-bottom: 10px;
        }
        
        p {
          color: #64748b;
          line-height: 1.6;
        }
      }
    }
  }
  
  .cta-section {
    background-image: linear-gradient(120deg, #3B82F6, #1D4ED8);
    color: white;
    text-align: center;
    padding: 60px 20px;
    margin-bottom: 60px;
    border-radius: 8px;
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
    
    .cta-content {
      max-width: 700px;
      margin: 0 auto;
      
      h2 {
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 15px;
      }
      
      p {
        font-size: 16px;
        margin-bottom: 30px;
        opacity: 0.9;
      }
      
      .cta-buttons {
        display: flex;
        justify-content: center;
        gap: 15px;
        flex-wrap: wrap;
      }
    }
  }

  .announcement-section {
    padding: 60px 0;
    background-color: #f8fafc;
  }

  .announcement-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .announcement-card {
    height: 100%;
    cursor: pointer;
    transition: transform 0.3s;
    margin-bottom: 20px;
  }

  .announcement-card:hover {
    transform: translateY(-5px);
  }

  .announcement-card-content {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .announcement-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 10px;
    color: #1d4ed8;
  }

  .announcement-excerpt {
    flex: 1;
    color: #4b5563;
    line-height: 1.6;
    margin-bottom: 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }

  .announcement-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
  }

  .announcement-date {
    color: #6b7280;
    font-size: 14px;
  }

  .read-more {
    display: flex;
    align-items: center;
  }

  .view-all-btn {
    text-align: center;
    margin-top: 30px;
  }

  .text-center {
    text-align: center;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #1d4ed8;
    margin: 0;
  }
  
  p {
    color: #64748b;
    margin: 10px 0 0;
  }
  
  &.center {
    flex-direction: column;
    text-align: center;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .home-container {
    .search-section {
      .search-container {
        h2 {
          font-size: 24px;
        }
        
        .quick-tags {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          
          span {
            width: 100%;
            margin-bottom: 5px;
          }
          
          .quick-tag {
            margin-bottom: 5px;
          }
        }
      }
    }
    
    .features-section {
      .feature-list {
        .feature-item {
          .feature-icon {
            font-size: 32px;
          }
        }
      }
    }
    
    .cta-section {
      .cta-content {
        h2 {
          font-size: 24px;
        }
      }
    }
  }
}
</style>



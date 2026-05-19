<template>
  <div class="house-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="20" animated />
    </div>
    <div v-else-if="!house" class="not-found">
      <el-empty description="未找到该房源信息" />
      <div class="back-btn">
        <el-button type="primary" @click="goBack">返回房源列表</el-button>
      </div>
    </div>
    <template v-else>
      <!-- 顶部标题区域 -->
      <div class="detail-header">
        <h1 class="house-title">{{ house.title }}</h1>
        <p class="house-address">
          <el-icon><Location /></el-icon>
          {{ house.address }}
        </p>
        <div class="house-price-tag">¥{{ house.price }} / 月</div>
      </div>
      
      <!-- 主内容布局 -->
      <div class="detail-main">
        <el-row :gutter="20">
          <!-- 左侧：图片 + 信息 -->
          <el-col :xs="24" :md="16">
            <!-- 房屋图片轮播 -->
            <div class="house-carousel">
              <el-carousel v-if="houseImages.length > 0" :interval="4000" height="350px" arrow="always" indicator-position="outside">
                <el-carousel-item v-for="(image, index) in houseImages" :key="index">
                  <el-image 
                    :src="image" 
                    fit="cover"
                    :preview-src-list="houseImages"
                    :initial-index="index"
                       :preview-teleported="true"
                    class="carousel-image"
                  />
                </el-carousel-item>
              </el-carousel>
              <div v-if="houseImages.length === 0" class="no-images">
                <el-icon><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </div>
            
            <!-- 房屋信息卡片 -->
            <el-card class="house-info-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <h2>房屋信息</h2>
                </div>
              </template>
              
              <!-- 基本信息部分 -->
              <div class="info-section">
                <div class="section-title">基本信息</div>
                <el-row :gutter="20" class="house-basic-info">
                  <el-col :xs="12" :sm="8" :md="8">
                    <div class="info-item">
                      <div class="info-label">户型</div>
                      <div class="info-value">{{ house.typeName }}</div>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="8">
                    <div class="info-item">
                      <div class="info-label">面积</div>
                      <div class="info-value">{{ house.area }} 平方米</div>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="8">
                    <div class="info-item">
                      <div class="info-label">状态</div>
                      <div class="info-value">
                        <el-tag v-if="house.status === 1" type="success">可租赁</el-tag>
                        <el-tag v-else-if="house.status === 2" type="warning">已出租</el-tag>
                        <el-tag v-else type="info">未上架</el-tag>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
              
              <!-- 房屋详情部分 -->
              <div class="info-section">
                <div class="section-title">房屋详情</div>
                <div class="house-description">
                  <p v-if="house.description">{{ house.description }}</p>
                  <el-empty v-else description="暂无详细描述" />
                </div>
              </div>
              
              <!-- 配套设施部分 -->
              <div class="info-section">
                <div class="section-title">配套设施</div>
                <div class="facilities-list">
                  <el-row :gutter="20">
                    <el-col :xs="12" :sm="8" :md="8" v-for="(value, key) in houseFacilities" :key="key">
                      <div class="facility-item" :class="{ 'available': value }">
                        <el-icon v-if="value"><Check /></el-icon>
                        <el-icon v-else><Close /></el-icon>
                        <span>{{ getFacilityLabel(key) }}</span>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <!-- 右侧：联系房东卡片 -->
          <el-col :xs="24" :md="8">
            <el-card class="contact-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <h2>联系房东</h2>
                </div>
              </template>
              <div class="contact-info">
                <el-avatar :size="80" :src="getLandlordAvatar()" />
                <h3 class="landlord-name">{{ house.landlordName || '未知房东' }}</h3>
                <div class="divider"></div>
                <div class="contact-buttons">
                  <el-button 
                    type="primary" 
                    icon="Phone" 
                    v-if="house.landlordPhone"
                    @click="showContactDialog = true"
                    size="large"
                  >
                    显示联系电话
                  </el-button>

                  <el-button 
                    type="success" 
                    icon="House" 
                    v-if="house.status === 1 && isLoggedIn && isTenant"
                    @click="handleRent"
                    size="large"
                    :disabled="isOwnedByCurrentUser"
                  >
                    立即租赁
                  </el-button>
                </div>
                <div class="notice-text">
                  <p>为确保您的租房安全：</p>
                  <ul>
                    <li>看房前请通过平台联系房东</li>
                    <li>切勿支付可疑定金或押金</li>
                    <li>签订合同前请仔细核对信息</li>
                  </ul>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>
    
    <!-- 联系电话对话框 -->
    <el-dialog
      v-model="showContactDialog"
      title="联系方式"
      width="300px"
      center
    >
      <div class="contact-dialog-content">
        <el-alert
          title="温馨提示"
          type="warning"
          description="为保护您的权益，请通过平台联系房东，切勿轻信特殊渠道收费等信息。"
          show-icon
          :closable="false"
        />
        <div class="phone-number" v-if="house">
          <el-icon><Phone /></el-icon>
          <span>{{ house.landlordPhone }}</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showContactDialog = false" type="primary">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 创建订单对话框 -->
    <el-dialog
      v-model="rentDialogVisible"
      title="确认租赁信息"
      width="500px"
    >
      <div class="rent-dialog-content">
        <el-alert
          title="租赁提醒"
          type="info"
          description="签订租赁合同前，建议您实地看房并核实房屋信息。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />
        
        <el-form :model="orderForm" label-width="100px">
          <el-form-item label="房屋">
            <span>{{ house.title }}</span>
          </el-form-item>
          <el-form-item label="月租金">
            <span class="price">¥ {{ house.price }}</span>
          </el-form-item>
          <el-form-item label="押金">
            <span class="price">¥ {{ house.price }}</span>
            <span class="hint">(默认一个月租金)</span>
          </el-form-item>
          <el-form-item label="租赁周期">
            <el-select v-model="orderForm.rentCycle" placeholder="请选择">
              <el-option label="月付" :value="1" />
              <el-option label="季付" :value="3" />
              <el-option label="半年付" :value="6" />
              <el-option label="年付" :value="12" />
            </el-select>
          </el-form-item>
          <el-form-item label="总金额">
            <span class="total-price">¥ {{ totalAmount }}</span>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createOrder" :loading="creating">提交订单</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Picture, Check, Close, Phone, House } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const houseId = route.params.id

// 用户登录状态和角色判断
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isTenant = computed(() => userStore.userInfo?.roleCode === 'TENANT')
const isOwnedByCurrentUser = computed(() => {
  return house.value && userStore.userInfo && house.value.landlordId === userStore.userInfo.id
})

// 基础API路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 房屋数据
const house = ref(null)
const loading = ref(true)
const showContactDialog = ref(false)

// 租赁相关
const rentDialogVisible = ref(false)
const creating = ref(false)
const orderForm = reactive({
  houseId: '',
  rentCycle: 1, // 默认月付
})

// 计算总金额
const totalAmount = computed(() => {
  if (!house.value) return 0
  return (parseFloat(house.value.price) * orderForm.rentCycle).toFixed(2)
})

// 初始化
onMounted(() => {
  fetchHouseDetail()
})

// 获取房屋详情
const fetchHouseDetail = async () => {
  loading.value = true
  try {
    await request.get(`/houses/${houseId}`, {}, {
      onSuccess: (res) => {
        house.value = res
        // 初始化订单表单
        if (res) {
          orderForm.houseId = res.id
        }
        // 数据加载完成后，确保DOM更新并启用滚动
        nextTick(() => {
          document.body.style.overflow = 'auto'
          window.scrollTo(0, 0)
        })
      },
      onError: () => {
        house.value = null
      }
    })
  } catch (error) {
    console.error('获取房屋详情失败:', error)
    house.value = null
  } finally {
    loading.value = false
  }
}

// 计算房屋图片列表
const houseImages = computed(() => {
  if (!house.value || !house.value.images) return []
  try {
    const imageArray = JSON.parse(house.value.images)
    return imageArray.map(img => formatImageUrl(img))
  } catch (e) {
    return []
  }
})

// 计算房屋配套设施
const houseFacilities = computed(() => {
  if (!house.value || !house.value.facilities) return {}
  try {
    return JSON.parse(house.value.facilities)
  } catch (e) {
    return {}
  }
})

// 格式化图片URL
const formatImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return baseAPI + path
}

// 获取房东头像
const getLandlordAvatar = () => {
  if (!house.value || !house.value.landlordImg) return ''
  return formatImageUrl(house.value.landlordImg)
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

// 处理租赁
const handleRent = () => {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm(
      '请先登录后再进行租赁操作',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }
  
  if (!isTenant.value) {
    ElMessage.warning('只有租客角色可以租赁房屋')
    return
  }
  
  if (house.value.status !== 1) {
    ElMessage.warning('该房屋当前不可租赁')
    return
  }
  
  // 显示租赁对话框
  rentDialogVisible.value = true
}

// 创建订单
const createOrder = async () => {
  creating.value = true
  try {
    const orderData = {
      houseId: orderForm.houseId,
      amount: parseFloat(totalAmount.value),
      deposit: parseFloat(house.value.price)
    }
    
    await request.post('/orders', orderData, {
      successMsg: '订单创建成功',
      onSuccess: (res) => {
        rentDialogVisible.value = false
        
        // 提示用户是否前往支付
        ElMessageBox.confirm(
          '订单已创建成功，是否立即支付？',
          '提示',
          {
            confirmButtonText: '去支付',
            cancelButtonText: '稍后支付',
            type: 'success'
          }
        ).then(() => {
          router.push('/my-orders')
        }).catch(() => {
          router.push('/my-orders')
        })
      }
    })
  } catch (error) {
    console.error('创建订单失败:', error)
  } finally {
    creating.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.push('/houses')
}

// 添加组件卸载时的清理
onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style lang="scss" scoped>
.house-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
  position: relative;
  
  .loading-container {
    padding: 20px;
  }
  
  .not-found {
    text-align: center;
    padding: 40px 0;
    
    .back-btn {
      margin-top: 20px;
    }
  }
  
  .detail-header {
    position: relative;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e5e7eb;
    
    .house-title {
      font-size: 28px;
      font-weight: 700;
      color: #1d4ed8;
      margin: 0 0 10px 0;
    }
    
    .house-address {
      display: flex;
      align-items: center;
      color: #64748b;
      font-size: 16px;
      margin: 0;
      
      .el-icon {
        margin-right: 5px;
        color: #3b82f6;
      }
    }
    
    .house-price-tag {
      position: absolute;
      right: 0;
      top: 0;
      background-color: #ef4444;
      color: white;
      padding: 6px 12px;
      font-size: 20px;
      font-weight: bold;
      border-radius: 4px;
    }
  }
  
  .detail-main {
    .house-carousel {
      margin-bottom: 24px;
      border-radius: 8px;
      overflow: hidden;
      
      .carousel-image {
        width: 100%;
        height: 100%;
      }
      
      .no-images {
        height: 350px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        background-color: #f5f7fa;
        color: #c0c4cc;
        border-radius: 8px;
        
        .el-icon {
          font-size: 60px;
          margin-bottom: 15px;
        }
      }
      
      :deep(.el-carousel__indicators) {
        padding: 8px 0;
      }
      
      :deep(.el-carousel__arrow) {
        background-color: rgba(0, 0, 0, 0.5);
        
        &:hover {
          background-color: rgba(0, 0, 0, 0.8);
        }
      }
    }
    
    .house-info-card {
      margin-bottom: 20px;
      border-radius: 8px;
      
      .card-header {
        h2 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #1d4ed8;
        }
      }
      
      .info-section {
        margin-bottom: 24px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #1d4ed8;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 1px solid #f1f5f9;
      }
      
      .house-basic-info {
        .info-item {
          margin-bottom: 16px;
          
          .info-label {
            color: #64748b;
            font-size: 14px;
            margin-bottom: 4px;
          }
          
          .info-value {
            font-size: 16px;
            font-weight: 600;
            color: #334155;
          }
        }
      }
      
      .house-description {
        white-space: pre-line;
        line-height: 1.7;
        color: #334155;
      }
      
      .facilities-list {
        .facility-item {
          display: flex;
          align-items: center;
          padding: 8px 0;
          color: #94a3b8;
          
          .el-icon {
            margin-right: 10px;
          }
          
          &.available {
            color: #0f172a;
            
            .el-icon {
              color: #3b82f6;
            }
          }
        }
      }
    }
    
    .contact-card {
      position: sticky;
      top: 20px;
      
      .card-header {
        h2 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #1d4ed8;
        }
      }
      
      .contact-info {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        
        .el-avatar {
          margin-bottom: 16px;
        }
        
        .landlord-name {
          margin: 0 0 16px 0;
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
        }
        
        .divider {
          width: 100%;
          height: 1px;
          background-color: #e5e7eb;
          margin: 0 0 20px 0;
        }
        
        .contact-buttons {
          width: 100%;
          margin-bottom: 20px;
          
          .el-button {
            width: 100%;
          }
        }
        
        .notice-text {
          text-align: left;
          width: 100%;
          padding: 12px;
          background-color: #f8fafc;
          border-radius: 8px;
          
          p {
            margin: 0 0 8px 0;
            font-weight: 600;
            color: #475569;
          }
          
          ul {
            margin: 0;
            padding-left: 20px;
            
            li {
              margin-bottom: 6px;
              color: #64748b;
              font-size: 14px;
              
              &:last-child {
                margin-bottom: 0;
              }
            }
          }
        }
      }
    }
  }
  
  .contact-dialog-content {
    .el-alert {
      margin-bottom: 20px;
    }
    
    .phone-number {
      text-align: center;
      font-size: 24px;
      font-weight: bold;
      color: #3b82f6;
      padding: 20px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        margin-right: 10px;
      }
    }
  }
  
  .dialog-footer {
    text-align: center;
    
    .el-button {
      width: 100%;
    }
  }
}

.price, .total-price {
  color: #f56c6c;
  font-weight: bold;
}

.total-price {
  font-size: 22px;
}

.hint {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}

.rent-dialog-content {
  padding: 10px;
}

.contact-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

// 响应式调整
@media (max-width: 768px) {
  .house-detail-container {
    padding: 15px;
    
    .detail-header {
      .house-title {
        font-size: 22px;
        padding-right: 80px;
      }
      
      .house-price-tag {
        font-size: 16px;
        padding: 4px 8px;
      }
    }
    
    .detail-main {
      .house-carousel {
        :deep(.el-carousel) {
          height: 250px !important;
        }
      }
      
      .contact-card {
        margin-top: 20px;
        position: static;
      }
    }
  }
}
</style> 
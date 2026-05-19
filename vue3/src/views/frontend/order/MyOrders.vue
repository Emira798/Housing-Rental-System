<template>
  <div class="my-orders-container">
    <div class="page-header">
      <h2>我的订单</h2>
    </div>
    
    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="订单编号">
          <el-input v-model="filterForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- Tab分组 -->
    <div class="tab-container">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="unpaid" />
        <el-tab-pane label="待确认" name="unconfirmed" />
        <el-tab-pane label="已确认" name="confirmed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
    </div>
    
    <!-- 订单列表 -->
    <div class="orders-list" v-loading="loading">
      <el-empty v-if="orders.length === 0" description="暂无订单信息" />
      
      <el-card v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-date">下单时间：{{ DateUtils.formatDateTime(order.createTime) }}</span>
        </div>
        
        <div class="order-content">
          <div class="house-info">
            <el-image
              v-if="getHouseImage(order.house)"
              :src="getHouseImage(order.house)"
              fit="cover"
              class="house-image"
            />
            <div class="house-image-placeholder" v-else>
              <el-icon><Picture /></el-icon>
            </div>
            
            <div class="house-details">
              <div class="house-title">{{ order.house?.title || '未知房源' }}</div>
              <div class="house-address">{{ order.house?.address || '未知地址' }}</div>
              <div class="house-meta">
                <span v-if="order.house?.area">{{ order.house.area }}㎡</span>
                <span v-if="order.house?.typeName">{{ order.house.typeName }}</span>
              </div>
            </div>
          </div>
          
          <div class="order-info">
            <div class="landlord-info">
              <span>房东：{{ order.landlord?.name || '未知' }}</span>
              <span>联系方式：{{ order.landlord?.phone || '未知' }}</span>
            </div>
            <div class="price-info">
              <div class="amount">
                <span class="label">订单金额：</span>
                <span class="value">¥ {{ order.amount }}</span>
              </div>
              <div class="deposit">
                <span class="label">押金：</span>
                <span class="value">¥ {{ order.deposit }}</span>
              </div>
            </div>
            <div class="status-info">
              <el-tag :type="getStatusType(order.status)" effect="dark">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="payment-info" v-if="order.paymentTime">
            <span class="label">支付时间：</span>
            <span>{{ DateUtils.formatDateTime(order.paymentTime) }}</span>
            <span class="divider">|</span>
            <span class="label">支付方式：</span>
            <span>{{ order.paymentMethod }}</span>
          </div>
          
          <div class="action-buttons">
            <el-button 
              v-if="order.status === 0" 
              type="primary" 
              size="small" 
              @click="handlePay(order)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="order.status === 0" 
              type="danger" 
              size="small" 
              @click="handleCancel(order)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="order.status === 1" 
              type="warning" 
              size="small" 
              @click="handleRefund(order)"
            >
              申请退款
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="viewOrderDetail(order)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="orders.length > 0">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="currentPage = $event"
          @update:page-size="pageSize = $event"
        />
      </div>
    </div>
    
    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="订单支付" width="500px">
      <el-form :model="payForm" label-width="120px">
        <el-form-item label="订单编号">
          <span>{{ payForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="房屋">
          <span>{{ payForm.houseTitle }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span class="price">¥ {{ payForm.amount }}</span>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="payForm.paymentMethod">
            <el-radio label="支付宝">支付宝</el-radio>
            <el-radio label="微信支付">微信支付</el-radio>
            <el-radio label="银行卡">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPay" :loading="paying">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailForm.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailForm.status)">
            {{ getStatusText(detailForm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房屋标题">{{ detailForm.house?.title }}</el-descriptions-item>
        <el-descriptions-item label="房屋地址">{{ detailForm.house?.address }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥ {{ detailForm.amount }}</el-descriptions-item>
        <el-descriptions-item label="押金金额">¥ {{ detailForm.deposit }}</el-descriptions-item>
        <el-descriptions-item label="房东姓名">{{ detailForm.landlord?.name }}</el-descriptions-item>
        <el-descriptions-item label="房东电话">{{ detailForm.landlord?.phone }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ detailForm.paymentTime ? DateUtils.formatDateTime(detailForm.paymentTime) : '未支付' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detailForm.paymentMethod || '未支付' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ DateUtils.formatDateTime(detailForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ DateUtils.formatDateTime(detailForm.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 状态选项
const statusOptions = [
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付待确认' },
  { value: 2, label: '已确认' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已退款' }
]

// 获取状态文本
const getStatusText = (status) => {
  const option = statusOptions.find(item => item.value === status)
  return option ? option.label : '未知状态'
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'    // 待支付
    case 1: return 'warning' // 已支付待确认
    case 2: return 'success' // 已确认
    case 3: return 'danger'  // 已取消
    case 4: return 'info'    // 已退款
    default: return 'info'
  }
}

// 数据加载与分页
const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选表单
const filterForm = reactive({
  orderNo: '',
  status: ''
})

// 当前激活的标签页
const activeTab = ref('all')

// 支付对话框
const payDialogVisible = ref(false)
const paying = ref(false)
const payForm = reactive({
  id: '',
  orderNo: '',
  houseTitle: '',
  amount: 0,
  paymentMethod: '支付宝'
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: '',
  orderNo: '',
  status: 0,
  amount: 0,
  deposit: 0,
  house: {},
  landlord: {},
  paymentTime: '',
  paymentMethod: '',
  createTime: '',
  updateTime: ''
})

// 获取房屋图片
const getHouseImage = (house) => {
  if (!house || !house.images) return null
  try {
    const images = JSON.parse(house.images)
    if (images && images.length > 0) {
      const imgUrl = images[0]
      return imgUrl.startsWith('http') ? imgUrl : baseAPI + imgUrl
    }
    return null
  } catch (e) {
    return null
  }
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      orderNo: filterForm.orderNo || undefined,
      status: filterForm.status !== '' ? filterForm.status : undefined
    }
    
    // 根据激活的标签页设置状态
    if (activeTab.value === 'unpaid') {
      params.status = 0
    } else if (activeTab.value === 'unconfirmed') {
      params.status = 1
    } else if (activeTab.value === 'confirmed') {
      params.status = 2
    } else if (activeTab.value === 'cancelled') {
      params.status = 3
    }
    
    await request.get('/orders/tenant', params, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        orders.value = res.records||[]
        total.value = res.total||0
      }
    })
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 重置搜索
const resetSearch = () => {
  filterForm.orderNo = ''
  filterForm.status = ''
  handleSearch()
}

// 处理标签页点击
const handleTabClick = () => {
  currentPage.value = 1
  fetchOrders()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrders()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

// 查看订单详情
const viewOrderDetail = async (order) => {
  try {
    await request.get(`/orders/${order.id}`, {}, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        Object.assign(detailForm, res)
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

// 处理支付
const handlePay = (order) => {
  payForm.id = order.id
  payForm.orderNo = order.orderNo
  payForm.houseTitle = order.house?.title || '未知房屋'
  payForm.amount = order.amount
  payForm.paymentMethod = '支付宝'
  payDialogVisible.value = true
}

// 确认支付
const confirmPay = async () => {
  if (!payForm.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }

  paying.value = true
  try {
    await request.put(`/orders/${payForm.id}/pay`, null, {
      params: {
        paymentMethod: payForm.paymentMethod
      },
      successMsg: '支付成功',
      onSuccess: () => {
        payDialogVisible.value = false
        fetchOrders()
      }
    })
  } catch (error) {
    console.error('支付订单失败:', error)
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancel = (order) => {
  ElMessageBox.confirm(
    '确认取消此订单吗？取消后不可恢复',
    '订单取消',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/orders/${order.id}/cancel`, null, {
        successMsg: '订单取消成功',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('取消订单失败:', error)
    }
  }).catch(() => {})
}

// 申请退款
const handleRefund = (order) => {
  ElMessageBox.confirm(
    '确认申请退款吗？申请后将立即处理退款',
    '退款申请',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post(`/orders/${order.id}/refund`, null, {
        successMsg: '退款申请成功',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('申请退款失败:', error)
    }
  }).catch(() => {})
}

// 页面加载时获取订单列表
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.my-orders-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-weight: 500;
  color: #303133;
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.orders-list {
  min-height: 300px;
}

.order-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
}

.order-no {
  font-weight: bold;
  color: #303133;
}

.order-date {
  color: #909399;
}

.order-content {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 15px;
}

.house-info {
  display: flex;
  flex: 3;
  min-width: 300px;
}

.house-image, .house-image-placeholder {
  width: 120px;
  height: 90px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 15px;
}

.house-image-placeholder {
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 24px;
}

.house-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.house-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 8px;
}

.house-address {
  color: #606266;
  margin-bottom: 8px;
  font-size: 14px;
}

.house-meta {
  display: flex;
  gap: 10px;
  color: #909399;
  font-size: 13px;
}

.order-info {
  flex: 2;
  min-width: 250px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.landlord-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.price-info {
  margin: 10px 0;
}

.price-info .amount, .price-info .deposit {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.price-info .label {
  width: 80px;
  color: #909399;
}

.price-info .value {
  font-weight: bold;
  color: #f56c6c;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.payment-info {
  color: #606266;
  font-size: 14px;
}

.payment-info .label {
  color: #909399;
}

.payment-info .divider {
  margin: 0 10px;
  color: #DCDFE6;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.tab-container {
  margin-bottom: 20px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

@media (max-width: 768px) {
  .order-content {
    flex-direction: column;
  }
  
  .order-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style> 
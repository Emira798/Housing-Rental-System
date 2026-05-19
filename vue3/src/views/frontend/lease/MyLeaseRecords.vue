<template>
  <div class="my-lease-container">
    <div class="page-header">
      <h2>我的租赁记录</h2>
    </div>
    
    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="租赁状态">
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
    
    <!-- 租赁记录列表 -->
    <div class="lease-records-list" v-loading="loading">
      <el-empty v-if="leaseRecords.length === 0" description="暂无租赁记录信息" />
      
      <el-card v-for="record in leaseRecords" :key="record.id" class="lease-card">
        <div class="lease-header">
          <span class="lease-order-no">订单号：{{ record.order?.orderNo }}</span>
          <span class="lease-date">创建时间：{{ DateUtils.formatDateTime(record.createTime) }}</span>
        </div>
        
        <div class="lease-content">
          <div class="house-info">
            <el-image
              v-if="getHouseImage(record.house)"
              :src="getHouseImage(record.house)"
              fit="cover"
              class="house-image"
            />
            <div class="house-image-placeholder" v-else>
              <el-icon><Picture /></el-icon>
            </div>
            
            <div class="house-details">
              <div class="house-title">{{ record.house?.title || '未知房源' }}</div>
              <div class="house-address">{{ record.house?.address || '未知地址' }}</div>
              <div class="house-meta">
                <span v-if="record.house?.area">{{ record.house.area }}㎡</span>
                <span v-if="record.house?.typeName">{{ record.house.typeName }}</span>
              </div>
            </div>
          </div>
          
          <div class="lease-info">
            <div class="landlord-info">
              <span>房东：{{ record.landlord?.name || '未知' }}</span>
              <span>联系方式：{{ record.landlord?.phone || '未知' }}</span>
            </div>
            <div class="lease-period">
              <div>
                <span class="label">租期：</span>
                <span class="value">{{ DateUtils.formatDate(record.startDate) }} 至 {{ DateUtils.formatDate(record.endDate) }}</span>
              </div>
              <div>
                <span class="label">租金：</span>
                <span class="value">¥ {{ record.rentAmount }}</span>
              </div>
              <div>
                <span class="label">支付周期：</span>
                <span class="value">{{ getPaymentCycleText(record.paymentCycle) }}</span>
              </div>
            </div>
            <div class="status-info">
              <el-tag :type="getStatusType(record.status)" effect="dark">
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="lease-footer">
          <div class="evaluation-info" v-if="record.evaluationScore">
            <span class="label">我的评价：</span>
            <el-rate
              v-model="record.evaluationScore"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
            <span class="evaluation-content">{{ record.evaluationContent }}</span>
          </div>
          
          <div class="action-buttons">
            <el-button 
              type="info" 
              size="small" 
              @click="downloadContract(record)"
              v-if="record.contractUrl"
            >
              下载合同
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="viewLeaseDetail(record)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="record.status === 1 && !record.evaluationScore" 
              type="warning" 
              size="small" 
              @click="handleEvaluate(record)"
            >
              评价
            </el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="leaseRecords.length > 0">
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
    
    <!-- 租赁详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="租赁详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailForm.order?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="租赁状态">
          <el-tag :type="getStatusType(detailForm.status)">
            {{ getStatusText(detailForm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房屋标题">{{ detailForm.house?.title }}</el-descriptions-item>
        <el-descriptions-item label="房屋地址">{{ detailForm.house?.address }}</el-descriptions-item>
        <el-descriptions-item label="月租金">¥ {{ detailForm.rentAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付周期">{{ getPaymentCycleText(detailForm.paymentCycle) }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ DateUtils.formatDate(detailForm.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ DateUtils.formatDate(detailForm.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="房东姓名">{{ detailForm.landlord?.name }}</el-descriptions-item>
        <el-descriptions-item label="房东电话">{{ detailForm.landlord?.phone }}</el-descriptions-item>
        <el-descriptions-item label="合同" v-if="detailForm.contractUrl">
          <el-button type="info" size="small" @click="downloadContract(detailForm)">
            下载合同
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="评价分数" v-if="detailForm.evaluationScore">
          <el-rate
            v-model="detailForm.evaluationScore"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value}"
          />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" v-if="detailForm.evaluationContent">
          {{ detailForm.evaluationContent }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ DateUtils.formatDateTime(detailForm.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="evaluateDialogVisible" title="租赁评价" width="500px">
      <el-form :model="evaluateForm" label-width="100px">
        <el-form-item label="房屋">
          <span>{{ evaluateForm.houseTitle }}</span>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate
            v-model="evaluateForm.score"
            :texts="['很差', '较差', '一般', '不错', '很好']"
            show-text
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="evaluateForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您对此次租赁体验的评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="evaluateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEvaluation" :loading="submitting">提交评价</el-button>
        </span>
      </template>
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
  { value: 0, label: '已取消' },
  { value: 1, label: '租赁中' },
  { value: 2, label: '已结束' },
  { value: 3, label: '已退租' }
]

// 获取状态文本
const getStatusText = (status) => {
  const option = statusOptions.find(item => item.value === status)
  return option ? option.label : '未知状态'
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'danger'  // 已取消
    case 1: return 'success' // 租赁中
    case 2: return 'info'    // 已结束
    case 3: return 'warning' // 已退租
    default: return 'info'
  }
}

// 获取支付周期文本
const getPaymentCycleText = (cycle) => {
  switch (cycle) {
    case 'MONTHLY': return '月付';
    case 'QUARTERLY': return '季付';
    case 'YEARLY': return '年付';
    default: return '未知';
  }
}

// 数据加载与分页
const loading = ref(false)
const leaseRecords = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选表单
const filterForm = reactive({
  status: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: '',
  orderId: '',
  houseId: '',
  tenantId: '',
  landlordId: '',
  startDate: '',
  endDate: '',
  rentAmount: 0,
  paymentCycle: '',
  status: 0,
  actualEndDate: '',
  contractUrl: '',
  evaluationScore: null,
  evaluationContent: '',
  createTime: '',
  updateTime: '',
  house: {},
  tenant: {},
  landlord: {},
  order: {}
})

// 评价对话框
const evaluateDialogVisible = ref(false)
const submitting = ref(false)
const evaluateForm = reactive({
  id: '',
  houseTitle: '',
  score: 5,
  content: ''
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

// 获取租赁记录列表
const fetchLeaseRecords = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      status: filterForm.status !== '' ? filterForm.status : undefined
    }
    
    await request.get('/lease-records/tenant', params, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        leaseRecords.value = res.records||[]
        total.value = res.total||0
      }
    })
  } catch (error) {
    console.error('获取租赁记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  currentPage.value = 1
  fetchLeaseRecords()
}

// 重置搜索
const resetSearch = () => {
  filterForm.status = ''
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchLeaseRecords()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchLeaseRecords()
}

// 查看租赁详情
const viewLeaseDetail = (record) => {
  Object.assign(detailForm, record)
  detailDialogVisible.value = true
}

// 下载合同
const downloadContract = (record) => {
  if (!record.contractUrl) {
    ElMessage.warning('合同文件不存在')
    return
  }
  
  // 获取token
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('您需要登录才能下载合同')
    router.push('/login')
    return
  }
  
  // 创建一个隐藏的a标签实现下载
  const downloadUrl = baseAPI + '/lease-records/' + record.id + '/download-contract?token=' + token
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = '租赁合同_' + record.id + '.pdf'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 评价租赁
const handleEvaluate = (record) => {
  evaluateForm.id = record.id
  evaluateForm.houseTitle = record.house?.title || '未知房屋'
  evaluateForm.score = 5
  evaluateForm.content = ''
  evaluateDialogVisible.value = true
}

// 提交评价
const submitEvaluation = async () => {
  if (evaluateForm.score < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  
  if (!evaluateForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  submitting.value = true
  try {
    await request.post(`/lease-records/${evaluateForm.id}/evaluation`, null, {
      params: {
        score: evaluateForm.score,
        content: evaluateForm.content
      },
      successMsg: '评价提交成功',
      onSuccess: () => {
        evaluateDialogVisible.value = false
        fetchLeaseRecords()
      }
    })
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    submitting.value = false
  }
}

// 页面加载时获取租赁记录列表
onMounted(() => {
  fetchLeaseRecords()
})
</script>

<style scoped>
.my-lease-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

.lease-records-list {
  min-height: 300px;
}

.lease-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.lease-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.lease-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
}

.lease-order-no {
  font-weight: bold;
  color: #303133;
}

.lease-date {
  color: #909399;
}

.lease-content {
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

.lease-info {
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

.lease-period {
  margin: 10px 0;
}

.lease-period .label {
  width: 80px;
  display: inline-block;
  color: #909399;
}

.lease-period .value {
  color: #303133;
  font-weight: 500;
}

.lease-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.evaluation-info {
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.evaluation-info .label {
  color: #909399;
  margin-right: 10px;
}

.evaluation-content {
  margin-left: 15px;
  color: #303133;
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

/* 删除合同容器相关样式 */
.contract-container {
  display: none;
}

@media (max-width: 768px) {
  .lease-content {
    flex-direction: column;
  }
  
  .lease-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 
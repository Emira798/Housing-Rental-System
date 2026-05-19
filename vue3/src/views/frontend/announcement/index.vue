 <template>
  <div class="announcement-container">
    <div class="page-header">
      <h1 class="page-title">新闻公告</h1>
    </div>

    <el-row :gutter="20">
      <!-- 左侧公告列表 -->
      <el-col :xs="24" :sm="8" :md="6">
        <div class="announcement-list-container">
          <div class="list-header">
            <h3>公告列表</h3>
          </div>
          <div v-loading="loading" class="announcement-list">
            <div
              v-for="item in announcementList"
              :key="item.id"
              class="announcement-item"
              :class="{ active: selectedAnnouncement?.id === item.id }"
              @click="handleSelectAnnouncement(item)"
            >
              <div class="announcement-item-title">{{ item.title }}</div>
              <div class="announcement-item-time">{{ formatDate(item.createTime) }}</div>
            </div>
            
            <!-- 空状态 -->
            <div v-if="!loading && announcementList.length === 0" class="empty-state">
              暂无公告
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="total"
              @current-change="handleCurrentChange"
              small
              background
            />
          </div>
        </div>
      </el-col>

      <!-- 右侧公告详情 -->
      <el-col :xs="24" :sm="16" :md="18">
        <div class="announcement-detail-container">
          <div v-if="selectedAnnouncement" class="announcement-detail">
            <h2 class="announcement-title">{{ selectedAnnouncement.title }}</h2>
            <div class="announcement-meta">
              <span>发布人: {{ selectedAnnouncement.admin?.name || '管理员' }}</span>
              <span>发布时间: {{ formatDate(selectedAnnouncement.createTime) }}</span>
            </div>
            <div class="announcement-content">
              <p v-html="formatContent(selectedAnnouncement.content)"></p>
            </div>
          </div>
          <div v-else class="announcement-placeholder">
            <el-empty description="请选择要查看的公告" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

// 表格数据
const loading = ref(false)
const announcementList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedAnnouncement = ref(null)

// 初始化
onMounted(() => {
  fetchAnnouncementList()
})

// 获取公告列表
const fetchAnnouncementList = async () => {
  loading.value = true
  try {
    await request.get('/announcements', {
      status: 1, // 只获取已发布的公告
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        announcementList.value = res.records
        total.value = res.total
        
        // 如果有数据且未选中任何公告，则默认选中第一条
        if (res.records.length > 0 && !selectedAnnouncement.value) {
          selectedAnnouncement.value = res.records[0]
        }
      }
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAnnouncementList()
}

// 选择公告
const handleSelectAnnouncement = (announcement) => {
  selectedAnnouncement.value = announcement
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return DateUtils.format(date, 'YYYY-MM-DD HH:mm')
}

// 格式化内容（将换行符转换为<br>）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.announcement-container {
  padding: 20px;
  min-height: 600px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.announcement-list-container {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.list-header h3 {
  margin: 0;
  color: #303133;
}

.announcement-list {
  flex: 1;
  overflow-y: auto;
  max-height: 500px;
}

.announcement-item {
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: all 0.3s;
}

.announcement-item:hover {
  background-color: #F5F7FA;
}

.announcement-item.active {
  background-color: #EFF6FF;
  border-left: 3px solid #3B82F6;
}

.announcement-item-title {
  font-weight: 500;
  margin-bottom: 5px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-item-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 20px;
  text-align: center;
  color: #909399;
}

.pagination-container {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #EBEEF5;
}

.announcement-detail-container {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.announcement-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 15px;
  text-align: center;
  color: #303133;
}

.announcement-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.announcement-content {
  line-height: 1.8;
  font-size: 16px;
  color: #606266;
}

.announcement-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 300px;
}

@media (max-width: 768px) {
  .announcement-list-container {
    margin-bottom: 20px;
  }
}
</style>
<template>
  <div class="announcement-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    <template v-else>
      <div v-if="announcement" class="announcement-detail">
        <div class="announcement-header">
          <h1 class="announcement-title">{{ announcement.title }}</h1>
          <div class="announcement-meta">
            <span><el-icon><User /></el-icon> {{ announcement.admin?.name || '管理员' }}</span>
            <span><el-icon><Calendar /></el-icon> {{ formatDate(announcement.createTime) }}</span>
          </div>
        </div>
        <div class="announcement-content">
          <p v-html="formatContent(announcement.content)"></p>
        </div>
        <div class="announcement-footer">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="goToAnnouncementList">查看全部公告</el-button>
        </div>
      </div>
      <div v-else class="not-found">
        <el-empty description="公告不存在或已被删除">
          <template #extra>
            <el-button type="primary" @click="goToAnnouncementList">返回公告列表</el-button>
          </template>
        </el-empty>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Calendar } from '@element-plus/icons-vue'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const announcement = ref(null)

// 初始化
onMounted(() => {
  const announcementId = route.params.id
  if (announcementId) {
    fetchAnnouncementDetail(announcementId)
  } else {
    loading.value = false
  }
})

// 获取公告详情
const fetchAnnouncementDetail = async (id) => {
  loading.value = true
  try {
    await request.get(`/announcements/${id}`, {}, {
      onSuccess: (data) => {
        announcement.value = data
      },
      onError: () => {
        announcement.value = null
      }
    })
  } catch (error) {
    console.error('获取公告详情失败:', error)
    announcement.value = null
  } finally {
    loading.value = false
  }
}

// 格式化内容（将换行符转换为<br>）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return DateUtils.formatDateTime(date)
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 前往公告列表
const goToAnnouncementList = () => {
  router.push('/announcements')
}
</script>

<style scoped>
.announcement-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.announcement-detail {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.announcement-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 20px;
}

.announcement-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
  text-align: center;
}

.announcement-meta {
  display: flex;
  justify-content: center;
  gap: 30px;
  color: #909399;
  font-size: 14px;
}

.announcement-meta span {
  display: flex;
  align-items: center;
}

.announcement-meta .el-icon {
  margin-right: 5px;
}

.announcement-content {
  line-height: 1.8;
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.announcement-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.not-found {
  padding: 50px 0;
  text-align: center;
}
</style> 
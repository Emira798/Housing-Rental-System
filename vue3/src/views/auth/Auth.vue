<template>
  <div class="auth-container">
    <div class="auth-box">
      <!-- 左侧 - 插画与品牌区域 -->
      <div class="auth-left">
        <img src="@/assets/房地产.svg" alt="房屋租赁" class="auth-illustration" />
        <div class="auth-brand">
          <h2 class="brand-title">品质房屋租赁系统</h2>
          <p class="brand-subtitle">寻找理想住所，开启美好生活</p>
          
          <div class="brand-feature">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Search /></el-icon>
              </div>
              <span class="feature-text">便捷搜索</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Check /></el-icon>
              </div>
              <span class="feature-text">实名认证</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Wallet /></el-icon>
              </div>
              <span class="feature-text">安全支付</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧 - 表单区域 -->
      <div class="auth-right">
        <div class="auth-header" v-if="showHeader">
          <h1 class="title">{{ title }}</h1>
          <div class="subtitle">{{ subtitle }}</div>
        </div>
        
        <el-form :model="formData" :rules="rules" ref="formRef" class="auth-form">
          <slot name="form-items"></slot>
          
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit" class="auth-button">
              {{ submitText }}
            </el-button>
          </el-form-item>
          
          <div class="auth-links">
            <slot name="auth-links"></slot>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, Check, Wallet } from '@element-plus/icons-vue'

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '提交'
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  title: {
    type: String,
    default: '欢迎使用'
  },
  subtitle: {
    type: String,
    default: '请填写以下信息'
  }
})

const formRef = ref(null)

const emit = defineEmits(['submit'])

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      emit('submit', formRef)
    }
  })
}

defineExpose({
  formRef
})
</script>

<style lang="scss" scoped>
@import '@/styles/auth.scss';

/* 可以在这里添加额外的样式来覆盖 auth.scss 中的默认样式 */
.auth-left {
  .auth-brand {
    .brand-subtitle {
      white-space: nowrap; /* 防止副标题换行 */
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .feature-icon {
    font-size: 18px;
    
    .el-icon {
      font-size: 18px;
    }
  }
}
</style> 
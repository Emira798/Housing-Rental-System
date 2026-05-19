<template>
  <div class="frontend-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <span class="logo-icon">🏠</span>
            <span class="logo-text">品质房屋租赁系统</span>
          </router-link>
        </div>
        <nav class="nav-menu">
          <router-link to="/" custom v-slot="{ navigate, isExactActive }">
            <a href="javascript:;" @click="navigate" :class="{ 'router-link-active': isExactActive }">首页</a>
          </router-link>
          <router-link to="/houses">房源列表</router-link>
          <!--
           <router-link to="/announcements">新闻公告</router-link>
  -->
           <template v-if="isLoggedIn">
             <router-link v-if="isLandlord" to="/landlord/houses">我的房源</router-link>
             <router-link v-if="isTenant" to="/my-orders">我的订单</router-link>
              <!--
               <router-link v-if="isTenant" to="/my-leases">我的租赁</router-link>
             -->
            <router-link to="/profile">个人中心</router-link>
                <a href="javascript:;" @click="handleLogout" class="logout-btn">退出登录</a>
              </template>
              <template v-else>
                <router-link to="/login" class="login-btn">登录</router-link>
                <router-link to="/register" class="register-btn">注册</router-link>
              </template>
            </nav>

            <!-- 移动端菜单按钮 -->
        <div class="mobile-menu-btn" @click="toggleMobileMenu">
          <el-icon :size="24"><Menu /></el-icon>
        </div>
      </div>

      <!-- 移动端菜单 -->
      <div class="mobile-menu" :class="{ 'active': mobileMenuActive }">
        <router-link to="/" custom v-slot="{ navigate, isExactActive }">
          <a href="javascript:;" @click="navigate" :class="{ 'router-link-active': isExactActive }">首页</a>
        </router-link>
        <router-link to="/houses">房源列表</router-link>
        <router-link to="/announcements">新闻公告</router-link>

        <template v-if="isLoggedIn">
          <router-link v-if="isLandlord" to="/landlord/houses">我的房源</router-link>
          <router-link v-if="isTenant" to="/my-orders">我的订单</router-link>
          <router-link v-if="isTenant" to="/my-leases">我的租赁</router-link>
          <router-link to="/profile">个人中心</router-link>
          <a href="javascript:;" @click="handleLogout">退出登录</a>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-copyright">
          <p>&copy; 2025 品质房屋租赁系统 All Rights Reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { Menu } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const mobileMenuActive = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isLandlord = computed(() => userStore.userInfo?.roleCode === 'LANDLORD')
const isTenant = computed(() => userStore.userInfo?.roleCode === 'TENANT')

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
  mobileMenuActive.value = false
}

const toggleMobileMenu = () => {
  mobileMenuActive.value = !mobileMenuActive.value
}
</script>

<style lang="scss" scoped>
.frontend-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
}

// 头部导航样式
.header {
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    height: 64px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    a {
      display: flex;
      align-items: center;
      text-decoration: none;
      color: #1d4ed8;
    }

    .logo-icon {
      font-size: 24px;
      margin-right: 8px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .nav-menu {
    display: flex;
    gap: 24px;
    align-items: center;

    a {
      color: #64748b;
      text-decoration: none;
      font-size: 16px;
      font-weight: 500;
      position: relative;
      padding: 8px 0;
      transition: color 0.3s;

      &:hover, &.router-link-active {
        color: #1d4ed8;
      }

      &.router-link-active::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 2px;
        background-color: #1d4ed8;
      }

      &.login-btn, &.register-btn {
        padding: 8px 16px;
        border-radius: 4px;
      }

      &.login-btn {
        border: 1px solid #1d4ed8;
        color: #1d4ed8;

        &:hover {
          background-color: rgba(29, 78, 216, 0.05);
        }
      }

      &.register-btn {
        background-color: #1d4ed8;
        color: white;

        &:hover {
          background-color: #1e40af;
        }
      }
    }
  }

  .mobile-menu-btn {
    display: none;
    cursor: pointer;
    color: #1d4ed8;
  }

  .mobile-menu {
    display: none;
    flex-direction: column;
    background: white;
    padding: 0;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease, padding 0.3s ease;

    &.active {
      padding: 16px;
      max-height: 300px;
    }

    a {
      color: #64748b;
      text-decoration: none;
      padding: 12px 0;
      border-bottom: 1px solid #f1f5f9;

      &:last-child {
        border-bottom: none;
      }

      &:hover, &.router-link-active {
        color: #1d4ed8;
      }
    }
  }
}

// 主内容区样式
.main-content {
  flex: 1;
  margin-top: 64px; // 头部导航的高度
  min-height: calc(100vh - 64px);
  position: relative;
  overflow-y: auto;
}

// 页脚样式
.footer {
  background-color: #1e293b;
  color: #e2e8f0;
  padding: 40px 0 20px;

  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .footer-links {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    margin-bottom: 30px;

    .footer-section {
      flex: 1;
      min-width: 200px;
      margin-bottom: 20px;

      h3 {
        font-size: 18px;
        margin-bottom: 15px;
        color: white;
      }

      a {
        display: block;
        color: #94a3b8;
        text-decoration: none;
        margin-bottom: 8px;
        transition: color 0.3s;

        &:hover {
          color: white;
        }
      }
    }
  }

  .footer-copyright {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    p {
      color: #94a3b8;
      font-size: 14px;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .header {
    .nav-menu {
      display: none;
    }

    .mobile-menu-btn {
      display: block;
    }

    .mobile-menu {
      display: flex;
    }
  }

  .footer {
    .footer-links {
      flex-direction: column;

      .footer-section {
        margin-bottom: 30px;
      }
    }
  }
}
</style>
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'house-type',
        name: 'HouseTypeManagement',
        component: () => import('@/views/backend/house-type/index.vue'),
        meta: { title: '房屋类型管理', icon: 'Menu' }
      },
      {
        path: 'house',
        name: 'HouseManagement',
        component: () => import('@/views/backend/house/index.vue'),
        meta: { title: '房屋管理', icon: 'House' }
      },
      {
        path: 'order',
        name: 'OrderManagement',
        component: () => import('@/views/backend/order/index.vue'),
        meta: { title: '订单管理', icon: 'Tickets' }
      },
      {
        path: 'announcement',
        name: 'AnnouncementManagement',
        component: () => import('@/views/backend/announcement/index.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      },
      {
        path: 'carousel',
        name: 'CarouselManagement',
        component: () => import('@/views/backend/carousel/index.vue'),
        meta: { title: '轮播图管理', icon: 'Picture' }
      },
      {
        path: 'transaction',
        name: 'TransactionManagement',
        component: () => import('@/views/backend/transaction/index.vue'),
        meta: { title: '收支明细', icon: 'Money' }
      },
      {
        path: 'lease',
        name: 'LeaseManagement',
        component: () => import('@/views/backend/lease/index.vue'),
        meta: { title: '租赁管理', icon: 'Document' }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/backend/user/PersonInfo.vue'),
        meta: { title: '个人信息', icon: 'UserFilled' }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'houses',
        name: 'Houses',
        component: () => import('@/views/frontend/house/HouseList.vue'),
        meta: { title: '房屋列表' }
      },
      {
        path: 'houses/:id',
        name: 'HouseDetail',
        component: () => import('@/views/frontend/house/HouseDetail.vue'),
        meta: { title: '房屋详情' }
      },
      {
        path: 'landlord/houses',
        name: 'LandlordHouses',
        component: () => import('@/views/frontend/house/LandlordHouses.vue'),
        meta: { title: '我的房源', requiresAuth: true }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/frontend/announcement/index.vue'),
        meta: { title: '新闻公告' }
      },
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/frontend/announcement/Detail.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/frontend/order/MyOrders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'my-leases',
        name: 'MyLeaseRecords',
        component: () => import('@/views/frontend/lease/MyLeaseRecords.vue'),
        meta: { title: '我的租赁', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ] 
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 动态设置后台路由的函数
export function setBackRoutes() {

  console.log('设置后台路由')

  return true
}

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 品质房屋租赁系统`
  }else{
    document.title = `品质房屋租赁系统`
  }

  const userStore = useUserStore()
  console.log("Current route:", to.path)
  console.log("User status:", {
    isLoggedIn: userStore.isLoggedIn,
    isUser: userStore.isUser
  })

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      next(userStore.isUser ? '/' : '/back/dashboard')
      return
    }

    if (!userStore.isTenant) {
      // 非普通用户只能访问后台路由
      if (to.path.startsWith('/back')) {
        next()
      } else {
        next('/back/dashboard')
      }
      return
    } else {
      // 普通用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
      } else {
        next()
      }
      return
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
  }

  next()
})

export default router

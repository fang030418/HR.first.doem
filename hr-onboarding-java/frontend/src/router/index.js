import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/utils/http'
import LoginView from '@/views/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true, title: '管理员登录' }
  },
  {
    path: '/',
    redirect: '/employees/new'
  },
  {
    path: '/employees/new',
    name: 'employee-create',
    component: () => import('@/views/EmployeeCreateView.vue'),
    meta: { requiresAuth: true, title: '新员工入职建号' }
  },
  {
    path: '/employees',
    name: 'employee-list',
    component: () => import('@/views/EmployeeListView.vue'),
    meta: { requiresAuth: true, title: '新员工列表' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !auth.getToken()) {
    // 未登录：重定向到登录页并记录来源，登录后可回跳
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.getToken()) {
    return { name: 'employee-create' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 新员工入职管理系统` : '新员工入职管理系统'
})

export default router
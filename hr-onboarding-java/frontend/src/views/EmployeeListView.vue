<template>
  <div>
    <header class="page-head">
      <span class="logo">新员工入职管理系统</span>
      <router-link to="/employees/new">新员工入职建号</router-link>
      <span class="spacer"></span>
      <span class="who">当前管理员：{{ username }}</span>
      <button type="button" class="btn btn-ghost" @click="onLogout">退出登录</button>
    </header>

    <main class="page-body">
      <section class="panel">
        <h2 class="panel-title">新员工列表</h2>

        <div class="toolbar">
          <input v-model.trim="keyword" type="text" maxlength="30" placeholder="按账号 / 姓名搜索"
            @keyup.enter="onSearch" />
          <button type="button" class="btn" @click="onSearch">查询</button>
          <button type="button" class="btn btn-ghost" @click="onResetSearch">重置</button>
          <span class="spacer"></span>
          <span class="total">{{ total ? `共 ${total} 位新员工` : '' }}</span>
          <button type="button" class="btn" @click="load">刷新</button>
        </div>

        <div v-if="alert.message" class="alert alert-error show">{{ alert.message }}</div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="!list.length" class="empty">
          {{ keyword ? `没有匹配「${keyword}」的员工` : '暂无新员工，请先前往「新员工入职建号」创建账号' }}
        </div>
        <table v-else class="list">
          <thead>
            <tr>
              <th>登录账号</th>
              <th>姓名</th>
            </tr>
          </thead>
          <!-- Vue 模板插值默认转义，天然避免 XSS -->
          <tbody>
            <tr v-for="row in list" :key="row.id">
              <td class="col-account">{{ row.loginAccount }}</td>
              <td>{{ row.name }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="list.length" class="pager">
          <button type="button" class="btn btn-ghost" :disabled="page <= 1" @click="goPage(page - 1)">
            上一页
          </button>
          <span>第 {{ page }} / {{ totalPages }} 页</span>
          <button type="button" class="btn btn-ghost" :disabled="page >= totalPages" @click="goPage(page + 1)">
            下一页
          </button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http, { auth } from '@/utils/http'

const router = useRouter()

const username = ref(auth.getUser() || '-')
const keyword = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const totalPages = ref(1)
const loading = ref(false)
const alert = reactive({ message: '' })

async function load() {
  loading.value = true
  alert.message = ''
  try {
    // 列表接口使用 GET 传参（query string）
    const res = await http.get('/employees', {
      params: { page: page.value, pageSize, keyword: keyword.value }
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
    page.value = res.data.page
    totalPages.value = res.data.totalPages
  } catch (err) {
    alert.message = err?.message || '加载失败'
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  load()
}

function onResetSearch() {
  keyword.value = ''
  page.value = 1
  load()
}

function goPage(target) {
  if (target < 1 || target > totalPages.value) return
  page.value = target
  load()
}

async function onLogout() {
  await auth.logout()
  router.replace('/login')
}

onMounted(async () => {
  try {
    const res = await http.get('/auth/me')
    username.value = res.data.username
  } catch { /* 已由拦截器处理 */ }
  load()
})
</script>
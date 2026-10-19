<template>
  <div class="login-body">
    <div class="login-bg"></div>
    <div class="login-brand">新员工入职管理系统</div>

    <div class="login-wrap">
      <section class="login-hero" aria-hidden="true">
        <h1>欢迎使用</h1>
        <p>新员工入职管理系统</p>
      </section>

      <section class="login-card">
        <h2>管理员登录</h2>

        <div v-if="alert.message" class="alert alert-error show">{{ alert.message }}</div>

        <form @submit.prevent="onSubmit">
          <div class="form-item">
            <label for="username">用户名或管理员账号</label>
            <div class="input-wrap">
              <span class="field-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </span>
              <input id="username" v-model.trim="form.username" type="text" maxlength="50"
                placeholder="请输入管理员账号" autocomplete="username" />
            </div>
          </div>

          <div class="form-item">
            <label for="password">密码</label>
            <div class="input-wrap">
              <span class="field-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </span>
              <input id="password" v-model="form.password" type="password" maxlength="128"
                placeholder="请输入密码" autocomplete="current-password" />
            </div>
          </div>

          <div class="login-row">
            <label class="remember">
              <input v-model="form.remember" type="checkbox">
              <span>记住我</span>
            </label>
            <a href="#" @click.prevent="forgotTip">忘记密码？</a>
          </div>

          <button type="submit" class="btn btn-block" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <p class="login-tip">
          默认管理员账号：<strong>admin</strong>&nbsp;/&nbsp;<strong>admin123</strong><br>
          登录后可为新员工入职建号
        </p>
      </section>

      <div class="login-collapse">© 2026 新员工入职管理系统</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { auth } from '@/utils/http'

const router = useRouter()
const route = useRoute()

const form = reactive({ username: '', password: '', remember: false })
const loading = ref(false)
const alert = reactive({ message: '' })

function forgotTip() {
  alert.message = '忘记密码请联系系统超级管理员在服务端重置（配置项 hr.admin.password）'
}

async function onSubmit() {
  alert.message = ''

  if (!form.username) {
    alert.message = '请输入管理员账号'
    return
  }
  if (!form.password) {
    alert.message = '请输入密码'
    return
  }

  loading.value = true
  try {
    const res = await http.post('/auth/login', {
      username: form.username,
      password: form.password,
      remember: form.remember
    })
    auth.save(res.data.token, res.data.username, form.remember)
    router.replace(route.query.redirect || '/employees/new')
  } catch (err) {
    alert.message = err?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <div>
    <header class="page-head">
      <span class="logo">新员工入职管理系统</span>
      <router-link to="/employees">新员工列表</router-link>
      <span class="spacer"></span>
      <span class="who">当前管理员：{{ username }}</span>
      <button type="button" class="btn btn-ghost" @click="onLogout">退出登录</button>
    </header>

    <main class="page-body">
      <section class="panel">
        <h2 class="panel-title">新员工入职建号</h2>

        <div v-if="alert.message" class="alert show" :class="alert.type">{{ alert.message }}</div>

        <form @submit.prevent="onSubmit" novalidate>
          <div class="form-grid">
            <div class="field">
              <label for="loginAccount">登录账号<span class="req">*</span></label>
              <input id="loginAccount" v-model.trim="form.loginAccount" type="text" maxlength="20"
                :class="{ invalid: errors.loginAccount }" placeholder="字母开头，4-20 位"
                autocomplete="off" @blur="validateOne('loginAccount')"
                @input="errors.loginAccount && validateOne('loginAccount')" />
              <div v-if="errors.loginAccount" class="field-msg show">{{ errors.loginAccount }}</div>
            </div>

            <div class="field">
              <label for="password">密码<span class="req">*</span></label>
              <input id="password" v-model="form.password" type="password" maxlength="32"
                :class="{ invalid: errors.password }" placeholder="6-32 位，须含字母和数字"
                autocomplete="new-password" @blur="validateOne('password')"
                @input="errors.password && validateOne('password')" />
              <div v-if="errors.password" class="field-msg show">{{ errors.password }}</div>
            </div>

            <div class="field">
              <label for="name">姓名<span class="req">*</span></label>
              <input id="name" v-model.trim="form.name" type="text" maxlength="20"
                :class="{ invalid: errors.name }" placeholder="请输入真实姓名"
                autocomplete="off" @blur="validateOne('name')"
                @input="errors.name && validateOne('name')" />
              <div v-if="errors.name" class="field-msg show">{{ errors.name }}</div>
            </div>

            <!-- 性别：按参考布局要求使用 radio 单选 -->
            <div class="field">
              <label>性别<span class="req">*</span></label>
              <div class="radio-group">
                <label v-for="g in GENDERS" :key="g" class="radio-item" :for="`gender-${g}`">
                  <input :id="`gender-${g}`" v-model="form.gender" type="radio" name="gender"
                    :value="g" @change="clearError('gender')" />
                  <span>{{ g }}</span>
                </label>
              </div>
              <div v-if="errors.gender" class="field-msg show">{{ errors.gender }}</div>
            </div>

            <div class="field">
              <label for="phone">手机号<span class="req">*</span></label>
              <input id="phone" v-model.trim="form.phone" type="text" maxlength="11"
                :class="{ invalid: errors.phone }" placeholder="11 位中国大陆手机号"
                inputmode="numeric" autocomplete="off" @blur="validateOne('phone')"
                @input="errors.phone && validateOne('phone')" />
              <div v-if="errors.phone" class="field-msg show">{{ errors.phone }}</div>
            </div>

            <div class="field">
              <label for="email">Email<span class="req">*</span></label>
              <input id="email" v-model.trim="form.email" type="text" maxlength="64"
                :class="{ invalid: errors.email }" placeholder="name@example.com"
                autocomplete="off" @blur="validateOne('email')"
                @input="errors.email && validateOne('email')" />
              <div v-if="errors.email" class="field-msg show">{{ errors.email }}</div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn" :disabled="loading">
              {{ loading ? '提交中...' : '提交' }}
            </button>
            <button type="button" class="btn btn-ghost" @click="onReset">重置</button>
            <router-link to="/employees" class="btn btn-ghost btn-link">查看员工列表</router-link>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http, { auth } from '@/utils/http'
import { GENDERS, RULES, validateEmployee } from '@/utils/validate'

const router = useRouter()
const username = ref(auth.getUser() || '-')

const emptyForm = () => ({
  loginAccount: '',
  password: '',
  name: '',
  gender: '',
  phone: '',
  email: ''
})

const form = reactive(emptyForm())
const errors = reactive({})
const alert = reactive({ message: '', type: 'alert-error' })
const loading = ref(false)

/** 校验单个字段；force=false 时值为空不提示 */
function validateOne(field, force = true) {
  const message = RULES[field](form[field])
  errors[field] = force ? message : ''
}

function clearError(field) {
  errors[field] = ''
}

function clearAll() {
  Object.keys(errors).forEach((key) => delete errors[key])
  alert.message = ''
}

function onReset() {
  Object.assign(form, emptyForm())
  clearAll()
}

async function onSubmit() {
  alert.message = ''
  const { valid, errors: allErrors } = validateEmployee(form)

  Object.keys(errors).forEach((key) => delete errors[key])
  Object.assign(errors, allErrors)

  if (!valid) {
    alert.message = '请检查表单中标红的字段'
    return
  }

  loading.value = true
  try {
    // 建号接口使用 POST 传参（JSON body）
    const res = await http.post('/employees', { ...form })
    alert.message = `创建成功：${res.data.name}（账号 ${res.data.loginAccount}），正在跳转员工列表...`
    alert.type = 'alert-success'
    setTimeout(() => router.push('/employees'), 1200)
  } catch (err) {
    // 后端返回的字段级错误回填到对应控件
    if (err?.errors) {
      Object.keys(err.errors).forEach((key) => {
        errors[key] = err.errors[key]
      })
    }
    alert.message = err?.message || '创建失败，请重试'
    alert.type = 'alert-error'
  } finally {
    loading.value = false
  }
}

async function onLogout() {
  await auth.logout()
  router.replace('/login')
}

onMounted(async () => {
  // 会话确认：token 失效时由 axios 拦截器统一跳转登录
  try {
    const res = await http.get('/auth/me')
    username.value = res.data.username
    auth.save(auth.getToken(), res.data.username, !!localStorage.getItem('hr_token'))
  } catch { /* 已由拦截器处理 */ }
})
</script>

<style scoped>
.btn-link {
  line-height: 32px;
  text-decoration: none;
}
</style>
/**
 * 前端校验规则 —— 与后端 EmployeeCreateRequest 注解保持同源定义。
 * 前端仅为交互体验，服务端仍是最终防线。
 */

export const PHONE_RE = /^1[3-9]\d{9}$/
export const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$/
export const ACCOUNT_RE = /^[A-Za-z][A-Za-z0-9_.-]{3,19}$/
export const GENDERS = ['男', '女']

export const RULES = {
  loginAccount(value) {
    const v = (value || '').trim()
    if (!v) return '登录账号不能为空'
    if (!ACCOUNT_RE.test(v)) {
      return '登录账号需以字母开头，仅含字母/数字/下划线/点/短横，长度 4-20 位'
    }
    return ''
  },
  password(value) {
    const v = value || ''
    if (!v) return '密码不能为空'
    if (v.length < 6 || v.length > 32) return '密码长度需为 6-32 位'
    if (!/[A-Za-z]/.test(v) || !/\d/.test(v)) return '密码需同时包含字母和数字'
    return ''
  },
  name(value) {
    const v = (value || '').trim()
    if (!v) return '姓名不能为空'
    if (v.length > 20) return '姓名长度不能超过 20 个字符'
    return ''
  },
  gender(value) {
    if (!value) return '请选择性别'
    if (!GENDERS.includes(value)) return '性别只能为男或女'
    return ''
  },
  phone(value) {
    const v = (value || '').trim()
    if (!v) return '手机号不能为空'
    if (!/^\d+$/.test(v)) return '手机号只能包含数字'
    if (!PHONE_RE.test(v)) return '手机号格式不正确（应为 1 开头的 11 位数字）'
    return ''
  },
  email(value) {
    const v = (value || '').trim()
    if (!v) return 'Email 不能为空'
    if (!EMAIL_RE.test(v)) return 'Email 格式不正确，请检查形如 name@example.com'
    if (v.length > 64) return 'Email 长度不能超过 64 个字符'
    return ''
  }
}

/** 校验整个表单，返回 { valid, errors } */
export function validateEmployee(form) {
  const errors = {}
  Object.keys(RULES).forEach((field) => {
    const message = RULES[field](form[field])
    if (message) errors[field] = message
  })
  return { valid: Object.keys(errors).length === 0, errors }
}
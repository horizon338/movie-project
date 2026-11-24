<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度需为 3-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '仅支持字母、数字和下划线', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '长度需为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const submit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      nickname: form.nickname,
      phone: form.phone,
      password: form.password,
    })
    ElMessage.success('注册成功，已自动登录')
    router.replace('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <p class="brand-line">光影票务 · 创建账号</p>
      <h2>注册后即可在线购票</h2>
      <p class="hint">选座、出票、退票均在线上完成，观影后可发布评价。</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="字母、数字或下划线，3-20 位" size="large" clearable />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填，默认与用户名一致" size="large" clearable />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="用于购票通知" size="large" clearable maxlength="11" />
        </el-form-item>
        <div class="grid-2">
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="6-32 位" size="large" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              size="large"
              show-password
            />
          </el-form-item>
        </div>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
          注册并登录
        </el-button>
      </el-form>

      <p class="switch">
        已有账号？
        <RouterLink to="/login" class="link">直接登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: flex;
  justify-content: center;
  padding: 28px 0;
}

.auth-card {
  width: min(600px, 100%);
  padding: 42px 44px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 8px;
}

.brand-line {
  margin: 0 0 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: var(--red);
}

.auth-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}

.hint {
  margin: 0 0 26px;
  font-size: 13px;
  color: var(--ink-3);
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.submit {
  width: 100%;
  margin-top: 4px;
}

.switch {
  margin: 22px 0 0;
  text-align: center;
  font-size: 13px;
  color: var(--ink-3);
}

.link {
  color: var(--red);
}

.link:hover {
  color: var(--red-deep);
}

@media (max-width: 620px) {
  .auth-card {
    padding: 30px 22px;
  }

  .grid-2 {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>

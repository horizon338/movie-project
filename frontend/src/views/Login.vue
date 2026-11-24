<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const submit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  loading.value = true
  try {
    const data = await userStore.login({ ...form })
    ElMessage.success(`欢迎回来，${data.user.nickname || data.user.username}`)
    const redirect = route.query.redirect
    if (redirect) {
      router.replace(String(redirect))
    } else {
      router.replace(data.user.role === 'ADMIN' ? '/admin' : '/')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-panel">
      <aside class="auth-aside">
        <p class="aside-brand">光影票务</p>
        <h1>好电影，<br />值得一张好票。</h1>
        <p class="aside-note">登录后即可选座购票、查看电子票与观影记录。</p>
        <ul class="perks">
          <li>实时座位图，锁定心仪座位</li>
          <li>30 分钟内完成支付自动出票</li>
          <li>开场前 2 小时支持自助退票</li>
        </ul>
      </aside>

      <div class="auth-card">
        <h2>账号登录</h2>
        <p class="hint">演示账号 demo / demo123 · 管理员 admin / admin123</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>

        <p class="switch">
          还没有账号？
          <RouterLink to="/register" class="link">立即注册</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: flex;
  justify-content: center;
  padding: 36px 0;
}

.auth-panel {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  width: min(940px, 100%);
  min-height: 540px;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 8px;
}

.auth-aside {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 52px 46px;
  background: #17181a;
  color: #f5f3ee;
}

.auth-aside::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--red);
}

.aside-brand {
  margin: 0 0 26px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.24em;
  color: #a9a59d;
}

.auth-aside h1 {
  margin: 0 0 18px;
  font-size: clamp(30px, 3.4vw, 38px);
  font-weight: 700;
  line-height: 1.32;
  letter-spacing: -0.01em;
  color: #fff;
}

.aside-note {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: #a9a59d;
}

.perks {
  margin: 30px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
  font-size: 13px;
  color: #c7c3bb;
}

.perks li {
  display: flex;
  align-items: center;
  gap: 10px;
}

.perks li::before {
  content: '';
  width: 5px;
  height: 5px;
  flex-shrink: 0;
  background: var(--red);
}

.auth-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 44px;
}

.auth-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}

.hint {
  margin: 0 0 28px;
  padding-left: 10px;
  border-left: 2px solid var(--line-strong);
  font-size: 12px;
  line-height: 1.7;
  color: var(--ink-3);
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

@media (max-width: 860px) {
  .auth-wrap {
    padding: 8px 0;
  }

  .auth-panel {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .auth-aside {
    display: none;
  }

  .auth-card {
    padding: 34px 24px;
  }
}
</style>

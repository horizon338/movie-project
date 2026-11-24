<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const year = new Date().getFullYear()

onMounted(() => userStore.restore())

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="header-inner">
        <RouterLink to="/" class="brand" aria-label="光影票务首页">
          <span class="brand-mark" aria-hidden="true">票</span>
          <span class="brand-word">光影票务</span>
        </RouterLink>

        <nav class="app-nav" aria-label="主导航">
          <RouterLink to="/" :class="{ active: route.path === '/' }">首页</RouterLink>
          <RouterLink
            v-if="userStore.isLoggedIn"
            to="/orders"
            :class="{ active: route.path.startsWith('/orders') }"
          >
            我的票夹
          </RouterLink>
          <RouterLink
            v-if="userStore.isAdmin"
            to="/admin"
            :class="{ active: route.path.startsWith('/admin') }"
          >
            管理后台
          </RouterLink>
        </nav>

        <div class="app-actions">
          <el-dropdown v-if="userStore.isLoggedIn" trigger="click" @command="handleCommand">
            <button class="user-chip" type="button" aria-label="账户菜单">
              <span class="avatar" aria-hidden="true">{{ userStore.displayName.slice(0, 1) }}</span>
              <span class="user-name">{{ userStore.displayName }}</span>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的票夹</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <template v-else>
            <RouterLink to="/login" class="login-link">登录</RouterLink>
            <RouterLink to="/register" class="register-link">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>

    <footer class="app-footer">
      <div class="footer-inner">
        <span>© {{ year }} 光影票务 · 在线电影票预订</span>
        <span class="footer-note">开场前 2 小时可自助退票 · 未支付订单 30 分钟自动释放</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px 4px 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  font: inherit;
  color: var(--ink);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.user-chip:hover {
  border-color: var(--line);
  background: var(--surface-muted);
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: var(--red);
  background: var(--red-soft);
}

.user-name {
  font-size: 13px;
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chev {
  font-size: 11px;
  color: var(--ink-3);
}

.login-link {
  font-size: 14px;
  color: var(--ink-2);
}

.login-link:hover {
  color: var(--ink);
}

.register-link {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 16px;
  border-radius: 4px;
  background: var(--red);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
  transition: background 0.15s;
}

.register-link:hover {
  background: var(--red-deep);
}

@media (max-width: 560px) {
  .user-name {
    display: none;
  }

  .chev {
    display: none;
  }
}
</style>

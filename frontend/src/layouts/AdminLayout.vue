<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const menus = [
  { path: '/admin', label: '票房统计' },
  { path: '/admin/movies', label: '影片管理' },
  { path: '/admin/cinemas', label: '影院影厅' },
  { path: '/admin/showtimes', label: '排片管理' },
]

const isActive = (menu) =>
  menu.path === '/admin' ? route.path === '/admin' : route.path.startsWith(menu.path)
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <p class="side-title">管理后台</p>
      <nav class="side-nav">
        <RouterLink
          v-for="menu in menus"
          :key="menu.path"
          :to="menu.path"
          :class="{ active: isActive(menu) }"
        >
          {{ menu.label }}
        </RouterLink>
      </nav>
      <RouterLink to="/" class="side-back">← 返回前台</RouterLink>
    </aside>
    <div class="admin-main">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
  padding-top: 8px;
}

.admin-side {
  position: sticky;
  top: 88px;
  padding: 18px 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.side-title {
  margin: 0 0 14px;
  padding-left: 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.2em;
  color: var(--ink-3);
}

.side-nav {
  display: grid;
  gap: 3px;
}

.side-nav a {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--ink-2);
  transition: background 0.15s, color 0.15s;
}

.side-nav a:hover {
  background: var(--surface-muted);
  color: var(--ink);
}

.side-nav a.active {
  position: relative;
  background: var(--red-soft);
  color: var(--red);
  font-weight: 600;
}

.side-nav a.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 2px;
  background: var(--red);
}

.side-back {
  display: block;
  margin-top: 18px;
  padding: 12px 10px 2px;
  border-top: 1px solid var(--line);
  font-size: 12px;
  color: var(--ink-3);
}

.side-back:hover {
  color: var(--ink);
}

.admin-main {
  min-width: 0;
}

@media (max-width: 860px) {
  .admin-shell {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .admin-side {
    position: static;
    padding: 12px;
  }

  .side-title {
    display: none;
  }

  .side-nav {
    display: flex;
    gap: 6px;
    overflow-x: auto;
  }

  .side-nav a {
    flex: 1 0 auto;
    justify-content: center;
    padding: 0 14px;
  }

  .side-nav a.active::before {
    display: none;
  }

  .side-back {
    display: none;
  }
}
</style>

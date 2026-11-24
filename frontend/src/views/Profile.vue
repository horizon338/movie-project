<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { myReviews } from '@/api/reviews'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const reviews = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await myReviews()
    reviews.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="profile-page">
    <div class="profile-card">
      <div class="avatar">{{ userStore.displayName.slice(0, 1) }}</div>
      <div class="info">
        <h1>{{ userStore.displayName }}</h1>
        <p class="meta">
          用户名 {{ userStore.user?.username }} · 手机 {{ userStore.user?.phone }}
        </p>
        <span class="role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</span>
      </div>
      <div class="quick">
        <el-button type="primary" @click="router.push('/orders')">我的票夹</el-button>
        <el-button @click="router.push('/')">去选片</el-button>
      </div>
    </div>

    <div class="section-head">
      <h2>我的评价</h2>
      <span>{{ reviews.length }} 条</span>
    </div>

    <div v-loading="loading" class="review-list">
      <article
        v-for="review in reviews"
        :key="review.id"
        class="review-row"
        role="link"
        tabindex="0"
        :aria-label="`${review.movieTitle}，查看影片`"
        @click="router.push(`/movies/${review.movieId}`)"
        @keyup.enter="router.push(`/movies/${review.movieId}`)"
      >
        <div class="poster">
          <el-image :src="review.posterUrl" fit="cover" class="poster-img">
            <template #placeholder><div class="poster-fallback">{{ review.movieTitle }}</div></template>
            <template #error><div class="poster-fallback">{{ review.movieTitle }}</div></template>
          </el-image>
        </div>
        <div class="body">
          <div class="title-row">
            <h3>{{ review.movieTitle }}</h3>
            <span class="score num">{{ review.rating }} 分</span>
          </div>
          <p class="content">{{ review.content }}</p>
          <p class="time num">{{ dayjs(review.createdAt).format('YYYY.MM.DD HH:mm') }}</p>
        </div>
      </article>

      <el-empty v-if="!loading && !reviews.length" :image-size="88" description="还没有评价，观影后来说两句">
        <el-button type="primary" @click="router.push('/')">去看看热映影片</el-button>
      </el-empty>
    </div>
  </section>
</template>

<style scoped>
.profile-page {
  padding-top: 2px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 26px 28px;
  margin-bottom: 36px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.avatar {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--red-soft);
  color: var(--red);
  font-size: 24px;
  font-weight: 700;
}

.info {
  flex: 1;
  min-width: 0;
}

.info h1 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
}

.meta {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--ink-3);
}

.role {
  display: inline-flex;
  align-items: center;
  height: 21px;
  padding: 0 8px;
  border-radius: 2px;
  background: #f1efea;
  color: var(--ink-3);
  font-size: 11px;
  font-weight: 600;
}

.quick {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.section-head span {
  font-size: 12px;
  color: var(--ink-3);
}

.review-list {
  min-height: 160px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.review-row {
  display: grid;
  grid-template-columns: 60px minmax(0, 1fr);
  gap: 18px;
  padding: 18px 22px;
  border-top: 1px solid var(--line);
  cursor: pointer;
  transition: background 0.15s;
}

.review-row:first-child {
  border-top: 0;
}

.review-row:hover {
  background: var(--surface-muted);
}

.poster {
  width: 60px;
  aspect-ratio: 3 / 4;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: #e9e6df;
}

.poster-img {
  width: 100%;
  height: 100%;
  display: block;
}

.poster-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 6px;
  font-size: 11px;
  color: var(--ink-3);
  text-align: center;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.title-row h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.score {
  font-size: 13px;
  font-weight: 700;
  color: var(--red);
  flex-shrink: 0;
}

.content {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.85;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.time {
  margin: 0;
  font-size: 11px;
  color: var(--ink-3);
}

@media (max-width: 700px) {
  .profile-card {
    flex-wrap: wrap;
    padding: 20px;
  }

  .quick {
    width: 100%;
  }

  .review-row {
    padding: 16px;
  }
}
</style>

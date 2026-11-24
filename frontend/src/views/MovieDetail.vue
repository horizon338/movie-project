<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getMovie, getShowtimes } from '@/api/movies'
import { listCinemas } from '@/api/cinemas'
import { getMyReview, listReviews, submitReview } from '@/api/reviews'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const movie = ref(null)
const cinemas = ref([])
const cinemaId = ref(null)
const showtimes = ref([])
const loading = ref(false)
const movieLoading = ref(true)

const reviews = ref([])
const reviewDialog = ref(false)
const reviewSaving = ref(false)
const myReviewId = ref(null)
const reviewForm = ref({ rating: 9, content: '' })

const weekNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const dateDays = computed(() =>
  Array.from({ length: 7 }, (_, index) => {
    const date = dayjs().add(index, 'day')
    const label = index === 0 ? '今天' : index === 1 ? '明天' : weekNames[date.day()]
    return {
      value: date.format('YYYY-MM-DD'),
      label,
      date: date.format('MM.DD'),
      today: index === 0,
    }
  }),
)

const selectedDate = ref(dayjs().format('YYYY-MM-DD'))

const groupedShowtimes = computed(() => {
  const groups = new Map()
  for (const item of showtimes.value) {
    if (!groups.has(item.cinemaId)) {
      groups.set(item.cinemaId, { id: item.cinemaId, name: item.cinemaName, items: [] })
    }
    groups.get(item.cinemaId).items.push(item)
  }
  return [...groups.values()]
})

const score = computed(() => Number(movie.value?.scoreAvg || 0).toFixed(1))

const loadMovie = async () => {
  movieLoading.value = true
  try {
    const res = await getMovie(route.params.id)
    movie.value = res.data
  } finally {
    movieLoading.value = false
  }
}

const loadCinemas = async () => {
  const res = await listCinemas()
  cinemas.value = res.data
}

const loadShowtimes = async () => {
  loading.value = true
  try {
    const params = { date: selectedDate.value }
    if (cinemaId.value) params.cinemaId = cinemaId.value
    const res = await getShowtimes(route.params.id, params)
    showtimes.value = res.data
  } finally {
    loading.value = false
  }
}

const pickSeat = (item) => {
  if (item.remainingSeats <= 0) return
  router.push(`/showtimes/${item.id}/seats`)
}

const goBack = () => router.push('/')

const scrollToShowtimes = () => {
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  document.getElementById('showtimes')?.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth' })
}

const loadReviews = async () => {
  const res = await listReviews(route.params.id)
  reviews.value = res.data
}

const loadMyReview = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getMyReview(route.params.id)
    myReviewId.value = res.data?.id ?? null
    if (res.data) {
      reviewForm.value = { rating: res.data.rating, content: res.data.content }
    }
  } catch {
    myReviewId.value = null
  }
}

const openReview = async () => {
  if (!userStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  await loadMyReview()
  reviewDialog.value = true
}

const saveReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewSaving.value = true
  try {
    const res = await submitReview({
      movieId: Number(route.params.id),
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    myReviewId.value = res.data.id
    reviewDialog.value = false
    ElMessage.success('评价已提交')
    await Promise.all([loadReviews(), loadMovie()])
  } finally {
    reviewSaving.value = false
  }
}

watch([selectedDate, cinemaId], loadShowtimes)

onMounted(() => {
  loadMovie()
  loadCinemas()
  loadShowtimes()
  loadReviews()
  loadMyReview()
})
</script>

<template>
  <div v-loading="movieLoading" class="detail-page">
    <button class="back-link" type="button" @click="goBack">← 返回影讯</button>

    <section v-if="movie" class="movie-hero">
      <div class="poster-col">
        <el-image :src="movie.posterUrl" fit="cover" class="poster">
          <template #placeholder>
            <div class="poster-fallback">{{ movie.title }}</div>
          </template>
          <template #error>
            <div class="poster-fallback">{{ movie.title }}</div>
          </template>
        </el-image>
      </div>

      <div class="info-col">
        <h1>{{ movie.title }}</h1>

        <div class="head-facts">
          <div class="score-block">
            <span class="score-value num">{{ score }}</span>
            <span class="score-label">观众评分</span>
          </div>
          <div class="facts">
            <span>{{ movie.genre }}</span>
            <span>{{ movie.durationMinutes }} 分钟</span>
            <span>{{ dayjs(movie.releaseDate).format('YYYY.MM.DD') }} 上映</span>
          </div>
        </div>

        <dl class="meta-list">
          <div><dt>导演</dt><dd>{{ movie.director }}</dd></div>
          <div><dt>主演</dt><dd>{{ movie.actors }}</dd></div>
        </dl>

        <p class="synopsis">{{ movie.synopsis }}</p>

        <div class="hero-actions">
          <el-button type="primary" @click="scrollToShowtimes">查看今日场次</el-button>
          <span class="hero-note">在线选座 · 开场前 2 小时可自助退票</span>
        </div>
      </div>
    </section>

    <section id="showtimes" class="showtime-panel">
      <div class="section-head">
        <div>
          <h2>排片购票</h2>
          <p>选择日期与影院，查看当日可售场次</p>
        </div>
        <el-select v-model="cinemaId" placeholder="全部影院" clearable class="cinema-select">
          <el-option v-for="item in cinemas" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </div>

      <div class="date-strip">
        <button
          v-for="day in dateDays"
          :key="day.value"
          class="date-cell"
          :class="{ active: selectedDate === day.value, today: day.today }"
          type="button"
          @click="selectedDate = day.value"
        >
          <span class="day">{{ day.label }}</span>
          <span class="date num">{{ day.date }}</span>
        </button>
      </div>

      <div v-loading="loading" class="showtime-body">
        <div v-for="group in groupedShowtimes" :key="group.id" class="cinema-block">
          <div class="cinema-head">
            <h3>{{ group.name }}</h3>
            <span>{{ group.items.length }} 个场次</span>
          </div>

          <div class="showtime-grid">
            <button
              v-for="item in group.items"
              :key="item.id"
              class="showtime-ticket"
              type="button"
              :disabled="item.remainingSeats <= 0"
              @click="pickSeat(item)"
            >
              <span class="stub-time">
                <span class="time num">{{ dayjs(item.startTime).format('HH:mm') }}</span>
                <span class="end num">{{ dayjs(item.endTime).format('HH:mm') }} 散场</span>
              </span>
              <span class="stub-divider" aria-hidden="true"></span>
              <span class="stub-info">
                <span class="hall">{{ item.hallName }}</span>
                <span class="seats" :class="{ tight: item.remainingSeats <= 10 }">
                  {{ item.remainingSeats > 0 ? `余 ${item.remainingSeats} 座` : '已售罄' }}
                </span>
              </span>
              <span class="stub-price">
                <span class="price num">¥{{ Number(item.price).toFixed(0) }}</span>
                <span class="pick">{{ item.remainingSeats > 0 ? '选座' : '售罄' }}</span>
              </span>
            </button>
          </div>
        </div>

        <el-empty
          v-if="!loading && groupedShowtimes.length === 0"
          :image-size="84"
          description="当日暂无可售场次，试试其他日期"
        />
      </div>
    </section>

    <section class="reviews-section">
      <div class="section-head">
        <div>
          <h2>观众评价</h2>
          <p>{{ reviews.length }} 条评价</p>
        </div>
        <el-button type="primary" plain @click="openReview">
          {{ myReviewId ? '修改我的评价' : '写评价' }}
        </el-button>
      </div>

      <div v-if="reviews.length" class="review-list">
        <article v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-avatar">{{ (review.nickname || '匿').slice(0, 1) }}</div>
          <div class="review-body">
            <div class="review-top">
              <span class="nickname">{{ review.nickname }}</span>
              <span v-if="review.mine" class="mine-tag">我的</span>
              <el-rate :model-value="review.rating / 2" disabled allow-half size="small" />
              <span class="rating-value num">{{ review.rating }} 分</span>
              <span class="review-time num">{{ dayjs(review.createdAt).format('YYYY.MM.DD HH:mm') }}</span>
            </div>
            <p class="review-content">{{ review.content }}</p>
          </div>
        </article>
      </div>
      <el-empty v-else :image-size="84" description="暂无评价，购票观影后来聊两句" />
    </section>

    <el-dialog v-model="reviewDialog" :title="myReviewId ? '修改评价' : '写评价'" width="520px">
      <div class="rating-row">
        <span class="rating-label">评分</span>
        <el-rate v-model="reviewForm.rating" :max="10" show-score score-template="{value} 分" />
      </div>
      <el-input
        v-model="reviewForm.content"
        type="textarea"
        :rows="5"
        maxlength="500"
        show-word-limit
        placeholder="说说这部影片的剧情、画面或表演…"
      />
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="reviewSaving" @click="saveReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page {
  padding-top: 2px;
}

.back-link {
  margin-bottom: 22px;
}

/* ---------- film header ---------- */

.movie-hero {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 44px;
  align-items: stretch;
  padding-bottom: 40px;
  border-bottom: 1px solid var(--line);
}

.poster-col {
  aspect-ratio: 3 / 4;
  border-radius: var(--radius);
  overflow: hidden;
  background: #e9e6df;
  box-shadow: 0 14px 34px rgba(23, 24, 26, 0.14);
}

.poster {
  width: 100%;
  height: 100%;
  display: block;
}

.poster-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 20px;
  text-align: center;
  font-size: 15px;
  color: var(--ink-3);
  background: #e9e6df;
}

.info-col h1 {
  margin: 4px 0 20px;
  font-size: clamp(28px, 3.4vw, 40px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.head-facts {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.score-block {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.score-value {
  font-size: 38px;
  font-weight: 700;
  line-height: 1;
  color: var(--red);
}

.score-label {
  font-size: 12px;
  color: var(--ink-3);
}

.facts {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 16px;
  font-size: 13px;
  color: var(--ink-2);
}

.meta-list {
  margin: 20px 0 18px;
  display: grid;
  gap: 8px;
  font-size: 13px;
}

.meta-list div {
  display: flex;
  gap: 18px;
}

.meta-list dt {
  width: 38px;
  flex-shrink: 0;
  color: var(--ink-3);
}

.meta-list dd {
  margin: 0;
  color: var(--ink-2);
}

.synopsis {
  max-width: 720px;
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: var(--ink-2);
}

.info-col {
  display: flex;
  flex-direction: column;
}

.hero-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: auto;
  padding-top: 22px;
}

.hero-note {
  font-size: 12px;
  color: var(--ink-3);
}

/* ---------- showtimes ---------- */

.showtime-panel {
  margin-top: 36px;
  padding: 24px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.section-head p {
  margin: 5px 0 0;
  font-size: 12px;
  color: var(--ink-3);
}

.cinema-select {
  width: 220px;
}

.date-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 20px 0 4px;
}

.date-cell {
  flex: 0 0 auto;
  min-width: 86px;
  display: grid;
  gap: 2px;
  justify-items: center;
  padding: 9px 14px 8px;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink);
  font: inherit;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.date-cell:hover {
  border-color: var(--ink-3);
}

.date-cell .day {
  font-size: 14px;
  font-weight: 600;
}

.date-cell .date {
  font-size: 11px;
  color: var(--ink-3);
}

.date-cell.today:not(.active) .day {
  color: var(--red);
}

.date-cell.active {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

.date-cell.active .date {
  color: rgba(255, 255, 255, 0.62);
}

.showtime-body {
  min-height: 90px;
  margin-top: 12px;
}

.cinema-block {
  padding: 18px 0 4px;
  border-top: 1px solid var(--line);
}

.cinema-block:first-child {
  border-top: 0;
  padding-top: 6px;
}

.cinema-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 13px;
}

.cinema-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.cinema-head span {
  font-size: 12px;
  color: var(--ink-3);
}

.showtime-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(218px, 1fr));
  gap: 10px;
}

/* ticket-stub signature */

.showtime-ticket {
  display: grid;
  grid-template-columns: auto 1px minmax(0, 1fr) auto;
  align-items: stretch;
  padding: 0;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink);
  font: inherit;
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.15s;
}

.showtime-ticket:hover:not(:disabled) {
  border-color: var(--ink);
}

.showtime-ticket:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: var(--surface-muted);
}

.stub-time {
  display: grid;
  gap: 2px;
  align-content: center;
  padding: 12px 14px;
}

.stub-time .time {
  font-size: 19px;
  font-weight: 700;
  line-height: 1.05;
}

.stub-time .end {
  font-size: 10px;
  color: var(--ink-3);
}

.stub-divider {
  position: relative;
  width: 1px;
  background: transparent;
  border-left: 1px dashed var(--line-strong);
}

.stub-divider::before,
.stub-divider::after {
  content: '';
  position: absolute;
  left: -5px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--surface);
  border: 1px solid var(--line);
}

.stub-divider::before {
  top: -5px;
}

.stub-divider::after {
  bottom: -5px;
}

.showtime-ticket:disabled .stub-divider::before,
.showtime-ticket:disabled .stub-divider::after {
  background: var(--surface-muted);
}

.stub-info {
  display: grid;
  gap: 2px;
  align-content: center;
  padding: 12px 12px;
  min-width: 0;
}

.stub-info .hall {
  font-size: 12px;
  color: var(--ink-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stub-info .seats {
  font-size: 11px;
  color: var(--ink-3);
}

.stub-info .seats.tight {
  color: var(--amber);
}

.stub-price {
  display: grid;
  gap: 1px;
  align-content: center;
  justify-items: end;
  padding: 12px 14px 12px 10px;
}

.stub-price .price {
  font-size: 16px;
  font-weight: 700;
  color: var(--red);
  line-height: 1.2;
}

.stub-price .pick {
  font-size: 11px;
  color: var(--ink-2);
}

.showtime-ticket:hover:not(:disabled) .pick {
  color: var(--red);
}

/* ---------- reviews ---------- */

.reviews-section {
  margin-top: 42px;
}

.reviews-section .section-head {
  margin-bottom: 18px;
}

.review-list {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.review-item {
  display: flex;
  gap: 14px;
  padding: 20px 24px;
  border-top: 1px solid var(--line);
}

.review-item:first-child {
  border-top: 0;
}

.review-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #f1efea;
  color: var(--ink-2);
  font-size: 14px;
  font-weight: 600;
}

.review-body {
  flex: 1;
  min-width: 0;
}

.review-top {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 10px;
  margin-bottom: 7px;
}

.nickname {
  font-size: 13px;
  font-weight: 600;
}

.mine-tag {
  padding: 1px 6px;
  border-radius: 2px;
  background: var(--red-soft);
  color: var(--red);
  font-size: 10px;
  font-weight: 600;
}

.rating-value {
  font-size: 12px;
  color: var(--red);
}

.review-time {
  margin-left: auto;
  font-size: 11px;
  color: var(--ink-3);
}

.review-content {
  margin: 0;
  font-size: 13px;
  line-height: 1.9;
  color: var(--ink-2);
  white-space: pre-wrap;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.rating-label {
  font-size: 13px;
  color: var(--ink-2);
}

/* ---------- responsive ---------- */

@media (max-width: 880px) {
  .movie-hero {
    grid-template-columns: 180px minmax(0, 1fr);
    gap: 24px;
  }

  .section-head {
    align-items: flex-start;
  }

  .cinema-select {
    width: 170px;
  }
}

@media (max-width: 640px) {
  .movie-hero {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .poster-col {
    width: 160px;
  }

  .info-col h1 {
    font-size: 28px;
  }

  .score-value {
    font-size: 32px;
  }

  .showtime-panel {
    padding: 18px 14px;
  }

  .section-head {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .cinema-select {
    width: 100%;
  }

  .showtime-grid {
    grid-template-columns: 1fr;
  }

  .review-item {
    padding: 16px 14px;
  }

  .review-time {
    width: 100%;
    margin-left: 0;
  }

  .reviews-section .section-head .el-button {
    align-self: flex-start;
  }
}
</style>

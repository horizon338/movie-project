<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { Search } from '@element-plus/icons-vue'
import { getMovie, listMovies } from '@/api/movies'
import { listCinemas } from '@/api/cinemas'

const router = useRouter()

const keyword = ref('')
const releaseRange = ref([])
const cinemaId = ref(null)
const cinemas = ref([])

const movies = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 12
const loading = ref(false)

const isFiltered = computed(
  () => Boolean(keyword.value.trim()) || releaseRange.value?.length === 2 || Boolean(cinemaId.value),
)

const featured = computed(() => (page.value === 1 && !isFiltered.value ? movies.value[0] : null))

const heroDetail = ref(null)

const score = (value) => Number(value || 0).toFixed(1)

watch(
  () => featured.value?.id,
  async (id) => {
    heroDetail.value = null
    if (!id) return
    try {
      const res = await getMovie(id)
      if (featured.value?.id === id) heroDetail.value = res.data
    } catch {
      heroDetail.value = null
    }
  },
  { immediate: true },
)

const loadCinemas = async () => {
  const res = await listCinemas()
  cinemas.value = res.data
}

const loadMovies = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (releaseRange.value?.length === 2) {
      params.releaseFrom = releaseRange.value[0]
      params.releaseTo = releaseRange.value[1]
    }
    if (cinemaId.value) params.cinemaId = cinemaId.value

    const res = await listMovies(params)
    movies.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  loadMovies()
}

const reset = () => {
  keyword.value = ''
  releaseRange.value = []
  cinemaId.value = null
  page.value = 1
  loadMovies()
}

const changePage = (value) => {
  page.value = value
  loadMovies()
}

const openDetail = (id) => router.push(`/movies/${id}`)

const scrollToCatalog = () => {
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  document.getElementById('catalog')?.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth' })
}

watch([releaseRange, cinemaId], () => search())

onMounted(() => {
  loadCinemas()
  loadMovies()
})
</script>

<template>
  <div class="home">
    <section v-if="featured" class="featured" aria-label="今日推荐影片">
      <div class="featured-copy">
        <p class="featured-eyebrow">正在热映 · {{ dayjs().format('M月D日') }}</p>
        <h1>{{ featured.title }}</h1>
        <div class="featured-meta">
          <span class="featured-score num">{{ score(featured.scoreAvg) }}</span>
          <span class="featured-score-label">观众评分</span>
          <span class="sep" aria-hidden="true"></span>
          <span>{{ featured.genre }}</span>
          <span class="sep" aria-hidden="true"></span>
          <span>{{ featured.durationMinutes }} 分钟</span>
          <span class="sep" aria-hidden="true"></span>
          <span>{{ dayjs(featured.releaseDate).format('MM.DD') }} 上映</span>
        </div>
        <p v-if="heroDetail?.synopsis" class="featured-synopsis">{{ heroDetail.synopsis }}</p>
        <div class="featured-actions">
          <el-button type="primary" size="large" @click="openDetail(featured.id)">
            选座购票
          </el-button>
          <button class="all-films" type="button" @click="scrollToCatalog">浏览全部影片</button>
        </div>
      </div>

      <div class="featured-media">
        <el-image :src="featured.posterUrl" fit="cover" class="featured-img">
          <template #placeholder>
            <div class="featured-fallback">{{ featured.title }}</div>
          </template>
          <template #error>
            <div class="featured-fallback">{{ featured.title }}</div>
          </template>
        </el-image>
      </div>
    </section>

    <section id="catalog" class="catalog">
      <div class="catalog-head">
        <h2 class="catalog-title">{{ isFiltered ? '筛选结果' : '正在热映' }}</h2>
        <span class="count">共 {{ total }} 部</span>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索片名或主演"
          clearable
          class="filter-search"
          @keyup.enter="search"
          @clear="search"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-date-picker
          v-model="releaseRange"
          type="daterange"
          range-separator="至"
          start-placeholder="上映开始"
          end-placeholder="上映结束"
          value-format="YYYY-MM-DD"
          class="filter-date"
        />
        <el-select v-model="cinemaId" placeholder="全部影院" clearable class="filter-cinema">
          <el-option v-for="item in cinemas" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button type="primary" @click="search">搜索</el-button>
        <button v-if="isFiltered" class="reset-link" type="button" @click="reset">清除筛选</button>
      </div>

      <div v-if="loading" class="movie-grid" aria-hidden="true">
        <div v-for="n in 8" :key="n" class="sk-card">
          <div class="sk-poster"></div>
          <div class="sk-line w70"></div>
          <div class="sk-line w40"></div>
        </div>
      </div>

      <div v-else class="movie-grid">
        <article
          v-for="movie in movies"
          :key="movie.id"
          class="movie-card"
          role="link"
          tabindex="0"
          :aria-label="`${movie.title}，查看场次`"
          @click="openDetail(movie.id)"
          @keyup.enter="openDetail(movie.id)"
        >
          <div class="poster-frame">
            <el-image :src="movie.posterUrl" fit="cover" class="poster" lazy>
              <template #placeholder>
                <div class="poster-fallback">{{ movie.title }}</div>
              </template>
              <template #error>
                <div class="poster-fallback">{{ movie.title }}</div>
              </template>
            </el-image>
          </div>
          <div class="movie-info">
            <div class="title-row">
              <h3>{{ movie.title }}</h3>
              <span v-if="Number(movie.scoreAvg) > 0" class="score num">
                {{ score(movie.scoreAvg) }}
              </span>
            </div>
            <p class="movie-meta">{{ movie.genre }} · {{ movie.durationMinutes }} 分钟</p>
            <p class="movie-release">{{ dayjs(movie.releaseDate).format('MM.DD') }} 上映</p>
          </div>
        </article>
      </div>

      <el-empty
        v-if="!loading && movies.length === 0"
        :image-size="96"
        description="没有找到符合条件的影片，换个关键词或影院试试"
      />

      <div v-if="total > pageSize" class="pager">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          background
          @current-change="changePage"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  padding-top: 4px;
}

/* ---------- featured film ---------- */

.featured {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
  min-height: 420px;
  margin-bottom: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #121316;
}

.featured-copy {
  position: relative;
  z-index: 1;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #f5f3ee;
}

.featured-eyebrow {
  margin: 0 0 16px;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #c5c1b9;
}

.featured-copy h1 {
  margin: 0 0 18px;
  font-size: clamp(30px, 3.8vw, 46px);
  font-weight: 700;
  line-height: 1.12;
  letter-spacing: -0.02em;
  color: #fff;
}

.featured-meta {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 9px;
  font-size: 13px;
  color: #b7b3ab;
}

.featured-score {
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
  color: #fff;
}

.featured-score-label {
  margin-right: 4px;
}

.sep {
  width: 1px;
  height: 11px;
  background: #3b3c40;
  align-self: center;
}

.featured-synopsis {
  max-width: 520px;
  margin: 22px 0 30px;
  font-size: 14px;
  line-height: 1.85;
  color: #b3afa7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-actions {
  display: flex;
  align-items: center;
  gap: 22px;
}

.all-films {
  padding: 0 0 3px;
  border: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.35);
  background: none;
  color: #d9d5cd;
  font: inherit;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}

.all-films:hover {
  color: #fff;
  border-color: #fff;
}

.featured-media {
  position: relative;
  min-height: 100%;
  overflow: hidden;
}

.featured-media::after {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(90deg, #121316 0%, rgba(18, 19, 22, 0) 34%);
  pointer-events: none;
}

.featured-img {
  position: absolute;
  inset: 0;
  display: block;
  width: 100%;
  height: 100%;
}

.featured-img :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.featured-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #55575c;
  font-size: 14px;
  background: #1b1c20;
}

/* ---------- catalog ---------- */

.catalog-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 18px;
}

.catalog-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.count {
  font-size: 13px;
  color: var(--ink-3);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px;
  margin-bottom: 28px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.filter-search {
  width: 260px;
}

.filter-date {
  width: 270px !important;
}

.filter-cinema {
  width: 200px;
}

.search-icon {
  color: var(--ink-3);
}

.reset-link {
  padding: 0 4px;
  border: 0;
  background: none;
  color: var(--ink-3);
  font: inherit;
  font-size: 13px;
  cursor: pointer;
}

.reset-link:hover {
  color: var(--ink);
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(172px, 1fr));
  gap: 30px 20px;
  min-height: 200px;
}

.movie-card {
  cursor: pointer;
}

.poster-frame {
  position: relative;
  aspect-ratio: 3 / 4;
  border-radius: var(--radius);
  overflow: hidden;
  background: #e9e6df;
  box-shadow: 0 1px 2px rgba(23, 24, 26, 0.06);
}

.poster {
  display: block;
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.2, 0.6, 0.2, 1);
}

.movie-card:hover .poster {
  transform: scale(1.03);
}

.poster-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 16px;
  text-align: center;
  font-size: 14px;
  color: var(--ink-3);
  background: #e9e6df;
}

.movie-info {
  padding: 12px 2px 0;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.title-row h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.15s;
}

.movie-card:hover .title-row h3,
.movie-card:focus-visible .title-row h3 {
  color: var(--red);
}

.score {
  flex-shrink: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--red);
}

.movie-meta,
.movie-release {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---------- skeleton ---------- */

.sk-poster {
  aspect-ratio: 3 / 4;
  border-radius: var(--radius);
  background: #e9e6df;
  animation: pulse 1.4s ease-in-out infinite;
}

.sk-line {
  height: 11px;
  margin-top: 11px;
  border-radius: 2px;
  background: #e9e6df;
  animation: pulse 1.4s ease-in-out infinite;
}

.w70 {
  width: 70%;
}

.w40 {
  width: 40%;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.55;
  }
  50% {
    opacity: 1;
  }
}

/* ---------- responsive ---------- */

@media (max-width: 880px) {
  .featured {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .featured-media {
    order: -1;
    height: 220px;
    min-height: 0;
  }

  .featured-img {
    position: absolute;
    inset: 0;
  }

  .featured-media::after {
    background: linear-gradient(180deg, rgba(18, 19, 22, 0) 38%, #121316 100%);
  }

  .featured-copy {
    padding: 28px 24px 34px;
  }

  .featured-copy h1 {
    font-size: 30px;
  }

  .featured-synopsis {
    margin: 18px 0 24px;
    -webkit-line-clamp: 2;
  }

  .filter-search,
  .filter-date,
  .filter-cinema {
    width: 100% !important;
  }
}

@media (max-width: 560px) {
  .featured {
    margin-bottom: 34px;
  }

  .featured-score {
    font-size: 28px;
  }

  .featured-meta .sep {
    display: none;
  }

  .movie-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 22px 14px;
  }

  .catalog-title {
    font-size: 21px;
  }
}
</style>

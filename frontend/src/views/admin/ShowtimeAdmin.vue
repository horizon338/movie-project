<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  adminCreateShowtime,
  adminListCinemas,
  adminListHalls,
  adminListMovies,
  adminListShowtimes,
} from '@/api/admin'

const loading = ref(false)
const showtimes = ref([])
const movies = ref([])
const cinemas = ref([])
const halls = ref([])

const filters = reactive({
  movieId: null,
  cinemaId: null,
  date: dayjs().format('YYYY-MM-DD'),
})

const dialog = ref(false)
const saving = ref(false)
const form = reactive({
  movieId: null,
  cinemaId: null,
  hallId: null,
  startTime: '',
  price: 45,
})

const loadOptions = async () => {
  const [movieRes, cinemaRes] = await Promise.all([
    adminListMovies({ page: 1, size: 50 }),
    adminListCinemas(),
  ])
  movies.value = movieRes.data.records
  cinemas.value = cinemaRes.data
}

const load = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.movieId) params.movieId = filters.movieId
    if (filters.cinemaId) params.cinemaId = filters.cinemaId
    if (filters.date) params.date = filters.date
    const res = await adminListShowtimes(params)
    showtimes.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  Object.assign(form, {
    movieId: filters.movieId,
    cinemaId: filters.cinemaId,
    hallId: null,
    startTime: filters.date ? `${filters.date}T10:00:00` : '',
    price: 45,
  })
  halls.value = []
  dialog.value = true
}

watch(
  () => form.cinemaId,
  async (cinemaId) => {
    form.hallId = null
    if (!cinemaId) {
      halls.value = []
      return
    }
    const res = await adminListHalls(cinemaId)
    halls.value = res.data
  },
)

const submit = async () => {
  if (!form.movieId || !form.hallId || !form.startTime) {
    ElMessage.warning('请选择影片、影厅和开场时间')
    return
  }
  saving.value = true
  try {
    await adminCreateShowtime({
      movieId: form.movieId,
      hallId: form.hallId,
      startTime: form.startTime,
      price: form.price,
    })
    ElMessage.success('排片成功，座位已自动生成')
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const disabledDate = (date) => date < new Date(dayjs().format('YYYY-MM-DD'))

onMounted(async () => {
  await loadOptions()
  await load()
})
</script>

<template>
  <section class="showtime-admin">
    <div class="page-head">
      <h1>排片管理</h1>
      <el-button type="primary" @click="openCreate">新增排片</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="filters.movieId" placeholder="全部影片" clearable class="filter">
        <el-option v-for="movie in movies" :key="movie.id" :label="movie.title" :value="movie.id" />
      </el-select>
      <el-select v-model="filters.cinemaId" placeholder="全部影院" clearable class="filter">
        <el-option v-for="cinema in cinemas" :key="cinema.id" :label="cinema.name" :value="cinema.id" />
      </el-select>
      <el-date-picker
        v-model="filters.date"
        type="date"
        value-format="YYYY-MM-DD"
        placeholder="全部日期"
        class="filter"
        style="width: 160px"
      />
      <el-button @click="load">查询</el-button>
      <span class="muted">{{ showtimes.length }} 个场次</span>
    </div>

    <el-table v-loading="loading" :data="showtimes" empty-text="暂无排片" class="table">
      <el-table-column prop="movieTitle" label="影片" min-width="140" />
      <el-table-column label="影院 / 影厅" min-width="220">
        <template #default="{ row }">{{ row.cinemaName }} · {{ row.hallName }}</template>
      </el-table-column>
      <el-table-column label="放映时间" width="210">
        <template #default="{ row }">
          {{ dayjs(row.startTime).format('MM-DD HH:mm') }} - {{ dayjs(row.endTime).format('HH:mm') }}
        </template>
      </el-table-column>
      <el-table-column label="票价" width="90">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(0) }}</template>
      </el-table-column>
      <el-table-column label="已售 / 总座位" width="120">
        <template #default="{ row }">{{ row.soldSeats }} / {{ row.totalSeats }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增排片" width="540px">
      <el-form :model="form" label-position="top">
        <el-form-item label="影片">
          <el-select v-model="form.movieId" placeholder="请选择影片" class="full">
            <el-option v-for="movie in movies" :key="movie.id" :label="movie.title" :value="movie.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影院">
          <el-select v-model="form.cinemaId" placeholder="请选择影院" class="full">
            <el-option v-for="cinema in cinemas" :key="cinema.id" :label="cinema.name" :value="cinema.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影厅">
          <el-select v-model="form.hallId" placeholder="请先选择影院" class="full" :disabled="!form.cinemaId">
            <el-option
              v-for="hall in halls"
              :key="hall.id"
              :label="`${hall.name}（${hall.seatCount} 座）`"
              :value="hall.id"
            />
          </el-select>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="开场时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="选择开场时间"
              :disabled-date="disabledDate"
            />
          </el-form-item>
          <el-form-item label="票价（元）">
            <el-input-number v-model="form.price" :min="1" :max="999" :precision="2" controls-position="right" />
          </el-form-item>
        </div>
        <p class="muted tip">保存后将按影厅座位布局自动生成座位</p>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存排片</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.table {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter {
  width: 200px;
}

.toolbar .muted {
  font-size: 12px;
}

.full {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.tip {
  margin: 0;
  font-size: 12px;
}

@media (max-width: 640px) {
  .filter {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

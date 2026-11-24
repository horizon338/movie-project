<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { adminCreateMovie, adminListMovies, adminUpdateMovie, adminUpload } from '@/api/admin'

const loading = ref(false)
const keyword = ref('')
const movies = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 8

const dialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref()

const emptyForm = {
  title: '',
  posterUrl: '',
  durationMinutes: 120,
  genre: '',
  director: '',
  actors: '',
  synopsis: '',
  releaseDate: '',
  offShelfDate: '',
  status: 1,
}
const form = reactive({ ...emptyForm })

const rules = {
  title: [{ required: true, message: '请输入片名', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请输入片长', trigger: 'blur' }],
  releaseDate: [{ required: true, message: '请选择上映日期', trigger: 'change' }],
}

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const res = await adminListMovies(params)
    movies.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  load()
}

const openCreate = () => {
  Object.assign(form, emptyForm)
  editingId.value = null
  dialog.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    title: row.title,
    posterUrl: row.posterUrl || '',
    durationMinutes: row.durationMinutes,
    genre: row.genre || '',
    director: row.director || '',
    actors: row.actors || '',
    synopsis: row.synopsis || '',
    releaseDate: row.releaseDate,
    offShelfDate: row.offShelfDate || '',
    status: row.status,
  })
  editingId.value = row.id
  dialog.value = true
}

const beforePosterUpload = (file) => {
  if (!/image\/(jpeg|png|webp)/.test(file.type)) {
    ElMessage.warning('仅支持 jpg / png / webp 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 5MB')
    return false
  }
  return true
}

const uploadPoster = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  const res = await adminUpload(formData)
  form.posterUrl = res.data.url
  ElMessage.success('海报已上传')
}

const submit = async () => {
  if (!(await formRef.value.validate().catch(() => false))) return
  saving.value = true
  try {
    const payload = {
      ...form,
      offShelfDate: form.offShelfDate || null,
      posterUrl: form.posterUrl || null,
    }
    if (editingId.value) {
      await adminUpdateMovie(editingId.value, payload)
      ElMessage.success('影片已更新')
    } else {
      await adminCreateMovie(payload)
      ElMessage.success('影片已创建')
      page.value = 1
    }
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="movie-admin">
    <div class="page-head">
      <h1>影片管理</h1>
      <el-button type="primary" @click="openCreate">录入新影片</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索片名"
        clearable
        class="search"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button @click="search">搜索</el-button>
      <span class="muted">共 {{ total }} 部</span>
    </div>

    <el-table v-loading="loading" :data="movies" class="table">
      <el-table-column label="海报" width="76">
        <template #default="{ row }">
          <el-image :src="row.posterUrl" fit="cover" class="thumb">
            <template #error><div class="thumb-fallback">{{ row.title?.slice(0, 2) }}</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="片名" min-width="140" />
      <el-table-column prop="genre" label="类型" width="120" />
      <el-table-column prop="durationMinutes" label="片长" width="80">
        <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
      </el-table-column>
      <el-table-column label="上映 / 下架" width="190">
        <template #default="{ row }">
          {{ row.releaseDate }} ~ {{ row.offShelfDate || '长期' }}
        </template>
      </el-table-column>
      <el-table-column prop="scoreAvg" label="评分" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > pageSize" class="pager">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        background
        @current-change="(value) => { page = value; load() }"
      />
    </div>

    <el-dialog v-model="dialog" :title="editingId ? '编辑影片' : '录入新影片'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="片名" prop="title">
            <el-input v-model="form.title" placeholder="请输入片名" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="form.genre" placeholder="如：科幻 / 冒险" />
          </el-form-item>
          <el-form-item label="片长（分钟）" prop="durationMinutes">
            <el-input-number v-model="form.durationMinutes" :min="1" :max="600" controls-position="right" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
          </el-form-item>
          <el-form-item label="导演">
            <el-input v-model="form.director" placeholder="导演" />
          </el-form-item>
          <el-form-item label="主演">
            <el-input v-model="form.actors" placeholder="主演，用 / 分隔" />
          </el-form-item>
          <el-form-item label="上映日期" prop="releaseDate">
            <el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" placeholder="上映日期" />
          </el-form-item>
          <el-form-item label="下架日期">
            <el-date-picker v-model="form.offShelfDate" type="date" value-format="YYYY-MM-DD" placeholder="不填表示长期" />
          </el-form-item>
        </div>

        <el-form-item label="剧情简介">
          <el-input v-model="form.synopsis" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>

        <el-form-item label="海报">
          <div class="poster-row">
            <el-upload
              :show-file-list="false"
              :http-request="uploadPoster"
              :before-upload="beforePosterUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="poster-box">
                <img v-if="form.posterUrl" :src="form.posterUrl" alt="海报" />
                <span v-else>点击上传</span>
              </div>
            </el-upload>
            <div class="poster-tip muted">
              支持 jpg / png / webp，小于 5MB<br />
              也可直接粘贴图片地址
              <el-input v-model="form.posterUrl" placeholder="https://..." size="small" class="poster-input" />
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar .search {
  width: 260px;
}

.toolbar .muted {
  font-size: 12px;
}

.table {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.thumb {
  width: 44px;
  height: 60px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: #e9e6df;
}

.thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--ink-3);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 18px;
}

.poster-row {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.poster-box {
  width: 96px;
  height: 128px;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--line-strong);
  background: var(--surface-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  color: var(--ink-3);
  font-size: 12px;
}

.poster-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.poster-tip {
  font-size: 12px;
  line-height: 1.9;
}

.poster-input {
  margin-top: 8px;
  width: 260px;
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

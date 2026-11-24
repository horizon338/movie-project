<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminCreateCinema,
  adminCreateHall,
  adminListCinemas,
  adminListHalls,
  adminUpdateCinema,
} from '@/api/admin'

const cinemas = ref([])
const halls = ref([])
const selected = ref(null)
const loading = ref(false)

const cinemaDialog = ref(false)
const cinemaSaving = ref(false)
const editingCinemaId = ref(null)
const cinemaForm = reactive({ name: '', address: '', phone: '', status: 1 })

const hallDialog = ref(false)
const hallSaving = ref(false)
const hallForm = reactive({ name: '', rowCount: 8, colCount: 12 })

const loadCinemas = async () => {
  loading.value = true
  try {
    const res = await adminListCinemas()
    cinemas.value = res.data
    if (selected.value) {
      selected.value = cinemas.value.find((item) => item.id === selected.value.id) || null
      if (selected.value) await loadHalls(selected.value.id)
    }
  } finally {
    loading.value = false
  }
}

const loadHalls = async (cinemaId) => {
  const res = await adminListHalls(cinemaId)
  halls.value = res.data
}

const selectCinema = async (row) => {
  selected.value = row
  halls.value = []
  if (row) {
    await loadHalls(row.id)
  }
}

const openCreateCinema = () => {
  Object.assign(cinemaForm, { name: '', address: '', phone: '', status: 1 })
  editingCinemaId.value = null
  cinemaDialog.value = true
}

const openEditCinema = (row) => {
  Object.assign(cinemaForm, {
    name: row.name,
    address: row.address || '',
    phone: row.phone || '',
    status: 1,
  })
  editingCinemaId.value = row.id
  cinemaDialog.value = true
}

const submitCinema = async () => {
  if (!cinemaForm.name.trim()) {
    ElMessage.warning('请输入影院名称')
    return
  }
  cinemaSaving.value = true
  try {
    if (editingCinemaId.value) {
      await adminUpdateCinema(editingCinemaId.value, { ...cinemaForm })
      ElMessage.success('影院已更新')
    } else {
      await adminCreateCinema({ ...cinemaForm })
      ElMessage.success('影院已创建')
    }
    cinemaDialog.value = false
    await loadCinemas()
  } finally {
    cinemaSaving.value = false
  }
}

const openHall = () => {
  if (!selected.value) return
  Object.assign(hallForm, { name: '', rowCount: 8, colCount: 12 })
  hallDialog.value = true
}

const submitHall = async () => {
  if (!hallForm.name.trim()) {
    ElMessage.warning('请输入影厅名称')
    return
  }
  hallSaving.value = true
  try {
    await adminCreateHall({ cinemaId: selected.value.id, ...hallForm })
    ElMessage.success('影厅已创建')
    hallDialog.value = false
    await loadHalls(selected.value.id)
    await loadCinemas()
  } finally {
    hallSaving.value = false
  }
}

onMounted(loadCinemas)
</script>

<template>
  <section class="cinema-admin">
    <div class="page-head">
      <h1>影院与影厅</h1>
    </div>

    <div class="panels">
      <div class="panel">
        <div class="panel-head">
          <h3>影院</h3>
          <el-button type="primary" size="small" @click="openCreateCinema">新增影院</el-button>
        </div>
        <el-table
          v-loading="loading"
          :data="cinemas"
          size="small"
          highlight-current-row
          @current-change="selectCinema"
        >
          <el-table-column prop="name" label="影院名称" min-width="160" />
          <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
          <el-table-column prop="phone" label="电话" width="120" />
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEditCinema(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>{{ selected ? `${selected.name} · 影厅` : '影厅' }}</h3>
          <el-button type="primary" size="small" :disabled="!selected" @click="openHall">
            新增影厅
          </el-button>
        </div>
        <el-table v-if="selected" :data="halls" size="small" empty-text="该影院暂无影厅">
          <el-table-column prop="name" label="影厅" min-width="140" />
          <el-table-column label="座位布局" width="120">
            <template #default="{ row }">{{ row.rowCount }} 排 × {{ row.colCount }} 列</template>
          </el-table-column>
          <el-table-column prop="seatCount" label="座位数" width="90" />
        </el-table>
        <el-empty v-else description="请选择左侧影院查看影厅" />
      </div>
    </div>

    <el-dialog v-model="cinemaDialog" :title="editingCinemaId ? '编辑影院' : '新增影院'" width="480px">
      <el-form :model="cinemaForm" label-position="top">
        <el-form-item label="影院名称">
          <el-input v-model="cinemaForm.name" placeholder="如：星光国际影城（万象城店）" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="cinemaForm.address" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="cinemaForm.phone" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cinemaDialog = false">取消</el-button>
        <el-button type="primary" :loading="cinemaSaving" @click="submitCinema">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="hallDialog" title="新增影厅" width="460px">
      <el-form :model="hallForm" label-position="top">
        <el-form-item label="影厅名称">
          <el-input v-model="hallForm.name" placeholder="如：3号厅（情侣座）" />
        </el-form-item>
        <div class="hall-grid">
          <el-form-item label="排数">
            <el-input-number v-model="hallForm.rowCount" :min="1" :max="30" controls-position="right" />
          </el-form-item>
          <el-form-item label="列数">
            <el-input-number v-model="hallForm.colCount" :min="1" :max="40" controls-position="right" />
          </el-form-item>
        </div>
        <p class="muted seat-tip">共 {{ hallForm.rowCount * hallForm.colCount }} 个座位</p>
      </el-form>
      <template #footer>
        <el-button @click="hallDialog = false">取消</el-button>
        <el-button type="primary" :loading="hallSaving" @click="submitHall">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.page-head h1 {
  margin: 0 0 16px;
  font-size: 24px;
  font-weight: 700;
}

.panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  align-items: start;
}

.panel {
  padding: 18px;
  border-radius: var(--radius);
  border: 1px solid var(--line);
  background: var(--surface);
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.panel-head h3 {
  margin: 0;
  font-size: 14px;
}

.hall-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.seat-tip {
  margin: 0;
  font-size: 12px;
}

@media (max-width: 900px) {
  .panels {
    grid-template-columns: 1fr;
  }
}
</style>

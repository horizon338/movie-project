<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { createOrder, getSeatMap } from '@/api/orders'

const route = useRoute()
const router = useRouter()

const map = ref(null)
const selectedIds = ref([])
const loading = ref(false)
const submitting = ref(false)

const rows = computed(() => {
  if (!map.value) return []
  const result = []
  for (let row = 1; row <= map.value.rows; row++) {
    result.push(
      map.value.seats
        .filter((seat) => seat.rowNo === row)
        .sort((a, b) => a.colNo - b.colNo),
    )
  }
  return result
})

const selectedSeats = computed(() => {
  if (!map.value) return []
  return map.value.seats
    .filter((seat) => selectedIds.value.includes(seat.id))
    .sort((a, b) => a.rowNo - b.rowNo || a.colNo - b.colNo)
})

const total = computed(() => Number(map.value?.price || 0) * selectedIds.value.length)

const seatClass = (seat) => ({
  selected: selectedIds.value.includes(seat.id),
  sold: seat.status !== 0,
  love: seat.type === 'LOVE',
})

const loadMap = async () => {
  loading.value = true
  try {
    const res = await getSeatMap(route.params.id)
    map.value = res.data
  } finally {
    loading.value = false
  }
}

const toggleSeat = (seat) => {
  if (seat.status !== 0) return
  const index = selectedIds.value.indexOf(seat.id)
  if (index >= 0) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(seat.id)
  }
}

const submit = async () => {
  if (!selectedIds.value.length) return
  submitting.value = true
  try {
    const res = await createOrder({
      showtimeId: Number(route.params.id),
      seatIds: selectedIds.value,
    })
    ElMessage.success('下单成功，请在 30 分钟内完成支付')
    router.replace(`/orders/${res.data.id}`)
  } catch {
    selectedIds.value = []
    await loadMap()
  } finally {
    submitting.value = false
  }
}

onMounted(loadMap)
</script>

<template>
  <div v-loading="loading" class="seat-page">
    <div class="page-head">
      <button class="back-link" type="button" @click="router.back()">← 返回</button>
      <div v-if="map" class="head-info">
        <h1>{{ map.movieTitle }}</h1>
        <p>
          {{ map.cinemaName }} · {{ map.hallName }} ·
          <span class="num">{{ dayjs(map.startTime).format('MM-DD HH:mm') }}</span> 开场
        </p>
      </div>
    </div>

    <div v-if="map" class="layout">
      <section class="seat-panel" aria-label="座位图">
        <div class="screen"><span>银幕</span></div>

        <div class="seat-scroll">
          <div v-for="(row, rowIndex) in rows" :key="rowIndex" class="seat-row">
            <span class="row-no num">{{ rowIndex + 1 }}</span>
            <div class="seats">
              <template v-for="seat in row" :key="seat.id">
                <span v-if="seat.type === 'BLANK'" class="seat blank"></span>
                <button
                  v-else
                  class="seat"
                  :class="seatClass(seat)"
                  type="button"
                  :disabled="seat.status !== 0"
                  :title="`${seat.rowNo}排${seat.colNo}座`"
                  :aria-label="`${seat.rowNo}排${seat.colNo}座`"
                  :aria-pressed="selectedIds.includes(seat.id)"
                  @click="toggleSeat(seat)"
                >
                  {{ seat.colNo }}
                </button>
              </template>
            </div>
            <span class="row-no num">{{ rowIndex + 1 }}</span>
          </div>
        </div>

        <div class="legend">
          <span><i class="dot available"></i>可选</span>
          <span><i class="dot selected"></i>已选</span>
          <span><i class="dot sold"></i>已售 / 锁定</span>
          <span><i class="dot love"></i>情侣座</span>
        </div>
      </section>

      <aside class="summary">
        <p class="price-label">
          票价 <b class="num">¥{{ Number(map.price).toFixed(0) }}</b> / 张
        </p>

        <div class="chosen">
          <span v-if="!selectedSeats.length" class="muted">请在座位图中点击选座</span>
          <span v-for="seat in selectedSeats" :key="seat.id" class="seat-tag">
            {{ seat.rowNo }}排{{ seat.colNo }}座
          </span>
        </div>

        <div class="total-row">
          <span>合计</span>
          <b class="num">¥{{ total.toFixed(2) }}</b>
        </div>

        <el-button
          type="primary"
          size="large"
          class="submit"
          :disabled="!selectedIds.length"
          :loading="submitting"
          @click="submit"
        >
          提交订单
        </el-button>
        <p class="tip muted">提交后座位锁定 30 分钟，请及时完成支付</p>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.seat-page {
  min-height: 480px;
  padding-top: 2px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}

.head-info h1 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.head-info p {
  margin: 0;
  font-size: 13px;
  color: var(--ink-3);
}

.layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}

.seat-panel {
  padding: 28px 20px 22px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.screen {
  width: min(560px, 80%);
  height: 30px;
  margin: 0 auto 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 100% 100% 0 0 / 22px 22px 0 0;
  background: var(--ink);
  box-shadow: 0 8px 22px rgba(23, 24, 26, 0.16);
}

.screen span {
  padding-left: 0.5em;
  font-size: 10px;
  letter-spacing: 0.5em;
  color: rgba(255, 255, 255, 0.55);
}

.seat-scroll {
  overflow-x: auto;
  padding-bottom: 8px;
}

.seat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 6px;
  min-width: 520px;
}

.row-no {
  width: 18px;
  flex-shrink: 0;
  text-align: center;
  font-size: 11px;
  color: var(--ink-3);
}

.seats {
  display: flex;
  gap: 5px;
}

.seat {
  width: 26px;
  height: 24px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid #cfccc4;
  border-radius: 4px 4px 5px 5px;
  background: var(--surface);
  color: var(--ink-3);
  font-size: 9px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.12s, border-color 0.12s, color 0.12s;
}

.seat:hover:not(:disabled) {
  border-color: var(--ink);
  color: var(--ink);
}

.seat.selected {
  background: var(--red);
  border-color: var(--red);
  color: #fff;
  font-weight: 700;
}

.seat.sold {
  background: #eceae4;
  border-color: #e3e0d9;
  color: #c5c1b8;
  cursor: not-allowed;
}

.seat.love {
  border-color: #c98a97;
}

.seat.love.selected {
  border-color: var(--red);
}

.seat.blank {
  border: none;
  background: transparent;
  cursor: default;
  visibility: hidden;
}

.legend {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 22px;
  margin-top: 20px;
  font-size: 12px;
  color: var(--ink-3);
}

.legend span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 11px;
  height: 11px;
  border: 1px solid #cfccc4;
  border-radius: 3px;
  background: var(--surface);
}

.dot.selected {
  background: var(--red);
  border-color: var(--red);
}

.dot.sold {
  background: #eceae4;
  border-color: #e3e0d9;
}

.dot.love {
  border-color: #c98a97;
}

.summary {
  position: sticky;
  top: 88px;
  padding: 22px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.price-label {
  margin: 0 0 18px;
  font-size: 13px;
  color: var(--ink-3);
}

.price-label b {
  margin: 0 2px;
  font-size: 20px;
  color: var(--ink);
}

.chosen {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 30px;
  margin-bottom: 18px;
}

.chosen .muted {
  font-size: 12px;
}

.seat-tag {
  padding: 3px 8px;
  border: 1px solid #f0cfcf;
  border-radius: 2px;
  background: var(--red-soft);
  color: var(--red);
  font-size: 12px;
}

.total-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-top: 16px;
  margin-bottom: 18px;
  border-top: 1px solid var(--line);
  font-size: 13px;
  color: var(--ink-2);
}

.total-row b {
  font-size: 24px;
  color: var(--red);
}

.submit {
  width: 100%;
}

.tip {
  margin: 12px 0 0;
  font-size: 11px;
  line-height: 1.7;
  text-align: center;
}

@media (max-width: 900px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .summary {
    position: static;
  }

  .seat-row {
    justify-content: flex-start;
  }
}

@media (max-width: 560px) {
  .page-head {
    gap: 12px;
  }

  .head-info h1 {
    font-size: 22px;
  }

  .seat-panel {
    padding: 20px 12px 16px;
  }

  .seat {
    width: 23px;
    height: 21px;
    font-size: 8px;
  }

  .seats {
    gap: 4px;
  }

  .seat-row {
    min-width: 460px;
  }
}
</style>

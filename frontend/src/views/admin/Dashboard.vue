<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminBoxOffice } from '@/api/admin'

const loading = ref(false)
const stats = ref(null)
const dailyRef = ref(null)
const movieRef = ref(null)
let dailyChart = null
let movieChart = null

const money = (value) => `¥${Number(value || 0).toFixed(2)}`

const axisStyle = {
  axisLine: { lineStyle: { color: '#e5e2db' } },
  axisLabel: { color: '#8b8e94' },
  splitLine: { lineStyle: { color: '#eeebe5' } },
}

const renderCharts = () => {
  if (!stats.value) return
  if (dailyRef.value) {
    dailyChart = dailyChart || echarts.init(dailyRef.value)
    dailyChart.setOption(
      {
        backgroundColor: 'transparent',
        grid: { left: 56, right: 20, top: 30, bottom: 34 },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: stats.value.dailyStats.map((item) => item.date.slice(5)),
          ...axisStyle,
          splitLine: { show: false },
        },
        yAxis: { type: 'value', ...axisStyle },
        series: [
          {
            type: 'line',
            smooth: true,
            symbolSize: 6,
            data: stats.value.dailyStats.map((item) => Number(item.revenue)),
            lineStyle: { color: '#c2262e', width: 2 },
            itemStyle: { color: '#c2262e' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(194,38,46,0.16)' },
                { offset: 1, color: 'rgba(194,38,46,0.01)' },
              ]),
            },
          },
        ],
      },
      true,
    )
  }

  if (movieRef.value) {
    const top = stats.value.movieStats.slice(0, 6)
    movieChart = movieChart || echarts.init(movieRef.value)
    movieChart.setOption(
      {
        backgroundColor: 'transparent',
        grid: { left: 56, right: 20, top: 24, bottom: 46 },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: top.map((item) => item.title),
          ...axisStyle,
          splitLine: { show: false },
          axisLabel: { color: '#8b8e94', interval: 0, rotate: 20 },
        },
        yAxis: { type: 'value', ...axisStyle },
        series: [
          {
            type: 'bar',
            barWidth: 24,
            data: top.map((item) => Number(item.revenue)),
            itemStyle: {
              borderRadius: [3, 3, 0, 0],
              color: '#17181a',
            },
          },
        ],
      },
      true,
    )
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await adminBoxOffice(7)
    stats.value = res.data
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

const resize = () => {
  dailyChart?.resize()
  movieChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  dailyChart?.dispose()
  movieChart?.dispose()
})
</script>

<template>
  <section v-loading="loading" class="dashboard">
    <div class="page-head">
      <h1>票房统计</h1>
      <button class="refresh-link" type="button" @click="load">刷新数据</button>
    </div>

    <div v-if="stats" class="stat-grid">
      <div class="stat-card highlight">
        <span class="label">总票房</span>
        <b class="num">{{ money(stats.totalRevenue) }}</b>
        <span class="hint">已扣除退款</span>
      </div>
      <div class="stat-card">
        <span class="label">今日票房</span>
        <b class="num">{{ money(stats.todayRevenue) }}</b>
        <span class="hint">按支付时间统计</span>
      </div>
      <div class="stat-card">
        <span class="label">售出票数</span>
        <b class="num">{{ stats.ticketCount }}</b>
        <span class="hint">已退款 {{ stats.refundedOrders }} 单</span>
      </div>
      <div class="stat-card">
        <span class="label">成交订单</span>
        <b class="num">{{ stats.paidOrders }}</b>
        <span class="hint">累计已支付订单</span>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <h3>近 7 日票房趋势</h3>
        <div ref="dailyRef" class="chart"></div>
      </div>
      <div class="panel">
        <h3>影片票房 TOP 6</h3>
        <div ref="movieRef" class="chart"></div>
      </div>
    </div>

    <div class="table-grid">
      <div class="panel">
        <h3>影片票房明细</h3>
        <el-table :data="stats?.movieStats ?? []" size="small" empty-text="暂无成交数据">
          <el-table-column prop="title" label="影片" min-width="140" />
          <el-table-column label="票房" width="120">
            <template #default="{ row }">{{ money(row.revenue) }}</template>
          </el-table-column>
          <el-table-column prop="orders" label="订单" width="80" />
          <el-table-column prop="tickets" label="票数" width="80" />
        </el-table>
      </div>

      <div class="panel">
        <h3>影院票房</h3>
        <el-table :data="stats?.cinemaStats ?? []" size="small" empty-text="暂无成交数据">
          <el-table-column prop="name" label="影院" min-width="170" />
          <el-table-column label="票房" width="120">
            <template #default="{ row }">{{ money(row.revenue) }}</template>
          </el-table-column>
          <el-table-column prop="orders" label="订单" width="80" />
        </el-table>
      </div>
    </div>
  </section>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-head h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.refresh-link {
  border: 0;
  background: none;
  padding: 0;
  color: var(--ink-3);
  font: inherit;
  font-size: 13px;
  cursor: pointer;
}

.refresh-link:hover {
  color: var(--ink);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 18px 20px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.stat-card .label {
  font-size: 12px;
  color: var(--ink-3);
}

.stat-card b {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.15;
  color: var(--ink);
}

.stat-card .hint {
  font-size: 11px;
  color: var(--ink-3);
}

.stat-card.highlight {
  background: var(--ink);
  border-color: var(--ink);
}

.stat-card.highlight .label {
  color: #a9a59d;
}

.stat-card.highlight b {
  color: #fff;
}

.stat-card.highlight .hint {
  color: #7f7c75;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.panel {
  min-width: 0;
  padding: 18px 20px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.panel h3 {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
}

.chart {
  height: 260px;
}

.table-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 900px) {
  .chart-grid,
  .table-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { listOrders } from '@/api/orders'

const router = useRouter()

const activeStatus = ref('ALL')
const orders = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 8
const loading = ref(false)

const tabs = [
  { name: 'ALL', label: '全部' },
  { name: 'CREATED', label: '待支付' },
  { name: 'PAID', label: '已出票' },
  { name: 'CANCELLED', label: '已取消' },
  { name: 'REFUNDED', label: '已退款' },
  { name: 'EXPIRED', label: '已超时' },
]

const STATUS = {
  CREATED: '待支付',
  PAID: '已出票',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
  EXPIRED: '已超时',
}

const statusText = (status) => STATUS[status] ?? status
const statusClass = (status) => `status-pill ${String(status).toLowerCase()}`
const seatsText = (order) =>
  order.seats.map((seat) => `${seat.rowNo}排${seat.colNo}座`).join('、')

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (activeStatus.value !== 'ALL') params.status = activeStatus.value
    const res = await listOrders(params)
    orders.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const changeTab = () => {
  page.value = 1
  load()
}

const changePage = (value) => {
  page.value = value
  load()
}

onMounted(load)
</script>

<template>
  <section class="orders-page">
    <div class="page-head">
      <h1>我的票夹</h1>
      <span class="count">共 {{ total }} 笔订单</span>
    </div>

    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="changeTab">
      <el-tab-pane v-for="tab in tabs" :key="tab.name" :label="tab.label" :name="tab.name" />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <article
        v-for="order in orders"
        :key="order.id"
        class="order-row"
        role="link"
        tabindex="0"
        :aria-label="`${order.movieTitle}，${statusText(order.status)}`"
        @click="router.push(`/orders/${order.id}`)"
        @keyup.enter="router.push(`/orders/${order.id}`)"
      >
        <div class="poster">
          <el-image :src="order.posterUrl" fit="cover" class="poster-img">
            <template #placeholder><div class="poster-fallback">{{ order.movieTitle }}</div></template>
            <template #error><div class="poster-fallback">{{ order.movieTitle }}</div></template>
          </el-image>
        </div>

        <div class="order-body">
          <div class="title-row">
            <h3>{{ order.movieTitle }}</h3>
            <span :class="statusClass(order.status)">{{ statusText(order.status) }}</span>
          </div>
          <p class="line">
            {{ order.cinemaName }} · {{ order.hallName }} ·
            <span class="num">{{ dayjs(order.startTime).format('YYYY.MM.DD HH:mm') }}</span>
          </p>
          <p class="seats-line">{{ seatsText(order) }}</p>
          <p class="line order-no">订单号 <span class="num">{{ order.orderNo }}</span></p>
        </div>

        <div class="order-side">
          <span class="amount num">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
          <el-button
            v-if="order.status === 'CREATED'"
            type="primary"
            size="small"
            @click.stop="router.push(`/orders/${order.id}`)"
          >
            去支付
          </el-button>
          <el-button
            v-else-if="order.status === 'PAID'"
            size="small"
            @click.stop="router.push(`/orders/${order.id}`)"
          >
            查看电子票
          </el-button>
        </div>
      </article>

      <el-empty v-if="!loading && orders.length === 0" :image-size="88" description="暂无相关订单">
        <el-button type="primary" @click="router.push('/')">去看看热映影片</el-button>
      </el-empty>
    </div>

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
</template>

<style scoped>
.orders-page {
  padding-top: 2px;
}

.page-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-bottom: 8px;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.count {
  font-size: 13px;
  color: var(--ink-3);
}

.status-tabs {
  margin-bottom: 20px;
}

.order-list {
  min-height: 200px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.order-row {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto;
  gap: 20px;
  padding: 20px 22px;
  border-top: 1px solid var(--line);
  cursor: pointer;
  transition: background 0.15s;
}

.order-row:first-child {
  border-top: 0;
}

.order-row:hover {
  background: var(--surface-muted);
}

.poster {
  width: 64px;
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

.order-body {
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.title-row h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-pill {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 21px;
  padding: 0 8px;
  border-radius: 2px;
  font-size: 11px;
  font-weight: 600;
}

.status-pill.created {
  color: var(--amber);
  background: var(--amber-soft);
}

.status-pill.paid {
  color: var(--green);
  background: var(--green-soft);
}

.status-pill.cancelled,
.status-pill.refunded {
  color: var(--ink-3);
  background: #f1efea;
}

.status-pill.expired {
  color: var(--red);
  background: var(--red-soft);
}

.line {
  margin: 0 0 5px;
  font-size: 12px;
  color: var(--ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.seats-line {
  margin: 0 0 5px;
  font-size: 12px;
  color: var(--ink-2);
}

.order-no {
  margin-top: 2px;
}

.order-no .num {
  letter-spacing: 0.03em;
}

.order-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 10px;
}

.amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

@media (max-width: 640px) {
  .order-row {
    grid-template-columns: 54px minmax(0, 1fr);
    gap: 14px;
    padding: 16px;
  }

  .poster {
    width: 54px;
  }

  .order-side {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .page-head h1 {
    font-size: 24px;
  }
}
</style>

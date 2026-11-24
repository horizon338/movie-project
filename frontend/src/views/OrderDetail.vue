<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { cancelOrder, getOrder, getTicket, payOrder, refundOrder } from '@/api/orders'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const ticket = ref(null)
const loading = ref(false)
const paying = ref(false)
const cancelling = ref(false)
const refunding = ref(false)
const remainSeconds = ref(0)

let timer = null
let reloadedAfterExpire = false

const STATUS = {
  CREATED: '待支付',
  PAID: '已出票',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
  EXPIRED: '已超时',
}

const statusText = computed(() => STATUS[order.value?.status] ?? '')
const statusClass = computed(() => `status-pill ${String(order.value?.status || '').toLowerCase()}`)
const seatsText = computed(
  () => order.value?.seats?.map((seat) => `${seat.rowNo}排${seat.colNo}座`).join('、') ?? '',
)
const countdown = computed(() => {
  const minutes = Math.floor(remainSeconds.value / 60)
  const seconds = remainSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
const canRefund = computed(() => {
  if (order.value?.status !== 'PAID') return false
  return dayjs(order.value.startTime).diff(dayjs(), 'minute') >= 120
})

const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const tick = () => {
  if (!order.value || order.value.status !== 'CREATED') {
    remainSeconds.value = 0
    stopTimer()
    return
  }
  remainSeconds.value = Math.max(0, dayjs(order.value.expireAt).diff(dayjs(), 'second'))
  if (remainSeconds.value === 0) {
    stopTimer()
    if (!reloadedAfterExpire) {
      reloadedAfterExpire = true
      setTimeout(load, 2000)
    }
  }
}

const syncTimer = () => {
  stopTimer()
  tick()
  if (order.value?.status === 'CREATED' && remainSeconds.value > 0) {
    timer = setInterval(tick, 1000)
  }
}

const load = async () => {
  loading.value = true
  try {
    const res = await getOrder(route.params.id)
    order.value = res.data
    if (order.value.status === 'PAID') {
      try {
        const resTicket = await getTicket(route.params.id)
        ticket.value = resTicket.data
      } catch {
        ticket.value = null
      }
    } else {
      ticket.value = null
    }
    syncTimer()
  } finally {
    loading.value = false
  }
}

const pay = async () => {
  try {
    await ElMessageBox.confirm(
      `本次为模拟支付，确认支付 ¥${Number(order.value.totalAmount).toFixed(2)}？`,
      '模拟支付',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return
  }
  paying.value = true
  try {
    const res = await payOrder(order.value.id)
    ticket.value = res.data
    ElMessage.success('支付成功，电子票已生成')
    await load()
  } catch {
    await load()
  } finally {
    paying.value = false
  }
}

const cancel = async () => {
  try {
    await ElMessageBox.confirm('取消后座位将立即释放，确定取消该订单？', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    await load()
  } finally {
    cancelling.value = false
  }
}

const refund = async () => {
  try {
    await ElMessageBox.confirm(
      `退票后座位将重新开放，¥${Number(order.value.totalAmount).toFixed(2)} 将原路退回，确定退票？`,
      '申请退票',
      { confirmButtonText: '确认退票', cancelButtonText: '再想想', type: 'warning' },
    )
  } catch {
    return
  }
  refunding.value = true
  try {
    await refundOrder(order.value.id)
    ElMessage.success('退票成功，款项将原路退回')
    await load()
  } finally {
    refunding.value = false
  }
}

onMounted(load)
onBeforeUnmount(stopTimer)
</script>

<template>
  <div v-loading="loading" class="order-page">
    <button class="back-link" type="button" @click="router.push('/orders')">← 我的票夹</button>

    <template v-if="order">
      <section class="order-head">
        <div class="head-copy">
          <p class="order-no">订单号 <span class="num">{{ order.orderNo }}</span></p>
          <h1>{{ order.movieTitle }}</h1>
          <p class="sub">
            {{ order.cinemaName }} · {{ order.hallName }} ·
            <span class="num">{{ dayjs(order.startTime).format('YYYY.MM.DD HH:mm') }}</span> 开场
          </p>
        </div>
        <div class="status-box">
          <span :class="statusClass">{{ statusText }}</span>
          <div v-if="order.status === 'CREATED'" class="countdown">
            <span>支付剩余</span>
            <b class="num" :class="{ danger: remainSeconds <= 300 }">{{ countdown }}</b>
          </div>
          <p v-if="order.status === 'EXPIRED'" class="expired-note">超时未支付，座位已释放</p>
        </div>
      </section>

      <section class="detail-card">
        <div class="poster">
          <el-image :src="order.posterUrl" fit="cover" class="poster-img">
            <template #placeholder><div class="poster-fallback">{{ order.movieTitle }}</div></template>
            <template #error><div class="poster-fallback">{{ order.movieTitle }}</div></template>
          </el-image>
        </div>

        <div class="detail-info">
          <dl>
            <div><dt>座位</dt><dd class="seats">{{ seatsText }}</dd></div>
            <div><dt>金额</dt><dd class="amount num">¥{{ Number(order.totalAmount).toFixed(2) }}</dd></div>
            <div><dt>下单时间</dt><dd class="num">{{ dayjs(order.createdAt).format('YYYY.MM.DD HH:mm:ss') }}</dd></div>
            <div v-if="order.paidAt">
              <dt>支付时间</dt>
              <dd class="num">{{ dayjs(order.paidAt).format('YYYY.MM.DD HH:mm:ss') }}</dd>
            </div>
            <div v-if="order.refundedAt">
              <dt>退款时间</dt>
              <dd class="num">{{ dayjs(order.refundedAt).format('YYYY.MM.DD HH:mm:ss') }}</dd>
            </div>
          </dl>

          <div v-if="order.status === 'CREATED'" class="actions">
            <el-button type="primary" size="large" :loading="paying" @click="pay">
              立即支付 ¥{{ Number(order.totalAmount).toFixed(2) }}
            </el-button>
            <el-button size="large" :loading="cancelling" @click="cancel">取消订单</el-button>
          </div>
          <div v-else-if="order.status === 'PAID'" class="actions">
            <el-button v-if="canRefund" :loading="refunding" @click="refund">申请退票</el-button>
            <el-tooltip v-else content="开场前 2 小时可退票" placement="top">
              <span><el-button disabled>申请退票</el-button></span>
            </el-tooltip>
            <el-button @click="router.push(`/movies/${order.movieId}`)">再看一场</el-button>
          </div>
        </div>
      </section>

      <section v-if="order.status === 'PAID' && ticket" class="eticket" aria-label="电子票">
        <div class="eticket-top">
          <span class="ticket-brand">光影票务</span>
          <span class="ticket-type">电子票 · E-TICKET</span>
        </div>

        <div class="eticket-body">
          <div class="eticket-main">
            <h2>{{ ticket.movieTitle }}</h2>
            <p class="ticket-line">{{ ticket.cinemaName }} · {{ ticket.hallName }}</p>
            <p class="ticket-when num">{{ dayjs(ticket.startTime).format('YYYY.MM.DD HH:mm') }}</p>
            <p class="ticket-seats">{{ seatsText }}</p>
            <p class="ticket-code num">票号 {{ ticket.ticketCode }}</p>
          </div>

          <div class="ticket-qr">
            <img :src="`data:image/png;base64,${ticket.qrBase64}`" alt="电子票二维码" />
            <span>入场请出示此二维码</span>
          </div>
        </div>

        <div class="eticket-foot">开场前 2 小时可在票夹自助退票 · 建议提前 15 分钟入场</div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.order-page {
  min-height: 420px;
  padding-top: 2px;
}

.back-link {
  margin-bottom: 22px;
}

/* ---------- heading ---------- */

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--line);
}

.order-no {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--ink-3);
  letter-spacing: 0.03em;
}

.head-copy h1 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.sub {
  margin: 0;
  font-size: 13px;
  color: var(--ink-3);
}

.status-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 2px;
  font-size: 12px;
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

.countdown {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-3);
}

.countdown b {
  font-size: 26px;
  letter-spacing: 0.03em;
  color: var(--ink);
}

.countdown b.danger {
  color: var(--red);
}

.expired-note {
  margin: 0;
  font-size: 12px;
  color: var(--ink-3);
}

/* ---------- detail ---------- */

.detail-card {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 26px;
  padding: 22px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.poster {
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
  padding: 10px;
  font-size: 12px;
  color: var(--ink-3);
  text-align: center;
}

.detail-info dl {
  margin: 0;
  display: grid;
  gap: 11px;
  font-size: 14px;
}

.detail-info dl div {
  display: flex;
  gap: 18px;
}

.detail-info dt {
  width: 72px;
  flex-shrink: 0;
  color: var(--ink-3);
}

.detail-info dd {
  margin: 0;
  color: var(--ink-2);
}

.seats {
  color: var(--ink);
  font-weight: 600;
}

.amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--red);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 26px;
}

/* ---------- e-ticket ---------- */

.eticket {
  margin-top: 24px;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(23, 24, 26, 0.07);
}

.eticket-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 46px;
  padding: 0 26px;
  background: var(--red);
  color: #fff;
}

.ticket-brand {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.ticket-type {
  font-size: 10px;
  letter-spacing: 0.2em;
  color: rgba(255, 255, 255, 0.72);
}

.eticket-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 232px;
}

.eticket-main {
  padding: 28px 30px;
}

.eticket-main h2 {
  margin: 0 0 16px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.ticket-line {
  margin: 0 0 7px;
  font-size: 13px;
  color: var(--ink-2);
}

.ticket-when {
  margin: 16px 0 6px;
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.ticket-seats {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.ticket-code {
  margin: 22px 0 0;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-3);
}

.ticket-qr {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 26px;
  border-left: 1px dashed var(--line-strong);
}

.ticket-qr::before,
.ticket-qr::after {
  content: '';
  position: absolute;
  left: -7px;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: var(--paper);
}

.ticket-qr::before {
  top: -7px;
}

.ticket-qr::after {
  bottom: -7px;
}

.ticket-qr img {
  width: 156px;
  height: 156px;
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
}

.ticket-qr span {
  font-size: 11px;
  color: var(--ink-3);
}

.eticket-foot {
  padding: 13px 26px;
  border-top: 1px dashed var(--line-strong);
  background: var(--surface-muted);
  font-size: 11px;
  color: var(--ink-3);
}

/* ---------- responsive ---------- */

@media (max-width: 860px) {
  .order-head {
    flex-direction: column;
  }

  .status-box {
    align-items: flex-start;
  }

  .detail-card {
    grid-template-columns: 96px minmax(0, 1fr);
    gap: 18px;
  }

  .eticket-body {
    grid-template-columns: 1fr;
  }

  .ticket-qr {
    border-left: 0;
    border-top: 1px dashed var(--line-strong);
  }

  .ticket-qr::before,
  .ticket-qr::after {
    display: none;
  }
}

@media (max-width: 560px) {
  .head-copy h1 {
    font-size: 24px;
  }

  .detail-card {
    grid-template-columns: 1fr;
  }

  .poster {
    width: 100px;
  }

  .detail-info dl div {
    gap: 12px;
  }

  .detail-info dt {
    width: 64px;
  }

  .eticket-main {
    padding: 22px 18px;
  }

  .eticket-main h2 {
    font-size: 22px;
  }
}
</style>

import request from './request'

export const getSeatMap = (showtimeId) => request.get(`/showtimes/${showtimeId}/seats`)

export const createOrder = (data) => request.post('/orders', data)

export const listOrders = (params) => request.get('/orders', { params })

export const getOrder = (id) => request.get(`/orders/${id}`)

export const payOrder = (id) => request.post(`/orders/${id}/pay`)

export const cancelOrder = (id) => request.post(`/orders/${id}/cancel`)

export const refundOrder = (id) => request.post(`/orders/${id}/refund`)

export const getTicket = (id) => request.get(`/orders/${id}/ticket`)

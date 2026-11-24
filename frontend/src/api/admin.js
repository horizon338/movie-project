import request from './request'

// 影片管理
export const adminListMovies = (params) => request.get('/admin/movies', { params })
export const adminCreateMovie = (data) => request.post('/admin/movies', data)
export const adminUpdateMovie = (id, data) => request.put(`/admin/movies/${id}`, data)

// 影院与影厅
export const adminListCinemas = () => request.get('/admin/cinemas')
export const adminCreateCinema = (data) => request.post('/admin/cinemas', data)
export const adminUpdateCinema = (id, data) => request.put(`/admin/cinemas/${id}`, data)
export const adminListHalls = (cinemaId) => request.get(`/admin/cinemas/${cinemaId}/halls`)
export const adminCreateHall = (data) => request.post('/admin/halls', data)

// 排片
export const adminListShowtimes = (params) => request.get('/admin/showtimes', { params })
export const adminCreateShowtime = (data) => request.post('/admin/showtimes', data)

// 票房统计
export const adminBoxOffice = (days = 7) => request.get('/admin/stats/box-office', { params: { days } })

// 海报上传
export const adminUpload = (formData) =>
  request.post('/admin/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })

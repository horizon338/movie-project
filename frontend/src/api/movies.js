import request from './request'

export const listMovies = (params) => request.get('/movies', { params })

export const getMovie = (id) => request.get(`/movies/${id}`)

export const getShowtimes = (id, params) => request.get(`/movies/${id}/showtimes`, { params })

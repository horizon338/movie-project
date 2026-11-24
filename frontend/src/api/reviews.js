import request from './request'

export const listReviews = (movieId) => request.get('/reviews', { params: { movieId } })

export const getMyReview = (movieId) => request.get('/reviews/mine', { params: { movieId } })

export const myReviews = () => request.get('/reviews/my')

export const submitReview = (data) => request.post('/reviews', data)

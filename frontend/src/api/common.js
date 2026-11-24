import request from './request'

export const ping = () => request.get('/ping')

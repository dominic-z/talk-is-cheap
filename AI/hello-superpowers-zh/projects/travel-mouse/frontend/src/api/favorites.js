import request from './request'

export const getFavorites = () => request.get('/favorites')
export const createFavorite = (data) => request.post('/favorites', data)
export const updateFavorite = (id, data) => request.put(`/favorites/${id}`, data)
export const deleteFavorite = (id) => request.delete(`/favorites/${id}`)

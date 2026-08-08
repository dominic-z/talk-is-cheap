import request from './request'

export const getDestinations = (dayId) => request.get(`/days/${dayId}/destinations`)
export const createDestination = (dayId, data) => request.post(`/days/${dayId}/destinations`, data)
export const updateDestination = (dayId, id, data) => request.put(`/days/${dayId}/destinations/${id}`, data)
export const deleteDestination = (dayId, id) => request.delete(`/days/${dayId}/destinations/${id}`)
export const getImages = (dayId, id) => request.get(`/days/${dayId}/destinations/${id}/images`)
export const uploadImage = (dayId, id, file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/days/${dayId}/destinations/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const deleteImage = (dayId, id, imgId) => request.delete(`/days/${dayId}/destinations/${id}/images/${imgId}`)
export const getRoute = (dayId) => request.get(`/days/${dayId}/route`)
export const updateRoute = (dayId, data) => request.put(`/days/${dayId}/route`, data)

import request from './request'

export const getPlans = () => request.get('/plans')
export const createPlan = (data) => request.post('/plans', data)
export const getPlan = (id) => request.get(`/plans/${id}`)
export const updatePlan = (id, data) => request.put(`/plans/${id}`, data)
export const deletePlan = (id) => request.delete(`/plans/${id}`)
export const saveDay = (planId, dayId) => request.put(`/plans/${planId}/days/${dayId}/save`)

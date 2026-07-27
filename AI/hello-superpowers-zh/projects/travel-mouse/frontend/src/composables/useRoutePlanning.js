import { ref } from 'vue'

export function useRoutePlanning(AMap) {
  const calculating = ref(false)

  // 计算从 from 到多个 to 的驾车时间
  const calcDrivingTime = (from, toList) => {
    return new Promise((resolve) => {
      calculating.value = true
      const driving = new AMap.Driving({ policy: 0 })
      const results = []
      let completed = 0

      if (toList.length === 0) {
        calculating.value = false
        resolve([])
        return
      }

      toList.forEach((to, index) => {
        driving.search(
          [from.longitude, from.latitude],
          [to.longitude, to.latitude],
          (status, result) => {
            completed++
            if (status === 'complete' && result.routes?.length) {
              const route = result.routes[0]
              results[index] = {
                destId: to.id,
                durationMinutes: Math.round(route.time / 60),
                distanceMeters: route.distance,
                transportMode: 'driving'
              }
            } else {
              results[index] = { destId: to.id, durationMinutes: null, distanceMeters: null, transportMode: 'driving' }
            }
            if (completed === toList.length) {
              calculating.value = false
              resolve(results)
            }
          }
        )
      })
    })
  }

  // 计算两点之间路线（用于绘制）
  const calcRoute = (from, to, mode = 'driving') => {
    return new Promise((resolve) => {
      let planner
      if (mode === 'walking') {
        planner = new AMap.Walking()
      } else {
        planner = new AMap.Driving({ policy: 0 })
      }
      planner.search(
        [from.longitude, from.latitude],
        [to.longitude, to.latitude],
        (status, result) => {
          if (status === 'complete' && result.routes?.length) {
            resolve(result.routes[0])
          } else {
            resolve(null)
          }
        }
      )
    })
  }

  return { calculating, calcDrivingTime, calcRoute }
}

import { ref } from 'vue'

export function usePoiSearch(AMap) {
  const results = ref([])
  const searching = ref(false)

  const search = (keyword, city = '全国') => {
    return new Promise((resolve) => {
      searching.value = true
      const placeSearch = new AMap.PlaceSearch({ city, pageSize: 10 })
      placeSearch.search(keyword, (status, result) => {
        searching.value = false
        if (status === 'complete' && result.poiList) {
          results.value = result.poiList.pois.map(poi => ({
            name: poi.name,
            address: poi.address || '',
            longitude: poi.location.lng,
            latitude: poi.location.lat,
            category: poi.type || ''
          }))
        } else {
          results.value = []
        }
        resolve(results.value)
      })
    })
  }

  return { results, searching, search }
}

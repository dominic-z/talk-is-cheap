import AMapLoader from '@amap/amap-jsapi-loader'
import { ref } from 'vue'

export function useAmap() {
  const map = ref(null)
  const loaded = ref(false)

  const initMap = async (containerId) => {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE
    }
    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: '2.0',
      plugins: ['AMap.PlaceSearch', 'AMap.Driving', 'AMap.Walking', 'AMap.Transfer', 'AMap.Geocoder']
    })
    map.value = new AMap.Map(containerId, {
      zoom: 12,
      center: [120.15, 30.28] // 默认杭州
    })
    loaded.value = true
    return { map: map.value, AMap }
  }

  return { map, loaded, initMap }
}

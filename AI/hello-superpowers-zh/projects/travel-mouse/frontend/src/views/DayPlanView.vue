<template>
  <div class="day-plan">
    <LeftPanel
      :dayId="dayId"
      :destinations="pendingList"
      :travel-times="travelTimes"
      @add-to-route="addToRoute"
      @remove-dest="removeDest"
      @add-dest="addDest"
    />
    <MapContainer @map-ready="onMapReady" />
    <RightPanel
      :route-list="routeList"
      :segments="routeSegments"
      @rollback="rollback"
      @save-day="saveDay"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import MapContainer from '../components/MapContainer.vue'
import LeftPanel from '../components/LeftPanel.vue'
import RightPanel from '../components/RightPanel.vue'
import { useRoutePlanning } from '../composables/useRoutePlanning'
import { getDestinations, createDestination, deleteDestination, updateDestination, getRoute, updateRoute } from '../api/destinations'
import { saveDay as apiSaveDay } from '../api/plans'

const route = useRoute()
const dayId = route.params.dayId
const planId = route.params.id

const destinations = ref([])
const routeList = ref([])
const routeSegments = ref([])
const travelTimes = ref([])
const currentLocation = ref(null)

let mapInstance = null
let AMapInstance = null
let routePlanner = null
let markers = []

const pendingList = computed(() => destinations.value.filter(d => !d.inRoute))

const onMapReady = ({ map, AMap }) => {
  mapInstance = map
  AMapInstance = AMap
  routePlanner = useRoutePlanning(AMap)

  // 地图点选添加目的地
  map.on('click', async (e) => {
    const { lng, lat } = e.lnglat
    const geocoder = new AMap.Geocoder()
    geocoder.getAddress([lng, lat], async (status, result) => {
      const address = status === 'complete' ? result.regeocode.formattedAddress : `自定义位置 (${lng.toFixed(4)}, ${lat.toFixed(4)})`
      const name = status === 'complete' && result.regeocode.pois?.length
        ? result.regeocode.pois[0].name
        : '自定义目的地'
      await addDest({ name, address, longitude: lng, latitude: lat, category: '自定义' })
    })
  })
}

onMounted(async () => {
  destinations.value = await getDestinations(dayId)
  routeList.value = destinations.value.filter(d => d.inRoute).sort((a, b) => a.routeOrder - b.routeOrder)
  if (routeList.value.length > 0) {
    currentLocation.value = routeList.value[routeList.value.length - 1]
  }
  routeSegments.value = await getRoute(dayId)
})

const addDest = async (poi) => {
  const dest = await createDestination(dayId, poi)
  destinations.value.push(dest)
}

const removeDest = async (id) => {
  await deleteDestination(dayId, id)
  destinations.value = destinations.value.filter(d => d.id !== id)
}

const addToRoute = async (dest) => {
  const order = routeList.value.length
  await updateDestination(dayId, dest.id, { inRoute: true, routeOrder: order })
  dest.inRoute = true
  dest.routeOrder = order
  routeList.value.push(dest)
  currentLocation.value = dest

  if (routePlanner && pendingList.value.length > 0) {
    travelTimes.value = await routePlanner.calcDrivingTime(dest, pendingList.value)
  }

  if (mapInstance && AMapInstance) {
    const marker = new AMapInstance.Marker({
      position: [dest.longitude, dest.latitude],
      title: dest.name
    })
    mapInstance.add(marker)
    markers.push(marker)
  }
}

const rollback = async () => {
  if (routeList.value.length === 0) return
  const last = routeList.value.pop()
  await updateDestination(dayId, last.id, { inRoute: false, routeOrder: null })
  last.inRoute = false
  last.routeOrder = null
  currentLocation.value = routeList.value.length > 0 ? routeList.value[routeList.value.length - 1] : null
  travelTimes.value = []
  if (markers.length) {
    mapInstance.remove(markers.pop())
  }
}

const saveDay = async () => {
  if (routeList.value.length > 1 && routePlanner) {
    const segments = []
    for (let i = 0; i < routeList.value.length - 1; i++) {
      const from = routeList.value[i]
      const to = routeList.value[i + 1]
      const routeResult = await routePlanner.calcRoute(from, to)
      segments.push({
        fromDestId: from.id,
        toDestId: to.id,
        transportMode: 'driving',
        durationMinutes: routeResult ? Math.round(routeResult.time / 60) : 0,
        distanceMeters: routeResult ? routeResult.distance : 0,
        routeOrder: i
      })
    }
    await updateRoute(dayId, { segments })
  }
  await apiSaveDay(planId, dayId)
  ElMessage.success('当天计划已保存！')
}
</script>

<style scoped>
.day-plan {
  display: flex;
  height: 100%;
}
.day-plan > :first-child { width: 22%; overflow-y: auto; border-right: 1px solid #e5e7eb; }
.day-plan > :nth-child(2) { flex: 1; }
.day-plan > :last-child { width: 22%; overflow-y: auto; border-left: 1px solid #e5e7eb; }
</style>

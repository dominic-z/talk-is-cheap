// fetch.js
import { ref, watchEffect, toValue } from 'vue'

export function useFetch(url) {
    const data = ref(null)
    const error = ref(null)

    //   fetch(url)
    //     .then((res) => res.json())
    //     .then((json) => (data.value = json))
    //     .catch((err) => (error.value = err))

    // 如果传递ref，需要重写
    const fetchData = () => {
        // reset state before fetching..
        data.value = null
        error.value = null

        // fetch(url.value) 也可以
        fetch(toValue(url))
            .then((res) => res.json())
            .then((json) => (data.value = json))
            .catch((err) => (error.value = err))
    }

    watchEffect(() => {
        fetchData()
    })

    return { data, error }
}
// src/api/paths.js
export const API_PATHS = {
    // 用户相关
    cluster: {
        schduler: {
            id: "/scheduler/cluster/id",
            leader: "/scheduler/cluster/leader",
        }
    },
    // 商品相关
    product: {
        detail: '/product/:id', // 带参数的路径
        list: '/product/list',
        create: '/product'
    },
    // 公共路径
    common: {
        upload: '/upload',
        config: '/config'
    }
};
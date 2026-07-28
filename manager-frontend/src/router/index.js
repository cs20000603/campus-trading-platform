import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/Login.vue') },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/Home.vue')},
        { path: 'admin', component: () => import('@/views/Admin.vue')},
        { path: 'user', component: () => import('@/views/User.vue')},
        { path: 'person', component: () => import('@/views/Person.vue')},
        { path: 'password', component: () => import('@/views/Password.vue')},
        { path: 'category', component: () => import('@/views/Category.vue')},
        { path: 'goods', component: () => import('@/views/Goods.vue')},
        { path: 'carousel', component: () => import('@/views/Carousel.vue')},
        { path: 'collect', component: () => import('@/views/Collect.vue')},
        { path: 'recharge', component: () => import('@/views/Recharge.vue')},
        { path: 'orders', component: () => import('@/views/Orders.vue')},
        { path: 'comment', component: () => import('@/views/Comment.vue')},
        { path: 'shop', component: () => import('@/views/Shop.vue')},
        { path: 'dataManager', component: () => import('@/views/DataManager.vue')},
      ]
    },
  ]
})

router.beforeEach(() => {
  window.scroll({ top: 0, behavior: "smooth" })
})

export default router
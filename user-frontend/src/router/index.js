import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/resetPassword', component: () => import('@/views/ResetPassword.vue') },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/Home.vue')},
        { path: 'person', component: () => import('@/views/Person.vue')},
        { path: 'password', component: () => import('@/views/Password.vue')},
        { path: 'goods', component: () => import('@/views/Goods.vue')},
        { path: 'goodsDetail', component: () => import('@/views/GoodsDetail.vue')},
        { path: 'userCollect', component: () => import('@/views/UserCollect.vue')},
        { path: 'userRecharge', component: () => import('@/views/UserRecharge.vue')},
        { path: 'cart', component: () => import('@/views/Cart.vue')},
        { path: 'userOrders', component: () => import('@/views/UserOrders.vue')},
        { path: 'userComment', component: () => import('@/views/UserComment.vue')},
        { path: 'shop', component: () => import('@/views/ShopList.vue')},
        { path: 'shopRegister', component: () => import('@/views/ShopRegister.vue')},
        { path: 'shopManage', component: () => import('@/views/ShopManage.vue')},
        { path: 'shopGoods', component: () => import('@/views/ShopGoods.vue')},
        { path: 'shopOrders', component: () => import('@/views/ShopOrders.vue')},
        { path: 'idleSquare', component: () => import('@/views/IdleSquare.vue')},
        { path: 'myIdle', component: () => import('@/views/MyIdle.vue')},
        { path: 'idlePublish', component: () => import('@/views/IdlePublish.vue')},
        { path: 'idleDetail', component: () => import('@/views/IdleDetail.vue')},
        { path: 'idleChat', component: () => import('@/views/IdleChat.vue')},
        { path: 'idleWanted', component: () => import('@/views/IdleWanted.vue')},
      ]
    },
  ]
})

router.beforeEach(() => {
  window.scroll({ top: 0, behavior: "smooth" })
})

export default router
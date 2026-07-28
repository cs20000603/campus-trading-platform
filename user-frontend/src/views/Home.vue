<!-- 用户端首页：展示轮播图、推荐商品、热销商品、最新上架 -->
<template>
  <div class="front-container">
    <!-- 第一行：左侧轮播图 + 右侧为您推荐 -->
    <div style="display: flex; grid-gap: 20px; margin-bottom: 10px">
      <div style="flex: 1">
        <!-- 轮播图区域，点击可跳转到对应商品详情 -->
        <el-carousel height="360px">
          <el-carousel-item v-for="item in data.carouselList" :key="item.id">
            <img @click="router.push('/front/goodsDetail?id=' + item.goodsId)" :src="item.img" alt="" style="width: 100%; height: 100%; object-fit: cover; cursor: pointer; border-radius: 6px">
          </el-carousel-item>
        </el-carousel>
      </div>
      <!-- 右侧推荐商品列表 -->
      <div class="card" style="width: 260px">
        <div style="display: flex; align-items: center; padding-bottom: 5px; border-bottom: 1px solid red">
          <img src="@/assets/imgs/love.png" alt="" style="width: 25px">
          <div style="color: red; font-weight: bold; font-size: 20px; margin-left: 2px">为您推荐</div>
        </div>
        <div style="padding: 20px 0">
          <div class="top-item" @click="router.push('/front/goodsDetail?id=' + item.id)" style="cursor: pointer; display: flex; grid-gap: 10px; margin-bottom: 20px" v-for="item in data.recommendGoods" :key="item.id">
            <img style="width: 65px; height: 65px" :src="item.img" alt="">
            <div>
              <div class="line2" style="margin-bottom: 5px">{{ item.name }}</div>
              <div style="color: red"><span>￥</span><b>{{ item.price }}</b></div>
            </div>

          </div>
        </div>
      </div>
    </div>
    <!-- 第一行结束 -->


    <div class="card" style="padding: 20px;">
      <!-- 第二行：热销商品（按销量降序取前4个） -->
      <div style="display: flex; align-items: flex-end; padding-bottom: 5px; border-bottom: 1px solid red; margin-bottom: 10px">
        <div style="flex: 1; display: flex; align-items: center">
          <img src="@/assets/imgs/hot.png" alt="" style="width: 25px">
          <div style="color: red; font-weight: bold; font-size: 20px; margin-left: 2px">热销商品</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer; font-size: 13px">查看更多</div>
      </div>
      <div style="margin-bottom: 40px">
        <el-row :gutter="20">
         <el-col :span="6" v-for="item in data.hotGoods" :key="item.id">
           <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="item" style="width: 100%; border-radius: 5px; height: 350px">
             <img :src="item.img" alt="" style="width: 100%; height: 260px; border-radius: 5px 5px 0 0">
             <div style="padding: 5px">
               <div class="line1" style="font-size: 16px;">{{ item.name }}</div>
               <div>
                 <span style="color: red">￥</span><b style="color: red; font-size: 20px">{{ item.price }}</b>
                 <span style="margin-left: 10px; color: #666">销量：{{ item.saleCount }}</span>
               </div>
             </div>
           </div>
         </el-col>
        </el-row>
      </div>
      <!-- 第二行结束 -->

      <!-- 第三行：最新上架商品（按创建时间排序取前4个） -->
      <div style="display: flex; align-items: flex-end; padding-bottom: 5px; border-bottom: 1px solid red; margin-bottom: 10px">
        <div style="flex: 1; display: flex; align-items: center">
          <img src="@/assets/imgs/new.png" alt="" style="width: 25px">
          <div style="color: red; font-weight: bold; font-size: 20px; margin-left: 2px">最新上架</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer; font-size: 13px">查看更多</div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="item in data.newGoods" :key="item.id">
            <div @click="router.push('/front/goodsDetail?id=' + item.id)" class="item" style="width: 100%; border-radius: 5px; height: 350px">
              <img :src="item.img" alt="" style="width: 100%; height: 260px; border-radius: 5px 5px 0 0">
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px">{{ item.name }}</div>
                <div>
                  <span style="color: red">￥</span><b style="color: red; font-size: 20px">{{ item.price }}</b>
                  <span style="margin-left: 10px; color: #666">销量：{{ item.saleCount }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <!-- 第三行结束 -->

  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import router from "@/router";

// 响应式数据对象
const data = reactive({
  carouselList: [],    // 轮播图列表
  hotGoods: [],        // 热销商品（按销量降序取前4个）
  newGoods: [],        // 最新上架商品（取前4个）
  recommendGoods: [],  // 推荐商品（过滤推荐标记，取前5个）
})

// 加载轮播图数据
request.get('/carousel/selectAll').then(res => {
  data.carouselList = res.data
})

// 加载热销商品：筛选上架商品，按销量降序，取前4个
request.get('/goods/selectAll', {
  params: {
    status: '上架'
  }
}).then(res => {
  data.hotGoods = res.data.sort((v1, v2) => v2.saleCount - v1.saleCount).splice(0, 4)
})

// 加载最新上架商品：筛选上架商品，取前4个
request.get('/goods/selectAll', {
  params: {
    status: '上架'
  }
}).then(res => {
  data.newGoods = res.data.splice(0, 4)
})

// 加载推荐商品：筛选上架且推荐标记为'是'的商品，取前5个
request.get('/goods/selectAll', {
  params: {
    status: '上架'
  }
}).then(res => {
  data.recommendGoods = res.data.filter(v => v.recommend === '是').splice(0, 5)
})
</script>

<style>
.item {
  cursor: pointer;
}
.item:hover {
  border: 1px solid red
}
.top-item:hover {
  color: red;
}
</style>
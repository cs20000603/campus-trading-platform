<template>
  <div>

    <div class="card" style="line-height: 30px; margin-bottom: 10px">
      <!-- 管理端首页：展示系统技术栈和功能模块介绍 -->
<div style="margin-bottom: 20px; font-size: 24px; font-weight: bold">校园小卖部管理员端</div>
      <div style="color: #666; margin-bottom: 20px">校园小卖部交易平台管理系统 v1.0</div>

      <el-descriptions title="后端技术栈" :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="核心框架">Spring Boot 3.3.1（Java 17）</el-descriptions-item>
        <el-descriptions-item label="Web 容器">Tomcat（内嵌）</el-descriptions-item>
        <el-descriptions-item label="ORM 框架">MyBatis 3.0.3 + PageHelper 1.4.6</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.0.33（InnoDB 引擎）</el-descriptions-item>
        <el-descriptions-item label="缓存">Redis（Spring Data Redis）</el-descriptions-item>
        <el-descriptions-item label="对象存储">MinIO 8.5.7</el-descriptions-item>
        <el-descriptions-item label="认证鉴权">JWT（jjwt 0.12.5）</el-descriptions-item>
        <el-descriptions-item label="实时推送">WebSocket（STOMP 协议）</el-descriptions-item>
        <el-descriptions-item label="工具库">Hutool 5.8.25</el-descriptions-item>
        <el-descriptions-item label="监控">Spring Boot Actuator</el-descriptions-item>
        <el-descriptions-item label="项目结构">Maven 多模块（父项目 + common + 双后端）</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="前端技术栈" :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="核心框架">Vue 3.3.4（Composition API）</el-descriptions-item>
        <el-descriptions-item label="构建工具">Vite 4.4.11</el-descriptions-item>
        <el-descriptions-item label="UI 组件库">Element Plus 2.14.1</el-descriptions-item>
        <el-descriptions-item label="路由">Vue Router 4.2.5</el-descriptions-item>
        <el-descriptions-item label="HTTP 请求">Axios 1.6.2</el-descriptions-item>
        <el-descriptions-item label="图表">ECharts 6.0.0</el-descriptions-item>
        <el-descriptions-item label="富文本编辑器">WangEditor 5.1.23</el-descriptions-item>
        <el-descriptions-item label="WebSocket 客户端">SockJS + STOMP.js</el-descriptions-item>
        <el-descriptions-item label="CSS 预处理">Sass 1.79</el-descriptions-item>
        <el-descriptions-item label="自动导入">unplugin-auto-import + unplugin-vue-components</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="系统功能模块" :column="2" border>
        <el-descriptions-item label="商品管理">商品发布、编辑、上下架、分类管理</el-descriptions-item>
        <el-descriptions-item label="订单管理">订单查询、状态流转、实时推送</el-descriptions-item>
        <el-descriptions-item label="店铺管理">店铺申请审核、营业执照验证、店铺信息维护</el-descriptions-item>
        <el-descriptions-item label="用户管理">普通用户与管理员信息管理</el-descriptions-item>
        <el-descriptions-item label="闲置交易">闲置发布、广场浏览、在线聊天、求购板</el-descriptions-item>
        <el-descriptions-item label="AI 助手">智能商品描述生成、商品上架建议</el-descriptions-item>
        <el-descriptions-item label="轮播图管理">首页轮播图配置</el-descriptions-item>
        <el-descriptions-item label="评价系统">订单评价查看与管理</el-descriptions-item>
        <el-descriptions-item label="收藏系统">用户收藏数据管理</el-descriptions-item>
        <el-descriptions-item label="充值管理">用户充值记录查看</el-descriptions-item>
      </el-descriptions>
    </div>

  </div>
</template>

<script setup>
import { reactive } from "vue";

// 响应式数据对象：存储当前登录管理员信息
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})
</script>
<template>
  <div class="front-container">
    <div class="card" style="padding: 20px">
      <!-- 我的闲置页面：管理自己发布的闲置商品，支持按状态筛选、下架、重新发布、删除 -->
<h3 style="margin-bottom: 15px">我的闲置</h3>
      <el-tabs v-model="data.activeTab" @tab-change="load">
        <el-tab-pane label="在售" name="在售" />
        <el-tab-pane label="已售出" name="已售出" />
        <el-tab-pane label="已下架" name="已下架" />
      </el-tabs>

      <el-table v-if="data.tableData.length > 0" :data="data.tableData" stripe>
        <el-table-column label="图片" width="80">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.images?.split(',')[0]" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" />
        <el-table-column label="价格" width="100">
          <template #default="scope"><span style="color: red; font-weight: bold">￥{{ scope.row.price }}</span></template>
        </el-table-column>
        <el-table-column label="成色" prop="condition" width="100" />
        <el-table-column label="浏览量" prop="views" width="80" />
        <el-table-column label="发布时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button v-if="scope.row.status === '在售'" size="small" @click="takeDown(scope.row.id)">下架</el-button>
            <el-button v-if="scope.row.status === '已下架'" size="small" type="success" @click="relist(scope.row)">重新发布</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无数据" />

      <div style="text-align: center; margin-top: 15px">
        <el-pagination background layout="prev, pager, next" :total="data.total" :page-size="10" v-model:current-page="data.pageNum" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

// 响应式数据对象
const data = reactive({
  activeTab: "在售",  // 当前选中的Tab页: 在售/已售出/已下架
  pageNum: 1,         // 当前页码
  total: 0,           // 总记录数
  tableData: [],      // 闲置商品列表
});

// 加载我的闲置商品列表（按状态筛选）
const load = () => {
  request.get("/idleGoods/myListings", {
    params: { pageNum: data.pageNum, pageSize: 10, status: data.activeTab },
  }).then(res => {
    data.tableData = (res.data?.list || []).filter(item => item.status === data.activeTab);
    data.total = data.tableData.length;
  });
};

// 下架闲置商品
const takeDown = (id) => {
  ElMessageBox.confirm("确认下架该闲置商品吗？", "确认", { type: "warning" }).then(() => {
    request.put("/idleGoods/takeDown/" + id).then(res => {
      if (res.code === "200") { ElMessage.success("已下架"); load(); }
    });
  }).catch(() => {});
};

// 重新发布已下架的闲置商品
const relist = (row) => {
  request.put("/idleGoods/update", { id: row.id, status: "在售" }).then(res => {
    if (res.code === "200") { ElMessage.success("已重新发布"); load(); }
  });
};

// 删除闲置商品（不可恢复）
const handleDelete = (id) => {
  ElMessageBox.confirm("确认删除该闲置商品吗？此操作不可恢复。", "确认删除", { type: "warning" }).then(() => {
    request.delete("/idleGoods/delete/" + id).then(res => {
      if (res.code === "200") { ElMessage.success("已删除"); load(); }
    });
  }).catch(() => {});
};

load();
</script>

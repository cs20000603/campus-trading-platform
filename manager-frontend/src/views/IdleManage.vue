<template>
  <div>
    <!-- 闲置商品管理页面：按标题/成色/分类/状态筛选所有闲置商品，支持下架和删除 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.title" style="width: 200px; margin-right: 10px" placeholder="标题关键词"></el-input>
      <el-select v-model="data.condition" placeholder="成色" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="全新" value="全新"></el-option>
        <el-option label="几乎全新" value="几乎全新"></el-option>
        <el-option label="轻微使用" value="轻微使用"></el-option>
        <el-option label="明显痕迹" value="明显痕迹"></el-option>
      </el-select>
      <el-select v-model="data.category" placeholder="分类" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="数码" value="数码"></el-option>
        <el-option label="书籍" value="书籍"></el-option>
        <el-option label="生活用品" value="生活用品"></el-option>
        <el-option label="服饰" value="服饰"></el-option>
        <el-option label="美妆" value="美妆"></el-option>
        <el-option label="运动" value="运动"></el-option>
        <el-option label="乐器" value="乐器"></el-option>
        <el-option label="其他" value="其他"></el-option>
      </el-select>
      <el-select v-model="data.status" placeholder="状态" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="在售" value="在售"></el-option>
        <el-option label="已售出" value="已售出"></el-option>
        <el-option label="已下架" value="已下架"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60"></el-table-column>
        <el-table-column label="图片" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.images" :src="scope.row.images.split(',')[0]" :preview-src-list="scope.row.images.split(',')" preview-teleported style="width: 50px; height: 50px; border-radius: 4px" />
            <span v-else style="color: #ccc">--</span>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" show-overflow-tooltip></el-table-column>
        <el-table-column label="价格" prop="price" width="90"></el-table-column>
        <el-table-column label="成色" prop="condition" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.condition" size="small" type="warning">{{ scope.row.condition }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" prop="category" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.category" size="small">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配送" prop="deliveryType" width="70"></el-table-column>
        <el-table-column label="卖家" prop="sellerName" width="100"></el-table-column>
        <el-table-column label="店铺" prop="shopName" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '在售'" type="success" size="small">在售</el-tag>
            <el-tag v-else-if="scope.row.status === '已售出'" type="info" size="small">已售出</el-tag>
            <el-tag v-else-if="scope.row.status === '已下架'" type="danger" size="small">已下架</el-tag>
            <span v-else>{{ scope.row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" prop="views" width="80"></el-table-column>
        <el-table-column label="发布时间" prop="createTime" width="160"></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button v-if="scope.row.status !== '已下架'" size="small" type="warning" @click="takeDown(scope.row.id)">下架</el-button>
            <el-button size="small" type="danger" @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div style="text-align: center; margin-top: 20px" v-if="data.total > 0">
      <el-pagination background layout="prev, pager, next" :total="data.total" :page-size="data.pageSize" v-model:current-page="data.pageNum" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";

// 响应式数据对象：搜索条件 + 分页 + 闲置商品列表
const data = reactive({
  title: '',        // 标题搜索关键词
  condition: '',    // 成色筛选
  category: '',     // 分类筛选
  status: '',       // 状态筛选
  tableData: [],    // 闲置商品列表
  pageNum: 1,       // 当前页码
  pageSize: 10,     // 每页条数
  total: 0,         // 总记录数
});

// 分页查询闲置商品列表（按标题/成色/分类/状态筛选）
const load = () => {
  request.get('/admin/idleGoods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title || undefined,
      condition: data.condition || undefined,
      category: data.category || undefined,
      status: data.status || undefined,
    }
  }).then(res => {
    data.tableData = res.data?.list || [];
    data.total = res.data?.total || 0;
  });
};

// 重置搜索条件并重新加载
const reset = () => {
  data.title = '';
  data.condition = '';
  data.category = '';
  data.status = '';
  load();
};

// 下架闲置商品
const takeDown = (id) => {
  ElMessageBox.confirm('确定要下架该闲置商品吗？', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    request.put('/admin/idleGoods/takeDown/' + id).then(() => {
      ElMessage.success('下架成功');
      load();
    });
  }).catch(() => {});
};

// 删除闲置商品（不可恢复）
const del = (id) => {
  ElMessageBox.confirm('确定要删除该闲置商品吗？此操作不可恢复。', '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    request.delete('/admin/idleGoods/delete/' + id).then(() => {
      ElMessage.success('删除成功');
      load();
    });
  }).catch(() => {});
};

load();
</script>

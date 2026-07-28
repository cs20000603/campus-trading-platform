<!-- 用户评价管理页面：查看、编辑、删除自己的商品评价 -->
<template>
  <div class="front-container">

    <!-- 搜索栏：按评价内容和商品名称查询 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.content" style="width: 300px; margin-right: 10px" placeholder="请输入评价内容查询"></el-input>
      <el-input v-model="data.goodsName" style="width: 300px; margin-right: 10px" placeholder="请输入商品名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <!-- 评价列表表格 -->
    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <!-- 商品图片 -->
        <el-table-column prop="goodsImg" label="商品图片">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px; display: block; border-radius: 5px" :src="scope.row.goodsImg" :preview-src-list="[scope.row.goodsImg]" preview-teleported></el-image>
          </template>
        </el-table-column>
        <!-- 商品名称：可点击跳转到商品详情 -->
        <el-table-column prop="goodsName" label="商品名称">
          <template #default="scope">
            <a :href="'/front/goodsDetail?id=' + scope.row.goodsId">{{ scope.row.goodsName }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号"></el-table-column>
        <!-- 评分：使用星级评分组件（只读） -->
        <el-table-column prop="score" label="评分" width="200">
          <template #default="scope">
            <el-rate disabled allow-half show-score v-model="scope.row.score" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="时间"></el-table-column>
        <!-- 操作按钮 -->
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <!-- 评价编辑弹窗 -->
    <el-dialog title="评价信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="评分" prop="score">
          <el-rate show-score allow-half v-model="data.form.score"></el-rate>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="3" v-model="data.form.content" autocomplete="off" placeholder="请输入评价内容" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const formRef = ref()  // 评价表单引用
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前登录用户
  pageNum: 1,         // 当前页码
  pageSize: 10,       // 每页条数
  total: 0,           // 总记录数
  formVisible: false, // 编辑弹窗是否显示
  form: {},           // 当前编辑的评价数据
  tableData: [],      // 评价列表数据
  content: null,      // 评价内容搜索关键词
  goodsName: null,    // 商品名称搜索关键词
  rules: {            // 表单校验规则
    score: [
      { required: true, message: '请输入评分', trigger: 'change' },
    ],
    content: [
      { required: true, message: '请输入内容', trigger: 'blur' },
    ],
  }
})

// 分页查询当前用户的评价
const load = () => {
  request.get('/comment/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      content: data.content,
      goodsName: data.goodsName,
      userId: data.user.id,
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 编辑评价：深拷贝行数据打开弹窗
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增评价
const add = () => {
  request.post('/comment/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 更新评价
const update = () => {
  request.put('/comment/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存：有id则更新，无则新增
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

// 删除评价
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/comment/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置搜索条件
const reset = () => {
  data.content = null
  data.goodsName = null
  load()
}
</script>
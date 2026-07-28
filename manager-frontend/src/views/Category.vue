<template>
  <div>
    <!-- 商品分类管理页面：按名称/店铺类型筛选、新增、编辑、删除商品分类 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 200px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-select v-model="data.shopType" placeholder="店铺类型" style="width: 150px; margin-right: 10px" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="超市" value="超市"></el-option>
        <el-option label="水果店" value="水果店"></el-option>
        <el-option label="服装店" value="服装店"></el-option>
        <el-option label="蛋糕店" value="蛋糕店"></el-option>
        <el-option label="奶茶店" value="奶茶店"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="店铺类型" prop="shopType" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.shopType" type="primary">{{ scope.row.shopType }}</el-tag>
            <span v-else style="color: #999">通用</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="商品分类信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="店铺类型">
          <el-select v-model="data.form.shopType" placeholder="请选择店铺类型" clearable style="width: 100%">
            <el-option label="通用" value=""></el-option>
            <el-option label="超市" value="超市"></el-option>
            <el-option label="水果店" value="水果店"></el-option>
            <el-option label="服装店" value="服装店"></el-option>
            <el-option label="蛋糕店" value="蛋糕店"></el-option>
            <el-option label="奶茶店" value="奶茶店"></el-option>
          </el-select>
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

// 表单引用
const formRef = ref()
// 响应式数据对象：分页 + 分类列表 + 表单 + 筛选条件
const data = reactive({
  pageNum: 1,       // 当前页码
  pageSize: 10,     // 每页条数
  total: 0,         // 分类总数
  formVisible: false,// 编辑弹窗是否显示
  form: {},         // 分类表单数据
  tableData: [],    // 分类列表
  name: null,       // 名称搜索关键词
  shopType: null,   // 店铺类型筛选
  rules: {
    name: [
      { required: true, message: '请输入名称', trigger: 'blur' },
    ]
  }
})

// 分页查询
const load = () => {
  request.get('/category/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      shopType: data.shopType
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/category/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/category/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // data.form有id就是更新，没有就是新增
      data.form.id ? update() : add()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/category/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.name = null
  data.shopType = null
  load()
}
</script>
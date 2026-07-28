<template>
  <div>
    <!-- 用户管理页面：查看、新增、编辑、删除普通用户账号 -->
    <div class="card" style="margin-bottom: 5px">
      <el-input style="width: 300px" v-model="data.name" placeholder="请输入名称查询" :prefix-icon="Search" />
      <el-button @click="load" type="primary" style="margin-left: 10px">查询</el-button>
      <el-button @click="reset" type="info">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button @click="handleAdd" type="primary">新增</el-button>
      </div>
      <div>
        <el-table :data="data.tableData" stripe style="width: 100%">
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="avatar" label="头像">
            <template #default="scope">
              <el-image v-if="scope.row.avatar" style="width: 50px; height: 50px; display: block; border-radius: 50%"
                        :src="scope.row.avatar" :preview-src-list="[scope.row.avatar]" preview-teleported></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" />
          <el-table-column prop="account" label="账户余额" />
          <el-table-column label="操作" width="180" fixed="right" >
            <template #default="scope">
              <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="card">
      <el-pagination v-model:current-page="data.pageNum" v-model:page-size="data.pageSize"
                     @current-change="load" background layout="total, prev, pager, next" :total="data.total" />
    </div>

    <el-dialog title="用户信息" v-model="data.formVisible" width="30%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="username" label="账号">
          <el-input :disabled="data.form.id !== undefined" v-model="data.form.username" placeholder="请输入账号" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="name" label="姓名">
          <el-input v-model="data.form.name" placeholder="请输入姓名" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="avatar" label="头像">
          <el-upload
              :action="baseUrl + '/files/upload'"
              list-type="picture"
              :on-success="handleFileUpload"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Search } from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

// 文件上传基础URL
const baseUrl = import.meta.env.VITE_BASE_URL
// 表单引用
const formRef = ref()
// 响应式数据对象：搜索 + 分页 + 用户列表 + 表单
const data = reactive({
  name: null,        // 姓名搜索关键词
  tableData: [],     // 用户列表
  total: 0,          // 用户总数
  pageNum: 1,        // 当前页码
  pageSize: 5,       // 每页条数
  formVisible: false,// 编辑弹窗是否显示
  form: {},          // 编辑表单数据
  rules: {           // 表单校验规则
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ]
  }
})

// 分页查询用户列表
const load = () => {
  request.get('/user/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()

// 重置搜索条件
const reset = () => {
  data.name = null
  load()
}

// 删除用户（二次确认）
const del = (id) => {
  ElMessageBox.confirm('您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/user/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 打开新增弹窗（清空表单）
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 打开编辑弹窗（深拷贝行数据到表单）
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增用户保存
const add = () => {
  request.post('/user/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑用户保存
const update = () => {
  request.put('/user/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存：根据表单是否有id判断新增还是编辑
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {  // 表单校验通过
      data.form.id ? update() : add()
    }
  })
}

// 表单头像上传组件的回调函数  res.data 就是头像的url
const handleFileUpload = (res) => {
  data.form.avatar = res.data
}
</script>
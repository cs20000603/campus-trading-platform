<template>
  <div class="front-container">
    <div style="display: flex; align-items: center; margin-bottom: 20px">
      <!-- 店铺商品管理页面：查看、新增、编辑、删除本店铺的商品，支持AI生成简介 -->
<h2 style="flex: 1">商品管理</h2>
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
    </div>

    <!-- 搜索筛选栏：按商品名称和分类查询 -->
    <div class="card" style="margin-bottom: 10px; padding: 15px">
      <el-input v-model="data.name" style="width: 200px; margin-right: 10px" placeholder="商品名称" clearable @clear="load"></el-input>
      <el-select v-model="data.categoryId" style="width: 150px; margin-right: 10px" placeholder="分类" clearable>
        <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <!-- 商品列表表格 -->
    <div class="card" style="margin-bottom: 10px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="图片" prop="img" width="100">
          <template #default="scope">
            <el-image v-if="scope.row.img" :src="scope.row.img" :preview-src-list="[scope.row.img]" style="width: 50px; height: 50px"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name" show-overflow-tooltip></el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="scope">
            <span v-if="scope.row.discountPrice" style="text-decoration: line-through; color: #999; margin-right: 5px">¥{{ scope.row.price }}</span>
            <b style="color: red">¥{{ scope.row.discountPrice || scope.row.price }}</b>
          </template>
        </el-table-column>
        <el-table-column label="库存" prop="store" width="80"></el-table-column>
        <el-table-column label="分类" prop="categoryName" width="100"></el-table-column>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === '上架' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销量" prop="saleCount" width="80"></el-table-column>
        <el-table-column label="创建时间" prop="time" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="card" style="padding: 15px">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <!-- 商品编辑弹窗：新增或编辑商品信息，支持AI生成简介 -->
    <el-dialog title="商品信息" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="name" label="名称">
          <el-input v-model="data.form.name" placeholder="请输入名称"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="图片">
          <el-upload :action="uploadUrl" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="price" label="价格">
          <el-input-number :min="0" v-model="data.form.price"></el-input-number>
        </el-form-item>
        <el-form-item label="折扣价">
          <el-input-number :min="0" v-model="data.form.discountPrice" placeholder="不填则无折扣"></el-input-number>
        </el-form-item>
        <el-form-item label="折扣截止">
          <el-date-picker v-model="data.form.discountEnd" type="datetime" placeholder="选择截止时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item prop="store" label="库存">
          <el-input-number :min="1" v-model="data.form.store"></el-input-number>
        </el-form-item>
        <el-form-item prop="categoryId" label="分类">
          <el-select style="width: 100%" v-model="data.form.categoryId">
            <el-option v-for="item in data.categoryList" :key="item.id" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="status" label="状态">
          <el-radio-group v-model="data.form.status">
            <el-radio-button value="上架">上架</el-radio-button>
            <el-radio-button value="下架">下架</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="description" label="简介">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input type="textarea" :rows="2" v-model="data.form.description" style="flex: 1"></el-input>
            <el-button type="warning" size="small" @click="aiGenerate" :loading="data.aiLoading" style="align-self: flex-start; white-space: nowrap">AI 生成</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import router from "@/router";

const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'  // 文件上传地址
const formRef = ref()  // 商品编辑表单引用

const shopId = router.currentRoute.value.query.shopId    // 从路由参数获取店铺ID
const shopType = router.currentRoute.value.query.shopType  // 从路由参数获取店铺类型

// 响应式数据对象
const data = reactive({
  pageNum: 1,         // 当前页码
  pageSize: 10,       // 每页条数
  total: 0,           // 商品总数
  formVisible: false, // 编辑弹窗是否显示
  form: {},           // 商品编辑表单数据
  tableData: [],      // 商品列表数据
  categoryList: [],   // 分类列表
  name: null,         // 商品名称搜索关键词
  categoryId: null,   // 分类筛选条件
  aiLoading: false,   // AI生成简介的加载状态
  rules: {            // 表单校验规则
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    img: [{ required: true, message: '请上传图片', trigger: 'blur' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    store: [{ required: true, message: '请输入库存', trigger: 'blur' }],
    categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  }
})

// 加载分类列表：根据店铺类型加载对应的分类
const loadCategory = () => {
  if (shopType) {
    request.get('/category/selectByShopType', { params: { shopType } }).then(res => {
      data.categoryList = res.data || []
    })
  } else {
    request.get('/category/selectAll').then(res => {
      data.categoryList = res.data
    })
  }
}
loadCategory()

// 商品图片上传成功回调
const handleImgSuccess = (res) => {
  data.form.img = res.data
}

// AI生成商品简介：根据商品名称和分类自动生成描述
const aiGenerate = () => {
  if (!data.form.name) {
    ElMessage.warning('请先输入商品名称')
    return
  }
  const category = data.categoryList.find(v => v.id === data.form.categoryId)
  data.aiLoading = true
  request.post('/ai/generateDesc', {
    name: data.form.name,
    category: category?.name
  }).then(res => {
    if (res.code === '200') {
      data.form.description = res.data
      ElMessage.success('已生成简介')
    } else {
      ElMessage.error(res.msg || '生成失败')
    }
  }).finally(() => {
    data.aiLoading = false
  })
}

// 分页查询本店铺的商品数据
const load = () => {
  request.get('/goods/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      categoryId: data.categoryId,
      shopId: shopId
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}
load()

// 新增商品：初始化默认值（状态上架、价格0、库存1）
const handleAdd = () => {
  data.form = { status: '上架', price: 0, store: 1, shopId: shopId }
  data.formVisible = true
}

// 编辑商品：深拷贝行数据打开弹窗
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增商品到后端
const add = () => {
  request.post('/goods/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('添加成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 更新商品信息
const update = () => {
  request.put('/goods/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('保存成功')
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

// 删除商品（需二次确认）
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/goods/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}
</script>

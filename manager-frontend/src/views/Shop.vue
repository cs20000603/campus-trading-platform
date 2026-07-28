<template>
  <div>
    <!-- 店铺管理页面：按名称/状态/类型筛选店铺，支持审核（线上/线下）、拒绝、歇业/重开、编辑、删除 -->
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 200px; margin-right: 10px" placeholder="店铺名称"></el-input>
      <el-select v-model="data.status" placeholder="状态" style="width: 120px; margin-right: 10px" clearable>
        <el-option label="全部" value=""></el-option>
        <el-option label="线上审核中" value="线上审核中"></el-option>
        <el-option label="线下审核中" value="线下审核中"></el-option>
        <el-option label="营业中" value="营业中"></el-option>
        <el-option label="已歇业" value="已歇业"></el-option>
        <el-option label="审核拒绝" value="审核拒绝"></el-option>
      </el-select>
      <el-select v-model="data.type" placeholder="店铺类型" style="width: 140px; margin-right: 10px" clearable>
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
      <el-table :data="data.tableData" stripe>
        <el-table-column label="ID" prop="id" width="60"></el-table-column>
        <el-table-column label="Logo" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.logo" :src="scope.row.logo" :preview-src-list="[scope.row.logo]" preview-teleported style="width: 40px; height: 40px; border-radius: 4px" />
            <span v-else style="color: #ccc">--</span>
          </template>
        </el-table-column>
        <el-table-column label="许可证" width="80">
          <template #default="scope">
            <el-image v-if="scope.row.license" :src="scope.row.license" :preview-src-list="[scope.row.license]" preview-teleported style="width: 40px; height: 40px; border-radius: 4px" />
            <span v-else style="color: #ccc">--</span>
          </template>
        </el-table-column>
        <el-table-column label="店铺名称" prop="name"></el-table-column>
        <el-table-column label="简介" prop="description" show-overflow-tooltip></el-table-column>
        <el-table-column label="电话" prop="phone" width="130"></el-table-column>
        <el-table-column label="地址" prop="address" show-overflow-tooltip></el-table-column>
        <el-table-column label="店主ID" prop="ownerId" width="80"></el-table-column>
        <el-table-column label="类型" prop="type" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.type">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '营业中' ? 'success' : scope.row.status === '线上审核中' ? 'warning' : scope.row.status === '线下审核中' ? '' : scope.row.status === '审核拒绝' ? 'danger' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="驳回理由" prop="rejectReason" width="160" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.rejectReason" style="color: #f56c6c">{{ scope.row.rejectReason }}</span>
            <span v-else style="color: #ccc">--</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="340">
          <template #default="scope">
            <el-button type="success" size="small" v-if="scope.row.status === '线上审核中'" @click="handleOnlineApprove(scope.row.id)">
              线上通过
            </el-button>
            <el-button type="primary" size="small" v-if="scope.row.status === '线下审核中'" @click="handleOfflineApprove(scope.row.id)">
              线下通过
            </el-button>
            <el-button type="danger" size="small" v-if="scope.row.status === '线上审核中' || scope.row.status === '线下审核中'" @click="handleReject(scope.row.id)">
              拒绝
            </el-button>
            <el-button type="danger" size="small" v-if="scope.row.status === '营业中'" @click="handleClose(scope.row)">
              歇业
            </el-button>
            <el-button type="warning" size="small" v-if="scope.row.status === '已歇业'" @click="handleOfflineApprove(scope.row.id)">
              重开
            </el-button>
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="店铺信息" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="经营类型">
          <el-select v-model="data.form.type" placeholder="请选择经营类型" clearable style="width: 100%">
            <el-option label="超市" value="超市"></el-option>
            <el-option label="水果店" value="水果店"></el-option>
            <el-option label="服装店" value="服装店"></el-option>
            <el-option label="蛋糕店" value="蛋糕店"></el-option>
            <el-option label="奶茶店" value="奶茶店"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="data.form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="data.form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="data.form.address" />
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
import {reactive, ref, onMounted, onUnmounted} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
import { onMessage, offMessage } from "@/utils/websocket";

// 表单引用
const formRef = ref()
// 响应式数据对象：分页 + 店铺列表 + 表单 + 筛选条件
const data = reactive({
  pageNum: 1,       // 当前页码
  pageSize: 10,     // 每页条数
  total: 0,         // 店铺总数
  formVisible: false,// 编辑弹窗是否显示
  form: {},         // 店铺表单数据
  tableData: [],    // 店铺列表
  name: null,       // 名称搜索关键词
  status: null,     // 状态筛选（审核中/营业中/已歇业/审核拒绝）
  type: null,       // 类型筛选（超市/水果店等）
  rules: {
    name: [
      { required: true, message: '请输入店铺名称', trigger: 'blur' },
    ]
  }
})

// 分页查询店铺列表（按名称/状态/类型筛选）
const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
  }
  if (data.name) params.name = data.name
  if (data.status) params.status = data.status
  if (data.type) params.type = data.type

  request.get('/shop/selectPage', { params }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/shop/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/shop/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? update() : add()
    }
  })
}

const handleOnlineApprove = (id) => {
  ElMessageBox.confirm('确认线上审核通过吗？通过后将进入线下审核。', '提示', { type: 'info' }).then(() => {
    request.put('/shop/onlineApprove/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('线上审核已通过')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleOfflineApprove = (id) => {
  ElMessageBox.confirm('确认线下审核通过吗？通过后店铺将正式营业。', '提示', { type: 'info' }).then(() => {
    request.put('/shop/offlineApprove/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleReject = (id) => {
  ElMessageBox.prompt('请输入拒绝原因或修改意见（商家可见）', '驳回申请', {
    type: 'warning',
    inputType: 'textarea',
    inputPlaceholder: '例如：经营许可证不清晰，请重新上传...',
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    request.put('/shop/reject/' + id, { reason: value }).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('已驳回，修改意见已发送')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleClose = (row) => {
  ElMessageBox.confirm('确认将该店铺设为歇业吗？', '提示', { type: 'warning' }).then(() => {
    request.put('/shop/update', { ...row, status: '已歇业' }).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('已设为歇业')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/shop/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const reset = () => {
  data.name = null
  data.status = null
  data.type = null
  load()
}

const handleWsMessage = (msg) => {
  if (msg.eventType === 'SHOP_APPLY') {
    ElMessage.info(msg.message)
    load()
  }
}

onMounted(() => onMessage(handleWsMessage))
onUnmounted(() => offMessage(handleWsMessage))
</script>

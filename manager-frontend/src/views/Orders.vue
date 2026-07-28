<template>
  <div>
    <!-- 订单管理页面：按订单编号/状态下单查询，支持修改订单状态和配送信息 -->
    <div class="card" style="margin-bottom: 5px; display: flex; gap: 10px; flex-wrap: wrap; align-items: center">
      <el-input v-model="data.orderNo" style="width: 250px" placeholder="请输入订单编号查询" clearable />
      <el-select v-model="data.statusFilter" placeholder="订单状态" style="width: 150px" clearable @change="load">
        <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe default-expand-all>
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px">
              <el-table :data="props.row.orderDetailList" border>
                <el-table-column label="商品图片" prop="goodsImg" width="100">
                  <template #default="scope">
                    <img :src="scope.row.goodsImg" alt="" style="width: 50px; height: 50px">
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip />
                <el-table-column label="商品单价" prop="goodsPrice" width="100" />
                <el-table-column label="数量" prop="num" width="100">
                  <template #default="scope">X {{ scope.row.num }}</template>
                </el-table-column>
                <el-table-column label="小计" width="150">
                  <template #default="scope">
                    <b style="color: red">{{ (scope.row.goodsPrice * scope.row.num).toFixed(2) }} 元</b>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="180">
          <template #default="scope">
            <b style="color: #333">{{ scope.row.orderNo }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总价格" width="100">
          <template #default="scope">
            <b style="color: red">{{ scope.row.total }}元</b>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="下单人" width="100" />
        <el-table-column prop="deliverType" label="配送类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
            <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间" width="170" />
        <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
        <el-table-column prop="deliver" label="配送信息" width="200" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="160" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)" :disabled="scope.row.status === '已取消' || scope.row.status === '已完成'">处理</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination @current-change="load" background layout="total, prev, pager, next"
        v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <!-- 处理订单弹窗：修改订单状态和配送信息 -->
    <el-dialog v-model="data.dialogVisible" title="处理订单" width="500px">
      <el-form :model="data.editForm" label-width="80px">
        <el-form-item label="订单编号">
          <span>{{ data.editForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag>{{ data.editForm.status }}</el-tag>
        </el-form-item>
        <el-form-item label="更新状态" required>
          <el-select v-model="data.editForm.newStatus" style="width: 100%" placeholder="请选择新状态">
            <el-option v-for="s in getNextStatuses(data.editForm.status)" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送信息">
          <el-input v-model="data.editForm.deliver" type="textarea" :rows="2" placeholder="如：顺丰快递 SF1234567890" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, onMounted, onUnmounted } from "vue";
import { ElMessageBox, ElMessage, ElNotification } from "element-plus";
import { onMessage, offMessage } from "@/utils/websocket";

// 订单状态流转顺序
const statusFlow = {
  '待接单': ['已出货', '已取消'],
  '已出货': ['已配送'],
  '已配送': ['已完成'],
}

const statusOptions = ['待接单', '已出货', '已配送', '已完成', '已取消']

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  orderNo: null,
  statusFilter: null,
  dialogVisible: false,
  editForm: { id: null, orderNo: '', status: '', newStatus: '', deliver: '' }
})

// 获取当前状态的下一可选状态
const getNextStatuses = (currentStatus) => {
  return statusFlow[currentStatus] || []
}

const load = () => {
  request.get('/orders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      orderNo: data.orderNo || undefined,
      status: data.statusFilter || undefined
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 打开处理订单弹窗
const handleEdit = (row) => {
  data.editForm = {
    id: row.id,
    orderNo: row.orderNo,
    status: row.status,
    newStatus: '',
    deliver: row.deliver || ''
  }
  data.dialogVisible = true
}

// 确认更新订单状态
const handleUpdate = () => {
  if (!data.editForm.newStatus) {
    ElMessage.warning('请选择新状态')
    return
  }
  request.put('/orders/update', {
    id: data.editForm.id,
    status: data.editForm.newStatus,
    deliver: data.editForm.deliver
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('订单状态已更新')
      data.dialogVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/orders/delete/' + id).then(res => {
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
  data.orderNo = null
  data.statusFilter = null
  data.pageNum = 1
  load()
}

// WebSocket监听：收到新订单或订单状态变更通知时刷新列表
const handleWsMessage = (msg) => {
  if (msg.eventType === 'ORDER_NEW' || msg.eventType === 'ORDER_STATUS') {
    ElNotification({ title: '订单通知', message: msg.message, type: 'info' })
    load()
  }
}

onMounted(() => onMessage(handleWsMessage))
onUnmounted(() => offMessage(handleWsMessage))
</script>

<style scoped>
.el-tag { font-weight: bold; }
.el-tag--warning { color: orange; background-color: #fff2de; }
</style>

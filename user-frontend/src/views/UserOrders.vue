<!-- 用户订单页面：查看订单列表、取消订单、确认收货、评价商品，支持WebSocket实时推送 -->
<template>
  <div class="front-container" style="width: 80%">
    <!-- 搜索栏：按订单编号和商品名称查询 -->
    <div style="margin-bottom: 10px;">
      <el-input clearable @clear="load" v-model="data.orderNo" style="width: 400px; height: 40px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
      <el-input clearable @clear="load" v-model="data.goodsName" style="width: 400px; height: 40px; margin-right: 10px" placeholder="请输入商品名称查询"></el-input>
      <el-button style="height: 40px" type="primary" @click="load">查 询</el-button>
    </div>

    <div class="card">
      <!-- 订单主表：支持展开查看订单明细 -->
      <el-table :data="data.tableData" stripe :cell-style="{'backgroundColor': '#e8efff'}" default-expand-all>
        <!-- 展开行：显示订单明细（商品图片、名称、单价、数量、小计） -->
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px">
              <el-table :data="props.row.orderDetailList" border>
                <el-table-column label="商品图片" prop="goodsImg" width="100">
                  <template #default="scope">
                    <img :src="scope.row.goodsImg" alt="" style="width: 50px; height: 50px">
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip></el-table-column>
                <el-table-column label="商品单价" prop="goodsPrice" width="100"></el-table-column>
                <el-table-column label="数量" prop="num" width="100">
                  <template #default="scope">
                    X {{ scope.row.num }}
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="150">
                  <template #default="scope">
                    <b style="color: red">{{ (scope.row.goodsPrice * scope.row.num).toFixed(2) }} 元</b>
                  </template>
                </el-table-column>
                <!-- 已完成订单才显示评价按钮 -->
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button @click="handleAddComment(scope.row)" type="success" v-if="props.row.status === '已完成'">评价</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单编号" width="240">
          <template #default="scope">
            <b style="color: #333">{{ scope.row.orderNo }}</b>
          </template>
        </el-table-column>
        <el-table-column prop="total" label="总价格">
          <template #default="scope">
            <b style="color: red">{{ scope.row.total }}元</b>
          </template>
        </el-table-column>
        <el-table-column prop="deliverType" label="配送类型"></el-table-column>
        <!-- 订单状态：使用不同颜色的标签展示 -->
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
            <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
            <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
            <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间"></el-table-column>
        <el-table-column prop="address" label="地址" width="300"></el-table-column>
        <el-table-column prop="deliver" label="配送信息" width="300"></el-table-column>
        <!-- 订单操作按钮：待接单可取消，已出货/已配送可确认收货 -->
        <el-table-column label="订单操作" align="center" width="120">
          <template #default="scope">
            <el-button @click="cancel(scope.row)"  type="danger" v-if="scope.row.status === '待接单'">取消</el-button>
            <el-button @click="done(scope.row)" type="primary" v-if="scope.row.status === '已出货' || scope.row.status === '已配送'">确认收货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <!-- 评价弹窗：评分和评价内容 -->
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
import {reactive, ref, onMounted, onUnmounted} from "vue";
import {ElMessageBox, ElMessage, ElNotification} from "element-plus";
import { onMessage, offMessage } from "@/utils/websocket";

const formRef = ref()  // 评价表单引用
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前登录用户
  pageNum: 1,         // 订单分页-当前页码
  pageSize: 3,        // 订单分页-每页条数
  total: 0,           // 订单分页-总记录数
  formVisible: false, // 评价弹窗是否显示
  form: {},           // 评价表单数据
  tableData: [],      // 订单列表数据
  orderNo: null,      // 订单编号搜索关键词
  goodsName: null,    // 商品名称搜索关键词
  rules: {            // 评价表单校验规则
    score: [
      { required: true, message: '请输入评分', trigger: 'change' },
    ],
    content: [
      { required: true, message: '请输入内容', trigger: 'blur' },
    ],
  }
})

// 分页查询当前用户的订单
const load = () => {
  request.get('/orders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      orderNo: data.orderNo,
      goodsName: data.goodsName,
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 取消订单：将订单状态改为"已取消"
const cancel = (row) => {
  ElMessageBox.confirm('您确认取消订单吗?', '二次确认', { type: 'warning' }).then(res => {
    data.form = row
    data.form.status = '已取消'
    updateOrder()
  }).catch(err => {})
}

// 确认收货：将订单状态改为"已完成"
const done = (row) => {
  ElMessageBox.confirm('您确认订单货物已经收到了吗?', '二次确认', { type: 'warning' }).then(res => {
    data.form = row
    data.form.status = '已完成'
    updateOrder()
  }).catch(err => {})
}

// 更新订单状态
const updateOrder = () => {
  request.put('/orders/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 打开评价弹窗：先查询是否已有评价，有则加载已有评价内容
const handleAddComment = (row) => {
  request.get('/comment/selectAll', {
    params: {
      orderId: row.orderId,
      goodsId: row.goodsId
    }
  }).then(res => {
    // 返回的是数组，有数据则加载已有评价，无数据则新建空的评价对象
    data.form = res.data?.length > 0 ? res.data[0] : { orderId: row.orderId, goodsId: row.goodsId, userId: data.user.id }
    data.formVisible = true
  })
}

// 新增评价
const addComment = () => {
  request.post('/comment/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑已有评价
const updateComment = () => {
  request.put('/comment/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存：根据是否有id判断新增还是更新
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      // data.form有id就是更新，没有就是新增
      data.form.id ? updateComment() : addComment()
    }
  })
}

// 处理WebSocket消息：订单状态更新时自动刷新列表
const handleWsMessage = (msg) => {
  if (msg.eventType === 'ORDER_STATUS') {
    ElNotification({ title: '订单状态更新', message: msg.message, type: 'info' })
    load()
  }
}

// 组件挂载时注册WebSocket监听，卸载时注销
onMounted(() => onMessage(handleWsMessage))
onUnmounted(() => offMessage(handleWsMessage))
</script>

<style scoped>
.el-tag {
  font-weight: bold;
}
.el-tag--warning {
  color: orange;
  background-color: #fff2de;
}
</style>
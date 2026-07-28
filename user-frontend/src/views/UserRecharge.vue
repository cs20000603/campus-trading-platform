<!-- 用户充值页面：查看充值记录、发起充值、查看账户余额 -->
<template>
  <div class="front-container">
    <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px">我的充值记录（{{ data.total }}）</div>
    <div class="card" style="padding: 20px">
      <!-- 顶部：日期筛选 + 账户余额显示 + 充值按钮 -->
      <div style="margin-bottom: 20px; display: flex; align-items: center">
        <div style="flex: 1">
          <el-date-picker style="width: 300px; margin-right: 10px" v-model="data.time" type="date" placeholder="请输入日期查询" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
        </div>

        <b style="margin-left: 20px; color: red">当前账户余额：{{ data.user.account }}元</b>
        <el-button @click="handleAdd" type="primary" style="margin-left: 20px">发起充值</el-button>
      </div>
      <!-- 充值记录表格 -->
      <div>
        <el-table :data="data.tableData" stripe>
          <el-table-column prop="money" label="充值金额">
            <template #default="scope">
              <b style="color: red">{{ scope.row.money }}元</b>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="支付方式"></el-table-column>
          <el-table-column prop="time" label="充值时间"></el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <!-- 充值弹窗：选择金额和支付方式 -->
    <el-dialog title="用户充值" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px; padding-top: 20px">
        <el-form-item label="充值金额" prop="money">
          <el-input-number style="width: 200px" :min="1" v-model="data.form.money" autocomplete="off" />
        </el-form-item>
        <!-- 支付方式：微信支付 / 支付宝 -->
        <el-form-item label="支付方式" prop="type" style="margin-top: 30px">
          <el-radio-group v-model="data.form.type">
            <el-radio value="微信支付"><img style="width: 100px; height: 40px" src="@/assets/imgs/wx.png" alt=""></el-radio>
            <el-radio value="支付宝"><img style="width: 100px; height: 40px" src="@/assets/imgs/zfb.png" alt=""></el-radio>
          </el-radio-group>
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

const formRef = ref()  // 充值表单引用
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),  // 当前登录用户
  pageNum: 1,         // 当前页码
  pageSize: 10,       // 每页条数
  total: 0,           // 充值记录总数
  formVisible: false, // 充值弹窗是否显示
  form: {},           // 充值表单数据（money, type）
  tableData: [],      // 充值记录列表
  time: null,         // 日期筛选条件
  rules: {            // 表单校验规则
    money: [
      { required: true, message: '请输入金额', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择支付方式', trigger: 'change' }
    ]
  }
})

// 重新加载账户余额
const loadAccount = () => {
  request.get('/user/selectById/' + data.user.id).then(res => {
    data.user.account = res.data.account
  })
}
loadAccount()

// 分页查询充值记录
const load = () => {
  request.get('/recharge/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id,
      time: data.time
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

// 打开充值弹窗，默认金额1元、微信支付
const handleAdd = () => {
  data.form = { money: 1, type: '微信支付' }
  data.formVisible = true
}

// 新增充值记录并刷新账户余额
const add = () => {
  data.form.userId = data.user.id
  request.post('/recharge/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      loadAccount()  // 充值后刷新余额
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 更新充值记录
const update = () => {
  request.put('/recharge/update', data.form).then(res => {
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

// 重置日期筛选
const reset = () => {
  data.time = null
  load()
}
</script>
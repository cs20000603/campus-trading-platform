<template>
  <div class="front-container">
    <div class="card" style="padding: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px">
        <!-- 求购板页面：发布求购需求、浏览他人求购信息、删除自己的求购 -->
<h3>求购板</h3>
        <el-button type="primary" @click="data.dialogVisible = true">发布求购</el-button>
      </div>

      <!-- 求购列表展示区 -->
      <div class="wanted-list" v-if="data.tableData.length > 0">
        <div class="wanted-item" v-for="item in data.tableData" :key="item.id">
          <div class="wanted-header">
            <span style="font-weight: bold; font-size: 15px">{{ item.title }}</span>
            <el-tag size="small" :type="item.status === '求购中' ? 'success' : 'info'">{{ item.status }}</el-tag>
          </div>
          <div v-if="item.description" style="color: #666; margin: 8px 0">{{ item.description }}</div>
          <div style="display: flex; gap: 10px; flex-wrap: wrap; margin: 6px 0">
            <span v-if="item.budget" style="color: red">预算: ￥{{ item.budget }}</span>
            <span v-if="item.category" style="color: #666">分类: {{ item.category }}</span>
            <span v-if="item.campusArea" style="color: #666">区域: {{ item.campusArea }}</span>
          </div>
          <div style="font-size: 12px; color: #999; display: flex; justify-content: space-between">
            <span>{{ item.userName }} · {{ item.createTime }}</span>
            <el-button v-if="isMine(item)" size="small" type="danger" @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无求购信息" />

      <div style="text-align: center; margin-top: 15px">
        <el-pagination background layout="prev, pager, next" :total="data.total" :page-size="10" v-model:current-page="data.pageNum" @current-change="load" />
      </div>
    </div>

    <el-dialog title="发布求购" v-model="data.dialogVisible" width="500px" destroy-on-close>
      <el-form :model="data.form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="data.form.title" placeholder="想求购什么？" maxlength="200" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="data.form.description" type="textarea" :rows="3" placeholder="具体需求的描述" />
        </el-form-item>
        <el-form-item label="预算">
          <el-input-number v-model="data.form.budget" :min="0" :precision="2" style="width: 200px" />
          <span style="margin-left: 6px; color: #999">元（选填）</span>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="data.form.category" style="width: 150px">
            <el-option label="数码" value="数码" />
            <el-option label="书籍" value="书籍" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服饰" value="服饰" />
            <el-option label="美妆" value="美妆" />
            <el-option label="运动" value="运动" />
            <el-option label="乐器" value="乐器" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="校区">
          <el-input v-model="data.form.campusArea" placeholder="所在校区或区域" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="data.saving">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const user = JSON.parse(localStorage.getItem("system-user") || "{}");

// 响应式数据对象
const data = reactive({
  dialogVisible: false,  // 发布求购弹窗是否显示
  pageNum: 1,            // 当前页码
  total: 0,              // 求购信息总数
  saving: false,         // 提交中的加载状态
  tableData: [],         // 求购列表数据
  form: { title: "", description: "", budget: undefined, category: "", campusArea: "" },  // 求购发布表单
});

// 分页查询求购信息
const load = () => {
  request.get("/idleWanted/selectPage", { params: { pageNum: data.pageNum, pageSize: 10 } }).then(res => {
    data.tableData = res.data?.list || [];
    data.total = res.data?.total || 0;
  });
};

// 判断当前求购信息是否属于当前登录用户
const isMine = (item) => item.userId === user.id;

// 提交求购发布
const submit = () => {
  if (!data.form.title) { ElMessage.warning("请输入标题"); return; }
  data.saving = true;
  request.post("/idleWanted/add", data.form).then(res => {
    if (res.code === "200") { ElMessage.success("发布成功"); data.dialogVisible = false; data.form = { title: "", description: "", budget: undefined, category: "", campusArea: "" }; load(); }
    else { ElMessage.error(res.msg); }
  }).finally(() => { data.saving = false; });
};

const handleDelete = (id) => {
  request.delete("/idleWanted/delete/" + id).then(res => {
    if (res.code === "200") { ElMessage.success("已删除"); load(); }
  });
};

load();
</script>

<style scoped>
.wanted-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.wanted-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

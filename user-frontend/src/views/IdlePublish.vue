<template>
  <div class="front-container">
    <div class="card" style="padding: 20px; max-width: 700px;">
      <!-- 发布闲置商品页面：填写标题、描述、图片、价格、成色等并提交 -->
<h3 style="margin-bottom: 20px">发布闲置商品</h3>
      <el-form :model="data.form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="data.form.title" placeholder="请输入商品标题" maxlength="200" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="data.form.description" type="textarea" :rows="5" placeholder="描述商品的使用情况、购买时间等" />
        </el-form-item>
        <el-form-item label="图片上传" required>
          <el-upload :action="uploadUrl" list-type="picture-card" :on-success="handleImgSuccess" :on-remove="handleImgRemove" :file-list="data.fileList" multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="color: #999; font-size: 12px">第一张为封面图，最多上传9张</div>
        </el-form-item>
        <el-form-item label="售价" required>
          <el-input-number v-model="data.form.price" :min="0.01" :precision="2" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元</span>
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="data.form.originalPrice" :min="0" :precision="2" style="width: 200px" />
          <span style="margin-left: 8px; color: #999">元（选填）</span>
        </el-form-item>
        <el-form-item label="成色" required>
          <el-select v-model="data.form.condition" style="width: 200px">
            <el-option label="全新" value="全新" />
            <el-option label="几乎全新" value="几乎全新" />
            <el-option label="轻微使用" value="轻微使用" />
            <el-option label="明显痕迹" value="明显痕迹" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送方式" required>
          <el-radio-group v-model="data.form.deliveryType">
            <el-radio value="自提">自提</el-radio>
            <el-radio value="可送">可送</el-radio>
            <el-radio value="均可">均可</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="校区/区域">
          <el-input v-model="data.form.campusArea" placeholder="如：东区宿舍1号楼" style="width: 250px" />
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
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="data.saving">发布</el-button>
          <el-button @click="$router.push('/front/idleSquare')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import router from "@/router";
import { ElMessage } from "element-plus";

const uploadUrl = import.meta.env.VITE_BASE_URL + "/files/upload";
const uploadedImages = [];

// 响应式数据对象
const data = reactive({
  form: { title: "", description: "", price: 0.01, originalPrice: undefined, condition: "轻微使用", deliveryType: "均可", campusArea: "", category: "" },
  fileList: [],   // 上传文件列表（组件展示用）
  saving: false,  // 提交中的加载状态
});

// 图片上传成功：将返回的URL存入uploadedImages数组
const handleImgSuccess = (res) => {
  const url = res.data || res;
  uploadedImages.push(url);
};

// 移除图片：同步删除uploadedImages中对应的URL
const handleImgRemove = (file) => {
  const url = file.response?.data || file.url;
  const idx = uploadedImages.indexOf(url);
  if (idx > -1) uploadedImages.splice(idx, 1);
};

// 提交发布：校验必填字段后调用后端接口
const submit = () => {
  if (!data.form.title) { ElMessage.warning("请输入标题"); return; }
  if (uploadedImages.length === 0) { ElMessage.warning("请上传至少一张图片"); return; }
  if (!data.form.price || data.form.price <= 0) { ElMessage.warning("请填写价格"); return; }
  const user = JSON.parse(localStorage.getItem('system-user') || '{}');
  if (!user.id) { ElMessage.warning("请先登录"); return; }
  data.saving = true;
  const form = { ...data.form, images: uploadedImages.join(","), sellerId: user.id, sellerName: user.name || user.username };
  request.post("/idleGoods/add", form).then(res => {
    if (res.code === "200") { ElMessage.success("发布成功！"); router.push("/front/idleSquare"); }
    else { ElMessage.error(res.msg); data.saving = false; }
  }).catch(() => { data.saving = false; });
};
</script>

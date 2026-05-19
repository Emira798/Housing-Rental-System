<template>
  <div class="house-container">
    <div class="page-header">
      <h2>房屋管理</h2>
      <div class="header-buttons">
        <el-button type="success" @click="exportToExcel" :icon="Download">导出Excel</el-button>
        <el-button type="primary" @click="handleAddClick" :icon="Plus">添加房屋</el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房屋标题">
          <el-input v-model="searchForm.title" placeholder="请输入房屋标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input-number v-model="searchForm.minPrice" placeholder="最低价" :min="0" :precision="2" :step="100" />
          <span class="range-separator">-</span>
          <el-input-number v-model="searchForm.maxPrice" placeholder="最高价" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="房屋类型">
          <el-select v-model="searchForm.typeId" placeholder="请选择类型" clearable>
            <el-option v-for="item in houseTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房屋状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option :value="0" label="下架" />
            <el-option :value="1" label="待出租" />
            <el-option :value="2" label="已出租" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-area">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ backgroundColor: '#EFF6FF', color: '#1D4ED8' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" fixed />
        <el-table-column prop="title" label="房屋标题" min-width="180" fixed />
        <el-table-column prop="area" label="面积(㎡)" width="100" align="center" />
        <el-table-column prop="price" label="价格(元/月)" width="120" align="center" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="typeName" label="房屋类型" width="100" align="center" />
        <el-table-column label="房屋图片" width="100" align="center">
          <template #default="scope">
            <el-image
              v-if="getFirstImage(scope.row.images)"
              :src="formatImageUrl(getFirstImage(scope.row.images))"
              :preview-src-list="formatImageList(scope.row.images)"
              style="width: 50px; height: 50px"
                    :preview-teleported="true"
              fit="cover"
            />
            <span v-else class="no-image">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="landlordName" label="房东" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">下架</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">待出租</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning">已出租</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              text
              @click="handleEdit(scope.row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status !== 0"
              type="info"
              size="small"
              text
              @click="handleChangeStatus(scope.row, 0)"
              :icon="Bottom"
            >
              下架
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              text
              @click="handleChangeStatus(scope.row, 1)"
              :icon="Top"
            >
              上架
            </el-button>
            <el-button
              v-if="scope.row.status !== 2"
              type="danger"
              size="small"
              text
              @click="handleDelete(scope.row)"
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          @update:current-page="val => currentPage = val"
          @update:page-size="val => pageSize = val"
          background
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加房屋' : '编辑房屋'"
      width="800px"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
        status-icon
      >
        <el-form-item label="房屋标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入房屋标题" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="area">
              <el-input-number v-model="form.area" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格(元/月)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="房屋地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入房屋地址" />
        </el-form-item>
        
        <el-form-item label="房屋类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择房屋类型" style="width: 100%">
            <el-option v-for="item in houseTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="房屋状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">下架</el-radio>
            <el-radio :label="1">待出租</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="房屋描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入房屋描述"
            rows="4"
          />
        </el-form-item>
        
        <el-form-item label="房屋图片" prop="images">
          <el-upload
            action="#"
            list-type="picture-card"
            :http-request="uploadImage"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :file-list="imageFileList"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="previewVisible" title="图片预览">
            <img w-full :src="previewUrl" alt="Preview Image" />
          </el-dialog>
        </el-form-item>
        
        <el-form-item label="配套设施" prop="facilities">
          <el-checkbox-group v-model="facilitiesSelected">
            <el-checkbox label="wifi">WiFi</el-checkbox>
            <el-checkbox label="elevator">电梯</el-checkbox>
            <el-checkbox label="airConditioner">空调</el-checkbox>
            <el-checkbox label="washer">洗衣机</el-checkbox>
            <el-checkbox label="refrigerator">冰箱</el-checkbox>
            <el-checkbox label="television">电视</el-checkbox>
            <el-checkbox label="waterHeater">热水器</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Edit, Delete, Search, Refresh, Top, Bottom, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'
import * as XLSX from 'xlsx'
import { useUserStore } from '@/store/user'
// 基础API路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const userStore = useUserStore()
// 房屋类型选项
const houseTypeOptions = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',
  minPrice: null,
  maxPrice: null,
  typeId: null,
  status: null
})

// 添加/编辑表单
const dialogVisible = ref(false)
const dialogType = ref('add') // add 或 edit
const submitLoading = ref(false)
const form = reactive({
  id: null,
  title: '',
  description: '',
  area: 0,
  price: 0,
  address: '',
  typeId: null,
  status: 1,
  images: '',
  facilities: ''
})

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')
const imageFileList = ref([])

// 配套设施选择
const facilitiesSelected = ref([])

const formRef = ref(null)
const rules = {
  title: [
    { required: true, message: '请输入房屋标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入房屋地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择房屋类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入房屋描述', trigger: 'blur' }
  ]
}

// 生命周期钩子
onMounted(() => {
  // 获取房屋类型列表
  fetchHouseTypeList()

  // 获取房屋列表
  fetchData()
})

// 获取房屋类型列表
const fetchHouseTypeList = async () => {
  try {

    await request.get('/house-types', {}, {
      onSuccess: (res) => {
        houseTypeOptions.value = res
      }
    })
  } catch (error) {
    console.error('获取房屋类型列表失败:', error)
  }
}


// 获取表格数据
const fetchData = async () => {
  loading.value = true
  try {
    await request.get('/houses/page', {
      title: searchForm.title,
      minPrice: searchForm.minPrice,
      maxPrice: searchForm.maxPrice,
      typeId: searchForm.typeId,
      landLordId: userStore.isLandlord ? userStore.userInfo.id : null,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records||[]
        total.value = res.total||0
      }
    })
  } catch (error) {
    console.error('获取房屋列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'title' ? '' : null
  })
  currentPage.value = 1
  fetchData()
}

// 每页数量变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchData()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchData()
}

// 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return null
  try {
    const imageArray = JSON.parse(images)
    return imageArray.length > 0 ? imageArray[0] : null
  } catch (e) {
    return null
  }
}

// 格式化图片URL
const formatImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return baseAPI + path
}

// 格式化图片列表
const formatImageList = (images) => {
  if (!images) return []
  try {
    const imageArray = JSON.parse(images)
    return imageArray.map(img => formatImageUrl(img))
  } catch (e) {
    return []
  }
}

// 添加按钮点击
const handleAddClick = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  resetForm()
  
  // 复制数据到表单
  Object.keys(form).forEach(key => {
    if (key in row) {
      form[key] = row[key]
    }
  })
  
  // 处理图片列表
  if (row.images) {
    try {
      const imageArray = JSON.parse(row.images)
      imageFileList.value = imageArray.map((url, index) => ({
        name: `图片${index + 1}`,
        url: formatImageUrl(url)
      }))
    } catch (e) {
      imageFileList.value = []
    }
  }
  
  // 处理配套设施
  if (row.facilities) {
    try {
      const facilitiesObj = JSON.parse(row.facilities)
      facilitiesSelected.value = Object.keys(facilitiesObj).filter(key => facilitiesObj[key])
    } catch (e) {
      facilitiesSelected.value = []
    }
  }
  
  dialogVisible.value = true
}

// 修改房屋状态
const handleChangeStatus = (row, status) => {
  const statusText = status === 0 ? '下架' : '上架'
  ElMessageBox.confirm(`确认将房屋「${row.title}」${statusText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/houses/${row.id}/status`, null, {
        params: { status },
        successMsg: `${statusText}成功`,
        errorMsg: `${statusText}失败`,
        onSuccess: () => {
          fetchData()
        }
      })
    } catch (error) {
      console.error(`${statusText}房屋失败:`, error)
    }
  }).catch(() => {
    // 取消操作，不做任何处理
  })
}

// 删除房屋
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除房屋「${row.title}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/houses/${row.id}`, {
        successMsg: '删除成功',
        errorMsg: '删除失败',
        onSuccess: () => {
          fetchData()
        }
      })
    } catch (error) {
      console.error('删除房屋失败:', error)
    }
  }).catch(() => {
    // 取消操作，不做任何处理
  })
}

// 上传图片前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  return true
}

// 自定义上传
const uploadImage = async (options) => {
  const { file } = options
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    await request.post('/file/upload/img', formData, {
      transformRequest: [(data) => data],
      successMsg: '图片上传成功',
      onSuccess: (data) => {
        // 添加到图片列表
        imageFileList.value.push({
          name: file.name,
          url: formatImageUrl(data)
        })
        options.onSuccess(data)
      }
    })
  } catch (error) {
    console.error('图片上传失败:', error)
    options.onError(error)
  }
}

// 图片预览
const handlePreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

// 移除图片
const handleRemove = (file, fileList) => {
  imageFileList.value = fileList
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.title = ''
  form.description = ''
  form.area = 0
  form.price = 0
  form.address = ''
  form.typeId = null
  form.status = 1
  form.images = ''
  form.facilities = ''
  
  imageFileList.value = []
  facilitiesSelected.value = []
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 处理图片信息
        const images = imageFileList.value.map(file => {
          // 从完整URL提取路径部分
          const url = file.url
          if (url.startsWith(baseAPI)) {
            return url.replace(baseAPI, '')
          }
          return url
        })
        form.images = JSON.stringify(images)
        
        // 处理配套设施
        const facilities = {}
        const allFacilities = ['wifi', 'elevator', 'airConditioner', 'washer', 'refrigerator', 'television', 'waterHeater']
        allFacilities.forEach(facility => {
          facilities[facility] = facilitiesSelected.value.includes(facility)
        })
        form.facilities = JSON.stringify(facilities)
        
        if (dialogType.value === 'add') {
          // 添加
          await request.post('/houses', form, {
            successMsg: '添加成功',
            errorMsg: '添加失败',
            onSuccess: () => {
              dialogVisible.value = false
              fetchData()
            }
          })
        } else {
          // 编辑
          await request.put(`/houses/${form.id}`, form, {
            successMsg: '更新成功',
            errorMsg: '更新失败',
            onSuccess: () => {
              dialogVisible.value = false
              fetchData()
            }
          })
        }
      } catch (error) {
        console.error(`${dialogType.value === 'add' ? '添加' : '更新'}房屋失败:`, error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 将 DateUtils 暴露给模板
const formatDateTime = (date) => {
  return DateUtils.formatDateTime(date);
}

// 导出Excel
const exportToExcel = () => {
  // 准备导出数据
  const exportData = tableData.value.map(item => ({
    '房屋标题': item.title,
    '面积(㎡)': item.area,
    '价格(元/月)': item.price,
    '地址': item.address,
    '房屋类型': item.typeName,
    '房东': item.landlordName,
    '状态': item.status === 0 ? '下架' : item.status === 1 ? '待出租' : '已出租',
    '创建时间': DateUtils.formatDateTime(item.createTime)
  }))

  try {
    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '房屋列表')

    // 设置列宽
    const columnWidths = [
      { wch: 30 }, // 房屋标题
      { wch: 10 }, // 面积
      { wch: 12 }, // 价格
      { wch: 40 }, // 地址
      { wch: 15 }, // 房屋类型
      { wch: 15 }, // 房东
      { wch: 10 }, // 状态
      { wch: 20 }, // 创建时间
    ]
    worksheet['!cols'] = columnWidths

    // 导出文件
    const fileName = `房屋列表_${DateUtils.formatDate(new Date())}.xlsx`
    XLSX.writeFile(workbook, fileName)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('导出Excel失败')
  }
}
</script>

<style lang="scss" scoped>
.house-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      font-size: 24px;
      color: #1D4ED8;
      margin: 0;
    }

    .header-buttons {
      display: flex;
      gap: 10px;
    }
  }
  
  .search-area {
    background: #FFFFFF;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
    margin-bottom: 20px;
    
    .range-separator {
      margin: 0 10px;
    }
  }
  
  .table-area {
    background: #FFFFFF;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(59, 130, 246, 0.08);
    
    .no-image {
      color: #909399;
      font-size: 12px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

:deep(.el-upload--picture-card) {
  background-color: #f0f9ff;
  border-color: #bfdbfe;
  &:hover {
    border-color: #3b82f6;
  }
}
</style> 
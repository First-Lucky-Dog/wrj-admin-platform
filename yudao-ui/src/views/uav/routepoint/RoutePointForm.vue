<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="航线" prop="routeId">
            <el-select
              v-model="formData.routeId"
              placeholder="请选择航线"
              clearable
              class="!w-240px"
            >
              <el-option :label="item.routeName" :value="item.id" :key="index" v-for="(item, index) in routeOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="点位序号" prop="seqNo">
            <el-input v-model="formData.seqNo" placeholder="请输入点位序号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度" prop="lng">
            <el-input v-model="formData.lng" placeholder="请输入经度" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度" prop="lat">
            <el-input v-model="formData.lat" placeholder="请输入纬度" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="高度" prop="alt">
            <el-input v-model="formData.alt" placeholder="请输入高度" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="速度" prop="speedMps">
            <el-input v-model="formData.speedMps" placeholder="请输入速度" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="动作类型" prop="actionType">
            <el-select v-model="formData.actionType" placeholder="请选择动作类型">
              <el-option
                v-for="dict in getStrDictOptions(DICT_TYPE.ACTION_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="动作参数" prop="actionParam">
            <el-input v-model="formData.actionParam" placeholder="请输入动作参数" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { getStrDictOptions, DICT_TYPE } from '@/utils/dict'
import { RoutePointApi, RoutePoint } from '@/api/uav/routepoint'
import { Route, RouteApi } from "@/api/uav/route"

/** 航线点位 表单 */
defineOptions({ name: 'RoutePointForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  routeId: undefined,
  seqNo: undefined,
  lng: undefined,
  lat: undefined,
  alt: undefined,
  speedMps: undefined,
  actionType: undefined,
  actionParam: undefined,
  remark: undefined
})
const formRules = reactive({
  routeId: [{ required: true, message: '航线不能为空', trigger: 'blur' }],
  seqNo: [{ required: true, message: '点位序号不能为空', trigger: 'blur' }],
  lng: [{ required: true, message: '经度不能为空', trigger: 'blur' }],
  lat: [{ required: true, message: '纬度不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

const routeOptions = ref<Route[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await RoutePointApi.getRoutePoint(id)
    } finally {
      formLoading.value = false
    }
  }
  routeOptions.value = await RouteApi.getRouteSelect()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as RoutePoint
    if (formType.value === 'create') {
      await RoutePointApi.createRoutePoint(data)
      message.success(t('common.createSuccess'))
    } else {
      await RoutePointApi.updateRoutePoint(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    routeId: undefined,
    seqNo: undefined,
    lng: undefined,
    lat: undefined,
    alt: undefined,
    speedMps: undefined,
    actionType: undefined,
    actionParam: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

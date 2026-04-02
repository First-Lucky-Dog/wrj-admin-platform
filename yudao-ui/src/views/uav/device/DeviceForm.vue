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
          <el-form-item label="设备编码" prop="deviceCode">
            <el-input v-model="formData.deviceCode" placeholder="请输入设备编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="型号" prop="model">
            <el-input v-model="formData.model" placeholder="请输入型号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="在线状态" prop="onlineStatus">
            <el-select v-model="formData.onlineStatus" placeholder="请选择在线状态">
              <el-option label="离线" :value="0" />
              <el-option label="在线" :value="1" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="飞行状态" prop="flightStatus">
            <el-select v-model="formData.flightStatus" placeholder="请选择飞行状态">
              <el-option label="待机" :value="0" />
              <el-option label="飞行中" :value="1" />
              <el-option label="返航" :value="2" />
              <el-option label="故障" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电量" prop="batteryLevel">
            <el-input v-model="formData.batteryLevel" placeholder="请输入电量" />
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
          <el-form-item label="最后心跳时间" prop="lastHeartbeatTime">
            <el-date-picker
              v-model="formData.lastHeartbeatTime"
              type="date"
              value-format="x"
              placeholder="选择最后心跳时间"
            />
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
import { DeviceApi, Device } from '@/api/uav/device'

/** 无人机设备 表单 */
defineOptions({ name: 'DeviceForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  deviceCode: undefined,
  deviceName: undefined,
  model: undefined,
  onlineStatus: undefined,
  flightStatus: undefined,
  batteryLevel: undefined,
  lng: undefined,
  lat: undefined,
  alt: undefined,
  lastHeartbeatTime: undefined,
  remark: undefined
})
const formRules = reactive({
  deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  onlineStatus: [{ required: true, message: '在线状态不能为空', trigger: 'change' }],
  flightStatus: [{ required: true, message: '飞行状态不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

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
      formData.value = await DeviceApi.getDevice(id)
    } finally {
      formLoading.value = false
    }
  }
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
    const data = formData.value as unknown as Device
    if (formType.value === 'create') {
      await DeviceApi.createDevice(data)
      message.success(t('common.createSuccess'))
    } else {
      await DeviceApi.updateDevice(data)
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
    deviceCode: undefined,
    deviceName: undefined,
    model: undefined,
    onlineStatus: undefined,
    flightStatus: undefined,
    batteryLevel: undefined,
    lng: undefined,
    lat: undefined,
    alt: undefined,
    lastHeartbeatTime: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

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
          <el-form-item label="任务" prop="missionId">
            <el-select
              v-model="formData.missionId"
              placeholder="请选择任务"
              clearable
              class="!w-240px"
            >
              <el-option :label="item.missionNo" :value="item.id" :key="index" v-for="(item, index) in missionOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备" prop="deviceId">
            <el-select
              v-model="formData.deviceId"
              placeholder="请选择设备"
              clearable
              class="!w-240px"
            >
              <el-option :label="item.deviceCode" :value="item.id" :key="index" v-for="(item, index) in deviceOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="轨迹时间" prop="trackTime">
            <el-date-picker
              v-model="formData.trackTime"
              type="date"
              value-format="x"
              placeholder="选择轨迹时间"
            />
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
          <el-form-item label="电量" prop="batteryLevel">
            <el-input v-model="formData.batteryLevel" placeholder="请输入电量" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="航向角" prop="heading">
            <el-input v-model="formData.heading" placeholder="请输入航向角" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="扩展数据" prop="extraJson">
            <el-input v-model="formData.extraJson" type="textarea" placeholder="请输入扩展数据" />
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
import { TrackPointApi, TrackPoint } from '@/api/uav/trackpoint'
import { Mission, MissionApi } from "@/api/uav/mission"
import { Device, DeviceApi } from "@/api/uav/device"

/** 飞行轨迹点 表单 */
defineOptions({ name: 'TrackPointForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  missionId: undefined,
  deviceId: undefined,
  trackTime: undefined,
  lng: undefined,
  lat: undefined,
  alt: undefined,
  speedMps: undefined,
  batteryLevel: undefined,
  heading: undefined,
  extraJson: undefined
})
const formRules = reactive({
  deviceId: [{ required: true, message: '设备不能为空', trigger: 'blur' }],
  trackTime: [{ required: true, message: '轨迹时间不能为空', trigger: 'blur' }],
  lng: [{ required: true, message: '经度不能为空', trigger: 'blur' }],
  lat: [{ required: true, message: '纬度不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

const missionOptions = ref<Mission[]>([])
const deviceOptions = ref<Device[]>([])

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
      formData.value = await TrackPointApi.getTrackPoint(id)
    } finally {
      formLoading.value = false
    }
  }
  missionOptions.value = await MissionApi.getMissionSelect()
  deviceOptions.value = await DeviceApi.getDeviceSelect()
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
    const data = formData.value as unknown as TrackPoint
    if (formType.value === 'create') {
      await TrackPointApi.createTrackPoint(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrackPointApi.updateTrackPoint(data)
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
    missionId: undefined,
    deviceId: undefined,
    trackTime: undefined,
    lng: undefined,
    lat: undefined,
    alt: undefined,
    speedMps: undefined,
    batteryLevel: undefined,
    heading: undefined,
    extraJson: undefined
  }
  formRef.value?.resetFields()
}
</script>

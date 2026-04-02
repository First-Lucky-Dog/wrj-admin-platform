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
          <el-form-item label="任务编号" prop="missionNo">
            <el-input v-model="formData.missionNo" placeholder="请输入任务编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备" prop="deviceId">
            <el-select v-model="formData.deviceId" placeholder="请选择设备">
              <el-option :label="item.deviceName" :value="item.id" :key="index" v-for="(item, index) in deviceOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="航线" prop="routeId">
            <el-select v-model="formData.routeId" placeholder="请选择航线">
              <el-option :label="item.routeName" :value="item.id" :key="index" v-for="(item, index) in routeOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="任务类型" prop="missionType">
            <el-select v-model="formData.missionType" placeholder="请选择任务类型">
              <el-option label="按模板" :value="1" />
              <el-option label="临时规划" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择状态">
              <el-option label="待执行" :value="0" />
              <el-option label="执行中" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="失败" :value="3" />
              <el-option label="取消" :value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划开始时间" prop="planStartTime">
            <el-date-picker
              v-model="formData.planStartTime"
              type="date"
              value-format="x"
              placeholder="选择计划开始时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实际开始时间" prop="startTime">
            <el-date-picker
              v-model="formData.startTime"
              type="date"
              value-format="x"
              placeholder="选择实际开始时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实际结束时间" prop="endTime">
            <el-date-picker
              v-model="formData.endTime"
              type="date"
              value-format="x"
              placeholder="选择实际结束时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实际里程" prop="actualDistanceM">
            <el-input v-model="formData.actualDistanceM" placeholder="请输入实际里程" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实际时长" prop="actualDurationS">
            <el-input v-model="formData.actualDurationS" placeholder="请输入实际时长" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="失败原因" prop="failReason">
            <el-input v-model="formData.failReason" type="textarea" placeholder="请输入失败原因" />
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
import { MissionApi, Mission } from '@/api/uav/mission'
import { Device, DeviceApi } from "@/api/uav/device"
import { Route, RouteApi } from "@/api/uav/route"

/** 飞行任务 表单 */
defineOptions({ name: 'MissionForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  missionNo: undefined,
  deviceId: undefined,
  routeId: undefined,
  missionType: undefined,
  status: undefined,
  planStartTime: undefined,
  startTime: undefined,
  endTime: undefined,
  actualDistanceM: undefined,
  actualDurationS: undefined,
  failReason: undefined,
  remark: undefined
})
const formRules = reactive({
  missionNo: [{ required: true, message: '任务编号不能为空', trigger: 'blur' }],
  deviceId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
  missionType: [{ required: true, message: '任务类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

const deviceOptions = ref<Device[]>([])
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
      formData.value = await MissionApi.getMission(id)
    } finally {
      formLoading.value = false
    }
  }

  deviceOptions.value = await DeviceApi.getDeviceSelect()
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
    const data = formData.value as unknown as Mission
    if (formType.value === 'create') {
      await MissionApi.createMission(data)
      message.success(t('common.createSuccess'))
    } else {
      await MissionApi.updateMission(data)
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
    missionNo: undefined,
    deviceId: undefined,
    routeId: undefined,
    missionType: undefined,
    status: undefined,
    planStartTime: undefined,
    startTime: undefined,
    endTime: undefined,
    actualDistanceM: undefined,
    actualDurationS: undefined,
    failReason: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

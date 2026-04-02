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
          <el-form-item label="设备" prop="deviceId">
            <el-select v-model="formData.deviceId" placeholder="请选择设备">
              <el-option :label="item.deviceName" :value="item.id" :key="index" v-for="(item, index) in deviceOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="任务" prop="missionId">
            <el-select v-model="formData.missionId" placeholder="请选择任务">
              <el-option :label="item.missionNo" :value="item.id" :key="index" v-for="(item, index) in missionOptions"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="指令类型" prop="commandType">
            <el-select v-model="formData.commandType" placeholder="请选择指令类型">
              <el-option
                v-for="dict in getStrDictOptions(DICT_TYPE.COMMAND_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="指令参数" prop="commandPayload">
            <el-input v-model="formData.commandPayload" placeholder="请输入指令参数" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下发状态" prop="sendStatus">
            <el-select v-model="formData.sendStatus" placeholder="请选择下发状态">
              <el-option label="待下发" :value="0" />
              <el-option label="待下发" :value="1" />
              <el-option label="待下发" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="回执状态" prop="ackStatus">
            <el-select v-model="formData.ackStatus" placeholder="请选择回执状态">
              <el-option label="无回执" :value="0" />
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="操作人ID" prop="operatorId">
            <el-input v-model="formData.operatorId" placeholder="请输入操作人ID" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="formData.operatorName" placeholder="请输入操作人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下发时间" prop="sendTime">
            <el-date-picker
              v-model="formData.sendTime"
              type="date"
              value-format="x"
              placeholder="选择下发时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="回执时间" prop="ackTime">
            <el-date-picker
              v-model="formData.ackTime"
              type="date"
              value-format="x"
              placeholder="选择回执时间"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="回执信息" prop="ackMessage">
            <el-input v-model="formData.ackMessage" type="textarea" placeholder="请输入回执信息" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" placeholder="请输入备注" />
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
import { CommandLogApi, CommandLog } from '@/api/uav/commandlog'
import { Device, DeviceApi } from "@/api/uav/device"
import { Mission, MissionApi } from "@/api/uav/mission"

/** 控制指令日志 表单 */
defineOptions({ name: 'CommandLogForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  deviceId: undefined,
  missionId: undefined,
  commandType: undefined,
  commandPayload: undefined,
  sendStatus: undefined,
  ackStatus: undefined,
  ackMessage: undefined,
  operatorId: undefined,
  operatorName: undefined,
  sendTime: undefined,
  ackTime: undefined,
  remark: undefined
})
const formRules = reactive({
  deviceId: [{ required: true, message: '设备不能为空', trigger: 'change' }],
  missionId: [{ required: true, message: '任务不能为空', trigger: 'change' }],
  commandType: [{ required: true, message: '指令类型不能为空', trigger: 'change' }],
  sendStatus: [{ required: true, message: '下发状态不能为空', trigger: 'change' }],
  ackStatus: [{ required: true, message: '回执状态不能为空', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

const deviceOptions = ref<Device[]>([])
const missionOptions = ref<Mission[]>([])

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
      formData.value = await CommandLogApi.getCommandLog(id)
    } finally {
      formLoading.value = false
    }
  }
  deviceOptions.value = await DeviceApi.getDeviceSelect()
  missionOptions.value = await MissionApi.getMissionSelect()
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
    const data = formData.value as unknown as CommandLog
    if (formType.value === 'create') {
      await CommandLogApi.createCommandLog(data)
      message.success(t('common.createSuccess'))
    } else {
      await CommandLogApi.updateCommandLog(data)
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
    deviceId: undefined,
    missionId: undefined,
    commandType: undefined,
    commandPayload: undefined,
    sendStatus: undefined,
    ackStatus: undefined,
    ackMessage: undefined,
    operatorId: undefined,
    operatorName: undefined,
    sendTime: undefined,
    ackTime: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

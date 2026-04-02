<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="任务" prop="missionId">
        <el-select
          v-model="queryParams.missionId"
          placeholder="请选择任务"
          clearable
          class="!w-240px"
        >
          <el-option :label="item.missionNo" :value="item.id" :key="index" v-for="(item, index) in missionOptions"/>
        </el-select>
      </el-form-item>
      <el-form-item label="设备" prop="deviceId">
        <el-select
          v-model="queryParams.deviceId"
          placeholder="请选择设备"
          clearable
          class="!w-240px"
        >
          <el-option :label="item.deviceName" :value="item.id" :key="index" v-for="(item, index) in deviceOptions"/>
        </el-select>
      </el-form-item>
      <el-form-item label="轨迹时间" prop="trackTime">
        <el-date-picker
          v-model="queryParams.trackTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['uav:track-point:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['uav:track-point:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['uav:track-point:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
        row-key="id"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        @selection-change="handleRowCheckboxChange"
    >
    <el-table-column type="selection" width="55" />
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="任务ID" align="center" prop="missionId" />
      <el-table-column label="设备ID" align="center" prop="deviceId" />
<!--      <el-table-column-->
<!--        label="轨迹时间"-->
<!--        align="center"-->
<!--        prop="trackTime"-->
<!--        :formatter="dateFormatter"-->
<!--        width="180px"-->
<!--      />-->
      <el-table-column label="经度" align="center" prop="lng" />
      <el-table-column label="纬度" align="center" prop="lat" />
      <el-table-column label="高度" align="center" prop="alt" />
      <el-table-column label="速度" align="center" prop="speedMps" />
      <el-table-column label="电量" align="center" prop="batteryLevel" />
      <el-table-column label="航向角" align="center" prop="heading" />
<!--      <el-table-column label="扩展数据" align="center" prop="extraJson" />-->
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['uav:track-point:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['uav:track-point:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <TrackPointForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { TrackPointApi, TrackPoint } from '@/api/uav/trackpoint'
import TrackPointForm from './TrackPointForm.vue'
import { Mission, MissionApi } from "@/api/uav/mission"
import { Device, DeviceApi } from "@/api/uav/device"

/** 飞行轨迹点 列表 */
defineOptions({ name: 'TrackPoint' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<TrackPoint[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  missionId: undefined,
  deviceId: undefined,
  trackTime: [],
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TrackPointApi.getTrackPointPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await TrackPointApi.deleteTrackPoint(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除飞行轨迹点 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await TrackPointApi.deleteTrackPointList(checkedIds.value);
    checkedIds.value = [];
    message.success(t('common.delSuccess'))
    await getList();
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrackPoint[]) => {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await TrackPointApi.exportTrackPoint(queryParams)
    download.excel(data, '飞行轨迹点.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const missionOptions = ref<Mission[]>([])
const deviceOptions = ref<Device[]>([])

/** 初始化 **/
onMounted(() => {
  getList()

  MissionApi.getMissionSelect().then(res => missionOptions.value = res)
  DeviceApi.getDeviceSelect().then(res => deviceOptions.value = res)
})
</script>

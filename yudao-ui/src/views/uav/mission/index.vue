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
      <el-form-item label="任务编号" prop="missionNo">
        <el-input
          v-model="queryParams.missionNo"
          placeholder="请输入任务编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
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
      <el-form-item label="航线" prop="routeId">
        <el-select
          v-model="queryParams.routeId"
          placeholder="请选择航线"
          clearable
          class="!w-240px"
        >
          <el-option :label="item.routeName" :value="item.id" :key="index" v-for="(item, index) in routeOptions"/>
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="missionType">
        <el-select
          v-model="queryParams.missionType"
          placeholder="请选择任务类型"
          clearable
          class="!w-240px"
        >
          <el-option label="按模板" :value="1" />
          <el-option label="临时规划" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="待执行" :value="0" />
          <el-option label="执行中" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="失败" :value="3" />
          <el-option label="取消" :value="4" />
        </el-select>
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
          v-hasPermi="['uav:mission:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['uav:mission:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['uav:mission:delete']"
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
      <el-table-column label="任务编号" align="center" prop="missionNo" />
      <el-table-column label="设备ID" align="center" prop="deviceId" />
      <el-table-column label="航线ID" align="center" prop="routeId" />
      <el-table-column label="任务类型" align="center" prop="missionType">
        <template #default="scope">
          <el-tag v-if="scope.row.missionType === 1">按模板</el-tag>
          <el-tag v-if="scope.row.missionType === 2">临时规划</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0">待执行</el-tag>
          <el-tag v-if="scope.row.status === 1">执行中</el-tag>
          <el-tag v-if="scope.row.status === 2">已完成</el-tag>
          <el-tag v-if="scope.row.status === 3">失败</el-tag>
          <el-tag v-if="scope.row.status === 4">取消</el-tag>
        </template>
      </el-table-column>
<!--      <el-table-column-->
<!--        label="计划开始时间"-->
<!--        align="center"-->
<!--        prop="planStartTime"-->
<!--        :formatter="dateFormatter"-->
<!--        width="180px"-->
<!--      />-->
<!--      <el-table-column-->
<!--        label="实际开始时间"-->
<!--        align="center"-->
<!--        prop="startTime"-->
<!--        :formatter="dateFormatter"-->
<!--        width="180px"-->
<!--      />-->
<!--      <el-table-column-->
<!--        label="实际结束时间"-->
<!--        align="center"-->
<!--        prop="endTime"-->
<!--        :formatter="dateFormatter"-->
<!--        width="180px"-->
<!--      />-->
      <el-table-column label="实际里程" align="center" prop="actualDistanceM" />
      <el-table-column label="实际时长" align="center" prop="actualDurationS" />
<!--      <el-table-column label="失败原因" align="center" prop="failReason" />-->
<!--      <el-table-column label="备注" align="center" prop="remark" />-->
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
            v-hasPermi="['uav:mission:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['uav:mission:delete']"
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
  <MissionForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { MissionApi, Mission } from '@/api/uav/mission'
import MissionForm from './MissionForm.vue'
import { Device, DeviceApi } from "@/api/uav/device"
import { Route, RouteApi } from "@/api/uav/route"

/** 飞行任务 列表 */
defineOptions({ name: 'Mission' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<Mission[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  missionNo: undefined,
  deviceId: undefined,
  routeId: undefined,
  missionType: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await MissionApi.getMissionPage(queryParams)
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
    await MissionApi.deleteMission(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除飞行任务 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await MissionApi.deleteMissionList(checkedIds.value);
    checkedIds.value = [];
    message.success(t('common.delSuccess'))
    await getList();
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Mission[]) => {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await MissionApi.exportMission(queryParams)
    download.excel(data, '飞行任务.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const deviceOptions = ref<Device[]>([])
const routeOptions = ref<Route[]>([])

/** 初始化 **/
onMounted(() => {
  getList()
  DeviceApi.getDeviceSelect().then(res => deviceOptions.value = res)
  RouteApi.getRouteSelect().then(res => routeOptions.value = res)
})
</script>

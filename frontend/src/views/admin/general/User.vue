<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">User Admin</span>
      </div>

      <el-alert
        title="Admin user APIs are not implemented yet"
        type="warning"
        :closable="false"
        show-icon
      >
        <div slot="description">
          Current SerenOJ backend only supports login, register, current user info,
          profile update and password change. The original HOJ admin user list,
          import, generate and delete APIs are not available, so this page is
          disabled for now to avoid 404 requests.
        </div>
      </el-alert>

      <el-card class="current-user-card" shadow="never">
        <div slot="header">
          <span>Current User</span>
          <el-button
            type="primary"
            size="mini"
            class="refresh-btn"
            :loading="loading"
            @click="refreshCurrentUser"
          >
            Refresh
          </el-button>
        </div>

        <el-table :data="currentUserRows" border size="small">
          <el-table-column prop="label" label="Field" width="160" />
          <el-table-column prop="value" label="Value" />
        </el-table>
      </el-card>

      <el-card class="api-card" shadow="never">
        <div slot="header">
          <span>Backend APIs To Implement Later</span>
        </div>
        <el-table :data="plannedApis" border size="small">
          <el-table-column prop="method" label="Method" width="90" />
          <el-table-column prop="path" label="Path" min-width="260" />
          <el-table-column prop="status" label="Status" min-width="180" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'AdminUserPlaceholder',
  data() {
    return {
      loading: false,
      plannedApis: [
        {
          method: 'GET',
          path: '/api/admin/user/get-user-list',
          status: 'Planned in PRD, not implemented'
        },
        {
          method: 'PUT',
          path: '/api/admin/user/edit-user',
          status: 'Planned in PRD, not implemented'
        }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    currentUserRows() {
      const info = this.userInfo || {}
      return [
        { label: 'UID', value: info.uid || '-' },
        { label: 'Username', value: info.username || '-' },
        { label: 'Nickname', value: info.nickname || '-' },
        { label: 'Email', value: info.email || '-' },
        { label: 'Roles', value: Array.isArray(info.roleList) ? info.roleList.join(', ') : '-' }
      ]
    }
  },
  methods: {
    refreshCurrentUser() {
      this.loading = true
      this.$store.dispatch('refreshUserAuthInfo').finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.current-user-card,
.api-card {
  margin-top: 20px;
}

.refresh-btn {
  float: right;
}
</style>
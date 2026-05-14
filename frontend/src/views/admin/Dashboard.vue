<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card class="admin-info">
          <div class="admin-profile">
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="88"
              color="#FFF"
              :src="userInfo.avatar"
            ></avatar>
            <h2>{{ userInfo.username }}</h2>
            <el-tag effect="dark" size="small" type="warning">
              {{ roleLabel }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="16">
        <el-card>
          <div slot="header">
            <span class="panel-title home-title">SerenOJ Admin</span>
          </div>
          <el-alert
            title="The frontend is trimmed to SerenOJ core admin features."
            type="info"
            :closable="false"
            show-icon
          ></el-alert>
          <el-steps :active="2" finish-status="success" style="margin-top: 24px;">
            <el-step title="Users" description="Connected"></el-step>
            <el-step title="Problems" description="Phase 3"></el-step>
            <el-step title="Judge" description="Phase 4"></el-step>
            <el-step title="Contest/Training" description="Later"></el-step>
          </el-steps>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="quick-card">
      <div slot="header">
        <span class="panel-title home-title">Quick Actions</span>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="$router.push('/admin/problem/create')">
        Create Problem
      </el-button>
      <el-button icon="el-icon-s-grid" @click="$router.push('/admin/problems')">
        Problem List
      </el-button>
      <el-button icon="el-icon-price-tag" @click="$router.push('/admin/problem/tag')">
        Tags
      </el-button>
      <el-button v-if="isSuperAdmin" icon="el-icon-user" @click="$router.push('/admin/user')">
        Users
      </el-button>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import Avatar from 'vue-avatar';

export default {
  name: 'dashboard',
  components: { Avatar },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin', 'isProblemAdmin']),
    roleLabel() {
      if (this.isSuperAdmin) {
        return this.$t('m.Super_Admin');
      }
      if (this.isProblemAdmin) {
        return this.$t('m.All_Problem_Admin');
      }
      return this.$t('m.Admin');
    },
  },
};
</script>

<style scoped>
.admin-info,
.quick-card {
  margin-bottom: 20px;
}
.admin-profile {
  padding: 20px 0;
  text-align: center;
}
.admin-profile h2 {
  margin: 14px 0 8px;
  color: #303133;
}
.quick-card .el-button {
  margin-bottom: 10px;
}
</style>

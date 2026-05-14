<template>
  <el-card class="coming-soon-card">
    <div class="coming-soon-content">
      <i class="el-icon-time coming-soon-icon"></i>
      <h2>{{ pageTitle }}</h2>
      <p>{{ description }}</p>
      <el-button type="primary" @click="$router.push('/problem')">Problem Set</el-button>
      <el-button @click="$router.push('/home')">Home</el-button>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'ComingSoon',
  computed: {
    pageTitle() {
      return this.$route.meta.moduleName || this.$route.meta.title || 'Coming Soon';
    },
    description() {
      return this.$route.meta.description || 'This module will be enabled in a later phase.';
    },
  },
};
</script>

<style scoped>
.coming-soon-card {
  max-width: 860px;
  margin: 40px auto;
}
.coming-soon-content {
  padding: 48px 24px;
  text-align: center;
}
.coming-soon-icon {
  color: #409eff;
  font-size: 54px;
}
.coming-soon-content h2 {
  margin: 18px 0 12px;
  color: #303133;
  font-size: 26px;
}
.coming-soon-content p {
  margin-bottom: 28px;
  color: #606266;
  font-size: 15px;
}
</style>

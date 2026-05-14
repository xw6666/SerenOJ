<template>
  <div class="admin-container">
    <div v-if="!mobileNar">
      <el-menu class="vertical_menu" :router="true" :default-active="currentPath">
        <el-tooltip :content="$t('m.Click_To_Change_Web_Language')" placement="bottom" effect="dark">
          <div class="logo" @click="changeWebLanguage(webLanguage === 'zh-CN' ? 'en-US' : 'zh-CN')">
            <img :src="imgUrl" alt="SerenOJ Admin" />
          </div>
        </el-tooltip>
        <el-menu-item index="/admin/">
          <i class="fa fa-tachometer fa-size" aria-hidden="true"></i>{{ $t('m.Dashboard') }}
        </el-menu-item>
        <el-submenu index="general" v-if="isSuperAdmin">
          <template slot="title"><i class="el-icon-menu"></i>{{ $t('m.General') }}</template>
          <el-menu-item index="/admin/user">{{ $t('m.User_Admin') }}</el-menu-item>
        </el-submenu>
        <el-submenu index="problem">
          <template slot="title"><i class="fa fa-bars fa-size" aria-hidden="true"></i>{{ $t('m.Problem_Admin') }}</template>
          <el-menu-item index="/admin/problems">{{ $t('m.Problem_List') }}</el-menu-item>
          <el-menu-item index="/admin/problem/create">{{ $t('m.Create_Problem') }}</el-menu-item>
          <el-menu-item index="/admin/problem/tag">{{ $t('m.Admin_Tag') }}</el-menu-item>
        </el-submenu>
      </el-menu>
      <div id="header">
        <el-row>
          <el-col :span="18">
            <div class="breadcrumb-container">
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/admin/' }">{{ $t('m.Home_Page') }}</el-breadcrumb-item>
                <el-breadcrumb-item v-for="item in routeList" :key="item.path">
                  {{ $t('m.' + item.meta.title.replaceAll(' ', '_')) }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
          </el-col>
          <el-col :span="6" v-show="isAuthenticated">
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="userInfo.avatar"
              class="drop-avatar"
            ></avatar>
            <el-dropdown @command="handleCommand" style="vertical-align: middle;">
              <span>{{ userInfo.username }}<i class="el-icon-caret-bottom el-icon--right"></i></span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="logout">{{ $t('m.Logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </el-col>
        </el-row>
      </div>
    </div>

    <div v-else>
      <mu-appbar class="mobile-nav" color="primary">
        <mu-button icon slot="left" @click="opendrawer = !opendrawer">
          <i class="el-icon-s-unfold"></i>
        </mu-button>
        {{ websiteConfig.shortName ? websiteConfig.shortName + ' ADMIN' : 'ADMIN' }}
        <mu-menu slot="right" v-show="isAuthenticated" :open.sync="openusermenu">
          <mu-button flat>{{ userInfo.username }}<i class="el-icon-caret-bottom"></i></mu-button>
          <mu-list slot="content" @change="handleCommand">
            <mu-list-item button value="logout">
              <mu-list-item-content><mu-list-item-title>{{ $t('m.Logout') }}</mu-list-item-title></mu-list-item-content>
            </mu-list-item>
          </mu-list>
        </mu-menu>
      </mu-appbar>
      <mu-drawer :open.sync="opendrawer" :docked="false" :right="false">
        <mu-list>
          <mu-list-item button to="/admin/dashboard" @click="opendrawer = false" active-class="mobile-menu-active">
            <mu-list-item-action><mu-icon value=":fa fa-tachometer" size="24"></mu-icon></mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Dashboard') }}</mu-list-item-title>
          </mu-list-item>
          <mu-list-item v-if="isSuperAdmin" button to="/admin/user" @click="opendrawer = false" active-class="mobile-menu-active">
            <mu-list-item-action><mu-icon value=":el-icon-menu" size="24"></mu-icon></mu-list-item-action>
            <mu-list-item-title>{{ $t('m.User_Admin') }}</mu-list-item-title>
          </mu-list-item>
          <mu-list-item button to="/admin/problems" @click="opendrawer = false" active-class="mobile-menu-active">
            <mu-list-item-action><mu-icon value=":fa fa-bars" size="24"></mu-icon></mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Problem_List') }}</mu-list-item-title>
          </mu-list-item>
          <mu-list-item button to="/admin/problem/create" @click="opendrawer = false" active-class="mobile-menu-active">
            <mu-list-item-action><mu-icon value=":el-icon-plus" size="24"></mu-icon></mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Create_Problem') }}</mu-list-item-title>
          </mu-list-item>
          <mu-list-item button to="/admin/problem/tag" @click="opendrawer = false" active-class="mobile-menu-active">
            <mu-list-item-action><mu-icon value=":el-icon-price-tag" size="24"></mu-icon></mu-list-item-action>
            <mu-list-item-title>{{ $t('m.Admin_Tag') }}</mu-list-item-title>
          </mu-list-item>
        </mu-list>
      </mu-drawer>
    </div>

    <div class="content-app">
      <transition name="fadeInUp" mode="out-in">
        <router-view></router-view>
      </transition>
      <div class="footer">
        Powered by <a :href="websiteConfig.projectUrl" style="color:#1E9FFF" target="_blank">{{ websiteConfig.projectName }}</a>
        <span style="margin-left:10px">
          <el-dropdown @command="changeWebLanguage" placement="top">
            <span class="el-dropdown-link" style="font-size:14px">
              <i class="fa fa-globe" aria-hidden="true"> {{ getLanguageLabelByValue(this.webLanguage) }}</i><i class="el-icon-arrow-up el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item v-for="(lang, index) in languages" :key="index" :command="lang.value">{{ lang.label }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import api from '@/common/api';
import mMessage from '@/common/message';
import Avatar from 'vue-avatar';
import { languages, getLangLabelByValue } from '@/i18n';

export default {
  name: 'app',
  components: { Avatar },
  data() {
    return {
      openusermenu: false,
      opendrawer: false,
      mobileNar: false,
      currentPath: '',
      routeList: [],
      imgUrl: require('@/assets/backstage.png'),
      languages: [],
    };
  },
  mounted() {
    this.languages = languages;
    this.currentPath = this.$route.path;
    this.getBreadcrumb();
    this.page_width();
    window.addEventListener('resize', this.page_width);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.page_width);
  },
  methods: {
    handleCommand(command) {
      if (command === 'logout') {
        api.admin_logout().then(() => {
          this.$router.push({ path: '/admin/login' });
          mMessage.success(this.$i18n.t('m.Log_Out_Successfully'));
          this.$store.commit('clearUserInfoAndToken');
        });
      }
    },
    page_width() {
      this.mobileNar = window.screen.width < 992;
    },
    getBreadcrumb() {
      this.routeList = this.$route.matched.filter((item) => item.meta.title);
    },
    changeWebLanguage(language) {
      this.$store.commit('changeWebLanguage', { language });
    },
    getLanguageLabelByValue(value) {
      return getLangLabelByValue(value);
    },
  },
  computed: {
    ...mapGetters(['userInfo', 'isSuperAdmin', 'isProblemAdmin', 'isAuthenticated', 'websiteConfig', 'webLanguage']),
  },
  watch: {
    $route() {
      this.currentPath = this.$route.path;
      this.getBreadcrumb();
    },
  },
};
</script>

<style scoped>
.admin-container {
  min-width: 300px;
  background-color: #eff3f5;
}
.vertical_menu {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  width: 210px;
  overflow-y: auto;
  z-index: 1000;
}
.logo {
  height: 60px;
  padding: 10px;
  cursor: pointer;
  text-align: center;
}
.logo img {
  max-height: 40px;
  max-width: 160px;
}
#header {
  position: fixed;
  left: 210px;
  right: 0;
  top: 0;
  z-index: 999;
  height: 60px;
  padding: 15px 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.content-app {
  margin-left: 210px;
  padding: 80px 20px 60px;
  min-height: 100vh;
}
.drop-avatar {
  margin-right: 8px;
  vertical-align: middle;
}
.footer {
  margin-top: 20px;
  text-align: center;
  color: #909399;
}
.mobile-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2500;
}
@media screen and (max-width: 991px) {
  .content-app {
    margin-left: 0;
    padding-top: 76px;
  }
}
</style>

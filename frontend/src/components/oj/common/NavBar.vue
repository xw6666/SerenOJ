<template>
  <div>
    <template v-if="!mobileNar">
      <div id="header">
        <el-menu
          :default-active="activeMenuName"
          mode="horizontal"
          router
          active-text-color="#2196f3"
          text-color="#495060"
        >
          <div class="logo">
            <el-tooltip
              :content="$t('m.Click_To_Change_Web_Language')"
              placement="bottom"
              effect="dark"
            >
              <el-image
                style="width: 139px; height: 50px"
                :src="imgUrl"
                fit="scale-down"
                @click="changeWebLanguage"
              ></el-image>
            </el-tooltip>
          </div>

          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>{{ $t('m.NavBar_Home') }}
          </el-menu-item>
          <el-menu-item index="/problem">
            <i class="el-icon-s-grid"></i>{{ $t('m.NavBar_Problem') }}
          </el-menu-item>
          <el-menu-item index="/training">
            <i class="el-icon-s-claim"></i>{{ $t('m.NavBar_Training') }}
          </el-menu-item>
          <el-menu-item index="/contest">
            <i class="el-icon-trophy"></i>{{ $t('m.NavBar_Contest') }}
          </el-menu-item>
          <el-menu-item index="/status">
            <i class="el-icon-s-marketing"></i>{{ $t('m.NavBar_Status') }}
          </el-menu-item>

          <template v-if="!isAuthenticated">
            <div class="btn-menu">
              <el-button type="primary" size="medium" round @click="handleBtnClick('Login')">
                {{ $t('m.NavBar_Login') }}
              </el-button>
              <el-button
                v-if="websiteConfig.register"
                size="medium"
                round
                @click="handleBtnClick('Register')"
                style="margin-left: 5px"
              >
                {{ $t('m.NavBar_Register') }}
              </el-button>
            </div>
          </template>
          <template v-else>
            <el-dropdown class="drop-menu" @command="handleRoute" placement="bottom" trigger="hover">
              <span class="el-dropdown-link">
                {{ userInfo.username }}<i class="el-icon-caret-bottom"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="/setting">{{ $t('m.NavBar_Setting') }}</el-dropdown-item>
                <el-dropdown-item command="/status">{{ $t('m.NavBar_Submissions') }}</el-dropdown-item>
                <el-dropdown-item v-if="isAdminRole" command="/admin/">{{ $t('m.NavBar_Management') }}</el-dropdown-item>
                <el-dropdown-item divided command="/logout">{{ $t('m.NavBar_Logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <avatar
              :username="userInfo.username"
              :inline="true"
              :size="30"
              color="#FFF"
              :src="avatar"
              class="drop-avatar"
            ></avatar>
          </template>
        </el-menu>
      </div>
      <div id="header-hidden"></div>
    </template>

    <template v-else>
      <div>
        <mu-appbar class="mobile-nav" color="primary">
          <mu-button icon slot="left" @click="opendrawer = !opendrawer">
            <i class="el-icon-s-unfold"></i>
          </mu-button>
          <span @click="changeWebLanguage">
            {{ websiteConfig.shortName ? websiteConfig.shortName : 'OJ' }}
          </span>
          <mu-button flat slot="right" @click="handleBtnClick('Login')" v-show="!isAuthenticated">
            {{ $t('m.NavBar_Login') }}
          </mu-button>
          <mu-button
            flat
            slot="right"
            @click="handleBtnClick('Register')"
            v-show="!isAuthenticated && websiteConfig.register"
          >
            {{ $t('m.NavBar_Register') }}
          </mu-button>
          <mu-menu slot="right" v-if="isAuthenticated" :open.sync="openusermenu">
            <mu-button flat>
              <avatar
                :username="userInfo.username"
                :inline="true"
                :size="30"
                color="#FFF"
                :src="avatar"
                :title="userInfo.username"
              ></avatar>
              <i class="el-icon-caret-bottom"></i>
            </mu-button>
            <mu-list slot="content" @change="handleCommand">
              <mu-list-item button value="/setting">
                <mu-list-item-content><mu-list-item-title>{{ $t('m.NavBar_Setting') }}</mu-list-item-title></mu-list-item-content>
              </mu-list-item>
              <mu-divider></mu-divider>
              <mu-list-item button value="/status">
                <mu-list-item-content><mu-list-item-title>{{ $t('m.NavBar_Submissions') }}</mu-list-item-title></mu-list-item-content>
              </mu-list-item>
              <mu-divider></mu-divider>
              <mu-list-item button value="/admin/" v-show="isAdminRole">
                <mu-list-item-content><mu-list-item-title>{{ $t('m.NavBar_Management') }}</mu-list-item-title></mu-list-item-content>
              </mu-list-item>
              <mu-divider></mu-divider>
              <mu-list-item button value="/logout">
                <mu-list-item-content><mu-list-item-title>{{ $t('m.NavBar_Logout') }}</mu-list-item-title></mu-list-item-content>
              </mu-list-item>
            </mu-list>
          </mu-menu>
        </mu-appbar>
        <mu-appbar style="width: 100%;"><!-- spacer --></mu-appbar>

        <mu-drawer :open.sync="opendrawer" :docked="false" :right="false">
          <mu-list>
            <mu-list-item button to="/home" @click="opendrawer = false" active-class="mobile-menu-active">
              <mu-list-item-action><mu-icon value=":el-icon-s-home" size="24"></mu-icon></mu-list-item-action>
              <mu-list-item-title>{{ $t('m.NavBar_Home') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item button to="/problem" @click="opendrawer = false" active-class="mobile-menu-active">
              <mu-list-item-action><mu-icon value=":el-icon-s-grid" size="24"></mu-icon></mu-list-item-action>
              <mu-list-item-title>{{ $t('m.NavBar_Problem') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item button to="/training" @click="opendrawer = false" active-class="mobile-menu-active">
              <mu-list-item-action><mu-icon value=":el-icon-s-claim" size="24"></mu-icon></mu-list-item-action>
              <mu-list-item-title>{{ $t('m.NavBar_Training') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item button to="/contest" @click="opendrawer = false" active-class="mobile-menu-active">
              <mu-list-item-action><mu-icon value=":el-icon-trophy" size="24"></mu-icon></mu-list-item-action>
              <mu-list-item-title>{{ $t('m.NavBar_Contest') }}</mu-list-item-title>
            </mu-list-item>
            <mu-list-item button to="/status" @click="opendrawer = false" active-class="mobile-menu-active">
              <mu-list-item-action><mu-icon value=":el-icon-s-marketing" size="24"></mu-icon></mu-list-item-action>
              <mu-list-item-title>{{ $t('m.NavBar_Status') }}</mu-list-item-title>
            </mu-list-item>
          </mu-list>
        </mu-drawer>
      </div>
    </template>

    <el-dialog
      :visible.sync="modalVisible"
      width="370px"
      class="dialog"
      :title="title"
      :close-on-click-modal="false"
    >
      <component :is="modalStatus.mode" v-if="modalVisible"></component>
      <div slot="footer" style="display: none"></div>
    </el-dialog>
  </div>
</template>

<script>
import Login from '@/components/oj/common/Login';
import Register from '@/components/oj/common/Register';
import ResetPwd from '@/components/oj/common/ResetPassword';
import { mapGetters, mapActions } from 'vuex';
import Avatar from 'vue-avatar';

export default {
  components: {
    Login,
    Register,
    ResetPwd,
    Avatar,
  },
  data() {
    return {
      mobileNar: false,
      opendrawer: false,
      openusermenu: false,
      imgUrl: require('@/assets/logo.png'),
    };
  },
  created() {
    this.page_width();
    window.addEventListener('resize', this.onResize);
  },
  mounted() {
    this.setHiddenHeaderHeight();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize);
  },
  methods: {
    ...mapActions(['changeModalStatus']),
    onResize() {
      this.page_width();
      this.setHiddenHeaderHeight();
    },
    page_width() {
      this.mobileNar = window.screen.width < 992;
    },
    handleBtnClick(mode) {
      this.changeModalStatus({ mode, visible: true });
    },
    handleRoute(route) {
      if (route) {
        this.$router.push(route);
      }
    },
    handleCommand(route) {
      this.openusermenu = false;
      if (route) {
        this.$router.push(route);
      }
    },
    changeWebLanguage() {
      this.$store.commit('changeWebLanguage', {
        language: this.webLanguage === 'zh-CN' ? 'en-US' : 'zh-CN',
      });
    },
    setHiddenHeaderHeight() {
      if (!this.mobileNar) {
        this.$nextTick(() => {
          const header = document.getElementById('header');
          const spacer = document.getElementById('header-hidden');
          if (header && spacer) {
            spacer.style.height = header.offsetHeight + 'px';
          }
        });
      }
    },
  },
  computed: {
    ...mapGetters([
      'modalStatus',
      'userInfo',
      'isAuthenticated',
      'isAdminRole',
      'websiteConfig',
      'webLanguage',
    ]),
    avatar() {
      return this.userInfo.avatar;
    },
    activeMenuName() {
      const top = this.$route.path.split('/')[1] || 'home';
      return '/' + top;
    },
    modalVisible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
    title() {
      const ojName = this.websiteConfig.shortName || 'OJ';
      if (this.modalStatus.mode === 'ResetPwd') {
        return this.$i18n.t('m.Dialog_Reset_Password') + ' - ' + ojName;
      }
      return this.$i18n.t('m.Dialog_' + this.modalStatus.mode) + ' - ' + ojName;
    },
  },
};
</script>

<style scoped>
#header {
  min-width: 300px;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 2000;
  background-color: #fff;
  box-shadow: 0 1px 5px 0 rgba(0, 0, 0, 0.1);
}
.mobile-nav {
  position: fixed;
  left: 0;
  top: 0;
  z-index: 2500;
  width: 100%;
}
.logo {
  cursor: pointer;
  margin-left: 2%;
  margin-right: 2%;
  float: left;
  width: 139px;
  height: 42px;
  margin-top: 5px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409eff !important;
}
.drop-menu {
  float: right;
  margin-right: 30px;
  position: relative;
  font-weight: 500;
  right: 10px;
  margin-top: 18px;
  font-size: 18px;
}
.drop-avatar {
  float: right;
  margin-right: 15px;
  position: relative;
  margin-top: 16px;
}
.btn-menu {
  font-size: 16px;
  float: right;
  margin-right: 10px;
  margin-top: 10px;
}
.dialog /deep/ .el-dialog__body {
  padding: 20px;
}
</style>

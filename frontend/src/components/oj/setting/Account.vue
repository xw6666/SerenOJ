<template>
  <div class="setting-main">
    <el-card>
      <p class="section-title">{{ $t('m.Change_Password') }}</p>
      <el-form
        class="setting-content"
        ref="formPassword"
        :model="formPassword"
        :rules="rulePassword"
      >
        <el-form-item :label="$t('m.Old_Password')" prop="oldPassword">
          <el-input v-model="formPassword.oldPassword" type="password" />
        </el-form-item>
        <el-form-item :label="$t('m.New_Password')" prop="newPassword">
          <el-input v-model="formPassword.newPassword" type="password" />
        </el-form-item>
        <el-form-item :label="$t('m.Confirm_New_Password')" prop="againPassword">
          <el-input v-model="formPassword.againPassword" type="password" />
        </el-form-item>
      </el-form>
      <el-button type="primary" :loading="loading" @click="changePassword">
        {{ $t('m.Update_Password') }}
      </el-button>
      <el-alert
        v-if="alert.show"
        :title="alert.title"
        :type="alert.type"
        :description="alert.description"
        :closable="false"
        style="margin-top:15px"
        show-icon
      ></el-alert>
    </el-card>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';

export default {
  data() {
    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.formPassword.newPassword) {
        callback(new Error(this.$i18n.t('m.Password_does_not_match')));
      } else {
        callback();
      }
    };
    const CheckNewPassword = (rule, value, callback) => {
      if (this.formPassword.oldPassword && this.formPassword.oldPassword === value) {
        callback(new Error(this.$i18n.t('m.The_new_password_does_not_change')));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      alert: {
        show: false,
        title: '',
        type: 'success',
        description: '',
      },
      formPassword: {
        oldPassword: '',
        newPassword: '',
        againPassword: '',
      },
      rulePassword: {
        oldPassword: [
          { required: true, trigger: 'blur', message: this.$i18n.t('m.The_current_password_cannot_be_empty') },
          { trigger: 'blur', min: 6, max: 20, message: this.$i18n.t('m.Password_Check_Between') },
        ],
        newPassword: [
          { required: true, trigger: 'blur', message: this.$i18n.t('m.The_new_password_cannot_be_empty') },
          { trigger: 'blur', min: 6, max: 20, message: this.$i18n.t('m.Password_Check_Between') },
          { validator: CheckNewPassword, trigger: 'blur' },
        ],
        againPassword: [
          { required: true, trigger: 'blur', message: this.$i18n.t('m.Password_Again_Check_Required') },
          { validator: CheckAgainPassword, trigger: 'blur' },
        ],
      },
    };
  },
  methods: {
    changePassword() {
      this.$refs.formPassword.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        const data = {
          oldPassword: this.formPassword.oldPassword,
          newPassword: this.formPassword.newPassword,
        };
        api.changePassword(data).then(
          (res) => {
            this.loading = false;
            const msg = res.data.msg || this.$i18n.t('m.Update_Successfully');
            myMessage.success(msg);
            this.alert = {
              show: true,
              title: this.$i18n.t('m.Update_Successfully'),
              type: 'success',
              description: msg,
            };
            setTimeout(() => {
              this.$store.dispatch('clearUserInfoAndToken');
              this.$router.push('/home');
            }, 1000);
          },
          () => {
            this.loading = false;
          }
        );
      });
    },
  },
};
</script>

<style scoped>
.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
  text-align: center;
}
.setting-content {
  max-width: 520px;
  margin: 0 auto;
}
.el-button {
  display: block;
  margin: 10px auto 0;
}
</style>

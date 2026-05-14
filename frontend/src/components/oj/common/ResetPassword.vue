<template>
  <div>
    <el-form :model="formResetPassword" :rules="rules" ref="formResetPassword">
      <el-form-item prop="email">
        <el-input
          v-model="formResetPassword.email"
          prefix-icon="el-icon-message"
          :placeholder="$t('m.Reset_Password_Email')"
          @keyup.enter.native="handleResetPwd"
        ></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button
        type="primary"
        @click="handleResetPwd"
        :loading="btnResetPwdLoading"
        :disabled="btnResetPwdDisabled"
      >
        {{ resetText }}
      </el-button>
      <el-link type="primary" @click="switchMode('Login')">{{ $t('m.Remember_Passowrd_To_Login') }}</el-link>
    </div>
  </div>
</template>
<script>
import { mapGetters, mapActions } from 'vuex';
import api from '@/common/api';
import mMessage from '@/common/message';
export default {
  data() {
    const CheckEmailNotExist = (rule, value, callback) => {
      api.checkUsernameOrEmail(undefined, value).then(
        (res) => {
          if (res.data.data.emailExists === false) {
            callback(new Error(this.$i18n.t('m.The_email_does_not_exists')));
          } else {
            callback();
          }
        },
        () => callback()
      );
    };
    return {
      resetText: this.$i18n.t('m.Send_Password_Reset_Email'),
      btnResetPwdLoading: false,
      btnResetPwdDisabled: false,
      formResetPassword: {
        email: '',
      },
      rules: {
        email: [
          {
            required: true,
            message: this.$i18n.t('m.Email_Check_Required'),
            type: 'email',
            trigger: 'blur',
          },
          { validator: CheckEmailNotExist, trigger: 'blur' },
        ],
      },
    };
  },
  methods: {
    ...mapActions(['changeModalStatus', 'changeResetTimeOut', 'startTimeOut']),
    switchMode(mode) {
      this.changeModalStatus({ mode, visible: true });
    },
    countDown() {
      const i = this.time;
      this.resetText = i + 's, ' + this.$i18n.t('m.Waiting_Can_Resend_Email');
      if (i === 0) {
        this.btnResetPwdDisabled = false;
        this.resetText = this.$i18n.t('m.Send_Password_Reset_Email');
        return;
      }
      setTimeout(() => this.countDown(), 1000);
    },
    handleResetPwd() {
      this.$refs.formResetPassword.validate((valid) => {
        if (!valid) return;
        this.resetText = 'Waiting...';
        mMessage.info(this.$i18n.t('m.The_system_is_processing'));
        this.btnResetPwdLoading = true;
        this.btnResetPwdDisabled = true;
        api.applyResetPassword(this.formResetPassword).then(
          () => {
            mMessage.message('success', this.$i18n.t('m.ResetPwd_Send_Email_Msg'), 10000);
            this.countDown();
            this.startTimeOut({ name: 'resetTimeOut' });
            this.btnResetPwdLoading = false;
          },
          () => {
            this.btnResetPwdLoading = false;
            this.btnResetPwdDisabled = false;
            this.resetText = this.$i18n.t('m.Send_Password_Reset_Email');
          }
        );
      });
    },
  },
  computed: {
    ...mapGetters(['resetTimeOut', 'modalStatus']),
    time: {
      get() {
        return this.resetTimeOut;
      },
      set(value) {
        this.changeResetTimeOut({ time: value });
      },
    },
  },
  created() {
    if (this.time !== 90 && this.time !== 0) {
      this.btnResetPwdDisabled = true;
      this.countDown();
    }
  },
};
</script>
<style scoped>
.footer {
  overflow: auto;
  margin-top: 20px;
  margin-bottom: -15px;
  text-align: center;
}
/deep/.el-button--primary {
  margin: 0 0 15px 0;
  width: 100%;
}
/deep/ .el-form-item__content {
  margin-left: 0 !important;
}
</style>

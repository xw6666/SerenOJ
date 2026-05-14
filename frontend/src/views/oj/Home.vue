<template>
  <div>
    <el-row :gutter="20">
      <el-col
        :md="15"
        :sm="24"
      >
        <el-card>
          <div
            slot="header"
            class="content-center"
          >
            <span class="panel-title home-title welcome-title">{{ $t('m.Welcome_to')
              }}{{ websiteConfig.shortName }}</span>
          </div>
          <el-carousel
            :interval="interval"
            :height="srcHight"
            class="img-carousel"
            arrow="always"
            indicator-position="outside"
          >
            <el-carousel-item
              v-for="(item, index) in carouselImgList"
              :key="index"
            >
              <el-image
                :src="item.url"
                fit="fill"
              >
                <div
                  slot="error"
                  class="image-slot"
                >
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </el-carousel-item>
          </el-carousel>
        </el-card>
        <!-- SerenOJ: Announcements and statistics removed, backend not ready -->
        <el-card class="card-top">
          <div
            slot="header"
            class="clearfix"
          >
            <span class="panel-title home-title">
              <i class="el-icon-s-platform"></i> {{
              'Getting Started'
            }}</span>
          </div>
          <div style="padding: 20px; text-align: center;">
            <p style="font-size: 16px; margin-bottom: 20px;">Welcome to SerenOJ! A lightweight online judge system.</p>
            <el-button type="primary" size="large" @click="$router.push('/problem')">进入题库</el-button>
            <el-button size="large" @click="$router.push('/admin')" style="margin-left: 10px;">管理后台</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :md="9" :sm="24" class="phone-margin">
        <el-card>
          <div slot="header" class="clearfix">
            <span class="panel-title home-title"><i class="el-icon-info"></i> 开发进度</span>
          </div>
          <el-steps direction="vertical" :active="2" finish-status="success">
            <el-step title="Phase 2.5" description="用户认证、注册、资料编辑" status="success"></el-step>
            <el-step title="Phase 3" description="题目模块开发中" status="process"></el-step>
            <el-step title="Phase 4" description="判题模块待开发" status="wait"></el-step>
            <el-step title="Phase 5" description="比赛模块待开发" status="wait"></el-step>
          </el-steps>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState, mapGetters } from "vuex";
export default {
  name: "home",
  components: {},
  data() {
    return {
      interval: 5000,
      srcHight: "440px",
      carouselImgList: [
        { url: require('@/assets/home1.jpeg') },
      ],
    };
  },
  mounted() {
    let screenWidth = window.screen.width;
    if (screenWidth < 768) {
      this.srcHight = "200px";
    } else {
      this.srcHight = "440px";
    }
  },
  computed: {
    ...mapState(["websiteConfig"]),
    ...mapGetters(["isAuthenticated"]),
  },
};
</script>
<style>
.contest-card-running {
  border-color: rgb(25, 190, 107);
}
.contest-card-schedule {
  border-color: #f90;
}
</style>
<style scoped>
/deep/.el-card__header {
  padding: 0.6rem 1.25rem !important;
}
.card-top {
  margin-top: 20px;
}
.home-contest {
  text-align: left;
  font-size: 21px;
  font-weight: 500;
  line-height: 30px;
}
.oj-logo {
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  margin-bottom: 1rem;
  padding: 0.5rem 1rem;
  background: rgb(255, 255, 255);
  min-height: 47px;
}
.oj-normal {
  border-color: #409eff;
}
.oj-error {
  border-color: #e65c47;
}

.el-carousel__item h3 {
  color: #475669;
  font-size: 14px;
  opacity: 0.75;
  line-height: 200px;
  margin: 0;
}

.contest-card {
  margin-bottom: 20px;
}
.contest-title {
  font-size: 1.15rem;
  font-weight: 600;
}
.contest-type-auth {
  text-align: center;
  margin-top: -10px;
  margin-bottom: 5px;
}
ul,
li {
  padding: 0;
  margin: 0;
  list-style: none;
}
.contest-info {
  text-align: center;
}
.contest-info li {
  display: inline-block;
  padding-right: 10px;
}

/deep/.contest-card-running .el-card__header {
  border-color: rgb(25, 190, 107);
  background-color: rgba(94, 185, 94, 0.15);
}
.contest-card-running .contest-title {
  color: #5eb95e;
}

/deep/.contest-card-schedule .el-card__header {
  border-color: #f90;
  background-color: rgba(243, 123, 29, 0.15);
}

.contest-card-schedule .contest-title {
  color: #f37b1d;
}

.content-center {
  text-align: center;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
.welcome-title {
  font-weight: 600;
  font-size: 25px;
  font-family: "Raleway";
}
.contest-status {
  float: right;
}
.img-carousel {
  height: 490px;
}

@media screen and (max-width: 768px) {
  .contest-status {
    text-align: center;
    float: none;
    margin-top: 5px;
  }
  .contest-header {
    text-align: center;
  }
  .img-carousel {
    height: 220px;
    overflow: hidden;
  }
  .phone-margin {
    margin-top: 20px;
  }
}
.title .el-link {
  font-size: 21px;
  font-weight: 500;
  color: #444;
}
.clearfix h2 {
  color: #409eff;
}
.el-link.el-link--default:hover {
  color: #409eff;
  transition: all 0.28s ease;
}
.contest .content-info {
  padding: 0 70px 40px 70px;
}
.contest .contest-description {
  margin-top: 25px;
}
span.rank-tag.no1 {
  line-height: 24px;
  background: #bf2c24;
}

span.rank-tag.no2 {
  line-height: 24px;
  background: #e67225;
}

span.rank-tag.no3 {
  line-height: 24px;
  background: #e6bf25;
}

span.rank-tag {
  font: 16px/22px FZZCYSK;
  min-width: 14px;
  height: 22px;
  padding: 0 4px;
  text-align: center;
  color: #fff;
  background: #000;
  background: rgba(0, 0, 0, 0.6);
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.cite {
  display: block;
  width: 14px;
  height: 0;
  margin: 0 auto;
  margin-top: -3px;
  border-right: 11px solid transparent;
  border-bottom: 0 none;
  border-left: 11px solid transparent;
}
.cite.no0 {
  border-top: 5px solid #bf2c24;
}
.cite.no1 {
  border-top: 5px solid #e67225;
}
.cite.no2 {
  border-top: 5px solid #e6bf25;
}

@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
/deep/.el-image {
  height: 100%;
  width: 100%;
}
</style>

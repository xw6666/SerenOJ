import axios from 'axios'
import Vue from 'vue'
import mMessage from '@/common/message'
import router from '@/router'
import store from "@/store"
import utils from '@/common/utils'
import i18n from '@/i18n'
// import NProgress from 'nprogress' // nprogress鎻掍欢
// import 'nprogress/nprogress.css' // nprogress鏍峰紡

// // 閰嶇疆NProgress杩涘害鏉￠€夐」  鈥斺€?鍔ㄧ敾鏁堟灉
// NProgress.configure({ ease: 'ease', speed: 1000,showSpinner: false})
Vue.prototype.$http = axios

const isMobile = /ipad|iphone|midp|rv:1.2.3.4|ucweb|android|windows ce|windows mobile/.test(navigator.userAgent.toLowerCase());


// 璇锋眰瓒呮椂鏃堕棿
axios.defaults.timeout = 90000;

axios.interceptors.request.use(

  config => {

    // NProgress.start();
    // 姣忔鍙戦€佽姹備箣鍓嶅垽鏂璿uex涓槸鍚﹀瓨鍦╰oken
    // 濡傛灉瀛樺湪锛屽垯缁熶竴鍦╤ttp璇锋眰鐨刪eader閮藉姞涓妕oken锛岃繖鏍峰悗鍙版牴鎹畉oken鍒ゆ柇浣犵殑鐧诲綍鎯呭喌
    // 鍗充娇鏈湴瀛樺湪token锛屼篃鏈夊彲鑳絫oken鏄繃鏈熺殑锛屾墍浠ュ湪鍝嶅簲鎷︽埅鍣ㄤ腑瑕佸杩斿洖鐘舵€佽繘琛屽垽鏂?
    const token = localStorage.getItem('token')
    if(config.url != '/api/login'){
      token && (config.headers.Authorization = token);
    }
    let type = config.url.split("/")[2];
    if (type === 'admin') { // 鎼哄甫璇锋眰鍖哄埆鏄惁涓篴dmin
      config.headers['Url-Type'] = type
    } else {
      config.headers['Url-Type'] = 'general'
    }

    return config;
  },
  error => {
    // NProgress.done();
    mMessage.error(error.response.data.msg);
    if (!isMobile) {
      Vue.prototype.$notify.error({
        title: i18n.t('m.Error'),
        message: error.response.data.msg,
        duration: 5000,
        offset: 50
      });
    }
    return Promise.error(error);
  })

// 鍝嶅簲鎷︽埅鍣?
axios.interceptors.response.use(
  response => {
    // NProgress.done();
    if (response.headers['refresh-token']) { // token缁害锛?
      store.commit('changeUserToken', response.headers['authorization'])
    }
    if (response.data.status === 200 || response.data.status == undefined) {
      return Promise.resolve(response);
    } else {
      mMessage.error(response.data.msg);
      if (!isMobile) {
        Vue.prototype.$notify.error({
          title: i18n.t('m.Error'),
          message: response.data.msg,
          duration: 5000,
          offset: 50
        });
      }
      return Promise.reject(response);
    }

  },
  // 鏈嶅姟鍣ㄧ姸鎬佺爜涓嶆槸200鐨勬儏鍐?
  error => {
    // NProgress.done();
    if (error.response) {
      if (error.response.headers['refresh-token']) { // token缁害锛侊紒
        store.commit('changeUserToken', error.response.headers['authorization'])
      }
      if (error.response.data instanceof Blob) { // 濡傛灉鏄枃浠舵搷浣滅殑杩斿洖锛岀敱鍚庣画杩涜澶勭悊
        return Promise.resolve(error.response);
      }
      switch (error.response.status) {
        // 401: 鏈櫥褰?token杩囨湡
        // 鏈櫥褰曞垯璺宠浆鐧诲綍椤甸潰锛屽苟鎼哄甫褰撳墠椤甸潰鐨勮矾寰?
        // 鍦ㄧ櫥褰曟垚鍔熷悗杩斿洖褰撳墠椤甸潰锛岃繖涓€姝ラ渶瑕佸湪鐧诲綍椤垫搷浣溿€?
        case 401:
          if (error.response.data.msg) {
            mMessage.error(error.response.data.msg);
            if (!isMobile) {
              Vue.prototype.$notify.error({
                title: i18n.t('m.Error'),
                message: error.response.data.msg,
                duration: 5000,
                offset: 50
              });
            }
          }
          if (error.response.config.headers['Url-Type'] === 'admin') {
            router.push("/admin/login")
          } else {
            store.commit('changeModalStatus', { mode: 'Login', visible: true });
          }
          store.commit('clearUserInfoAndToken');
          break;
        // 403
        // 鏃犳潈闄愯闂垨鎿嶄綔鐨勮姹?
        case 403:
          if (error.response.data.msg) {
            mMessage.error(error.response.data.msg);
            if (!isMobile) {
              Vue.prototype.$notify.error({
                title: i18n.t('m.Error'),
                message: error.response.data.msg,
                duration: 5000,
                offset: 50
              });
            }
          }
          let isAdminApi = error.response.config.url.startsWith('/api/admin');
          store.dispatch('refreshUserAuthInfo').then((res)=>{
            if(isAdminApi){
              router.push("/admin")
            }
          })
          break;
        // 404璇锋眰涓嶅瓨鍦?
        case 404:
          mMessage.error(i18n.t('m.Query_error_unable_to_find_the_resource_to_request'));
          break;
        // 鍏朵粬閿欒锛岀洿鎺ユ姏鍑洪敊璇彁绀?
        default:
          if (error.response.data) {
            if (error.response.data.msg) {
              mMessage.error(error.response.data.msg);
              if (!isMobile) {
                Vue.prototype.$notify.error({
                  title: i18n.t('m.Error'),
                  message: error.response.data.msg,
                  duration: 5000,
                  offset: 50
                });
              }
            } else {
              mMessage.error(i18n.t('m.Server_error_please_refresh_again'));
            }
          }
          break;
      }
      return Promise.reject(error);
    } else { //澶勭悊鏂綉鎴栬姹傝秴鏃讹紝璇锋眰娌″搷搴?
      if (error.code == 'ECONNABORTED' || error.message.includes('timeout')) {
        mMessage.error(i18n.t('m.Request_timed_out_please_try_again_later'));
      } else {
        mMessage.error(i18n.t('m.Network_error_abnormal_link_with_server_please_try_again_later'));
      }
      return Promise.reject(error);
    }
  }
);


// 澶勭悊oj鍓嶅彴鐨勮姹?
const ojApi = {
  // 鍒犻櫎 /api/get-website-config, /api/home-carousel 绛夐椤电粺璁℃帴鍙?
  // 鍒犻櫎 /api/captcha, discussion, group, msg 鎵€鏈夋帴鍙?

  // 鐢ㄦ埛璐︽埛鐨勭浉鍏宠姹?
  getRegisterEmail(email) {
    let params = {
      email: email
    }
    return ajax('/api/get-register-code', 'get', {
      params
    })
  },

  login(data) {
    return ajax('/api/login', 'post', {
      data
    })
  },
  checkUsernameOrEmail(username, email) {
    return ajax('/api/check-username-or-email', 'post', {
      data: {
        username,
        email
      }
    })
  },
  // 鑾峰彇楠岃瘉鐮佸凡鍒犻櫎锛孲erenOJ鍚庣閫氳繃鏃ュ織鎵撳嵃
  // 娉ㄥ唽
  register(data) {
    return ajax('/api/register', 'post', {
      data
    })
  },
  logout() {
    return ajax('/api/logout', 'get')
  },

  // 璐︽埛鐨勭浉鍏虫搷浣?
  getUserInfo() {
    return ajax('/api/get-user-info', 'get')
  },
  getUserAuthInfo() {
    return ajax('/api/get-user-info', 'get')
  },

  // 璐︽埛鐨勭浉鍏虫搷浣?
  applyResetPassword(data) {
    return ajax('/api/apply-reset-password', 'post', {
      data
    })
  },
  resetPassword(data) {
    return ajax('/api/reset-password', 'post', {
      data
    })
  },
  // Problem List椤电殑鐩稿叧璇锋眰
  getProblemTagList(oj) {
    return ajax('/api/get-tag-list', 'get')
  },

  getProblemTagsAndClassification(oj) {
    return ajax('/api/get-tag-list', 'get')
  },

  getProblemList(searchParams) {
    let params = {}
    Object.keys(searchParams).forEach((element) => {
      if (searchParams[element] !== '' && searchParams[element] !== null && searchParams[element] !== undefined) {
        params[element] = searchParams[element]
      }
    })
    return ajax('/api/get-problem-list', 'get', {
      params: params
    })
  },

  // 鏌ヨ褰撳墠鐧诲綍鐢ㄦ埛瀵归鐩殑鎻愪氦鐘舵€?
  getUserProblemStatus(pidList, isContestProblemList, cid, gid, containsEnd = false) {
    return ajax("/api/get-user-problem-status", 'post', {
      data: {
        pidList,
        isContestProblemList,
        cid,
        gid,
        containsEnd
      }
    })
  },
  // 闅忔満鏉ヤ竴棰?
  pickone() {
    return ajax('/api/get-random-problem', 'get')
  },

  // Problem璇︽儏椤电殑鐩稿叧璇锋眰
  getProblem(problemId, cid, gid) {
    return ajax('/api/get-problem-detail', 'get', {
      params: {
        pid: problemId
      }
    })
  },

  // 鑾峰彇棰樼洰浠ｇ爜妯℃澘
  getProblemCodeTemplate(pid) {
    return ajax('/api/get-problem-code-template', 'get', {
      params: {
        pid
      }
    })
  },

  // 鎻愪氦璇勬祴妯″潡
  submitCode(data) {
    return ajax('/api/submit-problem-judge', 'post', {
      data
    })
  },
  // 鑾峰彇鍗曚釜鎻愪氦鐨勪俊鎭?
  getSubmission(submitId) {
    return ajax('/api/get-submission-detail', 'get', {
      params: {
        submitId
      }
    })
  },
  // 鍦ㄧ嚎璋冭瘯
  submitTestJudge(data) {
    return ajax('/api/submit-problem-test-judge', 'post', {
      data
    })
  },
  // 鑾峰彇璋冭瘯缁撴灉
  getTestJudgeResult(testJudgeKey) {
    return ajax('/api/get-test-judge-result', 'get', {
      params: {
        testJudgeKey
      }
    })
  },
  // 鑾峰彇鏈€杩戜竴娆￠€氳繃鐨勪唬鐮?
  getUserLastAccepetedCode(pid, cid){
    let params = {
      pid
    }
    if(cid){
      params.cid = cid
    }
    return ajax('/api/get-last-ac-code', 'get', {
      params: params
    })
  },
  // 鑾峰彇棰樼洰涓撴敞妯″紡搴曢儴棰樼洰鍒楄〃
  getFullScreenProblemList(tid, cid){
    let params = {tid, cid}
    return ajax('/api/get-full-screen-problem-list', 'get', {
      params: params
    })
  },
  // 鑾峰彇鍗曚釜鎻愪氦鐨勫叏閮ㄦ祴璇曠偣璇︽儏
  getAllCaseResult(submitId) {
    return ajax('/api/get-all-case-result', 'get', {
      params: {
        submitId,
      }
    })
  },
  // 杩滅▼铏氭嫙鍒ら澶辫触杩涜閲嶆柊鎻愪氦
  reSubmitRemoteJudge(submitId) {
    return ajax("/api/resubmit", 'get', {
      params: {
        submitId,
      }
    })
  },
  // 鏇存柊鎻愪氦璇︽儏
  updateSubmission(data) {
    return ajax('/api/submission', 'put', {
      data
    })
  },
  getSubmissionList(limit, params) {
    params.limit = limit
    return ajax('/api/get-submission-list', 'get', {
      params
    })
  },
  checkSubmissonsStatus(submitIds, cid) {
    return ajax('/api/check-submissions-status', 'post', {
      data: { submitIds, cid }
    })
  },
  checkContestSubmissonsStatus(submitIds, cid) {
    return ajax('/api/check-contest-submissions-status', 'post', {
      data: { submitIds, cid }
    })
  },

  submissionRejudge(submitId) {
    return ajax('/api/admin/judge/rejudge', 'get', {
      params: {
        submitId
      }
    })
  },

  admin_manualJudge(submitId, status, score) {
    return ajax('/api/admin/judge/manual-judge', 'get', {
      params: {
        submitId,
        status,
        score
      }
    })
  },

  admin_cancelJudge(submitId) {
    return ajax('/api/admin/judge/cancel-judge', 'get', {
      params: {
        submitId
      }
    })
  },

  // ------------------------------------璁粌妯″潡鐨勮姹?--------------------------------------------

  // 鑾峰彇璁粌鍒嗙被鍒楄〃
  getTrainingCategoryList() {
    return ajax('/api/get-training-category', 'get')
  },


  // 鑾峰彇璁粌鍒楄〃
  getTrainingList(currentPage, limit, query) {
    let params = {
      currentPage,
      limit
    }
    if (query !== undefined) {
      Object.keys(query).forEach((element) => {
        if (query[element]) {
          params[element] = query[element]
        }
      })
    }
    return ajax('/api/get-training-list', 'get', {
      params: params
    })
  },

  // 鑾峰彇璁粌璇︽儏
  getTraining(tid) {
    return ajax('/api/get-training-detail', 'get', {
      params: { tid }
    })
  },
  // 娉ㄥ唽绉佹湁璁粌
  registerTraining(tid, password) {
    return ajax('/api/register-training', 'post', {
      data: {
        tid,
        password
      }
    })
  },
  // 鑾峰彇娉ㄥ唽璁粌鏉冮檺
  getTrainingAccess(tid) {
    return ajax('/api/get-training-access', 'get', {
      params: { tid }
    })
  },
  // 鑾峰彇璁粌棰樼洰鍒楄〃
  getTrainingProblemList(tid) {
    return ajax('/api/get-training-problem-list', 'get', {
      params: { tid }
    })
  },
  // 鑾峰彇璁粌棰樼洰璇︽儏
  getTrainingProblem(displayId, cid) {
    return ajax('/api/get-training-problem-details', 'get', {
      params: { displayId, cid }
    })
  },
  // 鑾峰彇璁粌璁板綍姒滃崟
  getTrainingRank(params) {
    return ajax('/api/get-training-rank', 'get', {
      params
    })
  },



  // ------------------------------------------------------------------------------------------------


  // 姣旇禌鍒楄〃椤电殑璇锋眰
  getContestList(currentPage, limit, query) {
    let params = {
      currentPage,
      limit
    }
    if (query !== undefined) {
      Object.keys(query).forEach((element) => {
        if (query[element] !== null && query[element] !== '' && query[element] !== undefined) {
          params[element] = query[element]
        }
      })
    }
    return ajax('/api/get-contest-list', 'get', {
      params: params
    })
  },

  // 姣旇禌璇︽儏鐨勮姹?
  getContest(cid) {
    return ajax('/api/get-contest-info', 'get', {
      params: { cid }
    })
  },
  // 鑾峰彇璧涘姒滃崟姣旇禌鐨勪俊鎭?
  getScoreBoardContestInfo(cid) {
    return ajax('/api/get-contest-outsize-info', 'get', {
      params: { cid }
    })
  },
  // 鎻愪緵姣旇禌澶栨鎺掑悕鏁版嵁
  getContestOutsideScoreboard(data) {
    return ajax('/api/get-contest-outside-scoreboard', 'post', {
      data
    })
  },
  // 娉ㄥ唽绉佹湁姣旇禌鏉冮檺
  registerContest(cid, password) {
    return ajax('/api/register-contest', 'post', {
      data: {
        cid,
        password
      }
    })
  },
  // 鑾峰彇娉ㄥ唽姣旇禌鏉冮檺
  getContestAccess(cid) {
    return ajax('/api/get-contest-access', 'get', {
      params: { cid }
    })
  },
  // 鑾峰彇姣旇禌棰樼洰鍒楄〃
  getContestProblemList(cid, containsEnd = false) {
    return ajax('/api/get-contest-problem', 'get', {
      params: { cid, containsEnd }
    })
  },
  // 鑾峰彇姣旇禌棰樼洰璇︽儏
  getContestProblem(displayId, cid, gid, containsEnd = false) {
    return ajax('/api/get-contest-problem-details', 'get', {
      params: { displayId, cid, containsEnd}
    })
  },
  // 鑾峰彇姣旇禌鎻愪氦鍒楄〃
  getContestSubmissionList(limit, params) {
    params.limit = limit
    return ajax('/api/contest-submissions', 'get', {
      params
    })
  },

  getContestRank(data) {
    return ajax('/api/get-contest-rank', 'post', {
      data
    })
  },

  // 鑾峰彇姣旇禌鍏憡鍒楄〃
  getContestAnnouncementList(currentPage, limit, cid) {
    let params = {
      currentPage,
      limit,
      cid
    }
    return ajax('/api/get-contest-announcement', 'get', {
      params
    })
  },

  // 鑾峰彇姣旇禌鏈槄璇诲叕鍛婂垪琛?
  getContestUserNotReadAnnouncement(data) {
    return ajax('/api/get-contest-not-read-announcement', 'post', {
      data
    })
  },

  // 鑾峰彇acm姣旇禌ac淇℃伅
  getACMACInfo(params) {
    return ajax('/api/get-contest-ac-info', 'get', {
      params
    })
  },
  // 纭ac淇℃伅
  updateACInfoCheckedStatus(data) {
    return ajax('/api/check-contest-ac-info', 'put', {
      data
    })
  },

  // 鎻愪氦鎵撳嵃鏂囨湰
  submitPrintText(data) {
    return ajax('/api/submit-print-text', 'post', {
      data
    })
  },

  // 鑾峰彇姣旇禌鎵撳嵃鏂囨湰鍒楄〃
  getContestPrintList(params) {
    return ajax('/api/get-contest-print', 'get', {
      params
    })
  },

  // 鏇存柊姣旇禌鎵撳嵃鐨勭姸鎬?
  updateContestPrintStatus(params) {
    return ajax('/api/check-contest-print-status', 'put', {
      params
    })
  },


  // 姣旇禌棰樼洰瀵瑰簲鐨勬彁浜ら噸鍒?
  ContestRejudgeProblem(params) {
    return ajax('/api/admin/judge/rejudge-contest-problem', 'get', {
      params
    })
  },

  // ACM璧涘埗鎴朞I璧涘埗鐨勬帓琛屾
  getUserRank(currentPage, limit, type, searchUser) {
    return ajax('/api/get-rank-list', 'get', {
      params: {
        currentPage,
        limit,
        type,
        searchUser
      }
    })
  },

  // about椤甸儴鍒嗚姹?
  getAllLanguages(all) {
    return ajax("/api/get-languages", 'get', {
      params: {
        all
      }
    })
  },
  // userhome椤电殑璇锋眰
  getUserInfo(uid, username) {
    return ajax("/api/get-user-home-info", 'get', {
      params: { uid, username }
    })
  },

  getUserCalendarHeatmap(uid, username) {
    return ajax("/api/get-user-calendar-heatmap", 'get', {
      params: { uid, username }
    })
  },

  // setting椤电殑璇锋眰
  changePassword(data) {
    return ajax("/api/change-password", 'post', {
      data
    })
  },
  getChangeEmailCode(email) {
    return ajax("/api/get-change-email-code", 'get',  {
      params: { email }
    })
  },
  changeEmail(data) {
    return ajax("/api/change-email", 'post', {
      data
    })
  },
  changeUserInfo(data) {
    return ajax("/api/change-userInfo", 'post', {
      data
    })
  },

  // 璁ㄨ/缇ょ粍/娑堟伅宸插垹闄わ紙PRD鐮嶆帀锛?

}

// 澶勭悊admin鍚庡彴绠＄悊鐨勮姹?
const adminApi = {
  // 鐧诲綍锛堢鐞嗙澶嶇敤 /api/login锛?
  admin_login(username, password) {
    return ajax('/api/login', 'post', {
      data: {
        username,
        password
      }
    })
  },
  admin_logout() {
    return ajax('/api/logout', 'get')
  },
  admin_getDashboardInfo() {
    return ajax('/api/admin/dashboard/get-dashboard-info', 'get')
  },
  getSessions(data) {
    return ajax('/api/admin/dashboard/get-sessions', 'post', {
      data
    })
  },
  //鑾峰彇鏁版嵁鍚庡彴鏈嶅姟鍜宯acos鐩稿叧璇︽儏
  admin_getGeneralSystemInfo() {
    return ajax('/api/admin/config/get-service-info', 'get')
  },

  getJudgeServer() {
    return ajax('/api/admin/config/get-judge-service-info', 'get')
  },

  // 鑾峰彇鐢ㄦ埛鍒楄〃
  admin_getUserList(currentPage, limit, keyword, onlyAdmin) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    params.onlyAdmin = onlyAdmin
    return ajax('/api/admin/user/get-user-list', 'get', {
      params: params
    })
  },
  // 缂栬緫鐢ㄦ埛
  admin_editUser(data) {
    return ajax('/api/admin/user/edit-user', 'put', {
      data
    })
  },
  admin_deleteUsers(ids) {
    return ajax('/api/admin/user/delete-user', 'delete', {
      data: { ids }
    })
  },
  admin_importUsers(users) {
    return ajax('/api/admin/user/insert-batch-user', 'post', {
      data: {
        users
      }
    })
  },
  admin_generateUser(data) {
    return ajax('/api/admin/user/generate-user', 'post', {
      data
    })
  },
  // 鑾峰彇鍏憡鍒楄〃
  admin_getAnnouncementList(currentPage, limit) {
    return ajax('/api/admin/announcement', 'get', {
      params: {
        currentPage,
        limit
      }
    })
  },
  // 鍒犻櫎鍏憡
  admin_deleteAnnouncement(aid) {
    return ajax('/api/admin/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  // 淇敼鍏憡
  admin_updateAnnouncement(data) {
    return ajax('/api/admin/announcement', 'put', {
      data
    })
  },
  // 娣诲姞鍏憡
  admin_createAnnouncement(data) {
    return ajax('/api/admin/announcement', 'post', {
      data
    })
  },


  // 鑾峰彇鍏憡鍒楄〃
  admin_getNoticeList(currentPage, limit, type) {
    return ajax('/api/admin/msg/notice', 'get', {
      params: {
        currentPage,
        limit,
        type
      }
    })
  },
  // 鍒犻櫎鍏憡
  admin_deleteNotice(id) {
    return ajax('/api/admin/msg/notice', 'delete', {
      params: {
        id
      }
    })
  },
  // 淇敼鍏憡
  admin_updateNotice(data) {
    return ajax('/api/admin/msg/notice', 'put', {
      data
    })
  },
  // 娣诲姞鍏憡
  admin_createNotice(data) {
    return ajax('/api/admin/msg/notice', 'post', {
      data
    })
  },

  // 绯荤粺閰嶇疆
  admin_getSMTPConfig() {
    return ajax('/api/admin/config/get-email-config', 'get')
  },
  admin_editSMTPConfig(data) {
    return ajax('/api/admin/config/set-email-config', 'put', {
      data
    })
  },

  admin_deleteHomeCarousel(id) {
    return ajax('/api/admin/config/home-carousel', 'delete', {
      params: {
        id
      }
    })
  },

  admin_testSMTPConfig(email) {
    return ajax('/api/admin/config/test-email', 'post', {
      data: {
        email
      }
    })
  },
  admin_getWebsiteConfig() {
    return ajax('/api/admin/config/get-web-config', 'get')
  },
  admin_editWebsiteConfig(data) {
    return ajax('/api/admin/config/set-web-config', 'put', {
      data
    })
  },
  admin_getDataBaseConfig() {
    return ajax('/api/admin/config/get-db-and-redis-config', 'get')
  },
  admin_editDataBaseConfig(data) {
    return ajax('/api/admin/config/set-db-and-redis-config', 'put', {
      data
    })
  },

  // 绯荤粺寮€鍏?
  admin_getSwitchConfig() {
    return ajax('/api/admin/switch/info', 'get')
  },

  admin_saveSwitchConfig(data) {
    return ajax('/api/admin/switch/update', 'put', {
      data
    })
  },

  getLanguages(pid, all) {
    return ajax('/api/get-languages', 'get', {
      params: {
        pid,
        all
      }
    })
  },
  getProblemLanguages(pid) {
    return ajax('/api/get-problem-languages', 'get', {
      params: {
        pid: pid
      }
    })
  },

  admin_getProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/problem/get-problem-list', 'get', {
      params
    })
  },

  admin_addRemoteOJProblem(name, problemId) {
    return ajax("/api/admin/problem/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId
      }
    })
  },

  admin_addContestRemoteOJProblem(name, problemId, cid, displayId) {
    return ajax("/api/admin/contest/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId,
        cid,
        displayId
      }
    })
  },

  admin_createProblem(data) {
    return ajax('/api/admin/problem', 'post', {
      data
    })
  },
  admin_editProblem(data) {
    return ajax('/api/admin/problem', 'put', {
      data
    })
  },
  admin_deleteProblem(pid) {
    return ajax('/api/admin/problem', 'delete', {
      params: {
        pid
      }
    })
  },
  admin_changeProblemAuth(data) {
    return ajax('/api/admin/problem/change-problem-auth', 'put', {
      data
    })
  },
  admin_getProblem(pid) {
    return ajax('/api/admin/problem', 'get', {
      params: {
        pid
      }
    })
  },
  admin_getAllProblemTagList(oj) {
    return ajax('/api/admin/tag/list', 'get')
  },

  downloadTestCase(pid) {
    return ajax('/api/file/download-testcase', 'get', {
      params: { pid }
    })
  },

  admin_getProblemTags(pid) {
    return ajax('/api/get-problem-tags', 'get', {
      params: {
        pid
      }
    })
  },
  admin_getProblemCases(pid, isUpload) {
    return ajax('/api/admin/problem/get-problem-cases', 'get', {
      params: {
        pid,
        isUpload
      }
    })
  },
  compileSPJ(data) {
    return ajax('/api/admin/problem/compile-spj', 'post', {
      data
    })
  },
  compileInteractive(data) {
    return ajax('/api/admin/problem/compile-interactive', 'post', {
      data
    })
  },

  admin_addTag(data) {
    return ajax('/api/admin/tag', 'post', {
      data
    })
  },

  admin_updateTag(data) {
    return ajax('/api/admin/tag', 'put', {
      data
    })
  },

  admin_deleteTag(tid) {
    return ajax('/api/admin/tag', 'delete', {
      params: {
        tid
      }
    })
  },

  admin_getTagClassification(oj) {
    return ajax('/api/admin/tag/classification', 'get', {
      params: {
        oj
      }
    })
  },

  admin_addTagClassification(data) {
    return ajax('/api/admin/tag/classification', 'post', {
      data
    })
  },

  admin_updateTagClassification(data) {
    return ajax('/api/admin/tag/classification', 'put', {
      data
    })
  },

  admin_deleteTagClassification(tcid) {
    return ajax('/api/admin/tag/classification', 'delete', {
      params: {
        tcid
      }
    })
  },

  admin_getGroupApplyProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/group-problem/list', 'get', {
      params
    })
  },

  admin_changeGroupProblemApplyProgress(data) {
    return ajax('/api/admin/group-problem/change-progress', 'put', {
      data
    })
  },

  admin_getTrainingList(currentPage, limit, keyword) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/api/admin/training/get-training-list', 'get', {
      params: params
    })
  },
  admin_changeTrainingStatus(tid, status, author) {
    return ajax('/api/admin/training/change-training-status', 'put', {
      params: {
        tid,
        status,
        author
      }
    })
  },

  admin_getTrainingProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/training/get-problem-list', 'get', {
      params
    })
  },

  admin_deleteTrainingProblem(pid, tid) {
    return ajax('/api/admin/training/problem', 'delete', {
      params: {
        pid,
        tid
      }
    })
  },

  admin_addTrainingProblemFromPublic(data) {
    return ajax('/api/admin/training/add-problem-from-public', 'post', {
      data
    })
  },

  admin_addTrainingRemoteOJProblem(name, problemId, tid) {
    return ajax("/api/admin/training/import-remote-oj-problem", "get", {
      params: {
        name,
        problemId,
        tid,
      }
    })
  },

  admin_updateTrainingProblem(data) {
    return ajax('/api/admin/training/problem', 'put', {
      data
    })
  },

  admin_createTraining(data) {
    return ajax('/api/admin/training', 'post', {
      data
    })
  },
  admin_getTraining(tid) {
    return ajax('/api/admin/training', 'get', {
      params: {
        tid
      }
    })
  },
  admin_editTraining(data) {
    return ajax('/api/admin/training', 'put', {
      data
    })
  },
  admin_deleteTraining(tid) {
    return ajax('/api/admin/training', 'delete', {
      params: {
        tid
      }
    })
  },

  admin_addCategory(data) {
    return ajax('/api/admin/training/category', 'post', {
      data
    })
  },

  admin_updateCategory(data) {
    return ajax('/api/admin/training/category', 'put', {
      data
    })
  },

  admin_deleteCategory(cid) {
    return ajax('/api/admin/training/category', 'delete', {
      params: {
        cid
      }
    })
  },


  admin_getContestProblemInfo(pid, cid) {
    return ajax('/api/admin/contest/contest-problem', 'get', {
      params: {
        cid,
        pid
      }
    })
  },
  admin_setContestProblemInfo(data) {
    return ajax('/api/admin/contest/contest-problem', 'put', {
      data
    })
  },

  admin_getContestProblemList(params) {
    params = utils.filterEmptyValue(params)
    return ajax('/api/admin/contest/get-problem-list', 'get', {
      params
    })
  },

  admin_getContestProblem(pid) {
    return ajax('/api/admin/contest/problem', 'get', {
      params: {
        pid,
      }
    })
  },
  admin_createContestProblem(data) {
    return ajax('/api/admin/contest/problem', 'post', {
      data
    })
  },
  admin_editContestProblem(data) {
    return ajax('/api/admin/contest/problem', 'put', {
      data
    })
  },
  admin_deleteContestProblem(pid, cid) {
    return ajax('/api/admin/contest/problem', 'delete', {
      params: {
        pid,
        cid
      }
    })
  },
  admin_addContestProblemFromPublic(data) {
    return ajax('/api/admin/contest/add-problem-from-public', 'post', {
      data
    })
  },

  exportProblems(data) {
    return ajax('export_problem', 'post', {
      data
    })
  },

  admin_createContest(data) {
    return ajax('/api/admin/contest', 'post', {
      data
    })
  },
  admin_getContest(cid) {
    return ajax('/api/admin/contest', 'get', {
      params: {
        cid
      }
    })
  },
  admin_editContest(data) {
    return ajax('/api/admin/contest', 'put', {
      data
    })
  },
  admin_deleteContest(cid) {
    return ajax('/api/admin/contest', 'delete', {
      params: {
        cid
      }
    })
  },
  admin_changeContestVisible(cid, visible, uid) {
    return ajax('/api/admin/contest/change-contest-visible', 'put', {
      params: {
        cid,
        visible,
        uid
      }
    })
  },
  admin_getContestList(currentPage, limit, keyword) {
    let params = { currentPage, limit }
    if (keyword) {
      params.keyword = keyword
    }
    return ajax('/api/admin/contest/get-contest-list', 'get', {
      params: params
    })
  },
  admin_getContestAnnouncementList(cid, currentPage, limit) {
    return ajax('/api/admin/contest/announcement', 'get', {
      params: {
        cid,
        currentPage,
        limit
      }
    })
  },
  admin_createContestAnnouncement(data) {
    return ajax('/api/admin/contest/announcement', 'post', {
      data
    })
  },
  admin_deleteContestAnnouncement(aid) {
    return ajax('/api/admin/contest/announcement', 'delete', {
      params: {
        aid
      }
    })
  },
  admin_updateContestAnnouncement(data) {
    return ajax('/api/admin/contest/announcement', 'put', {
      data
    })
  },

  admin_updateDiscussion(data) {
    return ajax("/api/admin/discussion", 'put', {
      data
    })
  },

  admin_deleteDiscussion(data) {
    return ajax("/api/admin/discussion", 'delete', {
      data
    })
  },
  admin_getDiscussionReport(currentPage, limit) {
    return ajax("/api/admin/discussion-report", 'get', {
      params: {
        currentPage,
        limit
      }
    })
  },
  admin_updateDiscussionReport(data) {
    return ajax("/api/admin/discussion-report", 'put', {
      data
    })
  }
}

// 闆嗕腑瀵煎嚭oj鍓嶅彴鐨刟pi鍜宎dmin绠＄悊绔殑api
let api = Object.assign(ojApi, adminApi)
export default api
/**
 * @param url
 * @param method get|post|put|delete...
 * @param params like queryString. if a url is index?a=1&b=2, params = {a: '1', b: '2'}
 * @param data post data, use for method put|post
 * @returns {axios}
 */
function ajax(url, method, options) {
  if (options !== undefined) {
    var { params = {}, data = {} } = options
  } else {
    params = data = {}
  }
  return new Promise((resolve, reject) => {
    axios({
      url,
      method,
      params,
      data
    }).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}





import { createStore } from 'vuex'

declare let SessionStorage: any;
const ADMIN = "ADMIN";

const store = createStore({
  state: {
    admin: SessionStorage.get(ADMIN) || {}
  },
  mutations: {
    setUser (state, admin) {
      console.log("store user：", admin);
      state.admin = admin;
      SessionStorage.set(ADMIN, admin);
    }
  },
  actions: {
  },
  modules: {
  }
});

export default store;
